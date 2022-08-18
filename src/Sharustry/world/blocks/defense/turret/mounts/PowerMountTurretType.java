package Sharustry.world.blocks.defense.turret.mounts;

import Sharustry.world.blocks.defense.turret.MountTurret;
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
    public ObjectMap<ObjectMap<BulletType, ? extends UnlockableContent>, TextureRegion> getStatData() {
        ObjectMap<ObjectMap<BulletType, ? extends UnlockableContent>, TextureRegion> types = new ObjectMap<>();
        BulletType bullet = this.bullet;
        if(bullet != null) types.put(of(bullet, null), Icon.power.getRegion());
        return types;
    }

    public class PowerMountTurret extends MountTurret {
        public PowerMountTurret(MountTurretType type, MultiTurret block, MultiTurret.MultiTurretBuild build, int i, float x, float y) {
            super(type, block, build, i, x, y);
        }

        @Override
        public BulletType peekAmmo(MultiTurret.MultiTurretBuild build) {
            return type.bullet;
        }

        @Override
        public BulletType useAmmo(MultiTurret.MultiTurretBuild build) {
            return type.bullet;
        }
    }
}
