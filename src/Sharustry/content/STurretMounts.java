package Sharustry.content;

import mindustry.ctype.ContentList;
import mindustry.gen.Sounds;
import Sharustry.world.blocks.defense.MultiTurretMount;

import static Sharustry.content.SBullets.*;

public class STurretMounts implements ContentList {
    public static MultiTurretMount unoMount, hailMount, waveMount;


    @Override
    public void load() {
        unoMount = new MultiTurretMount("unoM", unoBullet){{
           reloadTime = 15;
           ammoPerShot = 5;
           x = 2.75f;
           y = 2.75f;
           shootY = 13/4f;
           recoilAmount = 1;
           range = 9 * 8;
           title = "Uno";
        }};

        hailMount = new MultiTurretMount("hailM", hailBullet){{
            targetAir = false;
            reloadTime = 60;
            ammoPerShot = 20;
            x = -3.75f;
            y = -4f;
            shootY = 18/4f;
            recoilAmount = 2.5f;
            range = 18 * 8;
            title = "Mini Hail";
            shootSound = Sounds.bang;
        }};

        waveMount = new MultiTurretMount("waveM", miniSlag){{
           targetAir = false;
           reloadTime = 3;
           x = 4.25f;
           y = -3.5f;
           shootY = 16/4f;
           recoilAmount = 1;
           range = 13 * 8;
           title = "Mini Wave";
           shootSound = Sounds.none;
           loopSound = Sounds.spray;
        }};
    }
}
