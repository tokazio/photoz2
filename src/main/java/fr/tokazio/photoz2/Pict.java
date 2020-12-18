package fr.tokazio.photoz2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Pict {

    private final File file;
    private Image image;
    private boolean loaded;

    public Pict(final File file) {
        this.file = file;
    }

    public void load() {
        if (!loaded) {

            //TODO swingworker
            //panel.repaint() quand fini -> loadedListener

            try {
                image = ImageIO.read(file);
            } catch (IOException e) {
                e.printStackTrace();
                //replace by an error image
            } finally {
                loaded = true;
            }
        }
    }

    public Image asImage() {
        return image;
    }
}
