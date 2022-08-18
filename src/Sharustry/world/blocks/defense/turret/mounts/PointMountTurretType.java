package Sharustry.world.blocks.defense.turret.mounts;

import Sharustry.world.blocks.defense.turret.MountTurret;
import Sharustry.world.blocks.defense.turret.MultiTurret;
import arc.Core;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.util.Tmp;

public class PointMountTurretType extends MountTurretType {
    public PointMountTurretType(String name) {
        super(name);
    }

    @Override
    public void buildStat(Table table) {
        super.buildStat(table);
        rowAdd(table, "[lightgray]" + Core.bundle.format("stat.shar.targetsBullet") + ": [white]" + Core.bundle.get("yes"));
    }
    public class PointMountTurret extends MountTurret {
        public PointMountTurret(MountTurretType type, MultiTurret block, MultiTurret.MultiTurretBuild build, int i, float x, float y) {
            super(type, block, build, i, x, y);
        }

        @Override
        public void updateTile(MultiTurret.MultiTurretBuild build) {
            float[] loc = mountLocations(build);
            if(pointTarget != null
                    && pointTarget.within(new Vec2(loc[0], loc[1]), range)
                    && pointTarget.team != build.team
                    && pointTarget.type() != null
                    && pointTarget.type().hittable){
                float dest = build.angleTo(pointTarget);
                targetTurn(build, dest);
                reloadCounter += build.delta() * getPowerEfficiency(build);

                //shoot when possible
                if(Angles.within(rotation, dest, shootCone) && reloadCounter >= reload){
                    if(pointTarget.damage() > bulletDamage) pointTarget.damage(pointTarget.damage() - bulletDamage);
                    else pointTarget.remove();

                    Tmp.v1.trns(rotation, shootLength);

                    beamEffect.at(loc[0] + Tmp.v1.x, loc[1] + Tmp.v1.y, rotation, colorPoint, pointTarget);
                    shootEffect.at(loc[0] + Tmp.v1.x, loc[1] + Tmp.v1.y, rotation, colorPoint);
                    hitEffect.at(pointTarget.x, pointTarget.y, colorPoint);
                    shootSound.at(loc[0] + Tmp.v1.x, loc[1] + Tmp.v1.y, Mathf.random(0.9f, 1.1f));

                    reloadCounter = 0f;
                }
            }
        }
    }
}
