package sharustry.graphics;

import arc.func.Cons;
import mindustry.entities.Effect;

public class ScaledEffect extends Effect {
    public float scl;
    public ScaledEffect(float life, float clipsize, float scl, Cons<EffectContainer> renderer){
        super(life, clipsize, renderer);
        this.scl = scl;
    }
}
