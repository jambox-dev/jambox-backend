package org.jambox.backend.repository;

import org.jambox.backend.model.entity.Settings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettingsRepository extends MongoRepository<Settings, String> {
    Optional<Settings> findByTenantId(String tenantId);
}
