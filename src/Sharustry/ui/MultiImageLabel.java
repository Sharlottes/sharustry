package Sharustry.ui;

import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Stack;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.ui.ReqImage;

public class MultiImageLabel extends Stack {
    final Seq<Image> displays = new Seq<>();
    final Seq<Label> labels = new Seq<>();
    float time;

    public MultiImageLabel() { }
    public void add(TextureRegion image, String label) {
        add(new Image(image), new Label(label));
    }
    public void add(Image display, Label label) {
        displays.add(display);
        labels.add(label);
        super.add(display);
        super.add(label);
    }

    public void act(float delta) {
        super.act(delta);
        time += Time.delta / 60.0F;
        displays.each((req) -> req.visible = false);
        if (displays.size > 0) {
            displays.get((int)time % displays.size).visible = true;
            labels.get((int)time % displays.size).visible = true;
        }
    }
}
