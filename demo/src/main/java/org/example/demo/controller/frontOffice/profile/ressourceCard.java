package org.example.demo.controller.frontOffice.profile;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import org.example.demo.HelloApplication;
import org.example.demo.controller.frontOffice.ressource.addDemandes;
import org.example.demo.controller.frontOffice.ressource.addRessources;
import org.example.demo.models.Ressources;
import org.example.demo.models.User;
import org.example.demo.services.ressource.RessourcesService;

import java.io.IOException;

public class ressourceCard {

    @FXML
    private Text ressourcenameText;

    @FXML
    private Text ressourcetypeText;

    private Ressources ressource;
    private RessourcesService ressourceService;

    private User currentUser = new User();

    private Runnable refreshCallback;  // Callback to notify profileController to refresh

    @FXML
    private AnchorPane deleteRessource;

    @FXML
    private AnchorPane demanderRessource;

    @FXML
    private AnchorPane updateRessource;

    @FXML
    private AnchorPane viewRessource;

    // Constructor or initialization method
    public ressourceCard() {
        this.ressourceService = new RessourcesService();
    }


    public void setRessource(Ressources r, User currentUser) {
        this.ressource = r;
        this.currentUser = currentUser;

        ressourcenameText.setText(r.getName());
        ressourcetypeText.setText(r.getType());

        // Check if the current user is the owner of the resource
        if (currentUser.getId() == ressource.getUserId()) {
            // Show the delete and update buttons for the owner
            deleteRessource.setVisible(true);
            updateRessource.setVisible(true);
            demanderRessource.setVisible(false);  // Hide "Demander" button
            viewRessource.setVisible(true);  // Hide "View" button
        } else {
            // Show the "Demander" and "View" buttons for non-owners
            deleteRessource.setVisible(false);  // Hide delete button
            updateRessource.setVisible(false);  // Hide update button
            demanderRessource.setVisible(true);  // Show "Demander" button
            viewRessource.setVisible(true);  // Show "View" button
        }
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;  // Set the callback from profileController
    }

    @FXML
    void onDeleteRessourceClicked() {
        if (ressource != null) {
            try {
                // Call the supprimer method in the service
                ressourceService.supprimer(ressource);
                System.out.println("✅ Ressource supprimée avec succès.");
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void onUpdateRessourceClicked() {
        try {
            HelloApplication.changeSceneWithController(
                    "/org/example/demo/fxml/Frontoffice/ressources/addRessource.fxml",
                    controller -> {
                        if (controller instanceof addRessources) {
                            ((addRessources) controller).setRessourcePourModification(ressource);
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onDemandeRessourceClicked() {
        try {
            HelloApplication.changeSceneWithController(
                    "/org/example/demo/fxml/Frontoffice/ressources/addDemande.fxml",
                    controller -> {
                        if (controller instanceof addDemandes) {
                            ((addDemandes) controller).setRessource(ressource);
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
