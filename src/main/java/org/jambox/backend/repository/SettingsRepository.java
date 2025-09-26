package org.jambox.backend.repository;

import org.jambox.backend.model.entity.Settings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingsRepository extends MongoRepository<Settings, String> {
    default Settings getSettings() {
        return findById("SINGLETON").orElseGet(() -> {
            Settings settings = new Settings();
            return save(settings);
        });
    }
}
