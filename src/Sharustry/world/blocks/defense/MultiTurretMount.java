package Sharustry.world.blocks.defense;

import arc.audio.Sound;
import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;

public class MultiTurretMount {
    public float x = 0;
    public float y = 0;
    public float shootX = 0;
    public float xRand = 0;
    public float shootY = 0;
    public float yRand = 0;
    public float width = 3;
    public float height = 3;
    public float elevation = 1;

    public float reloadTime = 30;
    public BulletType bullet;
    public float ammoPerShot = 1;
    public float range = 80;
    public float rotateSpeed = 5;
    public float inaccuracy = 0;
    public float velocityInaccuracy = 0;
    public float shootCone = 8;
    public boolean targetAir = true;
    public boolean targetGround = true;

    public float recoilAmount = 1;
    public float restitution = 0.02f;
    public Color heatColor = Pal.turretHeat;
    public float cooldown = 0.02f;

    public String name;
    public String title = "ohno";
    public String icon = "error";

    public Effect shootEffect = Fx.none;
    public Effect smokeEffect = Fx.none;
    public Effect coolEffect = Fx.fuelburn;
    public Effect ejectEffect = Fx.none;
    public float ejectX = 1;
    public float ejectY = -1;
    public boolean altEject = true;
    public boolean ejectRight = true;
    public Sound shootSound = Sounds.pew;
    public Sound loopSound = Sounds.none;
    public float loopVolume = 1;
    public float shootShake = 0;

    public float minRange = 0;
    public int shots = 1;
    public float barrels = 1;
    public float barrelSpacing = 0;
    public boolean sequential = false;
    public float spread = 0;
    public float burstSpacing = 0;

    public Units.Sortf unitSort = Unit::dst2;

    public MultiTurretMount(String name, BulletType bullet){
        this.name = name;
        this.bullet = bullet;
    }
}