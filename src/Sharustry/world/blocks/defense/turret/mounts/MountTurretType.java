package Sharustry.world.blocks.defense.turret.mounts;

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
import mindustry.entities.pattern.ShootPattern;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.*;

public class MountTurretType {
    public int ammoPerShot = 1;

    public float xOffset = 0, yOffset = 0;
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
    public boolean extinguish = false; //whether can shoot into fire. == targetFire
    public boolean targetAir = true, targetGround = true;
    public Boolf<Unit> unitFilter = u -> true;
    public Boolf<Building> buildingFilter = b -> !b.block.underBullets;
    public BulletType bullet;
    public Color heatColor = Pal.turretHeat;

    public String name;
    public String title = "ohno";

    public Sound shootSound = Sounds.pew;
    public Sound loopSound = Sounds.none;
    public Effect ammoUseEffect = Fx.none;
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

    public SoundLoop loopSoundLoop;
    public DrawMountTurret drawer = new DrawMountTurret();

    public MountTurretType(String name) {
        this.name = name;
    }

    public MountTurretType(String name, BulletType bullet, Object... ammos){
        this(name);
        this.bullet = bullet;
    }

    public MountTurret<MountTurretType> create(MultiTurret block, MultiTurret.MultiTurretBuild build, int index, float x, float y) {
        return new MountTurret(this, block, build, index, x, y);
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
        loopSoundLoop = new SoundLoop(loopSound, loopVolume);
        laser = Core.atlas.find("shar-repair-laser");
        laserEnd = Core.atlas.find("shar-repair-laser-end");
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
        if(reload > 0) rowAdd(table, "[lightgray]" + Stat.reload.localized() + ": [white]" + Strings.autoFixed(60 / reload * shoot.shots, 1));
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
        float tX = x * tilesize + block.offset + (block.customMountLocation ? block.customgetMountLocationX.get(mount) : this.xOffset);
        float tY = y * tilesize + block.offset + (block.customMountLocation ? block.customgetMountLocationY.get(mount) : this.yOffset);

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
        Vec2 vec = mount.getMountLocation();
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

        float targetRot = Angles.angle(vec.x, vec.y, mount.targetPos.x, mount.targetPos.y);

        if(!mount.charging) mount.turnToTarget(targetRot);

        if (Angles.angleDist(mount.rotation, targetRot) < shootCone && canShoot) {
            build.wasShooting = true;
            mount.wasShooting = true;
            mount.updateShooting();
        }
    }
}