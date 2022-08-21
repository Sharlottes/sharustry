package Sharustry.world.blocks.defense.turret.mounts;

import Sharustry.world.blocks.defense.turret.MultiTurret;
import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.util.Tmp;
import mindustry.content.StatusEffects;
import mindustry.entities.Units;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.StatusEffect;
import mindustry.world.meta.Stat;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;

public class TractMountTurretType extends MountTurretType {

    public float force = 0.3f;
    public float scaledForce = 0f;
    public float damage = 0f;
    public float statusDuration = 300;

    public StatusEffect status = StatusEffects.none;

    public TextureRegion tractLaser, tractLaserEnd;
    public TractMountTurretType(String name) {
        super(name);
    }
    @Override
    public MountTurret create(MultiTurret block, MultiTurret.MultiTurretBuild build, int index, float x, float y) {
        return new TractMountTurret(this, block, build, index, x, y);
    }

    @Override
    public void load() {
        super.load();
        tractLaser = Core.atlas.find("shar-tlaser");
        tractLaserEnd = Core.atlas.find("shar-tlaser-end");
    }

    @Override
    public void buildStat(Table table) {
        super.buildStat(table);
        table.add("[lightgray]" + Stat.damage.localized() + ": [white]" + Core.bundle.format("stat.shar.damage", damage * 60f)).row();
    }


    public class TractMountTurret extends MountTurret<TractMountTurretType> {
        float lastX = 0f;
        float lastY = 0f;
        boolean any = false;
        public Unit tractTarget;
        public TractMountTurret(TractMountTurretType type, MultiTurret block, MultiTurret.MultiTurretBuild build, int i, float x, float y) {
            super(type, block, build, i, x, y);
        }

        @Override
        public void findTarget(){
            super.findTarget();
            Vec2 vec = getMountLocation();
            tractTarget = Units.closestEnemy(build.team, vec.x, vec.y, type.range, u -> u.checkTarget(type.targetAir, type.targetGround));
        }

        @Override
        public void updateTile(){
            super.updateTile();

            Vec2 vec = getMountLocation();
            any = false;

            //look at target
            if(tractTarget != null
                    && tractTarget.within(new Vec2(vec.x, vec.y), range + tractTarget.hitSize/2f)
                    && tractTarget.team() != build.team
                    && tractTarget.checkTarget(targetAir, targetGround)
                    && getPowerEfficiency() > 0.02f){
                if(!headless) control.sound.loop(shootSound, new Vec2(vec.x, vec.y), shootSoundVolume);

                float dest = build.angleTo(tractTarget);
                turnToTarget(dest);
                lastX = tractTarget.x;
                lastY = tractTarget.y;
                strength = Mathf.lerpDelta(strength, 1f, 0.1f);

                //shoot when possible
                if(Angles.within(rotation, dest, shootCone)){
                    if(damage > 0) tractTarget.damageContinuous(damage * getPowerEfficiency());

                    if(status != StatusEffects.none) tractTarget.apply(status, statusDuration);

                    any = true;
                    tractTarget.impulseNet(
                            Tmp.v1.set(new Vec2(vec.x, vec.y))
                                    .sub(tractTarget)
                                    .limit((force + (1f - tractTarget.dst(new Vec2(vec.x, vec.y)) / range) * scaledForce) * build.delta() * getPowerEfficiency()));
                }
            }else {
                strength = Mathf.lerpDelta(strength, 0, 0.1f);
            }
        }

        @Override
        public void draw(){
            super.draw();
            if(!any) return;

            Vec2 vec = getMountLocation();
            Draw.z(Layer.bullet);
            float ang = build.angleTo(lastX, lastY);

            Draw.mixcol(type.laserColor, Mathf.absin(4f, 0.6f));
            Drawf.laser(type.tractLaser, type.tractLaserEnd,
                    vec.x + Angles.trnsx(ang, type.shootLength), vec.y + Angles.trnsy(ang, type.shootLength),
                    lastX, lastY, strength * getPowerEfficiency() * type.laserWidth);
        }
    }
}
