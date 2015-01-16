/**
 * MySwing: Advanced Swing Utilites Copyright (C) 2005 Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.olivier;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import org.olivier.component.jtreecheckbox.check.CheckTreeManager;
import org.olivier.component.jtreecheckbox.check.TreePathSelectable;

/**
 * @author Santhosh Kumar T
 */
public class CheckTreeDemo {

   private static JTree createCheckTree(boolean dig, TreePathSelectable selectable) {
      JTree tree = new JTree();
      CheckTreeManager checkTreeManager = new CheckTreeManager(tree, dig, selectable);

      for (int i = 0; i < tree.getRowCount(); i++) {
         tree.expandRow(i);
      }

      return tree;
   }

   private static JScrollPane createScroll(Component comp) {
      JScrollPane scroll = new JScrollPane(comp);
      scroll.setBorder(BorderFactory.createEmptyBorder());
      return scroll;
   }

   public static void main(String[] args) {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) {
         e.printStackTrace();
      }

      Insets oldInsets = UIManager.getInsets("TabbedPane.contentBorderInsets"); //NOI18N
      UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 1, 0)); // bottom is 1. beacause tabs is bottom aligned
      JTabbedPane tabPane = new JTabbedPane(JTabbedPane.BOTTOM);
      UIManager.put("TabbedPane.contentBorderInsets", oldInsets); //NOI18N

      tabPane.addTab("DIG=TRUE", createScroll(createCheckTree(true, null)));
      tabPane.addTab("DIG=FALSE", createScroll(createCheckTree(false, null)));

      tabPane.addTab("LeafSelectable", createScroll(createCheckTree(true, new TreePathSelectable() {
         public boolean isSelectable(TreePath path) {
            return path.getPathCount() == 3;
         }
      })));
      tabPane.addTab("NonLeafSelectable", createScroll(createCheckTree(true, new TreePathSelectable() {
         public boolean isSelectable(TreePath path) {
            return path.getPathCount() != 3;
         }
      })));

      JFrame frame = new JFrame("JTree with CheckBoxes - santhosh@in.fiorano.com");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.getContentPane().add(tabPane);
      frame.setSize(300, 400);
      frame.setVisible(true);
   }
}
