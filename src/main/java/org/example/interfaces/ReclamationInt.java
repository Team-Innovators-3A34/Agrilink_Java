package org.example.interfaces;


import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public interface ReclamationInt {

    // Méthodes pour accéder aux champs de texte

    TextField getTxtDescription();

    // Méthode pour accéder à la ComboBox
    ComboBox<String> getCbStatut();

    // Méthode pour accéder à la TableView
    TableView<?> getTableReclamations();

    // Méthodes pour les actions des boutons
    void ajouterReclamation();
    void modifierReclamation();
    void supprimerReclamation();
}
