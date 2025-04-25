package org.example.demo.services.event;

import java.util.List;

public interface Interface<T> {
    void ajouter(T t);
    void modifier(T t);
    void supprimer(T t);
    List<T> rechercher();
}
