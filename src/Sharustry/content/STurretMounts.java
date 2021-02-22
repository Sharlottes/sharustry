package Sharustry.content;

import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.ctype.ContentList;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.entities.bullet.LightningBulletType;
import mindustry.gen.Sounds;
import Sharustry.world.blocks.defense.MultiTurretMount;
import mindustry.graphics.Pal;
import mindustry.io.JsonIO;

import static Sharustry.content.SBullets.*;

public class STurretMounts implements ContentList {
    public static MultiTurretMount laserMount, arcMount, unoMount, hailMount, waveMount;


    @Override
    public void load() {
        laserMount = new MultiTurretMount("laserM", miniAccelMissile){{
            title = "Laser";
            barrels = 2;
            spread = 4f;
            shots = 2;
            reloadTime = 20f;
            restitution = 0.03f;
            range = 100;
            shootCone = 15f;
            inaccuracy = 2f;
            rotateSpeed = 10f;
            powerUse = 6.3f;
            ammoType = MountAmmoType.power;
        }};

        arcMount = new MultiTurretMount("arcM",
            new LightningBulletType(){{
                damage = 20;
                lightningLength = 25;
                collidesAir = false;
            }}
        ){{
            title = "Arc";
            x = -4f;
            y = -4.75f;
            reloadTime = 35f;
            shootCone = 40f;
            rotateSpeed = 8f;
            powerUse = 3.3f;
            targetAir = false;
            range = 90f;
            shootEffect = Fx.lightningShoot;
            heatColor = Color.red;
            recoilAmount = 1f;
            shootSound = Sounds.spark;

            ammoType = MountAmmoType.power;
        }};


        unoMount = new MultiTurretMount("unoM",
            new LaserBulletType(140){{
                colors = new Color[]{Pal.lancerLaser.cpy().a(0.4f), Pal.lancerLaser, Color.white};
                hitEffect = Fx.hitLancer;
                despawnEffect = Fx.none;
                hitSize = 4;
                lifetime = 16f;
                drawSize = 400f;
                collidesAir = false;
                length = 173f;
            }}
        ){{
           ammoPerShot = 5;
           x = 2.75f;
           y = 2.75f;
           shootY = 13/4f;
           range = 9 * 8;
           title = "Uno";

            recoilAmount = 2f;
            reloadTime = 80f;
            cooldown = 0.03f;
            shootEffect = Fx.lancerLaserShoot;
            smokeEffect = Fx.none;
            chargeEffect = Fx.lancerLaserCharge;
            chargeBeginEffect = Fx.lancerLaserChargeBegin;
            chargeTime = 40f;
            chargeMaxDelay = 30f;
            chargeEffects = 7;
            targetAir = false;
            shootSound = Sounds.laser;
            powerUse = 6f;
            maxAmmo = 30;

            ammoType = MountAmmoType.power;
        }};


        hailMount = new MultiTurretMount("hailM"){{
            targetAir = false;
            reloadTime = 60;
            ammoPerShot = 5;
            x = -3.75f;
            y = -4f;
            shootY = 18/4f;
            recoilAmount = 2.5f;
            range = 18 * 8;
            title = "Mini Hail";
            shootSound = Sounds.bang;

            maxAmmo = 50;

            ammoType = MountAmmoType.item;
        }};

        waveMount = new MultiTurretMount("waveM"){{
            targetAir = false;
            reloadTime = 3;
            x = 4.25f;
            y = -3.5f;
            shootY = 16/4f;
            recoilAmount = 1;
            range = 13 * 8;
            title = "Mini Wave";
            shootSound = Sounds.none;
            loopSound = Sounds.spray;
            ammoPerShot = 5;
            maxAmmo = 100;
            extinguish = true;
            ammoType = MountAmmoType.liquid;
        }};
    }
}
