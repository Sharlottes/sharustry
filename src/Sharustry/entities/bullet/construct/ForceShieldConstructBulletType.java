package Sharustry.entities.bullet.construct;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Intersector;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.graphics.Layer;

import static arc.graphics.g2d.Fill.*;

public class ForceShieldConstructBulletType extends ConstructBulletType {
    public boolean skipCol = false;
    public float radius = 60f;
    public float regen = 0.4f;
    public float max = 500f;
    public float cooldown = 60f * 3;
    public Color shieldColor;

    public static Bullet paramBullet;
    public static ForceShieldConstructBulletType paramField;
    public static final Cons<Bullet> shieldConsumer = trait -> {
        if(trait.team != paramBullet.team && trait.type.absorbable && Intersector.isInsideHexagon(paramBullet.x, paramBullet.y, ((Float[])paramBullet.data)[2] * 2f, trait.x(), trait.y()) && ((Float[])paramBullet.data)[0] > 0){
            trait.absorb();
            Fx.absorb.at(trait);

            //break shield
            if(((Float[])paramBullet.data)[0] <= trait.damage()){
                ((Float[])paramBullet.data)[0] -= paramField.cooldown * paramField.regen;

                Fx.shieldBreak.at(paramBullet.x, paramBullet.y, paramField.radius, paramBullet.team.color);
            }

            ((Float[])paramBullet.data)[0] -= trait.damage();
            ((Float[])paramBullet.data)[1] = 1f;
        }
    };

    @Override
    public void update(Bullet b) {
        super.update(b);
        float realRad = ((Float[])b.data)[2];
        if(((Float[])b.data)[0] < max){
            ((Float[])b.data)[0] += Time.delta * regen;
        }

        ((Float[])b.data)[1] = Math.max(((Float[])b.data)[1] - Time.delta/10f, 0f);

        if(((Float[])b.data)[0] > 0){
            ((Float[])b.data)[3] = Mathf.lerpDelta(((Float[])b.data)[3], 1f, 0.06f);
            paramBullet = b;
            paramField = this;
            checkRadius(b);

            Groups.bullet.intersect(b.x - realRad, b.y - realRad, realRad * 2f, realRad * 2f, shieldConsumer);
        }else{
            ((Float[])b.data)[3] = 0f;
        }
    }

    @Override
    public void draw(Bullet b) {
        Color mix = Tmp.c1.set(mixColorFrom).lerp(mixColorTo, b.fin());

        float offset = -90 + (spin != 0 ? Mathf.randomSeed(b.id, 360f) + b.time * spin : 0f);
        Draw.mixcol(mix, mix.a);
        if(!skipCol) Draw.color(backColor);
        Draw.rect(backRegion, b.x, b.y, width, height, b.rotation() + offset + b.vel.len() * 200 * ((Float[]) b.data)[4]);
        if(!skipCol) Draw.color(frontColor);
        Draw.rect(frontRegion, b.x, b.y, width, height, b.rotation() + offset + b.vel.len() * 200 * ((Float[]) b.data)[4]);

        Draw.reset();

        checkRadius(b);

        if(((Float[])b.data)[0] > 0){
            Draw.z(Layer.shields);

            Draw.color(shieldColor, Color.white, Mathf.clamp(((Float[])b.data)[1]));

            if(Core.settings.getBool("animatedshields")){
                poly(b.x, b.y, 6, ((Float[])b.data)[2]);
            }else{
                Lines.stroke(1.5f);
                Draw.alpha(0.09f);
                poly(b.x, b.y, 6, radius);
                Draw.alpha(1f);
                Lines.poly(b.x, b.y, 6, radius);
            }
        }
    }

    @Override
    public void hit(Bullet b) {
        super.hit(b);
        checkRadius(b);
        Fx.forceShrink.at(b.x, b.y, ((Float[])b.data)[2], shieldColor);
    }

    @Override
    public void init(Bullet b) {
        super.init(b);

        b.data = new Float[]{0f, 0f, 0f, 0f, Mathf.random(0.5f, 1)}; //shield, shield alpha for drawing, realRad, radScl
    }

    public void checkRadius(Bullet b){
        //timer2 is used to store radius scale as an effect
        ((Float[])b.data)[2] = ((Float[])b.data)[3] * radius;
    }
}
