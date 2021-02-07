package Sharustry.content;

import mindustry.graphics.*;
import mindustry.ctype.*;
import mindustry.type.*;

public class SLiquids implements ContentList{
    public static Liquid boostLiquid;

    @Override
    public void load() {
        boostLiquid = new Liquid("boost-liquid", Pal.lancerLaser){{
            effect = SStatusEffects.boost;
        }};
    }
}
