package Sharustry.world.blocks.defense;

import Sharustry.content.SFx;
import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.util.Log;
import arc.util.Tmp;
import arc.util.io.*;
import mindustry.Vars;
import mindustry.graphics.*;
import mindustry.ui.Bar;
import mindustry.world.blocks.defense.Wall;
import mindustry.annotations.Annotations.*;

import static mindustry.Vars.minArmorDamage;

@Component
public class ShieldWall extends Wall {
    public boolean drawShields = true;
    public float armor = 0;
    public float maxShield = health;
    public float regenCooldown = 2 * 60f;
    public float regenAmount = 1;

    public ShieldWall(String name){
        super(name);
        update = true;
        sync = true;
    }

    @Override
    public void setBars() {
        super.setBars();

        bars.add("shield", (ShieldWall.ShieldWallBuild e) -> new Bar(Core.bundle.format("bar.shield"), Pal.accent, ()->e.shield/health));
    }

    public class ShieldWallBuild extends WallBuild {
        float shield;
        float shieldAlpha;
        float heat = 0f;

        @Override
        public void created() {
            super.created();
            shield = maxShield = health;
        }

        @Override
        public void draw(){
            super.draw();

            if(this.shieldAlpha > 0 && drawShields) drawShield();
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
            heat = regenCooldown;

            float shieldDamage = Math.min(Math.max(shield, 0), amount);
            shield -= shieldDamage;
            hitTime = 1f;
            amount -= shieldDamage;

            if(amount > 0){
                health -= amount;
                if(health <= 0 && !dead()) kill();

                if(hadShields && shield <= 0.0001f) SFx.blockShieldBreak.at(x, y, 0, this);
            }
        }

        @Override
        public void updateTile(){
            super.updateTile();
            shieldAlpha -= delta() / 15f;
            if(shieldAlpha < 0) shieldAlpha = 0f;

            Log.info(regenAmount * delta());
            heat -= delta();
            if(heat <= 0f && shield < maxShield) shield += regenAmount * delta();
        }

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

    }
}
