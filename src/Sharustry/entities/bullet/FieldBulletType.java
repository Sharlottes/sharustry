package Sharustry.entities.bullet;

import Sharustry.content.SFx;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;

public class FieldBulletType extends BasicBulletType {
    public float damage = 1;
    public float radius;
    public Color mainColor = Items.pyratite.color, subColor = Pal.lightOrange;
    public StatusEffect status = StatusEffects.none;
    public float statusDuration = 3 * 60;

    public FieldBulletType(float speed, float damage, float duration, float radius){
        super(speed, damage);
        this.radius = radius;
        lifetime = duration;
        collidesTiles = false;
        collides = false;
        collidesAir = false;
        keepVelocity = false;
    }

    public void fillLight(float x, float y, int sides, float radius, Color center, Color edge){
        float centerf = center.toFloatBits(), edgef = edge.toFloatBits();
        sides = Mathf.ceil(sides / 2f) * 2;
        float space = 360f / sides;

        for(int i = 0; i < sides; i += 2){
            float px = Angles.trnsx(space * i, radius);
            float py = Angles.trnsy(space * i, radius);
            float px2 = Angles.trnsx(space * (i + 1), radius);
            float py2 = Angles.trnsy(space * (i + 1), radius);
            float px3 = Angles.trnsx(space * (i + 2), radius);
            float py3 = Angles.trnsy(space * (i + 2), radius);
            Fill.quad(x, y, centerf, x + px, y + py, edgef, x + px2, y + py2, edgef, x + px3, y + py3, edgef);
        }
    }

    @Override
    public void draw(Bullet b){
        Draw.color(mainColor);
        Lines.stroke(1);
        Lines.circle(b.x, b.y, Mathf.clamp((1 - b.fin()) * 20) * radius);

        fillLight(b.x, b.y, Lines.circleVertices(85), Mathf.clamp((1 - b.fin()) * 20) * 85, mainColor.cpy().a(0), subColor.cpy().a(0.7f + 0.25f * Mathf.sin(b.time() * 0.05f)));

        Draw.color();
    }

    @Override
    public void hit(Bullet b, float x, float y) {

    }

    @Override
    public void despawned(Bullet b){

    }

    @Override
    public void update(Bullet b){
        if(b.time() % 80 <= 1 && b.lifetime() - b.time() > 100) SFx.distSplashFx.at(b.x, b.y, 0, mainColor, subColor);

        Units.nearbyEnemies(b.team, b.x - radius, b.y - radius, b.x + radius, b.y + radius, e -> {
            e.apply(status, statusDuration);
            e.damage(damage);
        });
    }

    @Override
    public void init(Bullet b){
        if(b == null) return;

        new Effect(45, e -> fillLight(e.x, e.y, Lines.circleVertices(85), 85, Color.clear, mainColor.cpy().a(e.fout()))).at(b.x, b.y, 0);
    }
}