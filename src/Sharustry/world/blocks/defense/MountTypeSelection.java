package Sharustry.world.blocks.defense;

import arc.Core;
import arc.func.Cons;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.ImageButton;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.gen.Tex;
import mindustry.ui.Cicon;
import mindustry.ui.Styles;

import static mindustry.Vars.control;

public class MountTypeSelection {
    private static float scrollPos = 0f;

    public static void buildTable(Table motherTable, MultiTurret.MultiTurretBuild build, Table table, Seq<MountTurretType> mounts, Cons<MountTurretType> consumer, boolean closeSelect){
        Table cont = new Table();
        cont.defaults().maxWidth(24*7f);

        for(MountTurretType.MultiTurretMountType mountType : MountTurretType.MultiTurretMountType.values()){
            cont.row();
            cont.table(Tex.underline, t -> {
                t.left().top();
                t.add("[accent]" + mountType.toString() + "[]");
                t.row();
            });
            cont.row();
            cont.table(tt -> {
                tt.left().top();
                ButtonGroup<ImageButton> group = new ButtonGroup<>();
                group.setMinCheckCount(0);
                int i = 0;
                for(MountTurretType mount : mounts.copy().filter(m -> m.mountType == mountType)){
                    ImageButton button = tt.button(Tex.whiteui, Styles.clearToggleTransi, 40, () -> {
                        if(closeSelect) control.input.frag.config.hideConfig();
                        if(mount.mountType == MountTurretType.MultiTurretMountType.mass) {
                            motherTable.clear();
                            build.buildConfiguration(motherTable);
                        }
                    }).group(group).get();

                    button.changed(() -> consumer.get(button.isChecked() ? mount : null));
                    button.getStyle().imageUp = new TextureRegionDrawable(Core.atlas.find("shar-"+mount.name+"-full"), Cicon.small.size);

                    if(i++ % 4 == 3) tt.row();
                }
                if(i % 4 != 0){
                    int remaining = 4 - (i % 4);
                    for(int j = 0; j < remaining; j++)
                        tt.image(Styles.black6).size(40);
                }
            }).top();
        }

        ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
        pane.setScrollingDisabled(true, false);
        pane.setScrollYForce(scrollPos);
        pane.update(() -> scrollPos = pane.getScrollY());

        pane.setOverscroll(false, false);
        table.add(pane).maxHeight(Scl.scl(40 * 5));
    }
}
