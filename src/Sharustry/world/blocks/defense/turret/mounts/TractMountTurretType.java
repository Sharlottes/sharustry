package Sharustry.world.blocks.defense.turret.mounts;

import Sharustry.world.blocks.defense.turret.MountTurret;
import Sharustry.world.blocks.defense.turret.MultiTurret;
import arc.Core;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.util.Tmp;
import mindustry.content.StatusEffects;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.meta.Stat;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;

public class TractMountTurretType extends MountTurretType {
    public TractMountTurretType(String name) {
        super(name);
    }

    @Override
    public void buildStat(Table table) {
        super.buildStat(table);
        rowAdd(table, "[lightgray]" + Stat.damage.localized() + ": [white]" + Core.bundle.format("stat.shar.damage", damage * 60f));
    }


    public class TractMountTurret extends MountTurret {
        public TractMountTurret(MountTurretType type, MultiTurret block, MultiTurret.MultiTurretBuild build, int i, float x, float y) {
            super(type, block, build, i, x, y);
        }


        @Override
        public void updateTile(MultiTurret.MultiTurretBuild build) {
            float[] loc = mountLocations(build);
            any = false;

            //look at target
            if(tractTarget != null
                    && tractTarget.within(new Vec2(loc[0], loc[1]), range + tractTarget.hitSize/2f)
                    && tractTarget.team() != build.team
                    && tractTarget.checkTarget(targetAir, targetGround)
                    && getPowerEfficiency(build) > 0.02f){
                if(!headless) control.sound.loop(shootSound, new Vec2(loc[0], loc[1]), shootSoundVolume);

                float dest = build.angleTo(tractTarget);
                targetTurn(build, dest);
                lastX = tractTarget.x;
                lastY = tractTarget.y;
                strength = Mathf.lerpDelta(strength, 1f, 0.1f);

                //shoot when possible
                if(Angles.within(rotation, dest, shootCone)){
                    if(damage > 0) tractTarget.damageContinuous(damage * getPowerEfficiency(build));

                    if(status != StatusEffects.none) tractTarget.apply(status, statusDuration);

                    any = true;
                    tractTarget.impulseNet(
                            Tmp.v1.set(new Vec2(loc[0], loc[1]))
                                    .sub(tractTarget)
                                    .limit((force + (1f - tractTarget.dst(new Vec2(loc[0], loc[1])) / range) * scaledForce) * build.delta() * getPowerEfficiency(build)));
                }
            }else {
                strength = Mathf.lerpDelta(strength, 0, 0.1f);
            }
        }

        @Override
        public void draw(MultiTurret.MultiTurretBuild build) {
            super.draw(build);
            if(!any) return;

            float[] loc = mountLocations(build);
            Draw.z(Layer.bullet);
            float ang = build.angleTo(lastX, lastY);

            Draw.mixcol(type.laserColor, Mathf.absin(4f, 0.6f));
            Drawf.laser(type.tractLaser, type.tractLaserEnd,
                    loc[2] + Angles.trnsx(ang, type.shootLength), loc[3] + Angles.trnsy(ang, type.shootLength),
                    lastX, lastY, strength * getPowerEfficiency(build) * type.laserWidth);

        }
    }
}
