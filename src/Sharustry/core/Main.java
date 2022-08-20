package Sharustry.core;

import Sharustry.content.*;
import Sharustry.world.blocks.defense.turret.mounts.MountTurretType;
import arc.Events;
import mindustry.game.EventType.*;
import mindustry.mod.Mod;

public class Main extends Mod {
    public Main(){
        Events.on(ContentInitEvent.class, e -> {
            for(MountTurretType mount : STurretMounts.mounttypes) mount.init();
        });

        Events.on(ClientLoadEvent.class, e -> {
            for(MountTurretType mount : STurretMounts.mounttypes) mount.load();
        });
    }

    @Override
    public void loadContent(){
        SBullets.load();
        SStatusEffects.load();
        STurretMounts.load();
        SBlocks.load();
        SUnitTypes.load();
        STechTree.load();
    }
}