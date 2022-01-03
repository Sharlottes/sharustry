package Sharustry.entities.bullet;

import arc.struct.Seq;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.graphics.Trail;

public class TrailBulletType extends BasicBulletType {
    public BulletType trailBullet;

    public TrailBulletType(float speed, float damage){
        super(speed, damage);
    }

    @Override
    public void update(Bullet b){
        super.update(b);
        if(b.time%2<=1) {
            trailBullet.create(b, b.team, b.x, b.y, b.rotation(), 0f, 1);
        }
    }
}
