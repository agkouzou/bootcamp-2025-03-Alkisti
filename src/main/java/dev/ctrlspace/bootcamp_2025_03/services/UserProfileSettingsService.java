package dev.ctrlspace.bootcamp_2025_03.services;

import dev.ctrlspace.bootcamp_2025_03.model.dto.UserProfileSettingsDTO;
import dev.ctrlspace.bootcamp_2025_03.exceptions.BootcampException;
import dev.ctrlspace.bootcamp_2025_03.model.User;
import dev.ctrlspace.bootcamp_2025_03.model.UserProfileSettings;
import dev.ctrlspace.bootcamp_2025_03.repository.UserProfileSettingsRepository;
import dev.ctrlspace.bootcamp_2025_03.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service

public class UserProfileSettingsService {

    private UserRepository userRepository;
    private UserProfileSettingsRepository profileRepository;

    public UserProfileSettingsService(UserRepository userRepository, UserProfileSettingsRepository profileRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    public UserProfileSettingsDTO getProfileSettingsByUserId(Long userId) throws BootcampException {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new BootcampException(HttpStatus.NOT_FOUND, "User not found"));

        return getProfileSettingsByUser(user);
    }

    public UserProfileSettingsDTO getProfileSettingsByUser(User user) throws BootcampException {
        Optional<UserProfileSettings> profileOpt = profileRepository.findByUser(user);

        if (profileOpt.isEmpty()) {
            return new UserProfileSettingsDTO();
        }

        return mapToDTO(profileOpt.get());
    }

    public UserProfileSettingsDTO updateProfileSettings(Long userId, UserProfileSettingsDTO dto) throws BootcampException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BootcampException(HttpStatus.NOT_FOUND, "User not found"));

        UserProfileSettings profile = profileRepository.findByUser(user)
                .orElse(new UserProfileSettings());

        profile.setUser(user);
        profile.setIntroduction(dto.getIntroduction());
        profile.setNickname(dto.getNickname());
        profile.setJob(dto.getJob());

        profile.setTraits(dto.getTraits() != null ? dto.getTraits() : Collections.emptyList());

        profile.setNotes(dto.getNotes());

        profile = profileRepository.save(profile);

        return mapToDTO(profile);
    }

    private UserProfileSettingsDTO mapToDTO(UserProfileSettings profile) {
        UserProfileSettingsDTO dto = new UserProfileSettingsDTO();
        dto.setIntroduction(profile.getIntroduction());
        dto.setNickname(profile.getNickname());
        dto.setJob(profile.getJob());
        dto.setTraits(profile.getTraits() != null ? profile.getTraits() : Collections.emptyList());
        dto.setNotes(profile.getNotes());
        return dto;
    }
}
