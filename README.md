# Projet Agrilink JavaFX

Gestion complète d'une exploitation agricole via une application desktop JavaFX, intégrant 6 modules de gestion, un ensemble de métiers (API et fonctionnalités) et plusieurs modèles d’IA pour l’analyse et la recommandation.
For a quick demo of our application check this link https://res.cloudinary.com/dw8lxfsv0/video/upload/v1747124485/Agrilinkpromo2_zwzzci.mp4

---

## Table des matières

1. [Description du projet](#description-du-projet)
2. [Technologies & Topics](#technologies--topics)
3. [Installation](#installation)
4. [Configuration](#configuration)
5. [Fonctionnalités Métiers & APIs](#fonctionnalites-metiers--apis)
6. [Modules de gestion](#modules-de-gestion)
7. [IA & Modèles](#ia--modeles)
8. [Utilisation](#utilisation)
9. [Contribution](#contribution)
10. [Licence](#licence)

---

## Description du projet

Agrilink JavaFX est une application desktop en Java 11+ avec interface JavaFX, connectée à une base MySQL via XAMPP/phpMyAdmin. Elle offre des fonctionnalités CRUD pour six modules clés et intègre de nombreux services (recherche dynamique, archivage, réponses automatiques, statistiques, SMS, PDF, voice-to-text, traduction, filtrage de propos, etc.).

---

## Technologies & Topics

* **Langage** : Java 11+
* **Interface** : JavaFX
* **BD** : MySQL (XAMPP) + phpMyAdmin
* **Accès BD** : JDBC
* **Build** : Maven / Gradle
* **IA** : TensorFlow, PyTorch, GPT2, RoBERTa

**Topics** : java, javafx, mysql, xampp, api, ia, recyclage, gestion

---

## Installation

```bash
git clone https://github.com/votre-organisation/agrilink.git
cd agrilink
mvn clean install  # ou gradle build
```

* Configurer XAMPP: démarrer Apache/MySQL, créer `agrilink_db` via phpMyAdmin.

---

## Configuration

Dans `application.properties`:

```properties
db.url=jdbc:mysql://127.0.0.1:3306/agrilink_db
db.user=root
db.password=
ai.api.key=votre_cle_api_ia
```

---

## Fonctionnalités Métiers & APIs

* Tri et recherche (Ajax, multicritères, dynamique)
* Archivage des données
* Réponse automatique & notifications (SMS 326/327)
* Statistiques & rapports
* Génération de PDF (iText)
* Voice-to-text & synthèse vocale
* Recherche sur maladies des plantes & animaux
* Traduction (multilingue)
* Cryptage et filtrage de termes inappropriés
* Caméra émotion + reconnaissance vocale
* Affichage d’offres d’emploi, vidéos TikTok, articles
* Partage social (FB, LinkedIn, WhatsApp)
* Badge positif/négatif/neutral (RoBERTa)
* Modération de commentaires

---

## Modules de gestion

1. **Utilisateurs**: création, authentification locale, 2FA, reset password, rappel mot de passe, suggestion utilisateur.
2. **Ressources**: matériel, semences, engrais, réapprovisionnement.
3. **Points de recyclage**: carte interactive, gestions des emplacements.
4. **Événements**: planification, calendrier, expirations automatiques.
5. **Posts**: publication, modération, réactions.
6. **Réclamations**: soumission, classification (positif/négatif), suivi, badge.

---

## IA & Modèles

* Détection de maladies des plantes & animaux
* Prédiction de rendement des terres
* Détection de type de sol
* Contrôle qualité d’image
* Modèle d’émotion utilisateur
* Modèle de traitement de réclamation (positive/négative)
* Modèle d’acceptation/rejet de demande
* Météo locale & conseils agricoles
* Chatbot GPT2 pour conseils

---

## Utilisation

```bash
java -jar target/agrilink.jar
```

---

## Contribution

Fork > branche feature > commit > push > PR

---

## Licence

MIT. Voir [LICENSE](LICENSE)
