package Sharustry.entities.bullet;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Angles;
import arc.math.Interp;
import arc.struct.Seq;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.Units;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.turrets.Turret;

public class AccelBulletType extends BasicBulletType {
    public float pierceDec = 1, damageMultiplier = 1;
    public Color trailColors = Pal.lancerLaser;
    public boolean homing = false;

    public AccelBulletType(float speed, float damage){
        this.speed = speed;
        this.damage = damage;
        hitEffect = Fx.hitBulletSmall;
        despawnEffect = Fx.hitBulletSmall;
        this.lifetime+=15f;
    }

    @Override
    public void init(Bullet b){
        b.data = new BulletData(Seq.with(new Trail(6), new Trail(3)), 0);
    }

    @Override
    public void update(Bullet b) {
        super.update(b);
        if(b.time>=15f) {
            if (homing && b.owner instanceof Turret.TurretBuild build) {
                Posc target = build.target;
                if (target == null || ((BulletData)b.data).hitten)
                    target = Units.closestTarget(b.team, b.x, b.y, 50, e -> e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id), t -> collidesGround && !b.hasCollided(t.id));
                if (target != null)
                    b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), ((BulletData) b.data).hitten ? 5 * Time.delta : Math.min(5, build.dst(target) / 8) * Time.delta));

            }

            if(b.vel().len() < 15) b.vel().scl(1 + Interp.sineIn.apply(b.fin()) / 8);
            ((BulletData) b.data).trails.each(t -> t.update(b.x, b.y));
            b.damage += 1 + Interp.sineIn.apply(b.fin()) * damageMultiplier;
        }
    }

    @Override
    public void hit(Bullet b, float x, float y){
        b.damage *= pierceDec;
        super.hit(b,x,y);
        ((BulletData)b.data).hitten=true;
    }

    @Override
    public void draw(Bullet b){
        super.draw(b);
        Draw.color(trailColors);
        ((BulletData)b.data).trails.each(t->t.draw(this.frontColor, this.width));

        Fill.square(b.x, b.y, this.width, b.rotation()+45);
    }

    static class BulletData {
        Seq<Trail> trails;
        int pierceCount;
        boolean hitten=false;
        public BulletData(Seq<Trail> trails, int pierceCount) {
            this.trails = trails;
            this.pierceCount = pierceCount;
        }
    }
}
