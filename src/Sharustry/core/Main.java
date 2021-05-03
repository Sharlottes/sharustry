package Sharustry.core;

import Sharustry.content.STurretMounts;
import Sharustry.ui.SBar;
import Sharustry.world.blocks.defense.MountTurretType;
import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.*;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.entities.abilities.ShieldRegenFieldAbility;
import mindustry.entities.units.WeaponMount;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;

import Sharustry.content.ModLoader;
import mindustry.type.*;
import mindustry.ui.*;

import static arc.Core.scene;
import static mindustry.Vars.*;

public class Main extends Mod {
    float scrollPos;
    public Main(){
        Events.on(ClientLoadEvent.class, e -> {
            addTable();

            for(MountTurretType mount : STurretMounts.mounttypes) mount.load();
        });

        Events.on(WorldLoadEvent.class, e -> {
            addTable();
        });
    }

    public void addTable(){
        Vars.ui.hudGroup.addChild(new Table(table -> {
            table.update(() -> {
                if(Time.time % 60 == 0) {
                    table.remove();
                }
            });
            table.left();
            ImageButton button = table.button(Tex.whiteui, Styles.clearToggleTransi, 24, () -> {
                table.remove();
                table.remove();
                table.remove();
                table.remove();
                addTable();
                table.remove();
            }).get();
            button.getStyle().imageUp = Icon.cancel;
            button.setSize(16f);
            table.add(button).left().top();
            table.row();
            table.table(scene.getStyle(Button.ButtonStyle.class).up, t -> {
                t.defaults().size(25 * 8f);
                t.table(Tex.underline2,  tt -> {
                    tt.defaults().width(23 * 8f);
                    tt.defaults().height(4f * 8f);
                    tt.top();

                    tt.add(new SBar(
                            () -> Core.bundle.format("shar-stat.health", Mathf.round(player.unit().health,1)),
                            () -> Pal.health,
                            () -> Mathf.clamp(player.unit().health / player.unit().type.health)
                    )).growX().left();
                    tt.row();
                    tt.add(new Stack(){{
                        add(new Table(temp -> {
                            temp.defaults().width(23 * 8f);
                            temp.defaults().height(4f * 8f);
                            temp.add(new SBar(
                                    () -> Core.bundle.format("shar-stat.shield", Mathf.round(player.unit().shield,1)),
                                    () -> Pal.surge,
                                    () -> {
                                        ShieldRegenFieldAbility ability = (ShieldRegenFieldAbility)content.units().copy().filter(ut -> ut.abilities.find(abil -> abil instanceof ShieldRegenFieldAbility) != null).sort(ut -> {
                                            ShieldRegenFieldAbility ability1 = (ShieldRegenFieldAbility)ut.abilities.find(abil -> abil instanceof ShieldRegenFieldAbility);
                                            return ability1.max;
                                        }).peek().abilities.find(abil -> (abil instanceof ShieldRegenFieldAbility));
                                        float max = ability.max;
                                        return Mathf.clamp(player.unit().shield / max);
                                    }
                            )).growX().left();
                        }));
                        add(new Table(temp -> {
                            temp.left();
                            temp.add(new Image(Icon.defense));
                            temp.pack();
                        }));
                        add(new Table(temp -> {
                            temp.left();
                            Label label = new Label(() -> (int)(player.unit().type == null ? 0 : player.unit().type.armor) + "");
                            label.setColor(Pal.lightishGray);
                            label.setSize(0.6f);
                            temp.add(label).padLeft(7.5f);
                            temp.pack();
                        }));
                    }}).growX().left();
                });
            });
            UnitType type = player.unit().type;
            try{table.table(tx -> tx.table(scene.getStyle(Button.ButtonStyle.class).up, tt -> {
                tt.defaults().height(24 * 8f);
                tt.top();

                for(int r = 0; r < type.weapons.size; r++){
                    final int i = r;
                    Weapon weapon = type.weapons.get(i);
                    WeaponMount mount = player.unit().mounts[i];
                    TextureRegion region = !weapon.name.equals("") && weapon.outlineRegion.found() ? weapon.outlineRegion : type.icon(Cicon.full);
                    if(type.weapons.size > 1 && i % 2 == 0) tt.row();
                    else if(i % 2 == 0) tt.row();
                    tt.table(weapontable -> {
                        weapontable.add(new Stack(){{
                            add(new Table(o -> {
                                o.left();
                                o.add(new Image(region));
                            }));

                            add(new Table(h -> {
                                h.add(new Stack(){{
                                    add(new Table(e -> {
                                        e.defaults().growX().height(9).width(42f).padRight(2*8).padTop(8*2f);
                                        Bar reloadBar = new Bar(
                                                () -> "",
                                                () -> Pal.accent.cpy().lerp(Color.orange, mount.reload / weapon.reload),
                                                () -> mount.reload / weapon.reload);
                                        e.add(reloadBar);
                                        e.pack();
                                    }));
                                }}).padTop(2*8).padLeft(2*8);
                                h.pack();
                            }));
                        }}).left();
                    }).left();
                    tt.center();
                }
                ScrollPane pane = new ScrollPane(tt, Styles.smallPane);
                pane.setScrollingDisabled(true, false);
                pane.setScrollYForce(scrollPos);
                pane.update(() -> scrollPos = pane.getScrollY());

                pane.setOverscroll(false, false);
                tx.add(pane).maxHeight(Scl.scl(24 * 8f));
            }));
            }catch(Throwable err){ Log.info(err);}

            table.fillParent = true;
            table.visibility = () ->
                    Vars.ui.hudfrag.shown && !Vars.ui.minimapfrag.shown()
                            && (!Vars.mobile ||
                            !(player.unit().isBuilding() || Vars.control.input.block != null || !Vars.control.input.selectRequests.isEmpty()
                                    && !(Vars.control.input.lastSchematic != null && !Vars.control.input.selectRequests.isEmpty())));
        }));
    }
    @Override
    public void init(){
    }

    @Override
    public void loadContent(){
        new ModLoader().load();
    }


}
