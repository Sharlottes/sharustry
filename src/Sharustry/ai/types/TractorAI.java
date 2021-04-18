package Sharustry.ai.types;

import arc.math.*;
import arc.math.geom.Position;
import mindustry.ai.types.FlyingAI;
import mindustry.entities.Units;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class TractorAI extends FlyingAI {
    public float tractRange = 50 * 8;
    public float approach = 2 * 8;
    @Override
    protected void moveTo(Position target, float circleLength, float smooth){
        if(target == null) return;

        vec.set(target).sub(unit);

        float length = circleLength <= 0.001f ? 1f : Mathf.clamp((unit.dst(target) - circleLength) / smooth, -1f, 1f);

        vec.setLength(unit.realSpeed() * length);
        if(length < -0.5f){
            Building build = Units.findAllyTile(unit.team(), unit.x, unit.y, tractRange, b -> b instanceof Turret.TurretBuild && ((Turret.TurretBuild)b).hasAmmo());

            vec.rotate(180f);
            if(build != null) unit.vel.trns(Angles.angle(((Unit)target).x, ((Unit)target).y, build.x, build.y), 2f);
        }else if(length < 0){
            vec.setZero();
        }

        unit.moveAt(vec);
    }

    @Override
    public void updateMovement(){
        Unit target = Units.closestEnemy(unit.team, unit.x, unit.y, tractRange, u -> true);

        if(target != null && command() == UnitCommand.attack){
            moveTo(target, Math.max(unit.range(), Math.max(unit.range(), target.hitSize())) - 16f, approach);
            unit.lookAt(target);
        }

        if(target == null)
            if(command() == UnitCommand.attack && state.rules.waves && unit.team == state.rules.defaultTeam) moveTo(getClosestSpawner(), state.rules.dropZoneRadius + 120f);


        if(command() == UnitCommand.rally)
            moveTo(targetFlag(unit.x, unit.y, BlockFlag.rally, false), 60f);

    }
}

