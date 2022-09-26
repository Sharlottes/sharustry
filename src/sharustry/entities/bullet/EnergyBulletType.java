package sharustry.entities.bullet;

import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;

public class EnergyBulletType extends BasicBulletType {
    public EnergyBulletType(float speed, float damage) {
        super(speed, damage);
        pierce = true;
        pierceBuilding = true;
        hittable = false;
        reflectable = false;
    }

    @Override
    public void update(Bullet b) {
        b.collided.clear();
        super.update(b);
        b.hitSize=Math.min(width, height)*(1-b.fin());
    }
}
