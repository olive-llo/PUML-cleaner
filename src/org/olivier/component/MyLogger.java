/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.olivier.component;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author chantal
 */
public final class MyLogger {

   /**
    * A logger
    */
   public static final Logger LOGGER = initLogger();
   /**
    * Log dir
    */
   private static final String LOGDIR = "logs";
   /**
    * Log dir
    */
   private static final String LOGNAME = "puml";

   /**
    * Contructeur privé
    */
   private MyLogger() {

   }

   /**
    * Initialize the logger
    *
    * @return
    */
   private static Logger initLogger() {
      final Logger logger = Logger.getLogger(LOGNAME);

      final File logFolder = new File(LOGDIR);
      if (!logFolder.exists()) {
         final boolean result = logFolder.mkdir();
         if (!result) {
            logger.log(Level.SEVERE, "Not able to create log folder {0}", logFolder.getName());
         }
      }
      try {
         final FileHandler fileHandler = new FileHandler(new File(logFolder, LOGNAME + ".log").getPath(), 1024 * 1024, 3, true);
         fileHandler.setFormatter(new SimpleFormatter());
         logger.addHandler(fileHandler);
         logger.log(Level.INFO, "=====     START   =====");
      } catch (IOException ex) {
         logger.log(Level.SEVERE, MyLogger.class.getName(), ex);
      } catch (SecurityException ex) {
         logger.log(Level.SEVERE, MyLogger.class.getName(), ex);
      }

      return logger;
   }

}
