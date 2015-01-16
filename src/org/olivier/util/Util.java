/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.olivier.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author o.leliboux
 */
public class Util {

    /**
     * Permet de convertir un TreeNode en TreePath[]
     * @param tree a tree
     * @param expanded indicate if expanded or not
     * @return A tree path array
     */
    public static TreePath[] getPaths(JTree tree, boolean expanded) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        List<TreePath> list = new ArrayList<>();
        getPaths(tree, new TreePath(root), expanded, list);

        return (TreePath[]) list.toArray(new TreePath[list.size()]);
    }

    /**
     * Alimente les tableaux
     * @param tree a tree
     * @param parent a tree path
     * @param expanded indicate if expanded or not
     * @param list a list of tree path
     */
    private static void getPaths(JTree tree, TreePath parent, boolean expanded, List<TreePath> list) {
        if (expanded && !tree.isVisible(parent)) {
            return;
        }
        list.add(parent);
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                getPaths(tree, path, expanded, list);
            }
        }
    }     
}
