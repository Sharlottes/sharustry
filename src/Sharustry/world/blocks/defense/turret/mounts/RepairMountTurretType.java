package Sharustry.world.blocks.defense.turret.mounts;

import Sharustry.world.blocks.defense.turret.MountTurret;
import Sharustry.world.blocks.defense.turret.MultiTurret;
import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.Vars;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.meta.Stat;

import static mindustry.Vars.player;
import static mindustry.Vars.tilesize;

public class RepairMountTurretType extends MountTurretType {
    public RepairMountTurretType(String name) {
        super(name);
    }

    @Override
    public void buildStat(Table table) {
        super.buildStat(table);
        rowAdd(table, "[lightgray]" + Stat.range.localized() + ": [white]" + Core.bundle.format("stat.shar.range", repairRadius / Vars.tilesize));
    }

    @Override
    public void drawPlace(MultiTurret block, int mount, int x, int y, int rotation, boolean valid){
        float fade = Mathf.curve(Time.time % block.totalRangeTime, block.rangeTime * mount, block.rangeTime * mount + block.fadeTime) - Mathf.curve(Time.time % block.totalRangeTime, block.rangeTime * (mount + 1) - block.fadeTime, block.rangeTime * (mount + 1));
        float tX = x * tilesize + block.offset + (block.customMountLocation ? block.customMountLocationsX.get(mount) : this.x);
        float tY = y * tilesize + block.offset + (block.customMountLocation ? block.customMountLocationsY.get(mount) : this.y);

        Lines.stroke(3, Pal.gray);
        Draw.alpha(fade);
        Lines.dashCircle(tX, tY, range);
        Lines.stroke(1, Pal.heal);
        Draw.alpha(fade);
        Lines.dashCircle(tX, tY, range);
        Draw.color(player.team().color, fade);
        Draw.rect(turrets[3], tX, tY);
        Draw.reset();
    }

    public class LiquidMountTurret extends MountTurret {
        public LiquidMountTurret(MountTurretType type, MultiTurret block, MultiTurret.MultiTurretBuild build, int i, float x, float y) {
            super(type, block, build, i, x, y);
        }


        @Override
        public void updateTile(MultiTurret.MultiTurretBuild build) {
            float[] loc = mountLocations(build);
            boolean targetIsBeingRepaired = false;
            if(repairTarget != null){
                if(repairTarget.dead()
                        || repairTarget.dst(loc[0], loc[1]) - repairTarget.hitSize / 2f > repairRadius
                        || repairTarget.health() >= repairTarget.maxHealth()) repairTarget = null;
                else {
                    repairTarget.heal(type.repairSpeed * Time.delta * strength * getPowerEfficiency(build));
                    float dest = build.angleTo(repairTarget);
                    targetTurn(build, dest);
                    targetIsBeingRepaired = true;
                }
                if(targetIsBeingRepaired) strength = Mathf.lerpDelta(strength, 1f, 0.08f * Time.delta);
            }
            else strength = Mathf.lerpDelta(strength, 0f, 0.07f * Time.delta);
        }

        @Override
        public void draw(MultiTurret.MultiTurretBuild build) {
            super.draw(build);
            if(!(repairTarget != null && Angles.angleDist(build.angleTo(repairTarget), rotation) < 30f)) return;

            float[] loc = mountLocations(build);
            Draw.z(Layer.flyingUnit + 1); //above all units
            float ang = build.angleTo(repairTarget);
            float len = 5f + Mathf.absin(Time.time, 1.1f, 0.5f);
            float swingScl = 12f, swingMag = tilesize / 8f;
            float scl = repairTarget.hitSize / 8f;
            float random = Mathf.randomSeedRange(build.id, scl / 2f);
            Draw.color(type.laserColor);
            Drawf.laser(type.laser, type.laserEnd,
                    loc[2] + Angles.trnsx(ang, len), loc[3] + Angles.trnsy(ang, len),
                    repairTarget.x +
                            Mathf.sin((Time.time + 6 * 8f) * scl / 3, (swingScl + random) * scl, swingMag * scl),
                    repairTarget.y +
                            Mathf.sin((Time.time + 6 * 8f) * scl / 3, ((swingScl + random) + 2f) * scl, swingMag * scl)
                    , strength);
        }
    }
}
