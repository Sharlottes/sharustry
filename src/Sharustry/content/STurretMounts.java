package Sharustry.content;

import arc.graphics.Color;
import arc.struct.Seq;
import mindustry.content.Bullets;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.ctype.ContentList;
import mindustry.entities.bullet.LaserBoltBulletType;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.entities.bullet.LightningBulletType;
import mindustry.entities.bullet.MissileBulletType;
import mindustry.gen.Sounds;
import Sharustry.world.blocks.defense.MountTurretType;
import mindustry.graphics.Pal;

import static Sharustry.content.SBullets.*;

public class STurretMounts implements ContentList {
    public static MountTurretType miniDrillMount, miniMassMount, drillMount, massMount, electricLaserMountL, electricLaserMountR, healMissileMountL, healMissileMountR, healBeamMountL, healBeamMountR, healLaserMount2, healLaserMount, healBeamMount, repairMount, pointMount, tractMount, laserMount, arcMount, unoMount, hailMount, waveMount;
    public static Seq<MountTurretType> mounttypes = new Seq<>();
    @Override
    public void load() {
        miniDrillMount = new MountTurretType("mineM", MountTurretType.MultiTurretMountType.drill){{
            title = "Mini Minor";
            mountType = MultiTurretMountType.drill;

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

        miniMassMount = new MountTurretType("massM", MountTurretType.MultiTurretMountType.mass){{
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
            reloadTime = 100f;
            range = 440f;

            mountType = MultiTurretMountType.mass;
        }};

        drillMount = new MountTurretType("DrillM", MountTurretType.MultiTurretMountType.drill){{
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
        massMount = new MountTurretType("miniMassDriver", MountTurretType.MultiTurretMountType.mass){{
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
            reloadTime = 100f;
            range = 440f;
        }};
        electricLaserMountL = new MountTurretType("electLaserML", MountTurretType.MultiTurretMountType.power, new LaserBulletType(140){{
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
        }};

        electricLaserMountR = new MountTurretType("electLaserMR", MountTurretType.MultiTurretMountType.power, new LaserBulletType(140){{
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
        }};

        healMissileMountR = new MountTurretType("healMissileMR", MountTurretType.MultiTurretMountType.power, new MissileBulletType(3.5f, 15){{
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
            healBlock = true;
            title = "HealMissileRight";
            reloadTime = 40f;
            shots = 3;
            inaccuracy = 5f;
            shootSound = Sounds.missile;

            recoilAmount = 2f;
            heatColor = Pal.turretHeat;
            powerUse = 2f;

            barrels = 5;
        }};

        healMissileMountL = new MountTurretType("healMissileML", MountTurretType.MultiTurretMountType.power, new MissileBulletType(3.5f, 15){{
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

            healBlock = true;
            title = "HealMissileLeft";
            reloadTime = 40f;
            shots = 3;
            inaccuracy = 5f;
            shootSound = Sounds.missile;

            recoilAmount = 2f;
            heatColor = Pal.turretHeat;
            powerUse = 2f;

            barrels = 5;

        }};

        healBeamMountL = new MountTurretType("healBeamM2L", MountTurretType.MultiTurretMountType.power, new LaserBoltBulletType(6.2f, 20){{
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
        }};

        healBeamMountR = new MountTurretType("healBeamM2R", MountTurretType.MultiTurretMountType.power, new LaserBoltBulletType(6.2f, 20){{
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
        }};

        healBeamMount = new MountTurretType("healBeamM", MountTurretType.MultiTurretMountType.power, new LaserBoltBulletType(5.2f, 10){{
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
        }};

        healLaserMount2 = new MountTurretType("healLaserM2", MountTurretType.MultiTurretMountType.repair){{
            title = "HealLaser";
            repairSpeed = 0.75f;
            repairRadius = 75f;
            powerUse = 3f;
            laserColor = Color.valueOf("e8ffd7");
        }};

        healLaserMount = new MountTurretType("healLaserM", MountTurretType.MultiTurretMountType.repair){{
            title = "HealLaser";
            repairSpeed = 0.5f;
            repairRadius = 55f;
            powerUse = 1.5f;
            laserColor = Color.valueOf("e8ffd7");
        }};

        repairMount = new MountTurretType("miniRepairM", MountTurretType.MultiTurretMountType.repair){{
            title = "Mini repairPoint";
            repairSpeed = 0.5f;
            repairRadius = 65f;
            powerUse = 1f;
            laserColor = Color.valueOf("e8ffd7");
        }};

        pointMount = new MountTurretType("miniSegmentM", MountTurretType.MultiTurretMountType.point){{
            title = "Mini Segment";
            powerUse = 8f;
            range = 180f;
            shootCone = 5f;
            shootLength = 5f;
            bulletDamage = 30f;
            reloadTime = 9f;
        }};

        tractMount = new MountTurretType("miniTractM", MountTurretType.MultiTurretMountType.tract){{
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

        laserMount = new MountTurretType("laserM", MountTurretType.MultiTurretMountType.power, miniAccelMissile){{
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
        }};

        arcMount = new MountTurretType("arcM", MountTurretType.MultiTurretMountType.power, new LightningBulletType(){{
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
        }};


        unoMount = new MountTurretType("unoM", MountTurretType.MultiTurretMountType.power, new LaserBulletType(140){{
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
        }};


        hailMount = new MountTurretType("hailM", MountTurretType.MultiTurretMountType.item,
                Items.graphite, Bullets.artilleryDense,
                Items.silicon, Bullets.artilleryHoming,
                Items.pyratite, Bullets.artilleryIncendiary
        ){{
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
        }};

        waveMount = new MountTurretType("waveM", MountTurretType.MultiTurretMountType.liquid,
                Liquids.water, SBullets.miniWater,
                Liquids.slag, SBullets.miniSlag,
                Liquids.cryofluid, SBullets.miniCryo,
                Liquids.oil, SBullets.miniOil
        ){{
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
        }};

        mounttypes = Seq.with(miniDrillMount, miniMassMount, drillMount, massMount, electricLaserMountL, electricLaserMountR, healMissileMountL, healMissileMountR, healBeamMountL, healBeamMountR, healLaserMount2, healLaserMount, healBeamMount, repairMount, pointMount, tractMount, laserMount, arcMount, unoMount, hailMount, waveMount);
    }
}
