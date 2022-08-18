package Sharustry.world.blocks.defense.turret;

import Sharustry.world.blocks.defense.turret.mounts.*;
import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Vec2;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.*;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.Effect;
import mindustry.entities.Fires;
import mindustry.entities.Predict;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.ui.*;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.MassDriver;
import mindustry.world.consumers.ConsumeLiquidBase;
import mindustry.world.meta.BlockStatus;

import static mindustry.Vars.*;

public class MountTurret {
    public int skillCounter;
    public int totalAmmo;
    public int targetID;
    public float reloadCounter;
    public float recoil;
    public float shotCounter;
    public float rotation;
    public float lastX;
    public float lastY;
    public float strength;
    public float mineTimer;
    public float _heat;
    public float __heat;
    public float ___heat;
    public float charge;
    public boolean wasShooting;
    public boolean charging;
    public boolean any;

    public Posc target;
    public Unit tractTarget;
    public Unit repairTarget;
    public Bullet pointTarget;
    public Building healTarget;
    public Vec2 targetPos;
    public Tile mineTile;
    public Tile ore;
    public Item targetItem;
    public Seq<ItemEntry> ammos;
    public Seq<Tile> proxOres;
    public Seq<Item> proxItems;


    //set when build is created.
    public int i;
    public MultiTurret block;
    public MountTurretType type;

    public MassDriver.DriverState massState = MassDriver.DriverState.idle;
    public OrderedSet<Tile> waitingShooters = new OrderedSet<>();
    public int link = -1;
    public int linkIndex = i;

    public float x, y;

    public MountTurret(MountTurretType type, MultiTurret block, MultiTurret.MultiTurretBuild build, int i, float x, float y){
        this.type = type;
        this.block = block;
        this.i = i;
        this.x = x;
        this.y = y;
        shotCounter = 0f;
        skillCounter = 0;
        totalAmmo = 0;
        targetID = -1;
        reloadCounter = 0f;
        _heat = 0f;
        __heat = 0f;
        ___heat = 0f;
        recoil = 0f;
        rotation = 90f;
        mineTimer = 0f;
        lastX = 0f;
        lastY = 0f;
        strength = 0f;
        charge = 0f;
        wasShooting = false;
        charging = false;
        any = false;
        target = null;
        tractTarget = null;
        pointTarget = null;
        repairTarget = null;
        healTarget = null;
        mineTile = null;
        ore = null;
        targetItem = null;
        targetPos = new Vec2();
        ammos = new Seq<>();
        proxItems = new Seq<>();
        proxOres = new Seq<>();
    }

    public float getPowerEfficiency(MultiTurret.MultiTurretBuild build){
        return Mathf.clamp(build.power.graph.getPowerBalance()/type.powerUse, 0, 1);
    }

    public float[] mountLocations(MultiTurret.MultiTurretBuild build){
        Tmp.v1.trns(build.rotation - 90, x, y);
        Tmp.v1.add(build.x, build.y);
        Tmp.v2.trns(rotation, -(recoil+build.curRecoil));
        float i = (shotCounter % type.barrels) - (type.barrels - 1) / 2;
        Tmp.v3.trns(rotation - 90, type.shootX + type.barrelSpacing * i + type.xRand, type.shootY + type.yRand);

        float x = Tmp.v1.x;
        float y = Tmp.v1.y;
        float rX = x + Tmp.v2.x;
        float rY = y + Tmp.v2.y;
        float sX = rX + Tmp.v3.x;
        float sY = rY + Tmp.v3.y;

        return new float[]{x, y, rX, rY, sX, sY};
    }

    public BlockStatus status(MultiTurret.MultiTurretBuild build) {
        if(hasAmmo(build)) return BlockStatus.active;
        return BlockStatus.noInput;
    }

    public void control(MultiTurret.MultiTurretBuild build, LAccess type, double p1, double p2){
        if(type == LAccess.shoot && !build.unit.isPlayer())
            targetPos.set(World.unconv((float)p1), World.unconv((float)p2));
    }

    public void control(MultiTurret.MultiTurretBuild build, LAccess type, Object p1){
        if(type == LAccess.shootp && !build.unit.isPlayer() && p1 instanceof Posc){
            if(!hasAmmo(build)) return;
            BulletType bullet = peekAmmo(build);
            float speed = bullet.speed;
            //slow bullets never intersect
            if(speed < 0.1f) speed = 9999999f;

            targetPos.set(Predict.intercept(build, (Posc)p1, speed));
            if(targetPos.isZero()) targetPos.set((Posc)p1);
        }
    }

    public void removeFromProximity(){
        //reset when pushed
        targetItem = null;
        targetID = -1;
        mineTile = null;
    }

    public void handleItem(Item item){

    }

    public boolean acceptItem(MultiTurret.MultiTurretBuild build, Item item){
        return (build.hasMass() && build.items.total() < block.itemCapacity) || (type.mountAmmoType != null && type.mountAmmoType.get(item) != null && totalAmmo + type.mountAmmoType.get(item).ammoMultiplier <= type.maxAmmo)
                || (block.ammoTypes.get(item) != null && totalAmmo + block.ammoTypes.get(item).ammoMultiplier <= block.maxAmmo);
    }

    public boolean acceptLiquid(MultiTurret.MultiTurretBuild build, Liquid liquid){
        return type.liquidMountAmmoType != null && type.liquidMountAmmoType.get(liquid) != null && (build.liquids.current() == liquid || (type.liquidMountAmmoType.containsKey(liquid)
                && (!type.liquidMountAmmoType.containsKey(build.liquids.current()) || build.liquids.get(build.liquids.current()) <= 1f / type.liquidMountAmmoType.get(build.liquids.current()).ammoMultiplier + 0.001f)));
    }

    public int acceptStack(Item item, int amount){
        if(type.mountAmmoType != null && type.mountAmmoType.get(item) != null
                && totalAmmo + type.mountAmmoType.get(item).ammoMultiplier <= type.maxAmmo)
            return Math.min((int)((type.maxAmmo - totalAmmo) / type.mountAmmoType.get(item).ammoMultiplier), amount);
        return 0;
    }



    public void display(Table table, MultiTurret.MultiTurretBuild build){
        if(block.basicMounts.size > 3 && i % 4 == 0) table.row();
        else if(i % 4 == 0) table.row();
        table.add(new Stack(){{
            add(new Table(o -> {
                o.left();
                o.add(new Image(Core.atlas.find("shar-" + type.name + "-full"))).size(5*8f);
            }));

            add(new Table(h -> {
                if(type instanceof ItemMountTurretType) {
                    MultiReqImage itemReq = new MultiReqImage();

                    for(Item item : type.mountAmmoType.keys()) itemReq.add(new ReqImage(item.uiIcon, () -> hasAmmo(build)));

                    h.add(new Stack(){{
                        add(new Table(e -> {
                            e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*2f);
                            Bar itemBar = hasAmmo(build) ?
                                    new Bar(
                                            "",
                                            ammos.peek().item.color,
                                            () -> totalAmmo / type.maxAmmo) :
                                    new Bar(
                                            "",
                                            new Color(0.1f, 0.1f, 0.1f, 1),
                                            () -> 0);
                            e.add(itemBar);
                            e.pack();
                        }));

                        add(new Table(e -> {
                            e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*5f);
                            Bar reloadBar = new Bar(
                                    () -> "",
                                    () -> Pal.accent.cpy().lerp(Color.orange, reloadCounter / type.reload),
                                    () -> reloadCounter / type.reload);
                            e.add(reloadBar);
                            e.pack();
                        }));

                        if(type.chargeTime >= 0.001) add(new Table(e -> {
                            e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*8f);
                            Bar chargeBar = new Bar(
                                    () -> "",
                                    () -> Pal.surge.cpy().lerp(Pal.accent, reloadCounter / type.reload),
                                    () -> charge);
                            e.add(chargeBar);
                            e.pack();
                        }));
                        add(hasAmmo(build) ? new Table(e -> e.add(new ItemImage(ammos.peek().item.fullIcon, totalAmmo))) : new Table(e -> e.add(itemReq).size(18f)));
                    }}).padTop(2*8).padLeft(2*8);

                }
                else if(type instanceof LiquidMountTurretType) {
                    MultiReqImage liquidReq = new MultiReqImage();

                    for(Liquid liquid : type.liquidMountAmmoType.keys()) liquidReq.add(new ReqImage(liquid.uiIcon, () -> hasAmmo(build)));

                    h.add(new Stack(){{
                        add(new Table(e -> {
                            e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*2f);
                            Bar liquidBar = hasAmmo(build) && block.hasLiquids ?
                                new Bar("", build.liquids.current().color, () -> build.liquids.get(build.liquids.current()) / block.liquidCapacity)
                                : new Bar("", new Color(0.1f, 0.1f, 0.1f, 1), () -> 0);
                            e.add(liquidBar);
                            e.pack();
                        }));

                        add(new Table(e -> {
                            e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*5f);
                            Bar reloadBar = new Bar(
                                    () -> "",
                                    () -> Pal.accent.cpy().lerp(Color.orange, reloadCounter / type.reload),
                                    () -> reloadCounter / type.reload);
                            e.add(reloadBar);
                            e.pack();
                        }));

                        if(type.chargeTime >= 0.001) add(new Table(e -> {
                            e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*8f);
                            Bar chargeBar = new Bar(
                                    () -> "",
                                    () -> Pal.surge.cpy().lerp(Pal.accent, reloadCounter / type.reload),
                                    () -> charge);
                            e.add(chargeBar);
                            e.pack();
                        }));
                        add(hasAmmo(build) ? new Table(e -> e.add(new ItemImage(build.liquids.current().fullIcon, totalAmmo))) : new Table(e -> e.add(liquidReq).size(18f)));
                    }}).padTop(2*8).padLeft(2*8);

                } else {
                    MultiReqImage powerReq = new MultiReqImage();

                    powerReq.add(new ReqImage(Icon.powerSmall.getRegion(), () -> getPowerEfficiency(build) >= 0.001f));
                    h.add(new Stack(){{
                        add(new Table(e -> {
                            e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*2f);
                            Bar liquidBar = new Bar(
                                    "",
                                    Pal.powerBar,
                                    () -> getPowerEfficiency(build));
                            e.add(liquidBar);
                            e.pack();
                        }));
                        add(new Table(e -> {
                            e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*5f);
                            Bar reloadBar = new Bar(
                                    () -> "",
                                    () -> Pal.accent.cpy().lerp(Color.orange, reloadCounter / type.reload),
                                    () -> reloadCounter / type.reload);
                            e.add(reloadBar);
                            e.pack();
                        }));
                        if(type.chargeTime > 0) add(new Table(e -> {
                            e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*8f);
                            Bar chargeBar = new Bar(
                                    () -> "",
                                    () -> Pal.surge.cpy().lerp(Pal.accent, charge / type.chargeTime),
                                    () -> charge);
                            e.add(chargeBar);
                            e.pack();
                        }));
                        add(new Table(e -> e.add(powerReq)));
                    }}).padTop(2*8).padLeft(2*8);
                }

                h.pack();
            }));
        }}).left().size(7*8f);
    }

    public void draw(MultiTurret.MultiTurretBuild build) {
        float[] loc = mountLocations(build);

        Drawf.shadow(type.turrets[1], loc[2] - type.elevation, loc[3] - type.elevation, rotation - 90);
        Draw.rect(type.turrets[1], loc[2], loc[3], rotation - 90);
        Draw.rect(type.turrets[0], loc[2], loc[3], rotation - 90);

        if(type.turrets[2] != Core.atlas.find("error") && _heat > 0.00001){
            Draw.color(type.heatColor, _heat);
            Draw.blend(Blending.additive);
            Draw.rect(type.turrets[2], loc[2], loc[3], rotation - 90);
            Draw.blend();
            Draw.color();
        }
    }

    public void drawSelect(MultiTurret.MultiTurretBuild build) {
        float fade = Mathf.curve(Time.time % block.totalRangeTime, block.rangeTime * i, block.rangeTime * i + block.fadeTime) - Mathf.curve(Time.time % block.totalRangeTime, block.rangeTime * (i + 1) - block.fadeTime, block.rangeTime * (i + 1));
        float[] loc = mountLocations(build);
        Lines.stroke(3, Pal.gray);
        Draw.alpha(fade);

        if(mineTile != null){
            Lines.dashCircle(loc[0], loc[1], type.range);
            Lines.stroke(1, build.team.color);
            Draw.alpha(fade);
            Lines.dashCircle(loc[0], loc[1], type.range);

            Lines.stroke(1f, Pal.accent);
            Lines.poly(mineTile.worldx(), mineTile.worldy(), 4, tilesize / 2f * Mathf.sqrt2, Time.time);
            Draw.alpha(fade);
        }
        Lines.dashCircle(loc[0], loc[1], type instanceof RepairMountTurretType ? type.repairRadius : type.range);
        Lines.stroke(1, type instanceof RepairMountTurretType ? Pal.heal : build.team.color);
        Draw.alpha(fade);
        Lines.dashCircle(loc[0], loc[1], type instanceof RepairMountTurretType ? type.repairRadius : type.range);
        Draw.z(Layer.turret + 1);
        Draw.color(build.team.color, fade);
        Draw.rect(type.turrets[3], loc[2], loc[3], rotation - 90);
        Draw.reset();
    }

    public void drawConfigure(MultiTurret.MultiTurretBuild build) {
        if(type instanceof MassMountTurretType || (build.linkmount != this && build.linkmount != null)) return;

        float[] loc = mountLocations(build);
        float sin = Mathf.absin(Time.time, 6 / 2f, 1f);

        Draw.color(Pal.accent);
        Lines.stroke(1f);
        Drawf.circles(loc[0], loc[1], (build.tile.block().size  / 2f/ 2f + 1) * tilesize + sin - 2f, Pal.accent);

        for(Tile shooter : waitingShooters){
            Drawf.circles(shooter.drawx(), shooter.drawy(), (build.tile.block().size / 2f / 2f + 1) * tilesize + sin - 2f, Pal.place);
            Drawf.arrow(shooter.drawx(), shooter.drawy(), loc[0], loc[1], block.size / 2f * tilesize + sin, 4f + sin, Pal.place);
        }

        if(linkValid(build)){
            Building target = world.build(link);
            Drawf.circles(target.x, target.y, (target.block().size / 2f / 2f + 1) * tilesize + sin - 2f, Pal.place);
            Drawf.arrow(loc[0], loc[1], target.x, target.y, block.size / 2f * tilesize + sin, 4f + sin);
        }

        Drawf.dashCircle(loc[0], loc[1], type.range, Pal.accent);
    }



    public void update(MultiTurret.MultiTurretBuild build) {
        if(!Vars.headless && type.loopSound != null)
            type.loopSoundLoop.update(mountLocations(build)[4], mountLocations(build)[5], wasShooting && !build.dead());
    }

    public void updateAmmo(MultiTurret.MultiTurretBuild build) {
        if(block.hasItems) build.unit.ammo((float)build.unit.type().ammoCapacity * totalAmmo /  type.maxAmmo);
        if(block.hasLiquids) build.unit.ammo(build.unit.type().ammoCapacity * build.liquids.currentAmount() / block.liquidCapacity);
        if(block.hasPower) build.unit.ammo(build.power.status * build.unit.type().ammoCapacity);
    }
    
    public void updateTarget(MultiTurret.MultiTurretBuild build){
        if(!hasAmmo(build)) return;
        float[] loc = this.mountLocations(build);
        MountTurretType.rect.setSize(type.repairRadius * 2).setCenter(loc[0], loc[1]);

        tractTarget = Units.closestEnemy(build.team, loc[0], loc[1], type.range, u -> u.checkTarget(type.targetAir, type.targetGround));
        pointTarget = Groups.bullet.intersect(loc[0] - type.range, loc[1] - type.range, type.range * 2, type.range * 2).min(b -> b.team != build.team && b.type().hittable, b -> b.dst2(new Vec2(loc[0], loc[1])));
        repairTarget = Units.closest(build.team, loc[0], loc[1], type.repairRadius, Unit::damaged);
        healTarget = Units.findAllyTile(build.team, loc[0], loc[1], type.range, b -> b.damaged() && b != build);
        target = targetFind(build);
        if(type.healBlock && Units.findAllyTile(build.team, loc[0], loc[1], type.range, b -> build.damaged() && b.health <= b.block.health * block.healHealth) != null) target = healTarget;
    }

    public void updateTimer(MultiTurret.MultiTurretBuild build){
        wasShooting = false;
        recoil = Mathf.lerpDelta(recoil, 0, type.restitution);
        _heat = Mathf.lerpDelta(_heat, 0, type.cooldown);
        if(type.chargeTime >= 0.001 && charging && hasAmmo(build)) charge = Mathf.clamp(charge + Time.delta / type.chargeTime);
        else charge = 0;

        if(isTargetInvalid(build)) {
            target = null;
            tractTarget = null;
            pointTarget = null;
            healTarget = null;
        }

        __heat -= build.delta();
        ___heat -= build.delta();
    }
    public void updateTile(MultiTurret.MultiTurretBuild build) {
    }

    public void handlePayload(MultiTurret.MultiTurretBuild build,Bullet bullet, DriverBulletData data){
        if(!(type instanceof MassMountTurretType)) return;

        int totalItems = build.items.total();

        //add all the items possible
        for(int i = 0; i < data.items.length; i++){
            int maxAdd = Math.min(data.items[i], block.itemCapacity * 2 - totalItems);
            build.items.add(content.item(i), maxAdd);
            data.items[i] -= maxAdd;
            totalItems += maxAdd;

            if(totalItems >= block.itemCapacity * 2){
                break;
            }
        }

        Effect.shake(type.shake, type.shake, build);
        type.receiveEffect.at(bullet);

        reloadCounter = 0f;
        bullet.remove();
    }

    public Tile currentShooter(){
        return waitingShooters.isEmpty() ? null : waitingShooters.first();
    }

    public boolean shooterValid(MultiTurret.MultiTurretBuild build, Tile other, int mount){
        if(other == null) return true;
        return other.build instanceof MultiTurret.MultiTurretBuild
                && ((MultiTurret.MultiTurretBuild)other.build).mounts.get(linkIndex).link == build.tile.pos()
                && build.tile.dst(other) <= type.range;
    }

    public boolean linkValid(MultiTurret.MultiTurretBuild build){
        if(link == -1) return false;

        Building link = world.build(this.link);
        return (link instanceof MultiTurret.MultiTurretBuild
                && ((MultiTurret)link.block).basicMounts.size - 1 >= i
                && ((MultiTurret)link.block).basicMounts.get(linkIndex) instanceof MassMountTurretType)
                && link.team == build.team && build.within(link, type.range);
    }

    public boolean onConfigureTileTapped(MultiTurret.MultiTurretBuild build, Building other){
        if(build.linkmount != this) return true;

        if(build == other){
            build.configure(IntSeq.with(i, -1));
            return false;
        }
        if(link == other.pos()) {
            build.configure(IntSeq.with(i, -1));
            return false;
        }
        else if(other instanceof MultiTurret.MultiTurretBuild
                && ((MultiTurret.MultiTurretBuild)other).hasMass()
                && other.dst(build.tile) <= type.range && other.team == build.team){
            build.configure(IntSeq.with(i, other.pos()));
            return false;
        }
        return true;
    }

    public void targetTurn(MultiTurret.MultiTurretBuild build, float target){
        float speed = type.rotateSpeed * build.delta();
        if(type instanceof PowerMountTurretType || type.powerUse > 0.001f) speed *= getPowerEfficiency(build);
        else speed *= build.efficiency();
        rotation = Angles.moveToward(rotation, target, speed);
    }

    public Posc targetFind(MultiTurret.MultiTurretBuild build){
        float[] loc = this.mountLocations(build);

        if(type instanceof LiquidMountTurretType && type.extinguish && build.liquids.current().canExtinguish()) {
            int tr = (int) (type.range / tilesize);
            for(int x = -tr; x <= tr; x++) for(int y = -tr; y <= tr; y++) {
                Tile other = world.tileWorld(x + (int)loc[4]/8f, y + (int)loc[5]/8f);
                //do not extinguish fires on other team blocks
                if (other != null && Fires.has(x + (int)loc[4]/8, y + (int)loc[5]/8) && (other.build == null || other.team() == build.team)) return Fires.get(x + (int)loc[4]/8, y + (int)loc[5]/8);
            }
        }

        if(type.healBlock && Units.findAllyTile(build.team, loc[0], loc[1], type.range, Building::damaged) != null) return Units.findAllyTile(build.team, loc[0], loc[1], type.range, Building::damaged);
        else if(type.targetAir && !type.targetGround)
            return Units.bestEnemy(build.team, loc[0], loc[1], type.range, e -> !e.dead && !e.isGrounded(), type.unitSort);
        else return Units.bestTarget(build.team, loc[0], loc[1], type.range, e -> !e.dead && (e.isGrounded() || type.targetAir) && (!e.isGrounded() || type.targetGround), b -> true, type.unitSort);
    }

    public boolean isTargetInvalid(MultiTurret.MultiTurretBuild build){
        float[] loc = mountLocations(build);
        if(type.healBlock && Units.findAllyTile(build.team, loc[0], loc[1], type.range, Building::damaged) != null)
            return (!type.healBlock
                    || Units.findAllyTile(build.team, loc[0], loc[1], type.range, Building::damaged) == null)
                    || ((target == null || target instanceof Teamc && ((Teamc) target).team() != build.team
                    || target instanceof Healthc && !((Healthc) target).isValid())
                    && !build.isControlled() && !build.logicControlled());

        return Units.invalidateTarget(target, build.team, loc[0], loc[1]) && !build.isControlled() && !build.logicControlled();
    }

    public void targetPosition(MultiTurret.MultiTurretBuild build, Posc pos, float x, float y){
        if(!hasAmmo(build) || pos == null) return;

        BulletType bullet = peekAmmo(build);
        float speed = bullet.speed;

        if(speed < 0.1) speed = 9999999;

        targetPos.set(Predict.intercept(Tmp.v4.set(x, y), pos, speed));


        if(targetPos.isZero()) targetPos.set(target);
    }


    public void updateShooting(MultiTurret.MultiTurretBuild build){
        if(reloadCounter >= type.reload){
            shoot(build, type instanceof LiquidMountTurretType && block.hasLiquids ? type.liquidMountAmmoType.get(build.liquids.current()) : peekAmmo(build));
            reloadCounter = 0f;
        }else {
            float speed = build.delta() * peekAmmo(build).reloadMultiplier;
            if(type instanceof PowerMountTurretType || type.powerUse > 0.001f) speed *= getPowerEfficiency(build);
            else speed *= build.efficiency();
            if(speed >= 0.001f) reloadCounter += speed;
        }
    }

    public void shoot(MultiTurret.MultiTurretBuild build, BulletType bullet){
        for(int j = 0; j < type.shots; j++) {
            int spreadAmount = j;
            float[] loc = mountLocations(build);
            if (type.chargeTime >= 0.001f) {
                useAmmo(build);

                type.chargeBeginEffect.at(loc[4], loc[5], rotation);
                type.chargeSound.at(loc[4], loc[5], 1);

                for (int i = 0; i < type.chargeEffects; i++) {
                    Time.run(Mathf.random(type.chargeMaxDelay), () -> {
                        if(!build.isValid()) return;

                        type.chargeEffect.at(loc[4], loc[5], rotation);
                    });
                }

                charging = true;

                Time.run(type.chargeTime, () -> {
                    if (!build.isValid()) return;

                    recoil = type.recoilAmount;
                    _heat = 1f;
                    bullet(build, bullet, rotation + Mathf.range(type.inaccuracy));
                    effect(build, bullet);
                    charging = false;
                });
            }
            else {
                Time.run(type.burstSpacing * j, () -> {
                    if (!build.isValid() || !hasAmmo(build)) return;

                    if (type.loopSound != Sounds.none) type.loopSoundLoop.update(loc[4], loc[5], true);
                    if (type.sequential) shotCounter++;

                    bullet(build, bullet, spreadAmount);
                    effect(build, bullet);
                    useAmmo(build);
                    recoil = type.recoilAmount;
                    _heat = 1f;
                });
            }
        }

        if(!type.sequential) skillCounter++;
        for(int i = 0; i < type.skillDelays.size; i++) if(skillCounter % type.skillDelays.get(i) == 0) {
            skillCounter = 0;
            type.skillSeq.get(i).get(build, type).run();
        }
    }

    public void effect(MultiTurret.MultiTurretBuild build, BulletType bullet){
        float[] loc = mountLocations(build);

        (type.shootEffect == Fx.none ? bullet.shootEffect : type.shootEffect).at(loc[4], loc[5], rotation);
        (type.smokeEffect == Fx.none ? bullet.smokeEffect : type.smokeEffect).at(loc[4], loc[5], rotation);
        type.shootSound.at(loc[4], loc[5], Mathf.random(0.9f, 1.1f));
        if(type.shootShake > 0) Effect.shake(type.shootShake, type.shootShake, loc[4], loc[(int) build.y]);
        recoil = type.recoilAmount;
    }

    public void ejectEffects(MultiTurret.MultiTurretBuild build){
        if(!build.isValid()) return;

        int side = type.altEject ? Mathf.signs[(int) (shotCounter % 2)] : type.ejectRight ? 1 : 0;
        float[] loc = mountLocations(build);

        type.ejectEffect.at(loc[4], loc[5], rotation * side);
    }

    public void bullet(MultiTurret.MultiTurretBuild build, BulletType bullet, float spreadAmount){
        float[] loc = mountLocations(build);

        float lifeScl = bullet.scaleLife ? Mathf.clamp(Mathf.dst(loc[4], loc[5], targetPos.x, targetPos.y) / bullet.range, type.minRange / bullet.range, type.range / bullet.range) : 1;
        float angle = rotation + Mathf.range(type.inaccuracy + bullet.inaccuracy) + (spreadAmount - (type.shots / 2f)) * type.spread;
        bullet.create(build, build.team, loc[4], loc[5], angle, 1 + Mathf.range(type.velocityInaccuracy), lifeScl);
    }

    public BulletType peekAmmo(MultiTurret.MultiTurretBuild build){
        return null;
    }

    public BulletType useAmmo(MultiTurret.MultiTurretBuild build){
        return null;
    }

    public boolean hasAmmo(MultiTurret.MultiTurretBuild build){
        return true;
    }

    public boolean shouldTurn(){
        return !charging;
    }

    protected void turnToTargetBase(MultiTurret.MultiTurretBuild build, float target) {
        float speed = type.rotateSpeed * build.delta() * build.efficiency();
        float dist = Math.abs(Angles.angleDist(rotation, target));

        if(dist < speed) return;

        float angle = Mathf.mod(rotation, 360);
        float to = Mathf.mod(target, 360);
        float allRot = speed;

        if((angle > to && Angles.backwardDistance(angle, to) > Angles.forwardDistance(angle, to)) || (angle < to && Angles.backwardDistance(angle, to) < Angles.forwardDistance(angle, to))) allRot = -speed;

        rotation = (rotation + allRot) % 360;
    }

    /*TODO make multi cooling*/
    protected void updateCoolingBase(MultiTurret.MultiTurretBuild build) {
        float maxUsed = block.<ConsumeLiquidBase>findConsumer(consumer -> consumer instanceof ConsumeLiquidBase).amount / block.basicMounts.size;
        Liquid liquid = build.liquids.current();

        if(!(type.acceptCooling) || type.liquidMountAmmoType == null) return;
        float used = Math.min(Math.min(build.liquids.get(liquid), maxUsed * Time.delta), Math.max(0, ((type.reload - reloadCounter) / type.coolantMultiplier) / liquid.heatCapacity));
        if(type.powerUse > 0.001f) used *= getPowerEfficiency(build);
        else used *= build.efficiency();
        reloadCounter += used * liquid.heatCapacity * type.coolantMultiplier;

        build.liquids.remove(liquid, used);

        float[] loc = mountLocations(build);

        if(Mathf.chance(0.06 / block.basicMounts.size * used)) type.coolEffect.at(loc[0] + Mathf.range(type.width), loc[1] + Mathf.range(type.height));
    }

    public void targetMine(MultiTurret.MultiTurretBuild build, Building core){
        //target ore
        if(core == null) return;

        if(___heat <= 0.001 || targetItem == null){
            targetItem = iterateMap(build, core);
            ___heat = 16;
        }

        //if inventory is full, do not mine.
        if(targetItem == null || build.items.get(targetItem) >= block.itemCapacity){
            mineTile = null;
        }
        else{
            if(getPowerEfficiency(build) >= 0.2f && __heat <= 0.001 && targetItem != null && targetID > -1){
                ore = proxOres.get(targetID);
                __heat = 16;
            }

            if(ore != null && getPowerEfficiency(build) >= 0.2f){
                float dest = build.angleTo(ore);
                targetTurn(build, dest);

                if(Angles.within(rotation, dest, type.shootCone)){
                    mineTile = ore;
                }
                if(ore.block() != Blocks.air){
                    if(targetID > -1) mountReFind(build, targetID);
                    targetItem = null;
                    targetID = -1;
                    mineTile = null;
                }
            }
        }
    }

    public @Nullable Item iterateMap(MultiTurret.MultiTurretBuild build, Building core){
        if(proxOres == null || !proxOres.any()) return null;
        Item last = null;
        targetID = -1;
        for(int h = 0; h < proxOres.size; h++){
            if(mountCanMine(build, proxItems.get(h)) && (last == null || last.lowPriority || core.items.get(last) > core.items.get(proxItems.get(h)))){
                if(proxOres.get(h).block() != Blocks.air){
                    //try to relocate its ore
                    mountReFind(build, h);
                    //if it fails, ignore the ore
                    if(proxOres.get(h).block() != Blocks.air) continue;
                }
                last = proxItems.get(h);
                targetID = h;
            }
        }

        return last;
    }

    public void mountReMap(MultiTurret.MultiTurretBuild build){
        proxOres = new Seq<>();
        proxItems = new Seq<>();
        ObjectSet<Item> tempItems = new ObjectSet<>();

        float[] loc = mountLocations(build);
        Geometry.circle((int) loc[0] / 8, (int)loc[1] / 8, (int)(type.range / tilesize + 0.5f), (x, y) -> {
            Tile other = world.tile(x, y);
            if(other != null && other.drop() != null){
                Item drop = other.drop();
                if(!tempItems.contains(drop)){
                    tempItems.add(drop);
                    proxItems.add(drop);
                    proxOres.add(other);
                }
            }
        });
    }

    public void mountReFind(MultiTurret.MultiTurretBuild build, int h){
        Item item = proxItems.get(h);

        float[] loc = mountLocations(build);
        Geometry.circle((int) loc[0] / 8, (int)loc[1] / 8, (int)(type.range / tilesize + 0.5f), (x, y) -> {
            Tile other = world.tile(x, y);
            if(other != null && other.drop() != null && other.drop() == item && other.block() == Blocks.air){
                proxOres.set(h, other);
            }
        });
    }

    public boolean mountCanMine(MultiTurret.MultiTurretBuild build, Item item){
        return item.hardness >= type.minDrillTier && item.hardness <= type.maxDrillTier && build.items.get(item) < block.itemCapacity;
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
