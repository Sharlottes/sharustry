package Sharustry.world.blocks.defense.turret.mounts;

import Sharustry.world.blocks.defense.turret.MountTurret;
import Sharustry.world.blocks.defense.turret.MultiTurret;
import arc.Core;
import arc.audio.Sound;
import arc.func.Func2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Scaling;
import arc.util.Strings;
import arc.util.Time;
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
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.*;

public class MountTurretType {
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

    public float reload = 30;
    public float maxAmmo = 20;
    public float range = 80;
    public float rotateSpeed = 5;
    public float inaccuracy = 0;
    public float velocityInaccuracy = 0;
    public float shootCone = 8;

    public float recoilAmount = 1;
    public float restitution = 0.02f;
    public float cooldown = 0.02f;

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

    public Sound shootSound = Sounds.pew;
    public Sound loopSound = Sounds.none;

    public Effect shootEffect = Fx.none;
    public Effect smokeEffect = Fx.none;
    public Effect coolEffect = Fx.fuelburn;
    public Effect ejectEffect = Fx.none;
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

    public ObjectMap<Liquid, BulletType> liquidMountAmmoType;
    public ObjectMap<Item, BulletType> mountAmmoType;

    public TextureRegion[] turrets = new TextureRegion[]{};

    public SoundLoop loopSoundLoop;

    public MountTurretType(String name) {
        this.name = name;
    }

    public MountTurretType(String name, BulletType bullet, Object... ammos){
        this(name);
        this.bullet = bullet;
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

    public ObjectMap<ObjectMap<BulletType ,? extends UnlockableContent>, TextureRegion> getStatData() {
        return null;
    }
    public void buildStat(Table table) {
        rowAdd(table, "[lightgray]" + Stat.shootRange.localized() + ": [white]" + Strings.fixed(range / tilesize, 1) + " " + StatUnit.blocks);
        rowAdd(table, "[lightgray]" + Stat.targetsAir.localized() + ": [white]" + (!targetAir ? Core.bundle.get("no") : Core.bundle.get("yes")));
        rowAdd(table, "[lightgray]" + Stat.targetsGround.localized() + ": [white]" + (!targetGround ? Core.bundle.get("no") : Core.bundle.get("yes")));
        if(reload > 0) rowAdd(table, "[lightgray]" + Stat.reload.localized() + ": [white]" + Strings.autoFixed(60 / reload * shots, 1));
        if(inaccuracy > 0) rowAdd(table, "[lightgray]" + Stat.inaccuracy.localized() + ": [white]" + inaccuracy + " " + StatUnit.degrees.localized());
        if(chargeTime > 0.001f) rowAdd(table, "[lightgray]" + Core.bundle.get("stat.shar.chargeTime") + ": [white]" + Mathf.round(chargeTime/60, 100) + " " + Core.bundle.format("stat.shar.seconds"));
    }

    public void addStatS(Table w){
        w.left().row();
        w.add(title).right().top().row();
        w.image(Core.atlas.find("shar-"+ name + "-full")).size(60).scaling(Scaling.bounded).right().top();
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
                            o.add(new Image(Icon.power.getRegion())).size(8 * 3).padRight(4);
                        }),
                        new Table(t -> {
                            t.right().bottom();
                            t.add(((int)powerUse * 60) + "").fontScale(0.9f).color(Color.yellow).padTop(8);
                            t.pack();
                        })
                    ).padRight(4).right().top();
                    b.add(Core.bundle.format("stat.shar.power")).padRight(10).left().top();
                } else {
                    for(ObjectMap<BulletType, ? extends UnlockableContent> type : types.keys()){
                        int ii = 0;

                        for(BulletType bullet : type.keys()){
                            ii ++;

                            if(type.get(bullet) == null) {
                                b.stack(
                                    new Table(o -> {
                                        o.right();
                                        o.add(new Image(Icon.power.getRegion())).size(8 * 3).padRight(4);
                                    }),
                                    new Table(t -> {
                                        t.right().bottom();
                                        t.add(((int)powerUse * 60) + "").fontScale(0.9f).color(Color.yellow).padTop(8);
                                        t.pack();
                                    })
                                ).padRight(4).right().top();
                                b.add(Core.bundle.format("stat.shar.power")).padRight(10).left().top();
                            } else {
                                b.image(types.get(type)).size(8 * 4).padRight(4).right().top();
                                b.add(type.get(bullet).localizedName).padRight(10).left().top();
                            }

                            b.table(Tex.underline, e -> {
                                e.left().defaults().padRight(3).left();

                                if(bullet.damage > 0 && (bullet.collides || bullet.splashDamage <= 0)) rowAdd(e, Core.bundle.format("bullet.damage", bullet.damage));
                                if(bullet.buildingDamageMultiplier != 1) rowAdd(e, Core.bundle.format("bullet.buildingdamage", Strings.fixed((int)(bullet.buildingDamageMultiplier * 100),1)));
                                if(bullet.splashDamage > 0) rowAdd(e, Core.bundle.format("bullet.splashdamage", bullet.splashDamage, Strings.fixed(bullet.splashDamageRadius / tilesize, 1)));
                                if(bullet.ammoMultiplier > 0 && !Mathf.equal(bullet.ammoMultiplier, 1f)) rowAdd(e, Core.bundle.format("bullet.multiplier", Strings.fixed(bullet.ammoMultiplier, 1)));
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
        Lines.dashCircle(tX, tY, range);
        Lines.stroke(1, player.team().color);
        Draw.alpha(fade);
        Lines.dashCircle(tX, tY, range);
        Draw.color(player.team().color, fade);
        Draw.rect(turrets[3], tX, tY);
        Draw.reset();
    }
    public void update(MountTurret mount, MultiTurret.MultiTurretBuild build) {
        if(mount.isTargetInvalid(build)) return;
        float[] loc = mount.mountLocations(build);
        boolean canShoot = true;

        if(build.isControlled()) { //player behavior
            mount.targetPos.set(build.unit().aimX, build.unit().aimY);
            canShoot = build.unit().isShooting;
        }else if(build.logicControlled()) { //logic behavior
            mount.targetPos = build.targetPos;
            canShoot = build.logicShooting;
        }else { //default AI behavior
            mount.targetPosition(build, mount.target, loc[0], loc[1]);
            if(Float.isNaN(mount.rotation)) mount.rotation = 0f;
        }

        float targetRot = Angles.angle(loc[0], loc[1], mount.targetPos.x, mount.targetPos.y);

        if(!mount.charging) mount.targetTurn(build, targetRot);

        if (Angles.angleDist(mount.rotation, targetRot) < shootCone && canShoot) {
            build.wasShooting = true;
            mount.wasShooting = true;
            mount.updateShooting(build);
        }
    }
}