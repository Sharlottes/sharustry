package Sharustry.world.blocks.defense.turret;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.*;
import arc.util.io.*;
import mindustry.ui.Fonts;

public class GetlingTurret extends TemplatedTurret {
    public float missileLoad = 7f, maxMissile = 200, delayTime = 30;
    public GetlingTurret(String name) {
        super(name);
    }

    public class GetlingTurretBuild extends TemplatedTurretBuild {
        float hheat = 0f, delay = delayTime;
        int missiles = 0;

        @Override
        public void draw() {
            super.draw();
            Fonts.outline.draw(""+missiles, x, y-8, Color.white, 0.25f, true, Align.center);
            Draw.color(Tmp.c1.set(Color.white).lerp(heatColor, 1-delay/delayTime).a(1-delay/delayTime));
            Draw.rect(heatRegion, x, y, rotation);
        }

        @Override
        public void update() {
            super.update();
            hheat+= edelta();
            if(hheat>missileLoad && hasAmmo() && !wasShooting && missiles<maxMissile) {
                useAmmo();
                missiles++;
                hheat = 0f;
            }
            if(!wasShooting) delay = Mathf.approachDelta(delay, delayTime, 0.05f);
            Log.info(delay*5);
        }

        @Override
        protected void updateShooting(){
            reload += delta() * peekAmmo().reloadMultiplier * baseReloadSpeed();
            if(delay>0&&reload >= delay && !charging && missiles>0){

                missiles--;
                shoot(peekAmmo());
                if(delay > 5) delay = Mathf.approachDelta(delay, 0, missiles/maxMissile);
                else damage(delay);
                if(delay > 0) reload %= delay;
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            missiles = read.i();
            delay = read.f();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(missiles);
            write.f(delay);
        }
    }
}
