package Sharustry.world.blocks.defense;

import mindustry.entities.Predict;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Posc;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import arc.math.*;
import mindustry.entities.*;

public class SharTurret extends ItemTurret {
    public SharTurret(String name){
        super(name);
    }

    public class SharTurretBuild extends ItemTurretBuild {
        @Override
        public BulletType peekAmmo(){
            if(ammo.size == 0) return null;
            return ammo.peek().type();
        }

        @Override
        public void targetPosition(Posc pos){
            if(!hasAmmo() || pos == null) return;
            BulletType bullet = peekAmmo();
            if(bullet == null) return;
            float speed = bullet.speed;
            //slow bullets never intersect
            if(speed < 0.1f) speed = 9999999f;

            targetPos.set(Predict.intercept(this, pos, speed));
            if(targetPos.isZero()){
                targetPos.set(pos);
            }
        }

        @Override
        protected void updateShooting(){
            if(reload >= reloadTime){
                BulletType type = peekAmmo();
                if(type == null) return;
                shoot(type);

                reload = 0f;
            }else{
                BulletType type = peekAmmo();
                if(type == null) return;
                reload += delta() * type.reloadMultiplier * baseReloadSpeed();
            }
        }

        @Override
        protected void effects(){
            BulletType type = peekAmmo();
            if(type == null){
                shootEffect.at(x + tr.x, y + tr.y, rotation);
                smokeEffect.at(x + tr.x, y + tr.y, rotation);
            }
            else{
                peekAmmo().shootEffect.at(x + tr.x, y + tr.y, rotation);
                peekAmmo().smokeEffect.at(x + tr.x, y + tr.y, rotation);
            }

            shootSound.at(x + tr.x, y + tr.y, Mathf.random(0.9f, 1.1f));

            if(shootShake > 0){
                Effect.shake(shootShake, shootShake, this);
            }

            recoil = recoilAmount;
        }

        @Override
        public BulletType useAmmo(){
            if(cheating() && peekAmmo() != null) return peekAmmo();

            AmmoEntry entry = ammo.peek();
            entry.amount -= ammoPerShot;
            if(entry.amount <= 0) ammo.pop();
            totalAmmo -= ammoPerShot;
            totalAmmo = Math.max(totalAmmo, 0);
            ejectEffects();
            return entry.type();
        }
    }
}
