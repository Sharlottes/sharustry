package Sharustry.world.blocks.defense.turret;

import arc.Core;
import arc.func.Cons;
import arc.func.Func;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.meta.Stat;

import java.util.Objects;

public class SkillTurret extends TemplatedTurret {
    public Seq<Integer> skillDelays = new Seq<>();
    public Seq<Func<Building, Runnable>> skillSeq = new Seq<>();
    public Seq<String> skillNames = new Seq<>();
    public Seq<String> skillDescriptions = new Seq<>();
    public Seq<Cons<Table>> skillStats = new Seq<>();

    public SkillTurret(String name){
        super(name);
    }

    public <T extends Building> void addSkills(Func<T, Runnable> skill, int delay, String name){
        if(skill != null) {
            skillSeq.add((Func<Building, Runnable>) skill);
            skillDelays.add(delay);
            skillNames.add(name);
        }
    }

    @Override
    public void setStats() {
        super.setStats();
        for(int i = 0; i < skillSeq.size; i++) {
            final int j = i;
            stats.add(Stat.abilities, table -> {
                if(skillDescriptions.size >= skillSeq.size) table.table(Tex.underline, e -> {
                    e.left().defaults().padRight(3).left();
                    e.add("[white]" + skillNames.get(j) + "[]").fillX();
                    e.row();
                    e.add(Core.bundle.format("stat.shar.skillreload", ""+skillDelays.get(j)));
                    e.row();
                    if(skillStats.size >= skillSeq.size) {
                        skillStats.get(j).get(e);
                        e.row();
                    }
                    e.add("[lightgray]"+ Core.bundle.get("category.purpose") + ": " + skillDescriptions.get(j)+"");
                }).left();
            });
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
            super.shoot(type);
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
