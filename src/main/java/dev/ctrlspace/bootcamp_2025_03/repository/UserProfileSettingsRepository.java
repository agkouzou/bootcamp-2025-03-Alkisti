package dev.ctrlspace.bootcamp_2025_03.repository;

import dev.ctrlspace.bootcamp_2025_03.model.User;
import dev.ctrlspace.bootcamp_2025_03.model.UserProfileSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileSettingsRepository extends JpaRepository<UserProfileSettings, Long> {
    Optional<UserProfileSettings> findByUser(User user);
}
