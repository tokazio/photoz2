
package fr.tokazio.photoz2.front;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * @author rpetit
 */
public class UIUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(UIUtil.class);

    private static final Dimension SCREEN = Toolkit.getDefaultToolkit().getScreenSize();
    public static final Color BLUE = new Color(32, 128, 255);
    public static final Color RED = new Color(145, 0, 0);
    public static final Color GREEN = new Color(0, 145, 0);

    private UIUtil() {
        //hide
    }

    public static int getScreenWidth() {
        return (int) SCREEN.getWidth();
    }

    public static int getScreenHeight() {
        return (int) SCREEN.getHeight();
    }

    public static Dimension getScreenSize() {
        return SCREEN.getSize();
    }

    public static Graphics2D antialias(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2d;
    }

    public static ImageIcon loadIcon(String str) {
        try {
            return new ImageIcon(ImageIO.read(UIUtil.class.getResourceAsStream(str)));
        } catch (IOException e) {
            LOGGER.error("Error loading icon {}", str, e);
        }
        return null;
    }

    public static Color green() {
        return GREEN;
    }

    public static Color red() {
        return RED;
    }

    public static Font getFont(int size) {
        //TODO cache
        return new Font("Verdana", Font.PLAIN, size);
    }

    public static Color blue() {
        return BLUE;
    }
}
