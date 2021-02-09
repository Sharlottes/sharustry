package Sharustry.ui;

import arc.graphics.g2d.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.core.*;
import mindustry.type.*;
import mindustry.ui.Cicon;
import mindustry.ui.Styles;

public class SItemImage extends Stack{

    public SItemImage(TextureRegion region, int value, int value2, ItemStack stack){

        add(new Table(o -> {
            o.left();
            o.add(new Image(region)).size(32f);
        }));

        add(new Table(t -> {
            t.left().bottom();
            t.add(stack.amount > 1000 ? UI.formatAmount(value2) : value2 + "%");
            t.pack();
        }));
        add(new Table(h -> {
            h.right().top();
            h.add(stack.amount > 1000 ? UI.formatAmount(value) : value + "").fontScale(0.8f);
            h.pack();
        }));
    }

    public SItemImage(TextureRegion region, int amount){

        add(new Table(o -> {
            o.left();
            o.add(new Image(region)).size(32f);
        }));

        add(new Table(t -> {
            t.left().bottom();
            t.add(amount > 1000 ? UI.formatAmount(amount) : amount + "");
            t.pack();
        }));
    }

    public SItemImage(TextureRegion region){
        Table t = new Table().left().bottom();

        add(new Image(region));
        add(t);
    }

    public SItemImage(ItemStack stack){

        add(new Table(o -> {
            o.left();
            o.add(new Image(stack.item.icon(Cicon.medium))).size(32f);
        }));

        if(stack.amount != 0){
            add(new Table(t -> {
                t.left().bottom();
                t.add(stack.amount > 1000 ? UI.formatAmount(stack.amount) : stack.amount + "").style(Styles.outlineLabel);
                t.pack();
            }));
        }
    }
}
