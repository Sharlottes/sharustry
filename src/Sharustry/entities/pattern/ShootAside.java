package Sharustry.entities.pattern;

import arc.math.Mathf;
import mindustry.entities.pattern.ShootPattern;

public class ShootAside extends ShootPattern {
    @Override
    public void shoot(int totalShots, BulletHandler handler) {
        for (int side : Mathf.signs) {
            for (int i = 0; i < shots; i++) {
                handler.shoot(0, 0, side * 90, firstShotDelay + shotDelay * i);
            }
        }
    }
}
