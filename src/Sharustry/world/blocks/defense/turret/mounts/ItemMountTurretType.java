package Sharustry.world.blocks.defense.turret.mounts;

import Sharustry.world.blocks.defense.turret.ItemEntry;
import Sharustry.world.blocks.defense.turret.MultiTurret;
import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.OrderedMap;
import arc.struct.Seq;
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
    public ObjectMap<Item, BulletType> mountAmmoType;
    public ItemMountTurretType(String name, BulletType bullet, Object... ammos) {
        super(name, bullet, ammos);
        mountAmmoType = OrderedMap.of(ammos);
    }
    @Override
    public MountTurret create(MultiTurret block, MultiTurret.MultiTurretBuild build, int index, float x, float y) {
        return new ItemMountTurret(this, block, build, index, x, y);
    }
    @Override
    public ObjectMap<ObjectMap<BulletType, ? extends UnlockableContent>, TextureRegion> getStatData() {
        ObjectMap<ObjectMap<BulletType, ? extends UnlockableContent>, TextureRegion> types = new ObjectMap<>();
        for(Item item : content.items()){
            BulletType bullet = mountAmmoType.get(item);
            if(bullet != null) types.put(of(bullet, item), item.uiIcon);
        }
        return types;
    }

    @Override
    public void buildStat(Table table) {
        super.buildStat(table);
        rowAdd(table, "[lightgray]" + Core.bundle.get("stat.shar.ammo-shot") + ": [white]" + ammoPerShot);
    }

    public static class ItemMountTurret extends MountTurret<ItemMountTurretType> {
        Seq<ItemEntry> ammos = new Seq<>();
        public ItemMountTurret(ItemMountTurretType type, MultiTurret block, MultiTurret.MultiTurretBuild build, int i, float x, float y) {
            super(type, block, build, i, x, y);
        }

        @Override
        public void handleItem(Item item) {
            if(!(
                (type.mountAmmoType != null && type.mountAmmoType.get(item) != null && totalAmmo + type.mountAmmoType.get(item).ammoMultiplier <= type.maxAmmo)
                || (block.ammoTypes.get(item) != null && totalAmmo + block.ammoTypes.get(item).ammoMultiplier <= block.maxAmmo)
            )) return;

            if (item == Items.pyratite) Events.fire(EventType.Trigger.flameAmmo);

            BulletType bullet = type.mountAmmoType.get(item);
            if(bullet == null) return;
            totalAmmo = (int)(totalAmmo + bullet.ammoMultiplier);

            ItemEntry entry = ammos.find(ammo -> ammo.item == item);
            if(entry != null) {
                entry.amount += (int)bullet.ammoMultiplier;
                ammos.swap(mountIndex, ammos.size - 1);
            } else {
                ammos.add(new ItemEntry(item, (int)bullet.ammoMultiplier < type.ammoPerShot ? (int)bullet.ammoMultiplier + type.ammoPerShot : (int)bullet.ammoMultiplier));
            }
        }

        @Override
        public boolean acceptItem(Item item) {
            return (build.hasMass() && build.items.total() < block.itemCapacity) || (type.mountAmmoType != null && type.mountAmmoType.get(item) != null && totalAmmo + type.mountAmmoType.get(item).ammoMultiplier <= type.maxAmmo)
                    || (block.ammoTypes.get(item) != null && totalAmmo + block.ammoTypes.get(item).ammoMultiplier <= block.maxAmmo);
        }

        @Override
        public int acceptStack(Item item, int amount) {
            if(type.mountAmmoType != null && type.mountAmmoType.get(item) != null
                    && totalAmmo + type.mountAmmoType.get(item).ammoMultiplier <= type.maxAmmo)
                return Math.min((int)((type.maxAmmo - totalAmmo) / type.mountAmmoType.get(item).ammoMultiplier), amount);
            return super.acceptStack(item, amount);
        }

        @Override
        public BulletType peekAmmo() {
            return ammos.peek().types(this);
        }

        @Override
        public BulletType useAmmo() {
            if(build.cheating()) return peekAmmo();

            ItemEntry entry = ammos.peek();
            entry.amount -= type.ammoPerShot;
            if(entry.amount <= 0) ammos.pop();
            totalAmmo = Math.max(totalAmmo - type.ammoPerShot, 0);

            ejectEffects();
            return entry.types(this);
        }

        @Override
        public boolean hasAmmo() {
            if (ammos.size >= 2 && ammos.peek().amount < type.ammoPerShot) ammos.pop();
            return ammos.size > 0 && ammos.peek().amount >= type.ammoPerShot;
        }
        @Override
        public void display(Table table) {
            if(block.basicMounts.size > 3 && mountIndex % 4 == 0) table.row();
            else if(mountIndex % 4 == 0) table.row();
            table.stack(
                new Table(o -> {
                    o.left();
                    o.add(new Image(Core.atlas.find("shar-" + type.name + "-full"))).size(5*8f);
                }),
                new Table(h -> {
                    h.stack(
                            new Table(e -> {
                                e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*2f);
                                e.add(hasAmmo()
                                        ? new Bar("", ammos.peek().item.color, () -> totalAmmo / type.maxAmmo)
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
                            hasAmmo()
                                ? new Table(e -> e.add(new ItemImage(ammos.peek().item.fullIcon, totalAmmo)))
                                : new Table(e -> {
                                    MultiReqImage itemReq = new MultiReqImage();
                                    for(Item item : type.mountAmmoType.keys()) itemReq.add(new ReqImage(item.uiIcon, () -> hasAmmo()));
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

            write.b(ammos.size);
            for(ItemEntry entry : ammos) {
                write.s(entry.item.id);
                write.s(entry.amount);
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);

            int amount = read.ub();
            for (int i = 0; i < amount; i++) {
                Item item = Vars.content.item(revision < 2 ? read.ub() : read.s());
                short a = read.s();
                totalAmmo += a;

                if (item != null && type.mountAmmoType != null && type.mountAmmoType.containsKey(item))
                    ammos.add(new ItemEntry(item, a));
            }
        }
    }
}
