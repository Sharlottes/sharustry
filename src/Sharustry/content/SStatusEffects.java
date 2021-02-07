package Sharustry.content;

import mindustry.ctype.*;
import mindustry.type.*;

public class SStatusEffects implements ContentList{
    public static StatusEffect boost;

    @Override
    public void load(){
        boost = new StatusEffect("boost"){{
            speedMultiplier = 2f;
            reloadMultiplier = 2f;
            damageMultiplier = 2f;
            effectChance = 0.25f;
            effect = SFx.boost;
        }};
    }
}
