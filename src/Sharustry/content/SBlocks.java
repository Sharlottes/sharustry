package Sharustry.content;

import Sharustry.entities.bullet.FieldBulletType;
import Sharustry.graphics.SPal;
import Sharustry.world.blocks.logic.VariableLogicBlock;
import Sharustry.world.blocks.storage.BattleCore;
import arc.Core;
import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.entities.bullet.BulletType;
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
            //logic
            variableProcessor,
            //defense
            //marksman,
            balkan, jumble, conductron, trinity, asclepius, clinicus, fossor,
            //wall
            shieldWall,
            //drill
            adaptDrill, multiDrill,
            //crafterator
            multi,
            //armed core
            armedFoundation, armedNucleus;

    @Override
    public void load(){
        variableProcessor = new VariableLogicBlock("variable-processor"){{
            requirements(Category.logic, with(Items.copper, 80, Items.lead, 50, Items.silicon, 30));

            instructionsPerTick = 2;

            size = 1;

            range = 500 * 8f;
        }};

        balkan = new SkillTurret("balkan"){{
            addSkills(entity -> () -> {
                for(int i = 0; i < 8; i++){
                    Time.run(0.1f * 60 * i, () -> {
                        final float ex = entity.x + Mathf.range(16f);
                        final float ey = entity.y + Mathf.range(16f);
                        SFx.skill.at(ex, ey, ammoTypes.findKey(((TemplatedTurretBuild)entity).peekAmmo(), true).color);
                        for(int ii = 0; ii < 3; ii++) Time.run(15 * ii, () -> {
                            Sounds.missile.at(ex, ey);
                            SBullets.miniAccelMissile.create(entity, ex, ey, ((BaseTurretBuild) entity).rotation);
                        });
                    });
                }
            }, 5, Core.bundle.get("stat.shar.burstshoot-name"));

            addSkills(entity -> () -> {
                Sounds.unlock.at(entity.x, entity.y, 0.75f);
                if(((TemplatedTurretBuild)entity).hasAmmo() && ((TemplatedTurretBuild)entity).peekAmmo() == SBullets.testLaser) new FieldBulletType(0, -1, 897, 85).create(entity, entity.x, entity.y, 0);
                else new FieldBulletType(0, -1, 897, 85){{
                    mainColor = SPal.cryoium;
                    subColor = Items.titanium.color;
                    status = SStatusEffects.overFreezing;
                }}.create(entity, entity.x, entity.y, 0);
            }, 20, Core.bundle.get("stat.shar.overfreezing-name"));
            skillDescriptions.add(Core.bundle.get("stat.shar.burstshoot-description"), Core.bundle.get("stat.shar.overfreezing-description"));
            skillStats.add(
                table -> {
                    table.top();
                    table.add(Core.bundle.format("stat.shar.burstamount", 24));
                },
                table -> {
                    table.top();
                    table.add(new Stack(){{
                        add(new Table(h -> {
                            h.left();
                            h.add(Core.bundle.format("stat.shar.fieldstatus"));
                        }));
                        add(new Table(q -> {
                            q.right();
                            q.add(new Image(Core.atlas.find("shar-over-freezing"))).padLeft(15 * 8f);
                            q.add("[stat]" + SStatusEffects.overFreezing.localizedName +"[]");
                            q.pack();
                        }));
                    }});
                }
            );
            ammoType = "item";
            ammo(Items.titanium, SBullets.accelMissile);

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
            chargeEffect = SFx.balkanChargeCircles;
            chargeBeginEffect = SFx.balkanChargeBegin;
            heatColor = Color.blue;
            size = 2;
            health = 110 * size * size;
            targetAir = true;
            shootSound = Sounds.missile;

            acceptCoolant = false;
            requirements(Category.turret, with(Items.copper, 250, Items.lead, 80, Items.titanium, 40, Items.silicon, 60));
        }};
        /*
        marksman = new MultiTurret("marksman"){{
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
            }}, Items.titanium, "Marksman");
            addMountTurret(electricLaserMountR, electricLaserMountL);
            addCustomMountLocation(new Float[]{
                    -3f, -2f,
                    3f, -2f,
            });

            ammos(MultiTurretMount.MultiTurretMountType.power);
            ammos(MultiTurretMount.MultiTurretMountType.power);

            hasPower = true;
            size = 4;
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
        */
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
            addCustomMountLocation(new Float[]{
                -6.5f, -4.25f,
                6.5f, -4.25f,
                0f, 1.5f
            });

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

            health = 190 * size * size;
            shootSound = Sounds.laser;

        }};


        trinity = new MultiTurret("trinity"){{
            configurable = true;
            requirements(Category.turret, ItemStack.with(Items.copper, 200, Items.lead, 150, Items.silicon, 125, Items.graphite, 95, Items.titanium, 70));

            addBaseTurret(new ShrapnelBulletType(){{
                length = 110f;
                damage = 105f;
                ammoMultiplier = 5f;
                toColor = Pal.thoriumPink;
                shootEffect = smokeEffect = Fx.thoriumShoot;
            }}, Items.thorium, "Trinity");
            addMountTurret(repairMount, pointMount, massMount, tractMount);
            addCustomMountLocation(new Float[]{
                    -8f, 0f,
                    0f, 6.5f,
                    0f, 0f,
                    8f, 0f
            });

            ammos(MultiTurretMount.MultiTurretMountType.repair);
            ammos(MultiTurretMount.MultiTurretMountType.point);
            ammos(MultiTurretMount.MultiTurretMountType.mass);
            ammos(MultiTurretMount.MultiTurretMountType.tract);

            addSkills(entity -> () -> {
                if(Groups.unit.find(u -> Mathf.dst(entity.x, entity.y, u.x, u.y) <= range) == null
                        || Groups.unit.count(u -> Mathf.dst(entity.x, entity.y, u.x, u.y) <= range
                        && u.abilities.find(a -> a instanceof ForceFieldAbility) != null) == Groups.unit.count(u -> Mathf.dst(entity.x, entity.y, u.x, u.y) <= range)) return;
                SFx.shieldSpread.at(entity.x, entity.y, 0, range);

                Time.run(30, () -> Groups.unit.each(u -> Mathf.dst(entity.x, entity.y, u.x, u.y) <= range, target -> {
                    if(target.abilities.find(a -> a instanceof ForceFieldAbility) != null) return;
                    ForceFieldAbility abil = new ForceFieldAbility(Math.min(25 * 8, target.hitSize * 2.5f), Math.min(5000, target.type.health * 0.5f) / (2.5f * 60), Math.min(5000, target.type.health * 0.5f), Math.min(20 * 60f, target.hitSize * 60));
                    target.abilities.add(abil);
                    Time.run(60 * 60 * 60, () -> target.abilities.remove(abil));
                }));
            }, 20, Core.bundle.get("stat.shar.shieldreceive-name"));
            skillDescriptions.add(Core.bundle.get("stat.shar.shieldreceive-description"));
            skillStats.add(
                    table -> {
                        table.top();
                        table.add(Core.bundle.format("stat.shar.receiveRange", range));
                    }
            );

            hasItems = true;
            itemCapacity = 150;
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


            health = 160 * size * size;
            shootSound = Sounds.shotgun;
        }};

        asclepius = new MultiConstructTurret("asclepius"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 820, Items.lead, 430, Items.graphite, 320, Items.silicon, 580, Items.titanium, 120, Items.thorium, 140, Items.plastanium, 85));

            addBaseTurret(SBullets.artilleryHealBig, Items.plastanium,"Asclepius");
            addMountTurret(healBeamMountR, healBeamMountL, healMissileMountL, healMissileMountR, healLaserMount2);
            addCustomMountLocation(new Float[]{
                    -7.25f, 2f,
                    7.25f, 2f,
                    -10f, -4.5f,
                    10f, -4.5f,
                    0f, 1.5f});

            ammos(MultiTurretMount.MultiTurretMountType.power);
            ammos(MultiTurretMount.MultiTurretMountType.power);
            ammos(MultiTurretMount.MultiTurretMountType.power);
            ammos(MultiTurretMount.MultiTurretMountType.power);
            ammos(MultiTurretMount.MultiTurretMountType.repair);

            addSkills(entity -> () -> {
                final float shotAmount = 5;
                BulletType type = SBullets.force;
                for(int i = 0; i < shotAmount; i++) {
                    float angle = entity.rotation + Mathf.range(inaccuracy) + (i % 2 == 0 ? -i : i) * (90 / shotAmount);
                    Time.run(10 * i, () -> entity.bullet(type, angle, 0));
                }
            }, 3, Core.bundle.get("stat.shar.fireforce-name"));

            addSkills(entity -> () -> {
                final float shotAmount = 3;
                BulletType type = SBullets.assault;
                for(int i = 0; i < shotAmount; i++) {
                    float angle = entity.rotation + Mathf.range(inaccuracy) + (i % 2 == 0 ? -i : i) * (90 / shotAmount);
                    Time.run(20 * i, () -> entity.bullet(type, angle, 0));
                }
            }, 5, Core.bundle.get("stat.shar.fireassault-name"));
            skillDescriptions.add(Core.bundle.get("stat.shar.fireforce-description"), Core.bundle.get("stat.shar.fireassault-description"));
            skillStats.add(
                    table -> {
                        table.top();
                        table.add(Core.bundle.format("stat.shar.constructamount", 5));
                    },
                    table -> {
                        table.top();
                        table.add(Core.bundle.format("stat.shar.constructamount", 3));
                    }
            );
            customMountLocation = true;
            hasLiquids = true;
            hasItems = true;
            hasPower = true;
            size = 4;
            shots = 6;
            maxConstruct = 18;
            inaccuracy = 45f;
            reloadTime = 2.5f * 60f;
            ammoEjectBack = 5f;
            ammoUseEffect = Fx.casing3Double;
            ammoPerShot = 8;
            cooldown = 0.03f;
            velocityInaccuracy = 0.3f;
            restitution = 0.04f;
            recoilAmount = 8f;
            shootShake = 3.5f;
            range = 45 * 8f;
            burstSpacing = 7f;


            minRanged = 30 * 8f;
            inaccuracy = 25f;
            health = 370 * size * size;
            shootSound = Sounds.artillery;
        }};

        clinicus = new MultiConstructTurret("clinicus"){{
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
            size = 3;
            shots = 3;
            inaccuracy = 12f;
            reloadTime = 1.5f * 60f;
            ammoEjectBack = 5f;
            ammoUseEffect = Fx.casing3Double;
            ammoPerShot = 4;
            cooldown = 0.03f;
            velocityInaccuracy = 0.2f;
            restitution = 0.02f;
            recoilAmount = 6f;
            shootShake = 2f;
            range = 32 * 8f;
            burstSpacing = 5f;

            minRanged = 25 * 8f;
            inaccuracy = 17f;
            health = 170 * size * size;
            shootSound = Sounds.artillery;
        }};

        fossor = new MultiItemConstructTurret("fossor"){{
            configurable = true;
            requirements(Category.turret, ItemStack.with(Items.copper, 300, Items.lead, 180, Items.graphite, 140, Items.silicon, 200, Items.titanium, 180, Items.thorium, 130));

            addBaseTurret(SBullets.fossers, Items.plastanium,"Fossor");
            addMountTurret(drillMount, drillMount, massMount);
            addCustomMountLocation(new Float[]{
                -3f, -4f,
                3f, -4f,
                0f, -5f
            });

            ammos(MultiTurretMount.MultiTurretMountType.drill);
            ammos(MultiTurretMount.MultiTurretMountType.drill);
            ammos(MultiTurretMount.MultiTurretMountType.mass);

            itemCapacity = 150;
            customMountLocation = true;
            hasLiquids = true;
            hasItems = true;
            hasPower = true;
            size = 3;
            shots = 5;
            inaccuracy = 12f;
            reloadTime = 2f * 60f;
            ammoEjectBack = 5f;
            ammoUseEffect = Fx.casing3Double;
            ammoPerShot = 3;
            cooldown = 0.03f;
            velocityInaccuracy = 0.2f;
            restitution = 0.02f;
            recoilAmount = 6f;
            shootShake = 2f;
            range = 28 * 8f;
            burstSpacing = 5f;

            inaccuracy = 20f;
            health = 120 * size * size;
            shootSound = Sounds.artillery;
        }};

        jumble = new MultiTurret("multi-i"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 135, Items.lead, 75, Items.metaglass, 40, Items.graphite, 80, Items.silicon, 50));

            addBaseTurret(jumbleBullet, Items.graphite, "Aggregate");
            addMountTurret(unoMount, waveMount, hailMount);

            ammos(MultiTurretMount.MultiTurretMountType.power);
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

            shootCone = 35;
            size = 2;
            range = 15 * 8;
            maxAmmo = 30;
            recoilAmount = 2;
            reloadTime = 21;
            ammoPerShot = 2;
            hasPower = true;

            health = 80 * size * size;
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

        armedFoundation = new BattleCore("armedFoundation"){{
            customMountLocation = true;
            hasPower = true;
            requirements(Category.effect, with(Items.copper, 3000, Items.lead, 3000, Items.silicon, 2000));

            unitType = UnitTypes.beta;
            health = 3500;
            itemCapacity = 9000;
            size = 4;

            unitCapModifier = 16;
            researchCostMultiplier = 0.02f;

            addMountTurret(healLaserMount2, healBeamMountR, healBeamMountL);
            addCustomMountLocation(new Float[]{
                0f,0f,
                0f, -10f,
                0f, 10f
            });
            ammos(MultiTurretMount.MultiTurretMountType.repair);
            ammos(MultiTurretMount.MultiTurretMountType.power);
            ammos(MultiTurretMount.MultiTurretMountType.power);
        }};

        armedNucleus = new BattleCore("armedNucleus"){{
            customMountLocation = true;
            hasLiquids = true;
            hasItems = true;
            hasPower = true;
            requirements(Category.effect, with(Items.copper, 8000, Items.lead, 8000, Items.silicon, 5000, Items.thorium, 4000));

            unitType = UnitTypes.gamma;
            health = 6000;
            itemCapacity = 13000;
            size = 5;

            unitCapModifier = 24;
            researchCostMultiplier = 0.03f;

            addMountTurret(hailMount, hailMount, hailMount, hailMount, unoMount, unoMount, unoMount, unoMount);
            addCustomMountLocation(new Float[]{
                13f, 13f,
                -13f, 13f,
                13f, -13f,
                -13f, -13f,
                10f, 0f,
                0f, 10f,
                -10f, 0f,
                0f, -10f
            });

            for(int i = 0; i < 4; i++) {
                ammos(MultiTurretMount.MultiTurretMountType.item,
                    Items.graphite, Bullets.artilleryDense,
                    Items.silicon, Bullets.artilleryHoming,
                    Items.pyratite, Bullets.artilleryIncendiary
                );
            }
            ammos(MultiTurretMount.MultiTurretMountType.power);
            ammos(MultiTurretMount.MultiTurretMountType.power);
            ammos(MultiTurretMount.MultiTurretMountType.power);
            ammos(MultiTurretMount.MultiTurretMountType.power);
        }};
    }
}
