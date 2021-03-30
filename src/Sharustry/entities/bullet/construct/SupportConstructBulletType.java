package Sharustry.entities.bullet.construct;

import Sharustry.entities.bullet.construct.ConstructBulletType;
import arc.Core;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import arc.math.*;
import arc.util.*;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.StatusEffect;

public class SupportConstructBulletType extends ConstructBulletType {
    public BulletType fragBulletType;
    public float rotateAmount = 1, fragSpeed = 0.075f, fragSpacing = 8f;

    public float tractRange = 80f, pointRange = 70f, repairRange = 60f;
    public Color tractColor = Color.white, pointColor = Color.white, repairColor = Color.valueOf("e8ffd7");

    public TextureRegion repairLaser, repairLaserEnd, tractLaser, tractLaserEnd;

    public float tractLaserWidth = 0.6f;
    public float tractForce = 0.3f;
    public float tractScaledForce = 0f;
    public float tractDamage = 0f;
    public StatusEffect tractStatus = StatusEffects.none;
    public float tractStatusDuration = 300;

    public float pointBulletDamage = 10f;
    public float pointReloadTime = 30f;
    public Effect pointEffect = Fx.pointBeam;
    public Effect pointHitEffect = Fx.pointHit;
    public Effect pointShootEffect = Fx.sparkShoot;
    public Sound pointShootSound = Sounds.lasershoot;

    public float repairSpeed = 0.3f;

    public float shootLength = 3f;

    public SupportConstructBulletType(float speed, float damage){
        super(speed, damage);
    }

    @Override
    public void load() {
        super.load();

        tractLaser = Core.atlas.find("shar-tlaser");
        tractLaserEnd = Core.atlas.find("shar-tlaser-end");
        repairLaser = Core.atlas.find("laser");
        repairLaserEnd = Core.atlas.find("laser-end");
    }

    @Override
    public void init(Bullet b) {
        super.init(b);

        b.data = new Float[]{0f, 0f, 0f, 0f, 0f, 0f, Mathf.random(0.5f, 1)}; //tract, repair, repairbuild, lastX, lastY, reload, rotrand
    }

    @Override
    public void hit(Bullet b, float x, float y){
        super.hit(b, x, y);

        if(fragBulletType != null){
            for(int i = 0; i < 360 * rotateAmount; i += fragSpacing){
                final int j = i;
                Time.run(fragSpeed * i, ()-> fragBulletType.create(b, x + Angles.trnsx(j, 1), y + Angles.trnsy(j, 1), j, 0.25f, 1));
            }
        }
    }

    @Override
    public void despawned(Bullet b){
        despawnEffect.at(b.x, b.y, b.rotation(), hitColor);
        hitSound.at(b);

        Effect.shake(despawnShake, despawnShake, b);

        if(!b.hit && (fragBulletType != null || fragBullet != null || splashDamageRadius > 0 || lightning > 0)){
            hit(b);
        }
    }

    @Override
    public void draw(Bullet b) {
        super.draw(b);

        Unit repairTarget = Units.closest(b.team, b.x, b.y, repairRange, Unit::damaged);
        Building repairTargetbuild = Units.findAllyTile(b.team, b.x, b.y, repairRange, Building::damaged);

        Unit tractTarget = Units.closestEnemy(b.team, b.x, b.y, tractRange, u -> u.checkTarget(collidesAir, collidesGround));

        if(tractTarget != null
                && tractTarget.within(b, tractRange + tractTarget.hitSize/2f)
                && tractTarget.team() != b.team
                && tractTarget.checkTarget(collidesGround, collidesAir)
                && Angles.within(b.rotation(), b.angleTo(tractTarget), 360)){
            Draw.z(Layer.bullet);
            float ang = b.angleTo(((Float[])b.data)[3], ((Float[])b.data)[4]);

            Draw.mixcol(tractColor, Mathf.absin(4f, 0.6f));

            Drawf.laser(b.team, tractLaser, tractLaserEnd,
                    b.x + Angles.trnsx(ang, shootLength), b.y + Angles.trnsy(ang, shootLength),
                    ((Float[])b.data)[3], ((Float[])b.data)[4], ((Float[])b.data)[0] * tractLaserWidth);

            Draw.mixcol();
        }

        if(repairTarget != null && Angles.angleDist(b.angleTo(repairTarget), b.rotation()) < 30f){
            Draw.z(Layer.flyingUnit + 1); //above all units
            float ang = b.angleTo(repairTarget);
            float len = 5f;

            Draw.color(repairColor);
            Drawf.laser(b.team, repairLaser, repairLaserEnd, b.x + Angles.trnsx(ang, len), b.y + Angles.trnsy(ang, len), repairTarget.x(), repairTarget.y(), ((Float[])b.data)[1]);
            Draw.color();
        }
        if(repairTargetbuild != null && Angles.angleDist(b.angleTo(repairTargetbuild), b.rotation()) < 30f){
            Draw.z(Layer.flyingUnit + 1); //above all units
            float ang = b.angleTo(repairTargetbuild);
            float len = 5f;

            Draw.color(repairColor);
            Drawf.laser(b.team, repairLaser, repairLaserEnd, b.x + Angles.trnsx(ang, len), b.y + Angles.trnsy(ang, len), repairTargetbuild.x(), repairTargetbuild.y(), ((Float[])b.data)[2]);
            Draw.color();
        }
    }

    @Override
    public void update(Bullet b){
        super.update(b);

        new Rect().setSize(repairRange * 2).setCenter(b.x, b.y);
        Unit tractTarget = Units.closestEnemy(b.team, b.x, b.y, tractRange, u -> u.checkTarget(collidesAir, collidesGround));
        Bullet pointTarget = Groups.bullet.intersect(b.x - pointRange, b.y - pointRange, pointRange * 2, pointRange * 2).min(tb -> tb.team != b.team && tb.type().hittable, tb -> tb.dst2(b));
        Unit repairTarget = Units.closest(b.team, b.x, b.y, repairRange, Unit::damaged);
        Building repairTargetbuild = Units.findAllyTile(b.team, b.x, b.y, repairRange, Building::damaged);

        if(tractTarget != null && tractTarget.within(b, tractRange + tractTarget.hitSize/2f) && tractTarget.team() != b.team && tractTarget.checkTarget(collidesGround, collidesAir)){

            float dest = b.angleTo(tractTarget);
            ((Float[])b.data)[3] = tractTarget.x;
            ((Float[])b.data)[4] = tractTarget.y;
            ((Float[])b.data)[0] = Mathf.lerpDelta(((Float[])b.data)[0], 1f, 0.1f);

            //shoot when possible
            if(Angles.within(b.rotation(), dest, 360)){
                if(tractDamage > 0) tractTarget.damageContinuous(tractDamage);
                if(tractStatus != StatusEffects.none) tractTarget.apply(tractStatus, tractStatusDuration);

                tractTarget.impulseNet(Tmp.v1.set(b).sub(tractTarget).limit((tractForce + (1f - tractTarget.dst(b) / tractRange) * tractScaledForce)));
            }
        }else if (b.data instanceof Float[]) {
            ((Float[])b.data)[0] = Mathf.lerpDelta(((Float[])b.data)[0], 0, 0.1f);
        }


        if(pointTarget != null && pointTarget.within(b, pointRange) && pointTarget.team != b.team && pointTarget.type() != null && pointTarget.type().hittable){
            float dest = b.angleTo(pointTarget);

            ((Float[])b.data)[5] += Time.delta;

            //shoot when possible
            if(Angles.within(b.rotation(), dest, 360) && ((Float[])b.data)[5] >= pointReloadTime){
                if(pointTarget.damage() > pointBulletDamage) pointTarget.damage(pointTarget.damage() - pointBulletDamage);
                else pointTarget.remove();

                Tmp.v1.trns(b.rotation(), shootLength);

                pointEffect.at(b.x + Tmp.v1.x, b.y + Tmp.v1.y, b.rotation(), pointColor, new Vec2().set(pointTarget));
                //pointShootEffect.at(b.x + Tmp.v1.x, b.y + Tmp.v1.y, b.rotation(), pointColor);
                pointHitEffect.at(pointTarget.x, pointTarget.y, pointColor);
                pointShootSound.at(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Mathf.random(0.9f, 1.1f));

                ((Float[])b.data)[5] = 0f;
            }
        }

        if(repairTarget != null && !(repairTarget.dead() || repairTarget.dst(b) - repairTarget.hitSize/2f > repairRange || repairTarget.health() >= repairTarget.maxHealth())){
            repairTarget.heal(repairSpeed * Time.delta * ((Float[])b.data)[1]);
            ((Float[])b.data)[1] = Mathf.lerpDelta(((Float[])b.data)[1], 1f, 0.08f * Time.delta);
        }
        else ((Float[])b.data)[1] = Mathf.lerpDelta(((Float[])b.data)[1], 0f, 0.07f * Time.delta);

        if(repairTargetbuild != null && !(repairTargetbuild.dead() || repairTargetbuild.dst(b) - repairTargetbuild.block.size/2f > repairRange || repairTargetbuild.health() >= repairTargetbuild.maxHealth())){
            repairTargetbuild.heal(repairSpeed * Time.delta * ((Float[])b.data)[1]);
            ((Float[])b.data)[2] = Mathf.lerpDelta(((Float[])b.data)[2], 1f, 0.08f * Time.delta);
        }

        else ((Float[])b.data)[2] = Mathf.lerpDelta(((Float[])b.data)[2], 0f, 0.07f * Time.delta);
    }
}
