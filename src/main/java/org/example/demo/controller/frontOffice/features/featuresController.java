package org.example.demo.controller.frontOffice.features;

import org.example.demo.HelloApplication;

public class featuresController {

    /**
     * Handle click on Crop Recommendation button
     */
    public void onCropRecommendationClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/features/cropRecommendation.fxml");
    }

    /**
     * Handle click on Plant Disease Detection button
     */
    public void onPlantDiseaseDetectionClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/claim/PlanteView.fxml");
    }

    /**
     * Handle click on Land Yield Prediction button
     */
    public void onLandYieldPredictionClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/claim/Predict.fxml");
    }

    /**
     * Handle click on Soil Type Detection button
     */
    public void onSoilTypeDetectionClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/claim/sol.fxml");
    }

    /**
     * Handle click on Animal Disease Detection button
     */
    public void onAnimalDiseaseDetectionClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/claim/Animal.fxml");
    }

    /**
     * Handle click on Fruit Calorie Pattern button
     */
    public void onFruitCaloriePatternClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/claim/FruitPredictionView.fxml");
    }

    /**
     * Handle click on Fruit Disease Model button
     */
    public void onFruitDiseaseModelClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/claim/QualityPredictionView.fxml");
    }

    /**
     * Handle click on Saved Address button
     */
    public void onSavedAddressClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/features/savedAddress.fxml");
    }
}