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
    """Transcription en continu et analyse de sentiment (FR)."""
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
