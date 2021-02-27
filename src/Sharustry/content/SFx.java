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
        Draw.color(Pal.lightOrange, Items.pyratite.color, e.fin());
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
        else color(e.color);

        Fill.square(e.x, e.y, e.fslope() * 4f, 45f);
    }),

    balkanShoot = new Effect(21f, e -> {
        if(e.color == null) color(SPal.cryoium(e));
        else color(e.color);

        for(int i : Mathf.signs){
            Drawf.tri(e.x, e.y, 4f * e.fout(), 29*1.5f, e.rotation + 90f * i);
        }
    }),
    balkanChargeBegin = new Effect(60f, 100f, e -> {
        if(e.color == null) color(SPal.cryoium(e));
        else color(e.color);
        stroke(e.fin());
        Lines.circle(e.x, e.y, 4f + e.fout() * 6f);

        Fill.circle(e.x, e.y, e.fin() * 3f);

        color();
        Fill.circle(e.x, e.y, e.fin() * 2f);
    }),
    balkanChargeCircles = new Effect(38, e -> {
        if(e.color == null) color(SPal.cryoium(e));
        else color(e.color);
        stroke(e.fin());
        randLenVectors(e.id, 2, 1 +20f * e.fout(), (x, y) -> Fill.circle(e.x + x, e.y + y,  e.fslope() * 2f + 0.5f));
    }),
    balkanCharge = new MultiEffect(boost, balkanChargeCircles),

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
    }),

    basicHit = new ScaledEffect(14, 50, 0.75f, e -> {
        Draw.color(Color.white, Pal.lightOrange, e.fin());

        e.scaled(7, s -> {
                Lines.stroke((0.5f + s.fout()) * scl);
        Lines.circle(e.x, e.y, s.fin() * 5 * scl);
      });

        Lines.stroke((0.5f + e.fout()) * scl);

        Angles.randLenVectors(e.id, 5, e.fin() * 15 * scl, (x, y) -> {
            Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), (e.fout() * 3 + 1) * scl);
        });
    }),

    basicDespawn = new ScaledEffect(14, 50, 0.6f, e -> {
        Draw.color(Color.white, Pal.lightOrange, e.fin());

        e.scaled(7, s -> {
            Lines.stroke((0.5f + s.fout()) * scl);
            Lines.circle(e.x, e.y, s.fin() * 5 * scl);
        });

        Lines.stroke((0.5f + e.fout()) * scl);

        Angles.randLenVectors(e.id, 5, e.fin() * 15 * scl, (x, y) -> {
            Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), (e.fout() * 3 + 1) * scl);
        });
    }),

    smallBlastHit = new ScaledEffect(20, 50, 0.75f, e -> {
        Draw.color(Pal.bulletYellow);

        e.scaled(6, s -> {
            Lines.stroke(3 * s.fout() * scl);
            Lines.circle(e.x, e.y, (3 + s.fin() * 10) * scl);
        });

        Draw.color(Color.gray);

        Angles.randLenVectors(e.id, 5, (2 + 23 * e.finpow()) * scl, (x, y) -> Fill.circle(e.x + x, e.y + y, (e.fout() * 3 + 0.5f) * scl));

        Draw.color(Pal.lighterOrange);
        Lines.stroke(e.fout() * scl);

        Angles.randLenVectors(e.id + 1, 4, (1 + 23 * e.finpow()) * scl, (x, y) -> Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), (1 + e.fout() * 3) * scl));
    });

}
