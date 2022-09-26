package sharustry.ui;

import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;

public class MultiImageLabel extends Table {
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

    @Override
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
