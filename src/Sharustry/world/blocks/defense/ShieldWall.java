package Sharustry.world.blocks.defense;

import Sharustry.content.SFx;
import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.blocks.defense.Wall;
import mindustry.annotations.Annotations.*;

import static mindustry.Vars.minArmorDamage;

@Component
public class ShieldWall extends Wall {
    public boolean drawShields = true;
    public ShieldWall(String name){
        super(name);
    }

    @Override
    public void setBars() {
        super.setBars();

        bars.add("shield", (ShieldWall.ShieldWallBuild e) -> new Bar(Core.bundle.format("bar.shield"), Pal.accent, ()->e.shield/health));
    }

    public class ShieldWallBuild extends WallBuild {
        float shield;
        float armor;
        float shieldAlpha;

        @Override
        public void created() {
            super.created();
            shield=block.health;
        }

        @Override
        public void draw(){
            super.draw();

            if(this.shieldAlpha > 0 && drawShields){
                drawShield();
            }
        }
        public void drawShield(){
            float alpha = shieldAlpha;
            float radius = block.size * Vars.tilesize * 1.3f;
            Draw.z(Layer.blockOver);
            Fill.light(x, y, Lines.circleVertices(radius), radius, Tmp.c1.set(Pal.shieldIn), Tmp.c2.set(Pal.shield).lerp(Color.white, Mathf.clamp(hitTime() / 2f)).a(Pal.shield.a * alpha));
            Draw.reset();
        }

        @Override
        public void damage(float amount){
            rawDamage(Math.max(amount - armor, minArmorDamage * amount));
        }

        @Override
        public void damagePierce(float amount, boolean withEffect){
            float pre = hitTime;

            rawDamage(amount);

            if(!withEffect) hitTime = pre;
        }

        private void rawDamage(float amount){
            boolean hadShields = shield > 0.0001f;
            if(hadShields) shieldAlpha = 1f;

            float shieldDamage = Math.min(Math.max(shield, 0), amount);
            shield -= shieldDamage;
            hitTime = 1f;
            amount -= shieldDamage;
            if(amount > 0){
                Log.info(hadShields);
                health -= amount;
                if(health <= 0 && !dead()) kill();

                if(hadShields && shield <= 0.0001f) SFx.blockShieldBreak.at(x, y, 0, this);
            }
        }

        @Override
        public void updateTile(){
            super.update();

            Log.info(shield);
            shieldAlpha -= Time.delta / 15f;
            if(shieldAlpha < 0 || shield <= 0.0001) shieldAlpha = 0f;
        }
        /*
        @Override
        public void write(Writes write){
            super.write(write);
            write.f(shield);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            shield = read.f();
        }
        */
    }
}
