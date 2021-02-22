package Sharustry.world.blocks.defense;

import arc.audio.Sound;
import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.entities.*;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.*;
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
    public int ammoPerShot = 2;
    public float maxAmmo = 20;
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

    public float chargeTime = -1;
    public float shootLength = 8;
    public float chargeEffects = 5;
    public float chargeMaxDelay = 48;
    public Effect chargeEffect = Fx.none, chargeBeginEffect = Fx.none;

    public boolean extinguish = false;
    public MountAmmoType ammoType;
    public Sound chargeSound = Sounds.none;

    public float powerUse = 0f;

    public Units.Sortf unitSort = Unit::dst2;
    public MultiTurretMount(String name) {
        this.name = name;
    }
    public MultiTurretMount(String name, BulletType bullet){
        this.name = name;
        this.bullet = bullet;
    }

    public enum MountAmmoType {
        item, liquid, power
    }
}