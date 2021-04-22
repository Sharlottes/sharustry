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
    public TextureRegion leftRegion, rightRegion;
    public float offsetX = 1.5f, offsetY = 0;
    public MultiItemConstructTurret(String name){
        super(name);
    }

    @Override
    public void load() {
        super.load();

        leftRegion = Core.atlas.find(name + "-left");
        rightRegion = Core.atlas.find(name + "-right");
    }

    public class MultiItemConstructTurretBuild extends MultiConstructTurretBuild {

        @Override
        public float[] mountLocations(int mount){
            Tmp.v1.trns(this.rotation - 90, (customMountLocation ? customMountLocationsX.get(mount) : mounts.get(mount).x), (customMountLocation ? customMountLocationsY.get(mount) : mounts.get(mount).y) - offsetY);
            Tmp.v1.add(x, y);
            Tmp.v2.trns(_rotations.get(mount), -offsetY);
            float i = (_shotCounters.get(mount) % mounts.get(mount).barrels) - (mounts.get(mount).barrels - 1) / 2;
            Tmp.v3.trns(_rotations.get(mount) - 90, mounts.get(mount).shootX + mounts.get(mount).barrelSpacing * i + mounts.get(mount).xRand, mounts.get(mount).shootY + mounts.get(mount).yRand);

            float x = Tmp.v1.x;
            float y = Tmp.v1.y;
            float rX = x + Tmp.v2.x;
            float rY = y + Tmp.v2.y;
            float sX = rX + Tmp.v3.x;
            float sY = rY + Tmp.v3.y;

            return new float[]{x, y, rX, rY, sX, sY};
        }

        @Override
        public void draw() {
            Draw.rect(baseRegion, x, y);

            Draw.z(Layer.turret);
            Tmp.v4.trns(rotation, -offsetY);
            Tmp.v4.add(x, y);
            Drawf.shadow(outline, Tmp.v4.x - (size / 2f), Tmp.v4.y - (size / 2f), rotation - 90);
            Draw.rect(outline, Tmp.v4.x, Tmp.v4.y, rotation - 90);
            Draw.rect(region, Tmp.v4.x, Tmp.v4.y, rotation - 90);

            Tmp.v5.set(0, 0);
            Tmp.v5.trns(rotation, -offsetX, offsetY);
            Tmp.v5.trns(rotation, -recoil);
            Tmp.v5.add(x, y);
            Draw.rect(leftRegion, Tmp.v5.x, Tmp.v5.y, rotation - 90);
            Tmp.v5.set(0, 0);
            Tmp.v5.trns(rotation, offsetX, offsetY);
            Tmp.v5.trns(rotation, -recoil);
            Tmp.v5.add(x, y);
            Draw.rect(rightRegion, Tmp.v5.x, Tmp.v5.y, rotation - 90);

            if(heatRegion != Core.atlas.find("error") && _heat > 0.00001){
                Draw.color(heatColor, _heat);
                Draw.blend(Blending.additive);
                Draw.rect(heatRegion, Tmp.v4.x, Tmp.v4.y, rotation - 90);
                Draw.blend();
                Draw.color();
            }

            for(int i = 0; i < mounts.size; i++){
                float[] loc = mountLocations(i);

                Drawf.shadow(turrets.get(i)[1], loc[2] - mounts.get(i).elevation, loc[3] - mounts.get(i).elevation, _rotations.get(i) - 90);
            }

            for(int i = 0; i < mounts.size; i++){
                float[] loc = mountLocations(i);

                Draw.rect(turrets.get(i)[1], loc[2], loc[3], _rotations.get(i) - 90);
                Draw.rect(turrets.get(i)[0], loc[2], loc[3], _rotations.get(i) - 90);

                if(turrets.get(i)[2] != Core.atlas.find("error") && _heats.get(i) > 0.00001){
                    Draw.color(mounts.get(i).heatColor, _heats.get(i));
                    Draw.blend(Blending.additive);
                    Draw.rect(turrets.get(i)[2], loc[2], loc[3], _rotations.get(i) - 90);
                    Draw.blend();
                    Draw.color();
                }

                if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.tract && _anys.get(i)){
                    Draw.z(Layer.bullet);
                    float ang = angleTo(_lastXs.get(i), _lastYs.get(i));

                    Draw.mixcol(mounts.get(i).laserColor, Mathf.absin(4f, 0.6f));

                    Drawf.laser(team, mounts.get(i).laser, mounts.get(i).laserEnd,
                            x + Angles.trnsx(ang, mounts.get(i).shootLength), y + Angles.trnsy(ang, mounts.get(i).shootLength),
                            _lastXs.get(i), _lastYs.get(i), _strengths.get(i) * Mathf.clamp(power.graph.getPowerBalance()/mounts.get(i).powerUse, 0, 1) * mounts.get(i).laserWidth);

                    Draw.mixcol();
                    Draw.reset();
                }
                if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.repair
                        && _repairTargets.get(i) != null
                        && Angles.angleDist(angleTo(_repairTargets.get(i)), _rotations.get(i)) < 30f){
                    Draw.z(Layer.flyingUnit + 1); //above all units
                    float ang = angleTo(_repairTargets.get(i));
                    float len = 5f;

                    Draw.color(mounts.get(i).laserColor);
                    Drawf.laser(team, mounts.get(i).laser, mounts.get(i).laserEnd,
                            loc[0] + Angles.trnsx(ang, len), loc[1] + Angles.trnsy(ang, len),
                            _repairTargets.get(i).x(), _repairTargets.get(i).y(), _strengths.get(i));
                    Draw.color();
                    Draw.reset();
                }
                if(mounts.get(i).mountType == MultiTurretMount.MultiTurretMountType.drill
                        && _mineTiles.get(i) != null){
                    float focusLen = mounts.get(i).laserOffset / 2f + Mathf.absin(Time.time, 1.1f, 0.5f);
                    float swingScl = 12f, swingMag = tilesize / 8f;
                    float flashScl = 0.3f;

                    float px = loc[0] + Angles.trnsx(_rotations.get(i), focusLen);
                    float py = loc[1] + Angles.trnsy(_rotations.get(i), focusLen);

                    float ex = _mineTiles.get(i).worldx() + Mathf.sin(Time.time + 48, swingScl, swingMag);
                    float ey = _mineTiles.get(i).worldy() + Mathf.sin(Time.time + 48, swingScl + 2f, swingMag);

                    Draw.z(Layer.flyingUnit + 0.1f);

                    Draw.color(Color.lightGray, Color.white, 1f - flashScl + Mathf.absin(Time.time, 0.5f, flashScl));

                    Drawf.laser(team(), mounts.get(i).laser, mounts.get(i).laserEnd, px, py, ex, ey, mounts.get(i).laserWidth);

                    Draw.color();
                }
            }

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
