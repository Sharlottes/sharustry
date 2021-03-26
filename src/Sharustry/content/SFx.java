package Sharustry.content;

import Sharustry.world.blocks.defense.ShieldWall;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.content.Items;
import mindustry.entities.*;
import mindustry.graphics.*;
import mindustry.entities.effect.*;
import Sharustry.graphics.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;

public class SFx {
    public static final Effect

    distSplashFx = new Effect(80, e -> {
        Draw.color((Color) e.data, e.color, e.fin());
        Lines.stroke(2 * e.fout());
        Lines.circle(e.x, e.y, 85*e.fin());
    }),

    skill = new Effect(40f, e -> {
        Draw.color(e.color);
        Fill.circle(e.x, e.y, 4 * e.fout());

        if(e.fout() <= 0.5) {
            e.scaled(60, e1 -> {
                Lines.stroke(e.fout(0.5f) * 4);
                Lines.circle(e1.x, e1.y, -1 + (1 - e.fout(0.5f)) * 4);

                Angles.randLenVectors(e1.id, 8, 2 + 10 * (1 - e.fout(0.5f)), (x, y) -> {
                    Lines.lineAngle(e1.x + x, e1.y + y, Mathf.angle(x, y), ((0.5f - Math.abs((1 - e.fout(0.5f)) - 0.5f)) * 2) * 4 + 1);
                });
             });
        }
    }),
    boost = new Effect(50f, e -> {
        if(e.color == null) color(SPal.cryoium(e));
        else if(e.color == Color.white) color(SPal.cryoium(e));
        else color(e.color);

        Fill.square(e.x, e.y, e.fslope() * 4f, 45f);
    }),

    balkanShoot = new Effect(21f, e -> {
        if(e.color == null) color(SPal.cryoium(e));
        else if(e.color == Color.white) color(SPal.cryoium(e));
        else color(e.color);

        for(int i : Mathf.signs){
            Drawf.tri(e.x, e.y, 4f * e.fout(), 29*1.5f, e.rotation + 90f * i);
        }
    }),
    balkanChargeBegin = new Effect(60f, 100f, e -> {
        if(e.color == null) color(SPal.cryoium(e));
        else if(e.color == Color.white) color(SPal.cryoium(e));
        else color(e.color);

        stroke(e.fin());
        Lines.circle(e.x, e.y, 4f + e.fout() * 6f);

        Fill.circle(e.x, e.y, e.fin() * 3f);

        color();
        Fill.circle(e.x, e.y, e.fin() * 2f);
    }),
    balkanChargeCircles = new Effect(50, e -> {
        if(e.color == null) color(SPal.cryoium(e));
        else if(e.color == Color.white) color(SPal.cryoium(e));
        else color(e.color);
        Fill.square(e.x, e.y, e.fslope() * 4f, 45f);
        stroke(e.fin());
        e.scaled(38, e1 -> randLenVectors(e.id, 2, 1 +20f * e1.fout(), (x, y) -> Fill.circle(e1.x + x, e1.y + y,  e1.fslope() * 2f + 0.5f)));
    }),

    blockShieldBreak = new Effect(35, e -> {
        if(!(e.data instanceof ShieldWall.ShieldWallBuild)) return;
        ShieldWall.ShieldWallBuild build = e.data();

        float radius = build.block.size * build.block.size * 1.3f;

        e.scaled(16f, c -> {
            color(Pal.shield);
            stroke(c.fout() * 2f + 0.1f);

            randLenVectors(e.id, (int)(radius * 1.2f), radius / 2f + c.finpow() * radius * 1.25f, (x, y) -> lineAngle(c.x + x, c.y + y, Mathf.angle(x, y), c.fout() * 5 + 1f));
        });

        color(Pal.shield, e.fout());
        stroke(e.fout());
        Lines.circle(e.x, e.y, radius);
    });

}
