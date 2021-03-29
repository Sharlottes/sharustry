package Sharustry.entities.bullet;

import Sharustry.world.blocks.defense.DriverBulletData;
import Sharustry.world.blocks.defense.MultiTurret;
import arc.graphics.*;
import arc.math.*;
import mindustry.content.*;
import mindustry.entities.bullet.MassDriverBolt;
import mindustry.gen.*;

import static mindustry.Vars.*;

public class MountDriverBolt extends MassDriverBolt {
    @Override
    public void update(Bullet b){
        //data MUST be an instance of DriverBulletData
        if(!(b.data() instanceof DriverBulletData)){
            hit(b);
            return;
        }
        DriverBulletData data = ((DriverBulletData) b.data());

        float hitDst = 7f;

        //if the target is dead, just keep flying until the bullet explodes
        if(data.to.dead()){
            return;
        }

        float baseDst = data.from.dst(data.to);
        float dst1 = b.dst(data.from);
        float dst2 = b.dst(data.to);

        boolean intersect = false;

        //bullet has gone past the destination point: but did it intersect it?
        if(dst1 > baseDst){
            float angleTo = b.angleTo(data.to);
            float baseAngle = data.to.angleTo(data.from);

            //if angles are nearby, then yes, it did
            if(Angles.near(angleTo, baseAngle, 2f)){
                intersect = true;
                //snap bullet position back; this is used for low-FPS situations
                b.set(data.to.x + Angles.trnsx(baseAngle, hitDst), data.to.y + Angles.trnsy(baseAngle, hitDst));
            }
        }

        //if on course and it's in range of the target
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

        if(!(b.data() instanceof DriverBulletData)) return;
        DriverBulletData data = ((DriverBulletData) b.data());

        for(int i = 0; i < data.items.length; i++){
            int amountDropped = Mathf.random(0, data.items[i]);
            if(amountDropped > 0){
                float angle = b.rotation() + Mathf.range(100f);
                Fx.dropItem.at(b.x, b.y, angle, Color.white, content.item(i));
            }
        }
    }
}
