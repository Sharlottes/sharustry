package Sharustry.entities.bullet;

import Sharustry.world.blocks.defense.turret.DriverBulletData;
import arc.graphics.*;
import arc.math.*;
import arc.util.Tmp;
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

        Tmp.v1.set(data.from.mounts.get(data.fromIndex).x, data.from.mounts.get(data.fromIndex).y);
        Tmp.v2.set(data.to.mounts.get(data.toIndex).x, data.to.mounts.get(data.toIndex).y);

        float baseDst = Tmp.v1.dst(Tmp.v2);
        float dst1 = b.dst(Tmp.v1);
        float dst2 = b.dst(Tmp.v2);

        boolean intersect = false;

        //bullet has gone past the destination point: but did it intersect it?
        if(dst1 > baseDst){
            float angleTo = b.angleTo(Tmp.v2);
            float baseAngle = Tmp.v2.angleTo(Tmp.v1);

            //if angles are nearby, then yes, it did
            if(Angles.near(angleTo, baseAngle, 2f)){
                intersect = true;
                //snap bullet position back; this is used for low-FPS situations
                b.set(Tmp.v2.x + Angles.trnsx(baseAngle, hitDst), Tmp.v2.y + Angles.trnsy(baseAngle, hitDst));
            }
        }
        //if on course, and it's in range of the target
        if(Math.abs(dst1 + dst2 - baseDst) < 4f && dst2 <= hitDst){
            intersect = true;
        } //else, bullet has gone off course, does not get received.

        if(intersect) data.to.handlePayload(b, data);
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