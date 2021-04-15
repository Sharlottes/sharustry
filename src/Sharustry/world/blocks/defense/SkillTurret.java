package Sharustry.world.blocks.defense;

import arc.Core;
import arc.func.Func;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;

import java.util.Objects;

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
        public Seq<Integer> shotcounters = new Seq<>();

        @Override
        public void created() {
            for(int i = 0; i < skillDelays.size; i++) shotcounters.add(0);
            super.created();
        }

        @Override
        public void displayBars(Table bars) {
            super.displayBars(bars);

            for(int i = 0; i < skillDelays.size; i++) {
                final int j = i;
                bars.add(new Bar(() -> Core.bundle.format("bar.shar-skillReload") + shotcounters.get(j) + " / " + skillDelays.get(j), () -> Pal.lancerLaser.cpy().lerp(Pal.place, Mathf.absin(Time.time, 20, (shotcounters.get(j) / (skillDelays.get(j) * 2.5f)))), () -> (shotcounters.get(j) / (skillDelays.get(j) * 1f)))).growX();
                bars.row();
            }
        }

        @Override
        protected void shoot(BulletType type) {
            if(chargeTime > 0){
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
                if(alternate){
                    float i = (shotCounter % shots) - (shots-1)/2f;

                    tr.trns(rotation - 90, spread * i + Mathf.range(xRand), shootLength);
                    bullet(type, rotation + Mathf.range(inaccuracy));
                }else{
                    tr.trns(rotation, shootLength, Mathf.range(xRand));

                    for(int i = 0; i < shots; i++)
                        bullet(type, rotation + Mathf.range(inaccuracy + type.inaccuracy) + (i - (int)(shots / 2f)) * spread);
                }

                shotCounter++;
                recoil = recoilAmount;
                heat = 1f;
                effects();
                useAmmo();
            }

            for(int i = 0; i < skillDelays.size; i++) {
                shotcounters.set(i, shotcounters.get(i) + 1);
                if(Objects.equals(shotcounters.get(i), skillDelays.get(i))) {
                    shotcounters.set(i, 0);
                    skillSeq.get(i).get(this).run();
                }
            }
        }
    }
}
