package Sharustry.world.blocks.storage;

import Sharustry.content.SBullets;
import Sharustry.graphics.Drawm;
import Sharustry.world.blocks.defense.turret.DriverBulletData;
import Sharustry.world.blocks.defense.turret.MountTurret;
import Sharustry.world.blocks.defense.turret.MultiTurret;
import Sharustry.world.blocks.defense.turret.MountTurretType;
import arc.*;
import arc.func.Func;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import arc.util.pooling.Pools;
import mindustry.*;
import mindustry.audio.*;
import mindustry.content.*;
import mindustry.core.UI;
import mindustry.core.World;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.InputHandler;
import mindustry.logic.LAccess;
import mindustry.logic.Ranged;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.ControlBlock;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.blocks.distribution.MassDriver;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

import static arc.Core.scene;
import static mindustry.Vars.*;

import static arc.struct.ObjectMap.of;

public class BattleCore extends CoreBlock{
    public final static float logicControlCooldown = 60 * 2;

    public float healHealth = 0.4f;
    public boolean customMountLocation = false;
    public Seq<Float> customMountLocationsX = new Seq<>();
    public Seq<Float> customMountLocationsY = new Seq<>();
    public float rangeTime = 80;
    public float fadeTime = 20;
    public Seq<MountTurretType> mounts = new Seq<>();
    public int amount;
    public float totalRangeTime;
    public Seq<SoundLoop> loopSounds = new Seq<>();

    public Seq<TextureRegion[]> turrets = new Seq<>();
    public ObjectMap<Liquid, BulletType> liquidAmmoTypes = new ObjectMap<>();
    public Seq<ObjectMap<Liquid, BulletType>> liquidMountAmmoTypes = new Seq<>();
    public Seq<ObjectMap<Item, BulletType>> mountAmmoTypes = new Seq<>();

    public TextureRegion heatRegion, iconRegion;

    public int massIndex = 2;

    public BattleCore(String name){
        super(name);

        config(Item.class, (BattleCoreBuild tile, Item item) -> {
            tile.outputItem = item;
            if(tile.output.find(o -> o.item == item) == null) tile.output.add(new ItemStack(item, 0));
        });
        configClear((BattleCoreBuild tile) -> tile.outputItem = null);

        config(Point2.class, (Building tile, Point2 point) -> {
            //if(tile instanceof MultiTurret.MultiTurretBuild) ((MultiTurret.MultiTurretBuild)tile).link = Point2.pack(point.x + tile.tileX(), point.y + tile.tileY());
            if(tile instanceof BattleCore.BattleCoreBuild) ((BattleCore.BattleCoreBuild)tile).link = Point2.pack(point.x + tile.tileX(), point.y + tile.tileY());
        });
        config(Integer.class, (Building tile, Integer point) -> {
            //if(tile instanceof MultiTurret.MultiTurretBuild) ((MultiTurret.MultiTurretBuild)tile).link = point;
            if(tile instanceof BattleCore.BattleCoreBuild) ((BattleCore.BattleCoreBuild)tile).link = point;
        });
    }

    public void addMountTurret(MountTurretType... mounts){
        for(MountTurretType mount : mounts) this.mounts.add(mount);
        this.amount = this.mounts.size;
        this.totalRangeTime = rangeTime * this.mounts.size;
    }

    public void addCustomMountLocation(Float[] xy){
        customMountLocation = true;
        for(int ix = 0; ix < amount * 2; ix += 2) customMountLocationsX.add(xy[ix]);
        for(int iy = 1; iy < amount * 2; iy += 2) customMountLocationsY.add(xy[iy]);
    }


    public void ammos(MountTurretType.MultiTurretMountType ammotype, Object... objects){
        if(ammotype == MountTurretType.MultiTurretMountType.item) {
            mountAmmoTypes.add(OrderedMap.of(objects));
            liquidMountAmmoTypes.add((ObjectMap<Liquid, BulletType>) null);
        }
        if(ammotype == MountTurretType.MultiTurretMountType.liquid) {
            mountAmmoTypes.add((ObjectMap<Item, BulletType>) null);
            liquidMountAmmoTypes.add(OrderedMap.of(objects));
        }
        if(ammotype == MountTurretType.MultiTurretMountType.power
                || ammotype == MountTurretType.MultiTurretMountType.tract
                || ammotype == MountTurretType.MultiTurretMountType.point
                || ammotype == MountTurretType.MultiTurretMountType.repair
                || ammotype == MountTurretType.MultiTurretMountType.mass
                || ammotype == MountTurretType.MultiTurretMountType.drill){
            liquidMountAmmoTypes.add((ObjectMap<Liquid, BulletType>) null);
            mountAmmoTypes.add((ObjectMap<Item, BulletType>) null);
        }
    }

    @Override
    public void init(){
        consume(new ConsumeLiquidFilter(i -> liquidAmmoTypes.containsKey(i), 1f){
            @Override
            public float efficiency (Building entity){
                return entity.liquids.currentAmount() > 0.001f ? super.efficiency(entity) : 0;
            }
            @Override
            public void update(Building entity){ }
            @Override
            public void display(Stats stats){ }
        });

        super.init();
    }

    @Override
    public void createIcons(MultiPacker packer){
        Drawm.generateTeamRegion(packer, this);
        super.createIcons(packer);
    }
    @Override
    public void load(){
        super.load();
        heatRegion = Core.atlas.find(name + "-heat");
        Events.on(EventType.ClientLoadEvent.class, e -> {
            for(int i = 0; i < amount; i++){ //...why?
                //[Sprite, Outline, Heat, Fade Mask]
                TextureRegion[] sprites = {
                        Core.atlas.find("shar-"+mounts.get(i).name + ""),
                        Core.atlas.find("shar-"+mounts.get(i).name + "-outline"),
                        Core.atlas.find("shar-"+mounts.get(i).name + "-heat"),
                        Core.atlas.find("shar-"+mounts.get(i).name + "-mask")
                };
                turrets.add(sprites);

                //for some unknown reason, mounts cannot use @Load annotations...hmm
                mounts.get(i).laser = Core.atlas.find("shar-repair-laser");
                mounts.get(i).laserEnd = Core.atlas.find("shar-repair-laser-end");
                mounts.get(i).tractLaser = Core.atlas.find("shar-tlaser");
                mounts.get(i).tractLaserEnd = Core.atlas.find("shar-tlaser-end");
            }
        });
        for(int i = 0; i < mounts.size; i++) loopSounds.add(new SoundLoop(mounts.get(i).loopSound, mounts.get(i).loopVolume));
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        for(int i = 0; i < mounts.size; i++){
            float fade = Mathf.curve(Time.time % totalRangeTime, rangeTime * i, rangeTime * i + fadeTime) - Mathf.curve(Time.time % totalRangeTime, rangeTime * (i + 1) - fadeTime, rangeTime * (i + 1));
            float tX = x * tilesize + offset + (customMountLocation ? customMountLocationsX.get(i) : mounts.get(i).x);
            float tY = y * tilesize + offset + (customMountLocation ? customMountLocationsY.get(i) : mounts.get(i).y);

            Lines.stroke(3, Pal.gray);
            Draw.alpha(fade);
            if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.repair){
                Lines.dashCircle(tX, tY, mounts.get(i).repairRadius);
                Lines.stroke(1, Pal.heal);
                Draw.alpha(fade);
                Lines.dashCircle(tX, tY, mounts.get(i).repairRadius);
            }
            else{
                Lines.dashCircle(tX, tY, mounts.get(i).range);
                Lines.stroke(1, Vars.player.team().color);
                Draw.alpha(fade);
                Lines.dashCircle(tX, tY, mounts.get(i).range);
            }

            Draw.color(Vars.player.team().color, fade);
            Draw.rect(turrets.get(i)[3], tX, tY);
            Draw.reset();

            if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.mass){
                //check if a mass driver is selected while placing this driver
                if(!control.input.config.isShown()) continue;
                Building selected = control.input.config.getSelected();
                if(selected == null || !(selected.block instanceof MultiTurret) || !(selected.within(x * tilesize, y * tilesize, mounts.get(i).range))) continue;

                //if so, draw a dotted line towards it while it is in range
                float sin = Mathf.absin(Time.time, 6f, 1f);
                Tmp.v1.set(tX, tY).sub(selected.x, selected.y).limit((size / 2f + 1) * tilesize + sin + 0.5f);
                float x2 = x * tilesize - Tmp.v1.x, y2 = y * tilesize - Tmp.v1.y,
                        x1 = selected.x + Tmp.v1.x, y1 = selected.y + Tmp.v1.y;
                int segs = (int)(selected.dst(x * tilesize, y * tilesize) / tilesize);

                Lines.stroke(4f, Pal.gray);
                Lines.dashLine(x1, y1, x2, y2, segs);
                Lines.stroke(2f, Pal.placing);
                Lines.dashLine(x1, y1, x2, y2, segs);
                Draw.reset();
            }
        }
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{
                iconRegion
        };
    }

    void rowAdd(Table h, String str){
        h.row();
        h.add(str);
    }

    void addStatS(Table w, int i){
        w.left();
        w.row();
        w.add((mounts.get(i).title)).right().top();
        w.row();
        w.image(Core.atlas.find("shar-"+mounts.get(i).name + "-full")).size(60).scaling(Scaling.bounded).right().top();

        w.table(Tex.underline, h -> {
            h.left().defaults().padRight(3).left();

            rowAdd(h, "[lightgray]" + Stat.shootRange.localized() + ": [white]" + Strings.fixed((mounts.get(i).range) / tilesize, 1) + " " + StatUnit.blocks);
            if(!(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.point)) {
                rowAdd(h, "[lightgray]" + Stat.targetsAir.localized() + ": [white]" + (!(mounts.get(i).targetAir) ? Core.bundle.get("no") : Core.bundle.get("yes")));
                rowAdd(h, "[lightgray]" + Stat.targetsGround.localized() + ": [white]" + (!(mounts.get(i).targetGround) ? Core.bundle.get("no") : Core.bundle.get("yes")));
            }else rowAdd(h, "[lightgray]" + Core.bundle.format("stat.shar.targetsBullet") + ": [white]" + Core.bundle.get("yes"));
            if(mounts.get(i).inaccuracy > 0) rowAdd(h, "[lightgray]" + Stat.inaccuracy.localized() + ": [white]" + (mounts.get(i).inaccuracy) + " " + StatUnit.degrees.localized());
            if(mounts.get(i).chargeTime > 0.001f) rowAdd(h, "[lightgray]" + Core.bundle.get("stat.shar.chargeTime") + ": [white]" + Mathf.round(mounts.get(i).chargeTime/60, 100) + " " + Core.bundle.format("stat.shar.seconds"));
            if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.item) rowAdd(h, "[lightgray]" + Core.bundle.get("stat.shar.ammo-shot") + ": [white]" + (mounts.get(i).ammoPerShot));
            if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.tract) rowAdd(h, "[lightgray]" + Stat.damage.localized() + ": [white]" + Core.bundle.format("stat.shar.damage", mounts.get(i).damage * 60f));
            if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.repair) rowAdd(h, "[lightgray]" + Stat.range.localized() + ": [white]" + Core.bundle.format("stat.shar.range", mounts.get(i).repairRadius / tilesize));
            if(mounts.get(i).mountType != MountTurretType.MultiTurretMountType.repair || mounts.get(i).mountType != MountTurretType.MultiTurretMountType.tract) rowAdd(h, "[lightgray]" + Stat.reload.localized() + ": [white]" + Strings.autoFixed(60 / (mounts.get(i).reload) * (mounts.get(i).shots), 1));

            h.row();

            ObjectMap<ObjectMap<BulletType ,? extends UnlockableContent>, TextureRegion> types = new ObjectMap<>();
            //tract puts nothing lol
            if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.power) {
                BulletType bullet = mounts.get(i).bullet;
                if(bullet != null) types.put(of(bullet, null), Icon.power.getRegion());
            }

            if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.item){
                for(Item item : content.items()){
                    BulletType bullet = mountAmmoTypes.get(i).get(item);
                    if(bullet != null) types.put(of(bullet, item), item.uiIcon);
                }
            }

            if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.liquid){
                for(Liquid liquid : content.liquids()) {
                    BulletType bullet = liquidMountAmmoTypes.get(i).get(liquid);
                    if(bullet != null) types.put(of(bullet, liquid), liquid.uiIcon);
                }
            }

            h.table(b -> {
                if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.tract
                        || mounts.get(i).mountType == MountTurretType.MultiTurretMountType.point
                        || mounts.get(i).mountType == MountTurretType.MultiTurretMountType.repair){
                    b.add(new Stack() {{
                        add(new Table(o -> {
                            o.right();
                            o.add(new Image(Icon.power.getRegion())).size(8 * 3).padRight(4);
                        }));

                        add(new Table(t -> {
                            t.right().bottom();
                            t.add(((int) mounts.get(i).powerUse * 60) + "").fontScale(0.9f).color(Color.yellow).padTop(8);
                            t.pack();
                        }));
                    }}).padRight(4).right().top();

                    b.add(Core.bundle.format("stat.shar.power")).padRight(10).left().top();
                }
                else for(ObjectMap<BulletType, ? extends UnlockableContent> type : types.keys()){
                    int ii = 0;

                    for(BulletType bullet : type.keys()){
                        ii ++;

                        if(type.get(bullet) == null) {
                            b.add(new Stack(){{
                                add(new Table(o -> {
                                    o.right();
                                    o.add(new Image(Icon.power.getRegion())).size(8 * 3).padRight(4);
                                }));

                                add(new Table(t -> {
                                    t.right().bottom();
                                    t.add(((int)mounts.get(i).powerUse * 60) + "").fontScale(0.9f).color(Color.yellow).padTop(8);
                                    t.pack();
                                }));
                            }}).padRight(4).right().top();

                            b.add(Core.bundle.format("stat.shar.power")).padRight(10).left().top();
                        }else {
                            b.image(types.get(type)).size(8 * 4).padRight(4).right().top();
                            b.add(type.get(bullet).localizedName).padRight(10).left().top();
                        }

                        b.table(Tex.underline, e -> {
                            e.left().defaults().padRight(3).left();

                            if(bullet.damage > 0 && (bullet.collides || bullet.splashDamage <= 0)) rowAdd(e, Core.bundle.format("bullet.damage", bullet.damage));
                            if(bullet.buildingDamageMultiplier != 1) rowAdd(e, Core.bundle.format("bullet.buildingdamage", Strings.fixed((int)(bullet.buildingDamageMultiplier * 100),1)));
                            if(bullet.splashDamage > 0) rowAdd(e, Core.bundle.format("bullet.splashdamage", bullet.splashDamage, Strings.fixed(bullet.splashDamageRadius / tilesize, 1)));
                            if(bullet.ammoMultiplier > 0 && (mounts.get(i).mountType == MountTurretType.MultiTurretMountType.item) && !Mathf.equal(bullet.ammoMultiplier, 1f)) rowAdd(e, Core.bundle.format("bullet.multiplier", Strings.fixed(bullet.ammoMultiplier, 1)));
                            if(!Mathf.equal(bullet.reloadMultiplier, 1f)) rowAdd(e, Core.bundle.format("bullet.reloadCounter", bullet.reloadMultiplier));
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

        try {
            stats.remove(Stat.shootRange);
            stats.remove(Stat.inaccuracy);
            stats.remove(Stat.reload);
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
                addStatS(w, 0);
                table.row().left();
            }).left();

            table.row();
            table.left();
            table.add("[lightgray]" + Core.bundle.get("stat.shar.mini-t")).fillX().padLeft(24);

            //Mounts
            table.table(null, w -> {
                for(int i = 0; i < mounts.size; i++){
                    addStatS(w, i);
                    table.row();
                }
            });
        });
    }

    @Override
    public boolean canReplace(Block other){
        //coreblocks can upgrade smaller cores
        return super.canReplace(other) || (other instanceof CoreBlock && !(other instanceof BattleCore) && size >= other.size);
    }

    public class BattleCoreBuild extends CoreBuild implements ControlBlock, Ranged {
        public Seq<Integer> _totalAmmos = new Seq<>();
        public Seq<Integer> _targetIDs = new Seq<>();
        public Seq<Float> _mineTimers = new Seq<>();
        public Seq<Float> __heats = new Seq<>();
        public Seq<Float> ___heats = new Seq<>();
        public Seq<Float> _reloads = new Seq<>();
        public Seq<Float> _heats = new Seq<>();
        public Seq<Float> _recoils = new Seq<>();
        public Seq<Float> _shotCounters = new Seq<>();
        public Seq<Float> _rotations = new Seq<>();
        public Seq<Float> _lastXs = new Seq<>();
        public Seq<Float> _lastYs = new Seq<>();
        public Seq<Float> _strengths = new Seq<>();
        public Seq<Float> _charges = new Seq<>();
        public Seq<Boolean> _wasShootings = new Seq<>();
        public Seq<Boolean> _chargings = new Seq<>();
        public Seq<Boolean> _anys = new Seq<>();
        public Seq<Posc> _targets = new Seq<>();
        public Seq<Unit> _tractTargets = new Seq<>();
        public Seq<Unit> _repairTargets = new Seq<>();
        public Seq<Bullet> _pointTargets = new Seq<>();
        public Seq<Building> _healTargets = new Seq<>();
        public Seq<Vec2> _targetPoses = new Seq<>();
        public Seq<Seq<BattleCore.ItemEntry>> _ammos = new Seq<>();
        public Seq<Tile> _mineTiles = new Seq<>();
        public Seq<Tile> _ores = new Seq<>();
        public Seq<Item> _targetItems = new Seq<>();
        public Seq<Seq<Tile>> _proxOreses = new Seq<>();
        public Seq<Seq<Item>> _proxItemses = new Seq<>();

        public float logicControlTime = -1;
        public boolean logicShooting = false;
        public Item outputItem;
        public int outputAmount;
        public Seq<ItemStack> output = new Seq<>();
        public int link = -1;
        public MassDriver.DriverState massState = MassDriver.DriverState.idle;
        public OrderedSet<Tile> waitingShooters = new OrderedSet<>();

        public float scrollPos;
        public boolean massFired;
        public float scrollMax;
        public float pastvalue;

        public @Nullable BlockUnitc unit;

        @Override
        public void remove(){
            super.remove();
            if(sound != null) sound.stop();
        }

        @Override
        public float range(){
            return mounts.sort(((m -> m.range))).peek().range;
        }

        @Override
        public Unit unit() {
            if(unit == null){
                unit = (BlockUnitc)UnitTypes.block.create(team);
                unit.tile(this);
            }
            return (Unit)unit;
        }

        @Override
        public void created(){
            super.created();
            for(int i = 0; i < Vars.content.items().size; i++) output.add(new ItemStack(Vars.content.items().get(i), 0));

            for(int i = 0; i < mounts.size; i++){
                _targetIDs.add(-1);
                _totalAmmos.add(0);
                _reloads.add(0f);
                _heats.add(0f);
                _recoils.add(0f);
                _shotCounters.add(0f);
                _rotations.add(90f);
                _charges.add(0f);
                _mineTimers.add(0f);
                __heats.add(0f);
                ___heats.add(0f);
                _targets.add((Posc) null);
                _tractTargets.add((Unit) null);
                _pointTargets.add((Bullet) null);
                _repairTargets.add((Unit) null);
                _healTargets.add((Building) null);
                _targetPoses.add(new Vec2());
                _wasShootings.add(false);
                _chargings.add(false);
                _ammos.add(new Seq<ItemEntry>());
                _lastXs.add(0f);
                _lastYs.add(0f);
                _strengths.add(0f);
                _anys.add(false);
                _proxItemses.add(new Seq<Item>());
                _proxOreses.add(new Seq<Tile>());
                _mineTiles.add((Tile) null);
                _ores.add((Tile) null);
                _targetItems.add((Item) null);
                if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.drill) mountReMap(i);
            }
        }

        @Override
        public void buildConfiguration(Table table){
            if(massFired) {
                table.clear();
                buildConfiguration(table);
            }
            ItemSelection.buildTable(table, content.items(), () -> outputItem, this::configure, false);

            Table t = new Table();
            t.defaults().size(40);
            int w = 0;
            for(int i = 1; i <= Vars.content.items().size; i++){
                final int j = i - 1;
                t.add(new Stack(){{
                    add(new Table(o -> {
                        o.left();
                        o.add(new Image(output.get(j).item.uiIcon)).size(32f);
                    }));

                    add(new Table(h -> {
                        h.right().top();
                        h.add(new Label(() -> output.get(j).amount > 1000 ? UI.formatAmount(output.get(j).amount) : output.get(j).amount + "")).fontScale(0.8f).color(output.get(j).item.color);
                        h.pack();
                    }));
                }});
                if(w++ % 4 == 3){
                    t.row();
                }
            }
            if(w % 4 != 0){
                int remaining = 4 - (w % 4);
                for(int j = 0; j < remaining; j++){
                    t.image(Styles.black6);
                }
            }

            ScrollPane pane = new ScrollPane(t, Styles.smallPane);
            pane.setScrollingDisabled(true, false);
            pane.setScrollYForce(scrollPos);
            pane.update(() -> scrollPos = pane.getScrollY());

            pane.setOverscroll(false, false);
            table.add(pane).maxHeight(Scl.scl(40 * 5));
            table.row();
            table.add(new Stack(){{
                add(new Table(tt -> {
                    tt.top();
                    Slider slide = new Slider(0, scrollMax, 1f, false);
                    update(() -> slide.setRange(0, scrollMax));
                    if(output.find(o -> o.item == outputItem) != null) slide.setValue(output.find(o -> o.item == outputItem).amount);
                    else slide.setValue(pastvalue);
                    slide.moved(i -> {
                        ItemStack stack = output.find(o -> o.item == outputItem);
                        if(stack != null) {
                            stack.set(outputItem, (int)i);
                            pastvalue = i;
                        }
                        outputAmount = (int) i;
                    });
                    tt.add(slide).width(block.size * 3f - 20).padTop(4f);
                    tt.pack();
                }));
                add(new Table(tt -> {
                    tt.top();
                    Label label = new Label(() -> {
                        if(outputItem == null || !items.has(outputItem)) return Core.bundle.format("ui.itemnotfound");
                        Color col = Color.white.cpy().lerp(outputItem.color, Mathf.clamp(output.find(o -> o.item == outputItem).amount / (scrollMax)));
                        return "[#" + col.toString() + "]" + output.find(o -> o.item == outputItem).amount + "";
                    });
                    tt.add(label);
                }));
            }}).padTop(5).growX().top();
        }

        public void tf(Table table, int i){
            if(mounts.size > 3 && i % 4 == 0) table.row();

            table.add(new Stack(){{
                add(new Table(o -> {
                    o.left();
                    o.add(new Image(Core.atlas.find("shar-" + mounts.get(i).name + "-full")));
                }));

                add(new Table(h -> {
                    if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.item) {
                        MultiReqImage itemReq = new MultiReqImage();

                        for(Item item : mountAmmoTypes.get(i).keys()) itemReq.add(new ReqImage(item.uiIcon, () -> mountHasAmmo(i)));

                        h.add(new Stack(){{
                            add(new Table(e -> {
                                e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*2f);
                                Bar itemBar = mountHasAmmo(i) ?
                                        new Bar(
                                                "",
                                                _ammos.get(i).peek().item.color,
                                                () -> _totalAmmos.get(i) / mounts.get(i).maxAmmo) :
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
                                        () -> Pal.accent.cpy().lerp(Color.orange, _reloads.get(i) / mounts.get(i).reload),
                                        () -> _reloads.get(i) / mounts.get(i).reload);
                                e.add(reloadBar);
                                e.pack();
                            }));

                            if(mounts.get(i).chargeTime >= 0.001) add(new Table(e -> {
                                e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*8f);
                                Bar chargeBar = new Bar(
                                        () -> "",
                                        () -> Pal.surge.cpy().lerp(Pal.accent, _reloads.get(i) / mounts.get(i).reload),
                                        () -> _charges.get(i));
                                e.add(chargeBar);
                                e.pack();
                            }));
                            add(mountHasAmmo(i) ? new Table(e -> e.add(new ItemImage(_ammos.get(i).peek().item.uiIcon, 1))) : new Table(e -> e.add(itemReq).size(iconSmall)));
                        }}).padTop(2*8).padLeft(2*8);

                    }

                    if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.liquid) {
                        MultiReqImage liquidReq = new MultiReqImage();

                        for(Liquid liquid : liquidMountAmmoTypes.get(i).keys()) liquidReq.add(new ReqImage(liquid.uiIcon, () -> mountHasAmmo(i)));

                        h.add(new Stack(){{
                            add(new Table(e -> {
                                e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*2f);
                                Bar liquidBar = mountHasAmmo(i) ? new Bar(
                                        "",
                                        liquids.current().color,
                                        () -> liquids.get(liquids.current()) / liquidCapacity) : new Bar("", new Color(0.1f, 0.1f, 0.1f, 1), () -> 0);
                                e.add(liquidBar);
                                e.pack();
                            }));

                            add(new Table(e -> {
                                e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*5f);
                                Bar reloadBar = new Bar(
                                        () -> "",
                                        () -> Pal.accent.cpy().lerp(Color.orange, _reloads.get(i) / mounts.get(i).reload),
                                        () -> _reloads.get(i) / mounts.get(i).reload);
                                e.add(reloadBar);
                                e.pack();
                            }));

                            if(mounts.get(i).chargeTime >= 0.001) add(new Table(e -> {
                                e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*8f);
                                Bar chargeBar = new Bar(
                                        () -> "",
                                        () -> Pal.surge.cpy().lerp(Pal.accent, _reloads.get(i) / mounts.get(i).reload),
                                        () -> _charges.get(i));
                                e.add(chargeBar);
                                e.pack();
                            }));
                            add(mountHasAmmo(i) ? new Table(e -> e.add(new ItemImage(liquids.current().uiIcon, 1))) : new Table(e -> e.add(liquidReq).size(iconSmall)));
                        }}).padTop(2*8).padLeft(2*8);

                    }

                    if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.power
                            || mounts.get(i).mountType == MountTurretType.MultiTurretMountType.tract
                            || mounts.get(i).mountType == MountTurretType.MultiTurretMountType.point
                            || mounts.get(i).mountType == MountTurretType.MultiTurretMountType.repair
                            || mounts.get(i).mountType == MountTurretType.MultiTurretMountType.mass
                            || mounts.get(i).mountType == MountTurretType.MultiTurretMountType.drill) {
                        MultiReqImage powerReq = new MultiReqImage();

                        powerReq.add(new ReqImage(Icon.powerSmall.getRegion(), () -> Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1) >= 0.001f));
                        h.add(new Stack(){{
                            add(new Table(e -> {
                                e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*2f);
                                Bar liquidBar = new Bar(
                                        "",
                                        Pal.powerBar,
                                        () -> Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1));
                                e.add(liquidBar);
                                e.pack();
                            }));

                            if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.power
                                    || mounts.get(i).mountType == MountTurretType.MultiTurretMountType.point
                                    || mounts.get(i).mountType == MountTurretType.MultiTurretMountType.mass)
                                add(new Table(e -> {
                                    e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*5f);
                                    Bar reloadBar = new Bar(
                                            () -> "",
                                            () -> Pal.accent.cpy().lerp(Color.orange, _reloads.get(i) / mounts.get(i).reload),
                                            () -> _reloads.get(i) / mounts.get(i).reload);
                                    e.add(reloadBar);
                                    e.pack();
                                }));

                            if(mounts.get(i).chargeTime >= 0.001) add(new Table(e -> {
                                e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*8f);
                                Bar chargeBar = new Bar(
                                        () -> "",
                                        () -> Pal.surge.cpy().lerp(Pal.accent, _reloads.get(i) / mounts.get(i).reload),
                                        () -> _charges.get(i));
                                e.add(chargeBar);
                                e.pack();
                            }));
                            add(new Table(e -> e.add(powerReq)));
                        }}).padTop(2*8).padLeft(2*8);
                    }

                    h.pack();
                }));
            }}).left();
        }

        @Override
        public void displayConsumption(Table table1){
            table1.table(Tex.underline, table -> table.table(scene.getStyle(Button.ButtonStyle.class).up, c -> {
                int q = 0;
                for(int i = 0; i < Vars.content.items().size; i++) {
                    q++;
                    final int hh = q;
                    final int h1 = i;
                    Item item = Vars.content.items().get(h1);

                    c.add(new Stack(){{
                        add(new Table(o -> {
                            o.left();
                            o.add(new Image(item.uiIcon)).size(32f);
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
            }).center()).center();
            table1.row();
            table1.table(scene.getStyle(Button.ButtonStyle.class).up, t ->{
                for(int i = 0; i < mounts.size; i++) {
                    t.center();
                    tf(t, i);
                }
            });
        }

        @Override
        public void displayBars(Table bars){
            for(Func<Building, Bar> bar : block.listBars()){
                try{
                    bars.add(bar.get(self())).growX();
                    bars.row();
                }catch(ClassCastException e){
                    break;
                }
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            for(int i = 0; i < mounts.size; i++){
                float fade = Mathf.curve(Time.time % totalRangeTime, rangeTime * i, rangeTime * i + fadeTime) - Mathf.curve(Time.time % totalRangeTime, rangeTime * (i + 1) - fadeTime, rangeTime * (i + 1));
                float[] loc = mountLocations(i);
                Lines.stroke(3, Pal.gray);
                Draw.alpha(fade);

                if(_mineTiles.get(i) != null){
                    Lines.dashCircle(loc[0], loc[1], mounts.get(i).range);
                    Lines.stroke(1, this.team.color);
                    Draw.alpha(fade);
                    Lines.dashCircle(loc[0], loc[1], mounts.get(i).range);

                    Lines.stroke(1f, Pal.accent);
                    Lines.poly(_mineTiles.get(i).worldx(), _mineTiles.get(i).worldy(), 4, tilesize / 2f * Mathf.sqrt2, Time.time);
                    Draw.alpha(fade);
                }
                if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.repair){
                    Lines.dashCircle(loc[0], loc[1], mounts.get(i).repairRadius);
                    Lines.stroke(1, Pal.heal);
                    Draw.alpha(fade);
                    Lines.dashCircle(loc[0], loc[1], mounts.get(i).repairRadius);
                }
                else{
                    Lines.dashCircle(loc[0], loc[1], mounts.get(i).range);
                    Lines.stroke(1, this.team.color);
                    Draw.alpha(fade);
                    Lines.dashCircle(loc[0], loc[1], mounts.get(i).range);
                }

                Draw.z(Layer.turret + 1);
                Draw.color(this.team.color, fade);
                Draw.rect(turrets.get(i)[3], loc[2], loc[3], this._rotations.get(i) - 90);
                Draw.reset();
            }
        }

        @Override
        public void drawConfigure(){
            for(int i = 0; i < mounts.size; i++){
                if(mounts.get(i).mountType != MountTurretType.MultiTurretMountType.mass) continue;

                float[] loc = mountLocations(i);
                float sin = Mathf.absin(Time.time, 6f, 1f);

                Draw.color(Pal.accent);
                Lines.stroke(1f);
                Drawf.circles(loc[0], loc[1], (tile.block().size  / 2f/ 2f + 1) * tilesize + sin - 2f, Pal.accent);

                for(Tile shooter : waitingShooters){
                    Drawf.circles(shooter.drawx(), shooter.drawy(), (tile.block().size / 2f / 2f + 1) * tilesize + sin - 2f, Pal.place);
                    Drawf.arrow(shooter.drawx(), shooter.drawy(), loc[0], loc[1], size / 2f * tilesize + sin, 4f + sin, Pal.place);
                }

                if(linkValid(i)){
                    Building target = world.build(link);
                    Drawf.circles(target.x, target.y, (target.block().size / 2f / 2f + 1) * tilesize + sin - 2f, Pal.place);
                    Drawf.arrow(loc[0], loc[1], target.x, target.y, size / 2f * tilesize + sin, 4f + sin);
                }

                Drawf.dashCircle(loc[0], loc[1], mounts.get(i).range, Pal.accent);
            }
        }

        @Override
        public int removeStack(Item item, int amount){
            return super.removeStack(item, amount);
        }

        @Override
        public void handleItem(Building source, Item item){
            for(int h = 0; h < mounts.size; h++) {
                if(!(mountAmmoTypes.get(h) != null && mountAmmoTypes.get(h).get(item) != null
                        && _totalAmmos.get(h) + mountAmmoTypes.get(h).get(item).ammoMultiplier <= mounts.get(h).maxAmmo)) continue;
                BulletType type = mountAmmoTypes.get(h).get(item);
                if(type == null) continue;

                if(item == Items.pyratite) Events.fire(EventType.Trigger.flameAmmo);
                _totalAmmos.set(h, (int)(_totalAmmos.get(h) + type.ammoMultiplier));

                boolean asdf = true;
                for(int i = 0; i < _ammos.get(h).size; i++) {
                    BattleCore.ItemEntry entry = _ammos.get(h).get(i);

                    if(entry.item == item) {
                        entry.amount += (int)type.ammoMultiplier;
                        _ammos.get(h).swap(i, _ammos.get(h).size - 1);
                        asdf = false;
                        break;
                    }
                }

                if(asdf) {
                    _ammos.get(h).add(new BattleCore.ItemEntry(item, (int)type.ammoMultiplier < mounts.get(h).ammoPerShot ? (int)type.ammoMultiplier + mounts.get(h).ammoPerShot : (int)type.ammoMultiplier));
                }
            }
            super.handleItem(source, item);
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            boolean h = false;
            for(int i = 0; i < mounts.size; i++) {
                h = mountAmmoTypes.get(i) != null && mountAmmoTypes.get(i).get(item) != null
                        && _totalAmmos.get(i) + mountAmmoTypes.get(i).get(item).ammoMultiplier <= mounts.get(i).maxAmmo;
            }
            return h || super.acceptItem(source, item);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            for(int i = 0; i < mounts.size; i++)
                if(liquidMountAmmoTypes.get(i) != null
                        && liquidMountAmmoTypes.get(i).get(liquid) != null
                        && (liquids.current() == liquid || (liquidMountAmmoTypes.get(i).containsKey(liquid)
                        && (!liquidMountAmmoTypes.get(i).containsKey(liquids.current()) || liquids.get(liquids.current()) <= 1f / liquidMountAmmoTypes.get(i).get(liquids.current()).ammoMultiplier + 0.001f))))
                    return true;
            return false;
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            boolean h = false;
            int past1 = 0;

            for(int i = 0; i < mounts.size; i++) {
                h = mountAmmoTypes.get(i) != null && mountAmmoTypes.get(i).get(item) != null
                        && _totalAmmos.get(i) + mountAmmoTypes.get(i).get(item).ammoMultiplier <= mounts.get(i).maxAmmo;
                if(mountAmmoTypes.get(i) != null) past1 = Math.min(past1, (int)((mounts.get(i).maxAmmo - _totalAmmos.get(i)) / mountAmmoTypes.get(i).get(item).ammoMultiplier));
            }
            if(h) return Math.min(past1, amount);
            return super.acceptStack(item, amount, source);
        }

        public float[] mountLocations(int mount){
            Tmp.v1.trns(this.rotation - 90, (customMountLocation ? customMountLocationsX.get(mount) : mounts.get(mount).x), (customMountLocation ? customMountLocationsY.get(mount) : mounts.get(mount).y));
            Tmp.v1.add(x, y);
            Tmp.v2.trns(_rotations.get(mount), -_recoils.get(mount));
            float i = (_shotCounters.get(mount) % mounts.get(mount).barrels) - (mounts.get(mount).barrels - 1) / 2;
            Tmp.v3.trns(_rotations.get(mount) - 90, mounts.get(mount).shootX + mounts.get(mount).barrelSpacing * i + mounts.get(mount).xRand, mounts.get(mount).shootY + mounts.get(mount).yRand);

            float x = Tmp.v1.x;
            float y = Tmp.v1.y;
            float rX = x + Tmp.v2.x;
            float rY = y + Tmp.v2.y;
            float sX = rX + Tmp.v3.x;
            float sY = rY + Tmp.v3.y;

            return new float[]{x, y, rX, rY, sX, sY};
        }

        @Override
        public BlockStatus status() {
            BlockStatus h = BlockStatus.noInput;
            for(int i = 0; i < mounts.size; i++) {
                if(i==mounts.size-1) return h;
                h = BlockStatus.noInput;
                if(mountHasAmmo(i)) h = BlockStatus.active;
            }
            return h;
        }

        @Override
        public void draw() {
            super.draw();

            for(int i = 0; i < mounts.size; i++){
                float[] loc = mountLocations(i);

                Drawf.shadow(turrets.get(i)[1], loc[2] - mounts.get(i).elevation, loc[3] - mounts.get(i).elevation, _rotations.get(i) - 90);
            }

            for(int i = 0; i < mounts.size; i++){
                float[] loc = mountLocations(i);

                Draw.rect(turrets.get(i)[1], loc[2], loc[3], _rotations.get(i) - 90);
                Draw.rect(turrets.get(i)[0], loc[2], loc[3], _rotations.get(i) - 90);

                if(turrets.get(i)[2] != Core.atlas.find("error") && _heats.get(i) > 0.00001){
                    Draw.color(mounts.get(i).heatColor, _heats.get(i));
                    Draw.blend(Blending.additive);
                    Draw.rect(turrets.get(i)[2], loc[2], loc[3], _rotations.get(i) - 90);
                    Draw.blend();
                    Draw.color();
                }

                if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.tract && _anys.get(i)){
                    Draw.z(Layer.bullet);
                    float ang = angleTo(_lastXs.get(i), _lastYs.get(i));

                    Draw.mixcol(mounts.get(i).laserColor, Mathf.absin(4f, 0.6f));
                    Drawf.laser(mounts.get(i).tractLaser, mounts.get(i).tractLaserEnd,
                            loc[0] + Angles.trnsx(ang, mounts.get(i).shootLength), loc[1] + Angles.trnsy(ang, mounts.get(i).shootLength),
                            _lastXs.get(i), _lastYs.get(i), _strengths.get(i) * Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1) * mounts.get(i).laserWidth);

                    Draw.reset();
                }
                if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.repair
                        && _repairTargets.get(i) != null
                        && Angles.angleDist(angleTo(_repairTargets.get(i)), _rotations.get(i)) < 30f){
                    Draw.z(Layer.flyingUnit + 1); //above all units
                    float ang = angleTo(_repairTargets.get(i));
                    float len = 5f;

                    Draw.color(mounts.get(i).laserColor);
                    Drawf.laser(mounts.get(i).laser, mounts.get(i).laserEnd,
                            loc[0] + Angles.trnsx(ang, len), loc[1] + Angles.trnsy(ang, len),
                            _repairTargets.get(i).x(), _repairTargets.get(i).y(), _strengths.get(i));

                    Draw.reset();
                }
                if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.drill
                        && _mineTiles.get(i) != null){
                    float focusLen = mounts.get(i).laserOffset / 2f + Mathf.absin(Time.time, 1.1f, 0.5f);
                    float swingScl = 12f, swingMag = tilesize / 8f;
                    float flashScl = 0.3f;

                    float px = loc[0] + Angles.trnsx(_rotations.get(i), focusLen);
                    float py = loc[1] + Angles.trnsy(_rotations.get(i), focusLen);

                    float ex = _mineTiles.get(i).worldx() + Mathf.sin(Time.time + 48, swingScl, swingMag);
                    float ey = _mineTiles.get(i).worldy() + Mathf.sin(Time.time + 48, swingScl + 2f, swingMag);

                    Draw.z(Layer.flyingUnit + 0.1f);
                    Draw.color(Color.lightGray, Color.white, 1f - flashScl + Mathf.absin(Time.time, 0.5f, flashScl));
                    Drawf.laser(mounts.get(i).laser, mounts.get(i).laserEnd, px, py, ex, ey, mounts.get(i).laserWidth);

                    Draw.reset();
                }
            }

            Draw.reset();
        }


        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4){
            if(type == LAccess.shoot && !((ControlBlock) this).unit().isPlayer()){
                for(int i = 0; i < mounts.size; i++) _targetPoses.get(i).set(World.unconv((float)p1), World.unconv((float)p2));
                logicControlTime = logicControlCooldown;
                logicShooting = !Mathf.zero(p3);
            }

            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public void control(LAccess type, Object p1, double p2, double p3, double p4){
            if(type == LAccess.shootp && !((ControlBlock) this).unit().isPlayer()){
                logicControlTime = logicControlCooldown;
                logicShooting = !Mathf.zero(p2);

                if(p1 instanceof Posc){
                    for(int i = 0; i < mounts.size; i++){
                        if(!mountHasAmmo(i)) return;
                        BulletType bullet = mountPeekAmmo(i);
                        float speed = bullet.speed;
                        //slow bullets never intersect
                        if(speed < 0.1f) speed = 9999999f;

                        _targetPoses.get(i).set(Predict.intercept(this, (Posc)p1, speed));
                        if(_targetPoses.get(i).isZero()) _targetPoses.get(i).set((Posc)p1);
                    }
                }
            }

            super.control(type, p1, p2, p3, p4);
        }

        public boolean logicControlled(){
            return logicControlTime > 0;
        }

        protected float baseReloadSpeed(){
            return efficiency();
        }

        @Override
        public void update() {
            super.update();
            for(int i = 0; i < mounts.size; i++)
                if(!Vars.headless && loopSounds.get(i) != null)
                    loopSounds.get(i).update(mountLocations(i)[4], mountLocations(i)[5], _wasShootings.get(i) && !dead());
        }

        public float __heat;

        @Override
        public void updateTile() {
            //set block's ammo
            for(int i = 0; i < mounts.size; i++){
                ((ControlBlock) this).unit().ammo((float)((ControlBlock) this).unit().type().ammoCapacity * _totalAmmos.get(i) /  mounts.get(i).maxAmmo);
                ((ControlBlock) this).unit().ammo(((ControlBlock) this).unit().type().ammoCapacity * liquids.currentAmount() / liquidCapacity);
                ((ControlBlock) this).unit().ammo(power.status * ((ControlBlock) this).unit().type().ammoCapacity);
            }

            ((ControlBlock) this).unit().health(health);
            ((ControlBlock) this).unit().rotation(rotation);
            ((ControlBlock) this).unit().team(team);
            ((ControlBlock) this).unit().set(x, y);

            super.updateTile();
            if(outputItem != null) scrollMax = items.get(outputItem);
            if(mounts.find(m -> linkValid(mounts.indexOf(m))) != null && world.build(link) != null) scrollMax = Math.min(world.build(link).block.itemCapacity, scrollMax);

            if(logicControlTime > 0) logicControlTime -= Time.delta;

            float maxUsed = block.<ConsumeLiquidBase>findConsumer(consume -> consume instanceof ConsumeLiquidBase).amount / mounts.size;

            Liquid liquid = liquids.current();

            for(int i = 0; i < mounts.size; i++){
                if(!(mounts.get(i).acceptCooling) || liquidMountAmmoTypes.get(i) == null) continue;
                float used = Math.min(Math.min(liquids.get(liquid), maxUsed * Time.delta), Math.max(0, ((mounts.get(i).reload - _reloads.get(i)) / mounts.get(i).coolantMultiplier) / liquid.heatCapacity));
                if(mounts.get(i).powerUse > 0.001f) used *= Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1);
                else used *= baseReloadSpeed();
                _reloads.set(i, _reloads.get(i) + used * liquid.heatCapacity * mounts.get(i).coolantMultiplier);

                liquids.remove(liquid, used);

                float[] loc = mountLocations(i);

                if(Mathf.chance(0.06 / mounts.size * used)) mounts.get(i).coolEffect.at(loc[0] + Mathf.range(mounts.get(i).width), loc[1] + Mathf.range(mounts.get(i).height));
            }

            for(int i = 0; i < mounts.size; i++){
                _wasShootings.set(i, false);
                _recoils.set(i, Mathf.lerpDelta(_recoils.get(i), 0, mounts.get(i).restitution));
                _heats.set(i, Mathf.lerpDelta(_heats.get(i), 0, mounts.get(i).cooldown));
                if(mounts.get(i).chargeTime >= 0.001 && _chargings.get(i) && mountHasAmmo(i))
                    _charges.set(i, Mathf.clamp(_charges.get(i) + Time.delta / mounts.get(i).chargeTime));
                else _charges.set(i, 0f);

                if(!mountTargetValid(i)) {
                    _targets.set(i, null);
                    _tractTargets.set(i, null);
                    _pointTargets.set(i, null);
                    _healTargets.set(i, null);
                }

                __heats.set(i, __heats.get(i) - delta());
                ___heats.set(i, ___heats.get(i) - delta());
            }

            //get targets
            __heat -= delta();
            if(__heat <= 0.001){
                for(int i = 0; i < mounts.size; i++){
                    float[] loc = this.mountLocations(i);

                    if(mountHasAmmo(i)) {
                        this.mountLocations(i);
                        _targets.set(i, mountTargetFind(i));
                        _healTargets.set(i, Units.findAllyTile(team, loc[0], loc[1], mounts.get(i).range, b -> b.damaged() && b != this));

                        if(mounts.get(i).healBlock && Units.findAllyTile(team, loc[0], loc[1], mounts.get(i).range, b -> damaged() && b.health <= b.block.health * healHealth) != null) _targets.set(_healTargets.copy());
                        final int j = i;
                        _tractTargets.set(i, Units.closestEnemy(this.team, loc[0], loc[1], mounts.get(i).range, u -> u.checkTarget(mounts.get(j).targetAir, mounts.get(j).targetGround)));
                        _pointTargets.set(i, Groups.bullet.intersect(loc[0] - mounts.get(i).range, loc[1] - mounts.get(i).range, mounts.get(i).range * 2, mounts.get(i).range * 2).min(b -> b.team != team && b.type().hittable, b -> b.dst2(new Vec2(loc[0], loc[1]))));

                        MountTurretType.rect.setSize(mounts.get(i).repairRadius * 2).setCenter(loc[0], loc[1]);
                        _repairTargets.set(i, Units.closest(team, loc[0], loc[1], mounts.get(i).repairRadius, Unit::damaged));
                    }
                }
                __heat = 16;
            }

            for(int i = 0; i < mounts.size; i++){
                if(!mountHasAmmo(i)) continue;

                if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.mass) mountUpdateMass(i);
                else if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.drill) mountUpdateDrill(i);
                else if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.tract) mountUpdateTract(i);
                else if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.point) mountUpdatePoint(i);
                else if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.repair) mountUpdateRepair(i);
                else if(mountTargetValid(i)) { //item, liquid, power
                    float[] loc = this.mountLocations(i);
                    boolean canShoot = true;

                    if(isControlled()) { //player behavior
                        _targetPoses.get(i).set(unit().aimX, unit().aimY);
                        canShoot = unit().isShooting;
                    }else if(this.logicControlled()) { //logic behavior
                        canShoot = logicShooting;
                    }else { //default AI behavior
                        mountTargetPosition(i, _targets.get(i), loc[0], loc[1]);
                        if(Float.isNaN(_rotations.get(i))) _rotations.set(i, 0f);
                    }

                    float targetRot = Angles.angle(loc[0], loc[1], _targetPoses.get(i).x, _targetPoses.get(i).y);

                    if(!_chargings.get(i)) mountTargetTurn(i, targetRot);

                    if (Angles.angleDist(_rotations.get(i), targetRot) < mounts.get(i).shootCone && canShoot) {
                        _wasShootings.set(i, true);
                        mountUpdateShooting(i);
                    }
                }
            }
        }

        public void mountUpdateMass(int i){
            Building link = world.build(this.link);
            boolean hasLink = linkValid(i);
            float[] loc = this.mountLocations(i);

            if(hasLink) this.link = link.pos();

            //reloadCounter regardless of state
            if(_reloads.get(i) < mounts.get(i).reload) _reloads.set(i, _reloads.get(i) + delta() * Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1));


            //cleanup waiting shooters that are not valid
            if(!shooterValid(currentShooter(), i)){
                waitingShooters.remove(currentShooter());
            }

            // states
            if(massState == MassDriver.DriverState.idle){
                //start accepting when idle and there's space
                if(!waitingShooters.isEmpty() && (itemCapacity - items.total() >= mounts.get(i).minDistribute)){
                    massState = MassDriver.DriverState.accepting;
                }else if(hasLink){ //switch to shooting if there's a valid link.
                    massState = MassDriver.DriverState.shooting;
                }
            }

            //skip when there's no power
            if(Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1) <= 0.001f) return;

            if(massState == MassDriver.DriverState.accepting){
                //if there's nothing shooting at this, bail - OR, items full
                if(currentShooter() == null || (itemCapacity - items.total() < mounts.get(i).minDistribute)){
                    massState = MassDriver.DriverState.idle;
                    return;
                }
                _rotations.set(i, Mathf.slerpDelta(_rotations.get(i), tile.angleTo(currentShooter()), mounts.get(i).rotateSpeed * Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1)));

            }else if(massState == MassDriver.DriverState.shooting){
                //if there's nothing to shoot at OR someone wants to shoot at this thing, bail
                if(!hasLink || (!waitingShooters.isEmpty() && (itemCapacity - items.total() >= mounts.get(i).minDistribute))){
                    massState = MassDriver.DriverState.idle;
                    return;
                }

                float targetRotation = tile.angleTo(link);

                if(
                        items.total() >= mounts.get(i).minDistribute && //must shoot minimum amount of items
                        link.block.itemCapacity - link.items.total() >= mounts.get(i).minDistribute //must have minimum amount of space
                ){
                    if(link instanceof BattleCoreBuild other){
                        other.waitingShooters.add(tile);

                        if(_reloads.get(i) >= mounts.get(i).reload && !_chargings.get(i)){
                            _rotations.set(i, Mathf.slerpDelta(_rotations.get(i), targetRotation, mounts.get(i).rotateSpeed * Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1)));

                            //fire when it's the first in the queue and angles are ready.
                            if(other.currentShooter() == tile &&
                                    other.massState == MassDriver.DriverState.accepting
                                    && Angles.near(_rotations.get(i), targetRotation, 2f)
                                    && Angles.near(other._rotations.get(i), targetRotation + 180f, 2f)){
                                fire(i, other);

                                float timeToArrive = Math.min(mounts.get(i).bulletLifetime, Mathf.dst(loc[4], loc[5], other.x, other.y) / mounts.get(i).bulletSpeed);
                                Time.run(timeToArrive, () -> {
                                    //remove waiting shooters, it's done firing
                                    other.waitingShooters.remove(tile);
                                    other.massState = MassDriver.DriverState.idle;
                                });
                                //driver is immediately idle
                                massState = MassDriver.DriverState.idle;
                            }
                        }
                    }
                    else if(link instanceof MultiTurret.MultiTurretBuild other){
                        Seq<MountTurret> massmounts = other.mounts.copy().filter(m -> m.type.mountType == MountTurretType.MultiTurretMountType.mass);
                        for(int w = 0; w < massmounts.size; w++){
                            final int q = w;
                            massmounts.get(q).waitingShooters.add(tile);

                            if(_reloads.get(i) >= mounts.get(i).reload && !_chargings.get(i)){
                                _rotations.set(i, Mathf.slerpDelta(_rotations.get(i), targetRotation, mounts.get(i).rotateSpeed * Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1)));

                                //fire when it's the first in the queue and angles are ready.
                                if(massmounts.find(m -> m.type.mountType == MountTurretType.MultiTurretMountType.mass).currentShooter() == tile &&
                                        massmounts.get(q).massState == MassDriver.DriverState.accepting
                                        && Angles.near(_rotations.get(i), targetRotation, 2f)
                                        && Angles.near(massmounts.find(m -> m.type.mountType == MountTurretType.MultiTurretMountType.mass).rotation, targetRotation + 180f, 2f)){
                                    fire(i, other);

                                    float timeToArrive = Math.min(mounts.get(i).bulletLifetime, Mathf.dst(loc[4], loc[5], other.x, other.y) / mounts.get(i).bulletSpeed);

                                    Time.run(timeToArrive, () -> {
                                        //remove waiting shooters, it's done firing
                                        massmounts.get(q).waitingShooters.remove(tile);
                                        massmounts.get(q).massState = MassDriver.DriverState.idle;
                                    });
                                    //driver is immediately idle
                                    massState = MassDriver.DriverState.idle;
                                }
                            }
                        }
                    }
                }
            }
        }

        public void fire(int i, Building other){
            massFired = true;
            float[] loc = this.mountLocations(i);

            _reloads.set(i, 0f);

            DriverBulletData data = Pools.obtain(DriverBulletData.class, DriverBulletData::new);
            data.massMount = i;
            data.from = this;
            data.to = other;
            data.link = i;

            for(int h = 0; h < content.items().size; h++){
                int maxTransfer = Math.min(items.get(content.item(h)), Math.max(output.get(h).amount, 0));
                data.items[h] = maxTransfer;
                items.remove(content.item(h), maxTransfer);
            }
            float angle = tile.angleTo(other);
            if(other instanceof BattleCore.BattleCoreBuild) angle = Angles.angle(mountLocations(massIndex)[0], mountLocations(massIndex)[1], ((BattleCore.BattleCoreBuild)other).mountLocations(((BattleCore)other.block).massIndex)[0], ((BattleCore.BattleCoreBuild)other).mountLocations(((BattleCore)other.block).massIndex)[1]);
            if(other instanceof MultiTurret.MultiTurretBuild) angle = Angles.angle(mountLocations(massIndex)[0], mountLocations(massIndex)[1], ((MultiTurret.MultiTurretBuild)other).mounts.get(i).mountLocations((MultiTurret.MultiTurretBuild)other)[0], ((MultiTurret.MultiTurretBuild)other).mounts.get(i).mountLocations((MultiTurret.MultiTurretBuild)other)[1]);

            SBullets.mountDriverBolt.create(this, team,
                    loc[4] + Angles.trnsx(angle, mounts.get(i).translation), loc[5] + Angles.trnsy(angle, mounts.get(i).translation),
                    angle, -1f, mounts.get(i).bulletSpeed, mounts.get(i).bulletLifetime, data);

            mounts.get(i).shootEffect.at(loc[4] + Angles.trnsx(angle, mounts.get(i).translation), loc[5] + Angles.trnsy(angle, mounts.get(i).translation), angle);
            mounts.get(i).smokeEffect.at(loc[4] + Angles.trnsx(angle, mounts.get(i).translation), loc[5] + Angles.trnsy(angle, mounts.get(i).translation), angle);
            Effect.shake(mounts.get(i).shake, mounts.get(i).shake, new Vec2(loc[4], loc[5]));
            mounts.get(i).shootSound.at(tile, Mathf.random(0.9f, 1.1f));
            massFired = false;
        }

        public void handlePayload(Bullet bullet, DriverBulletData data){
            if(mounts.find(m -> m.mountType != MountTurretType.MultiTurretMountType.mass) == null) return;

            int totalItems = items.total();

            //add all the items possible
            for(int i = 0; i < data.items.length; i++){
                int maxAdd = Math.min(data.items[i], itemCapacity * 2 - totalItems);
                items.add(content.item(i), maxAdd);
                data.items[i] -= maxAdd;
                totalItems += maxAdd;

                if(totalItems >= itemCapacity * 2){
                    break;
                }
            }

            Effect.shake(mounts.get(data.massMount).shake, mounts.get(data.massMount).shake, this);
            mounts.get(data.massMount).receiveEffect.at(bullet);

            _reloads.set(data.massMount, 0f);
            bullet.remove();
        }

        public Tile currentShooter(){
            return waitingShooters.isEmpty() ? null : waitingShooters.first();
        }

        public boolean shooterValid(Tile other, int mount){
            if(other == null) return true;
            return (other.build instanceof BattleCore.BattleCoreBuild && ((BattleCore.BattleCoreBuild)other.build).link == tile.pos()) || (other.build instanceof MultiTurret.MultiTurretBuild && ((MultiTurret.MultiTurretBuild)other.build).mounts.copy().filter(m -> m.type.mountType == MountTurretType.MultiTurretMountType.mass).find(m -> m.link == tile.pos()) != null) && tile.dst(other) <= mounts.get(mount).range;
        }

        public boolean linkValid(int mount){
            if(link == -1) return false;
            Building link = world.build(this.link);
            return ((link instanceof MultiTurret.MultiTurretBuild
                    && ((MultiTurret)link.block).basicMounts.size - 1 >= mount
                    && ((MultiTurret)link.block).basicMounts.get(mount).mountType == MountTurretType.MultiTurretMountType.mass) || (link instanceof BattleCore.BattleCoreBuild
                    && ((BattleCore)link.block).mounts.size - 1 >= mount
                    && ((BattleCore)link.block).mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.mass))
                    && link.team == team
                    && within(link, mounts.get(mount).range);
        }
        @Override
        public Object config(){
            if(mounts.find(m -> m.mountType != MountTurretType.MultiTurretMountType.mass) != null) return null;
            return Point2.unpack(link).sub(tile.x, tile.y);
        }

        @Override
        public boolean onConfigureBuildTapped(Building other){
            if(this == other){
                configure(-1);
                return false;
            }
            if(link == other.pos()){
                configure(-1);
                return false;
            }else if(((other.block instanceof MultiTurret && ((MultiTurret)other.block).basicMounts.find(m -> m.mountType == MountTurretType.MultiTurretMountType.mass) != null)
                    || (other.block instanceof BattleCore && ((BattleCore)other.block).mounts.find(m -> m.mountType == MountTurretType.MultiTurretMountType.mass) != null))
                    && other.dst(tile) <= mounts.get(massIndex).range && other.team == team){
                configure(other.pos());
                return false;
            }
            return true;
        }

        public void mountUpdateDrill(int i){
            float[] loc = this.mountLocations(i);

            Building core = state.teams.closestCore(loc[4], loc[5], team);

            //target ore
            targetMine(core, i);
            if(core == null || _mineTiles.get(i) == null || Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1) <= 0.001f || !Angles.within(_rotations.get(i), angleTo(_mineTiles.get(i)), mounts.get(i).shootCone) || items.get(_mineTiles.get(i).drop()) >= itemCapacity){
                _mineTiles.set(i, null);
                _mineTimers.set(i, 0f);
            }

            if(_mineTiles.get(i) != null){
                //mine tile
                Item item = _mineTiles.get(i).drop();
                _mineTimers.set(i, _mineTimers.get(i) + Time.delta * mounts.get(i).mineSpeed * Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1));

                if(Mathf.chance(0.06 * Time.delta)){
                    Fx.pulverizeSmall.at(_mineTiles.get(i).worldx() + Mathf.range(tilesize / 2f), _mineTiles.get(i).worldy() + Mathf.range(tilesize / 2f), 0f, item.color);
                }

                if(_mineTimers.get(i) >= 50f + item.hardness * 15f){
                    _mineTimers.set(i, 0f);

                    if(state.rules.sector != null && team() == state.rules.defaultTeam) state.rules.sector.info.handleProduction(item, 1);

                    //items are synced anyway
                    InputHandler.transferItemTo(null, item, 1,
                            _mineTiles.get(i).worldx() + Mathf.range(tilesize / 2f),
                            _mineTiles.get(i).worldy() + Mathf.range(tilesize / 2f),
                            this);
                }

                if(!headless) control.sound.loop(mounts.get(i).shootSound, this, mounts.get(i).shootSoundVolume);
            }
        }
        public void targetMine(Building core, int i){
            //target ore
            if(core == null) return;

            if(___heats.get(i) <= 0.001 || _targetItems.get(i) == null){
                _targetItems.set(i, iterateMap(core, i));
                ___heats.set(i, 16f);
            }

            //if inventory is full, do not mine.
            if(_targetItems.get(i) == null || items.get(_targetItems.get(i)) >= itemCapacity){
                _mineTiles.set(i, null);
            }
            else{
                if(Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1) >= 0.2f && __heats.get(i) <= 0.001 && _targetItems.get(i) != null && _targetIDs.get(i) > -1){
                    _ores.set(i, _proxOreses.get(i).get(_targetIDs.get(i)));
                    __heats.set(i, 16f);
                }

                if(_ores.get(i) != null && Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1) >= 0.2f){
                    float dest = angleTo(_ores.get(i));
                    mountTargetTurn(i, dest);

                    if(Angles.within(_rotations.get(i), dest, mounts.get(i).shootCone)){
                        _mineTiles.set(i, _ores.get(i));
                    }
                    if(_ores.get(i).block() != Blocks.air){
                        if(_targetIDs.get(i) > -1) mountReFind(_targetIDs.get(i), i);
                        _targetItems.set(i, null);
                        _targetIDs.set(i, -1);
                        _mineTiles.set(i, null);
                    }
                }
            }
        }

        public @Nullable Item iterateMap(Building core, int i){
            if(_proxOreses.get(i) == null || !_proxOreses.get(i).any()) return null;
            Item last = null;
            _targetIDs.set(i, -1);
            for(int h = 0; h < _proxOreses.get(i).size; h++){
                if(mountCanMine(_proxItemses.get(i).get(h), i) && (last == null || last.lowPriority || core.items.get(last) > core.items.get(_proxItemses.get(i).get(h)))){
                    if(_proxOreses.get(i).get(h).block() != Blocks.air){
                        //try to relocate its ore
                        mountReFind(h, i);
                        //if it fails, ignore the ore
                        if(_proxOreses.get(i).get(h).block() != Blocks.air) continue;
                    }
                    last = _proxItemses.get(i).get(h);
                    _targetIDs.set(i, h);
                }
            }

            return last;
        }

        public void mountReMap(int mount){
            _proxOreses.set(mount, new Seq<>());
            _proxItemses.set(mount, new Seq<>());
            ObjectSet<Item> tempItems = new ObjectSet<>();

            float[] loc = mountLocations(mount);
            Geometry.circle((int) loc[0] / 8, (int)loc[1] / 8, (int)(mounts.get(mount).range / tilesize + 0.5f), (x, y) -> {
                Tile other = world.tile(x, y);
                if(other != null && other.drop() != null){
                    Item drop = other.drop();
                    if(!tempItems.contains(drop)){
                        tempItems.add(drop);
                        _proxItemses.get(mount).add(drop);
                        _proxOreses.get(mount).add(other);
                    }
                }
            });
        }

        public void mountReFind(int i, int mount){
            Item item = _proxItemses.get(mount).get(i);

            float[] loc = mountLocations(mount);
            Geometry.circle((int) loc[0] / 8, (int)loc[1] / 8, (int)(mounts.get(mount).range / tilesize + 0.5f), (x, y) -> {
                Tile other = world.tile(x, y);
                if(other != null && other.drop() != null && other.drop() == item && other.block() == Blocks.air){
                    _proxOreses.get(mount).set(i, other);
                }
            });
        }

        public boolean mountCanMine(Item item, int mount){
            return item.hardness >= mounts.get(mount).minDrillTier && item.hardness <= mounts.get(mount).maxDrillTier && items.get(item) < itemCapacity;
        }

        @Override
        public void removeFromProximity(){
            //reset when pushed
            for(int i = 0;i < mounts.size; i++) {
                _targetItems.set(i, null);
                _targetIDs.set(i, -1);
                _mineTiles.set(i, null);
            }
            super.removeFromProximity();
        }

        public void mountUpdateTract(int i){
            float[] loc = this.mountLocations(i);
            _anys.set(i, false);

            //look at target
            if(_tractTargets.get(i) != null
                    && _tractTargets.get(i).within(new Vec2(loc[0], loc[1]), mounts.get(i).range + _tractTargets.get(i).hitSize/2f)
                    && _tractTargets.get(i).team() != team
                    && _tractTargets.get(i).checkTarget(mounts.get(i).targetAir, mounts.get(i).targetGround)
                    && Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1) > 0.02f){
                if(!headless) control.sound.loop(mounts.get(i).shootSound, new Vec2(loc[0], loc[1]), mounts.get(i).shootSoundVolume);

                float dest = angleTo(_tractTargets.get(i));
                mountTargetTurn(i, dest);
                _lastXs.set(i, _tractTargets.get(i).x);
                _lastYs.set(i, _tractTargets.get(i).y);
                _strengths.set(i, Mathf.lerpDelta(_strengths.get(i), 1f, 0.1f));

                //shoot when possible
                if(Angles.within(_rotations.get(i), dest, mounts.get(i).shootCone)){
                    if(mounts.get(i).damage > 0) _tractTargets.get(i).damageContinuous(mounts.get(i).damage * Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1));

                    if(mounts.get(i).status != StatusEffects.none) _tractTargets.get(i).apply(mounts.get(i).status, mounts.get(i).statusDuration);

                    _anys.set(i, true);
                    _tractTargets.get(i).impulseNet(
                            Tmp.v1.set(new Vec2(loc[0], loc[1])).
                                    sub(_tractTargets.get(i)).
                                    limit((mounts.get(i).force + (1f - _tractTargets.get(i).dst(new Vec2(loc[0], loc[1])) / mounts.get(i).range) * mounts.get(i).scaledForce) * delta() * Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1) * timeScale));
                }
            }else {
                _strengths.set(i, Mathf.lerpDelta(_strengths.get(i), 0, 0.1f));
            }
        }

        public void mountUpdatePoint(int i){
            float[] loc = this.mountLocations(i);
            if(_pointTargets.get(i) != null
                    && _pointTargets.get(i).within(new Vec2(loc[0], loc[1]), mounts.get(i).range)
                    && _pointTargets.get(i).team != team
                    && _pointTargets.get(i).type() != null
                    && _pointTargets.get(i).type().hittable){
                float dest = angleTo(_pointTargets.get(i));
                mountTargetTurn(i, dest);
                _reloads.set(i, _reloads.get(i) + delta() * Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1));

                //shoot when possible
                if(Angles.within(_rotations.get(i), dest, mounts.get(i).shootCone) && _reloads.get(i) >= mounts.get(i).reload){
                    if(_pointTargets.get(i).damage() > mounts.get(i).bulletDamage) _pointTargets.get(i).damage(_pointTargets.get(i).damage() - mounts.get(i).bulletDamage);
                    else _pointTargets.get(i).remove();

                    Tmp.v1.trns(_rotations.get(i), mounts.get(i).shootLength);

                    mounts.get(i).beamEffect.at(loc[0] + Tmp.v1.x, loc[1] + Tmp.v1.y, _rotations.get(i), mounts.get(i).colorPoint, new Vec2().set(_pointTargets.get(i)));
                    mounts.get(i).shootEffect.at(loc[0] + Tmp.v1.x, loc[1] + Tmp.v1.y, _rotations.get(i), mounts.get(i).colorPoint);
                    mounts.get(i).hitEffect.at(_pointTargets.get(i).x, _pointTargets.get(i).y, mounts.get(i).colorPoint);
                    mounts.get(i).shootSound.at(loc[0] + Tmp.v1.x, loc[1] + Tmp.v1.y, Mathf.random(0.9f, 1.1f));

                    _reloads.set(i, 0f);
                }
            }
        }

        public void mountUpdateRepair(int i){
            float[] loc = this.mountLocations(i);
            boolean targetIsBeingRepaired = false;
            if(_repairTargets.get(i) != null){
                if(_repairTargets.get(i).dead()
                        || _repairTargets.get(i).dst(loc[0], loc[1]) - _repairTargets.get(i).hitSize / 2f > mounts.get(i).repairRadius
                        || _repairTargets.get(i).health() >= _repairTargets.get(i).maxHealth()) _repairTargets.set(i, null);
                else {
                    _repairTargets.get(i).heal(mounts.get(i).repairSpeed * Time.delta * _strengths.get(i) * Mathf.clamp(power.graph.getPowerBalance() / mounts.get(i).powerUse, 0, 1));
                    float dest = angleTo(_repairTargets.get(i));
                    mountTargetTurn(i, dest);
                    targetIsBeingRepaired = true;
                }
            }

            if(_repairTargets.get(i) != null && targetIsBeingRepaired) _strengths.set(i, Mathf.lerpDelta(_strengths.get(i), 1f, 0.08f * Time.delta));
            else _strengths.set(i, Mathf.lerpDelta(_strengths.get(i), 0f, 0.07f * Time.delta));
        }

        public void mountTargetTurn(int mount, float target){
            float speed = mounts.get(mount).rotateSpeed * delta();
            if(mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.power || mounts.get(mount).powerUse > 0.001f) speed *= Mathf.clamp(power.graph.getPowerBalance()/mounts.get(mount).powerUse, 0, 1);
            else speed *= baseReloadSpeed();
            _rotations.set(mount, Angles.moveToward(_rotations.get(mount), target, speed));
        }

        public Posc mountTargetFind(int mount){
            float[] loc = this.mountLocations(mount);

            if(mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.liquid && mounts.get(mount).extinguish && liquids.current().canExtinguish()) {
                int tr = (int) (mounts.get(mount).range / tilesize);
                for(int x = -tr; x <= tr; x++) for(int y = -tr; y <= tr; y++) {
                    Tile other = world.tileWorld(x + (int)loc[4]/8f, y + (int)loc[5]/8f);
                    //do not extinguish fires on other team blocks
                    if (other != null && Fires.has(x + (int)loc[4]/8, y + (int)loc[5]/8) && (other.build == null || other.team() == team)) return Fires.get(x + (int)loc[4]/8, y + (int)loc[5]/8);
                }
            }

            if(mounts.get(mount).healBlock && Units.findAllyTile(team, loc[0], loc[1], mounts.get(mount).range, Building::damaged) != null) return Units.findAllyTile(team, loc[0], loc[1], mounts.get(mount).range, Building::damaged);
            else if(mounts.get(mount).targetAir && !mounts.get(mount).targetGround)
                return Units.bestEnemy(this.team, loc[0], loc[1], mounts.get(mount).range, e -> !e.dead && !e.isGrounded(), mounts.get(mount).unitSort);
            else return Units.bestTarget(this.team, loc[0], loc[1], mounts.get(mount).range, e -> !e.dead && (e.isGrounded() || mounts.get(mount).targetAir) && (!e.isGrounded() || mounts.get(mount).targetGround), b -> true, mounts.get(mount).unitSort);
        }

        public boolean mountTargetValid(int mount){
            float[] loc = mountLocations(mount);
            if(mounts.get(mount).healBlock && Units.findAllyTile(team, loc[0], loc[1], mounts.get(mount).range, Building::damaged) != null) return (_targets != null && !(_targets instanceof Teamc && ((Teamc) _targets).team() != team) && !(_targets instanceof Healthc && !((Healthc) _targets).isValid())) || this.isControlled() || logicControlled();

            return !Units.invalidateTarget(_targets.get(mount), team, loc[0], loc[1]) || isControlled() || logicControlled();
        }

        public void mountTargetPosition(int mount, Posc pos, float x, float y){
            if(!mountHasAmmo(mount) || pos == null) return;

            BulletType bullet = mountPeekAmmo(mount);
            float speed = bullet.speed;

            if(speed < 0.1) speed = 9999999;

            _targetPoses.get(mount).set(Predict.intercept(Tmp.v4.set(x, y), pos, speed));


            if(_targetPoses.get(mount).isZero()) _targetPoses.get(mount).set(_targets.get(mount));
        }

        public void mountUpdateShooting(int mount){
            if(_reloads.get(mount) >= mounts.get(mount).reload){
                mountShoot(mount, mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.liquid ? liquidMountAmmoTypes.get(mount).get(liquids.current()) : mountPeekAmmo(mount));
                _reloads.set(mount, 0f);
            }else {
                float speed = delta() * mountPeekAmmo(mount).reloadMultiplier;
                if(mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.power || mounts.get(mount).powerUse > 0.001f) speed *= Mathf.clamp(power.graph.getPowerBalance()/mounts.get(mount).powerUse, 0, 1);
                else speed *= baseReloadSpeed();
                if(speed >= 0.001f) _reloads.set(mount, _reloads.get(mount) + speed);
            }
        }

        public void mountShoot(int mount, BulletType type){
            for(int j = 0; j < mounts.get(mount).shots; j++) {
                int spreadAmount = j;
                float[] loc = mountLocations(mount);
                if (mounts.get(mount).chargeTime >= 0.001f) {
                    mountUseAmmo(mount);

                    mounts.get(mount).chargeBeginEffect.at(loc[4], loc[5], _rotations.get(mount));
                    mounts.get(mount).chargeSound.at(loc[4], loc[5], 1);

                    for (int i = 0; i < mounts.get(mount).chargeEffects; i++) {
                        Time.run(Mathf.random(mounts.get(mount).chargeMaxDelay), () -> {
                            if(!isValid()) return;

                            mounts.get(mount).chargeEffect.at(loc[4], loc[5], _rotations.get(mount));
                        });
                    }

                    _chargings.set(mount, true);

                    Time.run(mounts.get(mount).chargeTime, () -> {
                        if (!isValid()) return;

                        _recoils.set(mount, mounts.get(mount).recoilAmount);
                        _heats.set(mount, 1f);
                        mountBullet(mount, type, _rotations.get(mount) + Mathf.range(mounts.get(mount).inaccuracy));
                        mountEffect(mount, type);
                        _chargings.set(mount, false);
                    });
                }
                else {
                    Time.run(mounts.get(mount).burstSpacing * j, () -> {
                        if (!isValid() || !mountHasAmmo(mount)) return;

                        if (mounts.get(mount).loopSound != Sounds.none) loopSounds.get(mount).update(loc[4], loc[5], true);
                        if (mounts.get(mount).sequential) _shotCounters.set(mount, _shotCounters.get(mount) + 1);

                        mountBullet(mount, type, spreadAmount);
                        mountEffect(mount, type);
                        mountUseAmmo(mount);
                        _recoils.set(mount, mounts.get(mount).recoilAmount);
                        _heats.set(mount, 1f);
                    });
                }
            }
        }

        public void mountEffect(int mount, BulletType type){
            float[] loc = mountLocations(mount);

            (mounts.get(mount).shootEffect == Fx.none ? type.shootEffect : mounts.get(mount).shootEffect).at(loc[4], loc[5], _rotations.get(mount));
            (mounts.get(mount).smokeEffect == Fx.none ? type.smokeEffect : mounts.get(mount).smokeEffect).at(loc[4], loc[5], _rotations.get(mount));
            mounts.get(mount).shootSound.at(loc[4], loc[5], Mathf.random(0.9f, 1.1f));
            if(mounts.get(mount).shootShake > 0) Effect.shake(mounts.get(mount).shootShake, mounts.get(mount).shootShake, loc[4], loc[(int) y]);
            _recoils.set(mount ,mounts.get(mount).recoilAmount);
        }

        public void mountEjectEffects(int mount){
            if(!isValid()) return;

            int side = mounts.get(mount).altEject ? Mathf.signs[(int) (_shotCounters.get(mount) % 2)] : mounts.get(mount).ejectRight ? 1 : 0;
            float[] loc = mountLocations(mount);

            mounts.get(mount).ejectEffect.at(loc[4], loc[5], _rotations.get(mount) * side);
        }

        public void mountBullet(int mount, BulletType type, float spreadAmount){
            float[] loc = mountLocations(mount);

            float lifeScl = type.scaleLife ? Mathf.clamp(Mathf.dst(loc[4], loc[5], _targetPoses.get(mount).x, _targetPoses.get(mount).y) / type.range, mounts.get(mount).minRange / type.range, mounts.get(mount).range / type.range) : 1;
            float angle = _rotations.get(mount) + Mathf.range(mounts.get(mount).inaccuracy + type.inaccuracy) + (spreadAmount - (mounts.get(mount).shots / 2f)) * mounts.get(mount).spread;
            type.create(this, this.team, loc[4], loc[5], angle, 1 + Mathf.range(mounts.get(mount).velocityInaccuracy), lifeScl);
        }

        public BulletType mountPeekAmmo(int mount){
            if(mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.power) return mounts.get(mount).bullet;
            if(mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.item) return _ammos.get(mount).peek().types(mount);
            if(mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.liquid) return liquidMountAmmoTypes.get(mount).get(liquids.current());

            return null;
        }

        public BulletType mountUseAmmo(int mount){
            if(mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.power) return mounts.get(mount).bullet;
            if(cheating()){
                if(mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.item) return mountPeekAmmo(mount);
                if(mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.liquid) return liquidMountAmmoTypes.get(mount).get(liquids.current());
            }

            if(mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.item){
                BattleCore.ItemEntry entry = _ammos.get(mount).peek();
                entry.amount -= mounts.get(mount).ammoPerShot;
                if(entry.amount <= 0) _ammos.get(mount).pop();

                float totalAmmos = _totalAmmos.get(mount);
                totalAmmos -= mounts.get(mount).ammoPerShot;
                totalAmmos = Math.max(totalAmmos, 0);
                _totalAmmos.set(mount, (int) totalAmmos);

                mountEjectEffects(mount);
                return entry.types(mount);
            }

            return null;
        }

        public boolean mountHasAmmo(int mount){
            if(mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.power
                    || mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.tract
                    || mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.point
                    || mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.repair
                    || mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.mass
                    || mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.drill) return true;
            if(mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.item) {
                if (_ammos.get(mount).size >= 2 && _ammos.get(mount).peek().amount < mounts.get(mount).ammoPerShot) _ammos.get(mount).pop();
                return _ammos.get(mount).size > 0 && _ammos.get(mount).peek().amount >= mounts.get(mount).ammoPerShot;
            }
            if(mounts.get(mount).mountType == MountTurretType.MultiTurretMountType.liquid) {
                return liquidMountAmmoTypes.get(mount) != null
                        && liquidMountAmmoTypes.get(mount).get(liquids.current()) != null
                        && liquids.currentAmount() >= 1f / liquidMountAmmoTypes.get(mount).get(liquids.current()).ammoMultiplier;
            }
            return false;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.s(outputItem == null ? -1 : outputItem.id);
            write.i(outputAmount);
            for(int i = 0; i < content.items().size; i++){
                write.i(output.get(i).amount);
            }

            for(int i = 0; i < mounts.size; i++) {
                try{
                    write.f(_reloads.get(i));
                    write.f(_rotations.get(i));
                } catch(Throwable e){
                    Log.warn(String.valueOf(e));
                }

                if(mounts.get(i).mountType == MountTurretType.MultiTurretMountType.mass) {
                    write.i(link);
                    write.b((byte) massState.ordinal());
                }

                if(mounts.get(i).mountType != MountTurretType.MultiTurretMountType.item) continue;

                write.b(_ammos.get(i).size);
                for (BattleCore.ItemEntry entry : _ammos.get(i)) {
                    write.s(entry.item.id);
                    write.s(entry.amount);
                }
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            outputItem = content.item(read.s());
            outputAmount = read.i();
            for(int i = 0; i < content.items().size; i++){
                output.get(i).amount = read.i();
            }

            for(int h = 0; h < mounts.size; h++) {
                try{
                    _reloads.set(h, read.f());
                    _rotations.set(h, read.f());
                } catch(Throwable e){
                    Log.warn(String.valueOf(e));
                }

                if(mounts.get(h).mountType == MountTurretType.MultiTurretMountType.mass) {
                    link = read.i();
                    massState = MassDriver.DriverState.all[read.b()];
                }

                if(mounts.get(h).mountType != MountTurretType.MultiTurretMountType.item) continue;

                int amount = read.ub();
                for(int i = 0; i < amount; i++) {
                    Item item = content.item(revision < 2 ? read.ub() : read.s());
                    short a = read.s();
                    _totalAmmos.set(h, _totalAmmos.get(h) + a);

                    if(item != null && mountAmmoTypes.get(h) != null && mountAmmoTypes.get(h).containsKey(item)) _ammos.get(h).add(new ItemEntry(item, a));
                }
            }
        }

        @Override
        public byte version(){
            return 2;
        }
    }
    public class ItemEntry {
        protected Item item;
        public int amount;

        ItemEntry(Item item, int amount){
            this.item = item;
            this.amount = amount;
        }

        public BulletType types(int i){
            return mountAmmoTypes.get(i).get(item);
        }
    }
}