package org.example.demo.controller.frontOffice;

import org.example.demo.HelloApplication;

public class sideBarController {
    public void onParamtereClicked(){
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/settings/settings.fxml");
    }
    public void onProfileClicked(){
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/profile/profile.fxml");
    }

}
