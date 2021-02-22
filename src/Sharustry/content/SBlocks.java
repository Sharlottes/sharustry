package Sharustry.content;

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

import static Sharustry.content.SBullets.mainBullet;
import static Sharustry.content.STurretMounts.*;
import static mindustry.type.ItemStack.*;

public class SBlocks implements ContentList{
    public static Block
            //defense
            balkan, jumble,
            //wall
            shieldWall,
            //drill
            adaptDrill, multiDrill,
            //crafterator
            multi;

    @Override
    public void load(){
        balkan = new SharTurret("balkan"){{
            ammo(
                Items.titanium, SBullets.accelMissile,
                Items.pyratite, SBullets.testLaser
            );
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

        jumble = new MultiTurret("multi-i", Items.graphite, mainBullet, "Aggregate", unoMount, waveMount, hailMount){{
            requirements(Category.turret, ItemStack.with(Items.copper, 135, Items.lead, 75, Items.metaglass, 40, Items.graphite, 80, Items.silicon, 50));
            ammos(MultiTurretMount.MountAmmoType.power,
                Items.copper, Bullets.standardCopper,
                Items.graphite, Bullets.standardDense,
                Items.pyratite, Bullets.standardIncendiary,
                Items.silicon, Bullets.standardHoming
            );

            ammos(MultiTurretMount.MountAmmoType.liquid,
                Liquids.water, SBullets.miniWater,
                Liquids.slag, SBullets.miniSlag,
                Liquids.cryofluid, SBullets.miniCryo,
                Liquids.oil, SBullets.miniOil
            );

            ammos(MultiTurretMount.MountAmmoType.item,
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
