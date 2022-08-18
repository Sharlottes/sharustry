package Sharustry.content;

import arc.struct.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.game.Objectives.*;
import mindustry.type.*;

import static mindustry.content.Blocks.*;
import static mindustry.content.TechTree.*;
import static Sharustry.content.SBlocks.*;

public class STechTree {
    static TechTree.TechNode context = null;

    public static void load(){
        margeNode(laserDrill, () -> {
            node(adaptDrill, () -> {
                node(multiDrill);
            });
        });

        margeNode(titaniumWall, () -> {
            node(shieldWall);
        });

        margeNode(coreFoundation, () -> {
            node(armedFoundation);
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
        context = TechTree.all.find(t -> t.content == parent);
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
}