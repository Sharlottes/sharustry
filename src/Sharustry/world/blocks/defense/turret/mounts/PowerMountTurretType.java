package Sharustry.world.blocks.defense.turret.mounts;

import Sharustry.world.blocks.defense.turret.MultiTurret;
import arc.graphics.g2d.TextureRegion;
import arc.struct.ObjectMap;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Icon;

import static arc.struct.ObjectMap.of;

public class PowerMountTurretType extends MountTurretType {
    public PowerMountTurretType(String name) {
        super(name);
    }
    public PowerMountTurretType(String name, BulletType bullet) {
        super(name, bullet);
    }
    @Override
    public MountTurret create(MultiTurret block, MultiTurret.MultiTurretBuild build, int index, float x, float y) {
        return new PowerMountTurret(this, block, build, index, x, y);
    }
    @Override
    public ObjectMap<ObjectMap<BulletType, ? extends UnlockableContent>, TextureRegion> getStatData() {
        ObjectMap<ObjectMap<BulletType, ? extends UnlockableContent>, TextureRegion> types = new ObjectMap<>();
        BulletType bullet = this.bullet;
        if(bullet != null) types.put(of(bullet, null), Icon.power.getRegion());
        return types;
    }

    public static class PowerMountTurret extends MountTurret<PowerMountTurretType> {
        public PowerMountTurret(PowerMountTurretType type, MultiTurret block, MultiTurret.MultiTurretBuild build, int i, float x, float y) {
            super(type, block, build, i, x, y);
        }
    }
}
