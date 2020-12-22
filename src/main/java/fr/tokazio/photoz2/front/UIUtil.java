
package fr.tokazio.photoz2.front;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * @author rpetit
 */
public class UIUtil {

    private static final Dimension SCREEN = Toolkit.getDefaultToolkit().getScreenSize();

    private UIUtil() {
        //hide
    }

    public static void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
        for (int i = startingIndex; i < rowCount; ++i) {
            tree.expandRow(i);
        }
        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
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
            e.printStackTrace();
        }
        return null;
    }
}
