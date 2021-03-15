package Sharustry.content;

import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.ctype.ContentList;
import mindustry.entities.bullet.LaserBoltBulletType;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.entities.bullet.LightningBulletType;
import mindustry.gen.Sounds;
import Sharustry.world.blocks.defense.MultiTurretMount;
import mindustry.graphics.Pal;

import static Sharustry.content.SBullets.*;

public class STurretMounts implements ContentList {
    public static MultiTurretMount healMissileMountL, healMissileMountR, healBeamMountL, healBeamMountR, healLaserMount2, healLaserMount, healBeamMount, repairMount, pointMount, tractMount, laserMount, arcMount, unoMount, hailMount, waveMount;


    @Override
    public void load() {
        healBeamMountL = new MultiTurretMount("healBeamM2L", new LaserBoltBulletType(6.2f, 20){{
            lifetime = 55f;
            healPercent = 8.5f;
            collidesTeam = true;
            backColor = Pal.heal;
            frontColor = Color.white;
            width *= 1.5f;
            height *= 1.5f;
            fragBullet = new LaserBoltBulletType(2.5f, 3){{
                lifetime = 25f;
                healPercent = 2.15f;
                collidesTeam = true;
                backColor = Pal.heal;
                frontColor = Color.white;
            }};
            fragVelocityMin = 0.75f;
        }}){{
            healBlock = true;
            title = "healBeamLeft";

            shootCone = 40f;
            shots = 5;
            burstSpacing = 6.5f;
            range = 15 * 8f;
            shootSound = Sounds.lasershoot;
            reloadTime = 85f;
            recoilAmount = 2.5f;
            heatColor = Pal.turretHeat;
            powerUse = 2.5f;
            mountType = MultiTurretMountType.power;
        }};
        healBeamMountR = new MultiTurretMount("healBeamM2R", new LaserBoltBulletType(6.2f, 20){{
            lifetime = 55f;
            healPercent = 8.5f;
            collidesTeam = true;
            backColor = Pal.heal;
            frontColor = Color.white;
            width *= 1.5f;
            height *= 1.5f;
            fragBullet = new LaserBoltBulletType(2.5f, 3){{
                lifetime = 25f;
                healPercent = 2.15f;
                collidesTeam = true;
                backColor = Pal.heal;
                frontColor = Color.white;
            }};
            fragAngle = 45;
            fragBullets = 3;
            fragVelocityMin = 0.75f;
        }}){{
            healBlock = true;
            title = "healBeamRight";

            shootCone = 40f;
            shots = 5;
            burstSpacing = 6.5f;
            range = 15 * 8f;
            shootSound = Sounds.lasershoot;
            reloadTime = 85f;
            recoilAmount = 2.5f;
            heatColor = Pal.turretHeat;
            powerUse = 2.5f;
            mountType = MultiTurretMountType.power;
        }};


        healBeamMount = new MultiTurretMount("healBeamM", new LaserBoltBulletType(5.2f, 10){{
            lifetime = 35f;
            healPercent = 5.5f;
            collidesTeam = true;
            backColor = Pal.heal;
            frontColor = Color.white;
        }}){{
            healBlock = true;
            title = "healBeam";

            x = -4f;
            y = -4.75f;
            shootCone = 40f;
            shots = 3;
            burstSpacing = 5;
            range = 10 * 8f;
            shootSound = Sounds.lasershoot;
            reloadTime = 105f;
            recoilAmount = 1.5f;
            heatColor = Pal.turretHeat;
            powerUse = 1.25f;
            mountType = MultiTurretMountType.power;
        }};

        healLaserMount2 = new MultiTurretMount("healLaserM2"){{
            title = "healLaser";
            repairSpeed = 0.75f;
            repairRadius = 75f;
            powerUse = 3f;
            laserColor = Color.valueOf("e8ffd7");
            mountType = MultiTurretMountType.repair;
        }};

        healLaserMount = new MultiTurretMount("healLaserM"){{
            title = "healLaser";
            repairSpeed = 0.5f;
            repairRadius = 55f;
            powerUse = 1.5f;
            laserColor = Color.valueOf("e8ffd7");
            mountType = MultiTurretMountType.repair;
        }};

        repairMount = new MultiTurretMount("repairM"){{
            title = "medic";
            repairSpeed = 0.5f;
            repairRadius = 65f;
            powerUse = 1f;
            laserColor = Color.valueOf("e8ffd7");
            mountType = MultiTurretMountType.repair;
        }};

        pointMount = new MultiTurretMount("pointM"){{
            title = "PointDefender";
            powerUse = 8f;
            range = 180f;
            shootCone = 5f;
            shootLength = 5f;
            bulletDamage = 30f;
            reloadTime = 9f;
            mountType = MultiTurretMountType.point;
        }};

        tractMount = new MultiTurretMount("tractM"){{
            title = "Tractor";
            shootCone = 6f;
            shootLength = 5f;
            force = 24f;
            scaledForce = 7f;
            range = 230f;
            damage = 0.3f;
            rotateSpeed = 10;
            powerUse = 3f;
            mountType = MultiTurretMountType.tract;
        }};

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
            mountType = MultiTurretMountType.power;
        }};

        arcMount = new MultiTurretMount("arcM", new LightningBulletType(){{
            damage = 20;
            lightningLength = 25;
            collidesAir = false;
        }}){{
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

            mountType = MultiTurretMountType.power;
        }};


        unoMount = new MultiTurretMount("unoM", new LaserBulletType(140){{
            colors = new Color[]{Pal.lancerLaser.cpy().a(0.4f), Pal.lancerLaser, Color.white};
            hitEffect = Fx.hitLancer;
            despawnEffect = Fx.none;
            hitSize = 4;
            lifetime = 16f;
            drawSize = 400f;
            collidesAir = false;
            length = 173f;
        }}){{
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

            mountType = MultiTurretMountType.power;
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

            mountType = MultiTurretMountType.item;
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
            mountType = MultiTurretMountType.liquid;
        }};
    }
}
