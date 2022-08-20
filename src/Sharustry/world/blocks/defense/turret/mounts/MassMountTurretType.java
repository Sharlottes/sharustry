package Sharustry.world.blocks.defense.turret.mounts;

import Sharustry.content.SBullets;
import Sharustry.world.blocks.defense.turret.DriverBulletData;
import Sharustry.world.blocks.defense.turret.MultiTurret;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.IntSeq;
import arc.struct.OrderedSet;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pools;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.MassDriver;

import static mindustry.Vars.*;
import static mindustry.Vars.content;

public class MassMountTurretType extends MountTurretType {
    public int minDistribute = 10;
    public int maxDistribute = 300;
    public float translation = 7f;
    public float knockback = 4f;
    public float bulletSpeed = 5.5f;
    public float bulletLifetime = 200f;
    public Effect receiveEffect = Fx.mineBig;
    public MassMountTurretType(String name) {
        super(name);
    }
    @Override
    public MountTurret create(MultiTurret block, MultiTurret.MultiTurretBuild build, int index, float x, float y) {
        return new MassMountTurret(this, block, build, index, x, y);
    }
    @Override
    public void drawPlace(MultiTurret block, int mount, int x, int y, int rotation, boolean valid) {
        super.drawPlace(block, mount, x, y, rotation, valid);
        float tX = x * tilesize + block.offset + (block.customMountLocation ? block.customgetMountLocationX.get(mount) : this.xOffset);
        float tY = y * tilesize + block.offset + (block.customMountLocation ? block.customgetMountLocationY.get(mount) : this.yOffset);

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

    public class MassMountTurret extends MountTurret<MassMountTurretType> {
        public MassDriver.DriverState massState = MassDriver.DriverState.idle;
        public OrderedSet<Tile> waitingShooters = new OrderedSet<>();
        public int link = -1;
        public int linkIndex = mountIndex;

        public MassMountTurret(MassMountTurretType type, MultiTurret block, MultiTurret.MultiTurretBuild build, int i, float x, float y) {
            super(type, block, build, i, x, y);
        }

        @Override
        public boolean onConfigureTileTapped(Building other) {
            if(link == other.pos()) {
                build.configure(IntSeq.with(mountIndex, -1));
                return false;
            }

            if(build.linkedMount != this) return true;

            if(build == other){
                build.configure(IntSeq.with(mountIndex, -1));
                return false;
            }
            else if(other instanceof MultiTurret.MultiTurretBuild turret && turret.hasMass()
                    && other.dst(build.tile) <= type.range && other.team == build.team){
                build.configure(IntSeq.with(mountIndex, other.pos()));
                return false;
            }
            return true;
        }

        Tile currentShooter(){
            return waitingShooters.isEmpty() ? null : waitingShooters.first();
        }

        boolean shooterValid(Tile other){
            if(other == null) return true;
            return other.build instanceof MultiTurret.MultiTurretBuild turret
                    && ((MassMountTurret) turret.mounts.get(linkIndex)).link == build.tile.pos()
                    && build.tile.dst(other) <= type.range;
        }

        boolean linkValid(){
            if(link == -1) return false;

            Building link = world.build(this.link);
            return (link.block instanceof MultiTurret turret
                    && turret.basicMounts.size - 1 >= mountIndex
                    && turret.basicMounts.get(linkIndex) instanceof MassMountTurretType)
                    && link.team == build.team && build.within(link, type.range);
        }

        @Override
        public void handlePayload(Bullet bullet, DriverBulletData data) {
            int totalItems = build.items.total();

            //add all the items possible
            for(int i = 0; i < data.items.length; i++){
                int maxAdd = Math.min(data.items[i], block.itemCapacity * 2 - totalItems);
                build.items.add(content.item(i), maxAdd);
                data.items[i] -= maxAdd;
                totalItems += maxAdd;

                if(totalItems >= block.itemCapacity * 2){
                    break;
                }
            }

            Effect.shake(type.shake, type.shake, build);
            type.receiveEffect.at(bullet);

            reloadCounter = 0f;
            bullet.remove();
        }

        @Override
        public void drawConfigure() {
            if(build.linkedMount != this && build.linkedMount != null) return;

            Vec2 vec = getMountLocation();
            float sin = Mathf.absin(Time.time, 6 / 2f, 1f);

            Draw.color(Pal.accent);
            Lines.stroke(1f);
            Drawf.circles(vec.x, vec.y, (build.tile.block().size  / 2f/ 2f + 1) * tilesize + sin - 2f, Pal.accent);

            for(Tile shooter : waitingShooters){
                Drawf.circles(shooter.drawx(), shooter.drawy(), (build.tile.block().size / 2f / 2f + 1) * tilesize + sin - 2f, Pal.place);
                Drawf.arrow(shooter.drawx(), shooter.drawy(), vec.x, vec.y, block.size / 2f * tilesize + sin, 4f + sin, Pal.place);
            }

            if(linkValid()){
                Building target = world.build(link);
                Drawf.circles(target.x, target.y, (target.block().size / 2f / 2f + 1) * tilesize + sin - 2f, Pal.place);
                Drawf.arrow(vec.x, vec.y, target.x, target.y, block.size / 2f * tilesize + sin, 4f + sin);
            }

            Drawf.dashCircle(vec.x, vec.y, type.range, Pal.accent);
        }

        @Override
        public void updateTile() {
            super.updateTile();

            Building link = world.build(this.link);
            boolean hasLink = linkValid();
            Vec2 vec = getMountLocation();

            if (hasLink) this.link = link.pos();

            //reloadCounter regardless of state
            if (reloadCounter < reload) reloadCounter += build.delta() * getPowerEfficiency();


            //cleanup waiting shooters that are not valid
            if (!shooterValid(currentShooter())) {
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
                    || build.mounts.find(m -> m instanceof DrillMountTurretType.DrillMountTurret drill && drill.mineTile == null) == null)
                build.dump();
            //skip when there's no power
            if (getPowerEfficiency() <= 0.001f) return;

            if (massState == MassDriver.DriverState.accepting) {
                //if there's nothing shooting at this, bail - OR, items full
                if (currentShooter() == null || (block.itemCapacity - build.items.total() < minDistribute)) {
                    massState = MassDriver.DriverState.idle;
                    return;
                }
                rotation = Mathf.slerpDelta(rotation, build.tile.angleTo(currentShooter()), rotateSpeed * getPowerEfficiency());

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
                        if (!(((MultiTurret.MultiTurretBuild) link).mounts.get(mountIndex).type instanceof MassMountTurretType))
                            linkIndex = ((MultiTurret.MultiTurretBuild) link).mounts.indexOf(((MultiTurret.MultiTurretBuild) link).mounts.copy().filter(m -> m.type instanceof MassMountTurretType).peek());

                        Vec2 lvec = ((MultiTurret.MultiTurretBuild) link).mounts.get(linkIndex).getMountLocation();
                        float targetRotation = Angles.angle(vec.x, vec.y, lvec.x, lvec.y);
                        MassMountTurret linkedMount = (MassMountTurret) other.mounts.get(linkIndex);
                        linkedMount.waitingShooters.add(build.tile);

                        if (reloadCounter >= reload && !charging) {
                            rotation = Mathf.slerpDelta(rotation, targetRotation, rotateSpeed * getPowerEfficiency());
                            //fire when it's the first in the queue and angles are ready.
                            if (linkedMount.currentShooter() == build.tile &&
                                    linkedMount.massState == MassDriver.DriverState.accepting
                                    && Angles.near(rotation, targetRotation, 2f)
                                    && Angles.near(linkedMount.rotation, targetRotation + 180f, 2f)) {
                                reloadCounter = 0f;

                                DriverBulletData data = Pools.obtain(DriverBulletData.class, DriverBulletData::new);
                                data.massMount = mountIndex;
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
                                
                                Vec2 linkedVec = linkedMount.getMountLocation();
                                float angle = Angles.angle(vec.x, vec.y, linkedVec.x, linkedVec.y);

                                SBullets.mountDriverBolt.create(build, build.team,
                                        vec.x + Angles.trnsx(angle, translation), vec.y + Angles.trnsy(angle, translation),
                                        angle, -1f, bulletSpeed, bulletLifetime, data);

                                shootEffect.at(vec.x + Angles.trnsx(angle, translation), vec.y + Angles.trnsy(angle, translation), angle);
                                smokeEffect.at(vec.x + Angles.trnsx(angle, translation), vec.y + Angles.trnsy(angle, translation), angle);
                                Effect.shake(shake, shake, new Vec2(vec.x, vec.y));
                                shootSound.at(build.tile, Mathf.random(0.9f, 1.1f));

                                float timeToArrive = Math.min(bulletLifetime, Mathf.dst(vec.x, vec.y, linkedVec.x, linkedVec.y) / bulletSpeed);
                                Time.run(timeToArrive, () -> {
                                    MassMountTurret currentLinkedMount = (MassMountTurret) other.mounts.get(linkIndex);
                                    //remove waiting shooters, it's done firing
                                    currentLinkedMount.waitingShooters.remove(build.tile);
                                    currentLinkedMount.massState = MassDriver.DriverState.idle;
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
            write.i(mountIndex);
            write.i(linkIndex);
            write.i(massState.ordinal());
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            link = read.i();
            mountIndex = read.i();
            linkIndex = read.i();
            massState = MassDriver.DriverState.all[read.i()];
        }
    }
}
