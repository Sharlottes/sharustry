package Sharustry.content;

import arc.graphics.Color;
import mindustry.ctype.*;
import mindustry.type.*;


public class SItems implements ContentList{
    public static Item coreResource;

    @Override
    public void load() {
        coreResource = new Item("core-resource", Color.valueOf("d99d73")){{
            cost = 100;
        }};
    }
}
