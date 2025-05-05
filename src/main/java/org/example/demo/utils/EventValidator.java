package org.example.demo.utils;

import org.example.demo.HelloApplication;
import org.example.demo.models.categorie;

import java.time.LocalDate;

public class EventValidator {

    public static String valider(String nom, String adresse, String longitudeStr, String latitudeStr,
                                 LocalDate date, String type, String placeStr, String image,
                                 String description, categorie categorie) {

        if (nom == null || nom.trim().length() < 3)
            return "Le nom doit contenir au moins 3 caractères.";

        if (adresse == null || adresse.trim().length() < 5)
            return "L'adresse doit contenir au moins 5 caractères.";

        if (description == null || description.trim().length() < 10)
            return "La description doit contenir au moins 10 caractères.";

        if (date == null)
            return "La date de l'événement est obligatoire.";
        if (!date.isAfter(LocalDate.now()))
            return "La date de l'événement doit être dans le futur.";

        if (type == null || type.trim().isEmpty())
            return "Le type de l'événement est obligatoire.";

        if (image == null || image.trim().isEmpty())
            return "L'image est obligatoire.";

        if (placeStr == null || placeStr.trim().isEmpty())
            return "Le nombre de places est obligatoire.";
        try {
            int places = Integer.parseInt(placeStr.trim());
            if (places <= 0)
                return "Le nombre de places doit être supérieur à 0.";
        } catch (NumberFormatException e) {
            return "Le nombre de places doit être un nombre valide.";
        }

        if (longitudeStr == null || latitudeStr == null || longitudeStr.trim().isEmpty() || latitudeStr.trim().isEmpty())
            return "Les coordonnées sont obligatoires.";
        try {
            double lng = Double.parseDouble(longitudeStr.trim());
            double lat = Double.parseDouble(latitudeStr.trim());
            if (lng < -180 || lng > 180 || lat < -90 || lat > 90)
                return "Les coordonnées doivent être valides (-180 à 180 pour longitude, -90 à 90 pour latitude).";
        } catch (NumberFormatException e) {
            return "Longitude et latitude doivent être des nombres valides.";
        }

        if (categorie == null)
            return "La catégorie est obligatoire.";

        return null; // ✅ Tout est valide
    }

    public static void showError(String message) {
        HelloApplication.error(message);
    }
}
