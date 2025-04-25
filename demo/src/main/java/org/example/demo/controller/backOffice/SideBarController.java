package org.example.demo.controller.backOffice;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import org.example.demo.HelloApplication;
import org.example.demo.utils.sessionManager;

import java.util.Arrays;
import java.util.List;

public class SideBarController {

    @FXML
    private HBox dashboardmenu;
    @FXML
    private HBox usermenu;
    @FXML
    private HBox eventmenu;
    @FXML
    private HBox ressourcemenu;
    @FXML
    private HBox recylingpointmenu;
    @FXML
    private HBox postmenu;
    @FXML
    private HBox claimmenu;

    private List<HBox> menuItems ;

    @FXML
    public void initialize() {

        if (!sessionManager.getInstance().isAdmin()) {
            Platform.runLater(() -> {
                HelloApplication.error("Access Denied: Insufficient permissions");
                sessionManager.getInstance().clearSession();
                HelloApplication.changeScene("/org/example/demo/fxml/Security/Login.fxml");
            });
            return;
        }

        menuItems = Arrays.asList(dashboardmenu, usermenu, eventmenu);
    }


    public void onLogoutButtonClick() {
        sessionManager.getInstance().clearSession();
        HelloApplication.changeScene("/org/example/demo/fxml/Security/Login.fxml");
    }

    public void onUsersClick() {
        setActiveMenu(usermenu);
        HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/user/listUsers.fxml");
    }

    public void onEventClick(){
        setActiveMenu(eventmenu);
        HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/event/listEvent.fxml");
    }

    public void onDashboardClicked() {
        setActiveMenu(dashboardmenu);
        HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/dashboard.fxml");
    }

    public void onRessourceClick() {
        setActiveMenu(ressourcemenu);
        HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/ressource/listRessource.fxml");
    }

    public void onRecyclingPointClick() {
        setActiveMenu(recylingpointmenu);
        HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/recyclingPoint/listRecyclingPoint.fxml");
    }

    public void onPostClick() {
        setActiveMenu(postmenu);
        HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/post/listPost.fxml");
    }

    public void onClaimClicked() {
        setActiveMenu(claimmenu);
        HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/claim/listClaim.fxml");
    }

    private void setActiveMenu(HBox activeBox) {
        for (HBox box : menuItems) {
            box.setStyle("-fx-background-color: transparent;"); // style par défaut
        }
        activeBox.setStyle("-fx-background-color: #e0e0e0;"); // style actif
    }
}
