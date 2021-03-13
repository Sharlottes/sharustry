package Sharustry.content;

import Sharustry.entities.bullet.FieldBulletType;
import Sharustry.graphics.SPal;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.entities.bullet.ShrapnelBulletType;
import mindustry.graphics.Pal;
import multilib.Recipe.*;
import Sharustry.world.blocks.defense.*;
import Sharustry.world.blocks.production.*;
import arc.graphics.Color;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.content.*;
import mindustry.world.*;
import mindustry.world.meta.*;

import static Sharustry.content.SBullets.jumbleBullet;
import static Sharustry.content.STurretMounts.*;
import static mindustry.type.ItemStack.*;

public class SBlocks implements ContentList{
    public static Block
            //defense
            balkan, jumble, conductron, technicus, clinicus,
            //wall
            shieldWall,
            //drill
            adaptDrill, multiDrill,
            //crafterator
            multi;

    @Override
    public void load(){
        balkan = new SkillTurret("balkan"){{

            addSkills(entity -> () -> {
                final Color data;
                if(((TemplatedTurretBuild)entity).hasAmmo() && ((TemplatedTurretBuild)entity).peekAmmo() == SBullets.testLaser) data = Items.pyratite.color;
                else data = null;
                for(int i = 0; i < 8; i++){
                    Time.run(0.1f * 60 * i, () -> {
                        final float ex = entity.x + Mathf.range(16f);
                        final float ey = entity.y + Mathf.range(16f);
                        SFx.skill.at(ex, ey, ammoTypes.findKey(((TemplatedTurretBuild)entity).peekAmmo(), true).color);
                        for(int ii = 0; ii < 3; ii++) Time.run(15 * ii, () -> {
                            Sounds.missile.at(ex, ey);
                            (data == null ? SBullets.miniAccelMissile : SBullets.miniAccelMissilePyra).create(entity, ex, ey, ((BaseTurretBuild) entity).rotation);
                        });
                    });
                }
            }, 5);

            addSkills(entity -> () -> {
                Sounds.unlock.at(entity.x, entity.y, 0.75f);
                if(((TemplatedTurretBuild)entity).hasAmmo() && ((TemplatedTurretBuild)entity).peekAmmo() == SBullets.testLaser) new FieldBulletType(0, -1, 897, 85).create(entity, entity.x, entity.y, 0);
                else new FieldBulletType(0, -1, 897, 85){{
                    mainColor = SPal.cryoium;
                    subColor = Items.titanium.color;
                }}.create(entity, entity.x, entity.y, 0);
            }, 20);

            ammoType = "item";
            ammo(
                Items.titanium, SBullets.accelMissile,
                Items.pyratite, SBullets.testLaser
            );

            hasPower = true;
            range = 45*8f;
            chargeTime = 40f;
            chargeMaxDelay = 30f;
            chargeEffects = 5;
            recoilAmount = 4f;
            reloadTime = 90f;
            cooldown = 0.05f;
            shootShake = 4f;
            shootEffect = SFx.balkanShoot;
            smokeEffect = Fx.none;
            chargeEffect = SFx.balkanCharge;
            chargeBeginEffect = SFx.balkanChargeBegin;
            heatColor = Color.blue;
            size = 2;
            health = 340 * size * size;
            targetAir = true;
            shootSound = Sounds.missile;

            acceptCoolant = false;
            requirements(Category.turret, with(Items.copper, 250, Items.lead, 80, Items.titanium, 40, Items.silicon, 60));
        }};

        conductron = new MultiTurret("conductron"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 200, Items.lead, 150, Items.silicon, 125, Items.graphite, 95, Items.titanium, 70));

            addBaseTurret(new LaserBulletType(140){{
                colors = new Color[]{Pal.lancerLaser.cpy().a(0.4f), Pal.lancerLaser, Color.white};
                hitEffect = Fx.hitLancer;
                despawnEffect = Fx.none;
                hitSize = 4;
                lifetime = 16f;
                drawSize = 400f;
                collidesAir = false;
                length = 173f;
            }}, Items.titanium, "Conductron");
            addMountTurret(arcMount, arcMount, laserMount);
            addCustomMountLocation(new Float[]{-6.5f, -4.25f, 6.5f, -4.25f, 0f, 1.5f});

            ammos(MultiTurretMount.MultiTurretMountType.power);
            ammos(MultiTurretMount.MultiTurretMountType.power);
            ammos(MultiTurretMount.MultiTurretMountType.power);

            hasPower = true;
            size = 3;
            maxAmmo = 30;
            ammoPerShot = 3;
            customMountLocation = true;
            range = 165f;
            chargeTime = 40f;
            chargeMaxDelay = 30f;
            chargeEffects = 7;
            recoilAmount = 2f;
            reloadTime = 80f;
            cooldown = 0.03f;
            shootShake = 2f;
            shootEffect = Fx.lancerLaserShoot;
            smokeEffect = Fx.none;
            chargeEffect = Fx.lancerLaserCharge;
            chargeBeginEffect = Fx.lancerLaserChargeBegin;
            heatColor = Color.red;

            health = 280 * size * size;
            shootSound = Sounds.laser;

        }};


        technicus = new MultiSkillTurret("technicus"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 200, Items.lead, 150, Items.silicon, 125, Items.graphite, 95, Items.titanium, 70));

            addBaseTurret(new ShrapnelBulletType(){{
                length = 110f;
                damage = 105f;
                ammoMultiplier = 5f;
                toColor = Pal.thoriumPink;
                shootEffect = smokeEffect = Fx.thoriumShoot;
            }}, Items.thorium, "Technicus");
            addMountTurret(tractMount, pointMount, repairMount);

            ammos(MultiTurretMount.MultiTurretMountType.tract);
            ammos(MultiTurretMount.MultiTurretMountType.point);
            ammos(MultiTurretMount.MultiTurretMountType.repair);

            addSkills(entity -> () -> {
                for(int i = 0; i < 8; i++){
                    Time.run(0.1f * 60 * i,
                        () -> {
                            float ex = entity.x + Mathf.range(16f);
                            float ey = entity.y + Mathf.range(16f);
                            SFx.skill.at(ex, ey);
                            Bullets.artilleryPlastic.create(entity, ex, ey, ((BaseTurretBuild)entity).rotation);
                        }
                    );
                }
            }, 20);

            hasPower = true;
            size = 3;
            spread = 4f;
            shots = 2;
            alternate = true;
            reloadTime = 20f;
            restitution = 0.03f;
            range = 100;
            shootCone = 15f;
            ammoUseEffect = Fx.casing1;
            inaccuracy = 2f;
            rotateSpeed = 10f;


            health = 110 * size * size;
            shootSound = Sounds.shotgun;
        }};

        clinicus = new MultiTurret("clinicus"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 335, Items.lead, 210, Items.graphite, 180, Items.silicon, 250, Items.thorium, 90));

            addBaseTurret(SBullets.artilleryHeal, Items.plastanium,"Clinicus");
            addMountTurret(healBeamMount, healBeamMount, healLaserMount);
            addCustomMountLocation(new Float[]{6.75f, -2.5f, -6.5f, -2.5f, 0f, 1f});

            ammos(MultiTurretMount.MultiTurretMountType.power);
            ammos(MultiTurretMount.MultiTurretMountType.power);
            ammos(MultiTurretMount.MultiTurretMountType.repair);

            customMountLocation = true;
            hasLiquids = true;
            hasItems = true;
            hasPower = true;
            targetAir = false;
            size = 3;
            shots = 6;
            inaccuracy = 12f;
            reloadTime = 5 * 60f;
            ammoEjectBack = 5f;
            ammoUseEffect = Fx.casing3Double;
            ammoPerShot = 2;
            cooldown = 0.03f;
            velocityInaccuracy = 0.2f;
            restitution = 0.02f;
            recoilAmount = 6f;
            shootShake = 2f;
            range = 95f;
            burstSpacing = 5f;
            minRange = 50f;

            inaccuracy = 17f;
            health = 130 * size * size;
            shootSound = Sounds.artillery;
        }};

        jumble = new MultiTurret("multi-i"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 135, Items.lead, 75, Items.metaglass, 40, Items.graphite, 80, Items.silicon, 50));

            addBaseTurret(jumbleBullet, Items.graphite, "Aggregate");
            addMountTurret(unoMount, waveMount, hailMount);

            ammos(MultiTurretMount.MultiTurretMountType.power, Liquids.water, SBullets.miniWater);
            ammos(MultiTurretMount.MultiTurretMountType.liquid,
                Liquids.water, SBullets.miniWater,
                Liquids.slag, SBullets.miniSlag,
                Liquids.cryofluid, SBullets.miniCryo,
                Liquids.oil, SBullets.miniOil
            );
            ammos(MultiTurretMount.MultiTurretMountType.item,
                Items.graphite, Bullets.artilleryDense,
                Items.silicon, Bullets.artilleryHoming,
                Items.pyratite, Bullets.artilleryIncendiary
            );

            size = 2;
            range = 15 * 8;
            maxAmmo = 30;
            recoilAmount = 2;
            reloadTime = 21;
            ammoPerShot = 2;
            hasPower = true;
        }};

        adaptDrill = new AttributeDrill("adapt-drill"){{
            requirements(Category.production, with(Items.copper, 35, Items.graphite, 30, Items.silicon, 30, Items.titanium, 20));
            drillTime = 280;
            size = 3;
            hasPower = true;
            tier = 4;
            updateEffect = Fx.pulverizeMedium;
            drillEffect = Fx.mineBig;
            defaultAttribute = Attribute.light;
            configurable = true;
            maxHeatBoost = 2;
            consumes.power(1.10f);
            consumes.liquid(Liquids.water, 0.08f).boost();
        }};

        multiDrill = new MultipleDrill("multi-drill"){{
            requirements(Category.production, with(Items.copper, 35, Items.graphite, 30, Items.silicon, 30, Items.titanium, 20));
            drillTime = 210;
            size = 3;
            hasPower = true;
            tier = 5;
            updateEffect = Fx.pulverizeMedium;
            drillEffect = Fx.mineBig;
            itemCapacity = 50;
            delay = 2 * 60;
            consumes.power(1.10f);
            consumes.liquid(Liquids.water, 0.08f).boost();
        }};

        shieldWall = new ShieldWall("shield-wall"){{
                requirements(Category.defense, with(Items.titanium, 6));
                health = 150 * 4;
                armor = 3;

        }};

        multi = new MultiCrafterator("multi-crafterator", 4){{
            requirements(Category.crafting, with(Items.copper, 10));
            size = 3;
            isSmelter = true;

            addRecipe(
                new InputContents(with(Items.sand, 1, Items.lead, 1)),
                new OutputContents(), 12f, true
            );
            addRecipe(
                new InputContents(with(Items.coal, 1, Items.sand, 1), new LiquidStack[]{new LiquidStack(Liquids.water, 5)}, 1),
                new OutputContents(new LiquidStack[]{new LiquidStack(Liquids.slag, 5)}), 60f
            );
            addRecipe(
                new InputContents(with(Items.pyratite, 1, Items.blastCompound, 1)),
                new OutputContents(with(Items.scrap, 1, Items.plastanium, 2, Items.sporePod, 2)), 72f
            );
            addRecipe(
                new InputContents(with(Items.sand, 1), 15),
                new OutputContents(with(Items.silicon, 1), 10), 30f, true
            );
        }};












        /* //fuck
        warehouseBattle = new BattleCoreBlock("warehouse-battle"){{

            size = 5;
            itemCapacity = 7000;
            flags = EnumSet.of(BlockFlag. core);
            requirements(Category.effect, with(Items.titanium, 600, Items.thorium, 500, Items.plastanium, 450, Items.phaseFabric, 150, Items.surgeAlloy, 100));
            turrets.add(
                new ItemBattleCoreBlock("duo-part1"){{
                    requirements(Category.turret, with(Items.copper, 35), true);
                    ammo(
                        Items.copper, Bullets.standardCopper,
                        Items.graphite, Bullets.standardDense,
                        Items.pyratite, Bullets.standardIncendiary,
                        Items.silicon, Bullets.standardHoming
                    );

                    spread = 4f;
                    shots = 2;
                    alternate = true;
                    reloadTime = 20f;
                    restitution = 0.03f;
                    range = 100;
                    shootCone = 15f;
                    ammoUseEffect = Fx.casing1;
                    health = 250;
                    inaccuracy = 2f;
                    rotateSpeed = 10f;
                    offsetx = +5;
                    offsety = +5;
                }},
                new ItemBattleCoreBlock("duo-part2"){{
                    requirements(Category.turret, with(Items.copper, 35), true);
                    ammo(
                        Items.copper, Bullets.standardCopper,
                        Items.graphite, Bullets.standardDense,
                        Items.pyratite, Bullets.standardIncendiary,
                        Items.silicon, Bullets.standardHoming
                    );

                    spread = 4f;
                    shots = 2;
                    alternate = true;
                    reloadTime = 20f;
                    restitution = 0.03f;
                    range = 100;
                    shootCone = 15f;
                    ammoUseEffect = Fx.casing1;
                    health = 250;
                    inaccuracy = 2f;
                    rotateSpeed = 10f;
                    offsetx = -5;
                    offsety = +5;
                }},
                new ItemBattleCoreBlock("duo-part3"){{
                    requirements(Category.turret, with(Items.copper, 35), true);
                    ammo(
                        Items.copper, Bullets.standardCopper,
                        Items.graphite, Bullets.standardDense,
                        Items.pyratite, Bullets.standardIncendiary,
                        Items.silicon, Bullets.standardHoming
                    );

                    spread = 4f;
                    shots = 2;
                    alternate = true;
                    reloadTime = 20f;
                    restitution = 0.03f;
                    range = 100;
                    shootCone = 15f;
                    ammoUseEffect = Fx.casing1;
                    health = 250;
                    inaccuracy = 2f;
                    rotateSpeed = 10f;
                    offsetx = +5;
                    offsety = -5;
                }},
                new ItemBattleCoreBlock("duo-part4"){{
                    requirements(Category.turret, with(Items.copper, 35), true);
                    ammo(
                        Items.copper, Bullets.standardCopper,
                        Items.graphite, Bullets.standardDense,
                        Items.pyratite, Bullets.standardIncendiary,
                        Items.silicon, Bullets.standardHoming
                    );

                    spread = 4f;
                    shots = 2;
                    alternate = true;
                    reloadTime = 20f;
                    restitution = 0.03f;
                    range = 100;
                    shootCone = 15f;
                    ammoUseEffect = Fx.casing1;
                    health = 250;
                    inaccuracy = 2f;
                    rotateSpeed = 10f;
                    offsetx = -5;
                    offsety = -5;
                }}
            );
        }};*/
    }
}
