package Sharustry.entities.bullet.construct;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Intersector;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.graphics.Layer;

import static arc.graphics.g2d.Fill.*;

public class ForceShieldConstructBulletType extends ConstructBulletType {
    public float radius = 60f;
    public float regen = 0.1f;
    public float max = 200f;
    public float cooldown = 60f * 5;

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
        float realRad = ((Float[])paramBullet.data)[2];
        if(((Float[])paramBullet.data)[0] < max){
            ((Float[])paramBullet.data)[0] += Time.delta * regen;
        }

        ((Float[])paramBullet.data)[1] = Math.max(((Float[])paramBullet.data)[1] - Time.delta/10f, 0f);

        if(((Float[])paramBullet.data)[0] > 0){
            ((Float[])paramBullet.data)[3] = Mathf.lerpDelta(((Float[])paramBullet.data)[3], 1f, 0.06f);
            paramBullet = b;
            paramField = this;
            checkRadius(b);

            Groups.bullet.intersect(b.x - realRad, b.y - realRad, realRad * 2f, realRad * 2f, shieldConsumer);
        }else{
            ((Float[])paramBullet.data)[3] = 0f;
        }
    }

    @Override
    public void draw(Bullet b) {
        super.draw(b);

        checkRadius(b);

        if(((Float[])paramBullet.data)[0] > 0){
            Draw.z(Layer.shields);

            Draw.color(b.team.color, Color.white, Mathf.clamp(((Float[])paramBullet.data)[1]));

            if(Core.settings.getBool("animatedshields")){
                poly(b.x, b.y, 6, ((Float[])paramBullet.data)[2]);
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
    public void init(Bullet b) {
        super.init(b);

        b.data = new Float[]{0f, 0f, 0f, 0f}; //shield, shield alpha for drawing, realRad, radScl
    }

    public void checkRadius(Bullet b){
        //timer2 is used to store radius scale as an effect
        ((Float[])b.data)[2] = ((Float[])b.data)[3] * radius;
    }
}
