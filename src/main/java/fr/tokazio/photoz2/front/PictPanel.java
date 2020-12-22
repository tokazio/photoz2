package fr.tokazio.photoz2.front;

import fr.tokazio.photoz2.Config;
import fr.tokazio.photoz2.OS;
import fr.tokazio.photoz2.back.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.List;

public class PictPanel implements MouseListener, MouseWheelListener, MouseMotionListener {

    private final JPanel panel;

    private VirtualFolder virtualFolder = new VirtualFolder("", "");

    private final List<PictLoadingListener> pictLoadingListeners = new LinkedList<>();


    private final List<DropListener<VirtualFolder>> dropListeners = new LinkedList<>();
    private int panelWidth = 1;//avoid /0
    private int panelHeight = 1;//avoid /0
    private final Selection selection = new Selection();
    //TODO tester skija avec opengl
    int rowMargin = 20;//espace entre les lignes
    private final Selection draggingSelection = new Selection();
    int marginL = 20;//marge à gauche
    int marginR = 20;//marge à droite
    boolean firstLoad = true;
    long lastLoadAt;
    long endScrollAt;
    int nbX = 4;
    int colMargin = 20;//espace entre les colonnes
    private Point pressedAt;
    private Point rectTo;
    private Point scrollFrom;

    private PictPanelListener listener;
    private Point draggingFrom;

    private Point dragTo;

    private int scrollY = 0;
    private int maxY = 0;

    public PictPanel() {
        this.panel = new JPanel() {

            @Override
            public void paintComponent(final Graphics g) {
                super.paintComponent(g);
                panelWidth = getWidth();
                panelHeight = getHeight();

                draw(UIUtil.antialias(g));
                if (firstLoad) {
                    load();
                }
            }
        };

        this.panel.addMouseListener(this);
        this.panel.addMouseWheelListener(this);
        //TODO drag scroll bar
        this.panel.addMouseMotionListener(this);

        //detection de la fin du scroll pour ne pas maj trop souvent
        final Timer timer = new Timer(35, e -> {
            if (System.currentTimeMillis() - endScrollAt > 250 && endScrollAt > lastLoadAt) {
                load();
            }
        });
        timer.setRepeats(true);
        timer.start();

    }

    //Stop le chargement de toutes les images
    private void stopLoad() {
        virtualFolder.getPictures().stopLoading();
        System.out.println("Stoped loading");
    }

    private float nbRowInPanel() {
        return (float) Math.ceil((panelHeight - rowMargin * 2) / (float) w());
    }

    private int firstRowInPanel() {
        return (Math.abs(scrollY)) / (w() + rowMargin);
    }

    //Lance le chargement des images visibles dans le panel
    private void load() {
        firstLoad = false;

        int nbY = (int) nbRowInPanel();
        System.out.println(nbX + " col(s), " + nbY + " line(s)");

        //id de la 1ère ligne à dessiner
        int startAtRow = firstRowInPanel();

        //nb de photos affichées
        int nbShow = nbY * nbX;

        int imgStart = startAtRow * nbX;
        int imgEnd = imgStart + nbShow - 1;// pictList.size() - 1;

        //Comme on calcul à la ligne, il se peut que la dernière ne soit pas complète
        if (imgEnd > virtualFolder.getPictures().size() - 1) {
            imgEnd = virtualFolder.getPictures().size() - 1;
        }

        doLoad(imgStart, imgEnd);
    }

    private int w() {
        final int t = panelWidth - marginL - marginR;
        final int w = t / nbX;
        return w - colMargin;
    }

    private void doLoad(int imgStart, int imgEnd) {
        lastLoadAt = System.currentTimeMillis();
        System.out.println("Load from " + imgStart + " to " + imgEnd);
        boolean fired = false;
        for (int i = imgStart; i <= imgEnd; i++) {
            virtualFolder.getPictures().load(i, w(), w());
            if (!fired) {
                fired = true;
                firePendingChanged();
            }
        }
    }

    public Component asComponent() {
        return this.panel;
    }

    public PictPanel setListener(final PictPanelListener listener) {
        this.listener = listener;
        return this;
    }

    private void draw(final Graphics2D g) {

        final long start = System.currentTimeMillis();

        //background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, panelWidth, panelHeight);

        //Selection rect background
        final Rectangle selectionRect = computeSelectionRect();
        if (pressedAt != null && rectTo != null) {
            g.setColor(new Color(148, 180, 255));
            g.fillRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
        }

        int colMargin = marginL;
        int x = colMargin;

        int w = w();

        //position y de départ du dessin de la grille
        int y = scrollY + rowMargin;

        //pour simplification on part toujours de la 1ère image, le must serait de ne s'occuper que de celle à afficher vraiment
        //on s'arrête quand même avant de calculer/afficher celles qui sont trop basse
        int nbVraimentDessinee = 0;
        int i = 0;
        while (y < panelHeight && i < virtualFolder.getPictures().size()) {
            final Id id = new Id(i);
            PictLoader pictLoader = virtualFolder.getPictures().get(i);
            final boolean isDragging = selection.contains(id) && draggingFrom != null;

            //passage à la ligne, revient à la 1ère colonne
            if (x + w > panelWidth) {
                x = marginL;
                y += w + rowMargin;
            }

            //On commence vraiment à dessiner à partir de la ligne au dessus de la première
            //pour avoir le bas d'une éventuelle ligne précédente
            if (y > -w - rowMargin) {

                //default background color
                g.setColor(Color.LIGHT_GRAY);
                //background color when has error
                if (pictLoader.hasError()) {
                    g.setColor(Color.RED);
                }
                //background color when dragging
                if (isDragging) {
                    g.setColor(new Color(200, 200, 200));
                    g.setStroke(new DashedStroke(3));
                    g.drawRect(x, y, w, w);
                    g.setStroke(new BasicStroke(1));
                } else {
                    g.fillRect(x, y, w, w);
                }
                //Draw the real image
                if (!isDragging) {
                    if (pictLoader.asImage() != null) {
                        g.drawImage(pictLoader.asImage(), x, y, w, w, null);
                        g.setColor(Color.GRAY);
                        g.drawRect(x, y, w, w);
                        nbVraimentDessinee++;
                    }
                    g.setFont(g.getFont().deriveFont(10f));
                    if (!pictLoader.isLoaded() && pictLoader.getProgress() < 100) {
                        g.setColor(Color.GRAY);
                        g.fillRect(x, y, (int) (w * (pictLoader.getProgress() / 100)), w);
                        g.setColor(Color.DARK_GRAY);
                        String txt = (int) pictLoader.getProgress() + "%";
                        int tw = g.getFontMetrics().stringWidth(txt);
                        g.drawString(txt, x + ((w - tw) / 2f), y + ((w - 10) / 2f));
                    }
                    int tw = g.getFontMetrics().stringWidth(pictLoader.getExt());
                    g.setColor(Color.GRAY);
                    g.fillRect(x + w - tw - 6, y + w - 18, tw + 6, 18);
                    g.setColor(Color.WHITE);
                    g.drawString(pictLoader.getExt(), x + w - tw - 3, y + w - 5);
                    if (pictLoader.hasError()) {
                        String txt = pictLoader.getError().getMessage();
                        tw = g.getFontMetrics().stringWidth(txt);
                        g.setColor(Color.WHITE);
                        g.drawString(txt, x + ((w - tw) / 2f), y + ((w - 10) / 2f));
                    }
                    g.setColor(Color.MAGENTA);
                    if (Config.getInstance().debug()) {
                        g.drawString("#" + i + " (id=" + pictLoader.getId() + ")", x, y + 20);
                    }

                    //draw selected
                    if (selection.contains(id) || draggingSelection.contains(id)) {
                        g.setColor(new Color(32, 128, 255));
                        g.setStroke(new BasicStroke(3));
                        g.drawRect(x, y, w, (int) w);
                        g.setStroke(new BasicStroke(1));
                    }

                    //draw drag to bar before the image
                    if (id.asInt() == toListId(dragTo)) {
                        g.setColor(new Color(32, 128, 255));
                        g.fillRect(x - colMargin / 2 - 1, y, 3, w);
                    }
                }

            } else {
                virtualFolder.getPictures().unload(pictLoader);
            }
            //colonne suivante
            x += w + colMargin;
            i++;

            //Selection rect foreground
            if (pressedAt != null && rectTo != null) {
                g.setColor(new Color(32, 128, 255));
                g.setStroke(new BasicStroke(1));
                g.drawRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
            }
        }
        final long end = System.currentTimeMillis();
        //le dessin ne devrait pas prendre plus de 33ms (30/sec)
        System.out.println(System.currentTimeMillis() + "> Drawn " + nbVraimentDessinee + " images in " + (end - start) + "ms");

        //scroll

        //TODO si on est en bas et qu'on resize il faudrait repositionner les scrolls sinon c'est la merde à l'affichage

        int sw = 20;//scroll width
        int sx = panelWidth - sw;//scroll position x

        int totRow = (int) Math.ceil(virtualFolder.getPictures().size() / (float) nbX);//nombre total de ligne d'image dans la liste
        int totY = totRow * (w + rowMargin);//hauteur total en px
        maxY = totY - panelHeight;//hauteur max de scroll en px

        int pl = panelHeight - 2 * sw;//hauteur entre les buts up et down
        //hauteur de la barre
        int sh = (int) ((panelHeight / (float) totY) * pl);

        //position de la scrollbar
        int sp = (int) ((Math.abs(scrollY) / (float) totY) * pl);

        //background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(sx, 0, sw, panelHeight);

        //but up
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(sx, 0, sw, sw);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(sx, 0, sw, sw);

        //bar
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(sx, sw + 1 + sp, sw, sh);

        //but down
        g.fillRect(sx, panelHeight - sw, sw, sw);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(sx, panelHeight - sw, sw, sw);
    }

    private Rectangle computeSelectionRect() {
        if (pressedAt != null && rectTo != null) {
            int sx = pressedAt.x;
            if (rectTo.x < pressedAt.x) {
                sx = rectTo.x;
            }
            int sy = pressedAt.y;
            if (rectTo.y < pressedAt.y) {
                sy = rectTo.y;
            }
            final int sw = Math.abs(rectTo.x - pressedAt.x);
            final int sh = Math.abs(rectTo.y - pressedAt.y);
            return new Rectangle(sx, sy, sw, sh);
        }
        return new Rectangle(0, 0, 0, 0);
    }

    /*
    public void loadFiles(List<File> in) {
        int i = 0;
        for (File f : in) {
            pictLoaderList.add(new PictLoader(i++, f).addLoadedListener((p) -> {
                System.out.println("Loading ended for #" + p.getId());
                firePendingChanged();
                panel.repaint();
            }).addProgressListener((p, v) -> panel.repaint()));
        }
    }

     */

    public void loadVirtualFolder(final VirtualFolder virtualFolder) {
        firstLoad = true;
        if (virtualFolder.isFresh()) {
            for (PictLoader pl : virtualFolder.getPictures().all()) {
                pl.addLoadedListener(p -> {
                    System.out.println("Loading ended for #" + p.getId());
                    firePendingChanged();
                    panel.repaint();
                }).addProgressListener((p, v) -> {
                    panel.repaint();
                });
            }
            virtualFolder.setNotFresh();
        }
        this.virtualFolder = virtualFolder;
        firstLoad = true;
        load();
        scrollY = 0;
        panel.repaint();
    }

    public PictPanel addLoadingListener(final PictLoadingListener l) {
        if (l != null) {
            pictLoadingListeners.add(l);
        }
        return this;
    }

    private void firePendingChanged() {
        if (virtualFolder.getPictures().pendingCount() == 0) {
            System.out.println("No more pending");
            for (PictLoadingListener l : pictLoadingListeners) {
                l.onEnd();
            }
        } else {
            System.out.println("Pending " + virtualFolder.getPictures().pendingCount());
            for (PictLoadingListener l : pictLoadingListeners) {
                l.onStart();
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        //TODO selection
        if (e.getX() > panelWidth - 20) {
            if (e.getY() < 20) {
                scrollUp(20);
                endScrollAt = System.currentTimeMillis();
                panel.repaint();
                load();
            } else if (e.getY() > panelHeight - 20) {
                scrollDown(20);
                endScrollAt = System.currentTimeMillis();
                panel.repaint();
                load();
            }
        }

    }

    private int toListId(final Point gridPoint) {
        if (gridPoint == null) {
            return -1;
        }
        return gridPoint.y * nbX + gridPoint.x;
    }

    private Point toGrid(final Point pxPoint, final boolean withMargin) {
        int w = w();
        int xAt = ((int) pxPoint.getX() - marginL) / (w + colMargin);
        int l = marginL + xAt * (w + colMargin);
        int r = l + w;
        if (!withMargin && (pxPoint.getX() < l || pxPoint.getX() > r)) {
            xAt = -1;
        }

        int ptY = pxPoint.y - scrollY;
        int yAt = (ptY - rowMargin) / (w + rowMargin);
        int t = (rowMargin + yAt * (w + rowMargin));
        int b = t + w;
        if (!withMargin && (ptY < t || ptY > b)) {
            yAt = -1;
        }

        return new Point(xAt, yAt);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //scroll
        if (e.getX() > panelWidth - 20) {
            scrollFrom = e.getPoint();
            return;
        }
        //select
        if (selection.contains(toListId(toGrid(e.getPoint(), false)))) {
            draggingFrom = e.getPoint();
        } else {
            pressedAt = e.getPoint();
            rectTo = null;
            if (!e.isControlDown() && !e.isShiftDown()) {
                selection.clear();
            }
        }
    }

    public PictPanel addDropListener(final DropListener<VirtualFolder> l) {
        if (l != null) {
            this.dropListeners.add(l);
        }
        return this;
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        final Component root = SwingUtilities.getRoot(panel);
        final Point p = SwingUtilities.convertPoint(panel, e.getPoint(), root);
        final Component c = SwingUtilities.getDeepestComponentAt(root, p.x, p.y);
        System.out.println("Dropped to " + c.getName() + " (" + c.getClass().getName() + ")");
        final Point treePoint = SwingUtilities.convertPoint(root, p, c);
        if ("VirtualFolderTree".equals(c.getName())) {
            //drag to tree
            if (!dropListeners.isEmpty()) {
                final PictLoaderList selectedFiles = new PictLoaderList();
                for (Id id : selection.all()) {
                    selectedFiles.add(virtualFolder.getPictures().get(id.asInt()));
                }
                System.out.println("Dropping " + selectedFiles.size() + " files...");
                for (DropListener<VirtualFolder> l : dropListeners) {
                    final VirtualFolder vf = l.dropTo(treePoint);
                    if (vf != null && !virtualFolder.equals(vf)) {
                        virtualFolder.getPictures().remove(selection);
                        l.drop(selectedFiles);
                        l.dropped();
                        System.out.println("Dropped successfully");
                    } else {
                        System.out.println("Can't drop to null or same folder");
                    }
                }
                panel.repaint();
            }
            c.firePropertyChange("dropping-end", treePoint.x, treePoint.y);
            selection.clear();
            draggingFrom = null;
            dragTo = null;
        } else {
            if (pressedAt != null) {
                if (rectTo != null) {
                    draggingSelection.clear();
                    detectClickAndDragSelection(selection);
                } else {

                    final Point gridPoint = toGrid(e.getPoint(), false);
                    int xAt = gridPoint.x;
                    int yAt = gridPoint.y;

                    System.out.println("Click at " + xAt + "," + yAt + " (" + toListId(new Point(xAt, yAt)) + ")");

                    if (xAt >= 0 && yAt >= 0) {
                        if (!selection.isEmpty() && e.isShiftDown()) {
                            int to = toListId(new Point(xAt, yAt));
                            int min = selection.getFirst().asInt();
                            if (to < min) {
                                min = to;
                                to = selection.getLast().asInt();
                            }
                            for (int i = min; i <= to; i++) {
                                selection.add(new Id(i));
                            }
                        } else {

                            final Id id = new Id(toListId(new Point(xAt, yAt)));
                            if (selection.contains(id)) {
                                selection.remove(id);
                            } else {
                                selection.add(id);
                            }
                        }
                    }
                }
                System.out.println("Selection: " + selection);
                pressedAt = null;
            }
            if (draggingFrom != null) {

                System.out.println("Move selection " + selection + " to " + dragTo);

                virtualFolder.getPictures().move(selection, toListId(dragTo));

                selection.clear();

                draggingFrom = null;
                dragTo = null;
            }
        }
        panel.repaint();
    }

    private void detectClickAndDragSelection(final Selection selectTo) {
        final Point from = toGrid(pressedAt, true);
        final Point to = toGrid(rectTo, true);

        int fx = from.x;
        int ex = to.x;
        if (to.x < from.x) {
            fx = to.x;
            ex = from.x;
        }

        int fy = from.y;
        int ey = to.y;
        if (to.y < from.y) {
            fy = to.y;
            ey = from.y;
        }

        for (int sx = fx; sx <= ex; sx++) {
            for (int sy = fy; sy <= ey; sy++) {
                final Id id = new Id(toListId(new Point(sx, sy)));
                //if (selection.contains(id)) {
                //    selection.remove(id);
                //} else {
                selectTo.add(id);
                //}
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private void scrollUp(int scrollAmount) {
        scrollY += scrollAmount;
        if (scrollY > 0) {
            scrollY = 0;
        }
    }

    private void scrollDown(int scrollAmount) {
        scrollY -= scrollAmount;
        if (scrollY < -maxY) {
            scrollY = -maxY;
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() == 0) {
            endScrollAt = System.currentTimeMillis();
        } else if (e.getWheelRotation() < 0) {
            stopLoad();
            scrollUp(120);
            if (OS.isWindows()) {
                endScrollAt = System.currentTimeMillis();
            }
        } else if (e.getWheelRotation() > 0) {
            stopLoad();
            scrollDown(120);
            if (OS.isWindows()) {
                endScrollAt = System.currentTimeMillis();
            }
        }
        panel.repaint();
    }

    public void setPictNbOnARow(int nb) {
        System.out.println("Set pict nb on a row:" + nb);
        stopLoad();
        this.nbX = nb;
        panel.repaint();
        //load();
        endScrollAt = System.currentTimeMillis();//trigger load() 250ms later max
    }

    public void resized() {
        if (!firstLoad) {
            System.out.println("Resized");
            stopLoad();
            scrollDown(0);
            if (listener != null) {
                listener.nbPerRowChanged(nbX);//will trigger the repaint via the slider
            }
        }

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (pressedAt != null) {
            rectTo = e.getPoint();
            draggingSelection.clear();
            detectClickAndDragSelection(draggingSelection);
        }
        if (scrollFrom != null) {
            int decY = e.getPoint().y - scrollFrom.y;
            scrollY -= (int) ((decY / (float) panelHeight) * maxY);
            if (scrollY > 0) {
                scrollY = 0;
            }
            if (scrollY < -maxY) {
                scrollY = -maxY;
            }
            scrollFrom = e.getPoint();
        }
        if (draggingFrom != null) {
            final Component root = SwingUtilities.getRoot(panel);
            final Point p = SwingUtilities.convertPoint(panel, e.getPoint(), root);
            final Component c = SwingUtilities.getDeepestComponentAt(root, p.x, p.y);
            final Point treePoint = SwingUtilities.convertPoint(root, p, c);
            if ("VirtualFolderTree".equals(c.getName())) {
                c.firePropertyChange("dropping-begin", treePoint.x, treePoint.y);
                c.repaint();
            } else {
                dragTo = toGrid(e.getPoint(), true);
            }
        }
        panel.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

}
