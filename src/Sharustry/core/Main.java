package Sharustry.core;

import Sharustry.content.STurretMounts;
import Sharustry.ui.SBar;
import Sharustry.world.blocks.defense.MountTurretType;
import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.*;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.entities.abilities.ShieldRegenFieldAbility;
import mindustry.entities.units.WeaponMount;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;

import Sharustry.content.ModLoader;
import mindustry.type.*;
import mindustry.ui.*;

import static arc.Core.scene;
import static mindustry.Vars.*;

public class Main extends Mod {
    public Main(){
        Events.on(ClientLoadEvent.class, e -> {
            new HudUi().addTable();

            for(MountTurretType mount : STurretMounts.mounttypes) mount.load();
        });

        Events.on(WorldLoadEvent.class, e -> {
            new HudUi().addTable();
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
