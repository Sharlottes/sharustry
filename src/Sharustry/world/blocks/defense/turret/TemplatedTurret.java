package Sharustry.world.blocks.defense.turret;

import arc.*;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.World;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.LAccess;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class TemplatedTurret extends Turret {
    public BulletType shootType;
    public ObjectMap<Item, BulletType> ammoTypes;
    public ObjectMap<Liquid, BulletType> liqAmmoTypes;
    public boolean extinguish = true;

    public TemplatedTurret(String name){
        super(name);
        if(isPowerTurret()) hasPower = true;
    }

    boolean isLiquidTurret() {
        return liqAmmoTypes != null;
    }
    boolean isItemTurret() {
        return ammoTypes != null;
    }
    
    boolean isPowerTurret() {
        return shootType != null;
    }

    public void limitRange(){
        limitRange(9f);
    }
    public void limitRange(float margin){
        if(isPowerTurret()) limitRange(shootType, margin);
        if(isItemTurret()) {
            for (ObjectMap.Entry<Item, BulletType> entry : ammoTypes.entries()) {
                limitRange(entry.value, margin);
            }
        }
    }

    public void itemAmmo(Object... objects){
        ammoTypes = OrderedMap.of(objects);
        hasItems = true;
    }
    public void liquidAmmo(Object... objects) {
        liqAmmoTypes = OrderedMap.of(objects);
        hasLiquids = true;
        loopSound = Sounds.spray;
        shootSound = Sounds.none;
        smokeEffect = Fx.none;
        shootEffect = Fx.none;
    }

    @Override
    public void setStats(){
        super.setStats();

        if(isLiquidTurret()) {
            stats.remove(Stat.itemCapacity);
            stats.add(Stat.ammo, StatValues.ammo(ammoTypes));
        }
        if(isPowerTurret()) stats.add(Stat.ammo, StatValues.ammo(ObjectMap.of(this, shootType)));
        if(isLiquidTurret()) stats.add(Stat.ammo, StatValues.ammo(liqAmmoTypes));
    }

    @Override
    public void init(){
        if(isItemTurret()) consume(new ConsumeItemFilter(i -> ammoTypes.containsKey(i)) {
            @Override
            public void build(Building build, Table table) {
                MultiReqImage image = new MultiReqImage();
                content.items().each(i -> filter.get(i) && i.unlockedNow(),
                item -> image.add(new ReqImage(new Image(item.uiIcon),
                () -> build instanceof TemplatedTurretBuild it && !it.ammo.isEmpty() && ((ItemEntry)it.ammo.peek()).item == item)));

                table.add(image).size(8 * 4);
            }

            @Override
            public float efficiency(Building build){
                //valid when there's any ammo in the turret
                return build instanceof TemplatedTurretBuild it && !it.ammo.isEmpty() ? 1f : 0f;
            }

            @Override
            public void display(Stats stats){
                //don't display
            }
        });
        if(isLiquidTurret()) consume(new ConsumeLiquidFilter(i -> liqAmmoTypes.containsKey(i), 1f){

            @Override
            public void update(Building build){

            }

            @Override
            public void display(Stats stats){

            }
        });

        super.init();
    }

    public class TemplatedTurretBuild extends TurretBuild {
        @Override
        public void handleBullet(@Nullable Bullet bullet, float offsetX, float offsetY, float angleOffset){ }

        @Override
        public boolean shouldActiveSound(){
            if(isLiquidTurret()) return wasShooting && enabled;
            return super.shouldActiveSound();
        }

        @Override
        public void onProximityAdded(){
            super.onProximityAdded();

            if(isItemTurret() && cheating() && ammo.size > 0) handleItem(this, ammoTypes.entries().next().key);
        }

        @Override
        public void updateTile(){
            if(isLiquidTurret()) unit.ammo(unit.type().ammoCapacity * liquids.currentAmount() / liquidCapacity);
            if(isPowerTurret()) unit.ammo(power.status * unit.type().ammoCapacity);
            if(isItemTurret()) unit.ammo(Mathf.clamp((float)unit.type().ammoCapacity * totalAmmo / maxAmmo));

            super.updateTile();
        }

        @Override
        public void findTarget(){
            if(isLiquidTurret() && extinguish && liquids.current().canExtinguish()){
                int tx = World.toTile(x), ty = World.toTile(y);
                Fire result = null;
                float mindst = 0f;
                int tr = (int)(range / tilesize);
                for(int x = -tr; x <= tr; x++){
                    for(int y = -tr; y <= tr; y++){
                        Tile other = world.tile(x + tx, y + ty);
                        var fire = Fires.get(x + tx, y + ty);
                        float dst = fire == null ? 0 : dst2(fire);
                        //do not extinguish fires on other team blocks
                        if(other != null && fire != null && Fires.has(other.x, other.y) && dst <= range * range && (result == null || dst < mindst) && (other.build == null || other.team() == team)){
                            result = fire;
                            mindst = dst;
                        }
                    }
                }

                if(result != null){
                    target = result;
                    //don't run standard targeting
                    return;
                }
            }

            super.findTarget();
        }

        @Override
        public double sense(LAccess sensor){
            if(isPowerTurret()) {
                return switch (sensor) {
                    case ammo -> power.status;
                    case ammoCapacity -> 1;
                    default -> super.sense(sensor);
                };
            }
            return super.sense(sensor);
        }

        @Override
        public BulletType useAmmo(){
            if(isPowerTurret()) return shootType;
            if(isLiquidTurret()){
                if(cheating()) return liqAmmoTypes.get(liquids.current());
                BulletType type = liqAmmoTypes.get(liquids.current());
                liquids.remove(liquids.current(), 1f / type.ammoMultiplier);
                return type;
            }
            return super.useAmmo();
        }

        @Override
        public BulletType peekAmmo(){
            if(isPowerTurret()) return shootType;
            if(isLiquidTurret()) return liqAmmoTypes.get(liquids.current());
            return super.peekAmmo();
        }

        @Override
        public boolean hasAmmo(){
            if(isLiquidTurret()) return liqAmmoTypes.get(liquids.current()) != null && liquids.currentAmount() >= 1f / liqAmmoTypes.get(liquids.current()).ammoMultiplier;
            return super.hasAmmo();
        }

        @Override
        public void displayBars(Table bars){
            super.displayBars(bars);

            if(isItemTurret()) {
                bars.add(new Bar("stat.ammo", Pal.ammo, () -> (float)totalAmmo / maxAmmo)).growX();
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
            if(isItemTurret()) {
                BulletType type = ammoTypes.get(item);

                if(type == null) return 0;

                return Math.min((int) ((maxAmmo - totalAmmo) / ammoTypes.get(item).ammoMultiplier), amount);
            }
            return super.acceptStack(item, amount, source);
        }

        @Override
        public void handleStack(Item item, int amount, Teamc source){
            if(isItemTurret()) for(int i = 0; i < amount; i++) handleItem(null, item);
            else super.handleStack(item, amount, source);
        }

        @Override
        public int removeStack(Item item, int amount){
            if(isItemTurret()) return 0;
            return super.removeStack(item, amount);
        }

        @Override
        public void handleItem(Building source, Item item){
            super.handleItem(source, item);
            if(isItemTurret()){
                BulletType type = ammoTypes.get(item);
                if(type == null || totalAmmo + type.ammoMultiplier > maxAmmo) return;

                if(item == Items.pyratite) Events.fire(Trigger.flameAmmo);

                totalAmmo += type.ammoMultiplier;

                //find ammo entry by type
                for(int i = 0; i < ammo.size; i++){
                    ItemEntry entry = (ItemEntry) ammo.get(i);

                    //if found, put it to the right
                    if(entry.item == item){
                        entry.amount += type.ammoMultiplier;
                        ammo.swap(i, ammo.size - 1);
                        return;
                    }
                }

                //must not be found
                ammo.add(new ItemEntry(item, (int)type.ammoMultiplier));
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            if(isItemTurret()) return ammoTypes.get(item) != null && totalAmmo + ammoTypes.get(item).ammoMultiplier <= maxAmmo;
            return super.acceptItem(source, item);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            if(isLiquidTurret()) return liqAmmoTypes.get(liquid) != null &&
                (liquids.current() == liquid ||
                ((!liqAmmoTypes.containsKey(liquids.current()) || liquids.get(liquids.current()) <= 1f / liqAmmoTypes.get(liquids.current()).ammoMultiplier + 0.001f)));
            return super.acceptLiquid(source, liquid);
        }

        @Override
        public byte version(){
            if(isItemTurret()) return 2;
            return super.version();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            if(isItemTurret()){
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
            if(isItemTurret()){
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

    class ItemEntry extends AmmoEntry {
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
