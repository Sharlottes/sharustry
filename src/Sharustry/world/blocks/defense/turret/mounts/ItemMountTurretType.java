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
import mindustry.world.blocks.defense.turrets.ItemTurret;

import static arc.struct.ObjectMap.of;
import static mindustry.Vars.content;

public class ItemMountTurretType extends MountTurretType {
    public int ammoPerShot = 2;
    public ObjectMap<Item, BulletType> mountAmmoType;
    public ItemMountTurretType(String name, BulletType bullet, Object... ammo) {
        super(name, bullet, ammo);
        mountAmmoType = OrderedMap.of(ammo);
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
        table.add("[lightgray]" + Core.bundle.get("stat.shar.ammo-shot") + ": [white]" + ammoPerShot).row();
    }

    public static class ItemMountTurret extends MountTurret<ItemMountTurretType> {
        public ItemMountTurret(ItemMountTurretType type, MultiTurret block, MultiTurret.MultiTurretBuild build, int i, float x, float y) {
            super(type, block, build, i, x, y);
        }

        @Override
        public void handleItem(Item item) {
            if(item == Items.pyratite) Events.fire(EventType.Trigger.flameAmmo);

            BulletType bullet = type.mountAmmoType.get(item);
            if(bullet == null) return;
            totalAmmo += bullet.ammoMultiplier;

            //find ammo entry by type
            for(int i = 0; i < ammo.size; i++){
                ItemEntry entry = ammo.get(i);

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
            return super.acceptItem(item)
                    || (type.mountAmmoType.get(item) != null && totalAmmo + type.mountAmmoType.get(item).ammoMultiplier <= type.maxAmmo);
        }

        @Override
        public int acceptStack(Item item, int amount) {
            if(type.mountAmmoType.get(item) != null && totalAmmo + type.mountAmmoType.get(item).ammoMultiplier <= type.maxAmmo)
                return Math.min((int)((type.maxAmmo - totalAmmo) / type.mountAmmoType.get(item).ammoMultiplier), amount);
            return super.acceptStack(item, amount);
        }

        @Override
        public BulletType peekAmmo() {
            return type.mountAmmoType.get(ammo.peek().item);
        }

        @Override
        public boolean hasAmmo() {
            if (ammo.size >= 2 && ammo.peek().amount < type.ammoPerShot) ammo.pop();
            return ammo.size > 0 && ammo.peek().amount >= type.ammoPerShot;
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
                                        ? new Bar("", ammo.peek().item.color, () -> totalAmmo / type.maxAmmo)
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
                                ? new Table(e -> e.add(new ItemImage(ammo.peek().item.fullIcon, totalAmmo)))
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

            write.b(ammo.size);
            for(ItemEntry entry : ammo) {
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
                    ammo.add(new ItemEntry(item, a));
            }
        }
    }
}
