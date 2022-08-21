package Sharustry.content;

import arc.graphics.Color;
import arc.struct.Seq;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import Sharustry.world.blocks.defense.turret.mounts.*;
import mindustry.graphics.Pal;

import static Sharustry.content.SBullets.*;

public class STurretMounts {
    public static MountTurretType miniDrillMount, miniMassMount, drillMount, massMount, electricLaserMountL, electricLaserMountR, healMissileMountL, healMissileMountR, healBeamMountL, healBeamMountR, healLaserMount2, healLaserMount, healBeamMount, repairMount, pointMount, tractMount, laserMount, arcMount, unoMount, hailMount, waveMount;
    public static Seq<MountTurretType> mounttypes = new Seq<>();
    public static void load() {
        miniDrillMount = new DrillMountTurretType("mineM"){{
            title = "Mini Minor";
            laserWidth = 0.75f;
            shootSound = Sounds.minebeam;
            shootSoundVolume = 0.9f;

            minDrillTier = 0;
            maxDrillTier = 3;
            mineSpeed = 1.5f;
            laserOffset = 4f;
            shootCone = 6f;

            powerUse = 2f;
            range = 60f;
        }};

        miniMassMount = new MassMountTurretType("massM"){{
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
            reload = 100f;
            range = 440f;
        }};

        drillMount = new DrillMountTurretType("DrillM"){{
            title = "Minor";

            laserWidth = 0.75f;
            shootSound = Sounds.minebeam;
            shootSoundVolume = 0.9f;

            powerUse = 2f;
            minDrillTier = 0;
            maxDrillTier = 3;
            mineSpeed = 0.75f;
            laserOffset = 4f;
            shootCone = 6f;

            range = 60f;
        }};
        massMount = new MassMountTurretType("miniMassDriver"){{
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
            title = "MassDriver";
            reload = 100f;
            range = 440f;
        }};
        electricLaserMountL = new PowerMountTurretType("electLaserML", new LaserBulletType(140){{
            colors = new Color[]{Pal.lancerLaser.cpy().a(0.4f), Pal.lancerLaser, Color.white};
            hitEffect = Fx.hitLancer;
            despawnEffect = Fx.none;
            hitSize = 4;
            lifetime = 16f;
            drawSize = 400f;
            collidesAir = false;
            length = 173f;
        }}){{
            xOffset = 2.75f;
            yOffset = 2.75f;
            shootY = 13/4f;
            range = 9 * 8;
            title = "Electric Laser Left";

            recoil = 2f;
            reload = 80f;
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
        }};

        electricLaserMountR = new PowerMountTurretType("electLaserMR", new LaserBulletType(140){{
            colors = new Color[]{Pal.lancerLaser.cpy().a(0.4f), Pal.lancerLaser, Color.white};
            hitEffect = Fx.hitLancer;
            despawnEffect = Fx.none;
            hitSize = 4;
            lifetime = 16f;
            drawSize = 400f;
            collidesAir = false;
            length = 173f;
        }}){{
            xOffset = 2.75f;
            yOffset = 2.75f;
            shootY = 13/4f;
            range = 9 * 8;
            title = "Electric Laser Right";

            recoil = 2f;
            reload = 80f;
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
        }};

        healMissileMountR = new PowerMountTurretType("healMissileMR", new MissileBulletType(3.5f, 15){{
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
        }}){{
            targetHealing = true;
            title = "HealMissileRight";
            reload = 40f;
            shoot.shotDelay = 8.5f;
            shoot.shots = 3;
            inaccuracy = 5f;
            shootSound = Sounds.missile;

            recoil = 2f;
            heatColor = Pal.turretHeat;
            powerUse = 2f;
        }};

        healMissileMountL = new PowerMountTurretType("healMissileML", new MissileBulletType(3.5f, 15){{
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
        }}){{

            targetHealing = true;
            title = "HealMissileLeft";
            reload = 40f;
            shoot.shotDelay = 5f;
            shoot.shots = 3;
            inaccuracy = 5f;
            shootSound = Sounds.missile;

            recoil = 2f;
            heatColor = Pal.turretHeat;
            powerUse = 2f;
        }};

        healBeamMountL = new PowerMountTurretType("healBeamM2L", new LaserBoltBulletType(6.2f, 20){{
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
            targetHealing = true;
            title = "HealBeamLeft";

            shootCone = 40f;
            shoot.shotDelay = 8f;
            inaccuracy = 5f;
            shoot.shots = 3;
            range = 15 * 8f;
            shootSound = Sounds.lasershoot;
            reload = 85f;
            recoil = 2.5f;
            heatColor = Pal.turretHeat;
            powerUse = 2.5f;
        }};

        healBeamMountR = new PowerMountTurretType("healBeamM2R", new LaserBoltBulletType(6.2f, 20){{
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
            targetHealing = true;
            title = "HealBeamRight";

            shootCone = 40f;
            shoot.shotDelay = 8f;
            inaccuracy = 5f;
            shoot.shots = 3;
            range = 15 * 8f;
            shootSound = Sounds.lasershoot;
            reload = 85f;
            recoil = 2.5f;
            heatColor = Pal.turretHeat;
            powerUse = 2.5f;
        }};

        healBeamMount = new PowerMountTurretType("healBeamM", new LaserBoltBulletType(5.2f, 10){{
            lifetime = 35f;
            healPercent = 5.5f;
            collidesTeam = true;
            backColor = Pal.heal;
            frontColor = Color.white;
        }

            @Override
            public void update(Bullet b) {
                super.update(b);
                //Log.info("but actaully it's on "+ b.x + ", " + b.y);
            }
        }){{
            targetHealing = true;
            title = "HealBeam";

            xOffset = -4f;
            yOffset = -4.75f;
            shootCone = 40f;
            shoot.shotDelay = 7f;
            shoot.shots = 3;
            range = 10 * 8f;
            shootSound = Sounds.lasershoot;
            reload = 105f;
            recoil = 1.5f;
            heatColor = Pal.turretHeat;
            powerUse = 1.25f;
        }};

        healLaserMount2 = new RepairMountTurretType("healLaserM2"){{
            title = "HealLaser";
            repairSpeed = 0.75f;
            range = 75f;
            powerUse = 3f;
            laserColor = Color.valueOf("e8ffd7");
        }};

        healLaserMount = new RepairMountTurretType("healLaserM"){{
            title = "HealLaser";
            repairSpeed = 0.5f;
            range = 55f;
            powerUse = 1.5f;
            laserColor = Color.valueOf("e8ffd7");
        }};

        repairMount = new RepairMountTurretType("miniRepairM"){{
            title = "Mini repairPoint";
            repairSpeed = 0.5f;
            range = 65f;
            powerUse = 1f;
            laserColor = Color.valueOf("e8ffd7");
        }};

        pointMount = new PointMountTurretType("miniSegmentM"){{
            title = "Mini Segment";
            powerUse = 8f;
            range = 180f;
            shootCone = 5f;
            shootLength = 5f;
            bulletDamage = 30f;
            reload = 9f;
        }};

        tractMount = new TractMountTurretType("miniTractM"){{
            title = "Mini Parallax";
            shootCone = 6f;
            shootLength = 5f;
            force = 24f;
            scaledForce = 7f;
            range = 230f;
            damage = 0.3f;
            rotateSpeed = 10;
            powerUse = 3f;
        }};

        laserMount = new PowerMountTurretType("laserM", miniAccelMissile){{
            title = "Laser";
            shoot.shotDelay = 7f;
            shoot.shots = 2;
            spread = 4f;
            reload = 20f;
            range = 100;
            shootCone = 15f;
            inaccuracy = 2f;
            rotateSpeed = 10f;
            powerUse = 6.3f;
        }};

        arcMount = new PowerMountTurretType("arcM", new LightningBulletType(){{
            damage = 20;
            lightningLength = 25;
            collidesAir = false;
        }}){{
            title = "Arc";
            xOffset = -4f;
            yOffset = -4.75f;
            reload = 35f;
            shootCone = 40f;
            rotateSpeed = 8f;
            powerUse = 3.3f;
            range = 90f;
            shootEffect = Fx.lightningShoot;
            heatColor = Color.red;
            recoil = 1f;
            shootSound = Sounds.spark;
        }};

        unoMount = new PowerMountTurretType("unoM", new LaserBulletType(140){{
            colors = new Color[]{Pal.lancerLaser.cpy().a(0.4f), Pal.lancerLaser, Color.white};
            hitEffect = Fx.hitLancer;
            despawnEffect = Fx.none;
            hitSize = 4;
            lifetime = 16f;
            drawSize = 400f;
            collidesAir = true;
            length = 173f;
        }}){{
            xOffset = 2.75f;
            yOffset = 2.75f;
            shootY = 13/4f;
            range = 9 * 8;
            title = "Uno";

            recoil = 2f;
            reload = 80f;
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
        }};

        hailMount = new ItemMountTurretType("hailM", 
            Items.graphite, new ArtilleryBulletType(3f, 20, "shell"){{
                hitEffect = Fx.flakExplosion;
                knockback = 0.8f;
                lifetime = 80f;
                width = height = 11f;
                collidesTiles = false;
                splashDamageRadius = 25f * 0.75f;
                splashDamage = 33f;
            }},
            Items.silicon, new ArtilleryBulletType(3f, 20, "shell"){{
                hitEffect = Fx.flakExplosion;
                knockback = 0.8f;
                lifetime = 80f;
                width = height = 11f;
                collidesTiles = false;
                splashDamageRadius = 25f * 0.75f;
                splashDamage = 33f;
                reloadMultiplier = 1.2f;
                ammoMultiplier = 3f;
                homingPower = 0.08f;
                homingRange = 50f;
            }},
            Items.pyratite, new ArtilleryBulletType(3f, 20, "shell"){{
                hitEffect = Fx.blastExplosion;
                knockback = 0.8f;
                lifetime = 80f;
                width = height = 13f;
                collidesTiles = false;
                splashDamageRadius = 25f * 0.75f;
                splashDamage = 35f;
                status = StatusEffects.burning;
                statusDuration = 60f * 12f;
                frontColor = Pal.lightishOrange;
                backColor = Pal.lightOrange;
                makeFire = true;
                trailEffect = Fx.incendTrail;
                ammoMultiplier = 4f;
            }}
        ){{
            targetAir = false;
            reload = 60;
            ammoPerShot = 5;
            xOffset = -3.75f;
            yOffset = -4f;
            shootY = 18/4f;
            recoil = 2.5f;
            range = 18 * 8;
            title = "Mini Hail";
            shootSound = Sounds.bang;

            maxAmmo = 50;
        }};

        waveMount = new LiquidMountTurretType("waveM", 
            Liquids.water, new LiquidBulletType(){{
                collidesAir = false;
                liquid = Liquids.water;
                knockback = 0.25f;
                drag = 0.03f;
                puddleSize = 2;
                orbSize = 1;
            }},
            Liquids.slag, new LiquidBulletType(){{
                collidesAir = false;
                liquid = Liquids.slag;
                damage = 1;
                drag = 0.03f;
                puddleSize = 2;
                orbSize = 1;
            }},
            Liquids.cryofluid, new LiquidBulletType(){{
                collidesAir = false;
                liquid = Liquids.cryofluid;
                drag = 0.03f;
                puddleSize = 2;
                orbSize = 1;
            }},
            Liquids.oil, new LiquidBulletType(){{
                collidesAir = false;
                liquid = Liquids.oil;
                drag = 0.03f;
                puddleSize = 2;
                orbSize = 1;
            }}
        ){{
            reload = 3;
            xOffset = 4.25f;
            yOffset = -3.5f;
            shootY = 16/4f;
            recoil = 1;
            range = 13 * 8;
            title = "Mini Wave";
            shootSound = Sounds.none;
            loopSound = Sounds.spray;
            maxAmmo = 100;
            extinguish = true;
        }};

        mounttypes = Seq.with(miniDrillMount, miniMassMount, drillMount, massMount, electricLaserMountL, electricLaserMountR, healMissileMountL, healMissileMountR, healBeamMountL, healBeamMountR, healLaserMount2, healLaserMount, healBeamMount, repairMount, pointMount, tractMount, laserMount, arcMount, unoMount, hailMount, waveMount);
    }
}
