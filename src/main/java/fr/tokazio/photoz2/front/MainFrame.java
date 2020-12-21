package fr.tokazio.photoz2.front;

import fr.tokazio.photoz2.back.PictCollect;
import fr.tokazio.photoz2.back.VirtualFolder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class MainFrame implements ComponentListener {

    private static final String JSON_FILE = "folders.json";
    private final JFrame frame;
    private final PictPanel pictPanel;

    public MainFrame() {
        this.frame = new JFrame();
        frame.setTitle("Photoz 2");
        frame.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);

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

        JPanel center = new JPanel();
        center.setLayout(new BorderLayout());
        frame.add(center, BorderLayout.CENTER);

        JPanel toolsTop = new JPanel();
        center.add(toolsTop, BorderLayout.NORTH);
        toolsTop.setLayout(new GridLayout(1, 2));
        toolsTop.setBackground(Color.WHITE);
        toolsTop.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel lblFolderTitle = new JLabel("Folder title");
        toolsTop.add(lblFolderTitle);
        lblFolderTitle.setForeground(Color.DARK_GRAY);

        JLabel lblCount = new JLabel("? élément(s)");
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
        slider.setMinimum(60);
        slider.setMaximum(400);
        slider.setValue(100);

        //TODO slider change le nb de colonne (pas la largeur d'image)

        //+=======================================================================


        slider.addChangeListener(e -> pictPanel.setPictSize(slider.getValue()));
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
            String folderName = JOptionPane.showInputDialog("Nom du dossier: ");
            tree.addToSelected(new VirtualFolder(folderName), false);
            UIUtil.expandAllNodes(tree.asTree(), 0, tree.getRowCount());
        });

        //+=======================================================================

        frame.setPreferredSize(new Dimension(800, 300));
        frame.pack();
        frame.setLocationRelativeTo(null);


        frame.addComponentListener(this);

        pictPanel.loadFiles(new PictCollect().all());

        //tree.load(JSON_FILE);
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
}
