package Sharustry.content;

import mindustry.ctype.*;



public class ModLoader implements ContentList{
    private final ContentList[] contents = {
        new SItems(),
        new SStatusEffects(),
        new SLiquids(),
        new SBullets(),
        new SUnitTypes(),
        new STurretMounts(),
        new SBlocks(),
        new STechTree()
    };

    public void load(){
        for(ContentList list : contents){
            list.load();
        }
    }
}
