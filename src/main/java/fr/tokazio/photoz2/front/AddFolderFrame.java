package fr.tokazio.photoz2.front;

import fr.tokazio.photoz2.back.VirtualFolder;

import javax.swing.*;
import java.awt.*;

public class AddFolderFrame {

    private final JDialog frame;

    private VirtualFolder virtualFolder;

    public AddFolderFrame(Frame parent) {
        frame = new JDialog(parent, "Nouveau dossier", Dialog.ModalityType.APPLICATION_MODAL);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        frame.setBackground(Color.WHITE);

        JPanel m = new JPanel();
        m.setLayout(new BoxLayout(m, BoxLayout.Y_AXIS));
        frame.add(m, BorderLayout.CENTER);
        m.setBackground(Color.WHITE);



        JPanel b = new JPanel();
        b.setLayout(new BoxLayout(b, BoxLayout.X_AXIS));
        m.add(b);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        b.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));
        b.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        b.setBackground(Color.WHITE);
        JLabel lblNom = new JLabel("Nom du dossier");
        b.add(lblNom);
        JTextField nom = new JTextField();
        b.add(nom);

        JPanel a = new JPanel();
        a.setLayout(new BoxLayout(a, BoxLayout.X_AXIS));
        m.add(a);
        a.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        a.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));
        a.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        a.setBackground(Color.WHITE);
        JLabel lblLinkTo = new JLabel("Importer des images");
        a.add(lblLinkTo);
        JTextField linkTo = new JTextField();
        a.add(linkTo);
        JButton linkButton = new JButton("Parcourir...");
        linkButton.setBackground(Color.WHITE);
        linkButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        a.add(linkButton);
        //TODO inclure sous dossiers (change en live)

        //TODO barre chargement
        //TODO infos, nb images importée (change en live)

        JPanel vide = new JPanel();
        vide.setBackground(Color.WHITE);
        m.add(vide);


        JPanel c = new JPanel(new GridLayout(0, 2));
        JButton annuler = new JButton("Annuler");
        annuler.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        annuler.setBackground(UIUtil.red());
        annuler.setForeground(Color.WHITE);
        annuler.setFont(UIUtil.getFont(14));
        c.add(annuler);

        JButton ajouter = new JButton("Ajouter");
        ajouter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        ajouter.setBackground(UIUtil.green());
        ajouter.setForeground(Color.WHITE);
        ajouter.setFont(UIUtil.getFont(14));
        c.add(ajouter);

        frame.add(c, BorderLayout.SOUTH);

        linkButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle("Choisir un dossier d'image à lier...");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                linkTo.setText(chooser.getSelectedFile().getAbsolutePath());
                if (nom.getText().isEmpty()) {
                    nom.setText(chooser.getSelectedFile().getName());
                }
            }
        });

        annuler.addActionListener(e -> {
            virtualFolder = null;
            close();
        });

        ajouter.addActionListener(e -> {
            if (nom.getText().isEmpty()) {
                nom.requestFocus();
                return;
            }
            virtualFolder = new VirtualFolder(nom.getText(), linkTo.getText());
            close();
        });


        frame.setPreferredSize(new Dimension(600, 200));
        frame.pack();
        frame.setLocationRelativeTo(parent);
    }

    public AddFolderFrame show() {
        frame.setVisible(true);
        return this;
    }

    public AddFolderFrame close() {
        frame.setVisible(false);
        return this;
    }

    public VirtualFolder get() {
        return virtualFolder;
    }
}
