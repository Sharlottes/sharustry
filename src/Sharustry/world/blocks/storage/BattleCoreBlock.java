package Sharustry.world.blocks.storage;

import arc.*;
import arc.audio.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.annotations.Annotations.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.Units.*;
import mindustry.entities.bullet.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import mindustry.world.meta.values.BoosterListValue;

import static mindustry.Vars.*;

public class BattleCoreBlock extends CoreBlock {

    //after being logic-controlled and this amount of time passes, the turret will resume normal AI
    public final static float logicControlCooldown = 60 * 2;

    public final int timerTarget = timers++;
    public int targetInterval = 20;

    public Color heatColor = Pal.turretHeat;
    public Effect shootEffect = Fx.none;
    public Effect smokeEffect = Fx.none;
    public Effect ammoUseEffect = Fx.none;
    public Sound shootSound = Sounds.shoot;

    //general info
    public float range = 80f;
    public float rotateSpeed = 5;

    public boolean acceptCoolant = true;
    /** Effect displayed when coolant is used. */
    public Effect coolEffect = Fx.fuelburn;
    /** How much reload is lowered by for each unit of liquid of heat capacity. */
    public float coolantMultiplier = 5f;

    public float reloadTime = 10f;

    public int maxAmmo = 30;
    public int ammoPerShot = 1;
    public float ammoEjectBack = 1f;
    public float inaccuracy = 0f;
    public float velocityInaccuracy = 0f;
    public int shots = 1;
    public float spread = 4f;
    public float recoilAmount = 1f;
    public float restitution = 0.02f;
    public float cooldown = 0.02f;
    public float coolantUsage = 0.2f;
    public float shootCone = 8f;
    public float shootShake = 0f;
    public float shootLength = -1;
    public float xRand = 0f;
    /** Currently used for artillery only. */
    public float minRange = 0f;
    public float burstSpacing = 0;
    public boolean alternate = false;
    public boolean targetAir = true;
    public boolean targetGround = true;

    //charging
    public float chargeTime = -1f;
    public int chargeEffects = 5;
    public float chargeMaxDelay = 10f;
    public Effect chargeEffect = Fx.none;
    public Effect chargeBeginEffect = Fx.none;
    public Sound chargeSound = Sounds.none;

    public Sortf unitSort = Unit::dst2;

    protected Vec2 tr = new Vec2();
    protected Vec2 tr2 = new Vec2();

    public @Load(value = "@-base", fallback = "block-@size") TextureRegion baseRegion;
    public @Load("@-heat") TextureRegion heatRegion;

    public Cons<BattleCoreBlock.BattleCoreBuild> drawer = tile -> Draw.rect(region, tile.x + tr2.x, tile.y + tr2.y, tile.rotation - 90);
    public Cons<BattleCoreBlock.BattleCoreBuild> heatDrawer = tile -> {
        if(tile.heat <= 0.00001f) return;

        Draw.color(heatColor, tile.heat);
        Draw.blend(Blending.additive);
        Draw.rect(heatRegion, tile.x + tr2.x, tile.y + tr2.y, tile.rotation - 90);
        Draw.blend();
        Draw.color();
    };
    public float offsetx = 0;
    public float offsety = 0;
    public Seq<BattleCoreBlock> turrets = new Seq<>();


    public BattleCoreBlock(String name){
        super(name);
        priority = TargetPriority.core;
        update = true;
        solid = true;
        group = BlockGroup.none;
        flags = EnumSet.of(BlockFlag.core);
        outlineIcon = true;
        liquidCapacity = 20f;
    }

    @Override
    public boolean outputsItems(){
        return false;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        for(BattleCoreBlock turret : turrets) {
            Drawf.dashCircle((x+turret.offsetx) * tilesize + offset, (y+turret.offsety) * tilesize + offset, turret.range, Pal.placing);
        }
    }

    @Override
    public void init(){
        if(acceptCoolant && !consumes.has(ConsumeType.liquid)) {
            hasLiquids = true;
            consumes.add(new ConsumeLiquidFilter(liquid -> liquid.temperature <= 0.5f && liquid.flammability < 0.1f, coolantUsage)).update(false).boost();
        }

        if(shootLength < 0) shootLength = size * tilesize / 2f;

        super.init();
    }

    @Override
    public void setStats(){ //TODO.turrets' stat
        super.setStats();

        stats.add(Stat.shootRange, range / tilesize, StatUnit.blocks);
        if(acceptCoolant){
            stats.add(Stat.booster, new BoosterListValue(reloadTime, consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount, coolantMultiplier, true, l -> consumes.liquidfilters.get(l.id)));
        }
        stats.add(Stat.inaccuracy, (int)inaccuracy, StatUnit.degrees);
        stats.add(Stat.reload, 60f / (reloadTime + 1) * (alternate ? 1 : shots), StatUnit.none);
        stats.add(Stat.targetsAir, targetAir);
        stats.add(Stat.targetsGround, targetGround);
        if(ammoPerShot != 1) stats.add(Stat.ammoUse, ammoPerShot, StatUnit.perShot);
    }

    @Override
    public TextureRegion[] icons(){ //how to use icon
        return new TextureRegion[]{baseRegion, region};
    }



    public static abstract class AmmoEntry{
        public int amount;

        public abstract BulletType type();
    }


    public class BattleCoreBuild extends CoreBuild {
        public Seq<AmmoEntry> ammo = new Seq<>();
        public int totalAmmo;
        public float recoil, heat, logicControlTime = -1;
        public int shotCounter;
        public boolean logicShooting = false;
        public @Nullable Posc target;
        public Vec2 targetPos = new Vec2();
        public BlockUnitc unit = Nulls.blockUnit;
        public boolean wasShooting, charging;
        public float reload;
        public float rotation = 90;
        public Seq<? extends BattleCoreBuild> turretBuilds = new Seq<>();

        transient BattleCoreBlock block;

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4){
            if(type == LAccess.shoot && !unit.isPlayer()) {
                targetPos.set(World.unconv((float) p1), World.unconv((float) p2));
                logicControlTime = logicControlCooldown;
                logicShooting = !Mathf.zero(p3);
            }
            if(type == LAccess.enabled){
                enabled = !Mathf.zero((float)p1);
                enabledControlTime = timeToUncontrol;
            }
        }

        @Override
        public void control(LAccess type, Object p1, double p2, double p3, double p4){
            if(type == LAccess.shootp && !unit.isPlayer()) {
                logicControlTime = logicControlCooldown;
                logicShooting = !Mathf.zero(p2);

                if(p1 instanceof Posc) targetPosition((Posc) p1);
            }
            if(type == LAccess.configure && block.logicConfigurable && !net.client()){
                //change config only if it's new
                Object prev = senseObject(LAccess.config);
                if(prev != p1) configureAny(p1);
            }
        }

        @Override
        public double sense(LAccess sensor){
            if(sensor == LAccess.ammo) return totalAmmo;
            else if(sensor == LAccess.ammoCapacity) return maxAmmo;
            else if(sensor == LAccess.rotation) return rotation;
            else if(sensor == LAccess.shootX) return World.conv(targetPos.x);
            else if(sensor == LAccess.shootY) return World.conv(targetPos.y);
            else if(sensor == LAccess.shooting) return isShooting() ? 1 : 0;
            else return super.sense(sensor);
        }

        @Override
        public Unit unit(){
            return (Unit)unit;
        }

        public boolean logicControlled(){
            return logicControlTime > 0;
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount){
            if(acceptCoolant && liquids.currentAmount() <= 0.001f){
                Events.fire(Trigger.turretCool);
            }

            super.handleLiquid(source, liquid, amount);
        }

        @Override
        public void created(){
            for(BattleCoreBuild turretb : turretBuilds) {
                turretb.x = x + turretb.block.offsetx;
                turretb.y = y + turretb.block.offsety;

                turretb.unit = (BlockUnitc) UnitTypes.block.create(team);
                turretb.unit.tile(this);
            }
        }

        @Override
        public void drawSelect(){
            for(BattleCoreBuild turretb : turretBuilds) {
                Drawf.dashCircle(turretb.x, turretb.y, turretb.block.range, team.color);
            }
        }

        @Override
        public void draw(){ //later.later!
            for(BattleCoreBuild turretb : turretBuilds) {
                Draw.rect(turretb.block.baseRegion, turretb.x, turretb.y);
                Draw.color();

                Draw.z(Layer.turret);

                tr2.trns(turretb.rotation, -turretb.recoil);

                Drawf.shadow(turretb.block.region, turretb.x + tr2.x - (turretb.block.size / 2f), turretb.y + tr2.y - (turretb.block.size / 2f), turretb.rotation - 90);
                turretb.block.drawer.get(turretb);

                if (turretb.block.heatRegion != Core.atlas.find("error")) {
                    turretb.block.heatDrawer.get(turretb);
                }
            }
            super.draw();
        }

        @Override
        public void updateTile(){
            for(BattleCoreBuild turretb : turretBuilds) {
                if (!turretb.validateTarget()) turretb.target = null;

                turretb.wasShooting = false;

                turretb.recoil = Mathf.lerpDelta(turretb.recoil, 0f, turretb.block.restitution);
                turretb.heat = Mathf.lerpDelta(turretb.heat, 0f, turretb.block.cooldown);

                turretb.unit.health(turretb.health);
                turretb.unit.rotation(turretb.rotation);
                turretb.unit.team(team);
                turretb.unit.set(turretb.x, turretb.y);

                if (turretb.logicControlTime > 0) {
                    turretb.logicControlTime -= Time.delta;
                }

                if (turretb.hasAmmo()) {

                    if (timer(turretb.block.timerTarget, turretb.block.targetInterval)) {
                        turretb.findTarget();
                    }

                    if (turretb.validateTarget()) {
                        boolean canShoot = true;

                        if (turretb.isControlled()) { //player behavior
                            turretb.targetPos.set(turretb.unit.aimX(), turretb.unit.aimY());
                            canShoot = turretb.unit.isShooting();
                        } else if (turretb.logicControlled()) { //logic behavior
                            canShoot = turretb.logicShooting;
                        } else { //default AI behavior
                            turretb.targetPosition(turretb.target);

                            if (Float.isNaN(turretb.rotation)) {
                                turretb.rotation = 0;
                            }
                        }

                        float targetRot = angleTo(turretb.targetPos);

                        if (turretb.shouldTurn()) {
                            turretb.turnToTarget(targetRot);
                        }

                        if (Angles.angleDist(turretb.rotation, targetRot) < turretb.block.shootCone && canShoot) {
                            turretb.wasShooting = true;
                            turretb.updateShooting();
                        }
                    }
                }

                if (turretb.block.acceptCoolant) {
                    turretb.updateCooling();
                }
            }
        }

        public boolean isActive(){
            return target != null || wasShooting;
        }

        public void targetPosition(Posc pos){
            if(!hasAmmo() || pos == null) return;
            BulletType bullet = peekAmmo();
            float speed = bullet.speed;

            //slow bullets never intersect
            if(speed < 0.1f) speed = 9999999f;
            targetPos.set(Predict.intercept(this, pos, speed));
            if(targetPos.isZero()) targetPos.set(pos);
        }

        /** called from each weapon turrets */
        public boolean isShooting(){
            return (isControlled() ? unit.isShooting() : logicControlled() ? logicShooting : target != null);
        }

        protected boolean validateTarget(){
            return !Units.invalidateTarget(target, team, x, y) || isControlled() || logicControlled();
        }

        protected void findTarget(){
            if(targetAir && !targetGround){
                target = Units.bestEnemy(team, x, y, range, e -> !e.dead() && !e.isGrounded(), unitSort);
            }else{
                target = Units.bestTarget(team, x, y, range, e -> !e.dead() && (e.isGrounded() || targetAir) && (!e.isGrounded() || targetGround), b -> true, unitSort);
            }
        }

        protected void turnToTarget(float targetRot){
            rotation = Angles.moveToward(rotation, targetRot, rotateSpeed * delta() * baseReloadSpeed());
        }

        public boolean shouldTurn(){
            return !charging;
        }

        /** Consume ammo and return a type. */
        public BulletType useAmmo(){
            if(cheating()) return peekAmmo();

            AmmoEntry entry = ammo.peek();
            entry.amount -= ammoPerShot;
            if(entry.amount <= 0) ammo.pop();
            totalAmmo -= ammoPerShot;
            totalAmmo = Math.max(totalAmmo, 0);
            ejectEffects();
            return entry.type();
        }

        /** @return the ammo type that will be returned if useAmmo is called. */
        public BulletType peekAmmo(){
            return ammo.peek().type();
        }

        /** @return  whether the turret has ammo. */
        public boolean hasAmmo(){
            //skip first entry if it has less than the required amount of ammo
            if(ammo.size >= 2 && ammo.peek().amount < ammoPerShot){
                ammo.pop();
            }
            return ammo.size > 0 && ammo.peek().amount >= ammoPerShot;
        }

        protected void updateCooling(){
            float maxUsed = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount;

            Liquid liquid = liquids.current();

            float used = Math.min(Math.min(liquids.get(liquid), maxUsed * Time.delta), Math.max(0, ((reloadTime - reload) / coolantMultiplier) / liquid.heatCapacity)) * baseReloadSpeed();
            reload += used * liquid.heatCapacity * coolantMultiplier;
            liquids.remove(liquid, used);

            if(Mathf.chance(0.06 * used)){
                coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
            }
        }

        protected void updateShooting(){
            if(reload >= reloadTime){
                BulletType type = peekAmmo();

                shoot(type);

                reload = 0f;
            }else{
                reload += delta() * peekAmmo().reloadMultiplier * baseReloadSpeed();
            }
        }
        protected float baseReloadSpeed(){
            return efficiency();
        }
        protected void shoot(BulletType type){

            //when charging is enabled, use the charge shoot pattern
            if(chargeTime > 0){
                useAmmo();

                tr.trns(rotation, shootLength);
                chargeBeginEffect.at(x + tr.x, y + tr.y, rotation);
                chargeSound.at(x + tr.x, y + tr.y, 1);

                for(int i = 0; i < chargeEffects; i++){
                    Time.run(Mathf.random(chargeMaxDelay), () -> {
                        if(!isValid()) return;
                        tr.trns(rotation, shootLength);
                        chargeEffect.at(x + tr.x, y + tr.y, rotation);
                    });
                }

                charging = true;

                Time.run(chargeTime, () -> {
                    if(!isValid()) return;
                    tr.trns(rotation, shootLength);
                    recoil = recoilAmount;
                    heat = 1f;
                    bullet(type, rotation + Mathf.range(inaccuracy));
                    effects();
                    charging = false;
                });

                //when burst spacing is enabled, use the burst pattern
            }else if(burstSpacing > 0.0001f){
                for(int i = 0; i < shots; i++){
                    Time.run(burstSpacing * i, () -> {
                        if(!isValid() || !hasAmmo()) return;

                        recoil = recoilAmount;

                        tr.trns(rotation, shootLength, Mathf.range(xRand));
                        bullet(type, rotation + Mathf.range(inaccuracy));
                        effects();
                        useAmmo();
                        recoil = recoilAmount;
                        heat = 1f;
                    });
                }

            }else{
                //otherwise, use the normal shot pattern(s)

                if(alternate){
                    float i = (shotCounter % shots) - (shots-1)/2f;

                    tr.trns(rotation - 90, spread * i + Mathf.range(xRand), shootLength);
                    bullet(type, rotation + Mathf.range(inaccuracy));
                }else{
                    tr.trns(rotation, shootLength, Mathf.range(xRand));

                    for(int i = 0; i < shots; i++){
                        bullet(type, rotation + Mathf.range(inaccuracy + type.inaccuracy) + (i - (int)(shots / 2f)) * spread);
                    }
                }

                shotCounter++;

                recoil = recoilAmount;
                heat = 1f;
                effects();
                useAmmo();
            }
        }

        protected void bullet(BulletType type, float angle){ /** called from shoot() -> updateTile() */
            float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(x + tr.x, y + tr.y, targetPos.x, targetPos.y) / type.range(), minRange / type.range(), range / type.range()) : 1f;

            type.create(this, team, x + tr.x, y + tr.y, angle, 1f + Mathf.range(velocityInaccuracy), lifeScl);
        }

        protected void effects(){ /** called from shoot() -> updateTile() */
            Effect fshootEffect = shootEffect == Fx.none ? peekAmmo().shootEffect : shootEffect;
            Effect fsmokeEffect = smokeEffect == Fx.none ? peekAmmo().smokeEffect : smokeEffect;

            fshootEffect.at(x + tr.x, y + tr.y, rotation);
            fsmokeEffect.at(x + tr.x, y + tr.y, rotation);
            shootSound.at(x + tr.x, y + tr.y, Mathf.random(0.9f, 1.1f));

            if(shootShake > 0){
                Effect.shake(shootShake, shootShake, this);
            }

            recoil = recoilAmount;
        }

        protected void ejectEffects(){ /** called from useAmmo() */
            if(!isValid()) return;

            //alternate sides when using a double turret
            float scl = (block.shots == 2 && block.alternate && shotCounter % 2 == 1 ? -1f : 1f);

            ammoUseEffect.at(x - Angles.trnsx(rotation, ammoEjectBack), y - Angles.trnsy(rotation, ammoEjectBack), rotation * scl);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(reload);
            write.f(rotation);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 1){
                reload = read.f();
                rotation = read.f();
            }
        }

        @Override
        public byte version(){
            return 1;
        }
    }
}
