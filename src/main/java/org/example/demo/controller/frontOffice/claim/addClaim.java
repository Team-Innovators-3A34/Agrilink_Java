package org.example.demo.controller.frontOffice.claim;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.example.demo.utils.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import org.example.demo.HelloApplication;
import org.example.demo.models.Reclamation;
import org.example.demo.models.TypeRec;
import org.example.demo.models.User;
import org.example.demo.services.claim.ReclamationService;
import org.example.demo.services.claim.TypeRecService;
import org.example.demo.utils.TextUtils;
import org.example.demo.utils.sessionManager;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class addClaim implements Initializable {

    @FXML private BorderPane rootPane; // Ajoutez ce champ dans votre FXML
    @FXML private ImageView backtosettings;
    @FXML private Text title;


    @FXML private TextArea rapportTextArea;
    private BufferedWriter pyStdinWriter;


    @FXML private TextField titreField;
    @FXML private ImageView voiceTitleIcon;

    @FXML private ComboBox<TypeRec> typeField;

    @FXML private TextArea contentField;
    @FXML private ImageView voiceContentIcon;

    @FXML private HBox imageDropPane;
    @FXML private ImageView previewImageView;
    @FXML private Text dropPrompt;

    @FXML private Button addClaim;

    // Nouvel élément pour afficher les émotions
    @FXML private Label emotionLabel;
    @FXML private Button startEmotionAnalysisBtn;
    @FXML private Button stopEmotionAnalysisBtn;

    private User user;
    private TypeRecService typeRecService;
    private ReclamationService reclamationService;
    private Reclamation reclamationModif;

    private Model voskModel;
    private volatile boolean isRecording = false;
    private Thread recognitionThread;

    // Process Python pour l'analyse d'émotions
    private Process pythonProcess;
    private static final String PYTHON_EXE = "python"; // ou "python3" selon votre système
    private static final String SCRIPT_PATH = "C:/Users/user/Desktop/emotion/test.py";
    private Map<String, Object> latestEmotionData = new HashMap<>();
    private volatile boolean emotionAnalysisRunning = false;

    private final String INFOBIP_API_URL   = "https://v3ge81.api.infobip.com/sms/2/text/advanced";
    private final String INFOBIP_API_KEY   = "3b2120a3b435b16966acc3584993bc02-61f61f22-934b-411d-b4c8-efd104e3beb6";
    private final String INFOBIP_SENDER    = "447491163443";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        user = sessionManager.getInstance().getUser();
        typeRecService = new TypeRecService();
        reclamationService = new ReclamationService();

        // Remplir ComboBox des types
        List<TypeRec> categories = typeRecService.getAllCategories();
        typeField.getItems().setAll(categories);
        typeField.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(TypeRec item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty||item==null ? null : item.getNom());
            }
        });
        typeField.setButtonCell(typeField.getCellFactory().call(null));

        // Init Vosk
        try {
            LibVosk.setLogLevel(LogLevel.WARNINGS);
            URL m = getClass().getResource("/vosk-model-fr");
            if (m == null) throw new IOException("Modèle Vosk introuvable");
            voskModel = new Model(new File(m.toURI()).getAbsolutePath());
            voiceTitleIcon.setDisable(false);
            voiceContentIcon.setDisable(false);
        } catch (Exception e) {
            voiceTitleIcon.setDisable(true);
            voiceContentIcon.setDisable(true);
            System.err.println("Voice init error: " + e.getMessage());
        }

        // Drag‑and‑drop image
        imageDropPane.setOnDragOver(this::handleDragOver);
        imageDropPane.setOnDragDropped(this::handleDragDropped);
        imageDropPane.setOnMouseClicked(e -> selectImageWithDialog());

        // Configuration pour l'analyse d'émotions
        if (startEmotionAnalysisBtn != null) {
            startEmotionAnalysisBtn.setOnAction(e -> startEmotionAnalysis());
        }

        if (stopEmotionAnalysisBtn != null) {
            stopEmotionAnalysisBtn.setOnAction(e -> stopEmotionAnalysis());
        }

        // Créer le script Python d'analyse d'émotions s'il n'existe pas
        createEmotionAnalyzerScript();

        // Arrêter Python à la fermeture de la fenêtre
        Platform.runLater(() -> {
            if (rootPane != null && rootPane.getScene() != null && rootPane.getScene().getWindow() != null) {
                rootPane.getScene().getWindow().addEventFilter(
                        WindowEvent.WINDOW_HIDDEN, e -> stopEmotionAnalysis()
                );
            }
        });
        System.out.println("rapportTextArea = " + rapportTextArea); // debug, doit être non-null
    }

    /**
     * Crée le script Python d'analyse d'émotions s'il n'existe pas
     */
    private void createEmotionAnalyzerScript() {
        try {
            File scriptFile = new File(SCRIPT_PATH);
            if (!scriptFile.exists()) {
                Files.writeString(scriptFile.toPath(), getPythonScript());
                System.out.println("Script d'analyse d'émotions créé: " + scriptFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la création du script Python: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Contenu du script Python pour l'analyse d'émotions
     */
    private String getPythonScript() {
        return """
            import cv2
            from deepface import DeepFace
            import threading
            import speech_recognition as sr
            from textblob import TextBlob
            from textblob_fr import PatternTagger, PatternAnalyzer
            from collections import Counter
            import json
            import sys

            def audio_thread(stop_event, audio_results):
                \"\"\"Transcription en continu et analyse de sentiment (FR).\"\"\"
                recognizer = sr.Recognizer()
                mic = sr.Microphone()
                with mic as source:
                    recognizer.adjust_for_ambient_noise(source, duration=1)
                print("[AUDIO] Démarrage du fil audio.")

                while not stop_event.is_set():
                    with mic as source:
                        audio_data = recognizer.listen(source, phrase_time_limit=5)
                    try:
                        text = recognizer.recognize_google(audio_data, language="fr-FR")
                        blob = TextBlob(text, pos_tagger=PatternTagger(), analyzer=PatternAnalyzer())
                        polarity, subj = blob.sentiment
                        audio_results.append({
                            'text': text,
                            'polarity': polarity,
                            'subjectivity': subj
                        })
                        print(f"[AUDIO] '{text}' → polarité {polarity:.2f}, subjectivité {subj:.2f}")
                        # Envoi des données à Java
                        print(f"AUDIO_DATA:{json.dumps({'text': text, 'polarity': polarity, 'subjectivity': subj})}")
                        sys.stdout.flush()
                    except sr.UnknownValueError:
                        print("[AUDIO] non compris.")
                    except sr.RequestError as e:
                        print(f"[AUDIO] Erreur service vocal : {e}")

                print("[AUDIO] Fil audio arrêté.")

            if __name__ == "__main__":
                stop_event = threading.Event()
                audio_results = []
                video_results = []

                # Thread audio
                t_audio = threading.Thread(target=audio_thread, args=(stop_event, audio_results), daemon=True)
                t_audio.start()

                # Boucle vidéo dans le main thread
                cap = cv2.VideoCapture(0, cv2.CAP_DSHOW)
                if not cap.isOpened():
                    print("Erreur : impossible d'ouvrir la caméra.")
                    stop_event.set()
                    t_audio.join()
                    exit(1)

                cv2.namedWindow("Emotion Visage (q pour quitter)")
                font = cv2.FONT_HERSHEY_SIMPLEX

                try:
                    while True:
                        ret, frame = cap.read()
                        if not ret:
                            break

                        try:
                            res = DeepFace.analyze(
                                frame,
                                actions=["emotion"],
                                enforce_detection=False,
                                detector_backend="opencv"
                            )
                            if isinstance(res, list):
                                res = res[0] if res else None
                            if res:
                                dom = res["dominant_emotion"]
                                score = res["emotion"][dom]
                                video_results.append({
                                    'emotion': dom,
                                    'score': score
                                })
                                # affichage
                                region = res.get("region", {})
                                x, y, w, h = region.get("x",0), region.get("y",0), region.get("w",0), region.get("h",0)
                                label = f"{dom}: {score:.1f}%"
                                if w and h:
                                    cv2.rectangle(frame, (x,y),(x+w,y+h),(255,0,0),2)
                                (tw, th), baseline = cv2.getTextSize(label, font, 0.8, 2)
                                cv2.rectangle(frame, (10,10),(10+tw,10+th+baseline),(0,0,0),cv2.FILLED)
                                cv2.putText(frame, label, (10,10+th), font, 0.8, (255,255,255), 2)
                                
                                # Envoi des données à Java
                                print(f"VIDEO_DATA:{json.dumps({'emotion': dom, 'score': score})}")
                                sys.stdout.flush()
                        except Exception as e:
                            print(f"[VIDEO] Erreur: {e}")

                        cv2.imshow("Emotion Visage (q pour quitter)", frame)
                        if cv2.waitKey(1) & 0xFF == ord('q'):
                            break

                finally:
                    stop_event.set()
                    cap.release()
                    cv2.destroyAllWindows()
                    t_audio.join()
                    print("=== Fin ===")
            """;
    }

    /**
     * Démarre l'analyse des émotions via le script Python
     */
    private volatile boolean inFinalReport = false;

    private void startEmotionAnalysis() {
        if (emotionAnalysisRunning) return;

        // 1) Réinitialiser la TextArea et le flag
        Platform.runLater(() -> rapportTextArea.clear());
        inFinalReport = false;

        try {
            // 2) S’assurer que le script existe
            File script = new File(SCRIPT_PATH);
            if (!script.exists()) {
                createEmotionAnalyzerScript();
            }

            // 3) Lancer le process Python avec entrée/sortie en UTF-8
            ProcessBuilder pb = new ProcessBuilder(PYTHON_EXE, script.getAbsolutePath());
            pb.directory(script.getParentFile());
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            pythonProcess = pb.start();
            emotionAnalysisRunning = true;

            // 4) Préparer le writer pour envoyer STOP plus tard
            pyStdinWriter = new BufferedWriter(
                    new OutputStreamWriter(pythonProcess.getOutputStream(), StandardCharsets.UTF_8)
            );

            // 5) Thread de lecture de la sortie Python
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(pythonProcess.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Python: " + line);

                        // Ici votre code de traitement VIDEO_DATA/AUDIO_DATA…

                        // Détecter le début du rapport final
                        if (line.contains("=== Rapport final ===")) {
                            inFinalReport = true;
                            String finalLine = line;
                            Platform.runLater(() -> rapportTextArea.appendText(finalLine + "\n"));
                        }
                        else if (inFinalReport) {
                            String finalLine1 = line;
                            Platform.runLater(() -> rapportTextArea.appendText(finalLine1 + "\n"));
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }, "Emotion-Reader-Thread").start();

            // 6) Mettre à jour l’UI des boutons
            Platform.runLater(() -> {
                startEmotionAnalysisBtn.setDisable(true);
                stopEmotionAnalysisBtn.setDisable(false);
            });

        } catch (IOException e) {
            e.printStackTrace();
            HelloApplication.error("Impossible de lancer l'analyse d'émotions : " + e.getMessage());
        }
    }



    /**
     * Arrête l'analyse des émotions
     */
    @FXML
    private void stopEmotionAnalysis() {
        if (pythonProcess != null && pythonProcess.isAlive()) {
            try {
                // 1) Envoyer le mot-clé STOP au script Python
                pyStdinWriter.write("STOP\n");
                pyStdinWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 2) Attendre la fin propre et forcer si besoin
            try {
                if (!pythonProcess.waitFor(3, TimeUnit.SECONDS)) {
                    pythonProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            emotionAnalysisRunning = false;
            System.out.println("🛑 Analyse d'émotions arrêtée");

            Platform.runLater(() -> {
                startEmotionAnalysisBtn.setDisable(false);
                stopEmotionAnalysisBtn.setDisable(true);
            });
        }
    }


    /** Pré‑remplissage pour modification */
    public void setClaimPourModification(Reclamation rec) {
        this.reclamationModif = rec;
        titreField.setText(rec.getTitle());
        contentField.setText(rec.getContent());
        if (rec.getImage() != null && !rec.getImage().isEmpty()) {
            File f = new File("src/main/resources/images/" + rec.getImage());
            if (f.exists()) {
                previewImageView.setImage(new Image(f.toURI().toString(), 200, 0, true, true));
                previewImageView.setUserData(rec.getImage());
                dropPrompt.setVisible(false);
            }
        }
        typeField.getItems().stream()
                .filter(t -> t.getId() == rec.getType())
                .findFirst().ifPresent(typeField::setValue);
        title.setText("Modifier Réclamation");
    }

    @FXML
    private void backtosettings(MouseEvent e) {
        stopEmotionAnalysis(); // Arrêter l'analyse avant de changer de scène
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/settings/settings.fxml");
    }

    @FXML
    private void onVoiceTitle(MouseEvent e) {
        startVoiceRecognition(titreField);
    }

    @FXML
    private void onVoiceContent(MouseEvent e) {
        startVoiceRecognition(contentField);
    }

    @FXML
    private void onAddClaimClicked() {
        String titre = titreField.getText().trim();
        String contenu = contentField.getText().trim();
        String imageName = previewImageView.getUserData() != null
                ? previewImageView.getUserData().toString()
                : "";
        TypeRec sel = typeField.getValue();

        if (titre.isEmpty() || contenu.isEmpty() || sel == null) {
            HelloApplication.error("Veuillez remplir tous les champs obligatoires.");
            return;
        }

        // Ajout de l'émotion détectée si disponible
        if (emotionAnalysisRunning && !latestEmotionData.isEmpty()) {
            String emotion = (String) latestEmotionData.get("emotion");
            Double score = (Double) latestEmotionData.get("score");
            if (emotion != null && score != null) {
                contenu += "\n\nÉmotion détectée: " + emotion + " (" + String.format("%.1f", score) + "%)";
            }
        }

        // ** Masquage des mots interdits **
        List<String> bad = List.of("test", "fuck");
        String filtered = TextUtils.maskBadWords(contenu, bad);

        boolean ok;
        String smsText;
        if (reclamationModif == null) {
            Reclamation r = new Reclamation();
            r.setTitle(titre);
            r.setContent(filtered);
            r.setImage(imageName);
            r.setType(sel.getId());
            r.setIdUser(user.getId());
            r.setDate(LocalDateTime.now());
            r.setStatus("En cours");
            r.setMailUser(user.getEmail());
            r.setNomUser(user.getNom() + " " + user.getPrenom());
            r.setPriorite(0);
            r.setArchive("non");
            r.setTypeNom(sel.getNom());
            ok = reclamationService.ajouterReclamation(r);
            smsText = "Votre réclamation \"" + titre + "\" a bien été reçue.";
        } else {
            reclamationModif.setTitle(titre);
            reclamationModif.setContent(filtered);
            reclamationModif.setImage(imageName);
            reclamationModif.setType(sel.getId());
            ok = reclamationService.modifierReclamation(reclamationModif);
            smsText = "Votre réclamation \"" + titre + "\" a bien été mise à jour.";
        }

        stopEmotionAnalysis();

        if (ok) {
            HelloApplication.succes("Opération réussie !");
            if (user.getTelephone() != null && !user.getTelephone().isBlank()) {
                if (!latestEmotionData.isEmpty()) {
                    String emotion = (String) latestEmotionData.get("emotion");
                    if (emotion != null) {
                        smsText += " Nous avons détecté que vous étiez " + emotion + ".";
                    }
                }
                sendSms("+216" + user.getTelephone(), smsText);
            }
            HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/profile/profile.fxml");
        } else {
            HelloApplication.error("Erreur lors de l'opération.");
        }
    }

    private void handleDragOver(DragEvent evt) {
        Dragboard db = evt.getDragboard();
        if (db.hasFiles() && isImageFile(db.getFiles().get(0))) {
            evt.acceptTransferModes(TransferMode.COPY);
        }
        evt.consume();
    }

    private void handleDragDropped(DragEvent evt) {
        Dragboard db = evt.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            File f = db.getFiles().get(0);
            if (isImageFile(f)) {
                saveAndPreviewImage(f);
                success = true;
            }
        }
        evt.setDropCompleted(success);
        evt.consume();
    }

    private void selectImageWithDialog() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File sel = fc.showOpenDialog(imageDropPane.getScene().getWindow());
        if (sel != null) saveAndPreviewImage(sel);
    }

    private boolean isImageFile(File f) {
        String n = f.getName().toLowerCase();
        return n.endsWith(".png") || n.endsWith(".jpg")
                || n.endsWith(".jpeg") || n.endsWith(".gif");
    }

    private void saveAndPreviewImage(File src) {
        try {
            File dir = new File("src/main/resources/images");
            if (!dir.exists()) dir.mkdirs();
            File dest = new File(dir, src.getName());
            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            previewImageView.setImage(new Image(dest.toURI().toString(), 200, 0, true, true));
            previewImageView.setUserData(src.getName());
            dropPrompt.setVisible(false);
        } catch (IOException ex) {
            ex.printStackTrace();
            HelloApplication.error("Erreur lors de la sauvegarde de l'image.");
        }
    }

    private void startVoiceRecognition(TextInputControl target) {
        if (isRecording) return;
        isRecording = true;
        recognitionThread = new Thread(() -> {
            try {
                AudioFormat fmt = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, fmt);
                TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(fmt); line.start();
                Recognizer rec = new Recognizer(voskModel, 16000);
                byte[] buf = new byte[4096];
                long t0 = System.currentTimeMillis();
                while (isRecording && System.currentTimeMillis() - t0 < 5000) {
                    int n = line.read(buf, 0, buf.length);
                    if (n > 0 && rec.acceptWaveForm(buf, n)) {
                        String res = JsonParser.parseString(rec.getResult())
                                .getAsJsonObject()
                                .get("text").getAsString();
                        if (!res.isEmpty()) {
                            Platform.runLater(() ->
                                    target.appendText((target.getText().isEmpty() ? "" : " ") + res)
                            );
                            break;
                        }
                    }
                }
                String finalRes = JsonParser.parseString(rec.getFinalResult())
                        .getAsJsonObject()
                        .get("text").getAsString();
                if (!finalRes.isEmpty()) {
                    Platform.runLater(() ->
                            target.appendText((target.getText().isEmpty() ? "" : " ") + finalRes)
                    );
                }
                line.stop(); line.close(); rec.close();
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        HelloApplication.error("Erreur vocale : " + e.getMessage())
                );
            } finally {
                isRecording = false;
            }
        });
        recognitionThread.setDaemon(true);
        recognitionThread.start();
    }

    /** Envoi de SMS via Infobip */
    private void sendSms(String phone, String text) {
        try {
            JsonObject dest = new JsonObject();
            dest.addProperty("to", phone);
            JsonArray arr = new JsonArray(); arr.add(dest);
            JsonObject msg = new JsonObject();
            msg.add("destinations", arr);
            msg.addProperty("from", INFOBIP_SENDER);
            msg.addProperty("text", text);
            JsonArray msgs = new JsonArray(); msgs.add(msg);
            JsonObject payload = new JsonObject(); payload.add("messages", msgs);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(INFOBIP_API_URL))
                    .header("Authorization", "App " + INFOBIP_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpClient.newHttpClient()
                    .send(req, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}