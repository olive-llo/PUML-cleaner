/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.olivier.component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import static org.olivier.component.MyLogger.LOGGER;

/**
 *
 * @author olivier
 */
public final class MyProperties {

   /**
    * Contructeur privé
    */
   private MyProperties() {

   }

   /**
    * Read a properties file
    *
    * @param nameFile the name of the properties file
    * @return A Properties object
    */
   public static Properties loadProperties(final String nameFile) {
      final Properties prop = new Properties();
      InputStream input = null;

      try {
         // "config.properties"
         input = new FileInputStream(nameFile);

         // load a properties file
         prop.load(input);
      } catch (IOException ex) {
         LOGGER.log(Level.SEVERE, "IOException", ex);
      } finally {
         if (input != null) {
            try {
               input.close();
            } catch (IOException ex) {
               LOGGER.log(Level.SEVERE, "IOException", ex);
            }
         }
      }
      return prop;
   }

   /**
    * Write properties file
    *
    * @param properties The Properties object
    * @param nameFile The name of properties file
    */
   public static void writeProperties(final Properties properties, final String nameFile) {
      OutputStream output = null;

      try {

         output = new FileOutputStream(nameFile);

         // save properties to project root folder
         properties.store(output, null);

      } catch (IOException io) {
         LOGGER.log(Level.SEVERE, "IOException", io);
         JOptionPane.showMessageDialog(null, "Pb de sauvegarde du fichier " + nameFile + " !!");
      } finally {
         if (output != null) {
            try {
               output.close();
            } catch (IOException ex) {
               LOGGER.log(Level.SEVERE, "IOException", ex);
            }
         }

      }
   }
}
