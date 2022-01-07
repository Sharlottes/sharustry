package Sharustry.world.blocks.defense.turret;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.bullet.BulletType;
import mindustry.graphics.Pal;


public class AsideTurret extends TemplatedTurret {
    TextureRegion chargeRegion;

    public AsideTurret(String name) {
        super(name);
    }

    @Override
    public void load() {
        super.load();
        chargeRegion = Core.atlas.find(name + "-charge");
    }

    public class AsideTurretBuild extends TemplatedTurretBuild {
        float heattt = 0f;

        @Override
        protected void findTarget() {
            super.findTarget();
        }

        @Override
        public void update() {
            super.update();
            if(charging) heattt += Time.delta;
        }

        @Override
        public void draw() {
            super.draw();
            if(charging) {
                Draw.color(Tmp.c1.set(Color.white).lerp(Pal.lancerLaser, 1-heattt/chargeTime).a(0.5f+heattt/chargeTime/2));
                Tmp.v1.set(x, y).trns(rotation, -recoil);
                Draw.rect(chargeRegion, x+Tmp.v1.x, y+Tmp.v1.y, rotation-90);
                Draw.reset();
            }
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
                        heattt = 0f;
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
