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
package org.olivier.component.jtreecheckbox.check;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ActionMapUIResource;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;

/**
 * Maintenance tip - There were some tricks to getting this code working:
 *
 * 1. You have to overwite addMouseListener() to do nothing 2. You have to add a
 * mouse event on mousePressed by calling super.addMouseListener() 3. You have
 * to replace the UIActionMap for the keyboard event "pressed" with your own
 * one. 4. You have to remove the UIActionMap for the keyboard event "released".
 * 5. You have to grab focus when the next state is entered, otherwise clicking
 * on the component won't get the focus. 6. You have to make a TristateDecorator
 * as a button model that wraps the original button model and does state
 * management.
 *
 * modifed version of : http://www.javaspecialists.co.za/archive/Issue082.html
 *
 * @author Dr. Heinz M. Kabutz
 * @author Santhosh Kumar T
 */
public class TristateCheckBox extends JCheckBox {

   private final TristateDecorator model;

   public TristateCheckBox(String text, Icon icon, Boolean initial) {
      super(text, icon);
      // Add a listener for when the mouse is pressed
      super.addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent e) {
            grabFocus();
            model.nextState();
         }
      });
      // Reset the keyboard action map
      ActionMap map = new ActionMapUIResource();
      map.put("pressed", new AbstractAction() {      //NOI18N
         @Override
         public void actionPerformed(ActionEvent e) {
            grabFocus();
            model.nextState();
         }
      });
      map.put("released", null);                     //NOI18N
      SwingUtilities.replaceUIActionMap(this, map);
      // set the model to the adapted model
      model = new TristateDecorator(getModel());
      setModel(model);
      setState(initial);
   }

   public TristateCheckBox(String text, Boolean initial) {
      this(text, null, initial);
   }

   public TristateCheckBox(String text) {
      this(text, null);
   }

   public TristateCheckBox() {
      this(null);
   }

   /**
    * No one may add mouse listeners, not even Swing!
     * @param l the mouse listener to add
    */
   @Override
   public void addMouseListener(MouseListener l) {
   }

   /**
    * Set the new state to either SELECTED, NOT_SELECTED or DONT_CARE. If state
    * == null, it is treated as DONT_CARE.
     * @param state the state
    */
   public void setState(Boolean state) {
      model.setState(state);
   }

   /**
    * Return the current state, which is determined by the selection status of
    * the model.
     * @return the state
    */
   public Boolean getState() {
      return model.getState();
   }

   /**
    * Exactly which Design Pattern is this? Is it an Adapter, a Proxy or a
    * Decorator? In this case, my vote lies with the Decorator, because we are
    * extending functionality and "decorating" the original model with a more
    * powerful model.
    */
   private class TristateDecorator implements ButtonModel {

      private final ButtonModel other;

      private TristateDecorator(ButtonModel other) {
         this.other = other;
      }

      private void setState(Boolean state) {
         if (state == Boolean.FALSE) {
            other.setArmed(false);
            setPressed(false);
            setSelected(false);
         } else if (state == Boolean.TRUE) {
            other.setArmed(false);
            setPressed(false);
            setSelected(true);
         } else {
            other.setArmed(true);
            setPressed(true);
            setSelected(true);
         }
      }

      /**
       * The current state is embedded in the selection / armed state of the
       * model.
       *
       * We return the SELECTED state when the checkbox is selected but not
       * armed, DONT_CARE state when the checkbox is selected and armed (grey)
       * and NOT_SELECTED when the checkbox is deselected.
       */
      private Boolean getState() {
         if (isSelected() && !isArmed()) {
            // normal black tick
            return Boolean.TRUE;
         } else if (isSelected() && isArmed()) {
            // don't care grey tick
            return null;
         } else {
            // normal deselected
            return Boolean.FALSE;
         }
      }

      /**
       * We rotate between NOT_SELECTED, SELECTED and DONT_CARE.
       */
      private void nextState() {
         Boolean current = getState();
         if (Objects.equals(current, Boolean.FALSE)) {
            setState(Boolean.TRUE);
         } else if (Objects.equals(current, Boolean.TRUE)) {
            setState(null);
         } else if (current == null) {
            setState(Boolean.FALSE);
         }
      }

      /**
       * Filter: No one may change the armed status except us.
       */
      @Override
      public void setArmed(boolean b) {
      }

      public boolean isFocusTraversable() {
         return isEnabled();
      }

      /**
       * We disable focusing on the component when it is not enabled.
       */
      @Override
      public void setEnabled(boolean b) {
//            setFocusable(b);
         other.setEnabled(b);
      }

      /**
       * All these methods simply delegate to the "other" model that is being
       * decorated.
       */
      @Override
      public boolean isArmed() {
         return other.isArmed();
      }

      @Override
      public boolean isSelected() {
         return other.isSelected();
      }

      @Override
      public boolean isEnabled() {
         return other.isEnabled();
      }

      @Override
      public boolean isPressed() {
         return other.isPressed();
      }

      @Override
      public boolean isRollover() {
         return other.isRollover();
      }

      @Override
      public void setSelected(boolean b) {
         other.setSelected(b);
      }

      @Override
      public void setPressed(boolean b) {
         other.setPressed(b);
      }

      public void setRollover(boolean b) {
         other.setRollover(b);
      }

      public void setMnemonic(int key) {
         other.setMnemonic(key);
      }

      public int getMnemonic() {
         return other.getMnemonic();
      }

      public void setActionCommand(String s) {
         other.setActionCommand(s);
      }

      public String getActionCommand() {
         return other.getActionCommand();
      }

      public void setGroup(ButtonGroup group) {
         other.setGroup(group);
      }

      public void addActionListener(ActionListener l) {
         other.addActionListener(l);
      }

      public void removeActionListener(ActionListener l) {
         other.removeActionListener(l);
      }

      public void addItemListener(ItemListener l) {
         other.addItemListener(l);
      }

      public void removeItemListener(ItemListener l) {
         other.removeItemListener(l);
      }

      public void addChangeListener(ChangeListener l) {
         other.addChangeListener(l);
      }

      public void removeChangeListener(ChangeListener l) {
         other.removeChangeListener(l);
      }

      public Object[] getSelectedObjects() {
         return other.getSelectedObjects();
      }
   }

//   public static void main(String args[]) throws Exception {
//      UIManager.setLookAndFeel(
//              UIManager.getSystemLookAndFeelClassName());
//      JFrame frame = new JFrame("TristateCheckBoxTest");     //NOI18N
//      frame.getContentPane().setLayout(new GridLayout(0, 1, 5, 5));
//      final TristateCheckBox swingBox = new TristateCheckBox(
//              "Testing the tristate checkbox");  //NOI18N
//      swingBox.setMnemonic('T');
//      frame.getContentPane().add(swingBox);
//      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//      frame.pack();
//      frame.setVisible(true);
//   }
}
