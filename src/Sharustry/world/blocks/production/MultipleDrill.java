package Sharustry.world.blocks.production;

import Sharustry.ui.*;
import arc.scene.ui.layout.*;
import mindustry.world.blocks.production.*;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class MultipleDrill extends Drill {
    protected Seq<Item> returnItems = new Seq<>();
    protected Seq<Integer> returnItemsAmount = new Seq<>();
    public float delay = 60;

    public MultipleDrill(String name){
        super(name);
    }


    @Override
    public void setBars() {
        super.setBars();
        removeBar("drillspeed");
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.drillSpeed);
        stats.add(Stat.drillSpeed, delay / 60, StatUnit.itemsSecond);
    }
    @Override
    protected void countOre(Tile tile){
        returnItems.clear();
        returnItemsAmount.clear();
        oreCount.clear();
        itemArray.clear();

        for(Tile other : tile.getLinkedTilesAs(this, tempTiles))
            if(canMine(other)) oreCount.increment(getDrop(other));

        for(Item item : oreCount.keys())
            itemArray.add(item);

        itemArray.sort((item1, item2) -> {
            int type = Boolean.compare(!item1.lowPriority, !item2.lowPriority);
            if(type != 0) return type;
            int amounts = Integer.compare(oreCount.get(item1, 0), oreCount.get(item2, 0));
            if(amounts != 0) return amounts;
            return Integer.compare(item1.id, item2.id);
        });

        if(itemArray.size == 0) return;

        itemArray.each(i->returnItemsAmount.add(oreCount.get(i)));
        itemArray.each(i->returnItems.add(i));
    }

    @Override
    public void drawPlanConfigTop(BuildPlan req, Eachable<BuildPlan> list){
        if(!req.worldContext) return;

        Tile tile = req.tile();
        if(tile == null) return;

        countOre(tile);
        if(returnItems.size == 0 || !drawMineItem) return;

        float time = Time.time * 0.01f;
        Tmp.c1.set(returnItems.get((int)time % returnItems.size).color).lerp(returnItems.get(((int)time + 1) % returnItems.size).color, time % 1f);
        Draw.color(Tmp.c1);
        Draw.rect(itemRegion, req.drawx(), req.drawy());
        Draw.color();

    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        if(isMultiblock()){
            for(Tile other : tile.getLinkedTilesAs(this, tempTiles))
                if(canMine(other)) return true;

            return false;
        }else return canMine(tile);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        Tile tile = world.tile(x, y);
        if(tile == null) return;

        countOre(tile);

        if(returnItems.size != 0) {

            if(!drawMineItem) {
                float timec = Time.time * 0.01f;
                Tmp.c1.set(returnItems.get((int) timec % returnItems.size).color).lerp(returnItems.get(((int) timec + 1) % returnItems.size).color, timec % 1f);
                Draw.color(Tmp.c1);
                Draw.rect(itemRegion, tile.worldx() + offset, tile.worldy() + offset);
            }

            for (int i = 0; i < returnItems.size; i++){
                int value = Math.max(1,Mathf.round((60 * returnItemsAmount.get(i) / (drillTime + hardnessDrillMultiplier * returnItems.get(i).hardness))));
                float width = drawPlaceText(Core.bundle.format("bar.drillspeeditem", value, returnItems.get(i).localizedName, (int)(delay/60)), x, y+i, valid);
                float dx = x * tilesize + offset - width / 2f - 4f, dy = (y+i) * tilesize + offset + size * tilesize / 2f + 5;
                Draw.mixcol(Color.darkGray, 1f);

                Draw.rect(returnItems.get(i).uiIcon, dx, dy - 1);
                Draw.reset();
                Draw.rect(returnItems.get(i).uiIcon, dx, dy);
            }
            Draw.color();

        }else{
            Tile to = tile.getLinkedTilesAs(this, tempTiles).find(t -> t.drop() != null && t.drop().hardness > tier);
            Item item = to == null ? null : to.drop();
            if(item != null){
                float width = drawPlaceText(Core.bundle.get("bar.drilltierreq")+" item: ", x, y, valid);
                float dx = x * tilesize + offset + width / 2f - 4f, dy = y * tilesize + offset + size * tilesize / 2f + 5;
                Draw.rect(item.uiIcon, dx, dy + 1);
                Draw.reset();
            }
        }
    }

    public class MultipleDrillBuild extends DrillBuild {
        protected Seq<Item> dominatedItems = new Seq<>();
        protected Seq<Integer> dominatedItemsAmount = new Seq<>();
        protected Seq<ItemStack> itemS = new Seq<>();

        void countOre(Tile tile){
            dominatedItems.clear();
            dominatedItemsAmount.clear();
            oreCount.clear();
            itemArray.clear();

            for(Tile other : tile.getLinkedTilesAs(this.block, tempTiles))
                if(canMine(other)) oreCount.increment(getDrop(other));

            for(Item item : oreCount.keys()) itemArray.add(item);

            itemArray.sort((item1, item2) -> {
                int type = Boolean.compare(!item1.lowPriority, !item2.lowPriority);
                if (type != 0) return type;
                int amounts = Integer.compare(oreCount.get(item1, 0), oreCount.get(item2, 0));
                if (amounts != 0) return amounts;
                return Integer.compare(item1.id, item2.id);
            });

            itemArray.each(i->dominatedItemsAmount.add(oreCount.get(i)));
            itemArray.each(i->dominatedItems.add(i));
        }

        public int totalOre(){
            int h = 0;
            for(int i : dominatedItemsAmount) h +=i;

            return h;
        }

        @Override
        public void drawSelect(){
            if(dominatedItems.size != 0){
                float dx = x - size * tilesize/2f, dy = y + size * tilesize/2f;
                Draw.mixcol(Color.darkGray, 1f);
                float time = Time.time * 0.01f;
                int h = 0;
                if(Draw.getColor().a > 0.5) Draw.getColor().a -= (time % 1f);
                else if(Draw.getColor().a <= 0.5) {
                    Draw.getColor().a += (time % 1f);
                    h = 1;
                }
                Draw.rect(dominatedItems.get(((int)time + h) % dominatedItems.size).uiIcon, dx, dy - 1);
                Draw.reset();
                Draw.rect(dominatedItems.get(((int)time + h) % dominatedItems.size).uiIcon, dx, dy);

            }
        }

        @Override
        public void displayBars(Table table){
            super.displayBars(table);

            Tile tile = world.tile((int) x / 8, (int) y / 8);
            itemS.clear();
            if (tile != null) {
                for(int i=0;i<dominatedItems.size;i++)
                    itemS.add(new ItemStack(dominatedItems.get(i),dominatedItemsAmount.get(i)));

                table.table(c -> {
                    int h = 0;

                    for(ItemStack stack : itemS){
                        int value = (Math.max(1, Mathf.round(((optionalEfficiency > 0 ? liquidBoostIntensity * efficiency() : efficiency()) * stack.amount * warmup) / (drillTime + hardnessDrillMultiplier * stack.item.hardness) * 60 * timeScale)));
                        c.add(new ReqImage(new SItemImage(stack.item.uiIcon, value, (stack.amount*100)/totalOre(),stack), ()->true)).left().padRight(8);
                        if(++h % 4 == 0) table.row();
                    }
                }).left();
            }
        }

        @Override
        public void onProximityUpdate(){
            countOre(tile);
        }

        public boolean isOre(){
            for(int i = 0; i < dominatedItemsAmount.size; i++){
                if(dominatedItemsAmount.get(i) > 0){
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean shouldConsume(){
            return enabled;
        }

        @Override
        public void updateTile(){
            if(dominatedItems.size == 0) return;

            if(timer(timerDump, dumpTime)) dominatedItems.each(this::dump);
            timeDrilled += warmup * delta();

            boolean h = false;
            for(int i=0;i<dominatedItems.size;i++)
                if(items.get(dominatedItems.get(i)) < itemCapacity) h = true;

            if(isOre() && h){

                if(canConsume()) {
                    float speed = 1f;
                    if (optionalEfficiency > 0) speed = liquidBoostIntensity;
                    speed *= efficiency();

                    lastDrillSpeed = (speed * totalOre() * warmup) / delay;
                    warmup = Mathf.lerpDelta(warmup, speed, warmupSpeed);

                    progress += Time.delta;
                    if (Mathf.chanceDelta(updateEffectChance * warmup))
                        updateEffect.at(x + Mathf.range(size * 2f), y + Mathf.range(size * 2f));
                }else{
                    lastDrillSpeed = 0f;
                    warmup = Mathf.lerpDelta(warmup, 0f, warmupSpeed);
                    return;
                }

                if(progress >= delay) {
                    for(int i=0;i<dominatedItems.size;i++)
                        if(items.get(dominatedItems.get(i))<itemCapacity)
                            for(int ii=0;ii<Math.max(1,Mathf.round(((optionalEfficiency > 0 ? liquidBoostIntensity*efficiency():efficiency()) * dominatedItemsAmount.get(i) * warmup) / (drillTime + hardnessDrillMultiplier * dominatedItems.get(i).hardness) * 60 * timeScale));ii++)
                                offload(dominatedItems.get(i));
                    progress = 0;

                    drillEffect.at(x + Mathf.range(size), y + Mathf.range(size), dominatedItems.peek().color);
                }
            }else{
                lastDrillSpeed = 0f;
                warmup = Mathf.lerpDelta(warmup, 0f, warmupSpeed);
            }
        }

        @Override
        public void draw(){
            float s = 0.3f;
            float ts = 0.6f;

            Draw.rect(region, x, y);
            super.drawCracks();

            if(drawRim){
                Draw.color(heatColor);
                Draw.alpha(warmup * ts * (1f - s + Mathf.absin(Time.time, 3f, s)));
                Draw.blend(Blending.additive);
                Draw.rect(rimRegion, x, y);
                Draw.blend();
                Draw.color();
            }

            Draw.rect(rotatorRegion, x, y, timeDrilled * rotateSpeed);

            Draw.rect(topRegion, x, y);

            if((dominatedItems.size != 0) && drawMineItem){
                float time = Time.time * 0.01f;
                Tmp.c1.set(dominatedItems.get((int)time % dominatedItems.size).color).lerp(dominatedItems.get(((int)time + 1) % dominatedItems.size).color, time % 1f);
                Draw.color(Tmp.c1);

                Draw.rect(itemRegion, x, y);
                Draw.color();
            }
        }
    }

}
