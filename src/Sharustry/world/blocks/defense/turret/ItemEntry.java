package Sharustry.world.blocks.defense.turret;

import mindustry.entities.bullet.BulletType;
import mindustry.type.Item;

public class ItemEntry {
    public Item item;
    public int amount;
    public ItemEntry(Item item, int amount){
        this.item = item;
        this.amount = amount;
    }

    public BulletType types(MountTurret mount){
        return mount.type.mountAmmoType.get(item);
    }
}