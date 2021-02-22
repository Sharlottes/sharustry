package Sharustry.content;

import Sharustry.graphics.SPal;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.content.*;

import Sharustry.entities.bullet.*;
import mindustry.graphics.Pal;

public class SBullets implements ContentList{
    public static BulletType mainBullet, miniSlag, miniWater, miniCryo, miniOil, miniAccelMissile, accelMissile, testLaser;

    @Override
    public void load(){
        mainBullet = new BasicBulletType(){{
           ammoMultiplier = 3;
           speed = 2.5f;
           damage = 9;
           width = 5.5f;
           height = 7;
           lifetime = 60;
           shootEffect = Fx.shootSmall;
           smokeEffect = Fx.shootSmallSmoke;
        }};


        miniSlag = new LiquidBulletType(){{
            collidesAir = false;
            liquid = Liquids.slag;
            damage = 1;
            drag = 0.03f;
            puddleSize = 2;
            orbSize = 1;
        }};

        miniWater = new LiquidBulletType(){{
            collidesAir = false;
            liquid = Liquids.water;
            knockback = 0.25f;
            drag = 0.03f;
            puddleSize = 2;
            orbSize = 1;
        }};

        miniCryo = new LiquidBulletType(){{
            collidesAir = false;
            liquid = Liquids.cryofluid;
            drag = 0.03f;
            puddleSize = 2;
            orbSize = 1;
        }};

        miniOil = new LiquidBulletType(){{
            collidesAir = false;
            liquid = Liquids.oil;
            drag = 0.03f;
            puddleSize = 2;
            orbSize = 1;
        }};

        miniAccelMissile = new AccelBulletType(2f, 10){{
            backColor = SPal.cryoium.cpy().mul(Items.titanium.color);
            frontColor = trailColor = SPal.cryoium;
            shrinkY = 0f;
            width = 1.5f;
            height = 3.5f;
            hitSound = Sounds.explosion;
            trailChance = 0.45f;
            lifetime = 25f;
            sprite = "bullet";
            accelScl = 0.15f;
            pierceBuilding = true;
            pierceCap = 3;
            maxDamage = damage*4f;
        }};

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
