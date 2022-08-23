package Sharustry.world.blocks.defense.turret.mounts;

import Sharustry.content.STurretMounts;
import Sharustry.world.blocks.defense.turret.DriverBulletData;
import Sharustry.world.blocks.defense.turret.MultiTurret;
import arc.Core;
import arc.audio.Sound;
import arc.func.Boolf;
import arc.func.Func2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.audio.SoundLoop;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.core.World;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.*;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.pattern.ShootPattern;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.ui.MultiReqImage;
import mindustry.ui.ReqImage;
import mindustry.world.consumers.ConsumeLiquidFilter;
import mindustry.world.meta.BlockStatus;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.*;

public class MountTurretType {
    public int ammoPerShot = 1;

    public ShootPattern shoot = new ShootPattern();
    public float shootCone = 8;
    public float shootX = 0, shootY = Float.NEGATIVE_INFINITY;
    public float xRand = 0, yRand = 0;
    public float width = 3, height = 3;
    public float elevation = 1;

    public float reload = 30;
    public float maxAmmo = 20;
    public float range = 80;
    public float rotateSpeed = 5;
    public float inaccuracy = 0;
    public float velocityRnd = 0;
    /** Visual amount by which the turret recoils back per shot. */
    public float recoil = 1f;
    /** ticks taken for turret to return to starting position in ticks. uses reload time by default  */
    public float recoilTime = -1f;
    /** power curve applied to visual recoil */
    public float recoilPow = 1.8f;
    public float cooldown = 0.02f;
    public float loopVolume = 1;
    public float shake = 0;
    public float minRange = 0;
    public float spread = 0;
    public float cooldownTime = 20f;
    public float powerUse = 0f;
    public float soundPitchMin = 0.9f, soundPitchMax = 1.1f;
    public boolean consumeAmmoOnce = false;
    public boolean moveWhileCharging = false;
    public boolean sequential = false;
    public boolean targetAir = true, targetGround = true;
    public Boolf<Unit> unitFilter = u -> true;
    public Boolf<Building> buildingFilter = b -> !b.block.underBullets;
    public Color heatColor = Pal.turretHeat;

    public String name;
    public String title = "ohno";

    public Sound shootSound = Sounds.pew;
    public Sound loopSound = Sounds.none;
    public Effect ammoUseEffect = Fx.none;
    public Effect shootEffect = Fx.none;
    public Effect smokeEffect = Fx.none;
    public Effect coolEffect = Fx.fuelburn;
    public Units.Sortf unitSort = Unit::dst2;

    //charge region
    public float chargeTime = -1;
    public float shootLength = 8;
    public float chargeEffects = 5;
    public float chargeMaxDelay = 48;
    public float ammoEjectBack = 1f;
    public Effect chargeEffect = Fx.none, chargeBeginEffect = Fx.none;
    public Sound chargeSound = Sounds.none;
    //region end

    public Color laserColor = Color.white;
    public boolean targetHealing = false;
    public boolean accurateDelay = true;
    public boolean linearWarmup = false;
    public float shootWarmupSpeed = 0.1f;
    public float minWarmup = 0f;
    public float laserWidth = 0.6f;
    public float shootSoundVolume = 0.9f;
    //skill
    public Seq<Integer> skillDelays = new Seq<>();
    public Seq<Func2<Building, MountTurretType, Runnable>> skillSeq = new Seq<>();

    public TextureRegion laser, laserEnd, region;
    public DrawMountTurret drawer = new DrawMountTurret();

    public MountTurretType(String name) {
        this.name = name;
        STurretMounts.mounttypes.add(this);
    }

    public MountTurret<MountTurretType> create(MultiTurret block, MultiTurret.MultiTurretBuild build, int index, float x, float y) {
        return new MountTurret<>(this, block, build, index, x, y);
    }

    public void init() {
        if(shootY == Float.NEGATIVE_INFINITY) shootY = tilesize / 2f;
        if(elevation < 0) elevation = 1 / 2f;
        if(recoilTime < 0f) recoilTime = reload;
        if(cooldownTime < 0f) cooldownTime = reload;
    }
    public void load(){
        region = Core.atlas.find("shar-" + name);
        drawer.load(this);
        laser = Core.atlas.find("shar-repair-laser");
        laserEnd = Core.atlas.find("shar-repair-laser-end");
    }

    public <T1 extends Building, T2 extends MountTurretType> void addSkills(Func2<T1, T2, Runnable> skill, int delay){
        if(skill != null) {
            skillSeq.add((Func2<Building, MountTurretType, Runnable>) skill);
            skillDelays.add(delay);
        }
    }

    public ObjectMap<ObjectMap<BulletType ,? extends UnlockableContent>, TextureRegion> getStatData() {
        return null;
    }
    public void buildStat(Table table) {
        table.add("[lightgray]" + Stat.shootRange.localized() + ": [white]" + Strings.fixed(range / tilesize, 1) + " " + StatUnit.blocks).row();
        table.add("[lightgray]" + Stat.targetsAir.localized() + ": [white]" + (!targetAir ? Core.bundle.get("no") : Core.bundle.get("yes"))).row();
        table.add("[lightgray]" + Stat.targetsGround.localized() + ": [white]" + (!targetGround ? Core.bundle.get("no") : Core.bundle.get("yes"))).row();
        if(reload > 0) table.add("[lightgray]" + Stat.reload.localized() + ": [white]" + Strings.autoFixed(60 / reload * shoot.shots, 1)).row();
        if(inaccuracy > 0) table.add("[lightgray]" + Stat.inaccuracy.localized() + ": [white]" + inaccuracy + " " + StatUnit.degrees.localized()).row();
        if(chargeTime > 0.001f) table.add("[lightgray]" + Core.bundle.get("stat.shar.chargeTime") + ": [white]" + Mathf.round(chargeTime/60, 100) + " " + Core.bundle.format("stat.shar.seconds")).row();
    }

    public void addStats(Table w){
        w.left().row();
        w.add(title).right().top().row();
        w.add(new Image(region){
            final TextureRegionDrawable outline = new TextureRegionDrawable(drawer.outline);
            @Override
            public void draw() {
                outline.draw(x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);
                super.draw();
            }
        }).size(60).scaling(Scaling.bounded).right().top();
        w.table(Tex.underline, h -> {
            h.left().defaults().padRight(3).left();
            buildStat(h);
            h.row();
            h.table(b -> {
                ObjectMap<ObjectMap<BulletType ,? extends UnlockableContent>, TextureRegion> types = getStatData();
                if(types == null){
                    b.stack(
                        new Table(o -> {
                            o.right();
                            o.image(Icon.power.getRegion()).size(8 * 3).padRight(4);
                        }),
                        new Table(t -> {
                            t.right().bottom();
                            t.add(String.valueOf((int)powerUse * 60)).fontScale(0.9f).color(Color.yellow).padTop(8);
                            t.pack();
                        })
                    ).padRight(4).right().top();
                    b.add(Core.bundle.format("stat.shar.power")).padRight(10).left().top();
                } else {
                    for(ObjectMap<BulletType, ? extends UnlockableContent> type : types.keys()){
                        int ii = 0;

                        TextureRegion contentImage = types.get(type);
                        for(BulletType bullet : type.keys()){
                            ii ++;

                            UnlockableContent content = type.get(bullet);
                            if(content == null) {
                                b.stack(
                                    new Table(o -> {
                                        o.right();
                                        o.image(Icon.power.getRegion()).size(8 * 3).padRight(4);
                                    }),
                                    new Table(t -> {
                                        t.right().bottom();
                                        t.add(String.valueOf((int)powerUse * 60)).fontScale(0.9f).color(Color.yellow).padTop(8);
                                        t.pack();
                                    })
                                ).padRight(4).right().top();
                                b.add(Core.bundle.format("stat.shar.power")).padRight(10).left().top();
                            } else {
                                b.image(contentImage).size(8 * 4).padRight(4).right().top();
                                b.add(content.localizedName).padRight(10).left().top();
                            }

                            b.table(Tex.underline, e -> {
                                e.left().defaults().padRight(3).left();

                                if(bullet.damage > 0 && (bullet.collides || bullet.splashDamage <= 0)) e.add(Core.bundle.format("bullet.damage", bullet.damage)).row();
                                if(bullet.buildingDamageMultiplier != 1) e.add(Core.bundle.format("bullet.buildingdamage", Strings.fixed((int)(bullet.buildingDamageMultiplier * 100),1))).row();
                                if(bullet.splashDamage > 0) e.add(Core.bundle.format("bullet.splashdamage", bullet.splashDamage, Strings.fixed(bullet.splashDamageRadius / tilesize, 1))).row();
                                if(bullet.ammoMultiplier > 0 && !Mathf.equal(bullet.ammoMultiplier, 1f)) e.add(Core.bundle.format("bullet.multiplier", Strings.fixed(bullet.ammoMultiplier, 1))).row();
                                if(!Mathf.equal(bullet.reloadMultiplier, 1f)) e.add(Core.bundle.format("bullet.reloadCounter", bullet.reloadMultiplier)).row();
                                if(bullet.knockback > 0) e.add(Core.bundle.format("bullet.knockback", Strings.fixed(bullet.knockback, 1))).row();
                                if(bullet.healPercent > 0) e.add(Core.bundle.format("bullet.healpercent", bullet.healPercent)).row();
                                if(bullet.pierce || bullet.pierceCap != -1) e.add(bullet.pierceCap == -1 ? "@bullet.infinitepierce" : Core.bundle.format("bullet.pierce", bullet.pierceCap)).row();
                                if(bullet.status == StatusEffects.burning || bullet.status == StatusEffects.melting || bullet.incendAmount > 0) e.add("@bullet.incendiary").row();
                                if(bullet.status == StatusEffects.freezing) e.add("@bullet.freezing").row();
                                if(bullet.status == StatusEffects.tarred) e.add("@bullet.tarred").row();
                                if(bullet.status == StatusEffects.sapped) e.add("@bullet.sapping").row();
                                if(bullet.homingPower > 0.01) e.add("@bullet.homing").row();
                                if(bullet.lightning > 0) e.add("@bullet.shock").row();
                                if(bullet.fragBullet != null) e.add("@bullet.frag").row();
                            }).padTop(-9).left();

                            if(ii % 4 == 0) b.row();
                        }
                    }
                }
            });
            h.row();
        }).padTop(-15).left();
        w.row();
    }

    public void drawPlace(MultiTurret block, int mount, int x, int y, int rotation, boolean valid){
        float fade = Mathf.curve(Time.time % block.totalRangeTime, block.rangeTime * mount, block.rangeTime * mount + block.fadeTime) - Mathf.curve(Time.time % block.totalRangeTime, block.rangeTime * (mount + 1) - block.fadeTime, block.rangeTime * (mount + 1));
        float tX = x * tilesize + block.offset + block.mountOffsets.get(mount)[0];
        float tY = y * tilesize + block.offset + block.mountOffsets.get(mount)[1];

        Lines.stroke(3, Pal.gray);
        Draw.alpha(fade);
        Lines.dashCircle(tX, tY, range);
        Lines.stroke(1, targetHealing ? Pal.heal : player.team().color);
        Draw.alpha(fade);
        Lines.dashCircle(tX, tY, range);
        Draw.color(player.team().color, fade);
        Draw.rect(drawer.mask, tX, tY);
        Draw.reset();
    }
    public void update(MountTurret mount, MultiTurret.MultiTurretBuild build) {
        boolean canShoot = true;

        if(build.isControlled()) { //player behavior
            mount.targetPos.set(build.unit().aimX, build.unit().aimY);
            canShoot = build.unit().isShooting;
        }else if(build.logicControlled()) { //logic behavior
            mount.targetPos = build.targetPos;
            canShoot = build.logicShooting;
        }else { //default AI behavior
            mount.targetPosition(mount.target);
            if(Float.isNaN(mount.rotation)) mount.rotation = 0f;
        }

        float targetRot = Angles.angle(mount.x, mount.y, mount.targetPos.x, mount.targetPos.y);

        if(!mount.charging) mount.turnToTarget(targetRot);

        if (Angles.angleDist(mount.rotation, targetRot) < shootCone && canShoot) {
            build.wasShooting = true;
            mount.wasShooting = true;
            mount.updateShooting();
        }
    }


    public static abstract class AmmoEntry{
        public int amount;

        public abstract BulletType type();
    }
    public class MountTurret<T extends MountTurretType> {
        public float reloadCounter = 0f;
        public float rotation = 90f;
        public float strength = 0f;
        public boolean wasShooting = false;
        public boolean charging = false;
        public float curRecoil, heat, reTargetHeat;
        public float shootWarmup, charge;
        public int totalShots;
        public float x, y, xOffset, yOffset;
        public int mountIndex;
        public int queuedBullets;
        public int skillCounter;
        public int totalAmmo;
        Seq<AmmoEntry> ammo = new Seq<>();
        public Posc target;
        public Vec2 targetPos = new Vec2();
        public Vec2 recoilOffset = new Vec2();
        @Nullable SoundLoop sound;
        public T type;
        public MultiTurret block;
        public MultiTurret.MultiTurretBuild build;
        public MountTurret(T type, MultiTurret block, MultiTurret.MultiTurretBuild build, int mountIndex, float xOffset, float yOffset){
            this.type = type;
            this.block = block;
            this.build = build;
            this.mountIndex = mountIndex;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.x = build.x + xOffset;
            this.y = build.y + yOffset;
        }

        public float getPowerEfficiency(){
            return Mathf.clamp(build.power.graph.getPowerBalance()/type.powerUse, 0, 1);
        }

        public BlockStatus status() {
            if(hasAmmo()) return BlockStatus.active;
            return BlockStatus.noInput;
        }

        public void control(LAccess type, double p1, double p2){
            if(type == LAccess.shoot && !build.unit.isPlayer())
                targetPos.set(World.unconv((float)p1), World.unconv((float)p2));
        }

        public void control(LAccess type, Object p1){
            if(type == LAccess.shootp && !build.unit.isPlayer() && p1 instanceof Posc pos){
                if(!hasAmmo()) return;
                BulletType bullet = peekAmmo();
                float speed = bullet.speed;
                //slow bullets never intersect
                if(speed < 0.1f) speed = 9999999f;

                targetPos.set(Predict.intercept(build, pos, speed));
                if(targetPos.isZero()) targetPos.set(pos);
            }
        }
        public boolean isShooting(){
            return (build.isControlled() ? build.unit.isShooting() : build.logicControlled() ? build.logicShooting : target != null);
        }
        public void removeFromProximity(){ }
        public void handleItem(Item item){ }
        public boolean acceptItem(Item item){
            return false;
        }

        public boolean acceptLiquid(Liquid liquid) {
            return false;
        }

        public int acceptStack(Item item, int amount){
            return 0;
        }

        public void display(Table table){
            if(block.mountTypes.size > 3 && mountIndex % 4 == 0) table.row();
            else if(mountIndex % 4 == 0) table.row();
            table.stack(
                    new Table(o -> {
                        o.left();
                        o.add(new Image(type.region){
                            final TextureRegionDrawable outline = new TextureRegionDrawable(type.drawer.outline);
                            @Override
                            public void draw() {
                                outline.draw(x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);
                                super.draw();
                            }
                        }).size(5*8f);
                    }),
                    new Table(h -> {
                        h.stack(
                                new Table(e -> {
                                    e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*2f);
                                    e.add(new Bar("", Pal.powerBar, this::getPowerEfficiency));
                                    e.pack();
                                }),
                                new Table(e -> {
                                    e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*5f);
                                    e.add(new Bar(() -> "", () -> Pal.accent.cpy().lerp(Color.orange, reloadCounter / type.reload), () -> reloadCounter / type.reload));
                                    e.pack();
                                }),
                                new Table(e -> {
                                    if(type.chargeTime <= 0) return;
                                    e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*8f);
                                    e.add(new Bar(() -> "", () -> Pal.surge.cpy().lerp(Pal.accent, charge / type.chargeTime), () -> charge));
                                    e.pack();
                                }),
                                new Table(e -> {
                                    MultiReqImage powerReq = new MultiReqImage();
                                    powerReq.add(new ReqImage(Icon.powerSmall.getRegion(), () -> getPowerEfficiency() >= 0.001f));
                                    e.add(powerReq);
                                })
                        ).padTop(2*8).padLeft(2*8);
                        h.pack();
                    })
            ).left().size(7*8f);
        }


        public void targetPosition(Posc pos){
            if(!hasAmmo() || pos == null) return;
            BulletType bullet = peekAmmo();

            Vec2 offset = Tmp.v1.setZero();

            //when delay is accurate, assume unit has moved by chargeTime already
            if(type.accurateDelay && pos instanceof Hitboxc h){
                offset.set(h.deltaX(), h.deltaY()).scl(type.shoot.firstShotDelay / Time.delta);
            }

            targetPos.set(Predict.intercept(build, pos, offset.x, offset.y, bullet.speed <= 0.01f ? 99999999f : bullet.speed));

            if(targetPos.isZero()){
                targetPos.set(pos);
            }
        }
        public float progress(){
            return Mathf.clamp(reloadCounter / type.reload);
        }

        public float warmup(){
            return shootWarmup;
        }
        public float drawrot(){
            return rotation - 90;
        }
        public void draw() {
            type.drawer.draw(this);
        }
        public void drawConfigure() { }

        public void handlePayload(Bullet bullet, DriverBulletData data){ }

        public void update() {
            //update locaiton
            Tmp.v1.trns(build.rotation - 90, xOffset, yOffset);
            Tmp.v1.add(build.x, build.y);
            x = Tmp.v1.x;
            y = Tmp.v1.y;

            if(!Vars.headless && sound!= null) {
                sound.update(x, y, wasShooting && !build.dead());
            }
        }
        public void created() {
            if(loopSound != Sounds.none) sound = new SoundLoop(loopSound, loopVolume);
        }
        public void removed() {
            if(sound != null) sound.stop();
        }
        public void updateTile() {
            if(!validateTarget()) target = null;

            float warmupTarget = isShooting() && build.canConsume() ? 1f : 0f;
            if(type.linearWarmup){
                shootWarmup = Mathf.approachDelta(shootWarmup, warmupTarget, type.shootWarmupSpeed * (warmupTarget > 0 ? build.efficiency : 1f));
            }else{
                shootWarmup = Mathf.lerpDelta(shootWarmup, warmupTarget, type.shootWarmupSpeed * (warmupTarget > 0 ? build.efficiency : 1f));
            }

            wasShooting = false;

            curRecoil = Mathf.approachDelta(curRecoil, 0, 1 / type.recoilTime);
            heat = Mathf.approachDelta(heat, 0, 1 / type.cooldown);
            charge = charging() ? Mathf.approachDelta(charge, 1, 1 / type.shoot.firstShotDelay) : 0;
            recoilOffset.trns(rotation, -Mathf.pow(curRecoil, type.recoilPow) * type.recoil);
            reTargetHeat += Time.delta;

            updateReload();
            if(hasAmmo()){
                if(Float.isNaN(reloadCounter)) reloadCounter = 0;

                if(reTargetHeat >= 20f){
                    reTargetHeat = 0;
                    findTarget();
                }

                if(validateTarget()){
                    boolean canShoot = true;

                    if(build.isControlled()){ //player behavior
                        targetPos.set(build.unit.aimX(), build.unit.aimY());
                        canShoot = build.unit.isShooting();
                    }else if(build.logicControlled()){ //logic behavior
                        canShoot = build.logicShooting;
                    }else{ //default AI behavior
                        targetPosition(target);

                        if(Float.isNaN(rotation)) rotation = 0;
                    }

                    if(!build.isControlled()){
                        build.unit.aimX(targetPos.x);
                        build.unit.aimY(targetPos.y);
                    }

                    float targetRot = build.angleTo(targetPos);
                    if(shouldTurn()){
                        turnToTarget(targetRot);
                    }

                    if(Angles.angleDist(rotation, targetRot) < type.shootCone && canShoot){
                        wasShooting = true;
                        updateShooting();
                    }
                }
            }

            if(block.coolant != null) {
                updateCooling();
            }
        }


        protected boolean validateTarget(){
            return !Units.invalidateTarget(target, canHeal() ? Team.derelict : build.team, x,y) || build.isControlled() || build.logicControlled();
        }

        protected boolean canHeal(){
            return type.targetHealing && hasAmmo() && peekAmmo().collidesTeam && peekAmmo().heals();
        }
        public void findTarget(){
            if(type.targetAir && !type.targetGround){
                target = Units.bestEnemy(build.team, x,y, type.range, e -> !e.dead() && !e.isGrounded() && type.unitFilter.get(e), type.unitSort);
            }else{
                target = Units.bestTarget(build.team, x, y, type.range, e -> !e.dead() && type.unitFilter.get(e) && (e.isGrounded() || type.targetAir) && (!e.isGrounded() || type.targetGround), b -> type.targetGround && type.buildingFilter.get(b), type.unitSort);

                if(target == null && canHeal()){
                    target = Units.findAllyTile(build.team, x, y, type.range, b -> b.damaged() && b != build);
                }
            }
        }

        public void turnToTarget(float targetRot){
            rotation = Angles.moveToward(rotation, targetRot, type.rotateSpeed * build.delta());
        }
        public boolean shouldTurn(){
            return type.moveWhileCharging || !charging();
        }
        public boolean cheating() {return build.team.rules().cheat;}

        public BulletType useAmmo(){
            if(cheating()) return peekAmmo();

            AmmoEntry entry = ammo.peek();
            entry.amount -= type.ammoPerShot;
            if(entry.amount <= 0) ammo.pop();
            totalAmmo -= type.ammoPerShot;
            totalAmmo = Math.max(totalAmmo, 0);
            return peekAmmo();
        }

        public BulletType peekAmmo(){
            return ammo.size == 0 ? null : ammo.peek().type();}
        public boolean hasAmmo(){
            //skip first entry if it has less than the required amount of ammo
            if(ammo.size >= 2 && ammo.peek().amount < type.ammoPerShot && ammo.get(ammo.size - 2).amount >= type.ammoPerShot){
                ammo.swap(ammo.size - 1, ammo.size - 2);
            }
            return ammo.size > 0 && ammo.peek().amount >= type.ammoPerShot;
        }

        public boolean charging(){
            return queuedBullets > 0 && type.shoot.firstShotDelay > 0;
        }
        public void updateReload() {
            float multiplier = hasAmmo() ? peekAmmo().reloadMultiplier : 1f;
            reloadCounter += build.delta() * multiplier * build.baseReloadSpeed();

            //cap reload for visual reasons
            reloadCounter = Math.min(reloadCounter, type.reload);
        }

        public void updateShooting(){
            if(reloadCounter >= type.reload && shootWarmup >= type.minWarmup){
                shoot(peekAmmo());
                reloadCounter %= type.reload;
            }
        }

        protected void shoot(BulletType bullet){
            float
                    bulletX = x + Angles.trnsx(rotation - 90, type.shootX, type.shootY),
                    bulletY = y + Angles.trnsy(rotation - 90, type.shootX, type.shootY);

            if(type.shoot.firstShotDelay > 0){
                type.chargeSound.at(bulletX, bulletY, Mathf.random(type.soundPitchMin, type.soundPitchMax));
                bullet.chargeEffect.at(bulletX, bulletY, rotation);
            }

            type.shoot.shoot(totalShots, (xOffset, yOffset, angle, delay, mover) -> {
                queuedBullets ++;
                if(delay > 0f){
                    Time.run(delay, () -> bullet(bullet, xOffset, yOffset, angle, mover));
                }else{
                    bullet(bullet, xOffset, yOffset, angle, mover);
                }
                totalShots ++;
            });

            if(type.consumeAmmoOnce){
                useAmmo();
            }

            if(!type.sequential) skillCounter++;
            for(int i = 0; i < type.skillDelays.size; i++) if(skillCounter % type.skillDelays.get(i) == 0) {
                skillCounter = 0;
                type.skillSeq.get(i).get(build, type).run();
            }
        }
        protected void handleBullet(@Nullable Bullet bullet, float offsetX, float offsetY, float angleOffset){

        }
        protected void bullet(BulletType bullet, float xOffset, float yOffset, float angleOffset, Mover mover){
            queuedBullets --;

            if(build.dead || (!type.consumeAmmoOnce && !hasAmmo())) return;

            float
                    xSpread = Mathf.range(type.xRand),
                    ySpread = Mathf.range(type.yRand),
                    bulletX = build.x + this.xOffset + Angles.trnsx(rotation - 90, type.shootX + xOffset + xSpread, type.shootY + yOffset + ySpread),
                    bulletY = build.y + this.yOffset + Angles.trnsy(rotation - 90, type.shootX + xOffset + xSpread, type.shootY + yOffset + ySpread),
                    shootAngle = rotation + angleOffset + Mathf.range(type.inaccuracy),
                    lifeScl = bullet.scaleLife ? Mathf.clamp(Mathf.dst(bulletX, bulletY, targetPos.x, targetPos.y) / bullet.range, type.minRange / bullet.range, type.range / bullet.range) : 1f;

            //Log.info("build: ("  + build.x + ". "+ build.y +"), offset: ("+ this.xOffset + ", "+ this.yOffset + "), bullet will be on "+bulletX + ", "+bulletY);
            handleBullet(bullet.create(build, build.team, bulletX, bulletY, shootAngle, -1f, (1f - type.velocityRnd) + Mathf.random(type.velocityRnd), lifeScl, null, mover, targetPos.x, targetPos.y), xOffset, yOffset, shootAngle - rotation);

            (type.shootEffect == null ? bullet.shootEffect : type.shootEffect).at(bulletX, bulletY, rotation + angleOffset, bullet.hitColor);
            (type.smokeEffect == null ? bullet.smokeEffect : type.smokeEffect).at(bulletX, bulletY, rotation + angleOffset, bullet.hitColor);
            type.shootSound.at(bulletX, bulletY, Mathf.random(type.soundPitchMin, type.soundPitchMax));

            type.ammoUseEffect.at(
                    build.x + this.xOffset - Angles.trnsx(rotation, type.ammoEjectBack),
                    build.y + this.yOffset - Angles.trnsy(rotation, type.ammoEjectBack),
                    rotation * Mathf.sign(xOffset)
            );

            if(type.shake > 0){
                Effect.shake(type.shake, type.shake, build);
            }

            curRecoil = 1f;
            heat = 1f;

            if(!type.consumeAmmoOnce){
                useAmmo();
            }
        }

        /*TODO make multi cooling*/
        public void updateCooling() {
            if(reloadCounter < type.reload && block.coolant.efficiency(build) > 0 && build.efficiency > 0){
                float capacity = block.coolant instanceof ConsumeLiquidFilter filter ? filter.getConsumed(build).heatCapacity : 1f;
                block.coolant.update(build);
                reloadCounter += block.coolant.amount * build.edelta() * capacity * block.coolantMultiplier;

                if(Mathf.chance(0.06 * block.coolant.amount)){
                    type.coolEffect.at(xOffset + Mathf.range(block.size * tilesize / 2f), yOffset + Mathf.range(block.size * tilesize / 2f));
                }
            }
        }

        public void write(Writes write){
            try{
                write.f(reloadCounter);
                write.f(rotation);
            } catch(Throwable e){
                Log.warn(String.valueOf(e));
            }
        }

        public void read(Reads read, byte revision){
            try{
                reloadCounter = read.f();
                rotation = read.f();
            } catch(Throwable e){
                Log.warn(String.valueOf(e));
            }
        }
    }
}