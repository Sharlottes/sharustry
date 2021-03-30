package Sharustry.world.blocks.defense;

import arc.Core;
import arc.func.*;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.Seq;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Tex;
import mindustry.ui.*;
import static mindustry.Vars.control;

public class ConstructSelection {
    private static float scrollPos = 0f;

    public static void buildTable(Table table, Prov<BulletType> holder, Cons<BulletType> consumer, boolean closeSelect, Seq<BulletType> constructs){
        ButtonGroup<ImageButton> group = new ButtonGroup<>();
        group.setMinCheckCount(0);
        Table cont = new Table();
        cont.defaults().size(40);

        int i = 0;

        for(BulletType construct : constructs){
            ImageButton button = cont.button(Tex.whiteui, Styles.clearToggleTransi, 24, () -> {
                if(closeSelect) control.input.frag.config.hideConfig();
            }).group(group).get();
            button.changed(() -> consumer.get(button.isChecked() ? construct : null));
            button.getStyle().imageUp = new TextureRegionDrawable(Core.atlas.find("shar-construct"), Cicon.small.size);
            button.update(() -> button.setChecked(holder.get() == construct));

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
