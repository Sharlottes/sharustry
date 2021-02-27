package Sharustry.world.blocks.defense;

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
import mindustry.ctype.UnlockableContent;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

import static arc.struct.ObjectMap.*;
import static mindustry.Vars.*;

public class MultiTurret extends ItemTurret {
    public boolean customMountLocation = false;
    public Seq<Float> customMountLocationsX = new Seq<>();
    public Seq<Float> customMountLocationsY = new Seq<>();
    public float rangeTime = 80;
    public float fadeTime = 20;
    public String title;
    public Seq<MultiTurretMount> mounts = new Seq<>();
    public int amount;
    public float totalRangeTime;
    public Item ammoItem;
    public BulletType bullet;
    public Seq<SoundLoop> loopSounds = new Seq<>();

    public TextureRegion outline, baseTurret;
    public Seq<TextureRegion[]> turrets = new Seq<>();
    public ObjectMap<Liquid, BulletType> liquidAmmoTypes = new ObjectMap<>();
    public Seq<ObjectMap<Liquid, BulletType>> liquidMountAmmoTypes = new Seq<>();
    public Seq<ObjectMap<Item, BulletType>> mountAmmoTypes = new Seq<>();

    public MultiTurret(String name, Item ammoItem, BulletType mainBullet, String title, MultiTurretMount... mounts){
        this(name);
        addMountTurret(mounts);
        addBaseTurret(ammoItem, mainBullet, title);
    }

    //should call addBaseTurret() in contents
    public MultiTurret(String name, MultiTurretMount... mounts){
        this(name);
        addMountTurret(mounts);
    }

    //should call addMountTurret() in contents
    public MultiTurret(String name, Item ammoItem, BulletType mainBullet, String title){
        this(name);
        addBaseTurret(ammoItem, mainBullet, title);
    }

    //should call addBaseTurret() and addMountTurret() in contents
    public MultiTurret(String name){
        super(name);
    }

    public void addMountTurret(MultiTurretMount... mounts){
        for(MultiTurretMount mount : mounts) this.mounts.add(mount);
        this.amount = this.mounts.size;
        this.totalRangeTime = rangeTime * this.mounts.size;
    }

    public void addBaseTurret(Item ammoItem, BulletType mainBullet, String title){
        this.ammoItem = ammoItem;
        this.bullet = mainBullet;
        this.title = title;
        ammo(ammoItem, mainBullet);
    }

    public void addCustomMountLocation(Float[] xy){
        customMountLocation = true;
        for(int ix = 0; ix < amount * 2; ix += 2) customMountLocationsX.add(xy[ix]);
        for(int iy = 1; iy < amount * 2; iy += 2) customMountLocationsY.add(xy[iy]);
    }

    public void ammos(MultiTurretMount.MountAmmoType ammotype, Object... objects){
        if(ammotype == MultiTurretMount.MountAmmoType.item) {
            mountAmmoTypes.add(OrderedMap.of(objects));
            liquidMountAmmoTypes.add(null);
        }
        if(ammotype == MultiTurretMount.MountAmmoType.liquid) {
            mountAmmoTypes.add(null);
            liquidMountAmmoTypes.add(OrderedMap.of(objects));
        }
        if(ammotype == MultiTurretMount.MountAmmoType.power){
            liquidMountAmmoTypes.add(null);
            mountAmmoTypes.add(null);
        }
    }

    public boolean iamdump(int h, Building tile, Item item){
        return (tile instanceof MultiTurretBuild) && !((MultiTurretBuild) tile)._ammos.get(h).isEmpty() && (((MultiTurretBuild) tile)._ammos.get(h).peek()).item == item;
    }

    public ReqImage wtfisthis(int h, Building tile, Item item){
        return new ReqImage(new ItemImage(item.icon(Cicon.medium)), ()->iamdump(h, tile, item));
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

        consumes.add(new ConsumeItemFilter(i -> ammoTypes.containsKey(i)){
            @Override
            public void build(Building tile, Table table){
                MultiReqImage image = new MultiReqImage();
                Vars.content.items().each(i -> filter.get(i) && i.unlockedNow(), item -> {
                    for(int h = 0; h < amount; h++) image.add(wtfisthis(h,tile,item));
                });

                table.add(image).size(8 * 4); //what the fucking hell have i done?
            }

            @Override
            public boolean valid(Building entity){
                //valid when there's any ammo in the turret
                return entity instanceof MultiTurretBuild && !((MultiTurretBuild) entity).ammo.isEmpty();
            }

            @Override
            public void display(Stats stats){
                //don't display
            }
        });

        super.init();
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
            for(int i = 0; i < amount; i++){ //...why?
                //[Sprite, Outline, Heat, Fade Mask]
                TextureRegion[] sprites = {
                    Core.atlas.find("shar-"+mounts.get(i).name +""),
                    Core.atlas.find("shar-"+mounts.get(i).name + "-outline"),
                    Core.atlas.find("shar-"+mounts.get(i).name + "-heat"),
                    Core.atlas.find("shar-"+mounts.get(i).name + "-mask")
                };
                turrets.add(sprites);
            }
        });
        for(int i = 0; i < mounts.size; i++) loopSounds.add(new SoundLoop(mounts.get(i).loopSound, mounts.get(i).loopVolume));
    }


    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        for(int i = 0; i < mounts.size; i++){
            float fade = Mathf.curve(Time.time % totalRangeTime, rangeTime * i, rangeTime * i + fadeTime) - Mathf.curve(Time.time % totalRangeTime, rangeTime * (i + 1) - fadeTime, rangeTime * (i + 1));
            float tX = x * tilesize + this.offset + (customMountLocation ? customMountLocationsX.get(i) : mounts.get(i).x);
            float tY = y * tilesize + this.offset + (customMountLocation ? customMountLocationsY.get(i) : mounts.get(i).y);

            Lines.stroke(3, Pal.gray);
            Draw.alpha(fade);
            Lines.dashCircle(tX, tY, mounts.get(i).range);
            Lines.stroke(1, Vars.player.team().color);
            Draw.alpha(fade);
            Lines.dashCircle(tX, tY, mounts.get(i).range);

            Draw.color(Vars.player.team().color, fade);
            Draw.rect(turrets.get(i)[3], tX, tY);
            Draw.reset();
        }
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{
            this.baseRegion,
            Core.atlas.find(this.name + "-icon")
        };
    }

    void rowAdd(Table h, String str){
        h.row();
        h.add(str);
    }

    void addStatS(Table w, boolean main, int i){
        w.left();
        w.row();
        w.add((main?title:mounts.get(i).title)).right().top();
        w.row();
        w.image(main?baseTurret:Core.atlas.find("shar-"+mounts.get(i).name + "-full")).size(60).scaling(Scaling.bounded).right().top();

        w.table(Tex.underline, h -> {
            h.left().defaults().padRight(3).left();
            if(main ? inaccuracy > 0 : mounts.get(i).inaccuracy > 0) h.add("[lightgray]" + Stat.inaccuracy.localized() + ": [white]" + (main?this.inaccuracy:mounts.get(i).inaccuracy) + " " + StatUnit.degrees.localized());
            if(main ? range > 0 : mounts.get(i).range > 0) rowAdd(h, "[lightgray]" + Stat.shootRange.localized() + ": [white]" + Strings.fixed((main?this.range:mounts.get(i).range) / tilesize, 1) + " " + StatUnit.blocks);

            rowAdd(h, "[lightgray]" + Core.bundle.get("stat.shar.ammo-shot") + ": [white]" + (main?ammoPerShot:mounts.get(i).ammoPerShot));
            rowAdd(h, "[lightgray]" + Stat.reload.localized() + ": [white]" + Strings.autoFixed(60 / (main?this.reloadTime:mounts.get(i).reloadTime) * (main?this.shots:mounts.get(i).shots), 1));
            rowAdd(h, "[lightgray]" + Stat.targetsAir.localized() + ": [white]" + (!(main?this.targetAir:mounts.get(i).targetAir) ? Core.bundle.get("no") : Core.bundle.get("yes")));
            rowAdd(h, "[lightgray]" + Stat.targetsGround.localized() + ": [white]" + (!(main?this.targetGround:mounts.get(i).targetGround) ? Core.bundle.get("no") : Core.bundle.get("yes")));

            if(main ? chargeTime > 0.001f : mounts.get(i).chargeTime > 0.001f) rowAdd(h, "[lightgray]" + Core.bundle.get("stat.shar.chargeTime") + ": [white]" + Mathf.round(main?chargeTime/60:mounts.get(i).chargeTime/60, 100) + " " + Core.bundle.format("stat.shar.seconds"));
            h.row();

            ObjectMap<ObjectMap<BulletType ,? extends UnlockableContent>, TextureRegion> types = new ObjectMap<>();
            if(main) types.put(of(bullet, ammoItem), ammoItem.icon(Cicon.medium));
            else {
                if(mounts.get(i).ammoType == MultiTurretMount.MountAmmoType.power) {
                    BulletType bullet = mounts.get(i).bullet;
                    types.put(of(bullet, null), Icon.power.getRegion());
                }

                if(mounts.get(i).ammoType == MultiTurretMount.MountAmmoType.item){
                    for(Item item : content.items()){
                        BulletType bullet = mountAmmoTypes.get(i).get(item);
                        if(bullet == null) continue;
                        types.put(of(bullet, item), item.icon(Cicon.medium));
                    }
                }

                if(mounts.get(i).ammoType == MultiTurretMount.MountAmmoType.liquid){
                    for(Liquid liquid : content.liquids()) {
                        BulletType bullet = liquidMountAmmoTypes.get(i).get(liquid);
                        if(bullet == null) continue;
                        types.put(of(bullet, liquid), liquid.icon(Cicon.medium));
                    }
                }
            }

            h.table(b -> {
                for(ObjectMap<BulletType, ? extends UnlockableContent> type : types.keys()){
                    int ii = 0;

                    for(BulletType bullet : type.keys()){
                        ii ++;
                        if(type.get(bullet) == null) {
                            b.add(new Stack(){{
                                add(new Table(o -> {
                                    o.right();
                                    o.add(new Image(types.get(type))).size(8 * 3).padRight(4);
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
                            if(bullet.ammoMultiplier > 0 && !(bullet instanceof LiquidBulletType)) rowAdd(e, Core.bundle.format("bullet.multiplier", Strings.fixed(bullet.ammoMultiplier, 1)));
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

        stats.remove(Stat.shootRange);
        stats.remove(Stat.inaccuracy);
        stats.remove(Stat.reload);
        stats.remove(Stat.ammo);
        stats.remove(Stat.targetsAir);
        stats.remove(Stat.targetsGround);
        stats.remove(Stat.ammoUse);

        stats.add(Stat.weapons, table -> {
            table.add();
            table.row();
            table.left();
            table.add("[lightgray]" + Core.bundle.get("stat.shar.base-t")).fillX().padLeft(24);
            table.row();

            //Base Turret
            table.table(null, w -> {
                addStatS(w, true, 0);
                table.row().left();
            }).left();

            table.row();
            table.left();
            table.add("[lightgray]" + Core.bundle.get("stat.shar.mini-t")).fillX().padLeft(24);

            //Mounts
            table.table(null, w -> {
                for(int i = 0; i < mounts.size; i++){
                    addStatS(w, false, i);
                    table.row();
                }
            });
        });
    }

    public class MultiTurretBuild extends ItemTurretBuild {
        public Seq<Integer> _totalAmmos = new Seq<>();
        public Seq<Float> _reloads = new Seq<>();
        public Seq<Float> _heats = new Seq<>();
        public Seq<Float> _recoils = new Seq<>();
        public Seq<Float> _shotCounters = new Seq<>();
        public Seq<Float> _rotations = new Seq<>();
        public Seq<Boolean> _wasShootings = new Seq<>();
        public Seq<Boolean> _chargings = new Seq<>();
        public @Nullable Seq<Posc> _targets = new Seq<>();
        public Seq<Vec2> _targetPoss = new Seq<>();
        public Seq<Seq<ItemEntry>> _ammos = new Seq<>();
        public float _heat;

        @Override
        public void remove(){
            super.remove();
            if(sound != null) sound.stop();
        }

        @Override
        public void created(){
            super.created();
            for(int i = 0; i < mounts.size; i++){
                _totalAmmos.add(0);
                _reloads.add(0f);
                _heats.add(0f);
                _recoils.add(0f);
                _shotCounters.add(0f);
                _rotations.add(90f);
                _targets.add(null);
                _targetPoss.add(new Vec2());
                _wasShootings.add(false);
                _chargings.add(false);
                _ammos.add(new Seq<>());
            }
        }

        public void tf(Table table, int i){
            table.add(new Stack(){{
                add(new Table(o -> {
                    o.left();
                    o.add(new Image(Core.atlas.find("shar-" + mounts.get(i).name + "-full"))).size(8 * 9);
                }));

                add(new Table(h -> {
                    if(mounts.get(i).ammoType == MultiTurretMount.MountAmmoType.item) {
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

                    if(mounts.get(i).ammoType == MultiTurretMount.MountAmmoType.liquid) {
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

                    if(mounts.get(i).ammoType == MultiTurretMount.MountAmmoType.power) {
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
            for(int i = 0; i < mounts.size; i++) tf(table, i);
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
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            for(int i = 0; i < mounts.size; i++){
                float fade = Mathf.curve(Time.time % totalRangeTime, rangeTime * i, rangeTime * i + fadeTime) - Mathf.curve(Time.time % totalRangeTime, rangeTime * (i + 1) - fadeTime, rangeTime * (i + 1));
                float[] loc = mountLocations(i);

                Lines.stroke(3, Pal.gray);
                Draw.alpha(fade);
                Lines.dashCircle(loc[0], loc[1], mounts.get(i).range);
                Lines.stroke(1, this.team.color);
                Draw.alpha(fade);
                Lines.dashCircle(loc[0], loc[1], mounts.get(i).range);

                Draw.color(this.team.color, fade);
                Draw.rect(turrets.get(i)[3], loc[2], loc[3], this._rotations.get(i) - 90);
                Draw.reset();
            }
        }

        @Override
        public int removeStack(Item item, int amount){
            return 0;
        }

        @Override
        public void handleItem(Building source, Item item){
            for(int h = 0; h < mounts.size; h++) {
                if(mountAmmoTypes.get(h) == null) continue;

                if (item == Items.pyratite) Events.fire(EventType.Trigger.flameAmmo);

                BulletType type = mountAmmoTypes.get(h).get(item);
                _totalAmmos.set(h, (int)(_totalAmmos.get(h) + type.ammoMultiplier));

                boolean asdf = true;
                for(int i = 0; i < _ammos.get(h).size; i++) {
                    ItemEntry entry = _ammos.get(h).get(i);

                    if(entry.item == item) {
                        entry.amount += (int)type.ammoMultiplier;
                        _ammos.get(h).swap(i, _ammos.get(h).size - 1);
                        asdf = false;
                        break;
                    }
                }

                if(asdf) _ammos.get(h).add(new ItemEntry(item, (int)type.ammoMultiplier < mounts.get(h).ammoPerShot ? (int)type.ammoMultiplier + mounts.get(h).ammoPerShot : (int)type.ammoMultiplier));
            }

            if(ammoTypes.get(item) != null && totalAmmo + ammoTypes.get(item).ammoMultiplier <= maxAmmo) super.handleItem(source, item);
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            for(int i = 0; i < mounts.size; i++)
                if(mountAmmoTypes.get(i) != null && mountAmmoTypes.get(i).get(item) != null)
                    return Math.max(Math.min((int)(( mounts.get(i).maxAmmo - _totalAmmos.get(i)) / mountAmmoTypes.get(i).get(item).ammoMultiplier), amount), Math.min((int)((maxAmmo - totalAmmo) / ammoTypes.get(item).ammoMultiplier), amount));
            return 0;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            for(int i = 0; i < mounts.size; i++)
                if((mountAmmoTypes.get(i) != null && mountAmmoTypes.get(i).get(item) != null &&_totalAmmos.get(i) + mountAmmoTypes.get(i).get(item).ammoMultiplier <= mounts.get(i).maxAmmo)
                    || ammoTypes.get(item) != null && totalAmmo + ammoTypes.get(item).ammoMultiplier <= maxAmmo)
                    return true;
            return false;
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
            Tmp.v1.trns(this.rotation - 90, (customMountLocation ? customMountLocationsX.get(mount) : mounts.get(mount).x), (customMountLocation ? customMountLocationsY.get(mount) : mounts.get(mount).y) - recoil);
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
            }
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
            unit.ammo((float)unit.type().ammoCapacity * totalAmmo / maxAmmo);
            for(int i = 0; i < mounts.size; i++){
                unit.ammo((float)unit.type().ammoCapacity * _totalAmmos.get(i) /  mounts.get(i).maxAmmo);
                unit.ammo(unit.type().ammoCapacity * liquids.currentAmount() / liquidCapacity);
                unit.ammo(power.status * unit.type().ammoCapacity);
            }

            super.updateTile();

            for(int i = 0; i < mounts.size; i++){
                _wasShootings.set(i, false);
                _recoils.set(i, Mathf.lerpDelta(_recoils.get(i), 0, mounts.get(i).restitution));
                _heats.set(i, Mathf.lerpDelta(_heats.get(i), 0, mounts.get(i).cooldown));

                if(!validateMountTarget(i)) _targets.set(i, null);
            }

            __heat -= edelta();

            if(__heat <= 0.001){
                for(int i = 0; i < mounts.size; i++){
                    if(mountHasAmmo(i)) {
                        this.mountLocations(i);
                        _targets.set(i, findMountTargets(i));
                    }
                }
                __heat = 16;
            }

            for(int i = 0; i < mounts.size; i++){
                if(mountHasAmmo(i)) {
                    float[] loc = this.mountLocations(i);

                    if(validateMountTarget(i)) {
                        boolean canShoot = true;

                        if(isControlled()) { //player behavior
                            _targetPoss.get(i).set(unit().aimX, unit().aimY);
                            canShoot = unit().isShooting;
                        }else if(this.logicControlled()) { //logic behavior
                            _targetPoss.set(i, targetPos);
                            canShoot = logicShooting;
                        }else { //default AI behavior
                            mountTargetPosition(i, _targets.get(i), loc[0], loc[1]);
                            if(Float.isNaN(_rotations.get(i))) _rotations.set(i, 0f);
                        }

                        float targetRot = Angles.angle(loc[0], loc[1], _targetPoss.get(i).x, _targetPoss.get(i).y);

                        if(!_chargings.get(i)) mountTurnToTarget(i, targetRot);

                        if (Angles.angleDist(_rotations.get(i), targetRot) < mounts.get(i).shootCone && canShoot) {
                            wasShooting = true;
                            _wasShootings.set(i, true);
                            updateMountShooting(i);
                        }
                    }
                }
            }

        }

        @Override
        protected void turnToTarget(float target) {
            super.turnToTarget(target);

            float speed = rotateSpeed * delta() * baseReloadSpeed();
            float dist = Math.abs(Angles.angleDist(rotation, target));

            if(dist < speed) return;

            float angle = Mathf.mod(rotation, 360);
            float to = Mathf.mod(target, 360);
            float allRot = speed;

            if((angle > to && Angles.backwardDistance(angle, to) > Angles.forwardDistance(angle, to)) || (angle < to && Angles.backwardDistance(angle, to) < Angles.forwardDistance(angle, to))) allRot = -speed;

            for(int i = 0; i < mounts.size; i++) _rotations.set(i, (_rotations.get(i) + allRot) % 360);
        }

        public void mountTurnToTarget(int mount, float target){
            float speed = mounts.get(mount).rotateSpeed * delta();
            if(mounts.get(mount).ammoType == MultiTurretMount.MountAmmoType.power || mounts.get(mount).powerUse > 0.001f) speed *= Mathf.clamp(power.graph.getPowerBalance()/mounts.get(mount).powerUse, 0, 1);
            else speed *= baseReloadSpeed();
            _rotations.set(mount, Angles.moveToward(_rotations.get(mount), target, speed));
        }

        public Posc findMountTargets(int mount){
            float[] loc = this.mountLocations(mount);

            if(mounts.get(mount).ammoType == MultiTurretMount.MountAmmoType.liquid && mounts.get(mount).extinguish && liquids.current().canExtinguish()) {
                int tr = (int) (mounts.get(mount).range / tilesize);
                for(int x = -tr; x <= tr; x++) for(int y = -tr; y <= tr; y++) {
                    Tile other = world.tileWorld(x + (int)loc[4]/8f, y + (int)loc[5]/8f);
                    //do not extinguish fires on other team blocks
                    if (other != null && Fires.has(x + (int)loc[4]/8, y + (int)loc[5]/8) && (other.build == null || other.team() == team)) return Fires.get(x + (int)loc[4]/8, y + (int)loc[5]/8);
                }
            }

            if(mounts.get(mount).targetAir && !mounts.get(mount).targetGround)
                 return Units.bestEnemy(this.team, loc[0], loc[1], mounts.get(mount).range, e -> !e.dead && !e.isGrounded(), mounts.get(mount).unitSort);
            else return Units.bestTarget(this.team, loc[0], loc[1], mounts.get(mount).range, e -> !e.dead && (e.isGrounded() || mounts.get(mount).targetAir) && (!e.isGrounded() || mounts.get(mount).targetGround), b -> true, mounts.get(mount).unitSort);
        }

        public boolean validateMountTarget(int mount){
            float[] loc = mountLocations(mount);

            return !Units.invalidateTarget(_targets.get(mount), team, loc[0], loc[1]) || isControlled() || logicControlled();
        }

        public void mountTargetPosition(int mount, Posc pos, float x, float y){
            if(!mountHasAmmo(mount)) return;

            BulletType bullet = mountPeekAmmo(mount);
            float speed = bullet.speed;

            if(speed < 0.1) speed = 9999999;

            _targetPoss.get(mount).set(Predict.intercept(Tmp.v4.set(x, y), pos, speed));

            if(_targetPoss.get(mount).isZero()) _targetPoss.get(mount).set(_targets.get(mount));
        }

        public void updateMountShooting(int mount){
            if(_reloads.get(mount) >= mounts.get(mount).reloadTime){
                  mountShoot(mount, mounts.get(mount).ammoType == MultiTurretMount.MountAmmoType.liquid ? liquidMountAmmoTypes.get(mount).get(liquids.current()) : mountPeekAmmo(mount));
                  _reloads.set(mount, 0f);
            }else {
                float speed = delta() * mountPeekAmmo(mount).reloadMultiplier;
                if(mounts.get(mount).ammoType == MultiTurretMount.MountAmmoType.power || mounts.get(mount).powerUse > 0.001f) speed *= Mathf.clamp(power.graph.getPowerBalance()/mounts.get(mount).powerUse, 0, 1);
                else speed *= baseReloadSpeed();
                if(speed >= 0.001f) _reloads.set(mount, _reloads.get(mount) + speed);
            }
        }

        @Override
        protected void updateCooling() {
            super.updateCooling();

            float maxUsed = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount / mounts.size;

            Liquid liquid = liquids.current();

            for(int i = 0; i < mounts.size; i++){
                float used = Math.min(Math.min(liquids.get(liquid), maxUsed * Time.delta), Math.max(0, ((mounts.get(i).reloadTime - _reloads.get(i)) / coolantMultiplier) / liquid.heatCapacity));
                if(mounts.get(i).ammoType == MultiTurretMount.MountAmmoType.power || mounts.get(i).powerUse > 0.001f) used *= Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1);
                else used *= baseReloadSpeed();
                _reloads.set(i, _reloads.get(i) + used * liquid.heatCapacity * coolantMultiplier);

                liquids.remove(liquid, used);

                float[] loc = mountLocations(i);

                if(Mathf.chance(0.06 / mounts.size * used)) mounts.get(i).coolEffect.at(loc[0] + Mathf.range(mounts.get(i).width), loc[1] + Mathf.range(mounts.get(i).height));
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

        public void mountBullet(int mount, BulletType type, float spreadAmount){
            float[] loc = mountLocations(mount);

            float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(loc[4], loc[5], _targetPoss.get(mount).x, _targetPoss.get(mount).y) / type.range(), mounts.get(mount).minRange / type.range(), mounts.get(mount).range / type.range()) : 1;
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
        }

        public BulletType mountPeekAmmo(int mount){
            if(mounts.get(mount).ammoType == MultiTurretMount.MountAmmoType.power) return mounts.get(mount).bullet;
            if(mounts.get(mount).ammoType == MultiTurretMount.MountAmmoType.item) return _ammos.get(mount).peek().types(mount);
            if(mounts.get(mount).ammoType == MultiTurretMount.MountAmmoType.liquid) return liquidMountAmmoTypes.get(mount).get(liquids.current());

            return null;
        }

        public BulletType mountUseAmmo(int mount){
            if(mounts.get(mount).ammoType == MultiTurretMount.MountAmmoType.power) return mounts.get(mount).bullet;
            if(cheating()){
                if(mounts.get(mount).ammoType == MultiTurretMount.MountAmmoType.item) return mountPeekAmmo(mount);
                if(mounts.get(mount).ammoType == MultiTurretMount.MountAmmoType.liquid) return liquidMountAmmoTypes.get(mount).get(liquids.current());
            }

            if(mounts.get(mount).ammoType == MultiTurretMount.MountAmmoType.item){
                ItemEntry entry = _ammos.get(mount).peek();
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
            if(mounts.get(mount).ammoType == MultiTurretMount.MountAmmoType.power) {
                return true;
            }
            if(mounts.get(mount).ammoType == MultiTurretMount.MountAmmoType.item) {
                if (_ammos.get(mount).size >= 2 && _ammos.get(mount).peek().amount < mounts.get(mount).ammoPerShot) _ammos.get(mount).pop();
                return _ammos.get(mount).size > 0 && _ammos.get(mount).peek().amount >= mounts.get(mount).ammoPerShot;
            }
            if(mounts.get(mount).ammoType == MultiTurretMount.MountAmmoType.liquid) {
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
                if(mounts.get(i).ammoType != MultiTurretMount.MountAmmoType.item) continue;

                write.b(_ammos.get(i).size);
                for (AmmoEntry entry : _ammos.get(i)) {
                    ItemEntry it = (ItemEntry) entry;
                    write.s(it.item.id);
                    write.s(it.amount);
                }
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            for(int h = 0; h < mounts.size; h++) {
                if(mounts.get(h).ammoType != MultiTurretMount.MountAmmoType.item) continue;

                int amount = read.ub();
                for(int i = 0; i < amount; i++) {
                    Item item = Vars.content.item(revision < 2 ? read.ub() : read.s());
                    short a = read.s();
                    _totalAmmos.set(i, _totalAmmos.get(i) + a);

                    //only add ammo if this is a valid ammo type //NO ADD ON EVERY MOUNT WHICH ITEMAMMO
                    if(item != null && mountAmmoTypes.get(h) != null && mountAmmoTypes.get(h).containsKey(item)) _ammos.get(i).add(new ItemEntry(item, a));
                }
            }
        }

    }
    public class ItemEntry extends AmmoEntry{
        protected Item item;

        ItemEntry(Item item, int amount){
            this.item = item;
            this.amount = amount;
        }
        @Override
        public BulletType type(){
            return ammoTypes.get(item);
        }

        public BulletType types(int i){
            return mountAmmoTypes.get(i).get(item);
        }
    }
}