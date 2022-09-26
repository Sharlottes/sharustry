package sharustry.world.blocks.defense.turret;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.StatusEffects;
import mindustry.entities.Units;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.LAccess;
import mindustry.type.*;
import mindustry.ui.Bar;
import mindustry.world.blocks.defense.turrets.BaseTurret;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class HeatTractorBeamTurret extends BaseTurret {

    public final int timerTarget = timers++;
    public float retargetTime = 5f;
    public float shootCone = 6f;
    public float shootLength = 5f;
    public float laserWidth = 0.6f;
    public float force = 0.3f;
    public float scaledForce = 0f;
    public float maxdamage = 1000;

    public boolean targetAir, targetGround = true;
    public Color laserColor = Color.white;
    public StatusEffect status = StatusEffects.none;
    public float statusDuration = 300;
    public Sound shootSound = Sounds.tractorbeam;
    public float shootSoundVolume = 0f;

    public TextureRegion laser, laserStart, laserEnd, baseRegion;
    public float flashThreshold = 0.46f;

    public HeatTractorBeamTurret(String name) {
        super(name);
        coolantMultiplier = 1f;

        rotateSpeed = 10;
        hasPower = true;

    }



    public void load()
    {
        laser = Core.atlas.find(name + "-laser");
        laserEnd = Core.atlas.find(name + "-laserStart");
        laserStart = Core.atlas.find( name + "-laserStart");
        baseRegion = Core.atlas.find("block-" + size + "size");
    }


    @Override
    public void setBars()
    {
        super.setBars();
        addBar("heat", (HeatTractorBeamBuild entity) -> new Bar("bar.heat", Pal.lightishOrange, () -> entity.heat));
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{baseRegion, region};
    }

    @Override
    public void setStats()
    {
        super.setStats();

        stats.add(Stat.targetsAir, targetAir);
        stats.add(Stat.targetsGround, targetGround);
        stats.add(Stat.damage, maxdamage * 60f, StatUnit.perSecond);
    }

    public float damage;

    @Override
    public void init()
    {
        super.init();

        clipSize = Math.max(clipSize, (range + tilesize) * 2);
    }


    public class HeatTractorBeamBuild extends BaseTurretBuild
    {
        public @Nullable
        Unit target;

        public float heat;
        public float lastX, lastY, strength;
        public boolean any;
        public float coolant = 1f;
        public float flash;

        @Override
        public void damage(float damage) {
            damage = maxdamage * heat;
            super.damage(damage);
        }

        @Override
        public void updateTile(){
            if (target != null) {
                heat += Time.delta / 10;
                heat = heat * heat;
            } else {
                heat = 0;
            }

            if (heat >= 0.999f) heat = 1;

            if(timer(timerTarget, retargetTime))
                target = Units.closestEnemy(team, x, y, range, u -> u.checkTarget(targetAir, targetGround));

            if(target != null) {
                float maxUsed = block.<ConsumeLiquidBase>findConsumer(consume -> consume instanceof ConsumeLiquidBase).amount;
                Liquid liquid = liquids.current();
                float used = Math.min(Math.min(liquids.get(liquid), maxUsed * Time.delta), Math.max(0, (1f / coolantMultiplier) / liquid.heatCapacity));

                liquids.remove(liquid, used);

                if(Mathf.chance(0.06 * used)){
                    coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                }

                coolant = 1f + (used * liquid.heatCapacity * coolantMultiplier);
            }

            any = false;

            if(target != null && target.within(this, range + target.hitSize/2f) && target.team() != team && target.checkTarget(targetAir, targetGround) && efficiency() > 0.02f)
            {
                if(!headless)
                {
                    control.sound.loop(shootSound, this, shootSoundVolume);
                }

                float dest = angleTo(target);
                rotation = Angles.moveToward(rotation, dest, rotateSpeed * edelta());
                lastX = target.x;
                lastY = target.y;
                strength = Mathf.lerpDelta(strength, 1f, 0.1f);

                //shoot when possible
                if(Angles.within(rotation, dest, shootCone)){
                    if(damage > 0){
                        target.damageContinuous(damage * efficiency());
                    }

                    if(status != StatusEffects.none){
                        target.apply(status, statusDuration);
                    }

                    any = true;
                    target.impulseNet(Tmp.v1.set(this).sub(target).limit((force + (1f - target.dst(this) / range) * scaledForce) * edelta() * timeScale));
                }
            }else{
                strength = Mathf.lerpDelta(strength, 0, 0.1f);
            }
        }

        @Override
        public float efficiency(){
            return super.efficiency() * coolant;
        }

        @Override
        public void draw()
        {
            Draw.rect(baseRegion, x, y);
            Drawf.shadow(region, x - (size / 2f), y - (size / 2f), rotation - 90);
            Draw.rect(region, x, y, rotation - 90);

            //draw laser if applicable
            if(any)
            {
                Draw.z(Layer.bullet);
                float ang = angleTo(lastX, lastY);

                Draw.mixcol(laserColor, Mathf.absin(4f, 0.6f));

                Drawf.laser(laser, laserStart, laserEnd,
                        x + Angles.trnsx(ang, shootLength), y + Angles.trnsy(ang, shootLength),
                        lastX, lastY, strength * efficiency() * laserWidth);

                Draw.mixcol();
            }

            if(heat > flashThreshold){
                flash += (1f + ((heat - flashThreshold) / (1f - flashThreshold)) * 5.4f) * Time.delta;
                Draw.color(Color.red, Color.yellow, Mathf.absin(flash, 9f, 1f));
                Draw.alpha(0.3f);
            }

            Draw.reset();
        }

        @Override
        public void write(Writes write)
        {
            super.write(write);
            write.f(heat);
            write.f(rotation);
        }

        @Override
        public void read(Reads read, byte revision)
        {
            super.read(read, revision);
            rotation = read.f();
            heat = read.f();
        }

        @Override
        public double sense(LAccess sensor)
        {
            if(sensor == LAccess.heat) return heat;
            return super.sense(sensor);
        }
    }
}