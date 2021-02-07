package Sharustry.content;

import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.entities.effect.*;
import Sharustry.graphics.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;

public class SFx {
    public static final Effect
    boost = new Effect(50f, e -> {
        color(SPal.cryoium(e));

        Fill.square(e.x, e.y, e.fslope() * 4f, 45f);
    }),

    balkanShoot = new Effect(21f, e -> {
        color(SPal.cryoium(e));

        for(int i : Mathf.signs){
            Drawf.tri(e.x, e.y, 4f * e.fout(), 29*1.5f, e.rotation + 90f * i);
        }
    }),
    balkanChargeBegin = new Effect(60f, 100f, e -> {
        color(SPal.cryoium(e));
        stroke(e.fin());
        Lines.circle(e.x, e.y, 4f + e.fout() * 6f);

        Fill.circle(e.x, e.y, e.fin() * 3f);

        color();
        Fill.circle(e.x, e.y, e.fin() * 2f);
    }),
    balkanChargeCircles = new Effect(38, e -> {
        color(SPal.cryoium(e));
        stroke(e.fin());
        randLenVectors(e.id, 2, 1 +20f * e.fout(), (x, y) -> Fill.circle(e.x + x, e.y + y,  e.fslope() * 2f + 0.5f));
    }),
    balkanCharge = new MultiEffect(boost, balkanChargeCircles),

    blockShieldBreak = new Effect(35, e -> {
        if(!(e.data instanceof Buildingc)) return;
        Building build = e.data();

        float radius = build.block.size * build.block.size * 1.3f;

        e.scaled(16f, c -> {
            color(Pal.shield);
            stroke(c.fout() * 2f + 0.1f);

            randLenVectors(e.id, (int)(radius * 1.2f), radius/2f + c.finpow() * radius*1.25f, (x, y) -> {
                lineAngle(c.x + x, c.y + y, Mathf.angle(x, y), c.fout() * 5 + 1f);
            });
        });

        color(Pal.shield, e.fout());
        stroke(e.fout());
        Lines.circle(e.x, e.y, radius);
    });
}
