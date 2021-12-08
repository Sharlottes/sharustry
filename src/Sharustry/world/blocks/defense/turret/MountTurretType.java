package Sharustry.world.blocks.defense.turret;

import arc.Core;
import arc.audio.Sound;
import arc.func.Func2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.OrderedMap;
import arc.struct.Seq;
import arc.util.Scaling;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.audio.SoundLoop;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.*;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.StatusEffect;
import mindustry.ui.Cicon;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static arc.struct.ObjectMap.of;
import static mindustry.Vars.*;
import static mindustry.Vars.control;

public class MountTurretType implements Cloneable {
    public int shots = 1;
    public int ammoPerShot = 2;

    public float x = 0;
    public float y = 0;
    public float shootX = 0;
    public float xRand = 0;
    public float shootY = 0;
    public float yRand = 0;
    public float width = 3;
    public float height = 3;
    public float elevation = 1;

    public float reloadTime = 30;
    public float maxAmmo = 20;
    public float range = 80;
    public float rotateSpeed = 5;
    public float inaccuracy = 0;
    public float velocityInaccuracy = 0;
    public float shootCone = 8;

    public float recoilAmount = 1;
    public float restitution = 0.02f;
    public float cooldown = 0.02f;

    public float ejectX = 1;
    public float ejectY = -1;
    public float loopVolume = 1;
    public float shootShake = 0;

    public float minRange = 0;
    public float barrels = 1;
    public float barrelSpacing = 0;
    public float spread = 0;
    public float burstSpacing = 0;

    public float coolantMultiplier = 1;

    public float powerUse = 0f; //mountType: without item and liquid

    public boolean altEject = true;
    public boolean ejectRight = true;
    public boolean sequential = false;
    public boolean extinguish = false; //whether can shoot into fire. == targetFire
    public boolean targetAir = true;
    public boolean targetGround = true;

    public BulletType bullet;
    public Color heatColor = Pal.turretHeat;

    public String name;
    public String title = "ohno";
    public String icon = "error";

    public Sound shootSound = Sounds.pew;
    public Sound loopSound = Sounds.none;

    public Effect shootEffect = Fx.none;
    public Effect smokeEffect = Fx.none;
    public Effect coolEffect = Fx.fuelburn;
    public Effect ejectEffect = Fx.none;

    public MultiTurretMountType mountType; //power, item, liquid, tract
    public Units.Sortf unitSort = Unit::dst2;

    //charge region
    public float chargeTime = -1;
    public float shootLength = 8;
    public float chargeEffects = 5;
    public float chargeMaxDelay = 48;
    public Effect chargeEffect = Fx.none, chargeBeginEffect = Fx.none;
    public Sound chargeSound = Sounds.none;
    //region end

    //tract region, mountType: tract only
    public float laserWidth = 0.6f;
    public float force = 0.3f;
    public float scaledForce = 0f;
    public float damage = 0f;
    public float statusDuration = 300;
    public float shootSoundVolume = 0.9f;

    public Color laserColor = Color.white;
    public StatusEffect status = StatusEffects.none;

    public TextureRegion tractLaser, tractLaserEnd;
    //region end

    //point region, mountType: point only
    public Color colorPoint = Color.white;
    public Effect beamEffect = Fx.pointBeam;
    public Effect hitEffect = Fx.pointHit;

    public float bulletDamage = 10f;
    //region end

    //repair region, mountType: repair only
    public static final Rect rect = new Rect();

    public float repairRadius = 50f;
    public float repairSpeed = 0.3f;

    public TextureRegion laser, laserEnd;
    //region end

    //mass region, mountType: mass only
    public float translation = 7f;
    public int minDistribute = 10;
    public int maxDistribute = 300;
    public float knockback = 4f;
    public float bulletSpeed = 5.5f;
    public float bulletLifetime = 200f;
    public Effect receiveEffect = Fx.mineBig;
    public float shake = 3f;
    //region end

    //drill region, mountType: drill only
    /** Drill tiers, inclusive */
    public int minDrillTier = 0, maxDrillTier = 3;
    public float mineSpeed = 0.75f;
    public float laserOffset = 4f;
    //region end

    public boolean healBlock = false;
    public boolean acceptCooling = false;

    //skill
    public Seq<Integer> skillDelays = new Seq<>();
    public Seq<Func2<Building, MountTurretType, Runnable>> skillSeq = new Seq<>();

    public ObjectMap<Liquid, BulletType> liquidMountAmmoType = new ObjectMap<>();
    public ObjectMap<Item, BulletType> mountAmmoType = new ObjectMap<>();

    public TextureRegion[] turrets = new TextureRegion[]{};

    public SoundLoop loopSoundLoop;

    public MountTurretType(String name) {
        this.name = name;
    }

    public MountTurretType(String name, MultiTurretMountType mountType, BulletType bullet, Object... ammos){
        this(name);
        this.bullet = bullet;
        this.mountType = mountType;
        if(mountType == MountTurretType.MultiTurretMountType.item) {
            mountAmmoType = OrderedMap.of(ammos);
            liquidMountAmmoType = null;
        }
        if(mountType == MountTurretType.MultiTurretMountType.liquid) {
            mountAmmoType = null;
            liquidMountAmmoType = OrderedMap.of(ammos);
        }
        if(mountType == MultiTurretMountType.power
                || mountType == MultiTurretMountType.tract
                || mountType == MultiTurretMountType.point
                || mountType == MultiTurretMountType.repair
                || mountType == MultiTurretMountType.mass
                || mountType == MultiTurretMountType.drill){
            mountAmmoType = null;
            liquidMountAmmoType = null;
        }
    }

    public MountTurretType(String name, MultiTurretMountType mountType, Object... ammos){
        this(name);
        this.mountType = mountType;
        if(mountType == MountTurretType.MultiTurretMountType.item) {
            mountAmmoType = OrderedMap.of(ammos);
            liquidMountAmmoType = null;
        }
        if(mountType == MountTurretType.MultiTurretMountType.liquid) {
            mountAmmoType = null;
            liquidMountAmmoType = OrderedMap.of(ammos);
        }
        if(mountType == MultiTurretMountType.power
                || mountType == MultiTurretMountType.tract
                || mountType == MultiTurretMountType.point
                || mountType == MultiTurretMountType.repair
                || mountType == MultiTurretMountType.mass
                || mountType == MultiTurretMountType.drill){
            mountAmmoType = null;
            liquidMountAmmoType = null;
        }
    }

    public void load(){
        //[Sprite, Outline, Heat, Fade Mask]
        turrets = new TextureRegion[]{
                Core.atlas.find("shar-" + name + ""),
                Core.atlas.find("shar-" + name + "-outline"),
                Core.atlas.find("shar-" + name + "-heat"),
                Core.atlas.find("shar-" + name + "-mask")
        };

        //for some unknown reason, mounts cannot use @Load annotations...hmm
        laser = Core.atlas.find("shar-repair-laser");
        laserEnd = Core.atlas.find("shar-repair-laser-end");
        tractLaser = Core.atlas.find("shar-tlaser");
        tractLaserEnd = Core.atlas.find("shar-tlaser-end");

        loopSoundLoop = new SoundLoop(loopSound, loopVolume);
    }

    public <T1 extends Building, T2 extends MountTurretType> void addSkills(Func2<T1, T2, Runnable> skill, int delay){
        if(skill != null) {
            skillSeq.add((Func2<Building, MountTurretType, Runnable>) skill);
            skillDelays.add(delay);
        }
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
        w.image(Core.atlas.find("shar-"+ name + "-full")).size(60).scaling(Scaling.bounded).right().top();

        w.table(Tex.underline, h -> {
            h.left().defaults().padRight(3).left();
            rowAdd(h, "[lightgray]" + Stat.shootRange.localized() + ": [white]" + Strings.fixed(range / tilesize, 1) + " " + StatUnit.blocks);
            if(!(mountType == MountTurretType.MultiTurretMountType.point)) {
                rowAdd(h, "[lightgray]" + Stat.targetsAir.localized() + ": [white]" + (!targetAir ? Core.bundle.get("no") : Core.bundle.get("yes")));
                rowAdd(h, "[lightgray]" + Stat.targetsGround.localized() + ": [white]" + (!targetGround ? Core.bundle.get("no") : Core.bundle.get("yes")));
            }else rowAdd(h, "[lightgray]" + Core.bundle.format("stat.shar.targetsBullet") + ": [white]" + Core.bundle.get("yes"));
            if(inaccuracy > 0) rowAdd(h, "[lightgray]" + Stat.inaccuracy.localized() + ": [white]" + inaccuracy + " " + StatUnit.degrees.localized());
            if(chargeTime > 0.001f) rowAdd(h, "[lightgray]" + Core.bundle.get("stat.shar.chargeTime") + ": [white]" + Mathf.round(chargeTime/60, 100) + " " + Core.bundle.format("stat.shar.seconds"));
            if(mountType == MountTurretType.MultiTurretMountType.item) rowAdd(h, "[lightgray]" + Core.bundle.get("stat.shar.ammo-shot") + ": [white]" + ammoPerShot);
            if(mountType == MountTurretType.MultiTurretMountType.tract) rowAdd(h, "[lightgray]" + Stat.damage.localized() + ": [white]" + Core.bundle.format("stat.shar.damage", damage * 60f));
            if(mountType == MountTurretType.MultiTurretMountType.repair) rowAdd(h, "[lightgray]" + Stat.range.localized() + ": [white]" + Core.bundle.format("stat.shar.range", repairRadius / tilesize));
            else if(mountType != MountTurretType.MultiTurretMountType.tract) rowAdd(h, "[lightgray]" + Stat.reload.localized() + ": [white]" + Strings.autoFixed(60 / reloadTime * shots, 1));

            h.row();

            ObjectMap<ObjectMap<BulletType ,? extends UnlockableContent>, TextureRegion> types = new ObjectMap<>();
                if(mountType == MountTurretType.MultiTurretMountType.power) {
                    BulletType bullet = this.bullet;
                    if(bullet != null) types.put(of(bullet, null), Icon.power.getRegion());
                }

                if(mountType == MountTurretType.MultiTurretMountType.item){
                    for(Item item : content.items()){
                        BulletType bullet = mountAmmoType.get(item);
                        if(bullet != null) types.put(of(bullet, item), item.icon(Cicon.medium));
                    }
                }

                if(mountType == MountTurretType.MultiTurretMountType.liquid){
                    for(Liquid liquid : content.liquids()) {
                        BulletType bullet = liquidMountAmmoType.get(liquid);
                        if(bullet != null) types.put(of(bullet, liquid), liquid.icon(Cicon.medium));
                    }
                }

            h.table(b -> {
                if(mountType == MountTurretType.MultiTurretMountType.tract
                        || mountType == MountTurretType.MultiTurretMountType.point
                        || mountType == MountTurretType.MultiTurretMountType.repair
                        || mountType == MountTurretType.MultiTurretMountType.mass){
                    b.add(new Stack() {{
                        add(new Table(o -> {
                            o.right();
                            o.add(new Image(Icon.power.getRegion())).size(8 * 3).padRight(4);
                        }));

                        add(new Table(t -> {
                            t.right().bottom();
                            t.add(((int)powerUse * 60) + "").fontScale(0.9f).color(Color.yellow).padTop(8);
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
                                    t.add(((int)powerUse * 60) + "").fontScale(0.9f).color(Color.yellow).padTop(8);
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
                            if(mountType == MountTurretType.MultiTurretMountType.item && bullet.ammoMultiplier > 0 && !Mathf.equal(bullet.ammoMultiplier, 1f)) rowAdd(e, Core.bundle.format("bullet.multiplier", Strings.fixed(bullet.ammoMultiplier, 1)));
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

    public void drawPlace(MultiTurret block, int mount, int x, int y, int rotation, boolean valid){
        float fade = Mathf.curve(Time.time % block.totalRangeTime, block.rangeTime * mount, block.rangeTime * mount + block.fadeTime) - Mathf.curve(Time.time % block.totalRangeTime, block.rangeTime * (mount + 1) - block.fadeTime, block.rangeTime * (mount + 1));
        float tX = x * tilesize + block.offset + (block.customMountLocation ? block.customMountLocationsX.get(mount) : this.x);
        float tY = y * tilesize + block.offset + (block.customMountLocation ? block.customMountLocationsY.get(mount) : this.y);

        Lines.stroke(3, Pal.gray);
        Draw.alpha(fade);
        if(mountType == MountTurretType.MultiTurretMountType.repair){
            Lines.dashCircle(tX, tY, repairRadius);
            Lines.stroke(1, Pal.heal);
            Draw.alpha(fade);
            Lines.dashCircle(tX, tY, repairRadius);
        }
        else{
            Lines.dashCircle(tX, tY, range);
            Lines.stroke(1, Vars.player.team().color);
            Draw.alpha(fade);
            Lines.dashCircle(tX, tY, range);
        }

        Draw.color(Vars.player.team().color, fade);
        Draw.rect(turrets[3], tX, tY);
        Draw.reset();


        if(mountType == MountTurretType.MultiTurretMountType.mass){
            //check if a mass driver is selected while placing this driver
            if(!control.input.frag.config.isShown()) return;
            Building selected = control.input.frag.config.getSelectedTile();
            if(selected == null || !(selected.block instanceof MultiTurret) || !(selected.within(x * tilesize, y * tilesize, range))) return;

            //if so, draw a dotted line towards it while it is in range
            float sin = Mathf.absin(Time.time, 6f, 1f);
            Tmp.v1.set(tX, tY).sub(selected.x, selected.y).limit((block.size / 2f + 1) * tilesize + sin + 0.5f);
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

    public MountTurretType copy(){
        try{
            return (MountTurretType)clone();
        }catch(CloneNotSupportedException suck){
            throw new RuntimeException("very good language design", suck);
        }
    }

    public enum MultiTurretMountType {
        item,
        liquid,
        power,
        tract,
        point,
        repair,
        mass,
        drill
    }
}