package Sharustry.world.blocks.defense;

import arc.struct.Seq;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.*;

public class RailMultiSkillTurret extends MultiSkillTurret{

    public RailMultiSkillTurret(String name, BulletType type, Object ammo, String title, MultiTurretMount... mounts){
        super(name, type, ammo, title, mounts);
    }

    public RailMultiSkillTurret(String name){
        super(name);
    }

    public class RailMultiSkillTurretBuild extends MultiSkillTurretBuild {

    }
}
