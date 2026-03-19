package com.revhire.userservice.service;

import com.revhire.userservice.dto.request.JobSeekerProfileRequest;
import com.revhire.userservice.model.JobSeekerProfile;
import com.revhire.userservice.model.User;
import com.revhire.userservice.repository.JobSeekerProfileRepository;
import com.revhire.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobSeekerProfileService {

    private static final String PROFILE_NOT_FOUND = "Profile not found";

    private final JobSeekerProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Transactional
    public JobSeekerProfile updateProfile(JobSeekerProfileRequest profileDto, User user) {
        log.info("Updating profile for user: {}", user.getEmail());
        JobSeekerProfile profile = profileRepository.findByUserId(user.getId()).orElse(new JobSeekerProfile());

        if (profile.getUser() == null) {
            profile.setUser(user);
        }

        if (profileDto.getPhone() != null) {
            user.setPhone(profileDto.getPhone());
            userRepository.save(user);
        }

        profile.setHeadline(profileDto.getHeadline());
        profile.setSummary(profileDto.getSummary());
        profile.setLocation(profileDto.getLocation());
        profile.setEmploymentStatus(profileDto.getEmploymentStatus());
        if (profileDto.getProfileImage() != null) {
            profile.setProfileImage(profileDto.getProfileImage());
        }

        return profileRepository.save(profile);
    }

    public JobSeekerProfile getProfile(User user) {
        return profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException(PROFILE_NOT_FOUND));
    }

    public JobSeekerProfile getProfileById(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException(PROFILE_NOT_FOUND));
    }

    public Optional<JobSeekerProfile> findByUserId(Long userId) {
        return profileRepository.findByUserId(userId);
    }

    public java.util.List<JobSeekerProfile> searchByKeyword(String keyword) {
        return profileRepository.searchByKeyword(keyword);
    }

    @Transactional
    public void deleteProfile(Long seekerId, User user) {
        JobSeekerProfile profile = profileRepository.findById(seekerId)
                .orElseThrow(() -> new IllegalArgumentException(PROFILE_NOT_FOUND));

        if (!profile.getUser().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new IllegalStateException("Unauthorized to delete this profile");
        }

        User u = profile.getUser();
        if (u != null) {
            u.setStatus(false);
            userRepository.save(u);
        }
        log.info("Profile deactivated: {} by user: {}", seekerId, user.getEmail());
    }
}

