package fr.tokazio.photoz2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class PictPanel implements MouseListener, MouseWheelListener {

    private final JPanel panel;

    private final PictList pictList = new PictList();

    private final List<LoadingListener> loadingListeners = new LinkedList<>();
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
        pictList.stopLoading();
        System.out.println("Stoped loading");
    }

    //Lance le chargement des images dans la fenêtre
    private void load() {
        firstLoad = false;

        //Nombre de colonnes
        int nbX = ((panelWidth - marginL - marginR) / w) - 1;
        if (nbX < 1) {
            nbX = 1;
        }
        //Nombre de lignes
        int nbY = (int) Math.ceil((panelHeight - rowMargin * 2) / h);
        System.out.println(nbX + " col(s), " + nbY + " line(s)");

        //id de la 1ère ligne à dessiner
        int startAtRow = (int) ((Math.abs(scrollY)) / (h + rowMargin));

        //nb de lignes affichées
        int nbShow = (int) (nbY * nbX);

        int imgStart = startAtRow * nbX;
        int imgEnd = imgStart + nbShow - 1;// pictList.size() - 1;

        //Comme on calcul à la ligne, il se peut que la dernière ne soit pas complète
        if (imgEnd > pictList.size() - 1) {
            imgEnd = pictList.size() - 1;
        }

        doLoad("", imgStart, imgEnd);
    }

    private void doLoad(String str, int imgStart, int imgEnd) {
        lastLoadAt = System.currentTimeMillis();
        System.out.println("Load " + str + " from " + imgStart + " to " + imgEnd);
        boolean fired = false;
        for (int i = imgStart; i <= imgEnd; i++) {
            pictList.load(i, w, (int) h);
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

        //Nombre de colonnes
        int nbX = ((panelWidth - marginL - marginR) / w) - 1;
        if (nbX < 1) {
            nbX = 1;
        }
        //Nombre de lignes
        float nbY = (panelHeight - rowMargin * 2) / h;


        //position y de départ du dessin de la grille
        int y = scrollY + rowMargin;
        //marge entre colonne pour passer en largeur
        colMargin = ((panelWidth - marginL - marginR) - (nbX * w)) / nbX;

        //pour simplification on part toujours de la 1ère image, le must serait de ne s'occuper que de celle à afficher vraiment
        //on s'arrête quand même avant de calculer/afficher celles qui sont trop basse
        int nbVraimentDessinee = 0;
        int i = 0;
        while (y < panelHeight && i < pictList.size()) {
            Pict pict = pictList.get(i);

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
                if (pict.hasError()) {
                    g.setColor(Color.RED);
                }
                g.fillRect(x, y, w, (int) h);
                g.setFont(g.getFont().deriveFont(10f));
                if (pict.asImage() != null) {
                    g.drawImage(pict.asImage(), x, y, w, (int) h, null);
                    g.setColor(Color.GRAY);
                    g.drawRect(x, y, w, (int) h);
                }
                if (!pict.isLoaded() && pict.getProgress() < 100) {
                    g.setColor(Color.GRAY);
                    g.fillRect(x, y, (int) (w * (pict.getProgress() / 100)), (int) h);
                    g.setColor(Color.DARK_GRAY);
                    String txt = (int) pict.getProgress() + "%";
                    int tw = g.getFontMetrics().stringWidth(txt);
                    g.drawString(txt, x + ((w - tw) / 2f), y + ((h - 10) / 2f));
                }
                int tw = g.getFontMetrics().stringWidth(pict.getExt());
                g.setColor(Color.GRAY);
                g.fillRect(x + w - tw - 6, (int) (y + h - 18), tw + 6, 18);
                g.setColor(Color.WHITE);
                g.drawString(pict.getExt(), x + w - tw - 3, (int) (y + h - 5));
                if (pict.hasError()) {
                    String txt = pict.getError().getMessage();
                    tw = g.getFontMetrics().stringWidth(txt);
                    g.setColor(Color.WHITE);
                    g.drawString(txt, x + ((w - tw) / 2f), y + ((h - 10) / 2f));
                }
                g.setColor(Color.MAGENTA);
                g.drawString(i + "", x, y + 20);
            } else {
                pictList.unload(pict);
            }
            //colonne suivante
            x += w + colMargin;
            i++;
        }

        final long end = System.currentTimeMillis();
        //le dessin ne devrait pas prendre plus de 33ms (30/sec)
        System.out.println(System.currentTimeMillis() + "> Drawn " + nbVraimentDessinee + " images in " + (end - start) + "ms");

        //scroll

        //TODO bug sur la position quand on arrive en bas
        //TODO si on est en bas et qu'on resize il faudrait repositionner les scrolls sinon c'est la merde à l'affichage

        int sw = 20;//scroll width
        int sx = panelWidth - sw;//scroll position x

        int totRow = (int) Math.ceil(pictList.size() / (float) nbX);//nombre total de ligne d'image dans la liste
        int totY = (int) (totRow * (h + rowMargin));//hauteur total en px
        maxY = totY - panelHeight;//hauteur max de scroll en pi
        //System.out.println(totRow + " row in total (" + totY + "px)");


        int pl = panelHeight - 2 * sw;//hauteur entre les buts up et down
        //hauteur de la barre
        int sh = (int) ((panelHeight / (float) totY) * pl); //tester hauteurPanel/hauteurMax

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
            pictList.add(new Pict(i++, f).addLoadedListener((p) -> {
                System.out.println("Loading ended for #" + p.getId());
                firePendingChanged();
                panel.repaint();
            }).addProgressListener((p, v) -> {
                panel.repaint();
            }));
        }
    }

    public PictPanel addLoadingListener(LoadingListener l) {
        if (l != null) {
            loadingListeners.add(l);
        }
        return this;
    }

    private void firePendingChanged() {
        if (pictList.pendingCount() == 0) {
            System.out.println("No more pending");
            for (LoadingListener l : loadingListeners) {
                l.onEnd();
            }
        } else {
            System.out.println("Pending " + pictList.pendingCount());
            for (LoadingListener l : loadingListeners) {
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

    private void scrollDown(int scrollAmount) {
        scrollY -= scrollAmount;
        if (scrollY < -maxY) {
            scrollY = -maxY;
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

    private void scrollUp(int scrollAmount) {
        scrollY += scrollAmount;
        if (scrollY > 0) {
            scrollY = 0;
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

    public interface LoadingListener {

        void onStart();

        void onEnd();
    }
}
