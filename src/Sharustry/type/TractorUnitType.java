package Sharustry.type;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.entities.Units;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;

public class TractorUnitType extends UnitType {
    public float tractRange = 8 * 10f;
    public float tractLaserWidth = 0.6f;
    public float tractForce = 4f;
    public float tractScaledForce = 3f;
    public float tractStatusDuration = 300;
    public float shootLength = 3f;
    public float damage = 0f;
    public int targetAmount = 1;

    public Color tractColor = Color.white;
    public StatusEffect tractStatus = StatusEffects.none;
    TextureRegion tractLaser, tractLaserEnd;

    public TractorUnitType(String name){
        super(name);
    }

    @Override
    public void load() {
        super.load();
        tractLaser = Core.atlas.find("shar-tlaser");
        tractLaserEnd = Core.atlas.find("shar-tlaser-end");
    }

    @Override
    public void draw(Unit unit) {
        super.draw(unit);

        Seq<Unit> tractTargets = new Seq<>();
        for(int i = 0; i < targetAmount; i++){
            final int j = i;
            tractTargets.add(Units.closestEnemy(unit.team, unit.x, unit.y, tractRange, u -> {
                boolean istarget = true;
                if(j >= 1) istarget = u != tractTargets.get(j - 1);

                return istarget;
            }));
        }

        for(int i = 0; i < tractTargets.size; i++) {
            if (!unit.disarmed()
                    && (unit.ammo > 0 || !Vars.state.rules.unitAmmo || unit.team().rules().infiniteAmmo)
                    && tractTargets.get(i) != null
                    && tractTargets.get(i).within(unit, tractRange + tractTargets.get(i).hitSize / 2f)
                    && tractTargets.get(i).team() != unit.team
                    && Angles.within(unit.rotation(), unit.angleTo(tractTargets.get(i)), 25)) {
                Draw.z(Layer.bullet);
                float ang = unit.angleTo(tractTargets.get(i).x, tractTargets.get(i).y);

                Draw.mixcol(tractColor, Mathf.absin(4f, 0.6f));

                Drawf.laser(unit.team, tractLaser, tractLaserEnd,
                        unit.x + Angles.trnsx(ang, shootLength), unit.y + Angles.trnsy(ang, shootLength),
                        tractTargets.get(i).x, tractTargets.get(i).y, unit.shieldAlpha * tractLaserWidth);
                Draw.mixcol();
            }
        }
    }

    @Override
    public void update(Unit unit) {
        super.update(unit);

        Seq<Unit> tractTargets = new Seq<>();
        for(int i = 0; i < targetAmount; i++){
            final int j = i;
            tractTargets.add(Units.closestEnemy(unit.team, unit.x, unit.y, tractRange, u -> {
                boolean istarget = true;
                if(j >= 1) istarget = u != tractTargets.get(j - 1);

                return istarget;
            }));
        }

        for(int i = 0; i < tractTargets.size; i++) {
            if (!unit.disarmed()
                    && (unit.ammo > 0 || !Vars.state.rules.unitAmmo || unit.team().rules().infiniteAmmo)
                    && tractTargets.get(i) != null
                    && tractTargets.get(i).within(unit, tractRange + tractTargets.get(i).hitSize / 2f)
                    && tractTargets.get(i).team() != unit.team
                    && Angles.within(unit.rotation(), unit.angleTo(tractTargets.get(i)), 25)) {
                float dest = unit.angleTo(tractTargets.get(i));
                unit.rotation(Angles.moveToward(unit.rotation, dest, rotateSpeed * Time.delta));
                unit.shieldAlpha = Mathf.lerpDelta(unit.shieldAlpha, 1f, 0.1f);
                if (damage > 0) tractTargets.get(i).damageContinuous(damage);

                if (tractStatus != StatusEffects.none) tractTargets.get(i).apply(tractStatus, tractStatusDuration);
                tractTargets.get(i).impulseNet(Tmp.v1.set(unit).sub(tractTargets.get(i)).limit((tractForce + (1f - tractTargets.get(i).dst(unit) / tractRange) * tractScaledForce)));
            } else {
                unit.shieldAlpha = Mathf.lerpDelta(unit.shieldAlpha, 0, 0.1f);
            }
            unit.ammo--;
            if (unit.ammo < 0) unit.ammo = 0;
        }
    }
}
