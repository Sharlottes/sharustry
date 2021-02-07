package Sharustry.world.blocks.production;

import arc.*;
import arc.graphics.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.production.*;
import mindustry.world.meta.*;
import mindustry.world.meta.values.*;

import static mindustry.Vars.world;

public class AttributeDrill extends Drill{
    @Nullable public Attribute defaultAttribute;
    public float boostScale = 1;
    public float baseEfficiency = 1;
    public float maxHeatBoost = 1;

    public AttributeDrill(String name){ super(name);}

    @Override
    public void setBars(){
        super.setBars();

        bars.add("attribute", (AttributeDrillBuild e) ->
            new Bar(() -> Core.bundle.format("bar.attribute", (int)((baseEfficiency + Math.min(maxHeatBoost, boostScale * sumAttribute(e.attribute, e.tile.x, e.tile.y))) * 100f), e.attributeStr), () -> e.attributeColor, () -> (baseEfficiency + Math.min(maxHeatBoost, boostScale * sumAttribute(e.attribute, e.tile.x, e.tile.y)))/maxHeatBoost)
        );
    }



    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.affinities, table -> {
            for(Attribute attr : Attribute.all){
                if(attr==Attribute.heat) {table.row();}
                table.add("[lightgray] "+ attr +": []").left();
                for (Block block : Vars.content.blocks()
                        .select(block -> (block instanceof Floor) && ((Floor) block).attributes.get(attr) != 0 && !(((Floor) block).isLiquid && !floating))
                        .<Floor>as().with(s -> s.sort(f -> f.attributes.get(attr)))) {
                    new FloorEfficiencyValue(((Floor) block), ((Floor) block).attributes.get(attr) * boostScale, false).display(table);
                }
                table.row();
            }
        });
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x,y,rotation,valid);
        int oss = 0;
        Tile tile = world.tile(x, y);
        if(tile == null) return;
        Tile to = tile.getLinkedTilesAs(this, tempTiles).find(t -> t.drop() != null && t.drop().hardness > tier);
        Item item = to == null ? null : to.drop();
        if(!(returnItem == null && item == null)) oss = 1;
        int i = 0;
        for(Attribute attr : Attribute.all) {
            int i1 = i++;
            drawPlaceText(Core.bundle.format("bar.attribute", (int) ((baseEfficiency + Math.min(maxHeatBoost, boostScale * sumAttribute(attr, x, y))) * 100f), attr), x, y + i1 + oss, valid);
        }
    }

    public class AttributeDrillBuild extends DrillBuild {
        public float attrsum;
        public Attribute attribute;
        public Color attributeColor = Color.darkGray;
        public String attributeStr = "none";
        short config;
        public void created(){
            attribute = defaultAttribute;
        }

        @Override
        public void updateTile(){
            super.updateTile();
            if(attribute == Attribute.oil){
                attributeColor = Liquids.oil.barColor;
                attributeStr = "oil";
            }
            else if(attribute == Attribute.heat){
                attributeColor = Pal.lightOrange;
                attributeStr = "heat";
            }
            else if(attribute == Attribute.spores){
                attributeColor = Pal.sap;
                attributeStr = "spores";
            }
            else if(attribute == Attribute.water){
                attributeColor = Liquids.water.color;
                attributeStr = "water";
            }
            else if(attribute == Attribute.light) {
                attributeColor = Color.lightGray;
                attributeStr = "light";
            }

            attrsum = sumAttribute(attribute, tile.x, tile.y)<0?-baseEfficiency:sumAttribute(attribute, tile.x, tile.y);
        }
        @Override
        public float efficiency(){
            return (baseEfficiency + Math.min(maxHeatBoost, boostScale * attrsum)) * super.efficiency();
        }

        @Override
        public Attribute config(){
            return attribute;
        }

        public void buildConfiguration(Table table){
            super.buildConfiguration(table);

            table.button(Icon.star, () -> {
                attribute = Attribute.oil;
                config = 1;
            }).size(40);
            table.button(Icon.star, () -> {
                attribute = Attribute.heat;
                config = 2;
            }).size(40);
            table.button(Icon.star, () -> {
                attribute = Attribute.spores;
                config = 3;
            }).size(40);
            table.button(Icon.star, () -> {
                attribute = Attribute.water;
                config = 4;
            }).size(40);
            table.button(Icon.star, () -> {
                attribute = Attribute.light;
                config = 5;
            }).size(40);
            this.configure(attribute);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.s(config);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            short config = read.s();
            attribute = config==1?Attribute.oil:config==2?Attribute.heat:config==3?Attribute.spores:config==4?Attribute.water:Attribute.light;
        }
    }
}

