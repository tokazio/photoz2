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
import fr.tokazio.photoz2.back.VirtualFolderSerializer;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.IconUIResource;
import javax.swing.tree.*;
import java.awt.*;
import java.io.IOException;

public class DynamicTree extends JPanel {

    private final DefaultMutableTreeNode rootNode;
    private final DefaultTreeModel treeModel;
    private final JTree tree;
    private VirtualFolder rootVirtualFolder;

    public DynamicTree() {
        super(new GridLayout(1, 0));
        UIManager.put("Tree.collapsedIcon", new IconUIResource(DynamicTreeCellRenderer.COLLAPSED));
        UIManager.put("Tree.expandedIcon", new IconUIResource(DynamicTreeCellRenderer.EXPANDED));
        UIManager.getLookAndFeelDefaults().put("Tree.paintLines", false);

        rootNode = new DefaultMutableTreeNode("Tous");
        rootVirtualFolder = new VirtualFolder("Tous", null);
        rootNode.setUserObject(rootVirtualFolder);
        treeModel = new DefaultTreeModel(rootNode);
        treeModel.addTreeModelListener(new DynamicTreeModelListener());

        tree = new JTree(treeModel);
        tree.setLargeModel(true);
        tree.setEditable(false);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(false);
        tree.setBackground(Color.DARK_GRAY);
        tree.setCellRenderer(new DynamicTreeCellRenderer());
        tree.setFont(getFont().deriveFont(14f));

        final JScrollPane scrollPane = new JScrollPane(tree);
        //scrollPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        add(scrollPane);
    }

    public DynamicTree addSelectionListener(TreeSelectionListener listener) {
        tree.addTreeSelectionListener(listener);
        return this;
    }

    public VirtualFolder nodeAtPoint(final Point rootPoint) {
        final Component root = SwingUtilities.getRoot(tree);
        final Point treePoint = SwingUtilities.convertPoint(root, rootPoint, tree);
        return (VirtualFolder) ((DefaultMutableTreeNode) tree.getClosestPathForLocation(treePoint.x, treePoint.y).getLastPathComponent()).getUserObject();
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
            }
        }
    }

    private DefaultMutableTreeNode getSelectedNode() {
        DefaultMutableTreeNode parentNode = null;
        TreePath parentPath = tree.getSelectionPath();
        if (parentPath == null) {
            parentNode = rootNode;
        } else {
            parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
        }
        return parentNode;
    }

    private DefaultMutableTreeNode addNode(DefaultMutableTreeNode parent, VirtualFolder virtualFolder) {
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(virtualFolder);
        childNode.setUserObject(virtualFolder);
        if (parent == null) {
            parent = rootNode;
        }
        //It is key to invoke this on the TreeModel, and NOT DefaultMutableTreeNode
        treeModel.insertNodeInto(childNode, parent, parent.getChildCount());
        //Make sure the user can see the lovely new node.
        //tree.scrollPathToVisible(new TreePath(childNode.getPath()));
        return childNode;
    }

    public int getRowCount() {
        return tree.getRowCount();
    }

    public JTree asTree() {
        return tree;
    }

    public void addToSelected(final VirtualFolder virtualFolder, final boolean nodeOnly) {
        if (!nodeOnly) {
            final VirtualFolder parentVirtualFolder = (VirtualFolder) getSelectedNode().getUserObject();
            parentVirtualFolder.add(virtualFolder);
        }
        final DefaultMutableTreeNode node = addNode(getSelectedNode(), virtualFolder);
    }

    public void save(final String filename) throws IOException {
        VirtualFolderSerializer.getInstance().save(rootVirtualFolder, filename);
    }

    public void load(final String filename) throws IOException {
        rootVirtualFolder = VirtualFolderSerializer.getInstance().load(filename);
        loadChildren(rootNode, rootVirtualFolder);
        UIUtil.expandAllNodes(tree, 0, tree.getRowCount());
    }

    private void loadChildren(DefaultMutableTreeNode parent, VirtualFolder virtualFolder) {
        for (VirtualFolder f : rootVirtualFolder.getChildren().all()) {
            addNode(parent, f);
        }
    }

}
