package Sharustry.world.blocks.production;

import arc.*;
import arc.graphics.*;
import arc.math.Mathf;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.*;
import arc.struct.Seq;
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

    public float brightness = 0.9f;

    public AttributeDrill(String name){
        super(name);

        config(Attribute.class, (AttributeDrillBuild tile, Attribute attr) -> tile.attribute = attr);
    }

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
        public Color attributeColor;
        public String attributeStr;
        public Seq<Boolean> attributeClicked = new Seq<>();

        public int color = Pal.accent.rgba();
        public float smoothTime;

        public void created(){
            attribute = defaultAttribute;
            attributeStr = defaultAttribute.toString();
            attributeColor = Color.lightGray;

            for(Attribute attr : Attribute.all){
                attributeClicked.add(false);
            }
        }

        @Override
        public void updateTile(){
            super.updateTile();
            attrsum = sumAttribute(attribute, tile.x, tile.y)<0?-baseEfficiency:sumAttribute(attribute, tile.x, tile.y);

            if(attribute == Attribute.light) smoothTime = Mathf.lerpDelta(smoothTime, 1f, 0.02f);
            else smoothTime = Mathf.lerpDelta(smoothTime, 0, 0.02f);
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

            Log.info(attributeClicked.get(Attribute.water.ordinal()));
            table.button(new TextureRegionDrawable(Core.atlas.find("shar-status-tarred")).tint(attributeClicked.get(Attribute.oil.ordinal()) ? Color.valueOf("313131") : Color.white), 40, () -> {
                configure(Attribute.oil);
                for(Attribute attr : Attribute.all){
                    attributeClicked.set(attr.ordinal(), false);
                    if(attr == attribute) attributeClicked.set(attr.ordinal(), true);
                }
                table.clear();
                buildConfiguration(table);

                attributeColor = Color.valueOf("313131");
                attributeStr = "oil";
            }).size(40).color(Color.valueOf("313131"));
            table.button(new TextureRegionDrawable(Core.atlas.find("shar-status-burning")).tint(attributeClicked.get(Attribute.heat.ordinal()) ? Color.valueOf("ffc455") : Color.white), 40, () -> {
                configure(Attribute.heat);
                for(Attribute attr : Attribute.all){
                    attributeClicked.set(attr.ordinal(), false);
                    if(attr == attribute) attributeClicked.set(attr.ordinal(), true);
                }
                table.clear();
                buildConfiguration(table);

                attributeColor = Color.valueOf("ffc455");
                attributeStr = "heat";
            }).size(40).color(Color.valueOf("ffc455"));
            table.button(new TextureRegionDrawable(Core.atlas.find("shar-status-spore-slowed")).tint(attributeClicked.get(Attribute.spores.ordinal()) ? Pal.spore : Color.white), 40, () -> {
                configure(Attribute.spores);
                for(Attribute attr : Attribute.all){
                    attributeClicked.set(attr.ordinal(), false);
                    if(attr == attribute) attributeClicked.set(attr.ordinal(), true);
                }
                table.clear();
                buildConfiguration(table);

                attributeColor = Pal.spore;
                attributeStr = "spores";
            }).size(40).color(Pal.spore);
            table.button(new TextureRegionDrawable(Core.atlas.find("shar-status-wet")).tint(attributeClicked.get(Attribute.water.ordinal()) ? Color.royal : Color.white), 40, () -> {
                configure(Attribute.water);
                for(Attribute attr : Attribute.all){
                    attributeClicked.set(attr.ordinal(), false);
                    if(attr == attribute) attributeClicked.set(attr.ordinal(), true);
                }
                table.clear();
                buildConfiguration(table);

                attributeColor = Color.royal;
                attributeStr = "water";
            }).size(40).color(Color.royal);
            table.button(new TextureRegionDrawable(Core.atlas.find("shar-status-blasted")).tint(attributeClicked.get(Attribute.light.ordinal()) ? Color.lightGray : Color.white), 40, () -> {
                configure(Attribute.light);
                for(Attribute attr : Attribute.all){
                    attributeClicked.set(attr.ordinal(), false);
                    if(attr == attribute) attributeClicked.set(attr.ordinal(), true);
                }
                table.clear();
                buildConfiguration(table);

                attributeColor = Color.lightGray;
                attributeStr = "light";
            }).size(40).color(Color.lightGray);
        }

        @Override
        public void drawLight(){
            Drawf.light(team, x, y, (30f + Mathf.absin(10f, 7f)) * smoothTime * block.size, Tmp.c1.set(color), brightness * super.efficiency());
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(attribute.ordinal());
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            attribute = Attribute.all[read.i()];

            if(attribute == Attribute.oil){
                attributeColor = Color.valueOf("313131");
                attributeStr = "oil";
            }
            else if(attribute == Attribute.heat){
                attributeColor = Color.valueOf("ffc455");
                attributeStr = "heat";
            }
            else if(attribute == Attribute.spores){
                attributeColor = Pal.spore;
                attributeStr = "spores";
            }
            else if(attribute == Attribute.water){
                attributeColor = Color.royal;
                attributeStr = "water";
            }
            else if(attribute == Attribute.light) {
                attributeColor = Color.lightGray;
                attributeStr = "light";
            }
        }
    }
}

