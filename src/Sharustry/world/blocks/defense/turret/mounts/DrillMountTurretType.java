package Sharustry.world.blocks.defense.turret.mounts;

import Sharustry.world.blocks.defense.turret.MountTurret;
import Sharustry.world.blocks.defense.turret.MultiTurret;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.input.InputHandler;
import mindustry.type.Item;

import static mindustry.Vars.*;
import static mindustry.Vars.control;

public class DrillMountTurretType extends MountTurretType {
    public DrillMountTurretType(String name) {
        super(name);
    }
    public class DrillMountTurret extends MountTurret {

        public DrillMountTurret(MountTurretType type, MultiTurret block, MultiTurret.MultiTurretBuild build, int i, float x, float y) {
            super(type, block, build, i, x, y);;
            mountReMap(build);
        }

        @Override
        public void updateTile(MultiTurret.MultiTurretBuild build) {
            super.updateTile(build);

            float[] loc = mountLocations(build);

            Building core = state.teams.closestCore(loc[4], loc[5], build.team);

            //target ore
            targetMine(build, core);
            if (core == null
                    || mineTile == null
                    || Mathf.clamp(build.power.graph.getPowerBalance() / powerUse, 0, 1) <= 0.001f
                    || !Angles.within(rotation, build.angleTo(mineTile), shootCone)
                    || build.items.get(mineTile.drop()) >= block.itemCapacity) {
                mineTile = null;
                mineTimer = 0f;
            }

            if (mineTile != null) {
                //mine tile
                Item item = mineTile.drop();
                mineTimer += Time.delta * mineSpeed * getPowerEfficiency(build);

                if (Mathf.chance(0.06 * Time.delta)) {
                    Fx.pulverizeSmall.at(mineTile.worldx() + Mathf.range(tilesize / 2f), mineTile.worldy() + Mathf.range(tilesize / 2f), 0f, item.color);
                }

                if (mineTimer >= 50f + item.hardness * 15f) {
                    mineTimer = 0;

                    if (state.rules.sector != null && build.team() == state.rules.defaultTeam)
                        state.rules.sector.info.handleProduction(item, 1);

                    //items are synced anyway
                    InputHandler.transferItemTo(null, item, 1,
                            mineTile.worldx() + Mathf.range(tilesize / 2f),
                            mineTile.worldy() + Mathf.range(tilesize / 2f),
                            build);
                }

                if (!headless) control.sound.loop(shootSound, build, shootSoundVolume);
            }
        }

        @Override
        public void draw(MultiTurret.MultiTurretBuild build) {
            super.draw(build);
            if(mineTile == null) return;

            float[] loc = mountLocations(build);
            float focusLen = type.laserOffset / 2f + Mathf.absin(Time.time, 1.1f, 0.5f);
            float swingScl = 12f, swingMag = tilesize / 8f;
            float flashScl = 0.3f;

            float px = loc[2] + Angles.trnsx(rotation, focusLen);
            float py = loc[3] + Angles.trnsy(rotation, focusLen);

            float ex = mineTile.worldx() + Mathf.sin(Time.time + 48, swingScl, swingMag);
            float ey = mineTile.worldy() + Mathf.sin(Time.time + 48, swingScl + 2f, swingMag);

            Draw.z(Layer.flyingUnit + 0.1f);
            Draw.color(Color.lightGray, Color.white, 1f - flashScl + Mathf.absin(Time.time, 0.5f, flashScl));
            Drawf.laser(type.laser, type.laserEnd, px, py, ex, ey, type.laserWidth);
        }
    }
}
