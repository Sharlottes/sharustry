package Sharustry.world.blocks.defense;

import arc.func.Func;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;

public class MultiSkillTurret extends MultiTurret {
    public Seq<Integer> skillDelays = new Seq<>();
    public Seq<Func<Building, Runnable>> skillSeq = new Seq<>();

    public MultiSkillTurret(String name, BulletType type, Object ammo, String title, MultiTurretMount... mounts){
        super(name, type, ammo, title, mounts);
    }

    public MultiSkillTurret(String name){
        super(name);
    }

    public <T extends Building> void addSkills(Func<T, Runnable> skill, int delay){
        if(skill != null) {
            skillSeq.add((Func<Building, Runnable>) skill);
            skillDelays.add(delay);
        }
    }

    public class MultiSkillTurretBuild extends MultiTurretBuild {
        public int shotcounter;
        public Seq<Integer> _shotcounters = new Seq<>();

        @Override
        public void created(){
            super.created();
            for(int i = 0; i < mounts.size; i++) _shotcounters.add(0);
        }

        @Override
        protected void shoot(BulletType type) {
            super.shoot(type);

            shotcounter++;
            for(int i = 0; i < skillDelays.size; i++) if(shotcounter % skillDelays.get(i) == 0) {
                shotcounter = 0;
                skillSeq.get(i).get(this).run();
            }
        }

        @Override
        public void mountShoot(int mount, BulletType type) {
            super.mountShoot(mount, type);

            if(!mounts.get(mount).sequential) _shotcounters.set(mount, _shotcounters.get(mount)+1);
            for(int i = 0; i < mounts.get(mount).skillDelays.size; i++) if(_shotcounters.get(mount) % mounts.get(mount).skillDelays.get(i) == 0) {
                _shotcounters.set(mount, 0);
                mounts.get(mount).skillSeq.get(i).get(this, mounts.get(mount)).run();
            }
        }
    }
}
