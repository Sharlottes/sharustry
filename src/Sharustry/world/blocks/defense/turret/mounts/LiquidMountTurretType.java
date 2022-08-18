package Sharustry.world.blocks.defense.turret.mounts;

import Sharustry.world.blocks.defense.turret.MountTurret;
import Sharustry.world.blocks.defense.turret.MultiTurret;
import arc.graphics.g2d.TextureRegion;
import arc.struct.ObjectMap;
import arc.struct.OrderedMap;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.bullet.BulletType;
import mindustry.type.Liquid;

import static arc.struct.ObjectMap.of;
import static mindustry.Vars.content;

public class LiquidMountTurretType extends MountTurretType {
    public LiquidMountTurretType(String name, BulletType bullet, Object... ammos) {
        super(name, bullet, ammos);
        liquidMountAmmoType = OrderedMap.of(ammos);
    }

    @Override
    public ObjectMap<ObjectMap<BulletType, ? extends UnlockableContent>, TextureRegion> getStatData() {
        ObjectMap<ObjectMap<BulletType, ? extends UnlockableContent>, TextureRegion> types = new ObjectMap<>();
        for(Liquid liquid : content.liquids()) {
            BulletType bullet = liquidMountAmmoType.get(liquid);
            if(bullet != null) types.put(of(bullet, liquid), liquid.uiIcon);
        }
        return types;
    }
    public class LiquidMountTurret extends MountTurret {
        public LiquidMountTurret(MountTurretType type, MultiTurret block, MultiTurret.MultiTurretBuild build, int i, float x, float y) {
            super(type, block, build, i, x, y);
        }

        @Override
        public BulletType peekAmmo(MultiTurret.MultiTurretBuild build) {
            return type.liquidMountAmmoType.get(build.liquids.current());
        }

        @Override
        public BulletType useAmmo(MultiTurret.MultiTurretBuild build) {
            return build.cheating() ? peekAmmo(build) : super.useAmmo(build);
        }

        @Override
        public boolean hasAmmo(MultiTurret.MultiTurretBuild build) {
            return type.liquidMountAmmoType != null
                    && type.liquidMountAmmoType.get(build.liquids.current()) != null
                    && build.liquids.currentAmount() >= 1f / type.liquidMountAmmoType.get(build.liquids.current()).ammoMultiplier;
        }
    }
}
