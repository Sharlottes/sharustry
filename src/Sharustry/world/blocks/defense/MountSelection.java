package Sharustry.world.blocks.defense;

import arc.Core;
import arc.func.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.gen.Tex;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import mindustry.ui.*;

import static mindustry.Vars.*;

public class MountSelection {
    private static float scrollPos = 0f;

    public static void buildTable(Table table, Prov<MountTurretType> holder, Cons<MountTurretType> consumer, boolean closeSelect, Seq<MountTurretType> mounts){
        ButtonGroup<ImageButton> group = new ButtonGroup<>();
        group.setMinCheckCount(0);
        Table cont = new Table();
        cont.defaults().size(40);

        int i = 0;

        for(int h = 0; h < mounts.size; h++){
            ImageButton button = cont.button(Tex.whiteui, Styles.clearToggleTransi, 24, () -> {
                if(closeSelect) control.input.frag.config.hideConfig();
            }).group(group).get();
            final int j = h;
            button.changed(() -> consumer.get(button.isChecked() ? mounts.get(j) : null));
            button.getStyle().imageUp = new TextureRegionDrawable(Core.atlas.find("shar-"+mounts.get(h).name+"-full"), Cicon.small.size);
            button.update(() -> button.setChecked(holder.get() == mounts.get(j)));

            if(i++ % 4 == 3){
                cont.row();
            }
        }

        //add extra blank spaces so it looks nice
        if(i % 4 != 0){
            int remaining = 4 - (i % 4);
            for(int j = 0; j < remaining; j++){
                cont.image(Styles.black6);
            }
        }

        ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
        pane.setScrollingDisabled(true, false);
        pane.setScrollYForce(scrollPos);
        pane.update(() -> scrollPos = pane.getScrollY());

        pane.setOverscroll(false, false);
        table.add(pane).maxHeight(Scl.scl(40 * 5));
    }
}
