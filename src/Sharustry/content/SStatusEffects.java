package Sharustry.content;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Nullable;
import arc.util.Tmp;
import mindustry.ctype.*;
import mindustry.entities.Lightning;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.*;

import static mindustry.content.StatusEffects.*;

public class SStatusEffects implements ContentList{
    public static StatusEffect overFreezing;

    @Override
    public void load(){
        overFreezing = new StatusEffect("over-freezing"){
            {
                color = Color.valueOf("6ecdec").cpy().lerp(Pal.lancerLaser, 0.5f);
                speedMultiplier = 0.45f;
                healthMultiplier = 0.5f;
                transitionDamage = 40f;
                effectChance = 0.05f;

                init(() -> {
                    opposite(melting, burning);

                    affinity(blasted, ((unit, time, newTime, result) -> {
                        unit.damagePierce(transitionDamage);
                        result.set(overFreezing, time);
                    }));

                    affinity(freezing, ((unit, time, newTime, result) -> {
                        result.set(overFreezing, Math.min(time + newTime, 300f));
                    }));
                });
                effect = SFx.boost;
            }


            @Override
            public void update(Unit unit, float time){
                float lightningDamage = 10;
                float lightning = 2;
                float lightningLength = 10;

                Color lightningColor = Pal.surge;
                int lightningLengthRand = 0;
                float lightningCone = 360f;
                float lightningAngle = 0f;

                super.update(unit, time);
                if(Mathf.chanceDelta(effectChance)){
                    Tmp.v1.rnd(unit.type.hitSize /2f);
                    Tmp.v2.set(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y);
                    effect.at(Tmp.v2.x, Tmp.v2.y, unit.rotation);

                    for(int i = 0; i < lightning; i++){
                        Lightning.create(Team.derelict, lightningColor, lightningDamage < 0 ? damage : lightningDamage, Tmp.v2.x, Tmp.v2.y, unit.rotation() + Mathf.range(lightningCone/2) + lightningAngle, (int) (lightningLength + Mathf.random(lightningLengthRand)));
                    }
                }
            }
        };
    }
}
