package fr.tokazio.photoz2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.List;

public class PictPanel implements MouseListener, MouseWheelListener {

    private final JPanel panel;

    private final PictList pictList = new PictList();

    int startY = 0;
    int maxY = 0;

    int panelWidth = 1;//avoid /0
    int panelHeight = 1;//avoid /0

    public PictPanel() {
        this.panel = new JPanel() {

            @Override
            public void paintComponent(final Graphics g) {
                super.paintComponent(g);
                panelWidth = getWidth();
                panelHeight = getHeight();
                draw((Graphics2D) g);
            }
        };
        this.panel.addMouseListener(this);
        this.panel.addMouseWheelListener(this);
        //TODO drag scroll bar
    }

    //TODO tester skija avec opengl

    private void draw(final Graphics2D g) {
        final long start = System.currentTimeMillis();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, panelWidth, panelHeight);

        float h = 100;//hauteur d'une image
        int w = 100;//largeur d'une image
        int rowMargin = 20;//espace entre les lignes

        int marginL = 20;//marge à gauche
        int marginR = 20;//marge à droite

        int colMargin = marginL;
        int x = colMargin;

        //Nombre de colonnes
        int nbX = ((panelWidth - marginL - marginR) / w) - 1;
        //Nombre de lignes
        float nbY = (panelHeight - rowMargin * 2) / h;

        //id de la 1ère ligne à dessiner
        int startAtRow = (int) ((Math.abs(startY) - rowMargin) / h);
        if (startAtRow > 1) {
            startAtRow -= 1;
        }

        //nb de lignes affichées
        int nbShow = (int) (nbY * nbX);

        //position y de départ du dessin de la grille
        int y = startY + rowMargin;
        //marge entre colonne pour passer en largeur
        colMargin = ((panelWidth - marginL - marginR) - (nbX * w)) / nbX;

        //pour simplification on part toujours de la 1ère image, le must serait de ne s'occuper que de celle à afficher vraiment
        //on s'arrête quand même avant de calculer/afficher celles qui sont trop basse
        int nbVraimentDessinee = 0;
        int i = 0;
        while (y < panelHeight && i < pictList.size()) {
            Pict pict = pictList.get(i);
            i++;
            //passage à la ligne, revient à la 1ère colonne
            if (x + w > panelWidth) {
                x = marginL;
                y += h + rowMargin;
            }
            //On commence vraiment à dessiner à partir de la ligne au dessus de la première
            //pour avoir le bas d'une éventuelle ligne précédente
            if (y > -h - rowMargin) {
                pict.load();//TODO load async avec panel.repaint() quand terminé
                nbVraimentDessinee++;
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(x, y, w, (int) h);
                if (pict.asImage() != null) {
                    g.drawImage(pict.asImage(), x, y, w, (int) h, null);
                    g.setColor(Color.GRAY);
                    g.drawRect(x, y, w, (int) h);
                    //TODO draw tag bottom right with the file ext
                }

            }
            //colonne suivante
            x += w + colMargin;
        }

        final long end = System.currentTimeMillis();
        //le dessin ne devrait pas prendre plus de 33ms (30/sec)
        System.out.println("Drawn " + nbVraimentDessinee + " images in " + (end - start) + "ms");

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
        //int sh = pl;//par défaut la plage dispo
        //sh = sh * (nbShow / pictList.size());//finalement en % du nombre de lignes visibles
        int sh = (int) ((panelHeight / (float) totY) * panelHeight); //tester hauteurPanel/hauteurMax
        //System.out.println(panelHeight + " visible sur " + totY);

        //position de la scrollbar
        int sp = (int) ((startAtRow / ((float) totRow + 1)) * pl);

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

    public Component asComponent() {
        return this.panel;
    }

    public void load(List<File> in) {
        for (File f : in) {
            pictList.add(new Pict(f));
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        //TODO selection
        if (e.getX() > panelWidth - 20) {
            if (e.getY() < 20) {
                scrollUp(20);
            } else if (e.getY() > panelHeight - 20) {
                scrollDown(20);
            } else {
                //scroll bar
            }
        }

    }

    private void scrollDown(int scrollAmount) {
        if (startY - scrollAmount < -maxY) {
            return;
        }
        startY -= scrollAmount;
        panel.repaint();
    }

    private void scrollUp(int scrollAmount) {
        if (startY + scrollAmount > 0) {
            return;
        }
        startY += scrollAmount;
        panel.repaint();
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

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() < 0) {
            scrollUp(120);//e.getScrollAmount()*10);
        } else {
            scrollDown(120);//e.getScrollAmount()*10);
        }
    }
}
