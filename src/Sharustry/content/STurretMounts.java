package Sharustry.content;

import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.ctype.ContentList;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.gen.Sounds;
import Sharustry.world.blocks.defense.MultiTurretMount;
import mindustry.graphics.Pal;

import static Sharustry.content.SBullets.*;

public class STurretMounts implements ContentList {
    public static MultiTurretMount unoMount, hailMount, waveMount;


    @Override
    public void load() {

        unoMount = new MultiTurretMount("unoM",
            new LaserBulletType(140){{
                colors = new Color[]{Pal.lancerLaser.cpy().a(0.4f), Pal.lancerLaser, Color.white};
                hitEffect = Fx.hitLancer;
                despawnEffect = Fx.none;
                hitSize = 4;
                lifetime = 16f;
                drawSize = 400f;
                collidesAir = false;
                length = 173f;
            }}
        ){{
           ammoPerShot = 5;
           x = 2.75f;
           y = 2.75f;
           shootY = 13/4f;
           range = 9 * 8;
           title = "Uno";

            recoilAmount = 2f;
            reloadTime = 80f;
            cooldown = 0.03f;
            shootEffect = Fx.lancerLaserShoot;
            smokeEffect = Fx.none;
            chargeEffect = Fx.lancerLaserCharge;
            chargeBeginEffect = Fx.lancerLaserChargeBegin;
            chargeTime = 40f;
            chargeMaxDelay = 30f;
            chargeEffects = 7;
            targetAir = false;
            shootSound = Sounds.laser;
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
