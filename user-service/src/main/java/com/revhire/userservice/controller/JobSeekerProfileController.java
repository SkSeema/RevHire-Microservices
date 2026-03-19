package com.revhire.userservice.controller;

import com.revhire.userservice.client.ApplicationClient;
import com.revhire.userservice.client.ResumeClient;
import com.revhire.userservice.dto.external.ResumeDetailsDto;
import com.revhire.userservice.dto.request.JobSeekerProfileRequest;
import com.revhire.userservice.dto.response.JobSeekerProfileResponse;
import com.revhire.userservice.model.JobSeekerProfile;
import com.revhire.userservice.model.User;
import com.revhire.userservice.security.UserDetailsImpl;
import com.revhire.userservice.repository.UserRepository;
import com.revhire.userservice.service.JobSeekerProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/seeker/profile")
@RequiredArgsConstructor
@Slf4j
public class JobSeekerProfileController {

    private final JobSeekerProfileService profileService;
    private final UserRepository userRepository;
    private final ResumeClient resumeClient;
    private final ApplicationClient applicationClient;

    private User getUserFromContext(UserDetailsImpl userDetails) {
        if (userDetails == null)
            return null;
        return userRepository.findById(userDetails.getId()).orElse(null);
    }

    @PostMapping
    public ResponseEntity<?> updateProfile(
            @RequestBody JobSeekerProfileRequest profileDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            JobSeekerProfile profile = profileService.updateProfile(profileDto, user);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            JobSeekerProfile profile = profileService.getProfile(user);
            return ResponseEntity.ok(mapToResponse(profile));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<JobSeekerProfileResponse> getProfileByUserId(@PathVariable("userId") Long userId) {
        return profileService.findByUserId(userId).map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/id/{profileId}")
    public ResponseEntity<JobSeekerProfileResponse> getProfileById(@PathVariable("profileId") Long profileId) {
        try {
            JobSeekerProfile profile = profileService.getProfileById(profileId);
            return ResponseEntity.ok(mapToResponse(profile));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchSeekers(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(profileService.searchByKeyword(keyword).stream().map(this::mapToResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobSeekerProfileResponse> getProfileByDirectId(@PathVariable("id") Long id) {
        try {
            JobSeekerProfile profile = profileService.getProfileById(id);
            return ResponseEntity.ok(mapToResponse(profile));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable("id") Long id,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null) {
            return ResponseEntity.status(403).build();
        }
        profileService.deleteProfile(id, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null) {
            return ResponseEntity.status(403).build();
        }
        // In this context, deactivating the user is sufficient for "deleting account"
        user.setStatus(false);
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/applications")
    public ResponseEntity<?> getSeekerApplications(@PathVariable("id") Long id,
                                                  @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        // Fetch profile to get userId if 'id' is profileId
        JobSeekerProfile profile = profileService.getProfileById(id);
        return ResponseEntity.ok(applicationClient.getApplicationsByUserId(profile.getUser().getId()));
    }

    private JobSeekerProfileResponse mapToResponse(JobSeekerProfile profile) {
        JobSeekerProfileResponse resp = new JobSeekerProfileResponse();
        resp.setId(profile.getId());
        resp.setUserId(profile.getUser().getId());
        resp.setName(profile.getUser().getName());
        resp.setEmail(profile.getUser().getEmail());
        resp.setPhone(profile.getUser().getPhone());
        resp.setProfileImage(profile.getProfileImage());
        resp.setHeadline(profile.getHeadline());
        resp.setSummary(profile.getSummary());
        resp.setLocation(profile.getLocation());
        resp.setEmploymentStatus(profile.getEmploymentStatus());
        try {
            ResumeDetailsDto resume = resumeClient.getResumeDetails(profile.getId());
            if (resume != null) {
                resp.setTitle(resume.getTitle());
                resp.setObjective(resume.getObjective());
                resp.setEducation(resume.getEducation());
                resp.setExperience(resume.getExperience());
                resp.setSkills(resume.getSkills());
                resp.setProjects(resume.getProjects());
                resp.setCertifications(resume.getCertifications());
                resp.setSkillsList(resume.getSkillsList());
                resp.setResumeUploaded(resume.isResumeUploaded());
                resp.setProfileSummarySet(resume.isProfileSummarySet());
                resp.setSkillsSet(resume.isSkillsSet());
                resp.setExperienceSet(resume.isExperienceSet());
                resp.setEducationSet(resume.isEducationSet());
                resp.setCompletionPercentage(resume.getCompletionPercentage());
            }
        } catch (Exception ignored) {
            // Keep profile responses usable even if resume-service enrichment is unavailable.
        }
        return resp;
    }
}
