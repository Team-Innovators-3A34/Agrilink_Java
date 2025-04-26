package org.example.demo.controller.frontOffice.profile;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import org.example.demo.models.Ressources;
import org.example.demo.models.User;
import org.example.demo.services.ressource.RessourcesService;

import java.io.IOException;
import java.util.List;

public class userProfileController {

    @FXML
    private Label dateLabel;

    @FXML
    private Button deleteBtn;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Button detailsBtn;

    @FXML
    private Button downloadPdfButton;

    @FXML
    private Button editBtn;

    @FXML
    private Text emailText;

    @FXML
    private Label idLabel;

    @FXML
    private StackPane imageContainer;

    @FXML
    private Label initialLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Text nomprenomText;

    @FXML
    private GridPane pointList;

    @FXML
    private VBox postCard;

    @FXML
    private ImageView postImageView;

    @FXML
    private VBox postsContainer;

    @FXML
    private Circle profileCircle;

    @FXML
    private TabPane profilePane;

    @FXML
    private ImageView profileimage;

    @FXML
    private Tab resourceTab;

    @FXML
    private GridPane ressourceList;

    @FXML
    private Circle statusDot;

    @FXML
    private Label statusLabel;

    @FXML
    private Tab tabRecyclingPoints;

    @FXML
    private Label typeLabel;

    private User user;
    RessourcesService ressourcesService = new RessourcesService();


    public void setUser(User user) {
        //////////////////////////////////////////////////////////////////////////
        //User Load
        this.user = user;
        System.out.println(user.getId()+" "+user.getEmail());
        if (user.getRoles().equals("[\"ROLE_RECYCLING_INVESTOR\",\"ROLE_USER\"]")) {
            profilePane.getTabs().remove(resourceTab);
        }

        if (!user.getRoles().equals("[\"ROLE_RECYCLING_INVESTOR\",\"ROLE_USER\"]")) {
            profilePane.getTabs().remove(tabRecyclingPoints);
        }
        emailText.setText(user.getEmail());
        nomprenomText.setText(user.getNom()+" "+user.getPrenom());
        String imageFileName = user.getImage();
        if (imageFileName != null && !imageFileName.isEmpty()) {
            String imagePath = "/images/" + imageFileName;
            try {
                Image image = new Image(getClass().getResource(imagePath).toString());
                profileimage.setImage(image);
            } catch (NullPointerException e) {
                System.out.println("Image profile not found");
            }
        } else {
            System.out.println("Image profile not found");
        }
        //////////////////////////////////////////////////////////////////////////
        //Ressource Load
        if (!user.getRoles().equals("[\"ROLE_RECYCLING_INVESTOR\",\"ROLE_USER\"]")) {
            List<Ressources> ressources = ressourcesService.RessourceParUser(user.getId());
            System.out.println(user.getId());
            int column = 0, row = 0;
            ressourceList.getChildren().clear();
            for (Ressources ressource : ressources) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Frontoffice/profile/ressourceCard.fxml"));
                    AnchorPane card = loader.load();
                    ressourceCard controller = loader.getController();
                    controller.setRessource(ressource, user);
                    card.setPrefWidth(190);
                    card.setPrefHeight(230);
                    ressourceList.add(card, column, row);
                    GridPane.setMargin(card, new Insets(10));
                    column++;
                    if (column == 3) { column = 0; row++; }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }



}
