package Sharustry.entities.bullet.construct;

import Sharustry.world.blocks.defense.MultiConstructTurret;
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
    }

    @Override
    public void draw(Bullet b) {
        super.draw(b);

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

        b.data = new Float[]{0f, 0f, 0f}; //reload, shotCounter, heat for drawing
    }

    @Override
    public void update(Bullet b) {
        super.update(b);
        ((Float[])b.data)[2] = Mathf.lerpDelta(((Float[])b.data)[2], 0f, heatCooldown);
        Unit target = Units.closestEnemy(b.team, b.x, b.y, range, u -> u.checkTarget(collidesAir, collidesGround));
        if(target != null){
            shooting(bullet, b);
        }
    }

    public void shooting(BulletType type, Bullet b){
        if(((Float[])b.data)[0] >= reloadTime){
            if(burstSpacing > 0.0001f){
                for(int i = 0; i < shots; i++){
                    Time.run(burstSpacing * i, () -> {
                        bullet(type, Mathf.range(inaccuracy), b);
                        ((Float[])b.data)[2] = 1f;
                    });
                }

            }else{
                //otherwise, use the normal shot pattern(s)
                if(alternate){
                    bullet(type, Mathf.range(inaccuracy), b);
                }else{
                    for(int i = 0; i < shots; i++){
                        bullet(type, Mathf.range(inaccuracy + type.inaccuracy) + (i - (int)(shots / 2f)) * spread, b);
                    }
                }
                ((Float[])b.data)[1]++;
                ((Float[])b.data)[2] = 1f;
            }
            ((Float[])b.data)[3] = 0f;
        }else{
            ((Float[])b.data)[3] += Time.delta * type.reloadMultiplier;
        }
    }

    public void bullet(BulletType type, float angle, Bullet b){
        Unit pos = Units.closestEnemy(b.team, b.x, b.y, range, u -> u.checkTarget(collidesAir, collidesGround));
        Vec2 targetPos = new Vec2();
        float speed = bullet.speed;
        //slow bullets never intersect
        if(speed < 0.1f) speed = 9999999f;

        targetPos.set(Predict.intercept((Position) this, pos, speed));
        if(targetPos.isZero()){
            targetPos.set(pos);
        }
        float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(b.x, b.y, targetPos.x, targetPos.y) / type.range(), minRange / type.range(), range / type.range()) : 1f;

        type.create((Entityc) this, b.team, b.x, b.y, angle, 1f + Mathf.range(velocityInaccuracy), lifeScl);
    }
}
