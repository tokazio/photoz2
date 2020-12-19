package fr.tokazio.photoz2;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PictUtils {

    private PictUtils() {
        super();
    }

    public static Image getScaledImage(final Image srcImg, final int w, final int h) {
        Dimension newDim = getScaledDimension(new Dimension(srcImg.getWidth(null), srcImg.getHeight(null)), new Dimension(w, h));
        BufferedImage resizedImg = new BufferedImage((int) newDim.getWidth(), (int) newDim.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(srcImg, 0, 0, (int) newDim.getWidth(), (int) newDim.getHeight(), null);
        g2.dispose();
        return resizedImg;
    }

    public static Dimension getScaledDimension(final Dimension imgSize, final Dimension boundary) {
        int new_width = imgSize.width;
        int new_height = imgSize.height;
        if (imgSize.width > boundary.width) {
            new_width = boundary.width;
            new_height = (new_width * imgSize.height) / imgSize.width;
        }
        if (new_height > boundary.height) {
            new_height = boundary.height;
            new_width = (new_height * imgSize.width) / imgSize.height;
        }
        return new Dimension(new_width, new_height);
    }
}
