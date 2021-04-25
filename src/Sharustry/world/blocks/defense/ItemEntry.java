package Sharustry.world.blocks.defense;

import mindustry.entities.bullet.BulletType;
import mindustry.type.Item;

public class ItemEntry {
    protected Item item;
    public int amount;
    ItemEntry(Item item, int amount){
        this.item = item;
        this.amount = amount;
    }

    public BulletType types(MountTurret mount){
        return mount.type.mountAmmoType.get(item);
    }
}