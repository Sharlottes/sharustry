package Sharustry.content;

import arc.struct.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.game.Objectives.*;
import mindustry.type.*;

import static mindustry.content.Blocks.*;
import static mindustry.content.TechTree.*;
import static Sharustry.content.SBlocks.*;

public class STechTree implements ContentList{
    static TechTree.TechNode context = null;

    @Override
    public void load(){
        margeNode(laserDrill, () -> {
            node(adaptDrill, () -> {
                node(multiDrill);
            });
        });

        margeNode(multiPress, () -> {
            node(multi);
        });

        margeNode(titaniumWall, () -> {
            node(shieldWall);
        });

        margeNode(coreNucleus, () -> {
            node(armedNucleus);
        });

        margeNode(segment, () -> {
            node(trinity);
        });

        margeNode(hail, () -> {
            node(jumble);
        });

        margeNode(lancer, () -> {
            node(balkan);
            node(conductron);
        });

        margeNode(ripple, () -> {
            node(clinicus, () -> {
                node(asclepius);
            });
        });
    }
    private static void margeNode(UnlockableContent parent, Runnable children){
        TechNode parnode = TechTree.all.find(t -> t.content == parent);
        context = parnode;
        children.run();
    }

    private static void node(UnlockableContent content, ItemStack[] requirements, Seq<Objective> objectives, Runnable children){
        TechNode node = new TechNode(context, content, requirements);
        if(objectives != null) node.objectives = objectives;

        TechNode prev = context;
        context = node;
        children.run();
        context = prev;
    }

    private static void node(UnlockableContent content, ItemStack[] requirements, Runnable children){
        node(content, requirements, null, children);
    }

    private static void node(UnlockableContent content, Seq<Objective> objectives, Runnable children){
        node(content, content.researchRequirements(), objectives, children);
    }

    private static void node(UnlockableContent content, Runnable children){
        node(content, content.researchRequirements(), children);
    }

    private static void node(UnlockableContent block){
        node(block, () -> {});
    }

    private static void nodeProduce(UnlockableContent content, Seq<Objective> objectives, Runnable children){
        node(content, content.researchRequirements(), objectives.and(new Produce(content)), children);
    }

    private static void nodeProduce(UnlockableContent content, Runnable children){
        nodeProduce(content, Seq.with(), children);
    }

    private static void nodeProduce(UnlockableContent content){
        nodeProduce(content, Seq.with(), () -> {});
    }
}