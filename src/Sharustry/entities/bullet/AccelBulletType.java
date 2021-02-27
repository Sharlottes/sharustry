package Sharustry.entities.bullet;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.struct.Seq;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;

public class AccelBulletType extends BasicBulletType {
    public float accelScl = 1;
    public float maxDamage = damage * 5;
    public Color trailColors = Pal.lancerLaser;

    public AccelBulletType(float speed, float damage){
        this.speed = speed;
        this.damage = damage;
        hitEffect = Fx.hitBulletSmall;
        despawnEffect = Fx.hitBulletSmall;
    }

    @Override
    public void init(Bullet b){
        b.data = new Seq<Trail>();
        ((Seq<Trail>)b.data).add(new Trail(6), new Trail(3));
    }

    @Override
    public void update(Bullet b) {
        super.update(b);

        b.vel().scl(1.1f * Time.delta);

        ((Seq<Trail>)b.data).each(t->t.update(b.x,b.y));
    }

    @Override
    public void hit(Bullet b, float x, float y){
        float mult = b.vel().len();
        b.damage += Math.min(maxDamage, accelScl * mult * mult);
        super.hit(b,x,y);
    }

    @Override
    public void draw(Bullet b){
        super.draw(b);
        Draw.color(trailColors);
        ((Seq<Trail>)b.data).each(t->t.draw(this.frontColor, this.width));

        Fill.square(b.x, b.y, this.width, b.rotation()+45);
    }
}
