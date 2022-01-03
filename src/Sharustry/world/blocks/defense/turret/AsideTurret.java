package Sharustry.world.blocks.defense.turret;

import arc.math.Mathf;
import arc.util.Time;
import mindustry.entities.bullet.BulletType;


public class AsideTurret extends TemplatedTurret {
    public AsideTurret(String name) {
        super(name);
    }

    public class AsideTurretBuild extends TemplatedTurretBuild {
        @Override
        protected void findTarget() {
            super.findTarget();
        }

        @Override
        protected void shoot(BulletType type){
            for(int ag : Mathf.signs) {
                float rot = rotation+60*ag;
                //when charging is enabled, use the charge shoot pattern
                if (chargeTime > 0) {
                    if(ag==1) useAmmo();

                    tr.trns(rot, shootLength);
                    chargeBeginEffect.at(x + tr.x, y + tr.y, rot);
                    chargeSound.at(x + tr.x, y + tr.y, 1);

                    for (int i = 0; i < chargeEffects; i++) {
                        Time.run(Mathf.random(chargeMaxDelay), () -> {
                            if (dead) return;
                            tr.trns(rot, shootLength);
                            chargeEffect.at(x + tr.x, y + tr.y, rot);
                        });
                    }

                    charging = true;

                    Time.run(chargeTime, () -> {
                        if (dead) return;
                        tr.trns(rot, shootLength);
                        recoil = recoilAmount;
                        heat = 1f;
                        for (int i = 0; i < shots; i++) {
                            bullet(type, rot + Mathf.range(inaccuracy + type.inaccuracy) + (i - (int) (shots / 2f)) * spread);
                        }
                        effects();
                        charging = false;
                    });

                    //when burst spacing is enabled, use the burst pattern
                } else if (burstSpacing > 0.0001f) {
                    for (int i = 0; i < shots; i++) {
                        int ii = i;
                        Time.run(burstSpacing * i, () -> {
                            if (dead || !hasAmmo()) return;
                            tr.trns(rot, shootLength, Mathf.range(xRand));
                            bullet(type, rot + Mathf.range(inaccuracy + type.inaccuracy) + (ii - (int) (shots / 2f)) * spread);
                            effects();
                            if(ag==1) useAmmo();
                            recoil = recoilAmount;
                            heat = 1f;
                        });
                    }

                } else {
                    //otherwise, use the normal shot pattern(s)

                    if (alternate) {
                        float i = (shotCounter % shots) - (shots - 1) / 2f;

                        tr.trns(rot - 90, spread * i + Mathf.range(xRand), shootLength);
                        bullet(type, rot + Mathf.range(inaccuracy + type.inaccuracy));
                    } else {
                        tr.trns(rot, shootLength, Mathf.range(xRand));

                        for (int i = 0; i < shots; i++) {
                            bullet(type, rot + Mathf.range(inaccuracy + type.inaccuracy) + (i - (int) (shots / 2f)) * spread);
                        }
                    }

                    shotCounter++;

                    recoil = recoilAmount;
                    heat = 1f;
                    effects();
                    if(ag==1) useAmmo();
                }
            }
        }
    }
}
