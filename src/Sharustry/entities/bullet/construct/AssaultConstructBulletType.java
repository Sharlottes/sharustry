package Sharustry.entities.bullet.construct;

import Sharustry.world.blocks.defense.turret.MultiConstructTurret;
import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;

public class AssaultConstructBulletType extends ConstructBulletType {
    public BulletType fragBulletType;
    public float fragSpeed = 10f;

    public float range = 45f, minRange = 0, reloadTime = 60;
    public BulletType bullet = Bullets.standardDense;
    public float burstSpacing = 5f, spread = 0f, velocityInaccuracy = 0f;

    public int shots = 3;
    public boolean alternate = false;

    public Color heatColor;
    public TextureRegion heatRegion;
    public float heatCooldown = 5;

    public Cons<Bullet> heatDrawer = b -> {
        if(((Float[])b.data)[2] <= 0.00001f) return;

        Draw.color(heatColor, ((Float[])b.data)[2]);
        Draw.blend(Blending.additive);
        Draw.rect(heatRegion, b.x, b.y, -90);
        Draw.blend();
        Draw.color();
    };

    public AssaultConstructBulletType(float speed, float damage){
        super(speed, damage);
        sprite = "shar-sGemini";
    }

    @Override
    public void draw(Bullet b) {
        Color mix = Tmp.c1.set(mixColorFrom).lerp(mixColorTo, b.fin());

        float offset = -90 + (spin != 0 ? Mathf.randomSeed(b.id, 360f) + b.time * spin : 0f);
        Draw.mixcol(mix, mix.a);
        Draw.color(backColor);
        Draw.rect(backRegion, b.x, b.y, width, height, b.rotation() + offset + b.vel.len() * 200 * ((Float[]) b.data)[3]);
        Draw.color(frontColor);
        Draw.rect(frontRegion, b.x, b.y, width, height, b.rotation() + offset + b.vel.len() * 200 * ((Float[]) b.data)[3]);

        Draw.reset();
        if(heatRegion != Core.atlas.find("error")){
            heatDrawer.get(b);
        }

        if(b.owner instanceof MultiConstructTurret.MultiConstructTurretBuild && ((MultiConstructTurret.MultiConstructTurretBuild)b.owner).selected){
            Drawf.dashCircle(b.x * Vars.tilesize, b.y * Vars.tilesize, range, Pal.placing);
        }
    }

    @Override
    public void init(Bullet b) {
        super.init(b);

        b.data = new Float[]{0f, 0f, 0f, Mathf.random(0.5f, 1), 0f}; //reload, shotCounter, heat for drawing, rotRand, frag
    }

    @Override
    public void update(Bullet b) {
        super.update(b);
        ((Float[])b.data)[2] = Mathf.lerpDelta(((Float[])b.data)[2], 0f, heatCooldown);
        Unit target = Units.closestEnemy(b.team, b.x, b.y, range, u -> u.checkTarget(collidesAir, collidesGround));
        if(target != null){
            shooting(bullet, b);
        }
        ((Float[])b.data)[4] += fragSpeed * Time.delta;
        if(fragBulletType != null){
            final float j = ((Float[])b.data)[4];
            Time.run(((Float[])b.data)[4] / 60f, ()-> fragBulletType.create(b, b.x + Angles.trnsx(j, 10), b.y + Angles.trnsy(j, 10), j, 0.25f, 1));
        }
    }

    public void shooting(BulletType type, Bullet b){
        Unit target = Units.closestEnemy(b.team, b.x, b.y, range, u -> u.checkTarget(collidesAir, collidesGround));
        if(((Float[])b.data)[0] >= reloadTime){
            if(burstSpacing > 0.0001f){
                for(int i = 0; i < shots; i++){
                    Time.run(burstSpacing * i, () -> {
                        bullet(type, Angles.angle(b.x, b.y, target.x, target.y) + Mathf.range(inaccuracy), b);
                        ((Float[])b.data)[2] = 1f;
                    });
                }

            }else{
                //otherwise, use the normal shot pattern(s)
                if(alternate){
                    bullet(type, Mathf.range(inaccuracy), b);
                }else{
                    for(int i = 0; i < shots; i++){
                        bullet(type, Angles.angle(b.x, b.y, target.x, target.y) + Mathf.range(inaccuracy + type.inaccuracy) + (i - (int)(shots / 2f)) * spread, b);
                    }
                }
                ((Float[])b.data)[1]++;
                ((Float[])b.data)[2] = 1f;
            }
            ((Float[])b.data)[0] = 0f;
        }else{
            ((Float[])b.data)[0] += Time.delta * type.reloadMultiplier;
        }
    }

    public void bullet(BulletType type, float angle, Bullet b){
        Unit pos = Units.closestEnemy(b.team, b.x, b.y, range, u -> u.checkTarget(collidesAir, collidesGround));
        Vec2 targetPos = new Vec2();
        float speed = bullet.speed;
        //slow bullets never intersect
        if(speed < 0.1f) speed = 9999999f;

        targetPos.set(Predict.intercept((Position) b, pos, speed));
        if(targetPos.isZero()){
            targetPos.set(pos);
        }
        float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(b.x, b.y, targetPos.x, targetPos.y) / type.range(), minRange / type.range(), range / type.range()) : 1f;

        type.create((Entityc) b, b.team, b.x, b.y, angle, 1f + Mathf.range(velocityInaccuracy), lifeScl);
    }
}
