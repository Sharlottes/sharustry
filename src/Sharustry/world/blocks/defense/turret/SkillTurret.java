package Sharustry.world.blocks.defense.turret;

import Sharustry.entities.skills.TurretSkill;
import arc.Core;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.meta.Stat;

public class SkillTurret extends TemplatedTurret {
    public Seq<TurretSkill<SkillTurretBuild>> skills = new Seq<>();

    public SkillTurret(String name){
        super(name);
    }

    @Override
    public void setStats() {
        super.setStats();
        for(TurretSkill<SkillTurretBuild> skill : skills) {
            stats.add(Stat.abilities, table -> {
                table.table(Tex.underline, e -> {
                    e.left().defaults().padRight(3).left();
                    e.add(skill.name).fillX().row();
                    e.add(Core.bundle.format("stat.shar.skillreload", skill.maxCount)).row();
                    skill.stats(e);
                    e.row();
                    e.add("[lightgray]"+ Core.bundle.get("category.purpose") + ": " + skill.description);
                }).left();
            });
        }
    }

    public class SkillTurretBuild extends TemplatedTurretBuild {
        public IntSeq shotcounters = new IntSeq();

        @Override
        public void created() {
            for(int i = 0; i < skills.size; i++) shotcounters.add(0);
            super.created();
        }

        @Override
        public void displayBars(Table bars) {
            super.displayBars(bars);

            for(int i = 0; i < skills.size; i++) {
                final int j = i;
                bars.add(new Bar(() -> Core.bundle.format("bar.shar-skillReload") + shotcounters.get(j) + " / " + skills.get(j).maxCount, () -> Pal.lancerLaser.cpy().lerp(Pal.place, Mathf.absin(Time.time, 20, (shotcounters.get(j) / (skills.get(j).maxCount * 2.5f)))), () -> (shotcounters.get(j) / skills.get(j).maxCount))).growX();
                bars.row();
            }
        }

        @Override
        protected void shoot(BulletType type) {
            super.shoot(type);
            for(int i = 0; i < skills.size; i++) {
                shotcounters.incr(i, 1);
                if(shotcounters.get(i) >= skills.get(i).maxCount) {
                    shotcounters.set(i, 0);
                    skills.get(i).active(this);
                }
            }
        }
    }
}
