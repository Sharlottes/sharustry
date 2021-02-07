package Sharustry.content;

import Sharustry.graphics.SPal;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.content.*;

import Sharustry.entities.bullet.*;
import mindustry.graphics.Pal;

public class SBullets implements ContentList{
    public static BulletType accelMissile, testLaser;

    @Override
    public void load(){
        accelMissile = new AccelBulletType(2.5f, 30){{
            backColor = SPal.cryoium.cpy().mul(Items.titanium.color);
            frontColor = trailColor = SPal.cryoium;
            shrinkY = 0f;
            width = 4f;
            height = 16f;
            hitSound = Sounds.explosion;
            trailChance = 0.2f;
            lifetime = 49f;
            sprite = "bullet";
            accelScl = 0.25f;
            pierceBuilding = true;
            pierceCap = 10;
            maxDamage = damage*10;
            shootEffect = SFx.balkanShoot;
        }};

        testLaser = new ContinuousLaserBulletType(70){{
            length = 200f;
            hitEffect = Fx.hitMeltdown;
            hitColor = Pal.meltdownHit;
            drawSize = 420f;

            incendChance = 0.4f;
            incendSpread = 5f;
            incendAmount = 1;
        }};
    }
}
