package Sharustry.world.blocks.defense;

import Sharustry.content.SBullets;
import Sharustry.graphics.SPal;
import arc.func.Func;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;

public class SkillTurret extends TemplatedTurret {
    public Seq<Integer> skillDelays = new Seq<>();
    public Seq<Func<Building, Runnable>> skillSeq = new Seq<>();

    public SkillTurret(String name){
        super(name);
    }

    public <T extends Building> void addSkills(Func<T, Runnable> skill, int delay){
        if(skill != null) {
            skillSeq.add((Func<Building, Runnable>) skill);
            skillDelays.add(delay);
        }
    }

    public class SkillTurretBuild extends TemplatedTurretBuild {
        public int shotcounter;

        @Override
        protected void shoot(BulletType type) {
            if(chargeTime > 0){
                final Color data;
                if(useAmmo() == SBullets.testLaser) data = Items.pyratite.color;
                else data = null;
                tr.trns(rotation, shootLength);
                if(data == null) chargeBeginEffect.at(x + tr.x, y + tr.y, rotation, SPal.cryoium); //i want to die
                else chargeBeginEffect.at(x + tr.x, y + tr.y, rotation, data);
                chargeSound.at(x + tr.x, y + tr.y, 1);

                for(int i = 0; i < chargeEffects; i++){
                    Time.run(Mathf.random(chargeMaxDelay), () -> {
                        if(!isValid()) return;
                        tr.trns(rotation, shootLength);
                        if(data == null) chargeEffect.at(x + tr.x, y + tr.y, rotation, SPal.cryoium);
                        else chargeEffect.at(x + tr.x, y + tr.y, rotation, data);
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

            shotcounter++;
            for(int i = 0; i < skillDelays.size; i++) if(shotcounter % skillDelays.get(i) == 0) {
                shotcounter = 0;
                skillSeq.get(i).get(this).run();
            }
        }

        @Override
        protected void effects(){
            Color data = null;
            if(useAmmo() == SBullets.testLaser) data = Items.pyratite.color;

            Effect fshootEffect = shootEffect == Fx.none ? peekAmmo().shootEffect : shootEffect;
            Effect fsmokeEffect = smokeEffect == Fx.none ? peekAmmo().smokeEffect : smokeEffect;

            if(data == null) fshootEffect.at(x + tr.x, y + tr.y, rotation, SPal.cryoium);
            else fshootEffect.at(x + tr.x, y + tr.y, rotation, data);
            fsmokeEffect.at(x + tr.x, y + tr.y, rotation);
            shootSound.at(x + tr.x, y + tr.y, Mathf.random(0.9f, 1.1f));

            if(shootShake > 0){
                Effect.shake(shootShake, shootShake, this);
            }

            recoil = recoilAmount;
        }
    }
}
