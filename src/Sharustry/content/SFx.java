package Sharustry.content;

import Sharustry.world.blocks.defense.ShieldWall;
import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Structs;
import arc.util.Tmp;
import mindustry.content.Items;
import mindustry.entities.*;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.gen.Groups;
import mindustry.graphics.*;
import Sharustry.graphics.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;

public class SFx {
    public static final Effect
    missileDead = new Effect(30, e -> {
        Lines.stroke(1.5f*e.fout(), Pal.lancerLaser);
        Lines.circle(e.x, e.y, e.fin()*12);
    }),
    mineExplode = new Effect(360, e -> {
        float cooldown = (float)e.data;
        float range = 30 * 8;
        e.scaled(cooldown, e1 -> {
                Draw.color(Pal.health, e1.fout(0.125f));
        Lines.arc(e.x, e.y, range/3, 70 * (e1.fin(Interp.pow2)), 0, 60);
    });

        if(e.time >= cooldown){
            e.scaled(40 + cooldown, e1 ->{
                Draw.color(Pal.health, e1.fout(40/e1.lifetime));
                Lines.circle(e.x, e.y, range/3 + range*2/3 * (1-e1.fout(40/e1.lifetime)));
            });
        }
    }),

    snipeLine = new Effect(10, e -> {
        e.lifetime = ((Float[])e.data)[0];
        Lines.stroke(1.5f, Pal.lancerLaser.cpy().lerp(Pal.health, 0.75f).lerp(Color.white, Mathf.absin(1, e.fin() * 1.5f)));
        Vec2 tr = Tmp.v1.set(e.x, e.y).trns(e.rotation, ((Float[])e.data)[1]);
        Lines.line(e.x, e.y, e.x+tr.x, e.y+tr.y);
    }),

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
        color(SPal.cryoium(e));
        Fill.square(e.x, e.y, e.fslope() * 4f, 45f);
    }),

    balkanShoot = new Effect(21f, e -> {
        color(SPal.cryoium(e));
        for(int i : Mathf.signs)
            Drawf.tri(e.x, e.y, 4f * e.fout(), 29*1.5f, e.rotation + 90f * i);
    }),

    traislarShoot = new Effect(45f, e -> {
        for(int r : Mathf.signs) {
            for(int i = 0; i < 2; i++){
                Draw.color(Pal.lancerLaser.cpy().lerp(Items.titanium.color,0.5f * i).a(1 - 0.5f * e.fin()));

                float m = 1 - 0.5f * i;
                float rot = e.rotation + Mathf.lerp(60, 30, e.fin()) * r;
                float w = 15 * e.fout() * m;
                Drawf.tri(e.x, e.y, w, (30 + 15) * m, rot + 180);
                Drawf.tri(e.x, e.y, w, 10 * m, rot);
            }

            Drawf.light(e.x, e.y, 60, Pal.bulletYellowBack, 0.6f * e.fout());
            e.scaled(30f, ee -> Drawf.tri(e.x, e.y, 4 * ee.fout(), 29 * 1.5f, e.rotation + Mathf.lerp(90,120, ee.fin()) * r));
        }
    }),

    balkanChargeBegin = new Effect(60f, 100f, e -> {
        stroke(e.fin(), SPal.cryoium(e));
        Lines.circle(e.x, e.y, 4f + e.fout() * 6f);

        Fill.circle(e.x, e.y, e.fin() * 3f);
        color();
        Fill.circle(e.x, e.y, e.fin() * 2f);
    }),
    balkanChargeCircles = new Effect(50, e -> {
        color(SPal.cryoium(e));
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
    }),

    hitLaserS = new Effect(8, e -> {
        color(Color.white, SPal.paradium, e.fin());
        stroke(0.25f + e.fout());
        Lines.circle(e.x, e.y, e.fin() * 2f);
    });

    public static void part2(Effect.EffectContainer eh, float range) {
        Color toCol = Pal.lancerLaser;

        eh.scaled(360, ea -> {
            ea.lifetime = 360;
            Draw.color(Pal.accent);

            ea.scaled(40, e1 -> Lines.arc(eh.x, eh.y, range, Mathf.round(70 * (e1.fin(Interp.pow2))), 0, 60));
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
            ea.scaled(60 + 180, e2 -> Groups.unit.each(u -> Mathf.dst(ea.x, ea.y, u.x, u.y) <= range && Structs.contains(u.abilities,  a -> a instanceof ForceFieldAbility), u -> {
                Color col1 = Pal.accent.cpy();
                col1.a = 0.7f * e2.fin() + 0.25f *  Mathf.sin(e2.fin() * 15);
                Draw.color(Pal.accent, col1, Mathf.sin(e2.fin() * 15));
                Lines.circle(u.x, u.y, u.hitSize + 8 * Mathf.sin(e2.fin() * 15));
            }));
        });
    };

    public static void part1(Effect.EffectContainer ew, float range) {
        float[] xy = {0f, 0f};
        Groups.unit.each(u -> Mathf.dst(ew.x, ew.y, u.x, u.y) <= range && Structs.contains(u.abilities, a -> a instanceof ForceFieldAbility), target -> {
            long id = ew.id;
            float tx = target.x;
            float ty = target.y;
            float x0;
            float y0;
            float radius = target.hitSize * 1.3f;
            ew.scaled(30, e1 -> {
                xy[0] = Mathf.lerp(ew.x, tx, e1.fin(Interp.pow2));
                xy[1] = Mathf.lerp(ew.y, ty, e1.fin(Interp.pow2));
            });
            x0 = xy[0];
            y0 = xy[1];
            Draw.color(Pal.accent, ew.fout(0.5f));
            Drawf.laser(Core.atlas.find("laser"), Core.atlas.find("laser-end"), ew.x, ew.y,  x0, y0, ew.fout(0.5f));
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
