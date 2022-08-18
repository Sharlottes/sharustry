package Sharustry.world.blocks.defense.turret.mounts;

import Sharustry.world.blocks.defense.turret.ItemEntry;
import Sharustry.world.blocks.defense.turret.MountTurret;
import Sharustry.world.blocks.defense.turret.MultiTurret;
import arc.Core;
import arc.Events;
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
import mindustry.type.Item;

import static arc.struct.ObjectMap.of;
import static mindustry.Vars.content;

public class ItemMountTurretType extends MountTurretType {
    public ItemMountTurretType(String name, BulletType bullet, Object... ammos) {
        super(name, bullet, ammos);
        mountAmmoType = OrderedMap.of(ammos);
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

    public class ItemMountTurret extends MountTurret {
        public ItemMountTurret(MountTurretType type, MultiTurret block, MultiTurret.MultiTurretBuild build, int i, float x, float y) {
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
                ammos.swap(i, ammos.size - 1);
            } else {
                ammos.add(new ItemEntry(item, (int)bullet.ammoMultiplier < type.ammoPerShot ? (int)bullet.ammoMultiplier + type.ammoPerShot : (int)bullet.ammoMultiplier));
            }
        }

        @Override
        public BulletType peekAmmo(MultiTurret.MultiTurretBuild build) {
            return ammos.peek().types(this);
        }

        @Override
        public BulletType useAmmo(MultiTurret.MultiTurretBuild build) {
            if(build.cheating()) return peekAmmo(build);

            ItemEntry entry = ammos.peek();
            entry.amount -= type.ammoPerShot;
            if(entry.amount <= 0) ammos.pop();
            totalAmmo = Math.max(totalAmmo - type.ammoPerShot, 0);

            ejectEffects(build);
            return entry.types(this);
        }

        @Override
        public boolean hasAmmo(MultiTurret.MultiTurretBuild build) {
            if (ammos.size >= 2 && ammos.peek().amount < type.ammoPerShot) ammos.pop();
            return ammos.size > 0 && ammos.peek().amount >= type.ammoPerShot;
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
