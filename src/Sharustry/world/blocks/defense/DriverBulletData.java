package Sharustry.world.blocks.defense;

import Sharustry.world.blocks.storage.BattleCore;
import arc.util.pooling.Pool;
import mindustry.gen.Building;

import static mindustry.Vars.content;

public class DriverBulletData implements Pool.Poolable {
    public int massMount;
    public Building from, to;

    public int[] items = new int[content.items().size];

    @Override
    public void reset(){
        from = null;
        to = null;
    }
}