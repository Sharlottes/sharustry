package Sharustry.world.blocks.defense;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.bullet.BulletType;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;

import static mindustry.Vars.tilesize;

public class MultiItemConstructTurret extends MultiConstructTurret{
    public TextureRegion leftRegion, rightRegion, leftOutline, rightOutline;
    public float offsetX = 1.5f, offsetY = 0;
    public MultiItemConstructTurret(String name){
        super(name);
    }

    @Override
    public void load() {
        super.load();

        leftRegion = Core.atlas.find(name + "-left");
        rightRegion = Core.atlas.find(name + "-right");
        leftOutline = Core.atlas.find(name + "-left" + "-outline");
        rightOutline = Core.atlas.find(name + "-right" + "-outline");
    }

    public class MultiItemConstructTurretBuild extends MultiConstructTurretBuild {

        @Override
        public void draw() {
            Draw.rect(baseRegion, x, y);

            Draw.z(Layer.turret);
            Tmp.v4.trns(rotation, 0);
            Tmp.v4.add(x, y);
            Drawf.shadow(outline, Tmp.v4.x - (size / 2f), Tmp.v4.y - (size / 2f), rotation - 90);
            Draw.rect(outline, Tmp.v4.x, Tmp.v4.y, rotation - 90);
            for(int i : Mathf.signs) {
                Tmp.v5.set(0, 0);
                Tmp.v5.trns(rotation, offsetX - recoil, i * offsetY);
                Tmp.v5.add(x, y);
                Drawf.shadow(i == -1 ? leftOutline : rightOutline, Tmp.v5.x - (size / 2f), Tmp.v5.y - (size / 2f), rotation - 90);
                Draw.rect(i == -1 ? leftOutline : rightOutline, Tmp.v5.x, Tmp.v5.y, rotation - 90);
            }
            Draw.rect(region, Tmp.v4.x, Tmp.v4.y, rotation - 90);
            for(int i : Mathf.signs) {
                Tmp.v5.set(0, 0);
                Tmp.v5.trns(rotation, offsetX - recoil, i * offsetY);
                Tmp.v5.add(x, y);
                Draw.rect(i == -1 ? leftRegion : rightRegion, Tmp.v5.x, Tmp.v5.y, rotation - 90);
            }

            if(heatRegion != Core.atlas.find("error") && _heat > 0.00001){
                Draw.color(heatColor, _heat);
                Draw.blend(Blending.additive);
                Draw.rect(heatRegion, Tmp.v4.x, Tmp.v4.y, rotation - 90);
                Draw.blend();
                Draw.color();
            }

            for(MountTurret mount : mounts) mount.draw(this);

            Draw.reset();
        }

        @Override
        protected void shoot(BulletType type){
            //when charging is enabled, use the charge shoot pattern
            if(chargeTime > 0){
                useAmmo();

                tr.trns(rotation, shootLength);
                chargeBeginEffect.at(x + tr.x, y + tr.y, rotation);
                chargeSound.at(x + tr.x, y + tr.y, 1);

                for(int i = 0; i < chargeEffects; i++){
                    Time.run(Mathf.random(chargeMaxDelay), () -> {
                        if(!isValid()) return;
                        tr.trns(rotation, shootLength);
                        chargeEffect.at(x + tr.x, y + tr.y, rotation);
                    });
                }

                charging = true;

                Time.run(chargeTime, () -> {
                    if(!isValid()) return;
                    tr.trns(rotation, shootLength);
                    recoil = recoilAmount;
                    heat = 1f;
                    bullet(type, rotation + Mathf.range(inaccuracy));
                    effects();
                    charging = false;
                });

                //when burst spacing is enabled, use the burst pattern
            }else if(burstSpacing > 0.0001f){
                for(int i = 0; i < shots; i++){
                    Time.run(burstSpacing * i, () -> {
                        if(!isValid() || !hasAmmo()) return;

                        recoil = recoilAmount;

                        tr.trns(rotation, shootLength, Mathf.range(xRand));
                        bullet(type, rotation + Mathf.range(inaccuracy));
                        effects();
                        useAmmo();
                        recoil = recoilAmount;
                        heat = 1f;
                    });
                }

            }else{
                //otherwise, use the normal shot pattern(s)

                if(alternate){
                    float i = (shotCounter % shots) - (shots-1)/2f;

                    tr.trns(rotation - 90, spread * i + Mathf.range(xRand), shootLength);
                    bullet(type, rotation + Mathf.range(inaccuracy));
                }else{
                    tr.trns(rotation, shootLength, Mathf.range(xRand));

                    for(int i = 0; i < shots; i++){
                        bullet(type, rotation + Mathf.range(inaccuracy + type.inaccuracy) + (i - (int)(shots / 2f)) * spread);
                    }
                }

                shotCounter++;

                recoil = recoilAmount;
                heat = 1f;
                effects();
                useAmmo();
            }

            doSkill();
        }
    }
}
