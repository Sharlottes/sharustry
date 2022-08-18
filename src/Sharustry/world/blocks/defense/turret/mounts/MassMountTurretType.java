package Sharustry.world.blocks.defense.turret.mounts;

import Sharustry.content.SBullets;
import Sharustry.world.blocks.defense.turret.DriverBulletData;
import Sharustry.world.blocks.defense.turret.MountTurret;
import Sharustry.world.blocks.defense.turret.MultiTurret;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pools;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.world.blocks.distribution.MassDriver;

import static mindustry.Vars.*;
import static mindustry.Vars.content;

public class MassMountTurretType extends MountTurretType {
    public MassMountTurretType(String name) {
        super(name);
    }

    @Override
    public void drawPlace(MultiTurret block, int mount, int x, int y, int rotation, boolean valid) {
        super.drawPlace(block, mount, x, y, rotation, valid);
        float tX = x * tilesize + block.offset + (block.customMountLocation ? block.customMountLocationsX.get(mount) : this.x);
        float tY = y * tilesize + block.offset + (block.customMountLocation ? block.customMountLocationsY.get(mount) : this.y);

        //check if a mass driver is selected while placing this driver
        if(!control.input.config.isShown()) return;
        Building selected = control.input.config.getSelected();
        if(selected == null || !(selected.block instanceof MultiTurret) || !(selected.within(x * tilesize, y * tilesize, range))) return;

        //if so, draw a dotted line towards it while it is in range
        float sin = Mathf.absin(Time.time, 6f, 1f);
        Tmp.v1.set(tX, tY).sub(selected.x, selected.y).limit((block.size / 2f + 1) * tilesize + sin + 0.5f);
        float x2 = x * tilesize - Tmp.v1.x, y2 = y * tilesize - Tmp.v1.y,
                x1 = selected.x + Tmp.v1.x, y1 = selected.y + Tmp.v1.y;
        int segs = (int)(selected.dst(x * tilesize, y * tilesize) / tilesize);

        Lines.stroke(4f, Pal.gray);
        Lines.dashLine(x1, y1, x2, y2, segs);
        Lines.stroke(2f, Pal.placing);
        Lines.dashLine(x1, y1, x2, y2, segs);
        Draw.reset();
    }

    public class MassMountTurret extends MountTurret {
        public MassMountTurret(MountTurretType type, MultiTurret block, MultiTurret.MultiTurretBuild build, int i, float x, float y) {
            super(type, block, build, i, x, y);
        }

        @Override

        public void updateTile(MultiTurret.MultiTurretBuild build) {
            Building link = world.build(this.link);
            boolean hasLink = linkValid(build);
            float[] loc = mountLocations(build);

            if (hasLink) this.link = link.pos();

            //reloadCounter regardless of state
            if (reloadCounter < reload) reloadCounter += build.delta() * getPowerEfficiency(build);


            //cleanup waiting shooters that are not valid
            if (!shooterValid(build, currentShooter(), i)) {
                waitingShooters.remove(currentShooter());
            }

            //switch states
            if (massState == MassDriver.DriverState.idle) {
                //start accepting when idle and there's space
                if (!waitingShooters.isEmpty() && (block.itemCapacity - build.items.total() >= minDistribute)) {
                    massState = MassDriver.DriverState.accepting;
                } else if (hasLink) { //switch to shooting if there's a valid link.
                    massState = MassDriver.DriverState.shooting;
                }
            }

            //dump when idle or accepting
            if (massState == MassDriver.DriverState.idle
                    || massState == MassDriver.DriverState.accepting
                    || build.mounts.find(m -> m.type instanceof DrillMountTurretType && m.mineTile == null) == null)
                build.dump();
            //skip when there's no power
            if (getPowerEfficiency(build) <= 0.001f) return;

            if (massState == MassDriver.DriverState.accepting) {
                //if there's nothing shooting at this, bail - OR, items full
                if (currentShooter() == null || (block.itemCapacity - build.items.total() < minDistribute)) {
                    massState = MassDriver.DriverState.idle;
                    return;
                }
                rotation = Mathf.slerpDelta(rotation, build.tile.angleTo(currentShooter()), rotateSpeed * getPowerEfficiency(build));

            } else if (massState == MassDriver.DriverState.shooting) {
                //if there's nothing to shoot at OR someone wants to shoot at this thing, bail
                if (!hasLink || (!waitingShooters.isEmpty() && (block.itemCapacity - build.items.total() >= minDistribute))) {
                    massState = MassDriver.DriverState.idle;
                    return;
                }

                if (build.items.total() >= minDistribute && //must shoot minimum amount of items
                        link.block.itemCapacity - link.items.total() >= minDistribute //must have minimum amount of space
                ) {
                    if (link instanceof MultiTurret.MultiTurretBuild other) {
                        if (!(((MultiTurret.MultiTurretBuild) link).mounts.get(i).type instanceof MassMountTurretType))
                            linkIndex = ((MultiTurret.MultiTurretBuild) link).mounts.indexOf(((MultiTurret.MultiTurretBuild) link).mounts.copy().filter(m -> m.type instanceof MassMountTurretType).peek());

                        float targetRotation = Angles.angle(mountLocations(build)[0], mountLocations(build)[1], ((MultiTurret.MultiTurretBuild) link).mounts.get(linkIndex).mountLocations(((MultiTurret.MultiTurretBuild) link))[0], ((MultiTurret.MultiTurretBuild) link).mounts.get(linkIndex).mountLocations(((MultiTurret.MultiTurretBuild) link))[1]);

                        other.mounts.get(linkIndex).waitingShooters.add(build.tile);

                        if (reloadCounter >= reload && !charging) {
                            rotation = Mathf.slerpDelta(rotation, targetRotation, rotateSpeed * getPowerEfficiency(build));
                            //fire when it's the first in the queue and angles are ready.
                            if (other.mounts.get(linkIndex).currentShooter() == build.tile &&
                                    other.mounts.get(linkIndex).massState == MassDriver.DriverState.accepting
                                    && Angles.near(rotation, targetRotation, 2f)
                                    && Angles.near(other.mounts.get(linkIndex).rotation, targetRotation + 180f, 2f)) {
                                reloadCounter = 0f;

                                DriverBulletData data = Pools.obtain(DriverBulletData.class, DriverBulletData::new);
                                data.massMount = i;
                                data.from = build;
                                data.to = other;
                                data.link = linkIndex;

                                int totalUsed = 0;
                                for (int h = 0; h < content.items().size; h++) {
                                    int maxTransfer = Math.min(build.items.get(content.item(h)), build.tile.block().itemCapacity - totalUsed);
                                    data.items[h] = maxTransfer;
                                    totalUsed += maxTransfer;
                                    build.items.remove(content.item(h), maxTransfer);
                                }

                                float angle = Angles.angle(loc[4], loc[5], other.mounts.get(linkIndex).mountLocations(((MultiTurret.MultiTurretBuild) link))[4], other.mounts.get(linkIndex).mountLocations(((MultiTurret.MultiTurretBuild) link))[5]);

                                SBullets.mountDriverBolt.create(build, build.team,
                                        loc[4] + Angles.trnsx(angle, translation), loc[5] + Angles.trnsy(angle, translation),
                                        angle, -1f, bulletSpeed, bulletLifetime, data);

                                shootEffect.at(loc[4] + Angles.trnsx(angle, translation), loc[5] + Angles.trnsy(angle, translation), angle);
                                smokeEffect.at(loc[4] + Angles.trnsx(angle, translation), loc[5] + Angles.trnsy(angle, translation), angle);
                                Effect.shake(shake, shake, new Vec2(loc[4], loc[5]));
                                shootSound.at(build.tile, Mathf.random(0.9f, 1.1f));

                                float timeToArrive = Math.min(bulletLifetime, Mathf.dst(loc[4], loc[5], other.mounts.get(linkIndex).mountLocations(((MultiTurret.MultiTurretBuild) link))[4], other.mounts.get(linkIndex).mountLocations(((MultiTurret.MultiTurretBuild) link))[5]) / bulletSpeed);
                                Time.run(timeToArrive, () -> {
                                    //remove waiting shooters, it's done firing
                                    other.mounts.get(linkIndex).waitingShooters.remove(build.tile);
                                    other.mounts.get(linkIndex).massState = MassDriver.DriverState.idle;
                                });
                                //driver is immediately idle
                                massState = MassDriver.DriverState.idle;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(link);
            write.i(i);
            write.i(linkIndex);
            write.i(massState.ordinal());
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            link = read.i();
            i = read.i();
            linkIndex = read.i();
            massState = MassDriver.DriverState.all[read.i()];
        }
    }
}
