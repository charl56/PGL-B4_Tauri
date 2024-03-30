package fr.eseo.tauri.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import fr.eseo.tauri.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    // Vous pouvez ajouter des requêtes personnalisées ici si nécessaire
}