package fr.tokazio.photoz2.front;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class ScrollBar implements MouseWheelListener {

    private final Component parent;

    private static final boolean withButtons = false;

    private int scrollY = 0;
    private int maxY = 0;
    private int totY = 0;
    private int rowH = 0;
    private int sw = 8;//scroll width
    private Color color = new Color(172, 172, 172, 172);

    public ScrollBar(final Component parent) {
        this.parent = parent;
        parent.addMouseWheelListener(this);
    }

    public int getWidth() {
        return maxY > 0 ? sw : 0;
    }

    public void draw(Graphics2D g) {
        if (maxY > 0) {
            final int sx = parent.getWidth() - sw - 2;//scroll position x
            final int pl = withButtons ? parent.getHeight() - 2 * sw : parent.getHeight();//hauteur entre les buts up et down
            final int sh = (int) ((parent.getHeight() / (float) totY) * pl);//hauteur de la barre
            final int sp = (int) ((Math.abs(scrollY) / (float) totY) * pl);//position de la scrollbar

            //bar
            g.setColor(color);
            g.fillRoundRect(sx, withButtons ? sw + 1 + sp : 1 + sp, sw, sh, sw, sw);

            if (withButtons) {
                //but up
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(sx, 0, sw, sw);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(sx, 0, sw, sw);
                //but down
                g.fillRect(sx, parent.getHeight() - sw, sw, sw);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(sx, parent.getHeight() - sw, sw, sw);
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (maxY > 0) {
            if (e.getWheelRotation() == 0) {
                //end scroll
            } else if (e.getWheelRotation() < 0) {
                scrollUp(rowH);
            } else if (e.getWheelRotation() > 0) {
                scrollDown(rowH);
            }
            parent.repaint();
        }
    }

    private void scrollUp(final int scrollAmount) {
        scrollY += scrollAmount;
        if (scrollY > 0) {
            scrollY = 0;
        }
    }

    private void scrollDown(final int scrollAmount) {
        scrollY -= scrollAmount;
        if (scrollY < -maxY) {
            scrollY = -maxY;
        }
    }

    public int scrollY() {
        return scrollY;
    }

    public void defineElements(int elementCount, int elementHeight) {
        this.rowH = elementHeight;
        this.totY = elementCount * elementHeight;//hauteur total en px
        maxY = totY - parent.getHeight();//hauteur max de scroll en px
    }

    public void resized() {
        if (maxY > 0) {
            scrollY = 0;
        }
    }
}
