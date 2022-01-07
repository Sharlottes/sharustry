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
import mindustry.gen.*;
import mindustry.content.*;

import Sharustry.entities.bullet.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;

import static Sharustry.content.SFx.missileDead;

public class SBullets implements ContentList{
    public static BulletType
            fossers, mountDriverBolt, force, assault, artilleryHealBig, artilleryHeal,
            jumbleBullet, miniSlag, miniWater, miniCryo, miniOil, miniAccelMissile, accelMissile, accelerMissile, accelBullet, trailBullet, testLaser;

    @Override
    public void load(){
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

            fragBulletType = Bullets.standardIncendiary;
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


        miniSlag = new LiquidBulletType(){{
            collidesAir = false;
            liquid = Liquids.slag;
            damage = 1;
            drag = 0.03f;
            puddleSize = 2;
            orbSize = 1;
        }};

        miniWater = new LiquidBulletType(){{
            collidesAir = false;
            liquid = Liquids.water;
            knockback = 0.25f;
            drag = 0.03f;
            puddleSize = 2;
            orbSize = 1;
        }};

        miniCryo = new LiquidBulletType(){{
            collidesAir = false;
            liquid = Liquids.cryofluid;
            drag = 0.03f;
            puddleSize = 2;
            orbSize = 1;
        }};

        miniOil = new LiquidBulletType(){{
            collidesAir = false;
            liquid = Liquids.oil;
            drag = 0.03f;
            puddleSize = 2;
            orbSize = 1;
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

        accelMissile = new AccelBulletType(2.5f, 25){{
            backColor = SPal.cryoium.cpy().mul(Items.titanium.color);
            frontColor = trailColor = SPal.cryoium;
            shrinkY = 0f;
            width = 4f;
            height = 16f;
            hitSound = Sounds.explosion;
            trailChance = 0.2f;
            lifetime = 47f;
            sprite = "bullet";
            pierce = true;
            pierceBuilding = true;
            pierceCap = 3;
            pierceDec = 0.5f;
            damageMultiplier = 1.3f;
            shootEffect = SFx.balkanShoot;
            despawnEffect = missileDead;
            hitEffect = missileDead;
        }};

        accelerMissile = new AccelBulletType(3f, 45){{
            backColor = SPal.cryoium.cpy().mul(Items.titanium.color);
            frontColor = trailColor = SPal.cryoium;
            shrinkY = 0f;
            width = 4f;
            height = 16f;
            hitSound = Sounds.explosion;
            trailChance = 0.2f;
            lifetime = 67f;
            sprite = "bullet";
            homing = true;
            pierce = true;
            pierceBuilding = true;
            pierceCap = 5;
            pierceDec = 0.75f;
            damageMultiplier = 1.5f;
            shootEffect = SFx.balkanShoot;
            despawnEffect = missileDead;
            hitEffect = missileDead;
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

        accelBullet = new BasicBulletType(4f, 75){{
            frontColor = trailColor = SPal.cryoium;
            width = 4f;
            height = 16f;
            hitSound = Sounds.explosion;
            trailChance = 0.2f;
            lifetime = 120f;
            sprite = "bullet";
            pierce = true;
            pierceBuilding = true;
            absorbable = false;
            pierceCap = 12;
            shootEffect = SFx.balkanShoot;
            despawnEffect = missileDead;
            hitEffect = missileDead;
        }
            @Override
            public void update(Bullet b){
                super.update(b);
                b.damage += Time.delta * 1.5f;
                ((BulletData)b.data).heat += Time.delta;
                if(((BulletData)b.data).heat > 2.5) {
                    ((BulletData)b.data).heat = 0f;
                    trailBullet.create(b, b.team, b.x, b.y, b.rotation(),  0f, 1);
                }
            }

            @Override
            public void init(Bullet b){
                b.data = new BulletData(Seq.with(new Trail(6), new Trail(3)), 0f);
            }

            @Override
            public void draw(Bullet b){
                drawTrail(b);
                Draw.color(Pal.lancerLaser);
                ((BulletData)b.data).trails.each(t->t.draw(this.frontColor, this.width));

                Drawf.tri(b.x, b.y, width, height, b.rotation());
                Drawf.tri(b.x, b.y, width, height/2, b.rotation()+180);
            }

            @Override
            public void hit(Bullet b, float x, float y) {
                super.hit(b, x, y);
                b.damage*=0.75f;
            }

            class BulletData {
                final Seq<Trail> trails;
                float heat;

                public BulletData(Seq<Trail> trails, float heat) {
                    this.trails = trails;
                    this.heat = heat;
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
