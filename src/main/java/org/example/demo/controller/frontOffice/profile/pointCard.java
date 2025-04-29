package org.example.demo.controller.frontOffice.profile;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import org.example.demo.HelloApplication;
import org.example.demo.controller.frontOffice.pointRecyclage.addRecyclingPoint;
import org.example.demo.models.User;
import org.example.demo.models.recyclingpoint;
import org.example.demo.services.recyclingpoint.recyclingpointService;

import java.io.IOException;

public class pointCard {

    @FXML
    private Text pointNameText;

    @FXML
    private Text pointTypeText;

    @FXML
    private AnchorPane deletePoint;

    @FXML
    private AnchorPane updatePoint;

    @FXML
    private AnchorPane viewPoint;

    private recyclingpoint point;
    private User currentUser = new User();
    private recyclingpointService pointService;

    private Runnable refreshCallback;

    @FXML
    private ImageView pointImage;

    public pointCard() {
        this.pointService = new recyclingpointService();
    }

    public void setPoint(recyclingpoint p, User currentUser) {
        this.point = p;
        this.currentUser = currentUser;

        pointNameText.setText(p.getNom());
        pointTypeText.setText(p.getType());

        // Chargement de l'image depuis le dossier resources/images
        try {
            String imagePath = "/images/" + p.getImage(); // ex : "photo1.png"
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            pointImage.setImage(image);
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement de l'image: " + e.getMessage());
            // Optionnel : mettre une image par défaut si l'image est manquante
            pointImage.setImage(new Image(getClass().getResourceAsStream("/images/default.png")));
        }

        if (currentUser.getId() == point.getOwner_id()) {
            deletePoint.setVisible(true);
            updatePoint.setVisible(true);
        } else {
            deletePoint.setVisible(false);
            updatePoint.setVisible(false);
        }

        viewPoint.setVisible(true);
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    @FXML
    void onDeletePointClicked() {
        if (point != null) {
            try {
                pointService.supprimer(point);
                System.out.println("🗑️ Point supprimé !");
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void onUpdatePointClicked() {
        try {
            HelloApplication.changeSceneWithController(
                    "/org/example/demo/fxml/Frontoffice/pointRecyclage/addRecyclingpoint.fxml",
                    controller -> {
                        if (controller instanceof addRecyclingPoint) {
                            ((addRecyclingPoint) controller).setPointPourModification(point);
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onViewPointClicked() {
        try {
            // Scene ou modal à créer pour les détails
            System.out.println("🔍 Voir les détails du point : " + point.getOwner_id());
            // Ajoute ici la navigation vers une vue de détails si souhaité
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
