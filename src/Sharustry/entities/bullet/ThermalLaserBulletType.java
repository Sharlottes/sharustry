package Sharustry.entities.bullet;

import mindustry.entities.bullet.*;
import mindustry.gen.Bullet;

public class ThermalLaserBulletType extends ContinuousLaserBulletType {

    public ThermalLaserBulletType(float damage){
        super(damage);
    }

    public static class ThermalLaserBullet extends Bullet {
        @Override
        public void update() {
            super.update();
        }
    }
}
