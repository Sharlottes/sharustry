package Sharustry.world.blocks.defense.turret.mounts;

import Sharustry.world.blocks.defense.turret.MultiTurret;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Time;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.input.InputHandler;
import mindustry.type.Item;
import mindustry.world.Tile;

import static mindustry.Vars.*;
import static mindustry.Vars.control;

public class DrillMountTurretType extends MountTurretType {
    public int minDrillTier = 0, maxDrillTier = 3;
    public float mineSpeed = 0.75f;
    public float laserOffset = 4f;
    public DrillMountTurretType(String name) {
        super(name);
    }
    @Override
    public MountTurret create(MultiTurret block, MultiTurret.MultiTurretBuild build, int index, float x, float y) {
        return new DrillMountTurret(this, block, build, index, x, y);
    }
    public class DrillMountTurret extends MountTurret<DrillMountTurretType> {
        int targetID = -1;
        float mineTimer = 0f;
        float reOreHeat = 0f;
        float reItemHeat = 0f;
        Seq<Tile> proxOres = new Seq<>();
        Seq<Item> proxItems = new Seq<>();
        Item targetItem;
        Tile ore;
        public Tile mineTile;
        public DrillMountTurret(DrillMountTurretType type, MultiTurret block, MultiTurret.MultiTurretBuild build, int i, float x, float y) {
            super(type, block, build, i, x, y);
            ObjectSet<Item> tempItems = new ObjectSet<>();

            float[] loc = mountLocations();
            Geometry.circle((int) loc[0] / 8, (int)loc[1] / 8, (int)(type.range / tilesize + 0.5f), (cx, cy) -> {
                Tile other = world.tile(cx, cy);
                if(other != null && other.drop() != null){
                    Item drop = other.drop();
                    if(!tempItems.contains(drop)){
                        tempItems.add(drop);
                        proxItems.add(drop);
                        proxOres.add(other);
                    }
                }
            });
        }

        boolean mountCanMine(Item item){
            return item.hardness >= type.minDrillTier && item.hardness <= type.maxDrillTier && build.items.get(item) < block.itemCapacity;
        }
        @Nullable
        Item iterateMap(Building core){
            if(proxOres == null || !proxOres.any()) return null;
            Item last = null;
            targetID = -1;
            for(int h = 0; h < proxOres.size; h++){
                if(mountCanMine(proxItems.get(h)) && (last == null || last.lowPriority || core.items.get(last) > core.items.get(proxItems.get(h)))){
                    if(proxOres.get(h).block() != Blocks.air){
                        //try to relocate its ore
                        mountReFind(h);
                        //if it fails, ignore the ore
                        if(proxOres.get(h).block() != Blocks.air) continue;
                    }
                    last = proxItems.get(h);
                    targetID = h;
                }
            }

            return last;
        }

        void mountReFind(int h){
            Item item = proxItems.get(h);

            float[] loc = mountLocations();
            Geometry.circle((int) loc[0] / 8, (int)loc[1] / 8, (int)(type.range / tilesize + 0.5f), (x, y) -> {
                Tile other = world.tile(x, y);
                if(other != null && other.drop() != null && other.drop() == item && other.block() == Blocks.air){
                    proxOres.set(h, other);
                }
            });
        }

        void targetMine(Building core){
            if(core == null) return;
            
            if(reItemHeat < 0 || targetItem == null){
                targetItem = iterateMap(core);
                reItemHeat = 16;
            }

            //if inventory is full, do not mine.
            if(targetItem == null || build.items.get(targetItem) >= block.itemCapacity){
                mineTile = null;
            } else {
                if(getPowerEfficiency() >= 0.2f && reOreHeat <= 0 && targetItem != null && targetID > -1){
                    ore = proxOres.get(targetID);
                    reOreHeat = 16;
                }

                if(ore != null && getPowerEfficiency() >= 0.2f){
                    float dest = build.angleTo(ore);
                    turnToTarget(dest);

                    if(Angles.within(rotation, dest, type.shootCone)){
                        mineTile = ore;
                    }
                    if(ore.block() != Blocks.air){
                        if(targetID > -1) mountReFind(targetID);
                        targetItem = null;
                        targetID = -1;
                        mineTile = null;
                    }
                }
            }
        }


        @Override
        public void updateTile(){
            super.updateTile();


            reOreHeat -= build.delta();
            reItemHeat -= build.delta();
            float[] loc = mountLocations();
            Building core = state.teams.closestCore(loc[2], loc[3], build.team);
            //target ore
            targetMine(core);
            if (core == null
                    || mineTile == null
                    || Mathf.clamp(build.power.graph.getPowerBalance() / powerUse, 0, 1) <= 0.001f
                    || !Angles.within(rotation, build.angleTo(mineTile), shootCone)
                    || build.items.get(mineTile.drop()) >= block.itemCapacity) {
                mineTile = null;
                mineTimer = 0f;
            }

            if (mineTile != null) {
                //mine tile
                Item item = mineTile.drop();
                mineTimer += Time.delta * mineSpeed * getPowerEfficiency();

                if (Mathf.chance(0.06 * Time.delta)) {
                    Fx.pulverizeSmall.at(mineTile.worldx() + Mathf.range(tilesize / 2f), mineTile.worldy() + Mathf.range(tilesize / 2f), 0f, item.color);
                }

                if (mineTimer >= 50f + item.hardness * 15f) {
                    mineTimer = 0;

                    if (state.rules.sector != null && build.team() == state.rules.defaultTeam)
                        state.rules.sector.info.handleProduction(item, 1);

                    //items are synced anyway
                    InputHandler.transferItemTo(null, item, 1,
                            mineTile.worldx() + Mathf.range(tilesize / 2f),
                            mineTile.worldy() + Mathf.range(tilesize / 2f),
                            build);
                }

                if (!headless) control.sound.loop(shootSound, build, shootSoundVolume);
            }
        }

        @Override
        public void draw(){
            super.draw();
            if(mineTile == null) return;

            float[] loc = mountLocations();
            float focusLen = type.laserOffset / 2f + Mathf.absin(Time.time, 1.1f, 0.5f);
            float swingScl = 12f, swingMag = tilesize / 8f;
            float flashScl = 0.3f;

            float px = loc[2] + Angles.trnsx(rotation, focusLen);
            float py = loc[3] + Angles.trnsy(rotation, focusLen);

            float ex = mineTile.worldx() + Mathf.sin(Time.time + 48, swingScl, swingMag);
            float ey = mineTile.worldy() + Mathf.sin(Time.time + 48, swingScl + 2f, swingMag);

            Draw.z(Layer.flyingUnit + 0.1f);
            Draw.color(Color.lightGray, Color.white, 1f - flashScl + Mathf.absin(Time.time, 0.5f, flashScl));
            Drawf.laser(type.laser, type.laserEnd, px, py, ex, ey, type.laserWidth);
        }

        @Override
        public void removeFromProximity() {
            //reset when pushed
            targetItem = null;
            targetID = -1;
            mineTile = null;
        }

        @Override
        public void drawSelect(){
            super.drawSelect();
            
            float fade = Mathf.curve(Time.time % block.totalRangeTime, block.rangeTime * mountIndex, block.rangeTime * mountIndex + block.fadeTime) - Mathf.curve(Time.time % block.totalRangeTime, block.rangeTime * (mountIndex + 1) - block.fadeTime, block.rangeTime * (mountIndex + 1));
            float[] loc = mountLocations();
            if(mineTile != null){
                Lines.dashCircle(loc[0], loc[1], type.range);
                Lines.stroke(1, build.team.color);
                Draw.alpha(fade);
                Lines.dashCircle(loc[0], loc[1], type.range);

                Lines.stroke(1f, Pal.accent);
                Lines.poly(mineTile.worldx(), mineTile.worldy(), 4, tilesize / 2f * Mathf.sqrt2, Time.time);
                Draw.alpha(fade);
            }
        }
    }
}
