package fr.tokazio.photoz2.back;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PictUtils {

    private PictUtils() {
        super();
    }

    public static Image getScaledImage(final Image srcImg, final int w, final int h) {
        final Dimension newDim = getScaledDimension(new Dimension(srcImg.getWidth(null), srcImg.getHeight(null)), new Dimension(w, h));
        final BufferedImage resizedImg = new BufferedImage((int) newDim.getWidth(), (int) newDim.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2 = resizedImg.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(srcImg, 0, 0, (int) newDim.getWidth(), (int) newDim.getHeight(), null);
        g2.dispose();
        return resizedImg;
    }

    public static Dimension getScaledDimension(final Dimension imgSize, final Dimension boundary) {
        int newWidth = imgSize.width;
        int newHeight = imgSize.height;
        if (imgSize.width > boundary.width) {
            newWidth = boundary.width;
            newHeight = (newWidth * imgSize.height) / imgSize.width;
        }
        if (newHeight > boundary.height) {
            newHeight = boundary.height;
            newWidth = (newHeight * imgSize.width) / imgSize.height;
        }
        return new Dimension(newWidth, newHeight);
    }
}
