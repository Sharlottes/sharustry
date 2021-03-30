package Sharustry.entities.bullet.construct;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.graphics.Pal;

/** the bullet type for construct turret. all sprite are taken from Nova Drift
 * do not ever use sprite without their permission.
 * do not ever use sound without their permission.*/

public class ConstructBulletType extends BulletType {
    public Color backColor = Pal.bulletYellowBack, frontColor = Pal.bulletYellow;
    public Color mixColorFrom = new Color(1f, 1f, 1f, 0f), mixColorTo = new Color(1f, 1f, 1f, 0f);
    public float width = 5f, height = 7f;
    public float spin = 0;
    public String sprite;

    public TextureRegion backRegion;
    public TextureRegion frontRegion;

    public ConstructBulletType(float speed, float damage, String bulletSprite){
        super(speed, damage);
        this.sprite = bulletSprite;
    }

    public ConstructBulletType(float speed, float damage){
        this(speed, damage, "shar-construct");
    }

    public ConstructBulletType(){
        this(1f, 1f, "shar-construct");
    }

    @Override
    public void load(){
        backRegion = Core.atlas.find(sprite + "-back");
        frontRegion = Core.atlas.find(sprite);
    }

    @Override
    public void draw(Bullet b) {
        Color mix = Tmp.c1.set(mixColorFrom).lerp(mixColorTo, b.fin());

        float offset = -90 + (spin != 0 ? Mathf.randomSeed(b.id, 360f) + b.time * spin : 0f);
        Draw.mixcol(mix, mix.a);
        Draw.color(backColor);
        Draw.rect(backRegion, b.x, b.y, width, height, b.rotation() + offset + b.vel.len() * 200 * ((Float[]) b.data)[6]);
        Draw.color(frontColor);
        Draw.rect(frontRegion, b.x, b.y, width, height, b.rotation() + offset + b.vel.len() * 200 * ((Float[]) b.data)[6]);

        Draw.reset();
    }
}
