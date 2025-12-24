package com.example.order.repository;

import com.example.order.entity.RestaurantSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RestaurantSettingsRepository extends JpaRepository<RestaurantSettings, Long> {
    Optional<RestaurantSettings> findBySettingKey(String settingKey);
}
