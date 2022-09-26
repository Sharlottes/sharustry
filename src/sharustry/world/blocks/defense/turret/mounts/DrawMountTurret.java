package sharustry.world.blocks.defense.turret.mounts;

import arc.Core;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.part.DrawPart;
import mindustry.graphics.*;
import mindustry.type.Liquid;

public class DrawMountTurret {
    public Seq<DrawPart> parts = new Seq<>();
    /** Prefix to use when loading base region. */
    public String basePrefix = "";
    /** Overrides the liquid to draw in the liquid region. */
    public @Nullable Liquid liquidDraw;
    public TextureRegion liquid, top, heat, preview, outline, mask;

    public DrawMountTurret(String basePrefix){
        this.basePrefix = basePrefix;
    }

    public DrawMountTurret(){
    }

    public void draw(MountTurretType.MountTurret mount){
        Draw.color();

        Draw.z(Layer.turret + 5 - 0.5f);
        Drawf.shadow(mask, mount.x + mount.recoilOffset.x - mount.type.elevation, mount.y + mount.recoilOffset.y - mount.type.elevation, mount.drawrot());

        Draw.z(Layer.turret + 5);
        drawTurret(mount);
        drawHeat(mount);

        if(outline.found()){
            //draw outline under everything when parts are involved
            Draw.z(Layer.turret + 5 - 0.01f);
            Draw.rect(outline, mount.x + mount.recoilOffset.x, mount.y + mount.recoilOffset.y, mount.drawrot());
            Draw.z(Layer.turret + 5);
        }
        if(parts.size > 0){
            float progress = mount.progress();

            //TODO no smooth reload
            var params = DrawPart.params.set(mount.warmup(), 1f - progress, 1f - progress, mount.heat, mount.curRecoil, mount.charge, mount.x + mount.recoilOffset.x, mount.y + mount.recoilOffset.y, mount.rotation);

            for(var part : parts){
                part.draw(params);
            }
        }
    }

    public void drawTurret(MountTurretType.MountTurret mount){
        Draw.rect(mount.type.region, mount.x + mount.recoilOffset.x, mount.y + mount.recoilOffset.y, mount.drawrot());

        if(liquid.found()){
            Liquid toDraw = liquidDraw == null ? mount.build.liquids.current() : liquidDraw;
            Drawf.liquid(liquid, mount.x + mount.recoilOffset.x, mount.y + mount.recoilOffset.y, mount.build.liquids.get(toDraw) / mount.block.liquidCapacity, toDraw.color.write(Tmp.c1).a(1f), mount.drawrot());
        }

        if(top.found()){
            Draw.rect(top, mount.x + mount.recoilOffset.x, mount.y + mount.recoilOffset.y, mount.drawrot());
        }
    }

    public void drawSelect(MountTurretType.MountTurret mount) {
        float fade = 
            Mathf.curve(Time.time % mount.block.totalRangeTime, 
                mount.block.rangeTime * mount.mountIndex, 
                mount.block.rangeTime * mount.mountIndex + mount.block.fadeTime) - 
            Mathf.curve(Time.time % mount.block.totalRangeTime, 
                mount.block.rangeTime * (mount.mountIndex + 1) - mount.block.fadeTime, 
                mount.block.rangeTime * (mount.mountIndex + 1));
        Lines.stroke(3, Pal.gray);
        Draw.alpha(fade);

        Lines.dashCircle(mount.x, mount.y, mount.type.range);
        Lines.stroke(1, mount.canHeal() ? Pal.heal : mount.build.team.color);
        Draw.alpha(fade);
        Lines.dashCircle(mount.x, mount.y, mount.type.range);
        Draw.z(Layer.turret + 5 + 1);
        Draw.color(mount.build.team.color, fade);
        Draw.rect(mask, mount.x, mount.y, mount.drawrot());
    }
    public void drawHeat(MountTurretType.MountTurret mount) {
        if(mount.heat <= 0.00001f || !heat.found()) return;

        Drawf.additive(heat, mount.type.heatColor.write(Tmp.c1).a(mount.heat), mount.x + mount.recoilOffset.x, mount.y + mount.recoilOffset.y, mount.drawrot(), Layer.turretHeat);
    }

    /**
     * 텍스쳐를 다른 색으로 채워 만듭니다
     * @param target 채울 텍스쳐
     * @param name 새 텍스쳐 이름
     * @param color 채울 색
     * @param stroke 두깨
     * @return 주어진 색으로 채워진 새로운 텍스쳐
     */
    TextureAtlas.AtlasRegion fillRegion(TextureRegion target, String name, int color, int stroke) {
        PixmapRegion pixmapRegion = Core.atlas.getPixmap(target);
        Pixmap pixmap = pixmapRegion.crop();
        Log.info("[Sharustry] generating regions..., width: " + pixmap.width + ", height: " + pixmap.height);

        //주변이 유색일 경우 색칠
        pixmap.each((x, y) -> {
            for(int sx = -stroke/2; sx <= stroke/2; sx++) {
                for(int sy = -stroke/2; sy <= stroke/2; sy++) {
                    if(pixmap.in(x + sx, y + sy) && (pixmapRegion.get(x + sx, y + sy) & 0x000000ff) != 0) {
                        pixmap.setRaw(x, y, color);
                        return;
                    }
                }
            }
        });

        /* //may not need to save as file?
        Fi pixFile = new Fi("sharustry/mounts/"+name+".png", Files.FileType.local);
        PixmapIO.writePng(pixFile, pixmap);
        Log.info("[Sharustry] generating successfully done, saved on: "+pixFile.absolutePath()+"\n----------------------------------------");
         */

        return Core.atlas.addRegion(name, new TextureRegion(new Texture(pixmap)));
    }
    /** Load any relevant texture regions. */
    public void load(MountTurretType type){
        preview = Core.atlas.find("shar-" + type.name + "-preview", type.region);
        liquid = Core.atlas.find("shar-" + type.name + "-liquid");
        top = Core.atlas.find("shar-" + type.name + "-top");
        heat = Core.atlas.find("shar-" + type.name + "-heat");

        Log.info("[Sharustry] generating " + type.title + " outline regions...");
        outline = fillRegion(type.region, "shar-" + type.name + "-ooutline", 0x585a61ff, 4);

        Log.info("[Sharustry] generating " + type.title + " mask regions...");
        mask = fillRegion(type.region, "shar-" + type.name + "-mmask", 0xffffffff, 0);


        for(var part : parts){
            part.turretShading = true;
            part.load("shar-" + type.name);
        }
    }
}
