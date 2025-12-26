package com.example.order.controller;

import com.example.order.entity.RestaurantSettings;
import com.example.order.repository.RestaurantSettingsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders/settings")
public class RestaurantSettingsController {

    private final RestaurantSettingsRepository settingsRepository;

    public RestaurantSettingsController(RestaurantSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @GetMapping
    // Public Access allowed for fetching Table List & Timezone (Guest Mode)
    public ResponseEntity<List<RestaurantSettings>> getAllSettings() {
        return ResponseEntity.ok(settingsRepository.findAll());
    }

    @GetMapping("/public")
    public ResponseEntity<List<RestaurantSettings>> getPublicSettings() {
        return ResponseEntity.ok(settingsRepository.findAll());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestaurantSettings> updateSetting(@RequestBody RestaurantSettings setting) {
        Optional<RestaurantSettings> existing = settingsRepository.findBySettingKey(setting.getSettingKey());

        RestaurantSettings toSave;
        if (existing.isPresent()) {
            toSave = existing.get();
            toSave.setSettingValue(setting.getSettingValue());
        } else {
            toSave = setting;
        }

        return ResponseEntity.ok(settingsRepository.save(toSave));
    }
}
