package fr.tokazio.photoz2.front;

import fr.tokazio.photoz2.back.VirtualFolder;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.IOException;

public class DynamicTreeCellRenderer implements javax.swing.tree.TreeCellRenderer {


    private static final String RSS = "/";
    public static final ImageIcon COLLAPSED = loadIcon(RSS + "collapsed.png");
    public static final ImageIcon EXPANDED = loadIcon(RSS + "expanded.png");
    private static final ImageIcon TOUTES = loadIcon(RSS + "toutes.png");
    private static final ImageIcon UNE = loadIcon(RSS + "une.png");
    private static final ImageIcon FOLDER = loadIcon(RSS + "folder.png");
    private static final ImageIcon PETIT = loadIcon(RSS + "petit.png");
    private static final ImageIcon GRAND = loadIcon(RSS + "grand.png");


    private static ImageIcon loadIcon(String str) {
        try {
            return new ImageIcon(ImageIO.read(DynamicTree.class.getResourceAsStream(str)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        final JLabel l = new JLabel();
        l.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        final VirtualFolder f = (VirtualFolder) node.getUserObject();
        l.setText(f.getName());
        if (!f.hasAParent()) {
            l.setIcon(TOUTES);
        } else if (f.getChildCount() > 0) {
            l.setIcon(FOLDER);
        } else {
            l.setIcon(UNE);
        }
        l.setFont(tree.getFont().deriveFont(Font.PLAIN));
        l.setForeground(Color.WHITE);
        JPanel panel = new JPanel(new GridLayout(1, 0)) {
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                size.width = Short.MAX_VALUE;
                size.height = 40;
                return size;
            }

            @Override
            public void setBounds(final int x, final int y, final int width, final int height) {
                super.setBounds(x, y, Math.min(200 - x, width), height + 10);
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
        if (selected) {
            panel.setBackground(new Color(32, 128, 255));
        } else {
            panel.setBackground(null);
        }
        panel.add(l);
        return panel;
    }
}
