package Sharustry.world.blocks.defense.turret;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.graphics.Drawf;

public class MultiItemConstructTurret extends MultiConstructTurret {
    public TextureRegion leftRegion, rightRegion, leftOutline, rightOutline;
    public float offsetX = 1.5f, offsetY = 0;
    public MultiItemConstructTurret(String name){
        super(name);
    }

    @Override
    public void load() {
        super.load();

        leftRegion = Core.atlas.find(name + "-left");
        rightRegion = Core.atlas.find(name + "-right");
        leftOutline = Core.atlas.find(name + "-left" + "-outline");
        rightOutline = Core.atlas.find(name + "-right" + "-outline");
    }

    public class MultiItemConstructTurretBuild extends MultiConstructTurretBuild {

        @Override
        public void draw() {
            super.draw();
            for(int i : Mathf.signs) {
                Tmp.v5.set(0, 0);
                Tmp.v5.trns(rotation, offsetX - recoil, i * offsetY);
                Tmp.v5.add(x, y);
                Drawf.shadow(i == -1 ? leftOutline : rightOutline, Tmp.v5.x - (size / 2f), Tmp.v5.y - (size / 2f), rotation - 90);
                Draw.rect(i == -1 ? leftOutline : rightOutline, Tmp.v5.x, Tmp.v5.y, rotation - 90);
            }
            for(int i : Mathf.signs) {
                Tmp.v5.set(0, 0);
                Tmp.v5.trns(rotation, offsetX - recoil, i * offsetY);
                Tmp.v5.add(x, y);
                Draw.rect(i == -1 ? leftRegion : rightRegion, Tmp.v5.x, Tmp.v5.y, rotation - 90);
            }
        }
    }
}
