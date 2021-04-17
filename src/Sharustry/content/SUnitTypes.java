package Sharustry.content;

import Sharustry.ai.types.TractorAI;
import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.ctype.ContentList;
import mindustry.entities.Units;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.AmmoTypes;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;

public class SUnitTypes implements ContentList {
    public static UnitType momo;

    @Override
    public void load() {
        momo = new UnitType("momo"){{
            defaultController = () -> new TractorAI(){{
                tractRange = 8 * 15f;
            }};
            flying = true;
            drag = 0.06f;
            accel = 0.12f;
            speed = 1.5f;
            health = 100;
            engineSize = 1.8f;
            engineOffset = 5.7f;
            range = 8 * 15f;
            isCounted = false;

            ammoType = AmmoTypes.powerLow;

            mineTier = 1;
            mineSpeed = 2.5f;

            constructor = UnitEntity::create;
        }
            final float tractRange = 8 * 15f;
            final float tractLaserWidth = 0.6f;
            final float tractForce = 4f;
            final float tractScaledForce = 3f;
            final float tractStatusDuration = 300;
            final float shootLength = 3f;
            final Color tractColor = Color.white;
            final StatusEffect tractStatus = StatusEffects.none;
            TextureRegion tractLaser, tractLaserEnd;

            @Override
            public void load() {
                super.load();
                tractLaser = Core.atlas.find("shar-tlaser");
                tractLaserEnd = Core.atlas.find("shar-tlaser-end");
            }

            @Override
            public void draw(Unit unit) {
                super.draw(unit);

                Unit tractTarget = Units.closestEnemy(unit.team, unit.x, unit.y, tractRange, u -> true);

                if(!unit.disarmed()
                        && (unit.ammo > 0 || !Vars.state.rules.unitAmmo || unit.team().rules().infiniteAmmo)
                        && tractTarget != null
                        && tractTarget.within(unit, tractRange + tractTarget.hitSize/2f)
                        && tractTarget.team() != unit.team
                        && Angles.within(unit.rotation(), unit.angleTo(tractTarget), 25)) {
                    Draw.z(Layer.bullet);
                    float ang = unit.angleTo(tractTarget.x, tractTarget.y);

                    Draw.mixcol(tractColor, Mathf.absin(4f, 0.6f));

                    Drawf.laser(unit.team, tractLaser, tractLaserEnd,
                            unit.x + Angles.trnsx(ang, shootLength), unit.y + Angles.trnsy(ang, shootLength),
                            tractTarget.x, tractTarget.y, unit.shieldAlpha * tractLaserWidth);
                    Draw.mixcol();
                }
            }

            @Override
            public void update(Unit unit) {
                super.update(unit);

                Unit tractTarget = Units.closestEnemy(unit.team, unit.x, unit.y, tractRange, u -> true);

                if(!unit.disarmed()
                        && (unit.ammo > 0 || !Vars.state.rules.unitAmmo || unit.team().rules().infiniteAmmo)
                        && tractTarget != null
                        && tractTarget.within(unit, tractRange + tractTarget.hitSize/2f)
                        && tractTarget.team() != unit.team
                        && Angles.within(unit.rotation(), unit.angleTo(tractTarget), 25)){
                    float dest = unit.angleTo(tractTarget);
                    unit.rotation(Angles.moveToward(unit.rotation, dest, rotateSpeed * Time.delta));
                    unit.shieldAlpha = Mathf.lerpDelta(unit.shieldAlpha, 1f, 0.1f);

                    if(tractStatus != StatusEffects.none) tractTarget.apply(tractStatus, tractStatusDuration);
                    tractTarget.impulseNet(Tmp.v1.set(unit).sub(tractTarget).limit((tractForce + (1f - tractTarget.dst(unit) / tractRange) * tractScaledForce)));
                }else {
                    unit.shieldAlpha = Mathf.lerpDelta(unit.shieldAlpha, 0, 0.1f);
                }
                unit.ammo--;
                if(unit.ammo < 0) unit.ammo = 0;
            }
        };
    }
}
