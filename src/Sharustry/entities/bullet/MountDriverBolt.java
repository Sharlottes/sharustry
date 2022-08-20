package Sharustry.entities.bullet;

import Sharustry.world.blocks.defense.turret.DriverBulletData;
import Sharustry.world.blocks.defense.turret.MultiTurret;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.Position;
import mindustry.content.*;
import mindustry.entities.bullet.MassDriverBolt;
import mindustry.gen.*;

import static mindustry.Vars.*;

public class MountDriverBolt extends MassDriverBolt {
    @Override
    public void update(Bullet b){
        //data MUST be an instance of DriverBulletData
        if(!(b.data() instanceof DriverBulletData data)){
            hit(b);
            return;
        }
        float hitDst = 7f;

        //if the target is dead, just keep flying until the bullet explodes
        if(data.to.dead()) return;

        Position fromM = new Position() {
            @Override
            public float getX() {
                if(data.from instanceof MultiTurret.MultiTurretBuild)
                    return ((MultiTurret.MultiTurretBuild)data.from).mounts.get(data.link).getMountLocation().x;

                return 0;
            }

            @Override
            public float getY() {
                if(data.from instanceof MultiTurret.MultiTurretBuild)
                    return ((MultiTurret.MultiTurretBuild)data.from).mounts.get(data.link).getMountLocation().y;

                return 0;
            }
        };

        Position toM = new Position() {
            @Override
            public float getX() {
                if(data.to instanceof MultiTurret.MultiTurretBuild)
                    return ((MultiTurret.MultiTurretBuild)data.to).mounts.get(data.link).getMountLocation().x;

                return 0;
            }

            @Override
            public float getY() {
                if(data.to instanceof MultiTurret.MultiTurretBuild)
                    return ((MultiTurret.MultiTurretBuild)data.to).mounts.get(data.link).getMountLocation().y;

                return 0;
            }
        };

        float baseDst = fromM.dst(toM);
        float dst1 = b.dst(fromM);
        float dst2 = b.dst(toM);

        boolean intersect = false;

        //bullet has gone past the destination point: but did it intersect it?
        if(dst1 > baseDst){
            float angleTo = b.angleTo(toM);
            float baseAngle = toM.angleTo(fromM);

            //if angles are nearby, then yes, it did
            if(Angles.near(angleTo, baseAngle, 2f)){
                intersect = true;
                //snap bullet position back; this is used for low-FPS situations
                b.set(toM.getX() + Angles.trnsx(baseAngle, hitDst), toM.getY() + Angles.trnsy(baseAngle, hitDst));
            }
        }
        //if on course, and it's in range of the target
        if(Math.abs(dst1 + dst2 - baseDst) < 4f && dst2 <= hitDst){
            intersect = true;
        } //else, bullet has gone off course, does not get received.

        if(intersect){
            if(data.to instanceof MultiTurret.MultiTurretBuild) ((MultiTurret.MultiTurretBuild)data.to).handlePayload(b, data);
        }
    }

    @Override
    public void despawned(Bullet b){
        super.despawned(b);

        if(!(b.data() instanceof DriverBulletData data)) return;

        for(int i = 0; i < data.items.length; i++){
            int amountDropped = Mathf.random(0, data.items[i]);
            if(amountDropped > 0){
                float angle = b.rotation() + Mathf.range(100f);
                Fx.dropItem.at(b.x, b.y, angle, Color.white, content.item(i));
            }
        }
    }
}