package org.example.demo.services.ressource;

import org.example.demo.models.Demandes;
import org.example.demo.models.User;

import java.time.LocalDate;
import java.util.List;

public class DemandeService {
    private final DemandesService demandesService;

    public DemandeService() {
        this.demandesService = new DemandesService(); // À adapter à ta façon de récupérer les données
    }

    public boolean hasDemandeQuiExpireAujourdhui(User user) {
        List<Demandes> demandes = demandesService.getDemandesByUserId(user);
        LocalDate today = LocalDate.now();

        for (Demandes demande : demandes) {
            if (demande.getExpireDate().isEqual(today)) {
                return true;
            }
        }
        return false;
    }
}
