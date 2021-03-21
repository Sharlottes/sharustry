package Sharustry.world.blocks.defense;

import arc.*;
import arc.graphics.*;
import arc.math.Mathf;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;
public class MultiConstructTurret extends MultiTurret {
    public int maxConstruct = 10;

    public MultiConstructTurret(String name){
        super(name);
    }

    public class MultiConstructTurretBuild extends MultiTurretBuild {
        public int totalConstruct;
        public int h;

        @Override
        protected void shoot(BulletType type){

            //when charging is enabled, use the charge shoot pattern
            if(chargeTime > 0){
                useAmmo();

                tr.trns(rotation, shootLength);
                chargeBeginEffect.at(x + tr.x, y + tr.y, rotation);
                chargeSound.at(x + tr.x, y + tr.y, 1);

                for(int i = 0; i < chargeEffects; i++){
                    Time.run(Mathf.random(chargeMaxDelay), () -> {
                        if(!isValid()) return;
                        tr.trns(rotation, shootLength);
                        chargeEffect.at(x + tr.x, y + tr.y, rotation);
                    });
                }

                charging = true;

                Time.run(chargeTime, () -> {
                    if(!isValid()) return;
                    tr.trns(rotation, shootLength);
                    recoil = recoilAmount;
                    heat = 1f;
                    bullet(type, rotation + Mathf.range(inaccuracy));
                    effects();
                    charging = false;
                });

                //when burst spacing is enabled, use the burst pattern
            }else if(burstSpacing > 0.0001f){
                for(int i = 0; i < shots; i++){
                    if(!(totalConstruct < maxConstruct)) return;
                    Time.run(burstSpacing * i, () -> {
                        if(!isValid() || !hasAmmo()) return;

                        recoil = recoilAmount;

                        tr.trns(rotation, shootLength, Mathf.range(xRand));
                        bullet(type, rotation + Mathf.range(inaccuracy));
                        effects();
                        useAmmo();
                        recoil = recoilAmount;
                        heat = 1f;
                    });
                }

            }else{
                //otherwise, use the normal shot pattern(s)

                if(alternate){
                    float i = (shotCounter % shots) - (shots-1)/2f;

                    tr.trns(rotation - 90, spread * i + Mathf.range(xRand), shootLength);
                    bullet(type, rotation + Mathf.range(inaccuracy));
                }else{
                    tr.trns(rotation, shootLength, Mathf.range(xRand));

                    for(int i = 0; i < shots; i++){
                        bullet(type, rotation + Mathf.range(inaccuracy + type.inaccuracy) + (i - (int)(shots / 2f)) * spread);
                    }
                }

                shotCounter++;

                recoil = recoilAmount;
                heat = 1f;
                effects();
                useAmmo();
            }
        }

        @Override
        protected void updateShooting(){
            if(!(totalConstruct < maxConstruct)) return;

            if(reload >= reloadTime && !charging){
                BulletType type = peekAmmo();
                shoot(type);
                reload = 0f;
            }else{
                reload += delta() * peekAmmo().reloadMultiplier * baseReloadSpeed();
            }
        }

        @Override
        public void updateTile() {
            for(int i = 0; i < Groups.bullet.size(); i ++) if(Groups.bullet.index(i).owner == self() && Groups.bullet.index(i).type == bullet) totalConstruct++;

            super.updateTile();
            totalConstruct=0;
        }

        @Override
        public void display(Table table){
            //display the block stuff
            table.table(w -> {
                w.left();
                w.add(new Image(block.getDisplayIcon(tile))).size(8 * 4);
                w.labelWrap(block.getDisplayName(tile)).left().width(190f).padLeft(5);

                w.table(t -> {
                    t.left();
                    if(bullet instanceof BasicBulletType) t.add(new Stack(){{
                        add(new Table(e -> {
                            e.add(new Image(((BasicBulletType)bullet).frontRegion));
                            e.pack();
                        }));
                        add(new Table(e -> {
                            e.right().bottom();
                            e.add(new Label(()-> {
                                String col = "[green]";
                                h = 0;
                                for(int i = 0; i < Groups.bullet.size(); i++)
                                    if(Groups.bullet.index(i).owner == self() && Groups.bullet.index(i).type == bullet){
                                        h++;
                                        if(h == maxConstruct) col = "[red]";
                                        else if(h > 0) col = "[yellow]";
                                        else if(h == 0) col = "[green]";
                                        else col = "[lightgray]";
                                    }
                                return col + h + " / " + maxConstruct;
                            }));
                        }));
                    }}).growX().left();
                }).left();
            }).growX().left();
            table.row();

            //only display everything else if the team is the same
            if(team == player.team()){
                table.table(bars -> {
                    bars.defaults().growX().height(18f).pad(4);

                    displayBars(bars);
                }).growX();
                table.row();
                table.table(this::displayConsumption).growX();

                boolean displayFlow = (block.category == Category.distribution || block.category == Category.liquid) && Core.settings.getBool("flow") && block.displayFlow;

                if(displayFlow){
                    String ps = " " + StatUnit.perSecond.localized();

                    if(items != null){
                        table.row();
                        table.left();
                        table.table(l -> {
                            Bits current = new Bits();

                            Runnable rebuild = () -> {
                                l.clearChildren();
                                l.left();
                                for(Item item : content.items()){
                                    if(items.hasFlowItem(item)){
                                        l.image(item.icon(Cicon.small)).padRight(3f);
                                        l.label(() -> items.getFlowRate(item) < 0 ? "..." : Strings.fixed(items.getFlowRate(item), 1) + ps).color(Color.lightGray);
                                        l.row();
                                    }
                                }
                            };

                            rebuild.run();
                            l.update(() -> {
                                for(Item item : content.items()){
                                    if(items.hasFlowItem(item) && !current.get(item.id)){
                                        current.set(item.id);
                                        rebuild.run();
                                    }
                                }
                            });
                        }).left();
                    }

                    if(liquids != null){
                        table.row();
                        table.table(l -> {
                            boolean[] had = {false};

                            Runnable rebuild = () -> {
                                l.clearChildren();
                                l.left();
                                l.image(() -> liquids.current().icon(Cicon.small)).padRight(3f);
                                l.label(() -> liquids.getFlowRate() < 0 ? "..." : Strings.fixed(liquids.getFlowRate(), 2) + ps).color(Color.lightGray);
                            };

                            l.update(() -> {
                                if(!had[0] && liquids.hadFlow()){
                                    had[0] = true;
                                    rebuild.run();
                                }
                            });
                        }).left();
                    }
                }

                if(net.active() && lastAccessed != null){
                    table.row();
                    table.add(Core.bundle.format("lastaccessed", lastAccessed)).growX().wrap().left();
                }

                table.marginBottom(-5);
            }
        }
    }
}
