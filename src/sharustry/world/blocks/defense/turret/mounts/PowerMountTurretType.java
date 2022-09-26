package sharustry.world.blocks.defense.turret.mounts;

import sharustry.world.blocks.defense.turret.MultiTurret;
import mindustry.entities.bullet.BulletType;

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
