package fr.tokazio.photoz2.front;

import fr.tokazio.photoz2.back.PictLoaderList;
import fr.tokazio.photoz2.back.VirtualFolder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

public class MainFrame implements ComponentListener, MouseListener {

    private static final String JSON_FILE = "folders.json";

    private final JFrame frame;
    private final PictPanel pictPanel;
    private final VirtualFolderTree tree;

    public MainFrame() {
        this.frame = new JFrame();
        frame.setTitle("Photoz 2");
        frame.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);

        frame.addMouseListener(this);

        frame.setLayout(new BorderLayout());

        final JPanel left = new JPanel();
        frame.add(left, BorderLayout.WEST);
        left.setPreferredSize(new Dimension(200, 0));
        left.setBackground(Color.WHITE);
        left.setLayout(new BorderLayout());

        tree = new VirtualFolderTree();
        left.add(tree.asComponent(), BorderLayout.CENTER);

        final JPanel butsFolder = new JPanel();
        left.add(butsFolder, BorderLayout.SOUTH);
        butsFolder.setBackground(Color.WHITE);

        final JButton addFolder = new JButton("+");
        butsFolder.add(addFolder, BorderLayout.SOUTH);
        addFolder.setFont(UIUtil.getFont(14));
        addFolder.setBackground(UIUtil.green());
        addFolder.setForeground(Color.WHITE);

        final JButton removeFolder = new JButton("-");
        butsFolder.add(removeFolder, BorderLayout.SOUTH);
        removeFolder.setFont(UIUtil.getFont(14));
        removeFolder.setBackground(UIUtil.red());
        removeFolder.setForeground(Color.WHITE);
        removeFolder.setVisible(false);

        final JPanel center = new JPanel();
        center.setLayout(new BorderLayout());
        frame.add(center, BorderLayout.CENTER);

        final JPanel toolsTop = new JPanel();
        center.add(toolsTop, BorderLayout.NORTH);
        toolsTop.setLayout(new GridLayout(1, 2));
        toolsTop.setBackground(Color.WHITE);
        toolsTop.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        final JLabel lblFolderTitle = new JLabel("Choisir un dossier...");
        toolsTop.add(lblFolderTitle);
        lblFolderTitle.setForeground(Color.DARK_GRAY);

        final JLabel lblCount = new JLabel("");
        toolsTop.add(lblCount);
        lblCount.setHorizontalAlignment(SwingConstants.RIGHT);
        lblCount.setForeground(Color.DARK_GRAY);

        pictPanel = new PictPanel();
        center.add(pictPanel.asComponent(), BorderLayout.CENTER);


        final JPanel toolsBottom = new JPanel();
        center.add(toolsBottom, BorderLayout.SOUTH);
        toolsBottom.setLayout(new GridLayout(1, 3));
        toolsBottom.setBackground(Color.WHITE);
        toolsBottom.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        final JLabel vide1 = new JLabel();
        vide1.setForeground(Color.DARK_GRAY);
        toolsBottom.add(vide1);

        final JLabel vide2 = new JLabel();
        vide2.setForeground(Color.DARK_GRAY);
        toolsBottom.add(vide2);

        final JSlider slider = new JSlider();
        toolsBottom.add(slider);
        slider.setBackground(Color.WHITE);
        slider.setMinimum(1);
        slider.setMaximum(24);
        slider.setValue(slider.getMaximum() - 4);

        //+=======================================================================

        pictPanel.addDropListener(new DropListener<>() {

            private VirtualFolder to;

            @Override
            public void drop(final PictLoaderList selection) {
                if (to != null) {
                    System.out.println("Dropped on '" + to.getName() + "'");
                    to.add(selection);
                }
            }

            @Override
            public VirtualFolder dropTo(final Point treePoint) {
                to = tree.nodeAtPoint(treePoint);
                return to;
            }

            @Override
            public void dropped() {
                try {
                    tree.save(JSON_FILE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        slider.addChangeListener(e -> pictPanel.setPictNbOnARow(slider.getMaximum() - slider.getValue()));
        pictPanel.addLoadingListener(new PictLoadingListener() {
            @Override
            public void onStart() {
                vide1.setText("Chargement...");
            }

            @Override
            public void onEnd() {
                vide1.setText("");
            }
        });

        addFolder.addActionListener(e -> {
            final VirtualFolder vf = new AddFolderFrame(frame).show().get();
            if (vf != null) {
                tree.addToSelected(vf);
                try {
                    tree.save(JSON_FILE);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        removeFolder.addActionListener(e -> {
            VirtualFolder vf = tree.getSelected();
            if (vf != null) {
                //TODO ask confirm
                tree.remove(vf);
                try {
                    tree.save(JSON_FILE);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        tree.addSelectionListener(vf -> {
            removeFolder.setVisible(vf != null);
            if (vf != null) {
                System.out.println("Selected " + vf.getName());
                lblFolderTitle.setText(vf.getFullName());
                lblCount.setText(count(vf));
                pictPanel.loadVirtualFolder(vf);
            }
        });

        //+=======================================================================

        frame.setPreferredSize(new Dimension(800, 600));
        frame.pack();
        frame.setLocationRelativeTo(null);


        frame.addComponentListener(this);

        pictPanel.setListener(new PictPanelListener() {
            @Override
            public void nbPerRowChanged(int nbX) {
                slider.setValue(slider.getMaximum() - nbX);
            }
        });

        try {
            tree.load(JSON_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String count(final VirtualFolder vf) {
        int c = vf.getPictures().size();
        return c + " élément" + (c > 1 ? "s" : "");
    }

    public void show() {
        frame.setVisible(true);
    }

    @Override
    public void componentResized(final ComponentEvent e) {
        pictPanel.resized();
        tree.resized();
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

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
