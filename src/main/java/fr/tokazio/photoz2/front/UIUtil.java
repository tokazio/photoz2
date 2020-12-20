
package fr.tokazio.photoz2.front;

import javax.swing.*;
import java.awt.*;

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
}
