package Sharustry.world.blocks.defense;

import Sharustry.ui.SItemImage;
import Sharustry.world.blocks.storage.BattleCore;
import arc.*;
import arc.func.Cons;
import arc.func.Func;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.Point2;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.Vars;
import mindustry.content.*;
import mindustry.core.UI;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.bullet.*;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.LAccess;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.Tile;
import mindustry.world.meta.*;

import java.util.Objects;

import static arc.struct.ObjectMap.*;
import static mindustry.Vars.*;

public class MultiTurret extends TemplatedTurret {
    public float healHealth = 0.4f;
    public boolean customMountLocation = false;
    public Seq<Float> customMountLocationsX = new Seq<>();
    public Seq<Float> customMountLocationsY = new Seq<>();
    public float rangeTime = 80;
    public float fadeTime = 20;
    public String title;
    public int amount;
    public float totalRangeTime;
    public Object mainAmmo;
    public BulletType bullet;

    public TextureRegion outline, baseTurret;

    public Seq<Integer> skillDelays = new Seq<>();
    public Seq<Func<MultiTurretBuild, Runnable>> skillSeq = new Seq<>();
    public Seq<String> skillNames = new Seq<>();
    public Seq<String> skillDescriptions = new Seq<>();
    public Seq<Cons<Table>> skillStats = new Seq<>();
    public Seq<MountTurretType> basicMounts = new Seq<>();

    public MultiTurret(String name, BulletType type, Object ammo, String title, MountTurretType... mounts){
        this(name);
        addMountTurret(mounts);
        addBaseTurret(type, ammo, title);
    }

    public MultiTurret(String name){
        super(name);

        config(Point2.class, (Building tile, Point2 point) -> {
            if(tile instanceof BattleCore.BattleCoreBuild) ((BattleCore.BattleCoreBuild)tile).link = Point2.pack(point.x + tile.tileX(), point.y + tile.tileY());
        });

        config(Integer.class, (Building tile, Integer point) -> {
            if(tile instanceof BattleCore.BattleCoreBuild) ((BattleCore.BattleCoreBuild)tile).link = point;
        });

        config(IntSeq.class, (Building tile, IntSeq index) -> {
            if(index.size != 2 || !(tile instanceof MultiTurretBuild)) return;
            ((MultiTurretBuild)tile).mounts.get(index.get(0)).link = index.get(1);
            ((MultiTurretBuild)tile).mounts.get(index.get(0)).linkIndex = index.get(0);
        });

        config(MountTurret.class, (Building tile, MountTurret point) -> {
            if(((MultiTurretBuild)tile).linkmount == point) ((MultiTurretBuild)tile).linkmount = null;
            else ((MultiTurretBuild)tile).linkmount = point;
        });
    }

    public void addMountTurret(MountTurretType... mounts){
            for(MountTurretType mount : mounts) basicMounts.add(mount);

            this.amount = basicMounts.size;
            this.totalRangeTime = rangeTime * basicMounts.size;
    }

    public void addBaseTurret(BulletType type, Object ammo, String title){
        if(ammo instanceof Item) {
            hasItems = true;
            mainAmmo = ammo;
            ammoType = "item";
            ammo(ammo, type);
        }
        if(ammo instanceof Liquid) {
            hasLiquids = true;
            mainAmmo = ammo;
            ammoType = "liquid";
            ammo(ammo, type);
        }
        if(ammo instanceof Float) {
            hasPower = true;
            mainAmmo = ammo;
            ammoType = "power";
            powerUse = (Float)ammo;
        }

        this.bullet = type;
        this.title = title;
    }

    public void addCustomMountLocation(Float[] xy){
        customMountLocation = true;
        for(int ix = 0; ix < amount * 2; ix += 2) customMountLocationsX.add(xy[ix]);
        for(int iy = 1; iy < amount * 2; iy += 2) customMountLocationsY.add(xy[iy]);
    }

    public <T extends MultiTurretBuild> void addSkills(Func<T, Runnable> skill, int delay, String name){
        if(skill != null) {
            skillSeq.add((Func<MultiTurretBuild, Runnable>) skill);
            skillDelays.add(delay);
            skillNames.add(name);
        }
    }

    @Override
    public void load(){
        super.load();

        teamRegion = Core.atlas.find("error");
        baseRegion = Core.atlas.find(name + "-base", "block-" + this.size);
        region = Core.atlas.find(name + "-baseTurret");
        heatRegion = Core.atlas.find(name + "-heat");
        outline = Core.atlas.find(name + "-outline");
        baseTurret = Core.atlas.find(name + "-baseTurret");
        Events.on(EventType.ClientLoadEvent.class, e -> {
            for(MountTurretType mount : basicMounts) mount.load();
        });
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        for(int i = 0; i < basicMounts.size; i++) basicMounts.get(i).drawPlace(this, i, x, y, rotation, valid);
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{
            this.baseRegion,
            Core.atlas.find(name + "-icon")
        };
    }

    void rowAdd(Table h, String str){
        h.row();
        h.add(str);
    }

    void addStatS(Table w){
        w.left();
        w.row();
        w.add(title).right().top();
        w.row();
        w.image(baseTurret).size(60).scaling(Scaling.bounded).right().top();

        w.table(Tex.underline, h -> {
            h.left().defaults().padRight(3).left();

            rowAdd(h, "[lightgray]" + Stat.shootRange.localized() + ": [white]" + Strings.fixed((range) / tilesize, 1) + " " + StatUnit.blocks);
            rowAdd(h, "[lightgray]" + Stat.reload.localized() + ": [white]" + Strings.autoFixed(60 / reloadTime * shots, 1));
            rowAdd(h, "[lightgray]" + Stat.targetsAir.localized() + ": [white]" + (!(targetAir) ? Core.bundle.get("no") : Core.bundle.get("yes")));
            rowAdd(h, "[lightgray]" + Stat.targetsGround.localized() + ": [white]" + (!(targetGround) ? Core.bundle.get("no") : Core.bundle.get("yes")));
            if(inaccuracy > 0) rowAdd(h, "[lightgray]" + Stat.inaccuracy.localized() + ": [white]" + (inaccuracy) + " " + StatUnit.degrees.localized());
            if(chargeTime > 0.001f) rowAdd(h, "[lightgray]" + Core.bundle.get("stat.shar.chargeTime") + ": [white]" + Mathf.round(chargeTime/60, 100) + " " + Core.bundle.format("stat.shar.seconds"));
            if(Objects.equals(ammoType, "item")) rowAdd(h, "[lightgray]" + Core.bundle.get("stat.shar.ammo-shot") + ": [white]" + (ammoPerShot));

            h.row();

            ObjectMap<ObjectMap<BulletType ,? extends UnlockableContent>, TextureRegion> types = new ObjectMap<>();
            TextureRegion icon = Core.atlas.find("error");
            if(mainAmmo instanceof Item) icon = ((Item)mainAmmo).icon(Cicon.medium);
            if(mainAmmo instanceof Liquid) icon = ((Liquid)mainAmmo).icon(Cicon.medium);
            if(mainAmmo instanceof Float) icon = Icon.power.getRegion();
            types.put(of(bullet, mainAmmo), icon);

            h.table(b -> {
                for(ObjectMap<BulletType, ? extends UnlockableContent> type : types.keys()){
                    int ii = 0;

                    for(BulletType bullet : type.keys()){
                        ii ++;

                        b.image(types.get(type)).size(8 * 4).padRight(4).right().top();
                        b.add(type.get(bullet).localizedName).padRight(10).left().top();

                        b.table(Tex.underline, e -> {
                            e.left().defaults().padRight(3).left();

                            if(bullet.damage > 0 && (bullet.collides || bullet.splashDamage <= 0)) rowAdd(e, Core.bundle.format("bullet.damage", bullet.damage));
                            if(bullet.buildingDamageMultiplier != 1) rowAdd(e, Core.bundle.format("bullet.buildingdamage", Strings.fixed((int)(bullet.buildingDamageMultiplier * 100),1)));
                            if(bullet.splashDamage > 0) rowAdd(e, Core.bundle.format("bullet.splashdamage", bullet.splashDamage, Strings.fixed(bullet.splashDamageRadius / tilesize, 1)));
                            if(bullet.ammoMultiplier > 0 && !(bullet instanceof LiquidBulletType) && !Mathf.equal(bullet.ammoMultiplier, 1f)) rowAdd(e, Core.bundle.format("bullet.multiplier", Strings.fixed(bullet.ammoMultiplier, 1)));
                            if(!Mathf.equal(bullet.reloadMultiplier, 1f)) rowAdd(e, Core.bundle.format("bullet.reload", bullet.reloadMultiplier));
                            if(bullet.knockback > 0) rowAdd(e, Core.bundle.format("bullet.knockback", Strings.fixed(bullet.knockback, 1)));
                            if(bullet.healPercent > 0) rowAdd(e, Core.bundle.format("bullet.healpercent", bullet.healPercent));
                            if(bullet.pierce || bullet.pierceCap != -1) rowAdd(e, bullet.pierceCap == -1 ? "@bullet.infinitepierce" : Core.bundle.format("bullet.pierce", bullet.pierceCap));
                            if(bullet.status == StatusEffects.burning || bullet.status == StatusEffects.melting || bullet.incendAmount > 0) rowAdd(e, "@bullet.incendiary");
                            if(bullet.status == StatusEffects.freezing) rowAdd(e, "@bullet.freezing");
                            if(bullet.status == StatusEffects.tarred) rowAdd(e, "@bullet.tarred");
                            if(bullet.status == StatusEffects.sapped) rowAdd(e, "@bullet.sapping");
                            if(bullet.homingPower > 0.01) rowAdd(e, "@bullet.homing");
                            if(bullet.lightning > 0) rowAdd(e, "@bullet.shock");
                            if(bullet.fragBullet != null) rowAdd(e, "@bullet.frag");
                        }).padTop(-9).left();

                        if(ii % 4 == 0) b.row();
                    }
                }
            });
            h.row();
        }).padTop(-15).left();
        w.row();
    }

    @Override
    public void setStats(){
        super.setStats();

        for(int i = 0; i < skillSeq.size; i++) {
            final int j = i;
            stats.add(Stat.abilities, table -> {
                if(skillDescriptions.size >= skillSeq.size) table.table(Tex.underline, e -> {
                    e.left().defaults().padRight(3).left();
                    e.add("[white]" + skillNames.get(j) + "[]").fillX();
                    e.row();
                    e.add(Core.bundle.format("stat.shar.skillreload", ""+skillDelays.get(j)));
                    e.row();
                    if(skillStats.size >= skillSeq.size) {
                        skillStats.get(j).get(e);
                        e.row();
                    }
                    e.add("[lightgray]"+ Core.bundle.get("category.purpose") + ": " + skillDescriptions.get(j)+"");
                }).left();
            });
        }

        try {
            stats.remove(Stat.shootRange);
            stats.remove(Stat.inaccuracy);
            stats.remove(Stat.reload);
            if(Objects.equals(ammoType, "item") || Objects.equals(ammoType, "liquid")) stats.remove(Stat.ammo);
            stats.remove(Stat.targetsAir);
            stats.remove(Stat.targetsGround);
            stats.remove(Stat.ammoUse);
            stats.remove(Stat.booster);
        } catch (Throwable a){
            Log.log(Log.LogLevel.warn,"@", a);
        }

        stats.add(Stat.weapons, table -> {
            table.add();
            table.row();
            table.left();
            table.add("[lightgray]" + Core.bundle.get("stat.shar.base-t")).fillX().padLeft(24);
            table.row();

            //Base Turret
            table.table(null, w -> {
                addStatS(w);
                table.row().left();
            }).left();

            table.row();
            table.left();
            table.add("[lightgray]" + Core.bundle.get("stat.shar.mini-t")).fillX().padLeft(24);

            //Mounts
            table.table(null, w -> {
                for(MountTurretType mount : basicMounts){
                    mount.addStatS(w);
                    table.row();
                }
            });
        });
    }

    public class MultiTurretBuild extends TemplatedTurretBuild {
        public Seq<Integer> shotcounters = new Seq<>();
        public Seq<MountTurret> mounts = new Seq<>();
        public MountTurret linkmount = null;
        public float _heat;

        @Override
        public void remove(){
            super.remove();
            if(sound != null) sound.stop();
        }

        @Override
        public Building init(Tile tile, Team team, boolean shouldAdd, int rotation) {
            return super.init(tile, team, shouldAdd, rotation);
        }

        @Override
        public void created(){
            super.created();
            for(int i = 0; i < skillDelays.size; i++) shotcounters.add(0);
            for(int i = 0; i < basicMounts.size; i++) mounts.add(new MountTurret(basicMounts.get(i), ((MultiTurret)block), this, i));
        }

        public boolean hasMass(){
            return mounts.find(m -> m.type.mountType == MountTurretType.MultiTurretMountType.mass) != null;
        }
        @Override
        public void displayConsumption(Table table){
            if(hasMass()) table.table(c -> {
                int q = 0;
                for(int i = 0; i < Vars.content.items().size; i++) {
                    q++;
                    final int hh = q;
                    final int h1 = i;
                    Item item = Vars.content.items().get(h1);

                    c.add(new Stack(){{
                        add(new Table(o -> {
                            o.left();
                            o.add(new Image(item.icon(Cicon.medium))).size(32f);
                        }));

                        add(new Table(h -> {
                            h.right().top();
                            h.add(new Label(() -> {
                                int amount = !items.has(Vars.content.items().get(h1)) ? 0 : items.get(Vars.content.items().get(h1));
                                return amount > 1000 ? UI.formatAmount(amount) : amount + "";
                            })).fontScale(0.8f).color(item.color);
                            h.pack();
                        }));
                    }}).left().padRight(8);
                    if(hh % 7 == 0) c.row();
                }
            }).center();
            table.row();
            table.table(t -> {
                for(MountTurret mount : mounts) {
                    t.center();
                    mount.display(t, this);
                }
            });
        }

        @Override
        public void displayBars(Table bars){
            for(Func<Building, Bar> bar : block.bars.list()){
                try{
                    bars.add(bar.get(self())).growX();
                    bars.row();
                }catch(ClassCastException e){
                    break;
                }
            }
            bars.add(new Bar("stat.ammo", Pal.ammo, () -> Mathf.clamp((float)totalAmmo / maxAmmo, 0f, 1f))).growX();
            bars.row();

            bars.add(new Bar(
                    () -> {
                        float value = Mathf.clamp(reload / reloadTime) * 100f;
                        return Core.bundle.format("bar.shar-reload", Strings.fixed(value, (Math.abs((int)value - value) <= 0.001f ? 0 : Math.abs((int)(value * 10) - value * 10) <= 0.001f ? 1 : 2)));
                    },
                    () -> Pal.accent.cpy().lerp(Color.orange, reload / reloadTime),
                    () -> reload / reloadTime)).growX();
            bars.row();

            if(chargeTime >= 0.001) bars.add(new Bar(
                    () -> {
                        float value = Mathf.clamp(charge) * 100f;
                        return Core.bundle.format("bar.shar-charge", Strings.fixed(value, (Math.abs((int)value - value) <= 0.001f ? 0 : Math.abs((int)(value * 10) - value * 10) <= 0.001f ? 1 : 2)));
                    },
                    () -> Pal.surge.cpy().lerp(Pal.accent, charge / chargeTime),
                    () -> charge)).growX();
            bars.row();

            for(int i = 0; i < skillDelays.size; i++) {
                final int j = i;
                bars.add(new Bar(
                        () -> Core.bundle.format("bar.shar-skillReload") + shotcounters.get(j) + " / " + skillDelays.get(j),
                        () -> Pal.lancerLaser.cpy().lerp(Pal.place, Mathf.absin(Time.time, 20, (shotcounters.get(j) / (skillDelays.get(j) * 2.5f)))),
                        () -> (shotcounters.get(j) / (skillDelays.get(j) * 1f)))).growX();
                bars.row();
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            for(MountTurret mount : mounts) mount.drawSelect(this);
        }

        @Override
        public void drawConfigure(){
            for(MountTurret mount : mounts) mount.drawConfigure(this);
        }

        @Override
        public void buildConfiguration(Table table) {
            super.buildConfiguration(table);
            if(!hasMass()) return;
            MountSelection.buildTable(table, mounts.copy().filter(m -> m.type.mountType == MountTurretType.MultiTurretMountType.mass), () -> linkmount, this::configure, this, false);
        }
        @Override
        public int removeStack(Item item, int amount){
            return 0;
        }

        @Override
        public void handleStack(Item item, int amount, Teamc source){
            for(int i = 0; i < amount; i++)
                handleItem(null, item);
        }

        @Override
        public void handleItem(Building source, Item item){
            for(MountTurret mount : mounts) mount.handleItem(item);

            if(ammoTypes.get(item) != null && totalAmmo + ammoTypes.get(item).ammoMultiplier <= maxAmmo) {
                if(Objects.equals(ammoType, "item")){
                    if(item == Items.pyratite) Events.fire(EventType.Trigger.flameAmmo);

                    BulletType type = ammoTypes.get(item);
                    totalAmmo += type.ammoMultiplier;

                    //find ammo entry by type
                    for(int i = 0; i < ammo.size; i++){
                        TemplatedTurret.ItemEntry entry = (TemplatedTurret.ItemEntry)ammo.get(i);

                        //if found, put it to the right
                        if(entry.item == item){
                            entry.amount += type.ammoMultiplier;
                            ammo.swap(i, ammo.size - 1);
                            return;
                        }
                    }

                    //must not be found
                    ammo.add(new TemplatedTurret.ItemEntry(item, (int)type.ammoMultiplier));
                } else if(!hasMass()) items.add(item, 1);
            }else if(hasMass() && items.total() < itemCapacity) items.add(item, 1);
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            boolean h = false;
            for(MountTurret mount : mounts) {
                h = mount.acceptItem(this, item);
                if(h) break;
            }
            return ((ammoTypes.get(item) != null && totalAmmo + ammoTypes.get(item).ammoMultiplier <= maxAmmo) && h) || (hasMass() && items.total() < itemCapacity);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            boolean h = false;
            for(MountTurret mount : mounts) {
                h = mount.acceptLiquid(this, liquid);
                if(h) break;
            }
            return h;
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            int past = 0;

            for(MountTurret mount : mounts) past = Math.min(past, mount.acceptStack(item, amount));
            return Math.min(past, amount);
        }

        @Override
        public BlockStatus status() {
            BlockStatus status = BlockStatus.noInput;
            for(MountTurret mount : mounts) status = mount.status(this);
            return status;
        }

        @Override
        public void draw() {
            Draw.rect(baseRegion, x, y);

            Draw.z(Layer.turret);
            Tmp.v4.trns(rotation, -recoil);
            Tmp.v4.add(x, y);

            Drawf.shadow(outline, Tmp.v4.x - (size / 2f), Tmp.v4.y - (size / 2f), rotation - 90);
            Draw.rect(outline, Tmp.v4.x, Tmp.v4.y, rotation - 90);
            Draw.rect(region, Tmp.v4.x, Tmp.v4.y, rotation - 90);

            if(heatRegion != Core.atlas.find("error") && _heat > 0.00001){
                Draw.color(heatColor, _heat);
                Draw.blend(Blending.additive);
                Draw.rect(heatRegion, Tmp.v4.x, Tmp.v4.y, rotation - 90);
                Draw.blend();
                Draw.color();
            }

            for(MountTurret mount : mounts) mount.draw(this);

            Draw.reset();
        }

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4){
            if(type == LAccess.shoot && !unit.isPlayer()){
                for(MountTurret mount : mounts) mount.control(this, type, p1, p2);
                logicControlTime = logicControlCooldown;
                logicShooting = !Mathf.zero(p3);
            }

            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public void control(LAccess type, Object p1, double p2, double p3, double p4){
            if(type == LAccess.shootp && !unit.isPlayer()){
                logicControlTime = logicControlCooldown;
                logicShooting = !Mathf.zero(p2);

                if(p1 instanceof Posc) {
                    for(MountTurret mount : mounts) mount.control(this, type, p1);
                }
            }

            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public void update() {
            super.update();
            for(MountTurret mount : mounts) mount.update(this);
        }

        public float __heat;
        @Override
        public void updateTile() {
            //set block's ammo
            unit.ammo((float)unit.type().ammoCapacity * totalAmmo / maxAmmo);
            for(MountTurret mount : mounts) mount.updateAmmo(this);

            super.updateTile();

            for(MountTurret mount : mounts) mount.updateTimer(this);

            //get targets
            __heat -= delta();
            if(__heat <= 0.001){
                for(MountTurret mount : mounts) mount.updateTarget(this);
                __heat = 16;
            }

            for(MountTurret mount : mounts) mount.updateTile(this);
        }

        public void handlePayload(Bullet bullet, DriverBulletData data){
            if(!hasMass()) return;

            for(MountTurret mount : mounts) mount.handlePayload(this, bullet, data);
        }

        @Override
        public void removeFromProximity() {
            for(MountTurret mount : mounts) mount.removeFromProximity();
            super.removeFromProximity();
        }

        @Override
        public Object config(){
            for(int i = 0; i < mounts.size; i++){
                if(mounts.get(i).type.mountType != MountTurretType.MultiTurretMountType.mass) continue;
                return ObjectMap.of(i, mounts.get(i).link);
            }
            return null;
        }

        @Override
        public boolean onConfigureTileTapped(Building other){
            for(MountTurret mount : mounts){
                boolean h = mount.onConfigureTileTapped(this, other);
                if(!h) return false;
            }

            return true;
        }

        @Override
        public boolean shouldTurn(){
            boolean h = !charging;
            for(MountTurret mount : mounts) {
                h = mount.shouldTurn();
                if(!h) break;
            }
            return h;
        }

        @Override
        protected void turnToTarget(float target) {
            super.turnToTarget(target);

            for(MountTurret mount : mounts) mount.turnToTargetBase(this, target);
        }

        @Override
        protected void updateCooling() {
            super.updateCooling();

            for(MountTurret mount : mounts) mount.updateCoolingBase(this);
        }

        @Override
        protected void shoot(BulletType type) {
            super.shoot(type);

            doSkill();
        }


        public void doSkill(){
            for(int i = 0; i < skillDelays.size; i++) {
                shotcounters.set(i, shotcounters.get(i) + 1);
                if(Objects.equals(shotcounters.get(i), skillDelays.get(i))) {
                    shotcounters.set(i, 0);
                    skillSeq.get(i).get(this).run();
                }
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);

            for(int i = 0; i < skillDelays.size; i++)
                write.i(shotcounters.get(i));
            for(MountTurret mount : mounts) mount.write(write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            for(int i = 0; i < skillDelays.size; i++)
                shotcounters.set(i, read.i());

            for(MountTurret mount : mounts) mount.read(read, revision);
        }
    }
}