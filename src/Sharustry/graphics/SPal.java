package Sharustry.graphics;

import arc.graphics.*;
import arc.math.Mathf;
import mindustry.content.Liquids;
import mindustry.entities.*;
import mindustry.graphics.Pal;

public class SPal {

    public static Color cryoium(Effect.EffectContainer e) {
        return Liquids.cryofluid.color.cpy().lerp(Color.white.cpy().mul(0.25f, 0.25f, 1f, e.fout()), e.fout() / 6f + Mathf.randomSeedRange(e.id, 0.1f));
    };
    public static Color
    cryoium = Liquids.cryofluid.color.cpy().lerp(Color.white.cpy().mul(0.25f, 0.25f, 1f, 1f), Mathf.range(0.1f)),
    paradium = Pal.lancerLaser.cpy().lerp(Pal.sap, 0.5f);
}
