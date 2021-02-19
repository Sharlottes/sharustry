package Sharustry.entities.bullet;

import arc.util.Log;
import mindustry.entities.bullet.*;
import mindustry.gen.Bullet;

public class ThermalLaserBulletType extends ContinuousLaserBulletType {

    public ThermalLaserBulletType(float damage){
        super(damage);
    }

    public class ThermalLaserBullet extends Bullet {
        @Override
        public void update() {
            super.update();
        }
    }
}
