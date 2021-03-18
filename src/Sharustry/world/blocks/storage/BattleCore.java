package Sharustry.world.blocks.storage;

import Sharustry.graphics.Drawm;
import Sharustry.world.blocks.defense.MultiTurretMount;
import arc.*;
import arc.func.Func;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.Vec2;
import arc.scene.ui.Image;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.audio.*;
import mindustry.content.*;
import mindustry.core.World;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.LAccess;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

import static arc.struct.ObjectMap.of;

public class BattleCore extends CoreBlock {
    public Block reviveBlock = Blocks.coreNucleus;

    public final static float logicControlCooldown = 60 * 2;

    public float healHealth = 0.4f;
    public boolean customMountLocation = false;
    public Seq<Float> customMountLocationsX = new Seq<>();
    public Seq<Float> customMountLocationsY = new Seq<>();
    public float rangeTime = 80;
    public float fadeTime = 20;
    public Seq<MultiTurretMount> mounts = new Seq<>();
    public int amount;
    public float totalRangeTime;
    public Seq<SoundLoop> loopSounds = new Seq<>();

    public Seq<TextureRegion[]> turrets = new Seq<>();
    public ObjectMap<Liquid, BulletType> liquidAmmoTypes = new ObjectMap<>();
    public Seq<ObjectMap<Liquid, BulletType>> liquidMountAmmoTypes = new Seq<>();
    public Seq<ObjectMap<Item, BulletType>> mountAmmoTypes = new Seq<>();

    public TextureRegion heatRegion, iconRegion;

    public BattleCore(String name, MultiTurretMount... mounts){
        this(name);
        addMountTurret(mounts);
    }

    public BattleCore(String name){
        super(name);
    }

    public void addMountTurret(MultiTurretMount... mounts){
        for(MultiTurretMount mount : mounts) this.mounts.add(mount);
        this.amount = this.mounts.size;
        this.totalRangeTime = rangeTime * this.mounts.size;
    }

    public void addCustomMountLocation(Float[] xy){
        customMountLocation = true;
        for(int ix = 0; ix < amount * 2; ix += 2) customMountLocationsX.add(xy[ix]);
        for(int iy = 1; iy < amount * 2; iy += 2) customMountLocationsY.add(xy[iy]);
    }

    public void ammos(MultiTurretMount.MultiTurretMountType ammotype, Object... objects){
        if(ammotype == MultiTurretMount.MultiTurretMountType.item) {
            mountAmmoTypes.add(OrderedMap.of(objects));
            liquidMountAmmoTypes.add(null);
        }
        if(ammotype == MultiTurretMount.MultiTurretMountType.liquid) {
            mountAmmoTypes.add(null);
            liquidMountAmmoTypes.add(OrderedMap.of(objects));
        }
        if(ammotype == MultiTurretMount.MultiTurretMountType.power
                || ammotype == MultiTurretMount.MultiTurretMountType.tract
                || ammotype == MultiTurretMount.MultiTurretMountType.point
                || ammotype == MultiTurretMount.MultiTurretMountType.repair){
            liquidMountAmmoTypes.add(null);
            mountAmmoTypes.add(null);
        }
    }

    @Override
    public void init(){
        consumes.add(new ConsumeLiquidFilter(i -> liquidAmmoTypes.containsKey(i), 1f){
            @Override
            public boolean valid(Building entity){
                return entity.liquids.total() > 0.001f;
            }

            @Override
            public void update(Building entity){

            }

            @Override
            public void display(Stats stats){

            }
        });

        super.init();
    }

    @Override
    public void load(){
        super.load();
        region = Core.atlas.find(name);
        iconRegion = Drawm.generateTeamRegion(this);
        teamRegion = Core.atlas.find(name + "-team");
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
            if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.repair){
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
        }
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{
                region,
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
            if(!(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.point)) {
                rowAdd(h, "[lightgray]" + Stat.targetsAir.localized() + ": [white]" + (!(mounts.get(i).targetAir) ? Core.bundle.get("no") : Core.bundle.get("yes")));
                rowAdd(h, "[lightgray]" + Stat.targetsGround.localized() + ": [white]" + (!(mounts.get(i).targetGround) ? Core.bundle.get("no") : Core.bundle.get("yes")));
            }else rowAdd(h, "[lightgray]" + Core.bundle.format("stat.shar.targetsBullet") + ": [white]" + Core.bundle.get("yes"));
            if(mounts.get(i).inaccuracy > 0) rowAdd(h, "[lightgray]" + Stat.inaccuracy.localized() + ": [white]" + (mounts.get(i).inaccuracy) + " " + StatUnit.degrees.localized());
            if(mounts.get(i).chargeTime > 0.001f) rowAdd(h, "[lightgray]" + Core.bundle.get("stat.shar.chargeTime") + ": [white]" + Mathf.round(mounts.get(i).chargeTime/60, 100) + " " + Core.bundle.format("stat.shar.seconds"));
            if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.item) rowAdd(h, "[lightgray]" + Core.bundle.get("stat.shar.ammo-shot") + ": [white]" + (mounts.get(i).ammoPerShot));
            if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.tract) rowAdd(h, "[lightgray]" + Stat.damage.localized() + ": [white]" + Core.bundle.format("stat.shar.damage", mounts.get(i).damage * 60f));
            if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.repair) rowAdd(h, "[lightgray]" + Stat.range.localized() + ": [white]" + Core.bundle.format("stat.shar.range", mounts.get(i).repairRadius / tilesize));
            if(mounts.get(i).mountType != MultiTurretMount.MultiTurretMountType.repair || mounts.get(i).mountType != MultiTurretMount.MultiTurretMountType.tract) rowAdd(h, "[lightgray]" + Stat.reload.localized() + ": [white]" + Strings.autoFixed(60 / (mounts.get(i).reloadTime) * (mounts.get(i).shots), 1));

            h.row();

            ObjectMap<ObjectMap<BulletType ,? extends UnlockableContent>, TextureRegion> types = new ObjectMap<>();
            //tract does put nothing lol
            if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.power) {
                BulletType bullet = mounts.get(i).bullet;
                if(bullet != null) types.put(of(bullet, null), Icon.power.getRegion());
            }

            if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.item){
                for(Item item : content.items()){
                    BulletType bullet = mountAmmoTypes.get(i).get(item);
                    if(bullet != null) types.put(of(bullet, item), item.icon(Cicon.medium));
                }
            }

            if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.liquid){
                for(Liquid liquid : content.liquids()) {
                    BulletType bullet = liquidMountAmmoTypes.get(i).get(liquid);
                    if(bullet != null) types.put(of(bullet, liquid), liquid.icon(Cicon.medium));
                }
            }

            h.table(b -> {
                if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.tract
                        || mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.point
                        || mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.repair){
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
                            if(bullet.ammoMultiplier > 0 && (mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.item) && !Mathf.equal(bullet.ammoMultiplier, 1f)) rowAdd(e, Core.bundle.format("bullet.multiplier", Strings.fixed(bullet.ammoMultiplier, 1)));
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
            table.left();
            table.add("[lightgray]" + Core.bundle.get("stat.shar.mini-t")).fillX().padLeft(24);

            //Mounts
            table.table(null, w -> {
                for(int i = 0; i < mounts.size; i++){
                    addStatS(w,  i);
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

    public class BattleCoreBuild extends CoreBuild {
        public Seq<Integer> _shotcounters = new Seq<>();
        public Seq<Integer> _totalAmmos = new Seq<>();
        public Seq<Float> _reloads = new Seq<>();
        public Seq<Float> _heats = new Seq<>();
        public Seq<Float> _recoils = new Seq<>();
        public Seq<Float> _shotCounters = new Seq<>();
        public Seq<Float> _rotations = new Seq<>();
        public Seq<Float> _lastXs = new Seq<>();
        public Seq<Float> _lastYs = new Seq<>();
        public Seq<Float> _strengths = new Seq<>();
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
        public float logicControlTime = -1;
        public boolean logicShooting = false;

        @Override
        public void remove(){
            super.remove();
            if(sound != null) sound.stop();
        }

        @Override
        public void created(){
            super.created();
            for(int i = 0; i < mounts.size; i++){
                _shotcounters.add(0);
                _totalAmmos.add(0);
                _reloads.add(0f);
                _heats.add(0f);
                _recoils.add(0f);
                _shotCounters.add(0f);
                _rotations.add(90f);
                _targets.add(null);
                _tractTargets.add(null);
                _pointTargets.add(null);
                _repairTargets.add(null);
                _healTargets.add(null);
                _targetPoses.add(new Vec2());
                _wasShootings.add(false);
                _chargings.add(false);
                _ammos.add(new Seq<>());
                _lastXs.add(0f);
                _lastYs.add(0f);
                _strengths.add(0f);
                _anys.add(false);
            }
        }
        public void tf(Table table, int i){
            if(mounts.size > 3 && i % 3 == 0) table.row();
            table.add(new Stack(){{
                add(new Table(o -> {
                    o.left();
                    o.add(new Image(Core.atlas.find("shar-" + mounts.get(i).name + "-full")));
                }));

                add(new Table(h -> {
                    if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.item) {
                        MultiReqImage itemReq = new MultiReqImage();

                        for(Item item : mountAmmoTypes.get(i).keys()) itemReq.add(new ReqImage(item.icon(Cicon.tiny), () -> mountHasAmmo(i)));

                        h.add(new Stack(){{
                            add(new Table(e -> {
                                e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*2f);
                                Bar itemBar = mountHasAmmo(i) ? new Bar("", _ammos.get(i).peek().item.color, () -> _totalAmmos.get(i) / mounts.get(i).maxAmmo) : new Bar("", new Color(0.1f, 0.1f, 0.1f, 1), () -> 0);
                                e.add(itemBar);
                                e.pack();
                            }));

                            add(mountHasAmmo(i) ? new Table(e -> e.add(new ItemImage(_ammos.get(i).peek().item.icon(Cicon.tiny)))) : new Table(e -> e.add(itemReq).size(Cicon.tiny.size)));
                        }}).padTop(2*8).padLeft(2*8);

                    }

                    if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.liquid) {
                        MultiReqImage liquidReq = new MultiReqImage();

                        for(Liquid liquid : liquidMountAmmoTypes.get(i).keys()) liquidReq.add(new ReqImage(liquid.icon(Cicon.tiny), () -> mountHasAmmo(i)));

                        h.add(new Stack(){{
                            add(new Table(e -> {
                                e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*2f);
                                Bar liquidBar = mountHasAmmo(i) ? new Bar("", liquids.current().color, () -> liquids.get(liquids.current()) / liquidCapacity) : new Bar("", new Color(0.1f, 0.1f, 0.1f, 1), () -> 0);
                                e.add(liquidBar);
                                e.pack();
                            }));
                            add(mountHasAmmo(i) ? new Table(e -> e.add(new ItemImage(liquids.current().icon(Cicon.tiny)))) : new Table(e -> e.add(liquidReq).size(Cicon.tiny.size)));
                        }}).padTop(2*8).padLeft(2*8);

                    }

                    if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.power
                            || mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.tract
                            || mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.point
                            || mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.repair) {
                        MultiReqImage powerReq = new MultiReqImage();

                        powerReq.add(new ReqImage(Icon.powerSmall.getRegion(), () -> Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1) >= 0.001f));
                        h.add(new Stack(){{
                            add(new Table(e -> {
                                e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*2f);
                                Bar liquidBar = new Bar("", Pal.powerBar, () -> Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1));
                                e.add(liquidBar);
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
        public void displayConsumption(Table table){
            for(int i = 0; i < mounts.size; i++){
                table.center();
                tf(table, i);
            }
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
            bars.row();
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            for(int i = 0; i < mounts.size; i++){
                float fade = Mathf.curve(Time.time % totalRangeTime, rangeTime * i, rangeTime * i + fadeTime) - Mathf.curve(Time.time % totalRangeTime, rangeTime * (i + 1) - fadeTime, rangeTime * (i + 1));
                float[] loc = mountLocations(i);
                Lines.stroke(3, Pal.gray);
                Draw.alpha(fade);

                if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.repair){
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
        public int removeStack(Item item, int amount){
            return super.removeStack(item, amount);
        }

        @Override
        public void handleItem(Building source, Item item){
            for(int h = 0; h < mounts.size; h++) {
                if(mountAmmoTypes.get(h) == null) continue;

                if (item == Items.pyratite) Events.fire(EventType.Trigger.flameAmmo);

                BulletType type = mountAmmoTypes.get(h).get(item);
                if(type == null) continue;
                if(_totalAmmos.get(h) + type.ammoMultiplier > mounts.get(h).maxAmmo) continue;
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

                if(asdf) _ammos.get(h).add(new BattleCore.ItemEntry(item, (int)type.ammoMultiplier < mounts.get(h).ammoPerShot ? (int)type.ammoMultiplier + mounts.get(h).ammoPerShot : (int)type.ammoMultiplier));
            }
            super.handleItem(source, item);
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            for(int i = 0; i < mounts.size; i++)
                if(mountAmmoTypes.get(i) != null && mountAmmoTypes.get(i).get(item) != null)
                    return Math.min((int)((mounts.get(i).maxAmmo - _totalAmmos.get(i)) / mountAmmoTypes.get(i).get(item).ammoMultiplier), amount);
            return 0;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            for(int i = 0; i < mounts.size; i++)
                if(mountAmmoTypes.get(i) != null
                        && mountAmmoTypes.get(i).get(item) != null
                        &&_totalAmmos.get(i) + mountAmmoTypes.get(i).get(item).ammoMultiplier <= mounts.get(i).maxAmmo) return true;
            return super.acceptItem(source, item);
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
                Draw.z(Layer.turret);
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

                if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.tract && _anys.get(i)){
                    Draw.z(Layer.bullet);
                    float ang = angleTo(_lastXs.get(i), _lastYs.get(i));

                    Draw.mixcol(mounts.get(i).laserColor, Mathf.absin(4f, 0.6f));

                    Drawf.laser(team, mounts.get(i).laser, mounts.get(i).laserEnd,
                            x + Angles.trnsx(ang, mounts.get(i).shootLength), y + Angles.trnsy(ang, mounts.get(i).shootLength),
                            _lastXs.get(i), _lastYs.get(i), _strengths.get(i) * Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1) * mounts.get(i).laserWidth);

                    Draw.mixcol();
                    Draw.reset();
                }
                if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.repair
                        && _repairTargets.get(i) != null
                        && Angles.angleDist(angleTo(_repairTargets.get(i)), _rotations.get(i)) < 30f){
                    Draw.z(Layer.flyingUnit + 1); //above all units
                    float ang = angleTo(_repairTargets.get(i));
                    float len = 5f;

                    Draw.color(mounts.get(i).laserColor);
                    Drawf.laser(team, mounts.get(i).laser, mounts.get(i).laserEnd,
                            loc[0] + Angles.trnsx(ang, len), loc[1] + Angles.trnsy(ang, len),
                            _repairTargets.get(i).x(), _repairTargets.get(i).y(), _strengths.get(i));
                    Draw.color();
                    Draw.reset();
                }
            }

            Draw.reset();
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
        public void control(LAccess type, double p1, double p2, double p3, double p4){
            if(type == LAccess.shoot && !unit.isPlayer()){
                for(int i = 0; i < mounts.size; i++) _targetPoses.get(i).set(World.unconv((float)p1), World.unconv((float)p2));
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

        @Override
        public void drawTeam(){
            //why
        }

        @Override
        public void updateTile() {
            for(int i = 0; i < mounts.size; i++){
                unit.ammo((float)unit.type().ammoCapacity * _totalAmmos.get(i) /  mounts.get(i).maxAmmo);
                unit.ammo(unit.type().ammoCapacity * liquids.currentAmount() / liquidCapacity);
                unit.ammo(power.status * unit.type().ammoCapacity);
            }

            unit.health(health);
            unit.rotation(rotation);
            unit.team(team);
            unit.set(x, y);

            super.updateTile();

            if(logicControlTime > 0) logicControlTime -= Time.delta;

            float maxUsed = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount / mounts.size;

            Liquid liquid = liquids.current();

            for(int i = 0; i < mounts.size; i++){
                if(!(mounts.get(i).acceptCooling) || liquidMountAmmoTypes.get(i) == null) continue;
                float used = Math.min(Math.min(liquids.get(liquid), maxUsed * Time.delta), Math.max(0, ((mounts.get(i).reloadTime - _reloads.get(i)) / mounts.get(i).coolantMultiplier) / liquid.heatCapacity));
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

                if(!validateMountTarget(i)) {
                    _targets.set(i, null);
                    _tractTargets.set(i, null);
                    _pointTargets.set(i, null);
                    _healTargets.set(i, null);
                }
            }

            __heat -= edelta();

            if(__heat <= 0.001){
                for(int i = 0; i < mounts.size; i++){
                    float[] loc = this.mountLocations(i);
                    if(mountHasAmmo(i)) {
                        this.mountLocations(i);
                        _targets.set(i, findMountTargets(i));
                        _healTargets.set(i, Units.findAllyTile(team, loc[0], loc[1], mounts.get(i).range, b -> b.damaged() && b != this));

                        if(mounts.get(i).healBlock && Units.findAllyTile(team, loc[0], loc[1], mounts.get(i).range, b -> damaged() && b.health <= b.block.health * healHealth) != null) _targets.set(_healTargets.copy());
                        final int j = i;
                        _tractTargets.set(i, Units.closestEnemy(this.team, loc[0], loc[1], mounts.get(i).range, u -> u.checkTarget(mounts.get(j).targetAir, mounts.get(j).targetGround)));
                        _pointTargets.set(i, Groups.bullet.intersect(loc[0] - mounts.get(i).range, loc[1] - mounts.get(i).range, mounts.get(i).range * 2, mounts.get(i).range * 2).min(b -> b.team != team && b.type().hittable, b -> b.dst2(new Vec2(loc[0], loc[1]))));

                        MultiTurretMount.rect.setSize(mounts.get(i).repairRadius * 2).setCenter(loc[0], loc[1]);
                        _repairTargets.set(i, Units.closest(team, loc[0], loc[1], mounts.get(i).repairRadius, Unit::damaged));
                    }
                }
                __heat = 16;
            }

            for(int i = 0; i < mounts.size; i++){
                float[] loc = this.mountLocations(i);
                if(mountHasAmmo(i)) {
                    if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.tract){
                        _anys.set(i, false);

                        //look at target
                        if(_tractTargets.get(i) != null
                                && _tractTargets.get(i).within(new Vec2(loc[0], loc[1]), mounts.get(i).range + _tractTargets.get(i).hitSize/2f)
                                && _tractTargets.get(i).team() != team
                                && _tractTargets.get(i).checkTarget(mounts.get(i).targetAir, mounts.get(i).targetGround)
                                && Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1) > 0.02f){
                            if(!headless) control.sound.loop(mounts.get(i).shootSound, new Vec2(loc[0], loc[1]), mounts.get(i).shootSoundVolume);

                            float dest = angleTo(_tractTargets.get(i));
                            mountTurnToTarget(i, dest);
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
                        }else _strengths.set(i, Mathf.lerpDelta(_strengths.get(i), 0, 0.1f));
                    }
                    else if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.point){
                        if(_pointTargets.get(i) != null
                                && _pointTargets.get(i).within(new Vec2(loc[0], loc[1]), mounts.get(i).range)
                                && _pointTargets.get(i).team != team
                                && _pointTargets.get(i).type() != null
                                && _pointTargets.get(i).type().hittable){
                            float dest = angleTo(_pointTargets.get(i));
                            mountTurnToTarget(i, dest);
                            _reloads.set(i, _reloads.get(i) + delta() * Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1));

                            //shoot when possible
                            if(Angles.within(_rotations.get(i), dest, mounts.get(i).shootCone) && _reloads.get(i) >= mounts.get(i).reloadTime){
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
                    else if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.repair){
                        boolean targetIsBeingRepaired = false;
                        if(_repairTargets.get(i) != null){
                            if(_repairTargets.get(i).dead()
                                    || _repairTargets.get(i).dst(loc[0], loc[1]) - _repairTargets.get(i).hitSize / 2f > mounts.get(i).repairRadius
                                    || _repairTargets.get(i).health() >= _repairTargets.get(i).maxHealth()) _repairTargets.set(i, null);
                            else {
                                _repairTargets.get(i).heal(mounts.get(i).repairSpeed * Time.delta * _strengths.get(i) * Mathf.clamp(power.graph.getPowerBalance() / mounts.get(i).powerUse, 0, 1));
                                float dest = angleTo(_repairTargets.get(i));
                                mountTurnToTarget(i, dest);
                                targetIsBeingRepaired = true;
                            }
                        }

                        if(_repairTargets.get(i) != null && targetIsBeingRepaired) _strengths.set(i, Mathf.lerpDelta(_strengths.get(i), 1f, 0.08f * Time.delta));
                        else _strengths.set(i, Mathf.lerpDelta(_strengths.get(i), 0f, 0.07f * Time.delta));
                    }
                    else if(validateMountTarget(i)) {
                        boolean canShoot = true;

                        //default AI behavior
                        if(this.logicControlled()) { //logic behavior
                            canShoot = logicShooting;
                        }else{
                            mountTargetPosition(i, _targets.get(i), loc[0], loc[1]);
                            if(Float.isNaN(_rotations.get(i))) _rotations.set(i, 0f);
                        }
                        float targetRot = Angles.angle(loc[0], loc[1], _targetPoses.get(i).x, _targetPoses.get(i).y);

                        if(!_chargings.get(i)) mountTurnToTarget(i, targetRot);

                        if (Angles.angleDist(_rotations.get(i), targetRot) < mounts.get(i).shootCone && canShoot) {
                            _wasShootings.set(i, true);
                            updateMountShooting(i);
                        }
                    }
                }
            }

        }

        public void mountTurnToTarget(int mount, float target){
            float speed = mounts.get(mount).rotateSpeed * delta();
            if(mounts.get(mount).mountType == MultiTurretMount.MultiTurretMountType.power || mounts.get(mount).powerUse > 0.001f) speed *= Mathf.clamp(power.graph.getPowerBalance()/mounts.get(mount).powerUse, 0, 1);
            else speed *= baseReloadSpeed();
            _rotations.set(mount, Angles.moveToward(_rotations.get(mount), target, speed));
        }

        public Posc findMountTargets(int mount){
            float[] loc = this.mountLocations(mount);

            if(mounts.get(mount).mountType == MultiTurretMount.MultiTurretMountType.liquid && mounts.get(mount).extinguish && liquids.current().canExtinguish()) {
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

        public boolean validateMountTarget(int mount){
            float[] loc = mountLocations(mount);
            if(mounts.get(mount).healBlock && Units.findAllyTile(team, loc[0], loc[1], mounts.get(mount).range, Building::damaged) != null) return (_targets != null && !(_targets instanceof Teamc && ((Teamc) _targets).team() != team) && !(_targets instanceof Healthc && !((Healthc) _targets).isValid())) || logicControlled();

            return !Units.invalidateTarget(_targets.get(mount), team, loc[0], loc[1]) || logicControlled();
        }

        public void mountTargetPosition(int mount, Posc pos, float x, float y){
            if(!mountHasAmmo(mount) || pos == null) return;

            BulletType bullet = mountPeekAmmo(mount);
            float speed = bullet.speed;

            if(speed < 0.1) speed = 9999999;

            _targetPoses.get(mount).set(Predict.intercept(Tmp.v4.set(x, y), pos, speed));


            if(_targetPoses.get(mount).isZero()) _targetPoses.get(mount).set(_targets.get(mount));
        }

        public void updateMountShooting(int mount){
            if(_reloads.get(mount) >= mounts.get(mount).reloadTime){
                mountShoot(mount, mounts.get(mount).mountType == MultiTurretMount.MultiTurretMountType.liquid ? liquidMountAmmoTypes.get(mount).get(liquids.current()) : mountPeekAmmo(mount));
                _reloads.set(mount, 0f);
            }else {
                float speed = delta() * mountPeekAmmo(mount).reloadMultiplier;
                if(mounts.get(mount).mountType == MultiTurretMount.MultiTurretMountType.power || mounts.get(mount).powerUse > 0.001f) speed *= Mathf.clamp(power.graph.getPowerBalance()/mounts.get(mount).powerUse, 0, 1);
                else speed *= baseReloadSpeed();
                if(speed >= 0.001f) _reloads.set(mount, _reloads.get(mount) + speed);
            }
        }

        protected float baseReloadSpeed(){
            return efficiency();
        }

        public void mountEffect(int mount, BulletType type){
            float[] loc = mountLocations(mount);

            (mounts.get(mount).shootEffect == Fx.none ? type.shootEffect : mounts.get(mount).shootEffect).at(loc[4], loc[5], _rotations.get(mount));
            (mounts.get(mount).smokeEffect == Fx.none ? type.smokeEffect : mounts.get(mount).smokeEffect).at(loc[4], loc[5], _rotations.get(mount));
            mounts.get(mount).shootSound.at(loc[4], loc[5], Mathf.random(0.9f, 1.1f));
            if(mounts.get(mount).shootShake > 0) Effect.shake(mounts.get(mount).shootShake, mounts.get(mount).shootShake, loc[4], loc[(int) y]);
            _recoils.set(mount ,mounts.get(mount).recoilAmount);
        }

        public void mountBullet(int mount, BulletType type, float spreadAmount){
            float[] loc = mountLocations(mount);

            float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(loc[4], loc[5], _targetPoses.get(mount).x, _targetPoses.get(mount).y) / type.range(), mounts.get(mount).minRange / type.range(), mounts.get(mount).range / type.range()) : 1;
            float angle = _rotations.get(mount) + Mathf.range(mounts.get(mount).inaccuracy + type.inaccuracy) + (spreadAmount - (mounts.get(mount).shots / 2f)) * mounts.get(mount).spread;
            type.create(this, this.team, loc[4], loc[5], angle, 1 + Mathf.range(mounts.get(mount).velocityInaccuracy), lifeScl);

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

            if(!mounts.get(mount).sequential) _shotCounters.set(mount, _shotCounters.get(mount)+1);

            if(!mounts.get(mount).sequential) _shotcounters.set(mount, _shotcounters.get(mount)+1);
            for(int i = 0; i < mounts.get(mount).skillDelays.size; i++) if(_shotcounters.get(mount) % mounts.get(mount).skillDelays.get(i) == 0) {
                _shotcounters.set(mount, 0);
                mounts.get(mount).skillSeq.get(i).get(this, mounts.get(mount)).run();
            }
        }

        public BulletType mountPeekAmmo(int mount){
            if(mounts.get(mount).mountType == MultiTurretMount.MultiTurretMountType.power) return mounts.get(mount).bullet;
            if(mounts.get(mount).mountType == MultiTurretMount.MultiTurretMountType.item) return _ammos.get(mount).peek().types(mount);
            if(mounts.get(mount).mountType == MultiTurretMount.MultiTurretMountType.liquid) return liquidMountAmmoTypes.get(mount).get(liquids.current());

            return null;
        }

        public BulletType mountUseAmmo(int mount){
            if(mounts.get(mount).mountType == MultiTurretMount.MultiTurretMountType.power) return mounts.get(mount).bullet;
            if(cheating()){
                if(mounts.get(mount).mountType == MultiTurretMount.MultiTurretMountType.item) return mountPeekAmmo(mount);
                if(mounts.get(mount).mountType == MultiTurretMount.MultiTurretMountType.liquid) return liquidMountAmmoTypes.get(mount).get(liquids.current());
            }

            if(mounts.get(mount).mountType == MultiTurretMount.MultiTurretMountType.item){
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
            if(mounts.get(mount).mountType == MultiTurretMount.MultiTurretMountType.power
                    || mounts.get(mount).mountType == MultiTurretMount.MultiTurretMountType.tract
                    || mounts.get(mount).mountType == MultiTurretMount.MultiTurretMountType.point
                    || mounts.get(mount).mountType == MultiTurretMount.MultiTurretMountType.repair) return true;
            if(mounts.get(mount).mountType == MultiTurretMount.MultiTurretMountType.item) {
                if (_ammos.get(mount).size >= 2 && _ammos.get(mount).peek().amount < mounts.get(mount).ammoPerShot) _ammos.get(mount).pop();
                return _ammos.get(mount).size > 0 && _ammos.get(mount).peek().amount >= mounts.get(mount).ammoPerShot;
            }
            if(mounts.get(mount).mountType == MultiTurretMount.MultiTurretMountType.liquid) {
                return liquidMountAmmoTypes.get(mount) != null
                        && liquidMountAmmoTypes.get(mount).get(liquids.current()) != null
                        && liquids.total() >= 1f / liquidMountAmmoTypes.get(mount).get(liquids.current()).ammoMultiplier;
            }
            return false;
        }

        public void mountEjectEffects(int mount){
            if(!isValid()) return;

            int side = mounts.get(mount).altEject ? Mathf.signs[(int) (_shotCounters.get(mount) % 2)] : mounts.get(mount).ejectRight ? 1 : 0;
            float[] loc = mountLocations(mount);

            mounts.get(mount).ejectEffect.at(loc[4], loc[5], _rotations.get(mount) * side);
        }

        @Override
        public void write(Writes write){ //for some reason, w/r doesn't work well.
            super.write(write);

            for(int i = 0; i < mounts.size; i++) {
                try{
                    write.f(_reloads.get(i));
                    write.f(_rotations.get(i));
                } catch(Throwable e){
                    Log.warn(String.valueOf(e));
                }
                if(mounts.get(i).mountType != MultiTurretMount.MultiTurretMountType.item) continue;

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

            for(int h = 0; h < mounts.size; h++) {
                try{
                    _reloads.set(h, read.f());
                    _rotations.set(h, read.f());
                } catch(Throwable e){
                    Log.warn(String.valueOf(e));
                }
                if(mounts.get(h).mountType != MultiTurretMount.MultiTurretMountType.item) continue;
                int amount = read.ub();
                for(int i = 0; i < amount; i++) {
                    Item item = content.item(revision < 2 ? read.ub() : read.s());
                    short a = read.s();
                    _totalAmmos.set(i, _totalAmmos.get(i) + a);

                    //only add ammo if this is a valid ammo type //NO ADD ON EVERY MOUNT WHICH ITEM AMMO
                    if(item != null && mountAmmoTypes.get(h) != null && mountAmmoTypes.get(h).containsKey(item)) _ammos.get(i).add(new ItemEntry(item, a));
                }
            }
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
