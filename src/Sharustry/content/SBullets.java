package Sharustry.content;

import Sharustry.graphics.SPal;
import arc.graphics.Color;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.content.*;

import Sharustry.entities.bullet.*;
import mindustry.graphics.Pal;

public class SBullets implements ContentList{
    public static BulletType mountDriverBolt, artilleryHealBig, artilleryHeal, jumbleBullet, miniSlag, miniWater, miniCryo, miniOil, miniAccelMissile, miniAccelMissilePyra, accelMissile, testLaser;

    @Override
    public void load(){
        mountDriverBolt = new MountDriverBolt();

        artilleryHeal = new HealingBulletType(3.25f, 50){{
            sprite = "shar-construct";
            drag = 0.0125f;

            fragSpacing *= 2f;
            tractForce = 8f;
            tractScaledForce = 7f;
            shootLength = 5f;
            pointBulletDamage = 9f;
            repairSpeed = 0.1f;
            repairRange = 45f;
            tractRange = 40f;
            pointRange = 35f;
            pointHitEffect = Fx.shootHeal;
            knockback = 2f;
            lifetime = 4 * 60f;
            width = height = 9f;
            collidesTiles = false;
            splashDamageRadius = 35f * 0.75f;
            splashDamage = 45f;
            fragBullets = 10;
            backColor = Pal.plastaniumBack;
            frontColor = Pal.plastaniumFront;
            mixColorFrom = Pal.plastanium.cpy().lerp(Pal.heal, 0.5f);
            mixColorTo = Pal.heal;
            fragBulletType = new LaserBoltBulletType(4.2f, 10){{
                lifetime = 25f;
                healPercent = 5.5f;
                collidesTeam = true;
                backColor = Pal.heal;
                frontColor = Color.white;
            }};
        }};

        artilleryHealBig = new HealingBulletType(4.25f, 150){{
            sprite = "shar-construct";
            drag = 0.0125f;

            despawnEffect = Fx.healWave;
            tractForce = 24f;
            tractScaledForce = 14f;
            shootLength = 5f;
            pointBulletDamage = 30f;
            repairSpeed = 0.35f;
            repairRange = 85f;
            tractRange = 80f;
            pointRange = 70f;
            pointHitEffect = Fx.shootHeal;
            knockback = 5f;
            lifetime = 7 * 60f;
            width = height = 13f;
            collidesTiles = false;
            splashDamageRadius = 35f * 0.75f;
            splashDamage = 45f;
            fragBullets = 10;
            backColor = Pal.plastaniumBack;
            frontColor = Pal.plastaniumFront;
            mixColorFrom = Pal.plastanium.cpy().lerp(Pal.heal, 0.5f);
            mixColorTo = Pal.heal;
            fragBulletType = new LaserBoltBulletType(5.2f, 5){{
                lifetime = 35f;
                healPercent = 5.5f;
                collidesTeam = true;
                backColor = Pal.heal;
                frontColor = Color.white;
            }};
        }};

        jumbleBullet = new BasicBulletType(){{
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

        miniAccelMissilePyra = new AccelBulletType(2f, 10){{
            backColor = Pal.lighterOrange.cpy().mul(Items.pyratite.color);
            frontColor = trailColor = Pal.lightOrange;
            trailColors = Pal.lightOrange;
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
