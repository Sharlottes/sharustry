package Sharustry.core;

import Sharustry.content.STurretMounts;
import Sharustry.world.blocks.defense.turret.MountTurretType;
import arc.Events;
import mindustry.game.EventType.*;
import mindustry.mod.Mod;

import Sharustry.content.ModLoader;

public class Main extends Mod {
    public Main(){
        Events.on(ClientLoadEvent.class, e -> {
            for(MountTurretType mount : STurretMounts.mounttypes) mount.load();
        });

        Events.on(WorldLoadEvent.class, e -> {
        });
    }

    @Override
    public void init(){
    }

    @Override
    public void loadContent(){
        new ModLoader().load();
    }


}
