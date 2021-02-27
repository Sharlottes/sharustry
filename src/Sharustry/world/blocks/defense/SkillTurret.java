package Sharustry.world.blocks.defense;

import arc.func.Func;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;

public class SkillTurret extends TemplatedTurret {
    public int skillDelay = 20;
    public Seq<Func<Building, Runnable>> skillSeq = new Seq<>();


    public SkillTurret(String name){
        super(name);
    }

    public <T extends Building> void addSkills(Func<T, Runnable> skill){
        if(skill != null) skillSeq.add((Func<Building, Runnable>) skill);
    }

    public class SkillTurretBuild extends TemplatedTurretBuild {
        public int shotcounter;

        @Override
        protected void shoot(BulletType type) {
            super.shoot(type);

            shotCounter++;
            if(shotCounter % skillDelay == 0) {
                shotcounter = 0;
                for(Func<Building, Runnable> skill : skillSeq) skill.get(this).run();
            }
        }

        @Override
        public void displayBars(Table bars) {
            super.displayBars(bars);

            bars.add(new Bar(() -> "Skill Stacks", () -> Pal.lancerLaser.cpy().lerp(Pal.sap, 0.5f), () -> shotCounter / (skillDelay * 1f)));
        }
    }
}
