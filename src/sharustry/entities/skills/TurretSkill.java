package sharustry.entities.skills;

import arc.func.Func;
import arc.scene.ui.layout.Table;
import mindustry.world.blocks.defense.turrets.Turret;

public class TurretSkill<T extends Turret.TurretBuild> extends Skill {
    public Func<T, Runnable> runner = build -> () -> {};
    public int maxCount = 5;
    public TurretSkill(String name) {
        super(name);
    }

    public TurretSkill(String name, Func<T, Runnable> runner, int maxCounter) {
        super(name);
        this.runner = runner;
        this.maxCount = maxCounter;
    }

    public TurretSkill(String name, Func<T, Runnable> runner) {
        super(name);
        this.runner = runner;
    }

    public void stats(Table table) {

    }

    public void active(T build) {
        runner.get(build).run();
    }
}
