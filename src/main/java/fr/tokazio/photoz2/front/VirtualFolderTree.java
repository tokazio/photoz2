package fr.tokazio.photoz2.front;

import fr.tokazio.photoz2.back.VirtualFolder;
import fr.tokazio.photoz2.back.VirtualFolderSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class VirtualFolderTree implements MouseListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualFolderTree.class);

    private static final String RSS = "/";
    private static final ImageIcon TOUTES = UIUtil.loadIcon(RSS + "toutes.png");
    private static final ImageIcon UNE = UIUtil.loadIcon(RSS + "une.png");
    private static final ImageIcon FOLDER = UIUtil.loadIcon(RSS + "folder.png");

    private static final int MARGIN_L = 5;
    private static final int DECAY = 20;
    private static final int ROW_H = 25;

    private final JPanel panel;
    private final List<VirtualFolder> selectables = new LinkedList<>();
    private final List<VirtualFolderTreeSelectionListener> treeSelectionListeners = new LinkedList<>();
    private int panelWidth = 1;//avoid /0
    private int panelHeight = 1;//avoid /0
    private VirtualFolder rootVirtualFolder;
    private VirtualFolder selected;
    private VirtualFolder dropping;

    private final ScrollBar scrollBar;
    private int y;

    public Component asComponent() {
        return this.panel;
    }

    public VirtualFolderTree() {
        this.panel = new JPanel() {

            @Override
            public void paintComponent(final Graphics g) {
                super.paintComponent(g);
                panelWidth = getWidth();
                panelHeight = getHeight();
                draw(UIUtil.antialias(g));
            }
        };
        scrollBar = new ScrollBar(panel);
        panel.setName(getClass().getSimpleName());
        panel.addMouseListener(this);
        panel.addPropertyChangeListener("dropping-begin", evt -> {
            long xEvt = (long) evt.getOldValue();
            long yEvt = (long) evt.getNewValue();
            final Point p = new Point((int) xEvt, (int) yEvt);
            dropping = nodeAtPoint(p);
            panel.repaint();
        });
        panel.addPropertyChangeListener("dropping-end", evt -> {
            dropping = null;
            panel.repaint();
        });

    }

    private void draw(final Graphics2D g) {
        final long start = System.currentTimeMillis();
        //background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, panelWidth, panelHeight);

        scrollBar.defineElements(selectables.size(), ROW_H);

        y = scrollBar.scrollY();
        //nodes
        drawSub(g, rootVirtualFolder, 0);
        //scrollbar
        scrollBar.draw(g);
        final long end = System.currentTimeMillis();
        //le dessin ne devrait pas prendre plus de 33ms (30/sec)
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(" Drawn {} nodes in {}ms", selectables.size(), (end - start));
        }
    }

    private void drawSub(final Graphics2D g, final VirtualFolder parent, final int level) {
        final String text = parent.getName();
        if (parent.equals(selected)) {
            g.setColor(UIUtil.blue());
            g.fillRect(0, y, panelWidth, ROW_H);
        }
        if (parent.equals(dropping)) {
            g.setColor(UIUtil.blue());
            g.drawRect(0, y, panelWidth - 2, ROW_H);
        }

        if (!parent.hasAParent()) {
            g.drawImage(TOUTES.getImage(), MARGIN_L + DECAY * level, y + 5, null);
        } else if (parent.getChildCount() > 0) {
            g.drawImage(FOLDER.getImage(), MARGIN_L + DECAY * level, y + 5, null);
        } else {
            g.drawImage(UNE.getImage(), MARGIN_L + DECAY * level, y + 7, null);
        }

        g.setColor(Color.WHITE);
        g.drawString(text, MARGIN_L + DECAY * level + 25, y + 17);
        y += ROW_H;
        for (VirtualFolder vf : parent.getChildren().all()) {
            drawSub(g, vf, level + 1);
        }
    }

    public VirtualFolder nodeAtPoint(final Point p) {
        if (p.getX() < panelWidth - scrollBar.getWidth()) {
            final int id = (p.y - scrollBar.scrollY()) / ROW_H;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Node at {} is #{}", p, id);
            }
            if (id < 1 || id > selectables.size() - 1) { //root not selectable
                return null;
            }
            return selectables.get(id);
        }
        return null;//on scrollbar
    }

    public void addToSelected(final VirtualFolder virtualFolder) {
        VirtualFolder parent = selected;
        if (parent == null) {
            parent = rootVirtualFolder;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Add {} to {}", virtualFolder.getName(), parent.getName());
        }
        parent.add(virtualFolder);
        selectables.clear();
        buildSelectables(rootVirtualFolder);
        panel.repaint();
    }

    public void save(final String filename) throws IOException {
        VirtualFolderSerializer.getInstance().save(rootVirtualFolder, filename);
    }

    public void addSelectionListener(final VirtualFolderTreeSelectionListener treeSelectionListener) {
        if (treeSelectionListener != null) {
            treeSelectionListeners.add(treeSelectionListener);
        }
    }

    public void load(final String filename) throws IOException {
        rootVirtualFolder = VirtualFolderSerializer.getInstance().load(filename);
        selectables.clear();
        buildSelectables(rootVirtualFolder);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Nodes are {}", selectables);
        }
        panel.repaint();
    }

    private void buildSelectables(final VirtualFolder vf) {
        selectables.add(vf);
        for (VirtualFolder child : vf.getChildren().all()) {
            buildSelectables(child);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        selected = nodeAtPoint(e.getPoint());
        panel.repaint();
        for (VirtualFolderTreeSelectionListener l : treeSelectionListeners) {
            l.valueChanged(selected);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //not used
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //not used
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //not used
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //not used
    }

    public void resized() {
        panel.repaint();
        scrollBar.resized();
    }

    public VirtualFolder getSelected() {
        return selected;
    }

    public boolean remove(final VirtualFolder vf) {
        return vf.getParent() != null && vf.getParent().remove(vf);
    }
}
