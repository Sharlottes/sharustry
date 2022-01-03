package Sharustry.world.blocks.defense.turret;

import arc.math.Mathf;
import arc.util.Time;
import mindustry.entities.bullet.BulletType;

import static Sharustry.content.SFx.snipeLine;

public class SnipeTurret extends TemplatedTurret {
    public SnipeTurret(String name) {
        super(name);
    }

    public SnipeTurret(String name, float chargeTime) {
        this(name);
        this.chargeTime = chargeTime;
    }

    public class SnipeTurretBuild extends TemplatedTurretBuild {

        @Override
        protected void shoot(BulletType type){
            useAmmo();

            tr.trns(rotation, shootLength);
            chargeBeginEffect.at(x + tr.x, y + tr.y, rotation);
            chargeSound.at(x + tr.x, y + tr.y, 1);
            snipeLine.at(x + tr.x, y + tr.y, rotation, new Float[]{chargeTime, target == null ? range : dst(target)});
            for(int i = 0; i < chargeEffects; i++){
                Time.run(Mathf.random(chargeMaxDelay), () -> {
                    if(dead) return;
                    tr.trns(rotation, shootLength);
                    chargeEffect.at(x + tr.x, y + tr.y, rotation);
                });
            }

            charging = true;
            Time.run(chargeTime, () -> {
                if(dead) return;
                tr.trns(rotation, shootLength);
                recoil = recoilAmount;
                heat = 1f;
                bullet(type, rotation + Mathf.range(inaccuracy + type.inaccuracy));
                effects();
                charging = false;
            });
        }
    }
}
