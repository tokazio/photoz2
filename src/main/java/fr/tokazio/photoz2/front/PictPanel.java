package fr.tokazio.photoz2.front;

import fr.tokazio.photoz2.back.PictLoader;
import fr.tokazio.photoz2.back.PictLoaderList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class PictPanel implements MouseListener, MouseWheelListener, MouseMotionListener {

    private final JPanel panel;

    private final PictLoaderList pictLoaderList = new PictLoaderList();

    private final List<PictLoadingListener> pictLoadingListeners = new LinkedList<>();
    int maxY = 0;

    int panelWidth = 1;//avoid /0
    int panelHeight = 1;//avoid /0
    int scrollY = 0;
    float h = 100;//hauteur d'une image

    //TODO tester skija avec opengl
    int w = 100;//largeur d'une image
    int rowMargin = 20;//espace entre les lignes
    int marginL = 20;//marge à gauche
    int marginR = 20;//marge à droite
    boolean firstLoad = true;
    long lastLoadAt;
    long endScrollAt;

    private final List<Id> selection = new LinkedList<>();
    private Point pressedAt;
    private Point rectTo;

    public PictPanel() {
        this.panel = new JPanel() {

            @Override
            public void paintComponent(final Graphics g) {
                super.paintComponent(g);
                panelWidth = getWidth();
                panelHeight = getHeight();

                draw((Graphics2D) g);
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
        pictLoaderList.stopLoading();
        System.out.println("Stoped loading");
    }

    private int nbCol() {
        int nbX = ((panelWidth - marginL - marginR) / w) - 1;
        if (nbX < 1) {
            nbX = 1;
        }
        return nbX;
    }

    private float nbRowInPanel() {
        return (float) Math.ceil((panelHeight - rowMargin * 2) / h);
    }

    private int firstRowInPanel() {
        return (int) ((Math.abs(scrollY)) / (h + rowMargin));
    }

    private int colMargin() {
        final int nbX = nbCol();
        return ((panelWidth - marginL - marginR) - (nbX * w)) / nbX;
    }

    //Lance le chargement des images visibles dans le panel
    private void load() {
        firstLoad = false;

        int nbX = nbCol();
        int nbY = (int) nbRowInPanel();
        System.out.println(nbX + " col(s), " + nbY + " line(s)");

        //id de la 1ère ligne à dessiner
        int startAtRow = firstRowInPanel();

        //nb de photos affichées
        int nbShow = (int) (nbY * nbX);

        int imgStart = startAtRow * nbX;
        int imgEnd = imgStart + nbShow - 1;// pictList.size() - 1;

        //Comme on calcul à la ligne, il se peut que la dernière ne soit pas complète
        if (imgEnd > pictLoaderList.size() - 1) {
            imgEnd = pictLoaderList.size() - 1;
        }

        doLoad(imgStart, imgEnd);
    }

    private void doLoad(int imgStart, int imgEnd) {
        lastLoadAt = System.currentTimeMillis();
        System.out.println("Load from " + imgStart + " to " + imgEnd);
        boolean fired = false;
        for (int i = imgStart; i <= imgEnd; i++) {
            pictLoaderList.load(i, w, (int) h);
            if (!fired) {
                fired = true;
                firePendingChanged();
            }
        }
    }

    public Component asComponent() {
        return this.panel;
    }

    private void draw(final Graphics2D g) {

        final long start = System.currentTimeMillis();

        //background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, panelWidth, panelHeight);

        int colMargin = marginL;
        int x = colMargin;

        int nbX = nbCol();

        //position y de départ du dessin de la grille
        int y = scrollY + rowMargin;
        //marge entre colonne pour passer en largeur
        colMargin = colMargin();

        //pour simplification on part toujours de la 1ère image, le must serait de ne s'occuper que de celle à afficher vraiment
        //on s'arrête quand même avant de calculer/afficher celles qui sont trop basse
        int nbVraimentDessinee = 0;
        int i = 0;
        while (y < panelHeight && i < pictLoaderList.size()) {
            PictLoader pictLoader = pictLoaderList.get(i);

            int idY = (int) (i / (float) nbX);
            int idX = i - (idY * nbX);
            System.out.println("Drawing " + idX + "," + idY);

            //passage à la ligne, revient à la 1ère colonne
            if (x + w > panelWidth) {
                x = marginL;
                y += h + rowMargin;
            }
            //On commence vraiment à dessiner à partir de la ligne au dessus de la première
            //pour avoir le bas d'une éventuelle ligne précédente
            if (y > -h - rowMargin) {
                nbVraimentDessinee++;
                g.setColor(Color.LIGHT_GRAY);
                if (pictLoader.hasError()) {
                    g.setColor(Color.RED);
                }
                g.fillRect(x, y, w, (int) h);
                g.setFont(g.getFont().deriveFont(10f));
                if (pictLoader.asImage() != null) {
                    g.drawImage(pictLoader.asImage(), x, y, w, (int) h, null);
                    g.setColor(Color.GRAY);
                    g.drawRect(x, y, w, (int) h);
                }
                if (!pictLoader.isLoaded() && pictLoader.getProgress() < 100) {
                    g.setColor(Color.GRAY);
                    g.fillRect(x, y, (int) (w * (pictLoader.getProgress() / 100)), (int) h);
                    g.setColor(Color.DARK_GRAY);
                    String txt = (int) pictLoader.getProgress() + "%";
                    int tw = g.getFontMetrics().stringWidth(txt);
                    g.drawString(txt, x + ((w - tw) / 2f), y + ((h - 10) / 2f));
                }
                int tw = g.getFontMetrics().stringWidth(pictLoader.getExt());
                g.setColor(Color.GRAY);
                g.fillRect(x + w - tw - 6, (int) (y + h - 18), tw + 6, 18);
                g.setColor(Color.WHITE);
                g.drawString(pictLoader.getExt(), x + w - tw - 3, (int) (y + h - 5));
                if (pictLoader.hasError()) {
                    String txt = pictLoader.getError().getMessage();
                    tw = g.getFontMetrics().stringWidth(txt);
                    g.setColor(Color.WHITE);
                    g.drawString(txt, x + ((w - tw) / 2f), y + ((h - 10) / 2f));
                }
                g.setColor(Color.MAGENTA);
                g.drawString(i + "", x, y + 20);

                if (selection.contains(new Id(i))) {//new Point(idX, idY))) {
                    g.setColor(new Color(32, 128, 255));
                    g.setStroke(new BasicStroke(3));
                    g.drawRect(x, y, w, (int) h);
                    g.setStroke(new BasicStroke(1));
                    g.setColor(new Color(32, 128, 255, 48));
                    g.fillRect(x, y, w, (int) h);
                }

            } else {
                pictLoaderList.unload(pictLoader);
            }
            //colonne suivante
            x += w + colMargin;
            i++;

            if (pressedAt != null && rectTo != null) {
                int sx = pressedAt.x;
                if (rectTo.x < pressedAt.x) {
                    sx = rectTo.x;
                }
                int sy = pressedAt.y;
                if (rectTo.y < pressedAt.y) {
                    sy = rectTo.y;
                }
                int sw = Math.abs(rectTo.x - pressedAt.x);
                int sh = Math.abs(rectTo.y - pressedAt.y);
                g.setColor(new Color(32, 128, 255));
                g.setStroke(new BasicStroke(1));
                g.drawRect(sx, sy, sw, sh);
                g.setColor(new Color(32, 128, 255, 24));
                g.fillRect(sx, sy, sw, sh);
            }

        }

        final long end = System.currentTimeMillis();
        //le dessin ne devrait pas prendre plus de 33ms (30/sec)
        System.out.println(System.currentTimeMillis() + "> Drawn " + nbVraimentDessinee + " images in " + (end - start) + "ms");

        //scroll

        //TODO si on est en bas et qu'on resize il faudrait repositionner les scrolls sinon c'est la merde à l'affichage

        int sw = 20;//scroll width
        int sx = panelWidth - sw;//scroll position x

        int totRow = (int) Math.ceil(pictLoaderList.size() / (float) nbX);//nombre total de ligne d'image dans la liste
        int totY = (int) (totRow * (h + rowMargin));//hauteur total en px
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

    public void loadFiles(List<File> in) {
        int i = 0;
        for (File f : in) {
            pictLoaderList.add(new PictLoader(i++, f).addLoadedListener((p) -> {
                System.out.println("Loading ended for #" + p.getId());
                firePendingChanged();
                panel.repaint();
            }).addProgressListener((p, v) -> {
                panel.repaint();
            }));
        }
    }

    public PictPanel addLoadingListener(final PictLoadingListener l) {
        if (l != null) {
            pictLoadingListeners.add(l);
        }
        return this;
    }

    private void firePendingChanged() {
        if (pictLoaderList.pendingCount() == 0) {
            System.out.println("No more pending");
            for (PictLoadingListener l : pictLoadingListeners) {
                l.onEnd();
            }
        } else {
            System.out.println("Pending " + pictLoaderList.pendingCount());
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
            } else {
                //scroll bar
            }
        }

    }

    private int toListId(Point gridPoint) {
        return gridPoint.y * nbCol() + gridPoint.x;
    }

    private Point toGrid(Point pxPoint, boolean withMargin) {
        int colMargin = colMargin();
        int xAt = ((int) pxPoint.getX() - marginL) / (w + colMargin);
        int l = marginL + xAt * (w + colMargin);
        int r = l + w;
        if (!withMargin && (pxPoint.getX() < l || pxPoint.getX() > r)) {
            xAt = -1;
        }

        int yAt = ((int) (pxPoint.getY() - rowMargin) / (int) (h + rowMargin));
        int t = (int) (rowMargin + yAt * (h + rowMargin));
        int b = t + w;
        if (!withMargin && (pxPoint.getY() < t || pxPoint.getY() > b)) {
            yAt = -1;
        }

        return new Point(xAt, yAt);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        pressedAt = e.getPoint();
        rectTo = null;
        if (!e.isShiftDown()) {
            selection.clear();
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        if (pressedAt != null) {

            if (rectTo != null) {
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
                        if (selection.contains(id)) {
                            selection.remove(id);
                        } else {
                            selection.add(id);
                        }
                    }
                }
            } else {

                final Point gridPoint = toGrid(e.getPoint(), false);
                int xAt = gridPoint.x;
                int yAt = gridPoint.y;

                System.out.println("Click at " + xAt + "," + yAt);

                if (xAt >= 0 && yAt >= 0) {
                    final Id id = new Id(toListId(new Point(xAt, yAt)));
                    if (selection.contains(id)) {
                        selection.remove(id);
                    } else {
                        selection.add(id);
                    }
                }
            }
            System.out.println(selection);
            panel.repaint();
            pressedAt = null;
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
        } else if (e.getWheelRotation() > 0) {
            stopLoad();
            scrollDown(120);
        }
        panel.repaint();
    }

    public void setPictSize(int size) {
        System.out.println("Set pict size");
        stopLoad();
        this.w = size;
        this.h = (float) size;
        panel.repaint();
        //load();
        endScrollAt = System.currentTimeMillis();//trigger load() 250ms later max
    }

    public void resized(ComponentEvent e) {
        if (!firstLoad) {
            System.out.println("Resized");
            stopLoad();
            panel.repaint();
            //load();
            endScrollAt = System.currentTimeMillis();//trigger load() 250ms later max
        }
    }


    @Override
    public void mouseDragged(MouseEvent e) {
        if (pressedAt != null) {
            rectTo = e.getPoint();
            panel.repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
