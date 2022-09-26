package sharustry;

import sharustry.content.*;
import sharustry.world.blocks.defense.turret.mounts.MountTurretType;
import arc.Events;
import mindustry.game.EventType.*;
import mindustry.mod.Mod;

public class Sharustry extends Mod {
    public Sharustry(){
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