package Sharustry.world.blocks.defense.turret.mounts;

import Sharustry.world.blocks.defense.turret.MultiTurret;
import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.Vars;
import mindustry.entities.Units;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.meta.Stat;

import static mindustry.Vars.player;
import static mindustry.Vars.tilesize;

public class RepairMountTurretType extends MountTurretType {
    public static final Rect rect = new Rect();
    public float repairSpeed = 0.3f;
    public RepairMountTurretType(String name) {
        super(name);
    }

    @Override
    public MountTurret create(MultiTurret block, MultiTurret.MultiTurretBuild build, int index, float x, float y) {
        return new RepairMountTurret(this, block, build, index, x, y);
    }
    @Override
    public void buildStat(Table table) {
        super.buildStat(table);
        table.add("[lightgray]" + Stat.range.localized() + ": [white]" + Core.bundle.format("stat.shar.range", range / Vars.tilesize)).row();
    }

    public class RepairMountTurret extends MountTurret<RepairMountTurretType> {
        Unit repairTarget;

        public RepairMountTurret(RepairMountTurretType type, MultiTurret block, MultiTurret.MultiTurretBuild build, int i, float x, float y) {
            super(type, block, build, i, x, y);
        }

        @Override
        public void updateTile() {
            super.updateTile();

            repairTarget = Units.closest(build.team, x, y, type.range, Unit::damaged);
            boolean targetIsBeingRepaired = false;
            if(repairTarget != null){
                if(repairTarget.dead()
                        || repairTarget.dst(x, y) - repairTarget.hitSize / 2f > range
                        || repairTarget.health() >= repairTarget.maxHealth()) repairTarget = null;
                else {
                    repairTarget.heal(type.repairSpeed * Time.delta * strength * getPowerEfficiency());
                    float dest = build.angleTo(repairTarget);
                    turnToTarget(dest);
                    targetIsBeingRepaired = true;
                }
                if(targetIsBeingRepaired) strength = Mathf.lerpDelta(strength, 1f, 0.08f * Time.delta);
            }
            else strength = Mathf.lerpDelta(strength, 0f, 0.07f * Time.delta);
        }

        @Override
        public void draw() {
            super.draw();
            if(!(repairTarget != null && Angles.angleDist(build.angleTo(repairTarget), rotation) < 30f)) return;

            Draw.z(Layer.flyingUnit + 1); //above all units
            float ang = build.angleTo(repairTarget);
            float len = 5f + Mathf.absin(Time.time, 1.1f, 0.5f);
            float swingScl = 12f, swingMag = tilesize / 8f;
            float scl = repairTarget.hitSize / 8f;
            float random = Mathf.randomSeedRange(build.id, scl / 2f);
            Draw.color(type.laserColor);
            Drawf.laser(type.laser, type.laserEnd,
                    x + Angles.trnsx(ang, len), y + Angles.trnsy(ang, len),
                    repairTarget.x +
                            Mathf.sin((Time.time + 6 * 8f) * scl / 3, (swingScl + random) * scl, swingMag * scl),
                    repairTarget.y +
                            Mathf.sin((Time.time + 6 * 8f) * scl / 3, ((swingScl + random) + 2f) * scl, swingMag * scl)
                    , strength);
        }
    }
}
