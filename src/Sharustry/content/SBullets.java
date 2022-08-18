package Sharustry.content;

import Sharustry.entities.bullet.construct.AssaultConstructBulletType;
import Sharustry.entities.bullet.construct.ForceShieldConstructBulletType;
import Sharustry.entities.bullet.construct.SupportConstructBulletType;
import Sharustry.graphics.SPal;
import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.MultiEffect;
import mindustry.gen.*;
import mindustry.content.*;

import Sharustry.entities.bullet.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;

import static Sharustry.content.SFx.missileDead;

public class SBullets {
    public static BulletType
            fossers, mountDriverBolt, force, assault, artilleryHealBig, artilleryHeal,
            jumbleBullet, miniAccelMissile, trailBullet, testLaser;

    public static void load(){
        mountDriverBolt = new MountDriverBolt();

        fossers = new ForceShieldConstructBulletType(){{
            speed = 3.25f;
            damage = 120;
            sprite = "shar-sMineMortar";
            skipCol = true;
            drag = 0.015f;

            lifetime = 10 * 60f;
            width = height = 10f;
            collidesTiles = false;
            splashDamageRadius = 35f * 0.75f;
            splashDamage = 12 * 8f;
            backColor = Color.orange;
            frontColor = Pal.accent;

            shieldColor = Color.valueOf("D6FFE4");
        }};

        artilleryHeal = new SupportConstructBulletType(3.25f, 50){{
            sprite = "shar-construct";
            drag = 0.0125f;

            fragSpacing *= 2f;
            tractForce = 8f;
            tractScaledForce = 7f;
            shootLength = 5f;
            pointBulletDamage = 9f;
            repairSpeed = 0.1f;
            repairRange = 45f;
            tractRange = 40f;
            pointRange = 35f;
            pointHitEffect = Fx.shootHeal;
            knockback = 2f;
            lifetime = 8 * 60f;
            width = height = 9f;
            collidesTiles = false;
            splashDamageRadius = 35f * 0.75f;
            splashDamage = 45f;
            fragBullets = 10;
            backColor = Pal.plastaniumBack;
            frontColor = Pal.plastaniumFront;
            mixColorFrom = Pal.plastanium.cpy().lerp(Pal.heal, 0.5f);
            mixColorTo = Pal.heal;
            fragBulletType = new LaserBoltBulletType(4.2f, 10){{
                lifetime = 25f;
                healPercent = 5.5f;
                collidesTeam = true;
                backColor = Pal.heal;
                frontColor = Color.white;
            }};
        }};

        force = new ForceShieldConstructBulletType(){{
            speed = 3.25f;
            damage = 120;
            sprite = "shar-sEclipseCore";
            skipCol = true;
            drag = 0.015f;

            lifetime = 10 * 60f;
            width = height = 10f;
            collidesTiles = false;
            splashDamageRadius = 35f * 0.75f;
            splashDamage = 12 * 8f;
            backColor = Color.orange;
            frontColor = Pal.accent;

            shieldColor = Color.valueOf("D6FFE4");
        }};

        assault = new AssaultConstructBulletType(3.75f, 200f){{
            sprite = "shar-sGemini";
            drag = 0.015f;

            fragBulletType = new BasicBulletType(3.2f, 16, "bullet"){{
                width = 10f;
                height = 12f;
                frontColor = Pal.lightishOrange;
                backColor = Pal.lightOrange;
                status = StatusEffects.burning;
                hitEffect = new MultiEffect(Fx.hitBulletSmall, Fx.fireHit);

                ammoMultiplier = 5;

                splashDamage = 10f;
                splashDamageRadius = 22f;

                makeFire = true;
                lifetime = 60f;
            }};
            lifetime = 12 * 60f;
            width = height = 17f;
            collidesTiles = false;
            splashDamageRadius = 35f * 0.75f;
            splashDamage = 12 * 8f;
            backColor = Color.orange;
            frontColor = Pal.accent;
            mixColorFrom = Pal.accent.cpy().lerp(Pal.lightOrange, 0.5f);
            mixColorTo = Pal.lightOrange;
        }};

        artilleryHealBig = new SupportConstructBulletType(4.25f, 150){{
            sprite = "shar-construct";
            drag = 0.0125f;

            despawnEffect = Fx.healWave;
            tractForce = 24f;
            tractScaledForce = 14f;
            shootLength = 5f;
            pointBulletDamage = 30f;
            repairSpeed = 0.35f;
            repairRange = 85f;
            tractRange = 80f;
            pointRange = 70f;
            pointHitEffect = Fx.shootHeal;
            knockback = 5f;
            lifetime = 15 * 60f;
            width = height = 13f;
            collidesTiles = false;
            splashDamageRadius = 35f * 0.75f;
            splashDamage = 45f;
            fragBullets = 10;
            backColor = Pal.plastaniumBack;
            frontColor = Pal.plastaniumFront;
            mixColorFrom = Pal.plastanium.cpy().lerp(Pal.heal, 0.5f);
            mixColorTo = Pal.heal;
            fragBulletType = new LaserBoltBulletType(5.2f, 5){{
                lifetime = 35f;
                healPercent = 5.5f;
                collidesTeam = true;
                backColor = Pal.heal;
                frontColor = Color.white;
            }};
        }};

        jumbleBullet = new BasicBulletType(){{
           ammoMultiplier = 3;
           speed = 2.5f;
           damage = 9;
           width = 5.5f;
           height = 7;
           lifetime = 60;
           shootEffect = Fx.shootSmall;
           smokeEffect = Fx.shootSmallSmoke;
        }};

        miniAccelMissile = new AccelBulletType(2f, 5){{
            backColor = SPal.cryoium.cpy().mul(Items.titanium.color);
            frontColor = trailColor = SPal.cryoium;
            shrinkY = 0f;
            width = 1.5f;
            height = 3.5f;
            hitSound = Sounds.explosion;
            trailChance = 0.45f;
            lifetime = 47f;
            sprite = "bullet";
            pierceCap = 2;
            homing = true;
        }};

        trailBullet = new EnergyBulletType(0, 0.5f){{
            lifetime = 120f;
            height = 15f;
            width = 24f;
            drag = 1;
            trailColor = SPal.cryoium;
            hitEffect = Fx.none;
            despawnEffect = Fx.none;
        }
            @Override
            public void draw(Bullet b) {
                drawTrail(b);
                Draw.color(trailColor.cpy().a(0.75f-b.fin()/2));

                for(int i : Mathf.signs) {
                    Drawf.tri(b.x, b.y, height*(1-b.fin()), width, b.rotation()+Mathf.lerp(120, 150, b.fin())*i);
                }
            }
        };


        testLaser = new ContinuousLaserBulletType(70){{
            length = 200f;
            hitEffect = Fx.hitMeltdown;
            hitColor = Pal.meltdownHit;
            drawSize = 420f;

            incendChance = 0.4f;
            incendSpread = 5f;
            incendAmount = 1;
        }};
    }
}
