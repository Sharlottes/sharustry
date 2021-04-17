package Sharustry.ai.types;

import arc.math.*;
import arc.math.geom.Position;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.ai.types.FlyingAI;
import mindustry.entities.Units;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class TractorAI extends FlyingAI {
    public float tractRange = 10 * 8;

    @Override
    protected void moveTo(Position target, float circleLength, float smooth){
        if(target == null) return;

        vec.set(target).sub(unit);

        float length = circleLength <= 0.001f ? 1f : Mathf.clamp((unit.dst(target) - circleLength) / smooth, -1f, 1f);

        vec.setLength(unit.realSpeed() * length);
        if(length < -0.5f){
            @Nullable Building build;
            Building build1 = Units.findAllyTile(unit.team(), unit.x, unit.y,tractRange, b -> b instanceof Turret.TurretBuild && ((Turret.TurretBuild)b).hasAmmo());
            Building build2 = Units.findAllyTile(unit.team(), unit.x, unit.y,100 * 8, b -> b instanceof Turret.TurretBuild && ((Turret.TurretBuild)b).hasAmmo());
            if(build1 == null) build = build2;
            else build = build1;

            vec.rotate(180f);
            if(build != null) unit.vel.trns(Angles.angle(((Unit)target).x, ((Unit)target).y, build.x, build.y), 2f);

            Log.info(build + "   " + (build != null ? Angles.angle(((Unit)target).x, ((Unit)target).y, build.x, build.y) : ""));
        }else if(length < 0){
            vec.setZero();
        }

        unit.moveAt(vec);
    }

    @Override
    public void updateMovement(){
        Unit target = (Unit) findTarget(unit.x, unit.y, tractRange, true, true);

        if(target != null && command() == UnitCommand.attack){
            moveTo(target, Math.max(tractRange, Math.max(tractRange, ((Unit) target).hitSize())) - 16f, 16f);
            unit.lookAt(target);
        }

        if(target == null){
            Unitc h = (Unitc) findTarget(unit.x, unit.y, 100 * 8, true, true);
            if(h == null) {
                if(command() == UnitCommand.attack && state.rules.waves && unit.team == state.rules.defaultTeam) moveTo(getClosestSpawner(), state.rules.dropZoneRadius + 120f);
            }
            moveTo(h, Math.max(tractRange, Math.max(tractRange, (h != null ? h.hitSize() : 0))) - 16f, 16f);
        }

        if(command() == UnitCommand.rally)
            moveTo(targetFlag(unit.x, unit.y, BlockFlag.rally, false), 60f);

    }

    @Override
    protected Teamc findTarget(float x, float y, float range, boolean air, boolean ground){
        Teamc result = target(x, y, range, air, ground);
        if(result != null) return result;

        return null;
    }
}

