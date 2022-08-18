package Sharustry.world.blocks.defense.turret;

import Sharustry.entities.bullet.construct.ConstructBulletType;
import arc.*;
import arc.graphics.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.meta.*;

public class MultiConstructTurret extends MultiTurret {
    public int maxConstruct = 10;

    public MultiConstructTurret(String name){
        super(name);
    }

    public class MultiConstructTurretBuild extends MultiTurretBuild {
        public boolean selected;
        public int totalConstruct;
        public int h;

        @Override
        public void drawSelect() {
            selected = true;
            super.drawSelect();
        }

        @Override
        protected void updateShooting(){
            if(!(totalConstruct < maxConstruct)) return;

            if(reloadCounter >= reload && !charging()){
                BulletType type = peekAmmo();
                shoot(type);
                reloadCounter = 0f;
            }else{
                reloadCounter += delta() * peekAmmo().reloadMultiplier * baseReloadSpeed();
            }
        }

        @Override
        public void updateTile() {
            for(int i = 0; i < Groups.bullet.size(); i ++) if(Groups.bullet.index(i).owner == self() && Groups.bullet.index(i).type == bullet) totalConstruct++;

            super.updateTile();
            totalConstruct = 0;
        }

        @Override
        public void display(Table table) {
            table.table(w -> {
                w.left();
                w.add(new Image(this.block.getDisplayIcon(this.tile))).size(32.0F);
                w.labelWrap(this.block.getDisplayName(this.tile)).left().width(190.0F).padLeft(5.0F);
                w.table(t -> {
                    t.left();
                    if (bullet instanceof ConstructBulletType) t.add(new Stack() {{
                        add(new Table(e -> {
                            e.add(new Image(((ConstructBulletType) bullet).frontRegion)).size(4*8f);
                            e.pack();
                        }));
                        add(new Table(e -> {
                            e.right().bottom();
                            Label label = new Label(() -> {
                                String col = "[green]";
                                h = 0;
                                for (int i = 0; i < Groups.bullet.size(); i++)
                                    if (Groups.bullet.index(i).owner == self() && Groups.bullet.index(i).type == bullet) {
                                        h++;
                                        if (h == maxConstruct) col = "[red]";
                                        else if (h > 0) col = "[yellow]";
                                        else if (h == 0) col = "[green]";
                                        else col = "[lightgray]";
                                    }
                                return col + h + " / " + maxConstruct;
                            });
                            label.setFontScale(0.8f);
                            e.add(label);
                        }));
                    }});
                });
            }).left();
            table.row();
            if (this.team == Vars.player.team()) {
                table.table((bars) -> {
                    bars.defaults().growX().height(18.0F).pad(4.0F);
                    this.displayBars(bars);
                }).growX();
                table.row();
                table.table(this::displayConsumption).growX();
                boolean displayFlow = (this.block.category == Category.distribution || this.block.category == Category.liquid) && this.block.displayFlow;
                if (displayFlow) {
                    String ps = " " + StatUnit.perSecond.localized();
                    if (this.items != null) {
                        table.row();
                        table.left();
                        table.table((l) -> {
                            Bits current = new Bits();
                            Runnable rebuild = () -> {
                                l.clearChildren();
                                l.left();

                                for (Item item : Vars.content.items()) {
                                    if (this.items.hasFlowItem(item)) {
                                        l.image(item.uiIcon).padRight(3.0F);
                                        l.label(() -> this.items.getFlowRate(item) < 0.0F ? "..." : Strings.fixed(this.items.getFlowRate(item), 1) + ps).color(Color.lightGray);
                                        l.row();
                                    }
                                }
                            };
                            rebuild.run();
                            l.update(() -> {
                                for (Item item : Vars.content.items()) {
                                    if (this.items.hasFlowItem(item) && !current.get(item.id)) {
                                        current.set(item.id);
                                        rebuild.run();
                                    }
                                }
                            });
                        }).left();
                    }

                    if (this.liquids != null) {
                        table.row();
                        table.table((l) -> {
                            boolean[] had = new boolean[]{false};
                            Runnable rebuild = () -> {
                                l.clearChildren();
                                l.left();
                                l.image(() -> this.liquids.current().uiIcon).padRight(3.0F);
                                l.label(() -> this.liquids.getFlowRate(this.liquids.current()) < 0.0F ? "..." : Strings.fixed(this.liquids.getFlowRate(this.liquids.current()), 2) + ps).color(Color.lightGray);
                            };
                            l.update(() -> {
                                if (!had[0] && this.liquids.hasFlowLiquid(this.liquids.current())) {
                                    had[0] = true;
                                    rebuild.run();
                                }

                            });
                        }).left();
                    }
                }

                if (Vars.net.active() && this.lastAccessed != null) {
                    table.row();
                    table.add(Core.bundle.format("lastaccessed", this.lastAccessed)).growX().wrap().left();
                }

                table.marginBottom(-5.0F);
            }
        }
    }
}
