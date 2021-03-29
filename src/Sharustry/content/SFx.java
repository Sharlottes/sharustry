package Sharustry.content;

import Sharustry.world.blocks.defense.ShieldWall;
import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.entities.*;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.graphics.*;
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

                Angles.randLenVectors(e1.id, 8, 2 + 10 * (1 - e.fout(0.5f)), (x, y) -> Lines.lineAngle(e1.x + x, e1.y + y, Mathf.angle(x, y), ((0.5f - Math.abs((1 - e.fout(0.5f)) - 0.5f)) * 2) * 4 + 1));
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
    }),

    shieldSpread = new Effect(360, e -> {
        float range = 100;

        e.scaled(60, e1 -> part1(e1, range));
        part2(e, range);
    });

    public static void part2(Effect.EffectContainer eh, float range) {
        Color toCol = Pal.lancerLaser;

        eh.scaled(360, ea -> {
        ea.lifetime = 360;
        Draw.color(Pal.accent);

        ea.scaled(40, e1 -> Lines.polySeg(60, 0, Mathf.round(70 * (e1.fin(Interp.pow2))), eh.x, eh.y, range, 0));
        if(ea.time >= 40){
            ea.scaled(40 + 20, e1 ->{
                    Draw.color(Pal.accent, 1);
            Lines.circle(eh.x, eh.y, range * e1.fout(20/60f));
            Draw.color(Pal.accent);
        });
        }
        if(ea.time >= 60){
            ea.scaled(60 + 60, e1 -> {
                    Draw.alpha(0.1f);
            Color col = Pal.accent.cpy();
            Color col1 = Pal.accent.cpy();

            col.a = ea.fin();
            col1.a = 0.7f * e1.fin() + 0.25f * Mathf.sin(ea.fin() * 30);

            col.lerp(toCol, 1- Mathf.sin(ea.fin() * 60));
            col1.lerp(toCol,Mathf.sin(ea.fin() * 60));

            Lines.circle(eh.x, eh.y, range * (e1.fout(30/40f)));
            Fill.light(eh.x, eh.y, Lines.circleVertices(range), range * ((0.5f - Math.abs((1 - e1.fout(30/40f)) - 0.5f)) * 2),  col,  col1);
        });
        }
        ea.scaled(60 + 180, e2 -> Groups.unit.each(u -> Mathf.dst(ea.x, ea.y, u.x, u.y) <= range && u.abilities.find(a -> a instanceof ForceFieldAbility) != null, u -> {
            Color col1 = Pal.accent.cpy();
            col1.a = 0.7f * e2.fin() + 0.25f *  Mathf.sin(e2.fin() * 15);
            Draw.color(Pal.accent, col1, Mathf.sin(e2.fin() * 15));
            Lines.circle(u.x, u.y, u.hitSize + 8 * Mathf.sin(e2.fin() * 15));
        }));
});

    }
    public static void part1(Effect.EffectContainer ew, float range) {
        Groups.unit.each(u -> Mathf.dst(ew.x, ew.y, u.x, u.y) <= range && u.abilities.find(a -> a instanceof ForceFieldAbility) != null, target -> {
            long id = ew.id;
            float tx = target.x;
            float ty = target.y;
            float x0;
            float y0;
            float radius = target.hitSize * 1.3f;
            float[] xy = {0f, 0f};
            ew.scaled(30, e1 -> {
                xy[0] = Mathf.lerp(ew.x, tx, e1.fin(Interp.pow2));
                xy[1] = Mathf.lerp(ew.y, ty, e1.fin(Interp.pow2));
            });
            x0 = xy[0];
            y0 = xy[1];
            Draw.color(Pal.accent, ew.fout(0.5f));
            Drawf.laser(Team.sharded, Core.atlas.find("laser"), Core.atlas.find("laser-end"), ew.x, ew.y,  x0, y0, ew.fout(0.5f));
            Draw.color(Pal.accent);

            if(ew.fout() <= 0.5) {
                ew.scaled(200, c -> {
                    Draw.color(Pal.accent, ew.fout(0.5f));
                    Lines.stroke(ew.fout(0.5f) * 1.2f + 0.1f);
                    Lines.circle(tx, ty, radius + (1 - ew.fout(0.5f)) * 8);

                    Angles.randLenVectors(id, (int)2.5f * Mathf.round(radius * 0.25f), radius/2 + Interp.pow3Out.apply(1 - ew.fout(0.5f)) * radius * 1.25f, (x, y) -> Lines.lineAngle(tx + x, ty + y, Mathf.angle(x, y), ((0.5f - Math.abs((1 - ew.fout(0.5f)) - 0.5f)) * 2) * 5 + 1));
                });
            }
        });
    }

}
