/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.olivier.ihm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.apache.commons.io.FilenameUtils;
import static org.olivier.component.MyLogger.LOGGER;
import org.olivier.component.MyProperties;
import org.olivier.component.NodeInfo;
import org.olivier.component.jtreecheckbox.check.CheckTreeManager;
import org.olivier.util.Util;

/**
 *
 * @author o.leliboux
 */
public class FramePUMLCleanerControler {

    private final static JFileChooser fileChooser = new JFileChooser();
    private final static FileFilter pumlFilter = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory() || pathname.getName().endsWith("puml");
        }

        @Override
        public String getDescription() {
            return "Fichier PUML";
        }
    };
    private final String fichierProperties = "pumlcleaner.properties";
    private final String nomFichierPropertie = "fichierpuml";
    private final String listeCategories = "categories";
    private final Properties myProperties;
    private String[] lines;
    private final String[] categories;// = {"abstract", "annotation", "class", "enum", "interface"};
    private final CheckTreeManager checkTreeManager;

    public FramePUMLCleanerControler(JTree jTreeFiltre) {
        myProperties = MyProperties.loadProperties(fichierProperties);        
        categories = getCategories().split(",");
        checkTreeManager = new CheckTreeManager(jTreeFiltre, true, null);
    }

    /**
     * Sélectionner un fichier PUML
     *
     * @param jTextFieldFichier le composant contenant le nom du fichier PUML en
     * cours d'utilisation
     */
    public void selectInputFile(JTextField jTextFieldFichier) {
        final File fichier = new File(jTextFieldFichier.getText());
        if (fichier.exists()) {
            fileChooser.setCurrentDirectory(fichier.getParentFile());
        }
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(pumlFilter);
        fileChooser.addChoosableFileFilter(pumlFilter);
        fileChooser.setDialogTitle("Choisir un fichier PUML ...");

        final int returnVal = fileChooser.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            if (file.exists()) {
                jTextFieldFichier.setText(file.getPath());
                myProperties.setProperty(nomFichierPropertie, jTextFieldFichier.getText());
            }
        }
    }

    /**
     * Récupère le nom du fichier conservé dans les properties
     * @return Le nom du fichier sauvegardé
     */
    public String getNomFichier() {
        return myProperties.getProperty(nomFichierPropertie, "Choisir un fichier ...");
    }

    /**
     * Récupère les noms des catégories sauvegardées dans le fichier properties
     * @return Les noms des catégories sauvegardées
     */
    private String getCategories() {
        return myProperties.getProperty(listeCategories, "abstract class,annotation,class,enum,interface");
    }

    /**
     * Lire le fichier PUML, l'analyser et l'afficher
     *
     * @param filename Le fichier PUML
     * @param jTextAreaFichier La zone d'affichage du fichier
     * @param jTreeFiltre La zone de filtrage
     */
    public void analyseInputFile(String filename, JTextArea jTextAreaFichier, JTree jTreeFiltre) {
        readInputFile(filename, jTextAreaFichier);

        analyserFichier(jTextAreaFichier, jTreeFiltre);
    }

    /**
     * Filtrer et sauver le fichier PUML dans un nouveau fichier
     *
     * @param filename Le fichier PUML
     * @param jTextAreaFichier La zone d'affichage du fichier
     * @param jTreeFiltre La zone de filtrage
     */
    public void filterInputFile(String filename, JTextArea jTextAreaFichier, JTree jTreeFiltre) {
        filtrerFichier(jTextAreaFichier, jTreeFiltre);

        saveFile(filename, jTextAreaFichier);
    }

    /**
     * Analyser le fichier PUML et initialiser le filtre
     *
     * @param jTextAreaFichier Le contenu du fichier PUML
     * @param jTreeFiltre Le filtre à initialiser
     */
    private void analyserFichier(JTextArea jTextAreaFichier, JTree jTreeFiltre) {
        lines = jTextAreaFichier.getText().split("[\\r\\n]+");

        final DefaultMutableTreeNode newFilter = initaliserNodes();

        // parcours de chaque ligne du fichier pour analyse
        for (String line : lines) {
            if (line.contains(">")) {
                break;
            } else {
                String categ = "";
                String noeud = null;
                // recherche de la catégorie
                for (String categorie : categories) {
                    if (line.contains(categorie)) {
                        categ = categorie;
                        noeud = line.replaceFirst(categorie, "").replaceAll(" ", "");
                        // ajout du noeud dans la bonne catégorie de l'arbre
                        for (int i = 0; i < newFilter.getChildCount(); i++) {
                            final DefaultMutableTreeNode dmtr = (DefaultMutableTreeNode) newFilter.getChildAt(i);
                            if (dmtr.toString().equals(categ)) {
                                final DefaultMutableTreeNode node = new DefaultMutableTreeNode(new NodeInfo(noeud));
                                dmtr.add(node);
                            }
                        }
                        break;
                    }
                }
            }
        }
        jTreeFiltre.setModel(new DefaultTreeModel(newFilter));
        jTextAreaFichier.requestFocus();
    }

    /**
     * Filtre le fichier PUML stocké dans le textArea à partir du filtre contenu
     * dans le JTree
     *
     * @param jTextAreaFichier
     * @param jTreeFiltre
     */
    private void filtrerFichier(JTextArea jTextAreaFichier, JTree jTreeFiltre) {
        jTextAreaFichier.setText("");
        // Liste des noeuds à supprimer
        final DefaultMutableTreeNode nodeToDelete = getNodeToDelete((DefaultMutableTreeNode) jTreeFiltre.getModel().getRoot(),
                checkTreeManager.getSelectionModel().getSelectionPaths());

        // traitement des lignes
        for (String line : lines) {
            if (isLinePrintable(line, nodeToDelete)) {
                jTextAreaFichier.append(line + "\n");
            }
        }
        jTextAreaFichier.requestFocus();
    }

    /**
     * Analyse une ligne et détermine si elle est imprimable ou pas
     *
     * @param line La ligne à analyser
     * @param nodeToDelete Liste des noeuds à supprimer
     * @return True si la ligne doit etre imprimée, false sinon
     */
    private boolean isLinePrintable(String line, DefaultMutableTreeNode nodeToDelete) {
        boolean isPrintable = true;

        // parcours des catégories
        for (int i = 0; i < nodeToDelete.getChildCount(); i++) {
            final TreeNode categ = nodeToDelete.getChildAt(i);
            // parcours des noeuds à supprimer
            for (int j = 0; j < categ.getChildCount(); j++) {
                final DefaultMutableTreeNode noeudToDelete = (DefaultMutableTreeNode) categ.getChildAt(j);
                final String nomNoeudToDelete = noeudToDelete.toString();

                if (line.contains(nomNoeudToDelete)) {
                    isPrintable = false;
                    break;
                }
            }
            if (!isPrintable) {
                break;
            }
        }
        return isPrintable;
    }

    /**
     * Renvoi la liste des noeuds non sélectionnés
     *
     * @param allTreeNode Liste de tous les noeuds
     * @param selectedPath Chemins sélectionnés
     * @return Liste des noeuds non sélectionnés
     */
    private DefaultMutableTreeNode getNodeToDelete(
            DefaultMutableTreeNode allTreeNode,
            TreePath[] selectedPath) {
        final DefaultMutableTreeNode nodeToDelete = new DefaultMutableTreeNode("Node to delete");
      // Parcours de l'arbre pour supprimer les noeuds sélectionnés
        // Parcours des catégories  
        for (int i = 0; i < allTreeNode.getChildCount(); i++) {
            final DefaultMutableTreeNode tnCateg = (DefaultMutableTreeNode) allTreeNode.getChildAt(i);
            // les catégories sont systématiquement ajoutées
            final DefaultMutableTreeNode newTnCateg = new DefaultMutableTreeNode(tnCateg.toString());//) allTreeNode.getChildAt(i);
            nodeToDelete.add(newTnCateg);
            // parcours des noeuds
            for (int j = 0; j < tnCateg.getChildCount(); j++) {
                final DefaultMutableTreeNode tnNoeud = (DefaultMutableTreeNode) tnCateg.getChildAt(j);
                // Ajout du noeud s'il n'est pas sélectionné
                if (!isNoeudSelected(tnNoeud, selectedPath)) {
                    /* 
                     Il est impératif de créer un nouveau noeud !
                     Si on se contente d'ajouter tnNoeud dans newTnCateg, celui-ci est supprimé de tnCateg !
                     Visiblement, un noeud ne peut pas appartenir à 2 arbres différents. Cela semble logique 
                     car la définition des ancêtres risque de poser problème.
                     A approfondir quand j'aurai le temps ...
                     */
                    final DefaultMutableTreeNode newTnNoeud = new DefaultMutableTreeNode(new NodeInfo(tnNoeud.toString()));
                    newTnCateg.add(newTnNoeud);
                }
            }
        }
        return nodeToDelete;
    }

    /**
     * Indique si un noeud est sélectionné ou pas
     *
     * @param noeudATester Le noeud à tester
     * @param selectedPath La liste des noeuds sélectionnés
     * @return True si le noeud est sélectionné, false sinon
     */
    private boolean isNoeudSelected(final DefaultMutableTreeNode noeudATester, final TreePath[] selectedPath) {
        boolean isSelected = false;

        // parcours des categories
        for (TreePath path : selectedPath) {
            try {
                // Cette ligne plante quand on sélectionne une catégorie complète d'ou le try-catch !
                final DefaultMutableTreeNode unNoeudSelectionner = (DefaultMutableTreeNode) path.getPath()[2];
                isSelected = noeudATester.toString().equals(unNoeudSelectionner.toString());
            } catch (ArrayIndexOutOfBoundsException ex) {
                // Quand on choisit une catégorie complète, il faut parcourir les noeuds de cette manière :
                // recup de la categorie
                final DefaultMutableTreeNode categ = (DefaultMutableTreeNode) path.getLastPathComponent();
                // parcours des noeuds dans la catégorie
                for (int j = 0; j < categ.getChildCount(); j++) {
                    final TreeNode unNoeudSelectionner = categ.getChildAt(j);
                    isSelected = noeudATester.toString().equals(unNoeudSelectionner.toString());
                    if (isSelected) {
                        break;
                    }
                }
            }
            // on sort de la boucle dès qu'on a la réponse
            if (isSelected) {
                break;
            }
        }

        return isSelected;
    }

    /**
     * Initialiser un arbre avec les catégories
     *
     * @return L'arbre initialisé
     */
    public DefaultMutableTreeNode initaliserNodes() {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("PUML filter");
        DefaultMutableTreeNode category = null;
        DefaultMutableTreeNode node = null;

        for (String categorie : categories) {
            category = new DefaultMutableTreeNode(categorie);
            top.add(category);
        }

//      //original Tutorial
//      node = new DefaultMutableTreeNode(new NodeInfo("The Java Tutorial: A Short Course on the Basics"));
//      category.add(node);
//
//      //Tutorial Continued
//      node = new DefaultMutableTreeNode(new NodeInfo("The Java Tutorial Continued: The Rest of the JDK"));
//      category.add(node);
//
//      //Swing Tutorial
//      node = new DefaultMutableTreeNode(new NodeInfo("The Swing Tutorial: A Guide to Constructing GUIs"));
//      category.add(node);
//
//      //...add more books for programmers...
//      category = new DefaultMutableTreeNode("Books for Java Implementers");
//      top.add(category);
//
//      //VM
//      node = new DefaultMutableTreeNode(new NodeInfo("The Java Virtual Machine Specification"));
//      category.add(node);
//
//      //Language Spec
//      node = new DefaultMutableTreeNode(new NodeInfo("The Java Language Specification"));
//      category.add(node);
        return top;
    }

    /**
     * Close the windows
     */
    public void closeWindows() {
        // transformation des categories en String
        final StringBuffer categ = new StringBuffer("");

        for (int i = 0; i < categories.length - 1; i++) {
            categ.append(categories[i]);
            categ.append(",");
        }
        categ.append(categories[categories.length - 1]);

        myProperties.setProperty(listeCategories, categ.toString());
        MyProperties.writeProperties(myProperties, fichierProperties);
        LOGGER.log(Level.INFO, "=====     STOP    =====");
    }

    /**
     * Sauve le contenu du Text Area dans un nouveau fichier
     *
     * @param filename Le nom du fichier source
     */
    private void saveFile(String filename, JTextArea jTextAreaFichier) {
        // sauvegarde du contenu du text area dans un nouveau fichier
        final File file = new File(filename);
        if (file.exists()) {
            // création du nouveau nom du fichier
            final String newfilename = getNewName(filename);;
            // sauvegarde
            final FileWriter writer;
            try {
                writer = new FileWriter(newfilename);
                final BufferedWriter bw = new BufferedWriter(writer);
                jTextAreaFichier.write(bw);
                bw.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "IOException", ex);
            }
        } else {
            JOptionPane.showMessageDialog(null, filename + " n'existe pas !", "Erreur", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Fourni un nouveau nom de fichier à partir de l'ancien
     *
     * @param filename L'ancien nom de fichier
     * @return Le nouveau nom de fichier
     */
    private String getNewName(String filename) {
        return FilenameUtils.getFullPath(filename) + FilenameUtils.getBaseName(filename) + "_clean." + FilenameUtils.getExtension(filename);
    }

    /**
     * Charge le fichier dans un Text Area
     *
     * @param filename Le fichier à charger
     */
    private void readInputFile(String filename, JTextArea jTextAreaFichier) {
        final FileReader reader;
        try {
            reader = new FileReader(filename);
            final BufferedReader br = new BufferedReader(reader);
            jTextAreaFichier.read(br, null);
            br.close();
            jTextAreaFichier.requestFocus();
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "FileNotFoundException", ex);
            JOptionPane.showMessageDialog(null, filename + " n'existe pas !", "Erreur", JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "IOException", ex);
        }
    }

    /**
     * Sélectionne les noeuds dans l'arbre de filtrage corespondant au motif
     * @param motif Le motif pour sélectionner les noeuds
     * @param jTreeFiltre L'arbre de filtrage
     */
    public void selectionnerNoeud(String motif, JTree jTreeFiltre) {
        // TODO : à finir

        TreePath[] allPath = Util.getPaths(jTreeFiltre, true);
        // parcours des categories
        for (TreePath path : allPath) {
            // recup de la racine
            final DefaultMutableTreeNode root = (DefaultMutableTreeNode) path.getLastPathComponent();

            // parcours des catégories
            for (int j = 0; j < root.getChildCount(); j++) {
                final TreeNode categ = root.getChildAt(j);
                
                // parcours des noeuds
                for (j = 0; j < categ.getChildCount(); j++) {
                    final TreeNode unNoeudSelectionner = categ.getChildAt(j);
                    if (!unNoeudSelectionner.toString().contains(motif)) {

                    }
                }
            }
        }    
        checkTreeManager.getSelectionModel().setSelectionPaths(allPath);
    }
}
