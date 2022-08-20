package Sharustry.world.blocks.defense.turret;

import Sharustry.world.blocks.defense.turret.mounts.ItemMountTurretType;
import Sharustry.world.blocks.defense.turret.mounts.MountTurret;
import mindustry.entities.bullet.BulletType;
import mindustry.type.Item;

public class ItemEntry {
    public Item item;
    public int amount;
    public ItemEntry(Item item, int amount){
        this.item = item;
        this.amount = amount;
    }
}