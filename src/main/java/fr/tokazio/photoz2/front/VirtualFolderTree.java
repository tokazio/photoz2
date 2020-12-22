package fr.tokazio.photoz2.front;

import fr.tokazio.photoz2.back.VirtualFolder;
import fr.tokazio.photoz2.back.VirtualFolderSerializer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class VirtualFolderTree implements MouseListener {

    private final JPanel panel;
    private final List<VirtualFolder> selectables = new LinkedList<>();
    private final List<VirtualFolderTreeSelectionListener> treeSelectionListeners = new LinkedList<>();
    private int panelWidth = 1;//avoid /0
    private int panelHeight = 1;//avoid /0
    private int marginL = 5;
    private int decay = 20;
    private int rowH = 25;
    private int scrollY = 0;
    private VirtualFolder rootVirtualFolder;
    private VirtualFolder selected;
    private VirtualFolder dropping;

    public VirtualFolderTree() {
        this.panel = new JPanel() {

            @Override
            public void paintComponent(final Graphics g) {
                super.paintComponent(g);
                panelWidth = getWidth();
                panelHeight = getHeight();
                draw((Graphics2D) g);
            }
        };
        panel.setName(getClass().getSimpleName());
        panel.addMouseListener(this);
        panel.addPropertyChangeListener("dropping", evt -> {
            long x = (long) evt.getOldValue();
            long y = (long) evt.getNewValue();
            final Point p = new Point((int) x, (int) y);
            dropping = nodeAtPoint(p);
        });
    }

    public Component asComponent() {
        return this.panel;
    }

    private void draw(final Graphics2D g) {

        final long start = System.currentTimeMillis();

        //background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, panelWidth, panelHeight);

        int y = 0;
        drawSub(g, rootVirtualFolder, y, 0);
    }

    private void drawSub(final Graphics2D g, final VirtualFolder parent, final int y, final int level) {
        final String text = parent.getName();
        if (parent.equals(selected)) {
            g.setColor(new Color(32, 128, 255));
            g.fillRect(0, y, panelWidth, rowH);
        }
        if (parent.equals(dropping)) {
            g.setColor(new Color(32, 128, 255));
            g.drawRect(0, y, panelWidth, rowH);
        }
        g.setColor(Color.WHITE);
        g.drawString(text, marginL + decay * level, y + 17);
        for (VirtualFolder vf : parent.getChildren().all()) {
            drawSub(g, vf, y + rowH, level + 1);
        }
    }

    public VirtualFolder nodeAtPoint(final Point p) {
        final int id = p.y / rowH;
        System.out.println("Node at " + p + " is #" + id);
        if (id < 1 || id > selectables.size() - 1) { //root not selectable
            return null;
        }
        return selectables.get(id);
    }

    public void addToSelected(final VirtualFolder virtualFolder) {
        VirtualFolder parent = selected;
        if (parent == null) {
            parent = rootVirtualFolder;
        }
        parent.add(virtualFolder);
        selectables.clear();
        loadSub(rootVirtualFolder);
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
        panel.repaint();
        selectables.clear();
        loadSub(rootVirtualFolder);
    }

    private void loadSub(final VirtualFolder vf) {
        selectables.add(vf);
        for (VirtualFolder child : vf.getChildren().all()) {
            loadSub(child);
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

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
