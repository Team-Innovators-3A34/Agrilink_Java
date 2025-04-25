package org.example.demo.controller.frontOffice.claim;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.example.demo.HelloApplication;
import org.example.demo.models.Reclamation;
import org.example.demo.models.Ressources;
import org.example.demo.models.TypeRec;
import org.example.demo.models.User;
import org.example.demo.services.claim.ReclamationService;
import org.example.demo.services.claim.TypeRecService;
import org.example.demo.utils.sessionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class addClaim {

    @FXML
    private Button addClaim;

    @FXML
    private ImageView backtosettings;

    @FXML
    private Text title;

    @FXML
    private TextArea contentField;

    @FXML
    private TextField imageField;

    @FXML
    private TextField titreField;

    @FXML
    private ComboBox<TypeRec> typeField;

    private User user;
    private TypeRecService typeRecService;
    private ReclamationService reclamationService;
    private Reclamation reclamationModif;


    public void setClaimPourModification(Reclamation reclamationModif) {
        this.reclamationModif=reclamationModif;
        titreField.setText(reclamationModif.getTitle());
        contentField.setText(reclamationModif.getContent());
        imageField.setText(reclamationModif.getImage());
        for (TypeRec type : typeField.getItems()) {
            if (type.getId() == reclamationModif.getType()) { // si tu compares par ID
                typeField.setValue(type);
                break;
            }
        }
        title.setText("Modifier votre reclamation");
    }

    public void initialize() {
        user = sessionManager.getInstance().getUser();
        typeRecService = new TypeRecService();
        reclamationService = new ReclamationService();

        List<TypeRec> categories = typeRecService.getAllCategories();
        typeField.getItems().addAll(categories);

        // Show only the 'nom' in the dropdown list
        typeField.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(TypeRec item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getNom());
            }
        });

        // Show 'nom' in the selected value (button)
        typeField.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(TypeRec item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getNom());
            }
        });
    }


    @FXML
    public void backtosettings(){
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/settings/settings.fxml");
    }

    @FXML
    void onAddClaimClicked(ActionEvent event) {
        String titre = titreField.getText();
        String contenu = contentField.getText();
        String image = imageField.getText();
        TypeRec selectedType = typeField.getValue();


        if (titre.isEmpty() || contenu.isEmpty() || image.isEmpty() || selectedType == null) {
            HelloApplication.error("Please fill in all fields.");
            return;
        }

        if(reclamationModif == null){
            Reclamation reclamation = new Reclamation();
            reclamation.setTitle(titre);
            reclamation.setContent(contenu);
            reclamation.setImage(image);
            reclamation.setType(selectedType.getId()); // full object
            reclamation.setIdUser(user.getId());
            reclamation.setDate(LocalDateTime.now());
            reclamation.setStatus("En cours");
            reclamation.setMailUser(user.getEmail());
            reclamation.setNomUser(user.getNom()+" "+ user.getPrenom());
            reclamation.setPriorite(0);
            reclamation.setArchive("non");
            reclamation.setTypeNom(selectedType.getNom());
            if(reclamationService.ajouterReclamation(reclamation)){
                HelloApplication.succes("Recalamtion bien Recu!");
                HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/profile/profile.fxml");

            }else {
                HelloApplication.error("Reclamation non valide");
            }
        }else {
            reclamationModif.setTitle(titre);
            reclamationModif.setContent(contenu);
            reclamationModif.setImage(image);
            reclamationModif.setType(selectedType.getId());
            if(reclamationService.modifierReclamation(reclamationModif)){
                HelloApplication.succes("Recalamtion bien Modifie!");
                HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/profile/profile.fxml");

            }else {
                HelloApplication.error("Reclamation non valide");
            }
        }
    }

}
