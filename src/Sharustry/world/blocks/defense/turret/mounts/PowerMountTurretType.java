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
        super(name);
        shootType = bullet;
    }
    @Override
    public MountTurret create(MultiTurret block, MultiTurret.MultiTurretBuild build, int index, float x, float y) {
        return new PowerMountTurret(this, block, build, index, x, y);
    }

    public class PowerMountTurret extends MountTurret<PowerMountTurretType> {
        public PowerMountTurret(PowerMountTurretType type, MultiTurret block, MultiTurret.MultiTurretBuild build, int i, float x, float y) {
            super(type, block, build, i, x, y);
        }

        @Override
        public BulletType useAmmo() {
            return shootType;
        }

        @Override
        public boolean hasAmmo() {
            return true;
        }

        @Override
        public BulletType peekAmmo() {
            return shootType;
        }
    }
}
