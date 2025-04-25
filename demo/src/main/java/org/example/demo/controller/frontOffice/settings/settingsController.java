package org.example.demo.controller.frontOffice.settings;

import org.example.demo.HelloApplication;
import org.example.demo.utils.sessionManager;

public class settingsController {

    public void onAcountInformationClicked(){
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/settings/profileDetail.fxml");
    }

    public void onUpdatePasswordClicked(){
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/settings/updatePassword.fxml");
    }

    public void onLogoutClicked(){
        sessionManager.getInstance().clearSession();
        HelloApplication.changeScene("/org/example/demo/fxml/Security/Login.fxml");
    }

    public void onClaimclicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/claim/addClaim.fxml");
    }
}
