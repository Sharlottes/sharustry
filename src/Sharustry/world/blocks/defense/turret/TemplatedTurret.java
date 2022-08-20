package Sharustry.world.blocks.defense.turret;

import arc.*;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.Nullable;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.Fires;
import mindustry.entities.Units;
import mindustry.entities.bullet.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.LAccess;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.consumers.*;
import mindustry.world.draw.DrawTurret;
import mindustry.world.meta.*;

import java.util.Objects;

import static mindustry.Vars.*;

public class TemplatedTurret extends Turret {
    public float minRanged;
    public String ammoType; //should be item(ItemTurret) or power(PowerTurret) or liquid(LiquidTurret).

    public ObjectMap<Item, BulletType> ammoTypes = new ObjectMap<>();

    public BulletType shootType;
    public float powerUse = 1f;

    public ObjectMap<Liquid, BulletType> liqAmmoTypes = new ObjectMap<>();
    public TextureRegion liquidRegion;
    public TextureRegion topRegion;
    TextureRegion chargeRegion;
    public boolean extinguish = true;

    public TemplatedTurret(String name){
        super(name);
        if(Objects.equals(ammoType, "item")) hasItems = true;
        if(Objects.equals(ammoType, "power")) hasPower = true;
        if(Objects.equals(ammoType, "liquid")){
            hasLiquids = true;
            loopSound = Sounds.spray;
            shootSound = Sounds.none;
            //outlinedIcon = 1; when next release
        }
    }

    @Override
    public void load() {
        super.load();
        liquidRegion = Core.atlas.find("shar-" + name + "-liquid");
        topRegion = Core.atlas.find("shar-" + name + "-top");
        chargeRegion = Core.atlas.find(name + "-charge");
    }

    /** Initializes accepted ammo map. Format: [item1, bullet1, item2, bullet2...] */
    public void ammo(Object... objects){
        if(Objects.equals(ammoType, "item")) ammoTypes = OrderedMap.of(objects);
        if(Objects.equals(ammoType, "liquid")) liqAmmoTypes = OrderedMap.of(objects);
    }

    @Override
    public void setStats(){
        super.setStats();

        if(Objects.equals(ammoType, "item")) {
            stats.remove(Stat.itemCapacity);
            stats.add(Stat.ammo, StatValues.ammo(ammoTypes));
        }
        if(Objects.equals(ammoType, "power")) stats.add(Stat.damage, shootType.damage, StatUnit.none);
        if(Objects.equals(ammoType, "liquid")) stats.add(Stat.ammo, StatValues.ammo(liqAmmoTypes));
    }

    @Override
    public void init(){
        if(Objects.equals(ammoType, "item")) consume(new ConsumeItemFilter(i -> ammoTypes.containsKey(i)) {
            @Override
            public void build(Building tile, Table table) {
                MultiReqImage image = new MultiReqImage();
                content.items().each(
                    i -> filter.get(i) && i.unlockedNow(),
                    item -> image.add(new ReqImage(new ItemImage(item.uiIcon, 1),
                        () -> tile instanceof TemplatedTurretBuild && !((TemplatedTurretBuild) tile).ammo.isEmpty() && ((ItemEntry) ((TemplatedTurretBuild) tile).ammo.peek()).item == item)
                    )
                );

                table.add(image).size(8 * 4);
            }

            @Override
            public float efficiency(Building entity) {
                //valid when there's any ammo in the turret
                return entity instanceof TemplatedTurretBuild && !((TemplatedTurretBuild) entity).ammo.isEmpty() ? super.efficiency(entity) : 0;
            }

            @Override
            public void display(Stats stats) {
                //don't display
            }
        });

        if(Objects.equals(ammoType, "power")) consumePowerCond(powerUse, TurretBuild::isActive);

        if(Objects.equals(ammoType, "liquid")) consume(new ConsumeLiquidFilter(i -> liqAmmoTypes.containsKey(i), 1f){
            @Override
            public float efficiency(Building entity){
                return entity.liquids.currentAmount() > 0.001f ? super.efficiency(entity) : 0;
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
    public TextureRegion[] icons(){
        if(Objects.equals(ammoType, "liquid") && topRegion.found()) return new TextureRegion[]{region, topRegion};
        return super.icons();
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        if(minRanged > 0 && player != null){
            Drawf.dashCircle(x, y, minRanged, Vars.player.team().color.cpy().lerp(Pal.lancerLaser, Mathf.sin(Time.time * 0.05f)));
        }
    }

    public class TemplatedTurretBuild extends TurretBuild {
        float charge;
        float heat = 0f;

        @Override
        public void handleBullet(@Nullable Bullet bullet, float offsetX, float offsetY, float angleOffset){ }
        @Override
        public void drawSelect() {
            super.drawSelect();

            if(minRanged > 0){
                Drawf.dashCircle(x, y, minRanged, team.color.cpy().lerp(Pal.lancerLaser, Mathf.sin(Time.time * 0.05f)));
            }
        }

        @Override
        public void draw(){
            super.draw();
            if(charging()) {
                Draw.color(Tmp.c1.set(Color.white).lerp(Pal.lancerLaser, 1-heat/shoot.firstShotDelay).a(0.5f+heat/shoot.firstShotDelay/2));
                Tmp.v1.set(x, y).trns(rotation, -recoil);
                Draw.rect(chargeRegion, x+Tmp.v1.x, y+Tmp.v1.y, rotation-90);
                Draw.reset();
            }
        }

        @Override
        public boolean shouldActiveSound(){
            if(Objects.equals(ammoType, "liquid")) return wasShooting;
            else return super.shouldActiveSound();
        }

        @Override
        public void onProximityAdded(){
            super.onProximityAdded();

            if(Objects.equals(ammoType, "item")) if(cheating() && ammo.size > 0) handleItem(this, ammoTypes.entries().next().key);
        }

        @Override
        public void updateTile(){
            if(Objects.equals(ammoType, "item")) unit.ammo((float)unit.type().ammoCapacity * totalAmmo / maxAmmo);
            if(Objects.equals(ammoType, "power")) unit.ammo(power.status * unit.type().ammoCapacity);
            if(Objects.equals(ammoType, "liquid")) unit.ammo(unit.type().ammoCapacity * liquids.currentAmount() / liquidCapacity);

            if(charging()) charge = Mathf.clamp(charge + Time.delta / shoot.firstShotDelay);
            else charge = 0;
            if(charging()) heat = Time.delta;

            super.updateTile();
        }

        @Override
        protected void findTarget(){
            if(Objects.equals(ammoType, "liquid") && extinguish && liquids.current().canExtinguish()){
                int tr = (int)(range / tilesize);
                for(int x = -tr; x <= tr; x++) for(int y = -tr; y <= tr; y++){
                    Tile other = world.tileWorld(x + tile.x, y + tile.y);

                    if(other != null && Fires.has(x + tile.x, y + tile.y) && (other.build == null || other.team() == team)){
                        target = Fires.get(x + tile.x, y + tile.y);
                        return;
                    }
                }
            }

            if(targetAir && !targetGround){
                target = Units.bestEnemy(team, x, y, range, e -> !e.dead() && !e.isGrounded() && !e.within(x, y, minRanged), unitSort);
            }else{
                target = Units.bestTarget(team, x, y, range, e -> !e.dead() && (e.isGrounded() || targetAir) && (!e.isGrounded() || targetGround) && !e.within(x, y, minRanged), b -> targetGround && !b.within(x, y, minRanged), unitSort);

                if(target == null && canHeal()){
                    target = Units.findAllyTile(team, x, y, range, b -> b.damaged() && b != this);
                }
            }
        }

        @Override
        public double sense(LAccess sensor){
            if(Objects.equals(ammoType, "power")){
                if(sensor == LAccess.ammo) return power.status;
                if(sensor == LAccess.ammoCapacity) return 1;
                else return super.sense(sensor);
            }

            else return super.sense(sensor);
        }

        @Override
        public BulletType useAmmo(){
            if(Objects.equals(ammoType, "power")) return shootType;
            else if(Objects.equals(ammoType, "liquid")){
                if(cheating()) return liqAmmoTypes.get(liquids.current());
                BulletType type = liqAmmoTypes.get(liquids.current());
                liquids.remove(liquids.current(), 1f / type.ammoMultiplier);
                return type;
            }

            else return super.useAmmo();
        }

        @Override
        public boolean hasAmmo(){
            if(Objects.equals(ammoType, "power")) return true;
            else if(Objects.equals(ammoType, "liquid")) return liqAmmoTypes.get(liquids.current()) != null && liquids.currentAmount() >= 1f / liqAmmoTypes.get(liquids.current()).ammoMultiplier;
            else return super.hasAmmo();
        }

        @Override
        public BulletType peekAmmo(){
            if(Objects.equals(ammoType, "power")) return shootType;
            else if(Objects.equals(ammoType, "liquid")) return liqAmmoTypes.get(liquids.current());
            else return super.peekAmmo();
        }

        @Override
        public void displayBars(Table bars){
            super.displayBars(bars);

            if(Objects.equals(ammoType, "item")) {
                bars.add(new Bar("stat.ammo", Pal.ammo, () -> (float) totalAmmo / maxAmmo)).growX();
                bars.row();
            }

            bars.add(new Bar(
                    () -> {
                        float value = Mathf.clamp(reloadCounter / reload) * 100f;
                        return Core.bundle.format("bar.shar-reloadCounter", Strings.fixed(value, (Math.abs((int)value - value) <= 0.001f ? 0 : Math.abs((int)(value * 10) - value * 10) <= 0.001f ? 1 : 2)));
                    },
                    () -> Pal.accent.cpy().lerp(Color.orange, reloadCounter / reload),
                    () -> reloadCounter / reload)).growX();
            bars.row();

            if(shoot.firstShotDelay >= 0.001) bars.add(new Bar(
                    () -> {
                        float value = Mathf.clamp(charge) * 100f;
                        return Core.bundle.format("bar.shar-charge", Strings.fixed(value, (Math.abs((int)value - value) <= 0.001f ? 0 : Math.abs((int)(value * 10) - value * 10) <= 0.001f ? 1 : 2)));
                    },
                    () -> Pal.surge.cpy().lerp(Pal.accent, charge / shoot.firstShotDelay),
                    () -> charge)).growX();
            bars.row();
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            if(Objects.equals(ammoType, "item")) {
                BulletType type = ammoTypes.get(item);

                if(type == null) return 0;

                return Math.min((int) ((maxAmmo - totalAmmo) / ammoTypes.get(item).ammoMultiplier), amount);
            } else return super.acceptStack(item, amount, source);
        }

        @Override
        public void handleStack(Item item, int amount, Teamc source){
            if(Objects.equals(ammoType, "item")) for(int i = 0; i < amount; i++) handleItem(null, item);
            else super.handleStack(item, amount, source);
        }

        @Override
        public int removeStack(Item item, int amount){
            if(Objects.equals(ammoType, "item")) return 0;
            else return super.removeStack(item, amount);
        }

        @Override
        public void handleItem(Building source, Item item){
            if(Objects.equals(ammoType, "item")){
                if(item == Items.pyratite) Events.fire(Trigger.flameAmmo);

                BulletType type = ammoTypes.get(item);
                totalAmmo += type.ammoMultiplier;

                //find ammo entry by type
                for(int i = 0; i < ammo.size; i++){
                    ItemEntry entry = (ItemEntry)ammo.get(i);

                    //if found, put it to the right
                    if(entry.item == item){
                        entry.amount += type.ammoMultiplier;
                        ammo.swap(i, ammo.size - 1);
                        return;
                    }
                }

                //must not be found
                ammo.add(new ItemEntry(item, (int)type.ammoMultiplier));
            } else super.handleItem(source, item);
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            if(Objects.equals(ammoType, "item")) return ammoTypes.get(item) != null && totalAmmo + ammoTypes.get(item).ammoMultiplier <= maxAmmo;
            else if(Objects.equals(ammoType, "liquid")) return false;
            else return super.acceptItem(source, item);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            if(Objects.equals(ammoType, "liquid")) return liqAmmoTypes.get(liquid) != null
                && (liquids.current() == liquid || (liqAmmoTypes.containsKey(liquid)
                && (!liqAmmoTypes.containsKey(liquids.current()) || liquids.get(liquids.current()) <= 1f / liqAmmoTypes.get(liquids.current()).ammoMultiplier + 0.001f)));
            else return super.acceptLiquid(source, liquid);
        }

        @Override
        public byte version(){
            if(Objects.equals(ammoType, "item")) return 2;
            else return super.version();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            if(Objects.equals(ammoType, "item")){
                write.b(ammo.size);
                for(AmmoEntry entry : ammo){
                    ItemEntry i = (ItemEntry)entry;
                    write.s(i.item.id);
                    write.s(i.amount);
                }
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            if(Objects.equals(ammoType, "item")){
                int amount = read.ub();
                for(int i = 0; i < amount; i++){
                    Item item = Vars.content.item(revision < 2 ? read.ub() : read.s());
                    short a = read.s();
                    totalAmmo += a;

                    if(item != null && ammoTypes.containsKey(item)) ammo.add(new ItemEntry(item, a));
                }
            }
        }
    }

    class ItemEntry extends AmmoEntry{
        protected Item item;

        ItemEntry(Item item, int amount){
            this.item = item;
            this.amount = amount;
        }

        @Override
        public BulletType type(){
            return ammoTypes.get(item);
        }
    }
}
