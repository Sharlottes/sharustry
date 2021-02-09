package Sharustry.world.blocks.defense;

import arc.Core;
import arc.Events;
import arc.graphics.Blending;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.Vec2;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.annotations.Annotations;
import mindustry.audio.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.BulletType;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.Cicon;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

public class MultiTurret extends ItemTurret {
    public float rangeTime;
    public float fadeTime;
    public String title;
    public Seq<MultiTurretMount> mounts = new Seq<>();
    public int amount;
    public float totalRangeTime = rangeTime * mounts.size;
    public Item ammoItem;
    public BulletType mainBullet;
    public Seq<SoundLoop> loopSounds = new Seq<>();

    public TextureRegion outline, baseTurret;
    public Seq<TextureRegion[]> turrets = new Seq<>();

    public @Annotations.Load(value = "@", length = 4) TextureRegion[] sprites;

    public MultiTurret(String name, Item ammoItem, BulletType mainBullet, float rangeTime, float fadeTime, String title, int amount, MultiTurretMount... mounts){
        this(name);
        this.rangeTime = rangeTime;
        for(MultiTurretMount mount : mounts) this.mounts.add(mount);
        this.amount = amount;
        this.ammoItem = ammoItem;
        this.mainBullet = mainBullet;
        this.fadeTime = fadeTime;
        this.title = title;
        ammo(ammoItem, mainBullet);
    }

    public MultiTurret(String name){
        super(name);
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
            for(int i = 0; i < amount; i++){
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
        for(int i = 0; i < mounts.size; i++)
            loopSounds.add(new SoundLoop(mounts.get(i).loopSound, mounts.get(i).loopVolume));
    }
    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        for(int i = 0; i < mounts.size; i++){
            float fade = Mathf.curve(Time.time % totalRangeTime, rangeTime * i, rangeTime * i + fadeTime) - Mathf.curve(Time.time % totalRangeTime, rangeTime * (i + 1) - fadeTime, rangeTime * (i + 1));
            float tX = x * Vars.tilesize + this.offset + mounts.get(i).x;
            float tY = y * Vars.tilesize + this.offset + mounts.get(i).y;

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

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.shootRange);
        stats.remove(Stat.inaccuracy);
        stats.remove(Stat.reload);

        stats.remove(Stat.ammo);
        StatValue ammoStat = table -> {
            table.row();
            table.image(ammoItem.icon(Cicon.medium)).size(8 * 4).padRight(4).right().top();
            table.add(ammoItem.localizedName).padRight(10).left().top();
            table.table(Tex.underline, b -> {
                b.left().defaults().padRight(3).left();

                b.add(Core.bundle.format("bullet.multiplier", mainBullet.ammoMultiplier));
            }).padTop(-9).left();
            table.row();
        };

        stats.add(Stat.ammo, ammoStat);

        stats.remove(Stat.targetsAir);
        stats.remove(Stat.targetsGround);

        StatValue wT = table -> {
            table.add();
            table.row();
            table.left();
            table.add("[lightgray]" + Core.bundle.get("stat.prog-mats.base-t")).fillX().padLeft(24);
            table.row();

            //Base Turret
            table.table(null, w -> {
                w.row();

                w.add(title).padRight(10).right().top();
                w.row();
                w.image(baseTurret).size(60).scaling(Scaling.bounded).right().top();

                w.table(Tex.underline, h -> {
                    h.left().defaults().padRight(3).left();

                    if(inaccuracy > 0)
                        h.add("[lightgray]" + Stat.inaccuracy.localized() + ": [white]" + inaccuracy + " " + StatUnit.degrees.localized());

                    if(range > 0){
                        h.row();
                        h.add("[lightgray]" + Stat.shootRange.localized() + ": [white]" + Strings.fixed(range / Vars.tilesize, 1) + " " + StatUnit.blocks);
                    }

                    h.row();
                    h.add("[lightgray]" + Core.bundle.get("stat.prog-mats.ammo-shot") + ": [white]" + ammoPerShot);
                    h.row();
                    h.add("[lightgray]" + Stat.reload.localized() + ": [white]" + Strings.autoFixed(60 / reloadTime * shots, 1));

                    h.row();
                    h.add("[lightgray]" + Stat.targetsAir.localized() + ": [white]" + (!targetAir ? Core.bundle.get("no") : Core.bundle.get("yes")));
                    h.row();
                    h.add("[lightgray]" + Stat.targetsGround.localized() + ": [white]" + (!targetGround ? Core.bundle.get("no") : Core.bundle.get("yes")));

                    BulletType type = mainBullet;

                    if(type.damage > 0 && (type.collides || type.splashDamage <= 0)){
                        // this.sep(table, Core.bundle.format("bullet.damage", type.damage));
                        h.row();
                        h.add(Core.bundle.format("bullet.damage", type.damage));
                    }

                    if(type.splashDamage > 0){
                        // this.sep(table, Core.bundle.format("bullet.splashdamage", type.splashDamage, Strings.fixed(type.splashDamageRadius / Vars.tilesize, 1)));
                        h.row();
                        h.add(Core.bundle.format("bullet.splashdamage", type.splashDamage, Strings.fixed(type.splashDamageRadius / Vars.tilesize, 1)));
                    }

                    if(type.knockback > 0){
                        // this.sep(table, Core.bundle.format("bullet.knockback", Strings.fixed(type.knockback, 1)));
                        h.row();
                        h.add(Core.bundle.format("bullet.knockback", Strings.fixed(type.knockback, 1)));
                    }

                    if(type.healPercent > 0){
                        // this.sep(table, Core.bundle.format("bullet.healpercent", type.healPercent));
                        h.row();
                        h.add(Core.bundle.format("bullet.healpercent", type.healPercent));
                    }

                    if(type.pierce || type.pierceCap != -1){
                        // this.sep(table, type.pierceCap == -1 ? "@bullet.infinitepierce" : Core.bundle.format("bullet.pierce", type.pierceCap));
                        h.row();
                        h.add(type.pierceCap == -1 ? "@bullet.infinitepierce" : Core.bundle.format("bullet.pierce", type.pierceCap));
                    }

                    if(type.status == StatusEffects.burning || type.status == StatusEffects.melting || type.incendAmount > 0){
                        // this.sep(table, "@bullet.incendiary");
                        h.row();
                        h.add("@bullet.incendiary");
                    }

                    if(type.status == StatusEffects.freezing){
                        // this.sep(table, "@bullet.freezing");
                        h.row();
                        h.add("@bullet.freezing");
                    }

                    if(type.status == StatusEffects.tarred){
                        // this.sep(table, "@bullet.tarred");
                        h.row();
                        h.add("@bullet.tarred");
                    }

                    if(type.status == StatusEffects.sapped){
                        // this.sep(table, "@bullet.sapping");
                        h.row();
                        h.add("@bullet.sapping");
                    }

                    if(type.homingPower > 0.01){
                        // this.sep(table, "@bullet.homing");
                        h.row();
                        h.add("@bullet.homing");
                    }

                    if(type.lightning > 0){
                        // this.sep(table, "@bullet.shock");
                        h.row();
                        h.add("@bullet.shock");
                    }

                    if(type.fragBullet != null){
                        // this.sep(table, "@bullet.frag");
                        h.row();
                        h.add("@bullet.frag");
                    }
                    h.row();
                }).padTop(-15).left();
                w.row();

            });

            table.row();
            table.left();
            table.add("[lightgray]" + Core.bundle.get("stat.prog-mats.mini-t")).fillX().padLeft(24);

            //Mounts
            table.table(null, w -> {
                for(int i = 0; i < mounts.size; i++){
                    MultiTurretMount mount = mounts.get(i);
                    w.row();

                    w.add(mount.title).padRight(10).right().top();
                    w.row();
                    w.image(Core.atlas.find(mount.name + "-full")).size(60).scaling(Scaling.bounded).right().top();

                    w.table(Tex.underline, h -> {
                        h.left().defaults().padRight(3).left();

                        if(mount.inaccuracy > 0)
                            h.add("[lightgray]" + Stat.inaccuracy.localized() + ": [white]" + mount.inaccuracy + " " + StatUnit.degrees.localized());

                        if(mount.range > 0){
                            h.row();
                            h.add("[lightgray]" + Stat.shootRange.localized() + ": [white]" + Strings.fixed(mount.range / Vars.tilesize, 1) + " " + StatUnit.blocks);
                        }

                        h.row();
                        h.add("[lightgray]" + Core.bundle.get("stat.prog-mats.ammo-shot") + ": [white]" + mount.ammoPerShot);
                        h.row();
                        h.add("[lightgray]" + Stat.reload.localized() + ": [white]" + Strings.autoFixed(60 / mount.reloadTime * mount.shots, 1));

                        h.row();
                        h.add("[lightgray]" + Stat.targetsAir.localized() + ": [white]" + (!mount.targetAir ? Core.bundle.get("no") : Core.bundle.get("yes")));
                        h.row();
                        h.add("[lightgray]" + Stat.targetsGround.localized() + ": [white]" + (!mount.targetGround ? Core.bundle.get("no") : Core.bundle.get("yes")));

                        BulletType type = mount.bullet;

                        if(type.damage > 0 && (type.collides || type.splashDamage <= 0)){
                            // this.sep(table, Core.bundle.format("bullet.damage", type.damage));
                            h.row();
                            h.add(Core.bundle.format("bullet.damage", type.damage));
                        }

                        if(type.splashDamage > 0){
                            // this.sep(table, Core.bundle.format("bullet.splashdamage", type.splashDamage, Strings.fixed(type.splashDamageRadius / Vars.tilesize, 1)));
                            h.row();
                            h.add(Core.bundle.format("bullet.splashdamage", type.splashDamage, Strings.fixed(type.splashDamageRadius / Vars.tilesize, 1)));
                        }

                        if(type.knockback > 0){
                            // this.sep(table, Core.bundle.format("bullet.knockback", Strings.fixed(type.knockback, 1)));
                            h.row();
                            h.add(Core.bundle.format("bullet.knockback", Strings.fixed(type.knockback, 1)));
                        }

                        if(type.healPercent > 0){
                            // this.sep(table, Core.bundle.format("bullet.healpercent", type.healPercent));
                            h.row();
                            h.add(Core.bundle.format("bullet.healpercent", type.healPercent));
                        }

                        if(type.pierce || type.pierceCap != -1){
                            // this.sep(table, type.pierceCap == -1 ? "@bullet.infinitepierce" : Core.bundle.format("bullet.pierce", type.pierceCap));
                            h.row();
                            h.add(type.pierceCap == -1 ? "@bullet.infinitepierce" : Core.bundle.format("bullet.pierce", type.pierceCap));
                        }

                        if(type.status == StatusEffects.burning || type.status == StatusEffects.melting || type.incendAmount > 0){
                            // this.sep(table, "@bullet.incendiary");
                            h.row();
                            h.add("@bullet.incendiary");
                        }

                        if(type.status == StatusEffects.freezing){
                            // this.sep(table, "@bullet.freezing");
                            h.row();
                            h.add("@bullet.freezing");
                        }

                        if(type.status == StatusEffects.tarred){
                            // this.sep(table, "@bullet.tarred");
                            h.row();
                            h.add("@bullet.tarred");
                        }

                        if(type.status == StatusEffects.sapped){
                            // this.sep(table, "@bullet.sapping");
                            h.row();
                            h.add("@bullet.sapping");
                        }

                        if(type.homingPower > 0.01){
                            // this.sep(table, "@bullet.homing");
                            h.row();
                            h.add("@bullet.homing");
                        }

                        if(type.lightning > 0){
                            // this.sep(table, "@bullet.shock");
                            h.row();
                            h.add("@bullet.shock");
                        }

                        if(type.fragBullet != null){
                            // this.sep(table, "@bullet.frag");
                            h.row();
                            h.add("@bullet.frag");
                        }

                        table.row();
                    }).padTop(-15).left();
                    table.row();
                }
            });
        };

        this.stats.add(Stat.weapons, wT);
    }

    public class MultiTurretBuild extends ItemTurretBuild {
        public Seq<Float> _reloads = new Seq<>();
        public Seq<Float> _heats = new Seq<>();
        public Seq<Float> _recoils = new Seq<>();
        public Seq<Float> _shotCounters = new Seq<>();
        public Seq<Float> _rotations = new Seq<>();
        public Seq<Boolean> _wasShootings = new Seq<>();
        public @Nullable Seq<Posc> _targets = new Seq<>();
        public Seq<Vec2> _targetPoss = new Seq<>();
        public float _heat;


        @Override
        public void created(){
            super.created();
            for(int i = 0; i < mounts.size; i++){
                _reloads.add(0f);
                _heats.add(0f);
                _recoils.add(0f);
                _shotCounters.add(0f);
                _rotations.add(90f);
                _targets.add(null);
                _targetPoss.add(new Vec2());
                _wasShootings.add(false);
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
                Lines.dashCircle(loc[0], loc[1], mounts.get(i).range);
                Lines.stroke(1, this.team.color);
                Draw.alpha(fade);
                Lines.dashCircle(loc[0], loc[1], mounts.get(i).range);

                Draw.color(this.team.color, fade);
                Draw.rect(turrets.get(i)[3], loc[2], loc[3], this._rotations.get(i) - 90);
                Draw.reset();
            }
        }

        public float[] mountLocations(int mount){
            Tmp.v1.trns(this.rotation - 90, mounts.get(mount).x, mounts.get(mount).y - recoil);
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

            for(int i = 0; i < mounts.size; i++){
                if(!Vars.headless){
                    float[] loc = this.mountLocations(i);

                    if(loopSounds.get(i) != null)
                        loopSounds.get(i).update(loc[4], loc[5], _wasShootings.get(i));
                }
            }
        }

        public float __heat;

        @Override
        public void updateTile() {
            super.updateTile();

            for(int i = 0; i < mounts.size; i++){
                _wasShootings.set(i, false);
                _recoils.set(i, Mathf.lerpDelta(_recoils.get(i), 0, mounts.get(i).restitution));
                _heats.set(i, Mathf.lerpDelta(_heats.get(i), 0, mounts.get(i).cooldown));

                if(!validateMountTarget(i)) _targets.set(i, null);
            }

            if(this.hasAmmo()){
                __heat -= edelta();

                if(__heat <= 0.001){
                    for(int i = 0; i < mounts.size; i++){
                        this.mountLocations(i);

                        _targets.set(i ,findMountTargets(i));
                    }
                    __heat = 16;
                }

                for(int i = 0; i < mounts.size; i++){
                    float[] loc = this.mountLocations(i);

                    if(this.validateMountTarget(i)){
                        boolean canShoot = true;

                        if(isControlled()){ //player behavior
                            _targetPoss.get(i).set(unit().aimX, unit().aimY);
                            canShoot = unit().isShooting;

                        }else if(this.logicControlled()){ //logic behavior
                            _targetPoss.set(i, targetPos);
                            canShoot = logicShooting;

                        }else{ //default AI behavior
                            mountTargetPosition(i, _targets.get(i), loc[0], loc[1]);
                            if(Float.isNaN(_rotations.get(i))) _rotations.set(i, 0f);
                        }

                        float targetRot = Angles.angle(loc[0], loc[1], _targetPoss.get(i).x, _targetPoss.get(i).y);

                        this.mountTurnToTarget(i, targetRot);

                        if(Angles.angleDist(_rotations.get(i), targetRot) < mounts.get(i).shootCone && canShoot){
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
            if((angle > to && Angles.backwardDistance(angle, to) > Angles.forwardDistance(angle, to)) || (angle < to && Angles.backwardDistance(angle, to) < Angles.forwardDistance(angle, to)))
                allRot = -speed;

            for(int i = 0; i < mounts.size; i++)
                _rotations.set(i, (_rotations.get(i) + allRot) % 360);
        }

        public void mountTurnToTarget(int mount, float target){
            _rotations.set(mount, Angles.moveToward(_rotations.get(mount), target, mounts.get(mount).rotateSpeed * delta() * baseReloadSpeed()));
        }

        public Posc findMountTargets(int mount){
            float[] loc = this.mountLocations(mount);

            if(mounts.get(mount).targetAir && !mounts.get(mount).targetGround)
                 return Units.bestEnemy(this.team, loc[0], loc[1], mounts.get(mount).range, e -> !e.dead && !e.isGrounded(), mounts.get(mount).unitSort);
            else return Units.bestTarget(this.team, loc[0], loc[1], mounts.get(mount).range, e -> !e.dead && (e.isGrounded() || mounts.get(mount).targetAir) && (!e.isGrounded() || mounts.get(mount).targetGround), b -> true, mounts.get(mount).unitSort);

        }

        public boolean validateMountTarget(int mount){
            float[] loc = mountLocations(mount);

            return !Units.invalidateTarget(_targets.get(mount), team, loc[0], loc[1]) || isControlled() || logicControlled();
        }

        public void mountTargetPosition(int mount, Posc pos, float x, float y){
            if(!hasAmmo()) return;

            BulletType bullet = mounts.get(mount).bullet;
            float speed = bullet.speed;

            if(speed < 0.1) speed = 9999999;

            _targetPoss.get(mount).set(Predict.intercept(Tmp.v4.set(x, y), pos, speed));

            if(_targetPoss.get(mount).isZero()) _targetPoss.get(mount).set(_targets.get(mount));

        }

        public void updateMountShooting(int mount){
            if(_reloads.get(mount) >= mounts.get(mount).reloadTime){
            BulletType type = mounts.get(mount).bullet;

                mountShoot(mount, type);

                _reloads.set(mount, 0f);
            }else
                _reloads.set(mount,_reloads.get(mount) + delta() * mounts.get(mount).bullet.reloadMultiplier * baseReloadSpeed());

        }

        @Override
        protected void updateCooling() {
            super.updateCooling();

            float maxUsed = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount / mounts.size;

            Liquid liquid = liquids.current();

            for(int i = 0; i < mounts.size; i++){
                float used = Math.min(Math.min(liquids.get(liquid), maxUsed * Time.delta), Math.max(0, ((mounts.get(i).reloadTime - _reloads.get(i)) / coolantMultiplier) / liquid.heatCapacity)) * baseReloadSpeed();
                _reloads.set(i, _reloads.get(i) + used * liquid.heatCapacity * coolantMultiplier);

                liquids.remove(liquid, used);

                float[] loc = mountLocations(i);

                if(Mathf.chance(0.06 / mounts.size * used)) mounts.get(i).coolEffect.at(loc[0] + Mathf.range(mounts.get(i).width), loc[1] + Mathf.range(mounts.get(i).height));
            }
        }

        public void mountShoot(int mount, BulletType type){
            for(int j = 0; j < mounts.get(mount).shots; j++){
                int spreadAmount = j;

                Time.run(mounts.get(mount).burstSpacing * j, () -> {
                    if(!this.isValid() || !this.hasAmmo()) return;

                    float[] loc = this.mountLocations(mount);

                    if(mounts.get(mount).shootShake > 0) Effect.shake(mounts.get(mount).shootShake, mounts.get(mount).shootShake, loc[4], loc[(int) y]);


                    Effect fsHootEffect = mounts.get(mount).shootEffect == Fx.none ? type.shootEffect : mounts.get(mount).shootEffect;
                    Effect fsMockedEffect = mounts.get(mount).smokeEffect == Fx.none ? type.smokeEffect : mounts.get(mount).smokeEffect;

                    fsHootEffect.at(loc[4], loc[5], _rotations.get(mount));
                    fsMockedEffect.at(loc[4], loc[5], _rotations.get(mount));

                    mounts.get(mount).shootSound.at(loc[4], loc[5], Mathf.random(0.9f, 1.1f));

                    _recoils.set(mount ,mounts.get(mount).recoilAmount);
                    _heats.set(mount ,1f);

                    mountUseAmmo(mount);
                    if(mounts.get(mount).loopSound != Sounds.none)
                        loopSounds.get(mount).update(loc[4], loc[5], true);

                    float velScl = 1 + Mathf.range(mounts.get(mount).velocityInaccuracy);
                    float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(loc[4], loc[5], _targetPoss.get(mount).x, _targetPoss.get(mount).y) / type.range(), mounts.get(mount).minRange / type.range(), mounts.get(mount).range / type.range()) : 1;
                    float angle = _rotations.get(mount) + Mathf.range(mounts.get(mount).inaccuracy + type.inaccuracy) + (spreadAmount - (mounts.get(mount).shots / 2f)) * mounts.get(mount).spread;

                    type.create(this, this.team, loc[4], loc[5], angle, velScl, lifeScl);

                    if(mounts.get(mount).sequential) _shotCounters.set(mount, _shotCounters.get(mount)+1);
                });
            }

            if(!mounts.get(mount).sequential) _shotCounters.set(mount, _shotCounters.get(mount)+1);

        }
        public BulletType mountUseAmmo(int mount){
            if(cheating()) return peekAmmo();

            AmmoEntry entry = ammo.peek();
            entry.amount -= mounts.get(mount).ammoPerShot;
            if(entry.amount <= 0) ammo.pop();
            totalAmmo -= mounts.get(mount).ammoPerShot;
            totalAmmo = (int) Mathf.maxZero(totalAmmo);
            mountEjectEffects(mount);
            return entry.type();
        }

        public void mountEjectEffects(int mount){
            if(!this.isValid()) return;

            int side = mounts.get(mount).altEject ? Mathf.signs[(int) (_shotCounters.get(mount) % 2)] : mounts.get(mount).ejectRight ? 1 : 0;
            float[] loc = this.mountLocations(mount);

            mounts.get(mount).ejectEffect.at(loc[4], loc[5], _rotations.get(mount) * side);
        }
    }
}
