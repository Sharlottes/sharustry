package sharustry.world.blocks.defense.turret;

import arc.util.pooling.Pool;

import static mindustry.Vars.content;

public class DriverBulletData implements Pool.Poolable {
    public int fromIndex, toIndex;
    public MultiTurret.MultiTurretBuild from, to;

    public int[] items = new int[content.items().size];

    @Override
    public void reset(){
        from = null;
        to = null;
    }
}