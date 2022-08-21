package Sharustry.world.blocks.defense.turret.mounts;

import Sharustry.world.blocks.defense.turret.MultiTurret;
import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.OrderedMap;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.bullet.BulletType;
import mindustry.game.EventType;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.ui.Bar;
import mindustry.ui.ItemImage;
import mindustry.ui.MultiReqImage;
import mindustry.ui.ReqImage;

import static arc.struct.ObjectMap.of;
import static mindustry.Vars.content;

public class ItemMountTurretType extends MountTurretType {
    public int ammoPerShot = 2;
    public ObjectMap<Item, BulletType> ammoTypes;
    public ItemMountTurretType(String name, BulletType bullet, Object... ammo) {
        super(name, bullet, ammo);
        ammoTypes = OrderedMap.of(ammo);
    }
    @Override
    public MountTurret create(MultiTurret block, MultiTurret.MultiTurretBuild build, int index, float x, float y) {
        return new ItemMountTurret(this, block, build, index, x, y);
    }
    @Override
    public ObjectMap<ObjectMap<BulletType, ? extends UnlockableContent>, TextureRegion> getStatData() {
        ObjectMap<ObjectMap<BulletType, ? extends UnlockableContent>, TextureRegion> types = new ObjectMap<>();
        for(Item item : content.items()){
            BulletType bullet = ammoTypes.get(item);
            if(bullet != null) types.put(of(bullet, item), item.uiIcon);
        }
        return types;
    }

    @Override
    public void buildStat(Table table) {
        super.buildStat(table);
        table.add("[lightgray]" + Core.bundle.get("stat.shar.ammo-shot") + ": [white]" + ammoPerShot).row();
    }

    public class ItemMountTurret extends MountTurret<ItemMountTurretType> {
        public ItemMountTurret(ItemMountTurretType type, MultiTurret block, MultiTurret.MultiTurretBuild build, int i, float x, float y) {
            super(type, block, build, i, x, y);
        }

        @Override
        public void handleItem(Item item) {
            if(item == Items.pyratite) Events.fire(EventType.Trigger.flameAmmo);

            BulletType bullet = type.ammoTypes.get(item);
            if(bullet == null) return;
            totalAmmo += bullet.ammoMultiplier;

            //find ammo entry by type
            for(int i = 0; i < ammo.size; i++){
                ItemEntry entry = (ItemEntry) ammo.get(i);

                //if found, put it to the right
                if(entry.item == item){
                    entry.amount += bullet.ammoMultiplier;
                    ammo.swap(i, ammo.size - 1);
                    return;
                }
            }

            //must not be found
            ammo.add(new ItemEntry(item, (int)bullet.ammoMultiplier));
        }

        @Override
        public boolean acceptItem(Item item) {
            return type.ammoTypes.get(item) != null && totalAmmo + type.ammoTypes.get(item).ammoMultiplier <= type.maxAmmo;
        }

        @Override
        public int acceptStack(Item item, int amount) {
            if(ammoTypes.get(item) == null) return 0;
            return Math.min((int)((maxAmmo - totalAmmo) / ammoTypes.get(item).ammoMultiplier), amount);
        }

        @Override
        public void display(Table table) {
            if(block.basicMounts.size > 3 && mountIndex % 4 == 0) table.row();
            else if(mountIndex % 4 == 0) table.row();
            table.stack(
                new Table(o -> {
                    o.left();
                    o.image(Core.atlas.find("shar-" + type.name + "-full")).size(5*8f);
                }),
                new Table(h -> {
                    h.stack(
                            new Table(e -> {
                                e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*2f);
                                e.add(hasAmmo() && ammo.peek() instanceof ItemEntry entry
                                        ? new Bar("", entry.item.color, () -> totalAmmo / type.maxAmmo)
                                        : new Bar("", new Color(0.1f, 0.1f, 0.1f, 1), () -> 0));
                                e.pack();
                            }),
                            new Table(e -> {
                                e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*5f);
                                e.add(new Bar(() -> "", () -> Pal.accent.cpy().lerp(Color.orange, reloadCounter / type.reload), () -> reloadCounter / type.reload));
                                e.pack();
                            }),
                            new Table(e -> {
                                if(type.chargeTime <= 0) return;
                                e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*8f);
                                e.add(new Bar(() -> "", () -> Pal.surge.cpy().lerp(Pal.accent, reloadCounter / type.reload), () -> charge));
                                e.pack();
                            }),
                            hasAmmo() && ammo.peek() instanceof ItemEntry entry
                                ? new Table(e -> e.add(new ItemImage(entry.item.fullIcon, totalAmmo)))
                                : new Table(e -> {
                                    MultiReqImage itemReq = new MultiReqImage();
                                    for(Item item : type.ammoTypes.keys()) itemReq.add(new ReqImage(item.uiIcon, this::hasAmmo));
                                    e.add(itemReq).size(18f);
                                })
                    ).padTop(2*8).padLeft(2*8);
                    h.pack();
                })
            ).left().size(7*8f);
        }

        @Override
        public void write(Writes write) {
            super.write(write);

            write.b(ammo.size);
            for(AmmoEntry entry : ammo) {
                ItemEntry i = (ItemEntry)entry;
                write.s(i.item.id);
                write.s(i.amount);
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);

            int size = read.ub();
            for (int i = 0; i < size; i++) {
                Item item = Vars.content.item(revision < 2 ? read.ub() : read.s());
                short a = read.s();
                totalAmmo += a;

                if (item != null && type.ammoTypes.containsKey(item))
                    ammo.add(new ItemEntry(item, a));
            }
        }
    }

    public class ItemEntry extends AmmoEntry {
        public Item item;

        ItemEntry(Item item, int amount){
            this.item = item;
            this.amount = amount;
        }

        @Override
        public BulletType type(){
            return ammoTypes.get(item);
        }

        @Override
        public String toString(){
            return "ItemEntry{" +
                    "item=" + item +
                    ", amount=" + amount +
                    '}';
        }
    }
}
