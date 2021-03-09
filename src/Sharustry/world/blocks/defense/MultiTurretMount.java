package Sharustry.world.blocks.defense;

import arc.audio.Sound;
import arc.func.Func2;
import arc.graphics.Color;
import arc.struct.Seq;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.*;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;

public class MultiTurretMount {
    public int shots = 1;
    public int ammoPerShot = 2;

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
    public float maxAmmo = 20;
    public float range = 80;
    public float rotateSpeed = 5;
    public float inaccuracy = 0;
    public float velocityInaccuracy = 0;
    public float shootCone = 8;

    public float recoilAmount = 1;
    public float restitution = 0.02f;
    public float cooldown = 0.02f;

    public float ejectX = 1;
    public float ejectY = -1;
    public float loopVolume = 1;
    public float shootShake = 0;

    public float minRange = 0;
    public float barrels = 1;
    public float barrelSpacing = 0;
    public float spread = 0;
    public float burstSpacing = 0;

    public float powerUse = 0f; //mountType: power only

    public boolean altEject = true;
    public boolean ejectRight = true;
    public boolean sequential = false;
    public boolean extinguish = false; //whether can shoot into fire. == targetFire
    public boolean targetAir = true;
    public boolean targetGround = true;

    public BulletType bullet;
    public Color heatColor = Pal.turretHeat;

    public String name;
    public String title = "ohno";
    public String icon = "error";

    public Sound shootSound = Sounds.pew;
    public Sound loopSound = Sounds.none;

    public Effect shootEffect = Fx.none;
    public Effect smokeEffect = Fx.none;
    public Effect coolEffect = Fx.fuelburn;
    public Effect ejectEffect = Fx.none;

    public MultiTurretMountType mountType; //power, item, liquid, tract
    public Units.Sortf unitSort = Unit::dst2;

    //charge region
    public float chargeTime = -1;
    public float shootLength = 8;
    public float chargeEffects = 5;
    public float chargeMaxDelay = 48;
    public Effect chargeEffect = Fx.none, chargeBeginEffect = Fx.none;
    public Sound chargeSound = Sounds.none;
    //region end

    //tract region, mountType: tract only
    public float laserWidth = 0.6f;
    public float force = 0.3f;
    public float scaledForce = 0f;
    public float damage = 0f;
    public float statusDuration = 300;
    public float shootSoundVolume = 0.9f;

    public Color laserColor = Color.white;
    public StatusEffect status = StatusEffects.none;
    //region end

    //skill
    public Seq<Integer> skillDelays = new Seq<>();
    public Seq<Func2<Building, MultiTurretMount, Runnable>> skillSeq = new Seq<>();

    public MultiTurretMount(String name) {
        this.name = name;
    }

    public MultiTurretMount(String name, BulletType bullet){
        this.name = name;
        this.bullet = bullet;
    }

    public <T1 extends Building, T2 extends MultiTurretMount> void addSkills(Func2<T1, T2, Runnable> skill, int delay){
        if(skill != null) {
            skillSeq.add((Func2<Building, MultiTurretMount, Runnable>) skill);
            skillDelays.add(delay);
        }
    }


    public enum MultiTurretMountType {
        item, liquid, power, tract
    }
}