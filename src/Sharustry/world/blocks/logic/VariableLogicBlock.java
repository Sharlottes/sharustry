package Sharustry.world.blocks.logic;


import arc.func.*;
import arc.graphics.Color;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.core.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.logic.LAssembler.*;
import mindustry.logic.LExecutor.*;
import mindustry.world.blocks.logic.LogicBlock;

import static mindustry.Vars.*;
public class VariableLogicBlock extends LogicBlock {
    public float maxInstructionsPerTick = 2000;
    public VariableLogicBlock(String name){
        super(name);

        //config(Float.class, (VariableLogicBuild tile, Float ipt) -> tile.instructionsPerTicks = ipt);
    }

    public class VariableLogicBuild extends LogicBuild {
        public float instructionsPerTicks;

        @Override
        public void created() {
            instructionsPerTicks = instructionsPerTick;
            super.created();
        }

        @Override
        public void buildConfiguration(Table table) {
            table.add(new Stack(){{
                add(new Table(tt -> {
                    tt.top();
                    Slider slide = new Slider(0, maxInstructionsPerTick, 1f, false);
                    slide.setValue(instructionsPerTicks);
                    slide.moved(i -> instructionsPerTicks = i);
                    tt.add(slide).width(block.size * 3f - 20).padTop(4f);
                    tt.pack();
                }));
                add(new Table(tt -> {
                    tt.top();
                    Label label = new Label(() -> {
                        Color col = Color.white.cpy().lerp(Color.valueOf("ff0000"), instructionsPerTicks / maxInstructionsPerTick);
                        return "[#" + col.toString() + "]" + instructionsPerTicks + "";
                    });
                    tt.add(label);
                }));
            }}).padTop(5).growX().top();
            super.buildConfiguration(table);
        }

        @Override
        public void updateTile(){
            executor.team = team;

            if(!checkedDuplicates){
                checkedDuplicates = true;
                IntSet removal = new IntSet();
                Seq<LogicLink> removeLinks = new Seq<>();
                for(LogicLink link : links){
                    Building build = world.build(link.x, link.y);
                    if(build != null){
                        if(!removal.add(build.id)){
                            removeLinks.add(link);
                        }
                    }
                }
                links.removeAll(removeLinks);
            }

            //check for previously invalid links to add after configuration
            boolean changed = false, updates = true;

            while(updates){
                updates = false;

                for(int i = 0; i < links.size; i++){
                    LogicLink l = links.get(i);

                    if(!l.active) continue;

                    boolean valid = validLink(world.build(l.x, l.y));
                    if(valid != l.valid){
                        changed = true;
                        l.valid = valid;
                        if(valid){
                            Building lbuild = world.build(l.x, l.y);

                            //this prevents conflicts
                            l.name = "";
                            //finds a new matching name after toggling
                            l.name = findLinkName(lbuild.block);

                            //remove redundant links
                            links.removeAll(o -> world.build(o.x, o.y) == lbuild && o != l);

                            //break to prevent concurrent modification
                            updates = true;
                            break;
                        }
                    }
                }
            }

            if(changed){
                updateCode(code, true, null);
            }

            if(enabled){
                accumulator += edelta() * instructionsPerTicks * (consValid() ? 1 : 0);

                if(accumulator > maxInstructionScale * instructionsPerTicks) accumulator = maxInstructionScale * instructionsPerTicks;

                for(int i = 0; i < (int)accumulator; i++){
                    if(executor.initialized()){
                        executor.runOnce();
                    }
                    accumulator --;
                }
            }
        }

        @Override
        public void updateCode(String str, boolean keep, Cons<LAssembler> assemble){
            if(str != null){
                code = str;

                try{
                    //create assembler to store extra variables
                    LAssembler asm = new LAssembler();
                    Seq<LStatement> st = LAssembler.read(str);
                    asm.instructions = st.map(l -> l.build(asm)).filter(l -> l != null).toArray(LInstruction.class);

                    //store connections
                    for(LogicLink link : links){
                        if(link.active && (link.valid = validLink(world.build(link.x, link.y)))){
                            asm.putConst(link.name, world.build(link.x, link.y));
                        }
                    }

                    //store link objects
                    executor.links = new Building[links.count(l -> l.valid && l.active)];
                    executor.linkIds.clear();

                    int index = 0;
                    for(LogicLink link : links){
                        if(link.active && link.valid){
                            Building build = world.build(link.x, link.y);
                            executor.links[index ++] = build;
                            if(build != null) executor.linkIds.add(build.id);
                        }
                    }

                    asm.putConst("@mapw", world.width());
                    asm.putConst("@maph", world.height());
                    asm.putConst("@links", executor.links.length);
                    asm.putConst("@ipt", instructionsPerTicks);

                    if(keep){
                        //store any older variables
                        for(Var var : executor.vars){
                            boolean unit = var.name.equals("@unit");
                            if(!var.constant || unit){
                                BVar dest = asm.getVar(var.name);
                                if(dest != null && (!dest.constant || unit)){
                                    dest.value = var.isobj ? var.objval : var.numval;
                                }
                            }
                        }
                    }

                    //inject any extra variables
                    if(assemble != null){
                        assemble.get(asm);
                    }

                    asm.getVar("@this").value = this;
                    asm.putConst("@thisx", World.conv(x));
                    asm.putConst("@thisy", World.conv(y));

                    executor.load(asm);
                }catch(Exception e){
                    Log.err("Failed to compile logic program @", code);
                    Log.err(e);

                    //handle malformed code and replace it with nothing
                    LAssembler asm = new LAssembler();
                    Seq<LStatement> st = LAssembler.read(str);
                    asm.instructions = st.map(l -> l.build(asm)).filter(l -> l != null).toArray(LInstruction.class);
                    executor.load(asm);
                }
            }
        }

        @Override
        public void write(Writes write) {
            write.f(instructionsPerTicks);
            super.write(write);
        }

        @Override
        public void read(Reads read, byte revision) {
            instructionsPerTicks = read.f();
            super.read(read, revision);
        }
    }
}
