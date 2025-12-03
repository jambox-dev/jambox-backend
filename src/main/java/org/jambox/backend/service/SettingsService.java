package org.jambox.backend.service;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.config.TenantContext;
import org.jambox.backend.model.entity.Settings;
import org.jambox.backend.repository.SettingsRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingsService {
    private final SettingsRepository settingsRepository;

    public Settings getSettings() {
        String tenantId = TenantContext.getTenantId();
        return settingsRepository.findByTenantId(tenantId).orElseGet(() -> {
            Settings settings = new Settings();
            settings.setTenantId(tenantId);
            return settingsRepository.save(settings);
        });
    }

    public Settings updateSettings(Settings settings) {
        String tenantId = TenantContext.getTenantId();
        Settings existing = getSettings();
        settings.setId(existing.getId());
        settings.setTenantId(tenantId);
        return settingsRepository.save(settings);
    }
}