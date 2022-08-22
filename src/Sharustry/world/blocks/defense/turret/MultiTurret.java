package Sharustry.world.blocks.defense.turret;

import Sharustry.content.STurretMounts;
import Sharustry.entities.skills.TurretSkill;
import Sharustry.ui.MultiImageLabel;
import Sharustry.world.blocks.defense.turret.mounts.*;
import arc.*;
import arc.func.Func;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.Vec2;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Button;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.Vars;
import mindustry.content.*;
import mindustry.core.UI;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.LAccess;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.draw.DrawTurret;
import mindustry.world.meta.*;

import static arc.Core.input;
import static arc.Core.scene;
import static mindustry.Vars.*;

public class MultiTurret extends TemplatedTurret {
    public float rangeTime = 80;
    public float fadeTime = 20;
    public float totalRangeTime;
    public Seq<TurretSkill<MultiTurretBuild>> skills = new Seq<>();
    public Seq<MountTurretType> mountTypes = new Seq<>();
    public Seq<Float[]> mountOffsets = new Seq<>();

    public MultiTurret(String name){
        super(name);
    }

    public void addMountTurret(MountTurretType mount, float offsetX, float offsetY) {
        mountTypes.add(mount);
        mountOffsets.add(new Float[]{offsetX, offsetY});
        if(!hasItems && mount instanceof ItemMountTurretType) hasItems = true;
        if(!hasLiquids && mount instanceof LiquidMountTurretType) hasLiquids = true;
        if(!hasPower && consumesPower) hasPower = true;
        if(!configurable && mount instanceof MassMountTurretType) configurable = true;

        this.totalRangeTime = rangeTime * mountTypes.size;
    }

    @Override
    public void load(){
        super.load();

        region = Core.atlas.find(name + "-baseTurret");
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        for(int i = 0; i < mountTypes.size; i++) mountTypes.get(i).drawPlace(this, i, x, y, rotation, valid);
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{
            ((DrawTurret)this.drawer).base,
            Core.atlas.find(name + "-icon")
        };
    }

    void addStatS(Table w){
        w.left();
        w.add(localizedName).right().top().row();
        w.image(region).size(60).scaling(Scaling.bounded).right().top();
        w.table(Tex.underline, h -> {
            h.left().defaults().padRight(3).left();

            h.add("[lightgray]" + Stat.shootRange.localized() + ": [white]" + Strings.fixed((range) / tilesize, 1) + " " + StatUnit.blocks).row();
            h.add("[lightgray]" + Stat.reload.localized() + ": [white]" + Strings.autoFixed(60 / reload * shoot.shots, 1)).row();
            h.add("[lightgray]" + Stat.targetsAir.localized() + ": [white]" + (!(targetAir) ? Core.bundle.get("no") : Core.bundle.get("yes"))).row();
            h.add("[lightgray]" + Stat.targetsGround.localized() + ": [white]" + (!(targetGround) ? Core.bundle.get("no") : Core.bundle.get("yes"))).row();
            if(inaccuracy > 0) h.add("[lightgray]" + Stat.inaccuracy.localized() + ": [white]" + (inaccuracy) + " " + StatUnit.degrees.localized()).row();
            if(shoot.firstShotDelay > 0.001f) h.add("[lightgray]" + Core.bundle.get("stat.shar.shoot.firstShotDelay") + ": [white]" + Mathf.round(shoot.firstShotDelay/60, 100) + " " + Core.bundle.format("stat.shar.seconds")).row();
            if(isItemTurret()) h.add("[lightgray]" + Core.bundle.get("stat.shar.ammo-shot") + ": [white]" + (ammoPerShot)).row();

            h.row();

            h.table(b -> {
                int ii = 0;
                for(BulletType bullet : (Iterable<? extends BulletType>) (isItemTurret() ? ammoTypes.values() : isLiquidTurret() ? liqAmmoTypes.values() : new BulletType[]{isPowerTurret() ? shootType : null})) {
                    if(bullet == null) continue;

                    ii ++;
                    b.add(new MultiImageLabel() {{

                        if(isItemTurret()) {
                            @Nullable Item item = ammoTypes.findKey(bullet, false);
                            if(item != null) add(item.uiIcon, item.localizedName);
                        }
                        if(isLiquidTurret()) {
                            @Nullable Liquid liquid = liqAmmoTypes.findKey(bullet, false);
                            if(liquid != null) add(liquid.uiIcon, liquid.localizedName);
                        }
                        if(isPowerTurret()) {
                            add(Icon.power.getRegion(), "Power");
                        }
                    }}).size(8 * 4).padRight(4).right().top();

                    b.table(Tex.underline, e -> {
                        e.left().defaults().padRight(3).left();
                            if(bullet.damage > 0 && (bullet.collides || bullet.splashDamage <= 0)) e.add(Core.bundle.format("bullet.damage", bullet.damage)).row();
                            if(bullet.buildingDamageMultiplier != 1) e.add(Core.bundle.format("bullet.buildingdamage", Strings.fixed((int)(bullet.buildingDamageMultiplier * 100),1))).row();
                            if(bullet.splashDamage > 0) e.add(Core.bundle.format("bullet.splashdamage", bullet.splashDamage, Strings.fixed(bullet.splashDamageRadius / tilesize, 1))).row();
                            if(bullet.ammoMultiplier > 0 && !(bullet instanceof LiquidBulletType) && !Mathf.equal(bullet.ammoMultiplier, 1f)) e.add(Core.bundle.format("bullet.multiplier", Strings.fixed(bullet.ammoMultiplier, 1))).row();
                            if(!Mathf.equal(bullet.reloadMultiplier, 1f)) e.add(Core.bundle.format("bullet.reloadCounter", bullet.reloadMultiplier)).row();
                            if(bullet.knockback > 0) e.add(Core.bundle.format("bullet.knockback", Strings.fixed(bullet.knockback, 1))).row();
                            if(bullet.healPercent > 0) e.add(Core.bundle.format("bullet.healpercent", bullet.healPercent)).row();
                            if(bullet.pierce || bullet.pierceCap != -1) e.add(bullet.pierceCap == -1 ? "@bullet.infinitepierce" : Core.bundle.format("bullet.pierce", bullet.pierceCap)).row();
                            if(bullet.status == StatusEffects.burning || bullet.status == StatusEffects.melting || bullet.incendAmount > 0) e.add("@bullet.incendiary").row();
                            if(bullet.status == StatusEffects.freezing) e.add("@bullet.freezing").row();
                            if(bullet.status == StatusEffects.tarred) e.add("@bullet.tarred").row();
                            if(bullet.status == StatusEffects.sapped) e.add("@bullet.sapping").row();
                            if(bullet.homingPower > 0.01) e.add("@bullet.homing").row();
                            if(bullet.lightning > 0) e.add("@bullet.shock").row();
                            if(bullet.fragBullet != null) e.add("@bullet.frag").row();
                    }).padTop(-9).left();

                    if(ii % 4 == 0) b.row();
                }
            });
            h.row();
        }).padTop(-15).left();
        w.row();
    }
    //TODO: add setStats on mounts
    @Override
    public void setStats(){
        super.setStats();

        for(TurretSkill skill : skills) {
            stats.add(Stat.abilities, table -> {
                table.table(Tex.underline, e -> {
                    e.left().defaults().padRight(3).left();
                    e.add(skill.name).fillX().row();
                    e.add(Core.bundle.format("stat.shar.skillreload", ""+skill.maxCount)).row();
                    skill.stats(e);
                    e.row();
                    e.add("[lightgray]"+ Core.bundle.get("category.purpose") + ": " + skill.description);
                }).left();
            });
        }

        stats.add(Stat.weapons, table -> {
            table.left();
            table.add("[lightgray]" + Core.bundle.get("stat.shar.base-t")).fillX().padLeft(24).row();

            //Base Turret
            table.table(null, w -> {
                addStatS(w);
                table.row().left();
            }).left().row();
            table.add("[lightgray]" + Core.bundle.get("stat.shar.mini-t")).fillX().padLeft(24);

            //Mounts
            table.table(null, w -> {
                for(MountTurretType mount : mountTypes){
                    mount.addStats(w);
                    table.row();
                }
            });
        });
    }

    public class MultiTurretBuild extends TemplatedTurretBuild {
        public IntSeq shotCounters = new IntSeq();
        public Seq<MountTurretType.MountTurret> mounts = new Seq<>();
        public MassMountTurretType.MassMountTurret selectedMassMount;

        @Override
        public void remove(){
            super.remove();
            if(sound != null) sound.stop();
        }

        @Override
        public void created(){
            super.created();

            for(int i = 0; i < skills.size; i++) shotCounters.add(0);
            for(int i = 0; i < mountTypes.size; i++) mounts.add(mountTypes.get(i).create(((MultiTurret)block), this, i, mountOffsets.get(i)[0], mountOffsets.get(i)[1]));
        }

        public MountTurretType.MountTurret addMount(MountTurretType mountType, float x, float y){
            MountTurretType.MountTurret mount = mountType.create((MultiTurret)block, this, mounts.size, x, y);
            mounts.add(mount);
            return mount;
        }
        public float baseReloadSpeed() {return super.baseReloadSpeed();}
        public boolean hasMass(){
            return mounts.contains(m -> m.type instanceof MassMountTurretType);
        }
        @Override
        public void displayConsumption(Table table1){
            if(hasMass()) table1.table(table -> table.table(scene.getStyle(Button.ButtonStyle.class).up, c -> {
                int q = 0;
                for(int i = 0; i < Vars.content.items().size; i++) {
                    q++;
                    final int hh = q;
                    final int h1 = i;
                    Item item = Vars.content.items().get(h1);

                    c.add(new Stack(){{
                        add(new Table(o -> {
                            o.left();
                            o.image(item.uiIcon).size(32f);
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
                    if(hh % 6 == 0) c.row();
                }
            }).center()).center();

            table1.row();
            table1.table(scene.getStyle(Button.ButtonStyle.class).up, t -> {
                for(MountTurretType.MountTurret mount : mounts) {
                    t.center();
                    mount.display(t);
                }
            });
        }

        @Override
        public void displayBars(Table bars){
            super.displayBars(bars);
            int i = 0;
            for(TurretSkill skill : skills) {
                final int j = i;
                bars.add(new Bar(
                        () -> Core.bundle.format("bar.shar-skillReload") + shotCounters.get(j) + " / " + skill.maxCount,
                        () -> Pal.lancerLaser.cpy().lerp(Pal.place, Mathf.absin(Time.time, 20, (shotCounters.get(j) / (skill.maxCount * 2.5f)))),
                        () -> (shotCounters.get(j) / (skill.maxCount * 1f)))).growX().row();
                i++;
            }
            bars.row();
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            for(MountTurretType.MountTurret mount : mounts) mount.type.drawer.drawSelect(mount);
        }

        @Override
        public void drawConfigure(){
            for(MountTurretType.MountTurret mount : mounts) mount.drawConfigure();
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
            for(MountTurretType.MountTurret mount : mounts) mount.handleItem(item);

            super.handleItem(source, item);
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return (ammoTypes.get(item) != null && totalAmmo + ammoTypes.get(item).ammoMultiplier <= maxAmmo)
                    || mounts.contains(mount -> mount.acceptItem(item));
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            boolean h = false;
            for(MountTurretType.MountTurret mount : mounts) {
                h = mount.acceptLiquid(liquid);
                if(h) break;
            }
            return h;
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            int past = 0;

            for(MountTurretType.MountTurret mount : mounts) past = Math.min(past, mount.acceptStack(item, amount));
            return Math.min(past, amount);
        }

        @Override
        public BlockStatus status() {
            BlockStatus status = BlockStatus.noInput;
            for(MountTurretType.MountTurret mount : mounts) status = mount.status();
            return status;
        }

        @Override
        public void draw() {
            super.draw();

            for(MountTurretType.MountTurret mount : mounts) mount.draw();

            if(selectedMassMount != null) {
                Draw.color(team.color);
                Draw.alpha(Mathf.absin(10, 1));
                Draw.rect(selectedMassMount.type.drawer.mask, selectedMassMount.x, selectedMassMount.y, selectedMassMount.drawrot());
                if(world.build(selectedMassMount.link) instanceof MultiTurretBuild multi && multi.mounts.get(selectedMassMount.linkIndex) instanceof MassMountTurretType.MassMountTurret mass && mass.linkValid()) {
                    Draw.rect(mass.type.drawer.mask, mass.x, mass.y, mass.drawrot());
                }
                Vec2 mouse = input.mouseWorld();
                Building build = world.build((int)mouse.x/8, (int)mouse.y/8);
                if(build instanceof MultiTurretBuild multi) {
                    MassMountTurretType.MassMountTurret mass = (MassMountTurretType.MassMountTurret) multi.mounts.find(mount -> mount instanceof MassMountTurretType.MassMountTurret && Math.abs(mouse.x - mount.x) < 4 && Math.abs(mouse.y - mount.y) < 4);
                    if(mass != null) {
                        Draw.color(team.color);
                        Drawf.dashLine(Pal.accent, selectedMassMount.x, selectedMassMount.y, mass.x, mass.y);
                        Draw.alpha(Mathf.absin(10, 1));
                        Draw.rect(mass.type.drawer.mask, mass.x, mass.y, mass.drawrot());
                        Draw.color();
                    }
                    else Drawf.dashLine(Pal.accent, selectedMassMount.x, selectedMassMount.y, mouse.x, mouse.y);
                } else {
                    Drawf.dashLine(Pal.accent, selectedMassMount.x, selectedMassMount.y, mouse.x, mouse.y);
                }
            }
        }

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4){
            if(type == LAccess.shoot && !unit.isPlayer()){
                for(MountTurretType.MountTurret mount : mounts) mount.control(type, p1, p2);
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
                    for(MountTurretType.MountTurret mount : mounts) mount.control(type, p1);
                }
            }

            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public void update() {
            super.update();
            for(MountTurretType.MountTurret mount : mounts) mount.update();
        }

        @Override
        public void updateTile() {
            super.updateTile();
            for(MountTurretType.MountTurret mount : mounts) mount.updateTile();
        }

        public void handlePayload(Bullet bullet, DriverBulletData data){
            if(!hasMass()) return;

            for(MountTurretType.MountTurret mount : mounts) mount.handlePayload(bullet, data);
        }

        @Override
        public void removeFromProximity() {
            for(MountTurretType.MountTurret mount : mounts) mount.removeFromProximity();
            super.removeFromProximity();
        }

        @Override
        public Object config(){
            for(int i = 0; i < mounts.size; i++){
                if(mounts.get(i) instanceof MassMountTurretType.MassMountTurret mass)
                    return ObjectMap.of(i, mass.link);
            }
            return null;
        }

        @Override
        public void onConfigureClosed() {
            super.onConfigureClosed();
            selectedMassMount = null;
        }

        @Override
        public void buildConfiguration(Table table) {
            super.buildConfiguration(table);
            ObjectMap<Button, MassMountTurretType.MassMountTurret> buttons = new ObjectMap<>();
            for(MountTurretType.MountTurret mount : mounts) {
                if(!(mount instanceof MassMountTurretType.MassMountTurret mass)) continue;
                Button button = new Button();
                button.setStyle(Styles.clearTogglei);
                button.add(new Image(mount.type.region){
                    final TextureRegionDrawable mask = new TextureRegionDrawable(mount.type.drawer.mask);
                    @Override
                    public void draw() {
                        if(((MassMountTurretType.MassMountTurret) mount).linkValid()) {
                            Draw.color(Pal.accent);
                            mask.draw(x + imageX, y + imageY, imageWidth * scaleX * 1.05f, imageHeight * scaleY * 1.05f);
                            Draw.rect(mount.type.drawer.mask, x, y);
                            Draw.color();
                        }
                        super.draw();
                    }
                });
                button.setChecked(selectedMassMount == mass);
                button.clicked(() -> {
                    if(selectedMassMount == buttons.get(button)) selectedMassMount = null;
                    else selectedMassMount = buttons.get(button);
                    for(ObjectMap.Entry<Button, MassMountTurretType.MassMountTurret> entry : buttons) {
                        entry.key.setChecked(selectedMassMount == entry.value);
                    }
                });
                table.add(button).size(5 * 8f);
                buttons.put(button, mass);
            };
        }

        @Override
        public boolean onConfigureBuildTapped(Building other){
            if(selectedMassMount != null && other.team == team && other instanceof MultiTurretBuild multi) {
                if(other == this) {
                    selectedMassMount.link = -1;
                    selectedMassMount.linkIndex = -1;
                    deselect();
                    return false;
                }

                MassMountTurretType.MassMountTurret mass = (MassMountTurretType.MassMountTurret) multi.mounts.find(mount -> mount instanceof MassMountTurretType.MassMountTurret && Math.abs(input.mouseWorldX() - mount.x) < 4 && Math.abs(input.mouseWorldY() - mount.y) < 4);
                if(mass != null && other.dst(mass.x, mass.y) <= mass.type.range) {
                    if (selectedMassMount.link == other.tile.pos()) {
                        selectedMassMount.link = -1;
                        selectedMassMount.linkIndex = -1;
                    } else {
                        selectedMassMount.link = other.tile.pos();
                        selectedMassMount.linkIndex = mass.mountIndex;
                    }
                    return false;
                }
            }

            return true;
        }

        @Override
        public boolean shouldTurn(){
            return !charging() && !mounts.contains(mount -> !mount.shouldTurn());
        }

        @Override
        protected void turnToTarget(float target) {
            super.turnToTarget(target);

            for(MountTurretType.MountTurret mount : mounts) mount.turnToTarget(target);
        }

        @Override
        protected void updateCooling() {
            super.updateCooling();

            for(MountTurretType.MountTurret mount : mounts) mount.updateCooling();
        }

        @Override
        protected void shoot(BulletType type) {
            super.shoot(type);

            for(int i = 0; i < skills.size; i++) {
                shotCounters.incr(i, 1);
                if(shotCounters.get(i) >= skills.get(i).maxCount) {
                    shotCounters.set(i, 0);
                    skills.get(i).active(this);
                }
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);

            for(int i = 0; i < skills.size; i++)
                write.i(shotCounters.get(i));
            write.i(mounts.size);
            for(int i = 0; i < mountTypes.size; i++) mounts.get(i).write(write);
            for(int i = mountTypes.size; i < mounts.size; i++){
                write.i(STurretMounts.mounttypes.indexOf(mounts.get(i).type));
                write.f(mounts.get(i).xOffset);
                write.f(mounts.get(i).yOffset);
                mounts.get(i).write(write);
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            for(int i = 0; i < skills.size; i++) shotCounters.set(i, read.i());
            int amount = read.i();
            for(int i = 0; i < mountTypes.size; i++) mounts.get(i).read(read, revision);
            for(int i = 0; i < amount - mountTypes.size; i++) {
                MountTurretType type = STurretMounts.mounttypes.get(read.i());
                float x = read.f();
                float y = read.f();
                if(type != null) addMount(type, x, y).read(read, revision);
            }
        }
    }
}