package Sharustry.content;

import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.ctype.ContentList;
import mindustry.entities.bullet.LaserBoltBulletType;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.entities.bullet.LightningBulletType;
import mindustry.entities.bullet.MissileBulletType;
import mindustry.gen.Sounds;
import Sharustry.world.blocks.defense.MultiTurretMount;
import mindustry.graphics.Pal;

import static Sharustry.content.SBullets.*;

public class STurretMounts implements ContentList {
    public static MultiTurretMount drillMount, massMount, electricLaserMountL, electricLaserMountR, healMissileMountL, healMissileMountR, healBeamMountL, healBeamMountR, healLaserMount2, healLaserMount, healBeamMount, repairMount, pointMount, tractMount, laserMount, arcMount, unoMount, hailMount, waveMount;


    @Override
    public void load() {
        drillMount = new MultiTurretMount("DrillM"){{
            title = "Mini Miner";
            mountType = MultiTurretMountType.drill;

            laserWidth = 0.75f;
            shootSound = Sounds.minebeam;
            shootSoundVolume = 0.9f;

            minDrillTier = 0;
            maxDrillTier = 3;
            mineSpeed = 0.75f;
            laserOffset = 4f;
            shootCone = 6f;

            range = 60f;
        }};
        massMount = new MultiTurretMount("miniMassDriver"){{
            rotateSpeed = 0.04f;
            translation = 7f;
            minDistribute = 10;
            knockback = 4f;
            bulletSpeed = 5.5f;
            bulletLifetime = 200f;
            shootEffect = Fx.shootBig2;
            smokeEffect = Fx.shootBigSmoke2;
            receiveEffect = Fx.mineBig;
            shootSound = Sounds.shootBig;
            shake = 3f;
            powerUse = 3f;
            title = "Mini MassDriver";
            reloadTime = 200f;
            range = 440f;

            mountType = MultiTurretMountType.mass;
        }};
        electricLaserMountL = new MultiTurretMount("electLaserML", new LaserBulletType(140){{
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
            title = "Electric Laser Left";

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
            shootSound = Sounds.laser;
            powerUse = 6f;
            maxAmmo = 30;

            mountType = MultiTurretMountType.power;
        }};

        electricLaserMountR = new MultiTurretMount("electLaserMR", new LaserBulletType(140){{
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
            title = "Electric Laser Right";

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
            shootSound = Sounds.laser;
            powerUse = 6f;
            maxAmmo = 30;

            mountType = MultiTurretMountType.power;
        }};

        healMissileMountR = new MultiTurretMount("healMissileMR"){{
            healBlock = true;
            title = "HealMissileRight";
            reloadTime = 40f;
            shots = 3;
            inaccuracy = 5f;
            shootSound = Sounds.missile;

            recoilAmount = 2f;
            heatColor = Pal.turretHeat;
            powerUse = 2f;
            mountType = MultiTurretMountType.power;

            barrels = 5;

            bullet = new MissileBulletType(3.5f, 15){{
                despawnEffect = hitEffect = Fx.healWave;
                healPercent = 5;
                width = 8f;
                height = 8f;
                shrinkY = 0f;
                drag = -0.003f;
                homingRange = 60f;
                keepVelocity = false;
                splashDamageRadius = 25f;
                splashDamage = 16f;
                lifetime = 2 * 60f;
                trailColor = Pal.heal;
                backColor = Color.white;
                frontColor = Pal.heal;
                weaveScale = 6f;
                weaveMag = 1f;
            }};
        }};

        healMissileMountL = new MultiTurretMount("healMissileML"){{

            healBlock = true;
            title = "HealMissileLeft";
            reloadTime = 40f;
            shots = 3;
            inaccuracy = 5f;
            shootSound = Sounds.missile;

            recoilAmount = 2f;
            heatColor = Pal.turretHeat;
            powerUse = 2f;
            mountType = MultiTurretMountType.power;

            barrels = 5;

            bullet = new MissileBulletType(3.5f, 15){{
                despawnEffect = hitEffect = Fx.healWave;
                healPercent = 5;
                width = 8f;
                height = 8f;
                shrinkY = 0f;
                drag = -0.003f;
                homingRange = 60f;
                keepVelocity = false;
                splashDamageRadius = 25f;
                splashDamage = 16f;
                lifetime = 2 * 60f;
                trailColor = Pal.heal;
                backColor = Color.white;
                frontColor = Pal.heal;
                weaveScale = 6f;
                weaveMag = 1f;
            }};
        }};

        healBeamMountL = new MultiTurretMount("healBeamM2L", new LaserBoltBulletType(6.2f, 20){{
            lifetime = 55f;
            healPercent = 8.5f;
            collidesTeam = true;
            backColor = Pal.heal;
            frontColor = Color.white;
            width *= 0.75f;
            height *= 0.75f;
            fragBullet = new LaserBoltBulletType(5.2f, 3){{
                lifetime = 15f;
                healPercent = 2.15f;
                collidesTeam = true;
                backColor = Pal.heal;
                frontColor = Color.white;
            }};
            fragBullets = 2;
            fragVelocityMin = 0.75f;
        }}){{
            healBlock = true;
            title = "HealBeamLeft";

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
            width *= 0.75f;
            height *= 0.75f;
            fragBullet = new LaserBoltBulletType(5.2f, 3){{
                lifetime = 15f;
                healPercent = 2.15f;
                collidesTeam = true;
                backColor = Pal.heal;
                frontColor = Color.white;
            }};
            fragBullets = 2;
            fragVelocityMin = 0.75f;
        }}){{
            healBlock = true;
            title = "HealBeamRight";

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
            title = "HealBeam";

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
            title = "HealLaser";
            repairSpeed = 0.75f;
            repairRadius = 75f;
            powerUse = 3f;
            laserColor = Color.valueOf("e8ffd7");
            mountType = MultiTurretMountType.repair;
        }};

        healLaserMount = new MultiTurretMount("healLaserM"){{
            title = "HealLaser";
            repairSpeed = 0.5f;
            repairRadius = 55f;
            powerUse = 1.5f;
            laserColor = Color.valueOf("e8ffd7");
            mountType = MultiTurretMountType.repair;
        }};

        repairMount = new MultiTurretMount("miniRepairM"){{
            title = "Mini repairPoint";
            repairSpeed = 0.5f;
            repairRadius = 65f;
            powerUse = 1f;
            laserColor = Color.valueOf("e8ffd7");
            mountType = MultiTurretMountType.repair;
        }};

        pointMount = new MultiTurretMount("miniSegmentM"){{
            title = "Mini Segment";
            powerUse = 8f;
            range = 180f;
            shootCone = 5f;
            shootLength = 5f;
            bulletDamage = 30f;
            reloadTime = 9f;
            mountType = MultiTurretMountType.point;
        }};

        tractMount = new MultiTurretMount("miniTractM"){{
            title = "Mini Parallax";
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
            collidesAir = true;
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
