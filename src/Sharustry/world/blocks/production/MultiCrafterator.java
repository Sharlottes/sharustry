package Sharustry.world.blocks.production;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.util.*;
import arc.scene.*;
import arc.scene.ui.layout.*;
import arc.math.geom.*;
import mindustry.gen.*;
import mindustry.type.*;
import multilib.Recipe.*;
import mindustry.ui.*;
import mindustry.ui.fragments.BlockInventoryFragment;

import static arc.Core.*;
import static mindustry.Vars.*;
import multilib.MultiCrafter;
import multilib.Recipe;

public class MultiCrafterator extends MultiCrafter {

    public MultiCrafterator(String name, Recipe[] recs){
        super(name, recs);
    }

    public MultiCrafterator(String name, int recLen){
        super(name, recLen);
    }


    public class MultiCrafteratorBuild extends MultiCrafterBuild {

        @Override
        public void draw() {
            Draw.rect(Core.atlas.find(name+"-bottom"), x, y);
            super.draw();
        }

        @Override
        public void displayConsumption(Table table){
            int recLen = recs.length;
            if(recLen <= 0) return;

            table.left();
            for(int i = 0; i < recLen; i++){
                ItemStack[] intputItemStacks = recs[i].input.items;
                LiquidStack[] inputLiquidStacks = recs[i].input.liquids;
                ItemStack[] outputItemStacks = recs[i].output.items;
                LiquidStack[] outputLiquidStacks = recs[i].output.liquids;

                for(ItemStack stack : intputItemStacks)
                    table.add(new ReqImage(new ItemImage(stack.item.icon(Cicon.medium), stack.amount), () -> items != null && items.has(stack.item, stack.amount))).size(8 * 4);

                for(LiquidStack stack : inputLiquidStacks)
                    table.add(new ReqImage(stack.liquid.icon(Cicon.medium), () -> liquids != null && liquids.get(stack.liquid) > stack.amount)).size(8 * 4);


                if(i < recLen - 1){
                    InputContents next = recs[i + 1].input;
                    table.row();
                }
            }
        }
    }
}
