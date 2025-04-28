package org.example.demo.services.recyclingpoint;

import java.util.List;

public interface Interface<T> {
    boolean ajouter(T t);
    boolean modifier(T t);
    void supprimer(T t);
    List<T> rechercher();
}
