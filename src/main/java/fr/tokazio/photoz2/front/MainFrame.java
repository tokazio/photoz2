package fr.tokazio.photoz2.front;

import fr.tokazio.photoz2.back.VirtualFolder;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
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

    public MainFrame() {
        this.frame = new JFrame();
        frame.setTitle("Photoz 2");
        frame.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);

        frame.addMouseListener(this);

        frame.setLayout(new BorderLayout());

        JPanel left = new JPanel();
        frame.add(left, BorderLayout.WEST);
        left.setPreferredSize(new Dimension(200, 0));
        left.setBackground(Color.DARK_GRAY);
        left.setLayout(new BorderLayout());

        DynamicTree tree = new DynamicTree();
        left.add(tree, BorderLayout.CENTER);

        JButton addFolder = new JButton("+");
        left.add(addFolder, BorderLayout.SOUTH);
        addFolder.setBackground(Color.WHITE);

        JPanel center = new JPanel();
        center.setLayout(new BorderLayout());
        frame.add(center, BorderLayout.CENTER);

        JPanel toolsTop = new JPanel();
        center.add(toolsTop, BorderLayout.NORTH);
        toolsTop.setLayout(new GridLayout(1, 2));
        toolsTop.setBackground(Color.WHITE);
        toolsTop.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel lblFolderTitle = new JLabel("Choisir un dossier...");
        toolsTop.add(lblFolderTitle);
        lblFolderTitle.setForeground(Color.DARK_GRAY);

        JLabel lblCount = new JLabel("");
        toolsTop.add(lblCount);
        lblCount.setHorizontalAlignment(SwingConstants.RIGHT);
        lblCount.setForeground(Color.DARK_GRAY);

        pictPanel = new PictPanel();
        center.add(pictPanel.asComponent(), BorderLayout.CENTER);


        JPanel toolsBottom = new JPanel();
        center.add(toolsBottom, BorderLayout.SOUTH);
        toolsBottom.setLayout(new GridLayout(1, 3));
        toolsBottom.setBackground(Color.WHITE);
        toolsBottom.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel vide1 = new JLabel();
        vide1.setForeground(Color.DARK_GRAY);
        toolsBottom.add(vide1);

        JLabel vide2 = new JLabel();
        vide2.setForeground(Color.DARK_GRAY);
        toolsBottom.add(vide2);

        JSlider slider = new JSlider();
        toolsBottom.add(slider);
        slider.setBackground(Color.WHITE);
        slider.setMinimum(1);
        slider.setMaximum(24);
        slider.setValue(slider.getMaximum() - 4);

        //TODO slider change le nb de colonne (pas la largeur d'image)

        //+=======================================================================

        pictPanel.addDropListener((e, p, selectedFiles) -> {
            final VirtualFolder folder = tree.nodeAtPoint(p);
            System.out.println("Dropped on " + folder.getName());
            folder.add(selectedFiles);
            return true;
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
            VirtualFolder vf = new AddFolderFrame(frame).show().get();
            if (vf != null) {
                tree.addToSelected(vf, false);
                UIUtil.expandAllNodes(tree.asTree(), 0, tree.getRowCount());
                try {
                    tree.save(JSON_FILE);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        tree.addSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
                VirtualFolder vf = (VirtualFolder) node.getUserObject();
                System.out.println("Selected " + vf.getName());
                lblFolderTitle.setText(vf.getFullName());
                lblCount.setText(count(vf));
                pictPanel.loadFiles(vf);
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

    private String count(VirtualFolder vf) {
        int c = vf.getImages().size();
        return c + " élément" + (c > 1 ? "s" : "");
    }

    public void show() {
        frame.setVisible(true);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        pictPanel.resized(e);
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
        System.out.println("Frame released");
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
