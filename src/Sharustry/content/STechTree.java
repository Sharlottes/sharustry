package Sharustry.content;

import static mindustry.content.Blocks.*;
import static mindustry.content.TechTree.*;
import static Sharustry.content.SBlocks.*;

public class STechTree {
    public static void load(){
        node(laserDrill, () -> {
            node(adaptDrill, () -> {
                node(multiDrill);
            });
        });

        node(titaniumWall, () -> {
            node(shieldWall);
        });

        node(segment, () -> {
            node(trinity);
        });

        node(hail, () -> {
            node(jumble);
        });

        node(lancer, () -> {
            node(balkan);
            node(conductron);
        });

        node(ripple, () -> {
            node(clinicus, () -> {
                node(asclepius);
            });
        });
    }
}