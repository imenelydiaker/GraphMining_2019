package frontEnd;

import back_end.*;
import com.jfoenix.controls.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import javafx.scene.layout.HBox;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.TreeSet;

import static java.lang.Integer.parseInt;

public class Controller {
    /* Calsse Controller : cette classe relie le code du backend à l'interface utilisateur.
     * Elle modifie aussi les valeurs des éléments de la GUI.
     * Le positionnement des éléments de la GUI est détaillé dans le fichier FXML.
     * Le style de ces éléments est détaillé dans la feuille de style : style.css */

    /*******************************************************************************************************************************/
    /****************************************************ELEMENTS DE LA GUI - FXML**************************************************/
    /*******************************************************************************************************************************/
    @FXML
    JFXRadioButton rdClimat, rdFoot;

    @FXML
    TableView table, tableAffichage;

    @FXML
    TableColumn col1, col2, col3, col4, col5, col6, col7;

    @FXML
    JFXButton bVisualiser, bUsersClustering, bImport, bAnnuler, bUsersCentraux;

    @FXML
    Label lbOrdre, lbVolume, lbDiametre, lbDegreMoy, lbProgressStatus, lbSelectedDataset, lbSelectedDatasetCl, lbSelectedDatasetVl;

    @FXML
    AnchorPane pnClustering, pnAffichage, pnCalculs, pnImport, pnProgress;

    @FXML
    JFXProgressBar importProgress;

    @FXML
    ProgressIndicator progressIndicator;

    @FXML
    JFXTextArea txtDescriptionDonnees;

    @FXML
    HBox menuBox;

    @FXML
    ImageView imgGrapheClustering;

    @FXML
    JFXTextField txtNbUsersCentraux, txtNbCommunautes, txtPourcentage;

    /*******************************************************************************************************************************/
    /****************************************************AUTRES VARIABLES UTILES****************************************************/
    /*******************************************************************************************************************************/
    private byte dataset = 0; //1: climat 2 : foot
    BaseDeTweet bd = null;
    private final int nbTweetsClimat = 1977769, nbTweetsFoot = 899597; //Nombre de lignes par fichier
    private final String pathClimat = "src/data/climat.txt", pathFoot = "src/data/foot.txt"; //Chemin des fichiers de données
    private final String
            //description du jeu de données Foot
            descriptionFoot = "Ce jeu de données met à votre disposition :\n- 899597 tweets.\n- Les tweets sélectionnés contiennent les mots clés suivants : " +
            "\"climat\", \"climatique\", \"environnement\", \"environnemental\" et \"environnementaux\".\n- Les tweets datent du 02/09/2019.\n" +
            "- Chaque tweet est présenté sous la forme suivante : \n\tIDTweet\tUserID\tDate de publilcation\tTexte\tRetweeterID\n" +
            "\tOù :\n\t\tIDTweet : représente le numéro d'identification du tweet.\n" +
            "\t\tUserID : représente le nom d'utilisateur de la personne qui a tweeté le tweet courant.\n" +
            "\t\tDate de publilcation : représente la date de publication du tweet.\n" +
            "\t\tTexte : contient le contenu du tweet, hashtag compris.\n" +
            "\t\tRetweeterID : représente le nom d'utilisateur de la personne qui a retweeté le tweet courant.",
            //description du jeu de données Climat
            descriptionClimat = "Ce jeu de données met à votre disposition :\n- 1977769 tweets.\n- Les tweets sélectionnés contiennent les mots clés suivants : " +
            "\"foot\", \"football\", \"#WWC2019\", \"#CM2019\", \"#FootFeminin\" et \"#FIFAWWC\".\n" +
            "- Les tweets ont été récoltés durant la période du 21/06/2019 au 10/07/2019 pendant la phase ﬁnale de la coupe du monde féminine.\n" +
            "- Chaque tweet est présenté sous la forme suivante : \n\tIDTweet\tUserID\tDate de publilcation\tTexte\tRetweeterID\n" +
            "\tOù :\n\t\tIDTweet : représente le numéro d'identification du tweet.\n" +
            "\t\tUserID : représente le nom d'utilisateur de la personne qui a tweeté le tweet courant.\n" +
            "\t\tDate de publilcation : représente la date de publication du tweet.\n" +
            "\t\tTexte : contient le contenu du tweet, hashtag compris.\n" +
            "\t\tRetweeterID : représente le nom d'utilisateur de la personne qui a retweeté le tweet courant.";

    /*******************************************************************************************************************************/
    /********************************************************FONCTIONS DE CALCUL*******************************************************/
    /*******************************************************************************************************************************/
    //Action du bouton importer
    public void importerDonnees() {
        String path = ""; //Chemin du fichier
        int nbTweets=0; //Nombre de tweets du fichier

        //Après la sélection du dataset :
        if(dataset==1){
            path = pathClimat;
            nbTweets = nbTweetsClimat;
        }
        else{
            if(dataset==2){
                path = pathFoot;
                nbTweets= nbTweetsFoot;
            }
            else
                errorDialog("Dataset non sélectionné !", "Veuillez sélectionner un dataset à importer.");
        }

        if(dataset>0) {//Si un dataset est séléctionné alors lancer l'import
            //Modification des propriétés des éléments de la page d'import des données
            bImport.setDisable(true);
            bAnnuler.setDisable(false);
            menuBox.setDisable(true);

            //Création de la base de tweets
            bd = new BaseDeTweet();

            //Import des données
            ImportTask importTask = new ImportTask(path, nbTweets); // création de la tâche d'import des données

            //Modification des propriétés des éléments de la page d'import des données
            importProgress.setVisible(true);
            importProgress.setProgress(0);
            lbProgressStatus.setVisible(true);
            importProgress.progressProperty().unbind();
            importProgress.progressProperty().bind(importTask.progressProperty()); //Relier la progress bar à la progression de la tâche d'import des données
            lbProgressStatus.textProperty().unbind();
            lbProgressStatus.textProperty().bind(importTask.messageProperty()); //Relier e message à afficher à la progression de la tâche d'import des données

            new Thread(importTask).start(); // création et lancement du thread d'import des données

            //Lorsque la tâche d'import est terminée :
            importTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
                bd.setTweets(importTask.getValue());//Quand l'import est terminé : on récupère le résultat
                importDefaultPane("Chargement des données terminé."); //Retour à la page par défaut
                informationDialog("Données importées !", "Vous pouvez à présent afficher les données, afficher les statistiques ou faire du Clustering.");
            });

            //Annulation de l'import
            bAnnuler.setOnAction(event -> {
                importTask.cancel(true);
                bd = null;
                importDefaultPane("Chargement des données annulé."); //Retour à la page par défaut
            });
        }

    }

    //Action du pane des calculs : calcul du volume, diamètre, ordre etc.
    private void calculs(){
        if(bd!=null) {
            //Modification des propriétés des éléments de la page de calcul des statistiques
            menuBox.setDisable(true);
            pnProgress.setVisible(true);

            //Classe  anonyme : création de la tâche qui fait les calculs
            Task calculsTask = new Task() {
                @Override
                protected Object call() {
                    bd.calculs();
                    return null;
                }
            };

            //Suivi de la progression de la tâche de calcul des statistiques
            progressIndicator.setProgress(0);
            progressIndicator.progressProperty().unbind();
            progressIndicator.progressProperty().bind(calculsTask.progressProperty()); //Relier la progress bar à la progression de la tâche de calcul

            new Thread(calculsTask).start(); //Lancer le thread des calculs

            pnCalculs.setDisable(true); //désactiver le panel des calculs jusqu'à la fin de la tâche

            //A la fin de la tâche calculTask (fin des calculs) :
            calculsTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
                afficherDataset(lbSelectedDataset); //Afficher le nom du dataset sur lequel ont été effectués les calculs
                afficherCalculs(); //Afficher les résultats des caculs effectués
                afficherUsersCentraux(); //Afficher les utilisateurs centraux
                calculsDefaultPane(); //Retour aux paramètres par défaut de la page
            });
        }
        else
            errorDialog("Données non importées !", "Veuillez importer les données avant de procéder aux calculs.");
    }

    //Récupérer la liste des utilistauers centraux à partir d'un nombre donné
    private TreeSet<Centrality> calculUsersCentraux(String nbUsers){
        int nb = checkNombre(nbUsers); //vérifier si le nombre saisiest correct
        System.out.println("calcul : "+nb);
        if(nb>0)
            return bd.UserCentraux(nb); //récupérer la liste des utilistauers centraux
        return null;
    }

    //Vérifier la saisie d'un nombre
    private int checkNombre(String nb){
        if(nb.isEmpty()) //Si rien n'est entré et le champ de saisie est vide
            return 5; //Valeur par défaut
        else {
            // Vérification sur la saisie
            try{
                int nombre=parseInt(nb);
                if(nombre<0 || nombre>10000){
                    return -1;
                }
                return nombre;
            }
            catch (NumberFormatException e){
                return -1;
            }
        }
    }

    //Action du bouton : Afficher le graphe simplifié
    public void clustering(){
        if(bd!=null) {
            if(bd.getCentrality()!=null) {
                TreeSet<Centrality> communautes = calculUsersCentraux(txtNbCommunautes.getText()); //vérifier la saisie et récupérer les utilisateurs centraux
                final int nbPercentage=checkNombre(txtPourcentage.getText()); //vérifier la saisie
                //Si le nombre de communautés saisi est correct alors : on lance le clustering
                if (communautes != null && nbPercentage!=-1 && nbPercentage<100) {
                    //Modification des propriétés des éléments de la page de clustering
                    menuBox.setDisable(true);
                    pnProgress.setVisible(true);
                    pnClustering.setDisable(true);

                    //Classe  anonyme : création de la tâche qui fait le clustering
                    Task clusteringTask = new Task() {
                        @Override
                        protected Object call() throws Exception { //Créer le graphe simplifié
                            new Clustering(communautes, bd.getBaseLink(),nbPercentage);
                            return null;
                        }
                    };

                    //Suivi de la progression de la tâche de clustering
                    progressIndicator.setProgress(0);
                    progressIndicator.progressProperty().unbind();
                    progressIndicator.progressProperty().bind(clusteringTask.progressProperty()); //Relier la progress bar à la progression de la tâche de clustering

                    new Thread(clusteringTask).start(); //Lancer le thread du clustering

                    //A la fin de la tâche
                    clusteringTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
                        afficherDataset(lbSelectedDatasetCl); //Afficher le nom du dataset sur lequel a été effectué le clustering

                        // Affichage du graphe simplifié dans la GUI
                        File imgFile = new File("graph.png");
                        try {
                            imgGrapheClustering.setImage(new Image(imgFile.toURI().toURL().toString())); //Ajouter l'image à l'imageView de la GUI
                            imgGrapheClustering.setOnMouseClicked(event1 -> { //Evénement de clique sur l'image
                                try {
                                    Desktop.getDesktop().open(imgFile);
                                } catch (IOException e) {
                                    errorDialog("Fichier introuvable !", "Le fichier contenant l'image est introuvable.");
                                }
                            });
                        } catch (MalformedURLException e) {
                            errorDialog("Image introuvable !", "L'image est introuvable.");
                        }
                        //Retour au panel par défaut
                        pnClustering.setDisable(false);
                        progressIndicator.progressProperty().unbind();
                        progressIndicator.setProgress(0);
                        pnProgress.setVisible(false);
                        menuBox.setDisable(false);
                    });
                }
                else
                    errorDialog("Données saisies incorrect ! ", "Entrez des nombres valide.");
            }
            else
                errorDialog("Graphe pas encore construit !", "Veuillez construire le graphe dans l'onglet \"Statistiques\" avant de procéder au Clustering.");
        }
        else
            errorDialog("Données non importées !", "Veuillez importer les données avant de procéder aux calculs.");
    }

    //Afficher le nom du dataset séléctionné dans les différents Labels des différentes pages
    private void afficherDataset(Label label){
        if(dataset==1) label.setText("Climat");
        else label.setText("Foot");
    }

    /*******************************************************************************************************************************/
    /*****************************************FONCTIONS QUI MODIFIENT LES ELEMENTS DE LA GUI****************************************/
    /*******************************************************************************************************************************/
    //Page d'import par défaut
    private void importDefaultPane(String message){
        bImport.setDisable(false);
        menuBox.setDisable(false);
        bAnnuler.setDisable(true);
        importProgress.progressProperty().unbind();
        importProgress.setProgress(0);
        importProgress.setVisible(false);
        lbProgressStatus.textProperty().unbind();
        lbProgressStatus.setText(message);
    }

    //Page de calculs par défaut
    private void calculsDefaultPane(){
        progressIndicator.progressProperty().unbind();
        progressIndicator.setProgress(0);
        pnProgress.setVisible(false);
        menuBox.setDisable(false);
        pnCalculs.setDisable(false);
    }

    //Affichage des calculs sur la GUI
    private void afficherCalculs(){
        DecimalFormat df = new DecimalFormat("0.00");
        lbOrdre.setText(String.valueOf(bd.getOrdre()));
        lbDegreMoy.setText(String.valueOf(df.format(bd.getDegreeMoyen())));
        lbVolume.setText(String.valueOf(bd.getVolume()));
        if (bd.getDiametre() == Double.POSITIVE_INFINITY)
            lbDiametre.setText("+∞");
        else
            lbDiametre.setText(String.valueOf(bd.getDiametre()));
    }
    
    //Sélection du jeu de données
    public void selectedDataset(){
        if(rdClimat.isSelected()) {
            dataset = 1;
            txtDescriptionDonnees.setText(descriptionClimat);
        }
        else {
            dataset = 2;
            txtDescriptionDonnees.setText(descriptionFoot);
        }
    }

    //Affichage des users centraux
    public void afficherUsersCentraux() {
        if(bd!=null) {
            TreeSet<Centrality> tab = calculUsersCentraux(txtNbUsersCentraux.getText());
            if (tab != null) {
                ObservableList<Centrality> usersCentraux = FXCollections.observableArrayList(tab);
                table.setItems(usersCentraux);
            } else errorDialog("Nombre saisi incorrect ! ", "Entrez un nombre compris dans l'intervalle [0,10000].");
        }
        else{
            errorDialog("Données non importées !", "Veuillez importer les données avant.");
        }
    }

    //Afficher les tweets
    private void afficherTweets(){
        ObservableList<Tweet> liste = FXCollections.observableArrayList(bd.getTweets());
        tableAffichage.setItems(liste);
    }


    /*******************************************************************************************************************************/
    /******************************************************AFFICHAGE DES PANELS******************************************************/
    /*******************************************************************************************************************************/
    //Panel d'import des données
    public void importPane(){
        pnCalculs.setVisible(false);
        pnClustering.setVisible(false);
        pnAffichage.setVisible(false);
        pnImport.setVisible(true);
        bAnnuler.setDisable(true);
        lbProgressStatus.setVisible(false);
        rdFoot.setSelected(false);
        rdClimat.setSelected(false);
        txtDescriptionDonnees.setText("");
        dataset=0;
    }

    //Panel d'affichage des données importées
    public void affichagePane(){
        pnCalculs.setVisible(false);
        pnClustering.setVisible(false);
        pnImport.setVisible(false);
        pnAffichage.setVisible(true);
        if(bd!=null){
            afficherDataset(lbSelectedDatasetVl); //afficher le nom du dataset séléctionné
            afficherTweets(); //Afficher les tweets dans la table view
        }
        else
            errorDialog("Données non importées !", "Veuillez importer les données avant de procéder aux calculs.");
    }

    //Panel d'affichage et calul des statistiques du graphe
    public void calculsPane(){
        pnAffichage.setVisible(false);
        pnClustering.setVisible(false);
        pnImport.setVisible(false);
        pnCalculs.setVisible(true);
        if(bd!=null)
            this.calculs();
        else
            errorDialog("Données non importées !", "Veuillez importer les données avant de procéder aux calculs.");
    }

    //Affichage du clustering
    public void clusteringPane(){
        pnAffichage.setVisible(false);
        pnImport.setVisible(false);
        pnCalculs.setVisible(false);
        pnClustering.setVisible(true);
        imgGrapheClustering.imageProperty().setValue(null);
}

    /*******************************************************************************************************************************/
    /******************************************************BOITES DE DIALOGUE*******************************************************/
    /*******************************************************************************************************************************/
    //Message d'information
    private void informationDialog(String header, String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("INFORMATION");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    //Message d'erreur
    private void errorDialog(String header, String content){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERREUR");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /*******************************************************************************************************************************/
    /********************************************************FONCTIONS FXML*********************************************************/
    /*******************************************************************************************************************************/
    //Initialisation des TableView de la GUI
    @FXML
    public void initialize() {
        //Initialisation de la table d'affichage des utilisateurs centraux
        col1.setCellValueFactory(new PropertyValueFactory<Centrality, String>("nom"));
        col2.setCellValueFactory(new PropertyValueFactory<Centrality, String>("poids"));
        //Initialisation de la table d'affichage des tweets
        col3.setCellValueFactory(new PropertyValueFactory<Tweet, String>("identifiant"));
        col4.setCellValueFactory(new PropertyValueFactory<Tweet, String>("tweeter"));
        col5.setCellValueFactory(new PropertyValueFactory<Tweet, String>("date"));
        col6.setCellValueFactory(new PropertyValueFactory<Tweet, String>("texte"));
        col7.setCellValueFactory(new PropertyValueFactory<Tweet, String>("retweeter"));
    }
}
