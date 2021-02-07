package Sharustry.world.blocks.storage;

import arc.*;
import arc.math.Angles;
import arc.math.Mathf;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.Time;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import mindustry.world.meta.values.*;

import static mindustry.Vars.*;

public class ItemBattleCoreBlock extends BattleCoreBlock {
    public ObjectMap<Item, BulletType> ammoTypes = new ObjectMap<>();

    public ItemBattleCoreBlock(String name){
        super(name);
        hasItems = true;
    }
    /** Initializes accepted ammo map. Format: [item1, bullet1, item2, bullet2...] */
    public void ammo(Object... objects){
        ammoTypes = OrderedMap.of(objects);
    }
    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.itemCapacity);
        stats.add(Stat.ammo, new AmmoListValue<>(ammoTypes));
    }

    @Override
    public void init(){
        consumes.add(new ConsumeItemFilter(i -> ammoTypes.containsKey(i)){
            @Override
            public void build(Building tile, Table table){
                MultiReqImage image = new MultiReqImage();
                content.items().each(i -> filter.get(i) && i.unlockedNow(), item -> image.add(new ReqImage(new ItemImage(item.icon(Cicon.medium)),
                        () -> (tile instanceof ItemBattleCoreBuild) && !(((ItemBattleCoreBuild) tile).ammo.isEmpty() && (((ItemEntry) ((ItemBattleCoreBuild) tile).ammo.peek()).item == item)))));

                table.add(image).size(8 * 4);
            }

            @Override
            public boolean valid(Building entity){
                //valid when there's any ammo in the turret
                return (entity instanceof ItemBattleCoreBuild) && !((ItemBattleCoreBuild) entity).ammo.isEmpty();
            }

            @Override
            public void display(Stats stats){
                //don't display
            }

        });

        super.init();
    }

    public class ItemBattleCoreBuild extends BattleCoreBuild {
        public Seq<ItemBattleCoreBuild> turretBuilds = new Seq<>();

        transient ItemBattleCoreBlock block;
        @Override
        public void onProximityAdded(){
            super.onProximityAdded();

            //add first ammo item to cheat blocks so they can shoot properly
            if(cheating() && ammo.size > 0){
                handleItem(this, ammoTypes.entries().next().key);
            }
        }

        @Override
        public void updateTile(){
            for(ItemBattleCoreBuild turretb : turretBuilds) {
                turretb.unit.ammo((float)unit.type().ammoCapacity * totalAmmo / maxAmmo);
                if (!turretb.validateTarget()) turretb.target = null;

                turretb.wasShooting = false;

                turretb.recoil = Mathf.lerpDelta(turretb.recoil, 0f, turretb.block.restitution);
                turretb.heat = Mathf.lerpDelta(turretb.heat, 0f, turretb.block.cooldown);

                turretb.unit.health(turretb.health);
                turretb.unit.rotation(turretb.rotation);
                turretb.unit.team(team);
                turretb.unit.set(turretb.x, turretb.y);

                if (turretb.logicControlTime > 0) {
                    turretb.logicControlTime -= Time.delta;
                }

                if (turretb.hasAmmo()) {

                    if (timer(turretb.block.timerTarget, turretb.block.targetInterval)) {
                        turretb.findTarget();
                    }

                    if (turretb.validateTarget()) {
                        boolean canShoot = true;

                        if (turretb.isControlled()) { //player behavior
                            turretb.targetPos.set(turretb.unit.aimX(), turretb.unit.aimY());
                            canShoot = turretb.unit.isShooting();
                        } else if (turretb.logicControlled()) { //logic behavior
                            canShoot = turretb.logicShooting;
                        } else { //default AI behavior
                            turretb.targetPosition(turretb.target);

                            if (Float.isNaN(turretb.rotation)) {
                                turretb.rotation = 0;
                            }
                        }

                        float targetRot = angleTo(turretb.targetPos);

                        if (turretb.shouldTurn()) {
                            turretb.turnToTarget(targetRot);
                        }

                        if (Angles.angleDist(turretb.rotation, targetRot) < turretb.block.shootCone && canShoot) {
                            turretb.wasShooting = true;
                            turretb.updateShooting();
                        }
                    }
                }

                if (turretb.block.acceptCoolant) {
                    turretb.updateCooling();
                }
            }
        }

        @Override
        public void displayBars(Table bars){
            super.displayBars(bars);

            bars.add(new Bar("stat.ammo", Pal.ammo, () -> (float)totalAmmo / maxAmmo)).growX();
            bars.row();
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            BulletType type = ammoTypes.get(item);

            if(type == null) return 0;

            return Math.min((int)((maxAmmo - totalAmmo) / ammoTypes.get(item).ammoMultiplier), amount);
        }

        @Override
        public void handleStack(Item item, int amount, Teamc source){
            for(int i = 0; i < amount; i++){
                handleItem(null, item);
            }
        }

        //currently can't remove items from turrets.
        @Override
        public int removeStack(Item item, int amount){
            return 0;
        }

        @Override
        public void handleItem(Building source, Item item){

            if(item == Items.pyratite){
                Events.fire(Trigger.flameAmmo);
            }

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
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return ammoTypes.get(item) != null && totalAmmo + ammoTypes.get(item).ammoMultiplier <= maxAmmo;
        }

        @Override
        public byte version(){
            return 2;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.b(ammo.size);
            for(AmmoEntry entry : ammo){
                ItemEntry i = (ItemEntry)entry;
                write.s(i.item.id);
                write.s(i.amount);
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            int amount = read.ub();
            for(int i = 0; i < amount; i++){
                Item item = Vars.content.item(revision < 2 ? read.ub() : read.s());
                short a = read.s();
                totalAmmo += a;

                //only add ammo if this is a valid ammo type
                if(item != null && ammoTypes.containsKey(item)){
                    ammo.add(new ItemEntry(item, a));
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