package Sharustry.core;

import arc.Events;
import mindustry.game.EventType.*;
import mindustry.mod.Mod;

import Sharustry.content.ModLoader;

@SuppressWarnings("unused")
public class Main extends Mod {
    public Main(){
        Events.on(ClientLoadEvent.class, e -> {

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
