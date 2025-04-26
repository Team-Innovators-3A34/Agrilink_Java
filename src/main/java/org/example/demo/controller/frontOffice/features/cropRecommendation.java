package org.example.demo.controller.frontOffice.features;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import org.example.demo.HelloApplication;
import org.example.demo.services.user.PredictionService;

public class cropRecommendation {

    @FXML
    private ImageView backtosettings;

    @FXML
    private Button predict;

    @FXML
    private Text prediction;

    @FXML
    private Text title;

    @FXML
    private TextField txtN;

    @FXML
    private TextField txtc;

    @FXML
    private TextField txth;

    @FXML
    private TextField txtk;

    @FXML
    private TextField txtmm;

    @FXML
    private TextField txtp;

    @FXML
    private TextField txtph;

    @FXML
    void backtosettings(MouseEvent event) {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/features/features.fxml");
    }

    @FXML
    void predictCrop(ActionEvent event) {
        if (txtN.getText().isEmpty() || txtp.getText().isEmpty() || txtk.getText().isEmpty()
                || txtc.getText().isEmpty() || txth.getText().isEmpty()
                || txtph.getText().isEmpty() || txtmm.getText().isEmpty()) {
            HelloApplication.error("Tous les champs sont obligatoires.");
            return;
        }
        try {
            double N = Double.parseDouble(txtN.getText());
            double P = Double.parseDouble(txtp.getText());
            double K = Double.parseDouble(txtk.getText());
            double temperature = Double.parseDouble(txtc.getText());
            double humidity = Double.parseDouble(txth.getText());
            double ph = Double.parseDouble(txtph.getText());
            double rainfall = Double.parseDouble(txtmm.getText());
            String json ="{\"N\": "+N+",\"P\": "+P+",\"K\": "+K+",\"temperature\": "+temperature+",\"humidity\": "+humidity+",\"ph\": "+ph+",\"rainfall\": "+rainfall+"}\n";


            PredictionService service = new PredictionService();
            String result = service.predict(json);

            if (result != null) {
                prediction.setText(
                        "🌱 Culture recommandée : " + result + "\n\n" +
                                "Selon l’analyse des nutriments du sol et des conditions climatiques, la culture la plus \nadaptée est : " + result + "." +
                                "Cette suggestion optimise le rendement tout en \nassurant une exploitation durable."
                );

            } else {
                prediction.setText("❌ Erreur lors de la prédiction.");
            }
        } catch (NumberFormatException e) {
            prediction.setText("⚠️ Veuillez remplir tous les champs correctement.");
        }
    }


}
