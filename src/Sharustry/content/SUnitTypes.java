package Sharustry.content;

import Sharustry.ai.types.TractorAI;
import Sharustry.type.TractorUnitType;
import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.ctype.ContentList;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.LaserBoltBulletType;
import mindustry.gen.*;
import mindustry.type.AmmoTypes;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

import static Sharustry.graphics.SPal.paradium;

public class SUnitTypes implements ContentList {
    public static UnitType momo, monopoly;

    @Override
    public void load() {
        momo = new TractorUnitType("momo"){{
            defaultController = TractorAI::new;
            flying = true;
            drag = 0.06f;
            accel = 0.12f;
            speed = 1.5f;
            health = 100 * 1.5f;
            engineSize = 1.8f;
            engineOffset = 5f;
            range = 10 * 8f;
            isCounted = false;

            ammoType = AmmoTypes.powerLow;

            mineTier = 1;
            mineSpeed = 2.5f;

            constructor = UnitEntity::create;
        }};

        monopoly = new TractorUnitType("monopoly"){{
            defaultController = TractorAI::new;

            tractRange = 8 * 30f;
            tractForce = 6f;
            tractScaledForce = 5f;

            flying = true;
            drag = 0.05f;
            speed = 2.6f;
            rotateSpeed = 15f;
            accel = 0.1f;
            range = 30 * 8f;
            health = 400 * 1.5f;
            buildSpeed = 0.5f;
            engineOffset = 6.5f;
            hitSize = 8f;
            lowAltitude = true;

            ammoType = AmmoTypes.power;

            mineTier = 2;
            mineSpeed = 3.5f;

            damage = 0.1f;
            BulletType beam = new LaserBoltBulletType(5.8f, 7){{
                lifetime = 48f;
                backColor = paradium;
                frontColor = Color.white;

                smokeEffect = SFx.hitLaserS;
                hitEffect = SFx.hitLaserS;
                despawnEffect = SFx.hitLaserS;

                width *= 0.75f;
                height *= 0.75f;
            }};

            weapons.add(
                new Weapon(name + "-weapon-mount"){{
                    top = false;
                    shootY = 2f;
                    x = 2f;
                    y = 0.5f;
                    reload = 30f;
                    ejectEffect = Fx.none;
                    recoil = 2f;
                    shootSound = Sounds.lasershoot;
                    alternate = false;
                    shotDelay = 0.1f * 60;
                    inaccuracy = 15f;
                    shots = 2;
                    bullet = beam;
                }},
                new Weapon(name + "-weapon"){{
                    top = false;
                    x = 3.5f;
                    y = -2.5f;
                    reload = 30f;
                    ejectEffect = Fx.none;
                    recoil = 2f;
                    shootSound = Sounds.lasershoot;
                    alternate = false;
                    shotDelay = 0.1f * 60;
                    inaccuracy = 15f;
                    shots = 2;
                    bullet = beam;
                }}
            );

            constructor = UnitEntity::create;
        }};
    }
}
