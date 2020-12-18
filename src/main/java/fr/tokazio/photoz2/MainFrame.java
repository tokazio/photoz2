package fr.tokazio.photoz2;

import javax.swing.*;
import java.awt.*;

public class MainFrame {

    private final JFrame frame;
    private final PictPanel pictPanel;

    public MainFrame() {
        this.frame = new JFrame();
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pictPanel = new PictPanel();
        frame.add(pictPanel.asComponent());
        frame.setPreferredSize(new Dimension(800, 600));
        frame.pack();
        frame.setLocationRelativeTo(null);

        pictPanel.load(new PictCollect().all());
    }

    public void show() {
        frame.setVisible(true);
    }

}
