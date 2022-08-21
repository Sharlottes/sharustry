package Sharustry.content;

import Sharustry.entities.bullet.AccelBulletType;
import Sharustry.entities.bullet.FieldBulletType;
import Sharustry.entities.pattern.ShootAside;
import Sharustry.graphics.SPal;
import Sharustry.world.blocks.defense.turret.*;
import Sharustry.world.blocks.logic.VariableLogicBlock;
import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Structs;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.pattern.ShootAlternate;
import mindustry.entities.pattern.ShootBarrel;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import Sharustry.world.blocks.defense.*;
import Sharustry.world.blocks.production.*;
import arc.graphics.Color;
import mindustry.gen.*;
import mindustry.graphics.Trail;
import mindustry.type.*;
import mindustry.content.*;
import mindustry.world.*;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawTurret;
import mindustry.world.meta.*;

import static Sharustry.content.SBullets.jumbleBullet;
import static Sharustry.content.SFx.missileDead;
import static Sharustry.content.STurretMounts.*;
import static mindustry.type.ItemStack.*;

public class SBlocks {
    public static Block
            //logic
            variableProcessor,
            //turret
            //flucturbare,
            sasitil, balkan, latusis, traislar, jumble, conductron, trinity, asclepius, clinicus, fossor,
            //defense
            shieldWall, explodeMine,
            //drill
            adaptDrill, multiDrill;

    public static void load(){
        variableProcessor = new VariableLogicBlock("variable-processor"){{
            requirements(Category.logic, with(Items.copper, 80, Items.lead, 50, Items.silicon, 30));

            instructionsPerTick = 2;
            size = 1;
            range = 500 * 8f;
        }};
        /*
        flucturbare = new AsideTurret("flucturbare") {{
            requirements(Category.turret, ItemStack.with(Items.titanium, 750, Items.thorium, 860, Items.lead, 1000));

            ammoType = "item";
            ammo(Items.titanium, SBullets.accelerMissile);
            spread = 5;
            shots = 2;
            size = 3;
            shootCone = 120f;
            range = 50*8f;
            minRanged = 13*8f;
            chargeTime = 55f;
            chargeMaxDelay = 35f;
            chargeEffects = 5;
            recoilAmount = 12f;
            reload = 110f;
            cooldown = 0.1f;
            shake = 6f;
            shootEffect = SFx.balkanShoot;
            smokeEffect = Fx.none;
            chargeEffect = SFx.balkanChargeCircles;
            chargeBeginEffect = SFx.balkanChargeBegin;
            heatColor = Color.blue;
            health = 150 * size * size;
            shootSound = Sounds.missile;
            hasPower = true;
            targetAir = true;

        }};
        */
        sasitil = new GetlingTurret("sasitil") {{
            ammoType = "item";
            ammo(Items.blastCompound, new MissileBulletType(3.7f, 10){{
                width = 8f;
                height = 8f;
                shrinkY = 0f;
                splashDamageRadius = 30f;
                splashDamage = 30f * 1.5f;
                ammoMultiplier = 5f;
                hitEffect = Fx.blastExplosion;
                despawnEffect = Fx.blastExplosion;

                status = StatusEffects.blasted;
                statusDuration = 60f;
            }});

            inaccuracy = 25f;
            range = 17*8f;
            recoil = 4f;
            reload = 60f;
            shake = 4f;
            shootCone = 120f;
            heatColor = Color.red;
            size = 3;
            health = 140 * size * size;
            shootSound = Sounds.missile;

            targetAir = true;
            hasPower = true;

            requirements(Category.turret, with(Items.copper, 250, Items.lead, 80, Items.titanium, 40, Items.silicon, 60));
        }};

        balkan = new SkillTurret("balkan"){{
            addSkills(entity -> () -> {
                for(int i = 0; i < 5; i++){
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
                            q.image(Core.atlas.find("shar-over-freezing")).padLeft(15 * 8f);
                            q.add("[stat]" + SStatusEffects.overFreezing.localizedName +"[]");
                            q.pack();
                        }));
                    }});
                }
            );
            ammoType = "item";
            ammo(Items.titanium, new AccelBulletType(2.5f, 25){{
                backColor = SPal.cryoium.cpy().mul(Items.titanium.color);
                frontColor = trailColor = SPal.cryoium;
                shrinkY = 0f;
                width = 4f;
                height = 16f;
                hitSound = Sounds.explosion;
                trailChance = 0.2f;
                lifetime = 47f;
                sprite = "bullet";
                pierce = true;
                pierceBuilding = true;
                pierceCap = 3;
                pierceDec = 0.5f;
                damageMultiplier = 1.3f;
                shootEffect = SFx.balkanShoot;
                despawnEffect = missileDead;
                hitEffect = missileDead;
            }});

            range = 30*8f;
            moveWhileCharging = false;
            accurateDelay = false;
            shoot.firstShotDelay = 40f;
            recoil = 4f;
            reload = 90f;
            cooldownTime = 0.05f;
            shake = 4f;
            shootEffect = SFx.balkanShoot;
            smokeEffect = Fx.none;
            heatColor = Color.blue;
            size = 2;
            health = 110 * size * size;
            shootSound = Sounds.missile;

            targetAir = true;
            hasPower = true;

            requirements(Category.turret, with(Items.copper, 250, Items.lead, 80, Items.titanium, 40, Items.silicon, 60));
        }};

        traislar = new TemplatedTurret("traislar") {{
            requirements(Category.turret, ItemStack.with(Items.titanium, 700, Items.thorium, 800, Items.lead, 1000));

            ammoType = "item";
            ammo(Items.titanium, new BasicBulletType(4f, 75){{
                chargeEffect = new MultiEffect(SFx.balkanChargeCircles, SFx.balkanChargeBegin);
                frontColor = trailColor = SPal.cryoium;
                width = 4f;
                height = 16f;
                hitSound = Sounds.explosion;
                trailChance = 0.2f;
                lifetime = 120f;
                sprite = "bullet";
                pierce = true;
                pierceBuilding = true;
                absorbable = false;
                pierceCap = 12;
                shootEffect = SFx.balkanShoot;
                despawnEffect = missileDead;
                hitEffect = missileDead;
            }
                @Override
                public void update(Bullet b){
                    super.update(b);
                    b.damage += Time.delta * 1.5f;
                    ((BulletData)b.data).heat += Time.delta;
                    if(((BulletData)b.data).heat > 2.5) {
                        ((BulletData)b.data).heat = 0f;
                        SBullets.trailBullet.create(b, b.team, b.x, b.y, b.rotation(),  0f, 1);
                    }
                }

                @Override
                public void init(Bullet b){
                    b.data = new BulletData(Seq.with(new Trail(6), new Trail(3)), 0f);
                }

                @Override
                public void draw(Bullet b){
                    drawTrail(b);
                    Draw.color(Pal.lancerLaser);
                    ((BulletData)b.data).trails.each(t->t.draw(this.frontColor, this.width));

                    Drawf.tri(b.x, b.y, width, height, b.rotation());
                    Drawf.tri(b.x, b.y, width, height/2, b.rotation()+180);
                }

                @Override
                public void hit(Bullet b, float x, float y) {
                    super.hit(b, x, y);
                    b.damage*=0.75f;
                }

                class BulletData {
                    final Seq<Trail> trails;
                    float heat;

                    public BulletData(Seq<Trail> trails, float heat) {
                        this.trails = trails;
                        this.heat = heat;
                    }
                }
            });
            size = 3;
            shootCone = 30f;
            range = 60*8f;
            shoot.firstShotDelay = 60f;
            recoil = 12f;
            reload = 150f;
            cooldownTime = 0.1f;
            shake = 6f;
            shootEffect = SFx.traislarShoot;
            smokeEffect = Fx.none;
            heatColor = Color.blue;
            health = 150 * size * size;
            shootSound = Sounds.missile;

            unitSort = (u, x, y) -> -u.dst2(x, y);
            moveWhileCharging = false;
            accurateDelay = false;
            hasPower = true;
            targetAir = true;
        }};

        latusis = new TemplatedTurret("latusis") {{
            requirements(Category.turret, ItemStack.with(Items.titanium, 750, Items.thorium, 860, Items.lead, 1000));

            ammoType = "item";
            ammo(Items.titanium, new AccelBulletType(3f, 45){{
                chargeEffect = new MultiEffect(SFx.balkanChargeCircles, SFx.balkanChargeBegin);
                backColor = SPal.cryoium.cpy().mul(Items.titanium.color);
                frontColor = trailColor = SPal.cryoium;
                shrinkY = 0f;
                width = 4f;
                height = 16f;
                hitSound = Sounds.explosion;
                trailChance = 0.2f;
                lifetime = 67f;
                sprite = "bullet";
                homing = true;
                pierce = true;
                pierceBuilding = true;
                pierceCap = 5;
                pierceDec = 0.75f;
                damageMultiplier = 1.5f;
                shootEffect = SFx.balkanShoot;
                despawnEffect = missileDead;
                hitEffect = missileDead;
            }});

            shoot = new ShootAside();
            shoot.shots = 2;
            shoot.firstShotDelay = 55f;
            size = 3;
            shootCone = 120f;
            range = 50*8f;
            minRanged = 13*8f;
            recoil = 12f;
            reload = 110f;
            cooldownTime = 0.1f;
            shake = 6f;
            shootEffect = SFx.balkanShoot;
            smokeEffect = Fx.none;
            heatColor = Color.blue;
            health = 150 * size * size;
            shootSound = Sounds.missile;
            unitSort = (u, x, y) -> (u.isFlying()?+1:-1);
            moveWhileCharging = false;
            accurateDelay = false;
            hasPower = true;
            targetAir = true;
        }};

        conductron = new MultiTurret("conductron"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 200, Items.lead, 150, Items.silicon, 125, Items.graphite, 95, Items.titanium, 70));

            addBaseTurret(new LaserBulletType(140){{
                chargeEffect = new MultiEffect(Fx.lancerLaserCharge, Fx.lancerLaserChargeBegin);
                colors = new Color[]{Pal.lancerLaser.cpy().a(0.4f), Pal.lancerLaser, Color.white};
                hitEffect = Fx.hitLancer;
                despawnEffect = Fx.none;
                hitSize = 4;
                lifetime = 16f;
                drawSize = 400f;
                collidesAir = false;
                length = 173f;
            }}, Items.titanium, "Conductron");
            addMountTurret(arcMount);
            addMountTurret(arcMount);
            addMountTurret(laserMount);

            addCustomMountLocation(new Float[]{
                    -6.5f, -4.25f,
                    6.5f, -4.25f,
                    0f, 1.5f
            });

            hasPower = true;
            size = 3;
            maxAmmo = 30;
            ammoPerShot = 3;
            customMountLocation = true;
            range = 180f;
            moveWhileCharging = false;
            accurateDelay = false;
            shoot.firstShotDelay = 40f;
            recoil = 2f;
            reload = 80f;
            cooldownTime = 0.03f;
            shake = 2f;
            shootEffect = Fx.lancerLaserShoot;
            smokeEffect = Fx.none;
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
            addSkills(entity -> () -> {
                if(Groups.unit.find(u -> Mathf.dst(entity.x, entity.y, u.x, u.y) <= range) == null
                        || Groups.unit.count(u -> Mathf.dst(entity.x, entity.y, u.x, u.y) <= range
                        && Structs.find(u.abilities, a -> a instanceof ForceFieldAbility) != null) == Groups.unit.count(u -> Mathf.dst(entity.x, entity.y, u.x, u.y) <= range)) return;
                SFx.shieldSpread.at(entity.x, entity.y, 0, range);

                Time.run(30, () -> Groups.unit.each(u -> Mathf.dst(entity.x, entity.y, u.x, u.y) <= range, target -> {
                    if(Structs.find(target.abilities, a -> a instanceof ForceFieldAbility) != null) return;
                    ForceFieldAbility abil = new ForceFieldAbility(Math.min(25 * 8, target.hitSize * 2.5f), Math.min(5000, target.type.health * 0.5f) / (2.5f * 60), Math.min(5000, target.type.health * 0.5f), Math.min(20 * 60f, target.hitSize * 60));
                    target.abilities(Structs.add(target.abilities, abil));
                    Time.run(60 * 60 * 60, () -> target.abilities(Structs.remove(target.abilities, abil)));
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
            shoot = new ShootAlternate(5f);
            shoot.shots = 2;
            reload = 20f;
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
                    0f, 1.5f
            });


            addSkills(entity -> () -> {
                final float shotAmount = 5;
                BulletType type = SBullets.force;
                for(int i = 0; i < shotAmount; i++) {
                    float
                        xSpread = Mathf.range(xRand),
                        bulletX = entity.x + Angles.trnsx(entity.rotation - 90, shootX + xSpread, shootY),
                        bulletY = entity.y + Angles.trnsy(entity.rotation - 90, shootX + xSpread, shootY),
                        angle = entity.rotation + Mathf.range(inaccuracy) + (i % 2 == 0 ? -i : i) * (90 / shotAmount);
                    Time.run(10 * i, () -> {
                        float lifeScl = !type.scaleLife ? 1f
                            : Mathf.clamp(Mathf.dst(bulletX, bulletY, entity.targetPos.x, entity.targetPos.y) / type.range, minRange / type.range, range / type.range);

                        entity.handleBullet(type.create(entity, entity.team, bulletX, bulletY, angle, -1f, (1f - velocityRnd) + Mathf.random(velocityRnd), lifeScl, null, null, entity.targetPos.x, entity.targetPos.y), 0, 0, angle - entity.rotation);
                    });
                }
            }, 3, Core.bundle.get("stat.shar.fireforce-name"));

            addSkills(entity -> () -> {
                final float shotAmount = 3;
                BulletType type = SBullets.assault;
                for(int i = 0; i < shotAmount; i++) {
                    float
                        xSpread = Mathf.range(xRand),
                        bulletX = entity.x + Angles.trnsx(entity.rotation - 90, shootX + xSpread, shootY),
                        bulletY = entity.y + Angles.trnsy(entity.rotation - 90, shootX + xSpread, shootY),
                        angle = entity.rotation + Mathf.range(inaccuracy) + (i % 2 == 0 ? -i : i) * (90 / shotAmount);
                    Time.run(20 * i, () -> {
                        float lifeScl = !type.scaleLife ? 1f
                                : Mathf.clamp(Mathf.dst(bulletX, bulletY, entity.targetPos.x, entity.targetPos.y) / type.range, minRange / type.range, range / type.range);

                        entity.handleBullet(type.create(entity, entity.team, bulletX, bulletY, angle, -1f, (1f - velocityRnd) + Mathf.random(velocityRnd), lifeScl, null, null, entity.targetPos.x, entity.targetPos.y), 0, 0, angle - entity.rotation);
                    });
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
            shoot = new ShootBarrel();
            ((ShootBarrel) shoot).barrelOffset = 7;
            shoot.shots = 6;
            maxConstruct = 18;
            reload = 2.5f * 60f;
            ammoEjectBack = 5f;
            ammoUseEffect = Fx.casing3Double;
            ammoPerShot = 8;
            cooldownTime = 0.03f;
            recoil = 8f;
            shake = 3.5f;
            range = 45 * 8f;

            minRanged = 30 * 8f;
            inaccuracy = 25f;
            health = 370 * size * size;
            shootSound = Sounds.artillery;
        }};

        clinicus = new MultiConstructTurret("clinicus"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 335, Items.lead, 210, Items.graphite, 180, Items.silicon, 250, Items.thorium, 90));

            addBaseTurret(SBullets.artilleryHeal, Items.plastanium,"Clinicus");
            addMountTurret(healBeamMount, healBeamMount, healLaserMount);

            addCustomMountLocation(new Float[]{
                    6.75f, -2.5f,
                    -6.5f, -2.5f,
                    0f, 1f
            });

            customMountLocation = true;
            hasLiquids = true;
            hasItems = true;
            hasPower = true;
            size = 3;
            shoot = new ShootBarrel();
            ((ShootBarrel) shoot).barrelOffset = 5;
            shoot.shots = 3;
            reload = 1.5f * 60f;
            ammoEjectBack = 5f;
            ammoUseEffect = Fx.casing3Double;
            ammoPerShot = 4;
            cooldownTime = 0.03f;
            recoil = 6f;
            shake = 2f;
            range = 32 * 8f;

            minRanged = 25 * 8f;
            inaccuracy = 17f;
            health = 170 * size * size;
            shootSound = Sounds.artillery;
        }};

        fossor = new MultiConstructTurret("fossor"){{
            configurable = true;
            requirements(Category.turret, ItemStack.with(Items.copper, 300, Items.lead, 180, Items.graphite, 140, Items.silicon, 200, Items.titanium, 180, Items.thorium, 130));

            addBaseTurret(SBullets.fossers, Items.plastanium,"Fossor");
            addMountTurret(miniDrillMount, miniDrillMount, miniMassMount, miniMassMount);

            addCustomMountLocation(new Float[]{
                    -7f, -8f,
                    7f, -8f,
                    0f, -4f,
                    0f, 0f
            });
            drawer = new DrawTurret() {
                TextureRegion left, leftOutline, right, rightOutline;
                float offsetX = 1.5f, offsetY = 0;

                @Override
                public void load(Block block) {
                    super.load(block);

                    left = Core.atlas.find(name + "-left");
                    right = Core.atlas.find(name + "-right");
                    leftOutline = Core.atlas.find(name + "-left" + "-outline");
                    rightOutline = Core.atlas.find(name + "-right" + "-outline");
                }

                @Override
                public void draw(Building build) {
                    super.draw(build);
                    for(int i : Mathf.signs) {
                        Tmp.v5.set(build.x, build.y).trns(build.rotation, offsetX - recoil, i * offsetY);
                        Drawf.shadow(i == -1 ? leftOutline : rightOutline, Tmp.v5.x - (size / 2f), Tmp.v5.y - (size / 2f), build.drawrot());
                        Draw.rect(i == -1 ? leftOutline : rightOutline, Tmp.v5.x, Tmp.v5.y, build.drawrot());
                        Tmp.v5.set(build.x, build.y).trns(build.rotation, offsetX - recoil, i * offsetY);
                        Draw.rect(i == -1 ? left : right, Tmp.v5.x, Tmp.v5.y, build.drawrot());
                    }
                }
            };
            itemCapacity = 150;
            customMountLocation = true;
            hasLiquids = true;
            hasItems = true;
            hasPower = true;
            size = 4;
            shoot = new ShootBarrel();
            ((ShootBarrel) shoot).barrelOffset = 5;
            shoot.shots = 5;
            reload = 2f * 60f;
            ammoEjectBack = 5f;
            ammoUseEffect = Fx.casing3Double;
            ammoPerShot = 3;
            cooldownTime = 0.03f;
            recoil = 6f;
            shake = 2f;
            range = 28 * 8f;
            inaccuracy = 20f;
            health = 120 * size * size;
            shootSound = Sounds.artillery;
        }};

        jumble = new MultiTurret("multi-i"){{
            requirements(Category.turret, ItemStack.with(Items.copper, 135, Items.lead, 75, Items.metaglass, 40, Items.graphite, 80, Items.silicon, 50));

            addBaseTurret(jumbleBullet, Items.graphite, "Aggregate");
            addMountTurret(unoMount, waveMount, hailMount);

            shootCone = 35;
            size = 2;
            range = 15 * 8;
            maxAmmo = 30;
            recoil = 2;
            reload = 21;
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
            consumePower(1.10f);

            consumeLiquid(Liquids.water, 0.08f).boost();
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
            consumePower(1.10f);
            consumeLiquid(Liquids.water, 0.08f).boost();
        }};

        shieldWall = new ShieldWall("shield-wall"){{
            requirements(Category.defense, with(Items.titanium, 6));
            health = 150 * 4;
            armor = 3;
        }};

        explodeMine = new ExplodeMine("explode-mine"){{
            requirements(Category.defense, with(Items.lead, 30, Items.silicon, 25, Items.blastCompound, 5));
            size = 2;
            health = 50;
            cooldownTime = 50;
            shots = 10;
            inaccuracy = 15;
            bullet = new BulletType(4f, 60f){{
                ammoMultiplier = 6f;
                hitSize = 7f;
                lifetime = 18f;
                pierce = true;
                collidesAir = false;
                statusDuration = 60f * 10;
                shootEffect = Fx.shootPyraFlame;
                hitEffect = Fx.hitFlameSmall;
                despawnEffect = Fx.none;
                status = StatusEffects.burning;
                hittable = false;
            }};
            shotsSpacing = 0.25f;
        }};
    }
}
