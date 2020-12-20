/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package fr.tokazio.photoz2.front;

import fr.tokazio.photoz2.back.VirtualFolder;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.IconUIResource;
import javax.swing.tree.*;
import java.awt.*;
import java.io.IOException;

public class DynamicTree extends JPanel {

    private static final String RSS = "/";
    private static final ImageIcon TOUTES = loadIcon(RSS + "toutes.png");
    private static final ImageIcon UNE = loadIcon(RSS + "une.png");
    private static final ImageIcon FOLDER = loadIcon(RSS + "folder.png");
    private static final ImageIcon COLLAPSED = loadIcon(RSS + "collapsed.png");
    private static final ImageIcon EXPANDED = loadIcon(RSS + "expanded.png");
    private static final ImageIcon PETIT = loadIcon(RSS + "petit.png");
    private static final ImageIcon GRAND = loadIcon(RSS + "grand.png");
    private final VirtualFolder rootVirtualFolder;
    private final TreeSelectionListener treeSelectionListener = (TreeSelectionEvent e) -> {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) e
                .getPath().getLastPathComponent();
        /*
        List<File> liste = new ArrayList<>();
        if (node.getUserObject() instanceof File) {
            File dir = (File) node.getUserObject();
            liste.addAll(getPhotosFromFolder(dir));
            titre.setText(dir.getName());
        } else {
            dossiersAExplorer.stream().filter((dir) -> (dir.isDirectory())).forEachOrdered((dir) -> {
                liste.addAll(getPhotosFromFolderAndSubfolder(dir));
            });
            titre.setText("Toutes les photos");
        }

        if (!liste.isEmpty()) {
            hasSelectedFiles.enable(true);
        }
        compte.setText(liste.size() + " élément" + (liste.size() > 1 ? "s" : ""));
        pictZone.setFiles(liste);

         */
    };
    protected DefaultMutableTreeNode rootNode;
    protected DefaultTreeModel treeModel;
    protected JTree tree;
    private final TreeCellRenderer treeRenderer = (JTree tree1, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) -> {
        JLabel l = new JLabel();
        if (((DefaultMutableTreeNode) value).getUserObject() instanceof VirtualFolder) {
            VirtualFolder f = (VirtualFolder) ((DefaultMutableTreeNode) value).getUserObject();
            l.setText(f.getName());
            if (((DefaultMutableTreeNode) value).getChildCount() > 0) {
                l.setIcon(FOLDER);
            } else {
                l.setIcon(UNE);
            }
        } else {
            l.setText(((DefaultMutableTreeNode) value).getUserObject().toString());
            l.setIcon(TOUTES);
        }
        //l.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        l.setFont(tree.getFont().deriveFont(Font.PLAIN));
        l.setForeground(Color.WHITE);
        JPanel panel = new JPanel(new GridLayout(1, 1));
        if (selected) {
            panel.setBackground(Color.GRAY);
        } else {
            panel.setBackground(null);
        }
        panel.add(l);
        return panel;
    };
    private Toolkit toolkit = Toolkit.getDefaultToolkit();

    public DynamicTree() {
        super(new GridLayout(1, 0));

        UIManager.put("Tree.collapsedIcon", new IconUIResource(COLLAPSED));
        UIManager.put("Tree.expandedIcon", new IconUIResource(EXPANDED));
        UIManager.getLookAndFeelDefaults().put("Tree.paintLines", false);

        rootNode = new DefaultMutableTreeNode("Tous");

        rootVirtualFolder = new VirtualFolder("Tous");
        rootNode.setUserObject(rootVirtualFolder);

        treeModel = new DefaultTreeModel(rootNode);
        treeModel.addTreeModelListener(new MyTreeModelListener());
        tree = new JTree(treeModel);

        tree.setEditable(false);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);

        tree.setBackground(Color.DARK_GRAY);

        tree.addTreeSelectionListener(treeSelectionListener);
        tree.setCellRenderer(treeRenderer);

        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setBorder(null);
        add(scrollPane);
    }

    private static ImageIcon loadIcon(String str) {
        try {
            return new ImageIcon(ImageIO.read(DynamicTree.class.getResourceAsStream(str)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Remove all nodes except the root node.
     */
    public void clear() {
        rootNode.removeAllChildren();
        treeModel.reload();
    }

    /**
     * Remove the currently selected node.
     */
    public void removeCurrentNode() {
        TreePath currentSelection = tree.getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
                    (currentSelection.getLastPathComponent());
            MutableTreeNode parent = (MutableTreeNode) (currentNode.getParent());
            if (parent != null) {
                treeModel.removeNodeFromParent(currentNode);
                return;
            }
        }

        // Either there was no selection, or the root was selected.
        toolkit.beep();
    }

    private DefaultMutableTreeNode getSelectedNode() {
        DefaultMutableTreeNode parentNode = null;
        TreePath parentPath = tree.getSelectionPath();

        if (parentPath == null) {
            parentNode = rootNode;
        } else {
            parentNode = (DefaultMutableTreeNode)
                    (parentPath.getLastPathComponent());
        }
        return parentNode;
    }

    /**
     * Add child to the currently selected node.
     */
    public DefaultMutableTreeNode addObject(Object child) {
        DefaultMutableTreeNode parentNode = getSelectedNode();
        return addObject(parentNode, child, true);
    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child) {
        return addObject(parent, child, false);
    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child,
                                            boolean shouldBeVisible) {
        DefaultMutableTreeNode childNode =
                new DefaultMutableTreeNode(child);

        if (parent == null) {
            parent = rootNode;
        }

        //It is key to invoke this on the TreeModel, and NOT DefaultMutableTreeNode
        treeModel.insertNodeInto(childNode, parent,
                parent.getChildCount());

        //Make sure the user can see the lovely new node.
        if (shouldBeVisible) {
            tree.scrollPathToVisible(new TreePath(childNode.getPath()));
        }
        return childNode;
    }

    public int getRowCount() {
        return tree.getRowCount();
    }

    public JTree asTree() {
        return tree;
    }

    public void add(final VirtualFolder virtualFolder, boolean nodeOnly) {
        if (!nodeOnly) {
            VirtualFolder rootVirtualFolder = (VirtualFolder) rootNode.getUserObject();
            rootVirtualFolder.add(virtualFolder);
        }
        DefaultMutableTreeNode node = addObject(virtualFolder.getName());
        node.setUserObject(virtualFolder);
        for (VirtualFolder f : virtualFolder.getChildren().all()) {
            add(node, f);
        }
        //save();
    }

    public void add(final DefaultMutableTreeNode parent, final VirtualFolder virtualFolder) {
        DefaultMutableTreeNode node = addObject(parent, virtualFolder.getName());
        node.setUserObject(virtualFolder);
        for (VirtualFolder f : virtualFolder.getChildren().all()) {
            add(node, f);
        }
        //save();
    }

    public void addToSelected(final VirtualFolder virtualFolder, final boolean nodeOnly) {
        if (!nodeOnly) {
            VirtualFolder parentVirtualFolder = (VirtualFolder) getSelectedNode().getUserObject();
            parentVirtualFolder.add(virtualFolder);
        }
        DefaultMutableTreeNode node = addObject(getSelectedNode(), virtualFolder.getName());
        node.setUserObject(virtualFolder);
        //save();
    }

    static class MyTreeModelListener implements TreeModelListener {
        public void treeNodesChanged(TreeModelEvent e) {
            DefaultMutableTreeNode node;
            node = (DefaultMutableTreeNode) (e.getTreePath().getLastPathComponent());

            /*
             * If the event lists children, then the changed
             * node is the child of the node we've already
             * gotten.  Otherwise, the changed node and the
             * specified node are the same.
             */

            int index = e.getChildIndices()[0];
            node = (DefaultMutableTreeNode) (node.getChildAt(index));

            System.out.println("The user has finished editing the node.");
            System.out.println("New value: " + node.getUserObject());
        }

        public void treeNodesInserted(TreeModelEvent e) {
        }

        public void treeNodesRemoved(TreeModelEvent e) {
        }

        public void treeStructureChanged(TreeModelEvent e) {
        }
    }

    /*
    private void save(final String filename) throws IOException {
        final VirtualFolder rootVirtualFolder = (VirtualFolder) rootNode.getUserObject();
        rootVirtulFolder.save(filename);
    }

    public void load(final String filename){
        VirtualFolder root;
        try{
            root = VirtualFolder.load(filename);
            for(VirtualFolder f : root.getChildren().all()){
                add(f,true);
            }
        }catch (Exception ex){
            ex.printStackTrace();
            root = new VirtualFolder("Tous");
        }
        rootNode.setUserObject(root);
        UIUtil.expandAllNodes(tree, 0, tree.getRowCount());
    }

     */

}
