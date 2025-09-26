package org.jambox.backend.service;

import org.jambox.backend.model.entity.Settings;
import org.jambox.backend.repository.SettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {
    private final SettingsRepository settingsRepository;

    public SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public Settings getSettings() {
        return settingsRepository.getSettings();
    }

    public Settings updateSettings(Settings settings) {
        settings.setId("SINGLETON");
        return settingsRepository.save(settings);
    }
}