package sharustry.entities.data;

import mindustry.ctype.UnlockableContent;
import mindustry.entities.bullet.BulletType;

public class AmmoData<T extends UnlockableContent> {
    BulletType bullet;
    T ammo;
}
