package com.revhire.resumeservice.service;

import com.revhire.resumeservice.dto.response.JobSeekerProfileResponse;
import com.revhire.resumeservice.model.ResumeText;
import com.revhire.resumeservice.model.SavedResume;
import com.revhire.resumeservice.repository.SavedResumeRepository;
import com.revhire.resumeservice.repository.ResumeTextRepository;
import com.revhire.resumeservice.security.UserPrincipal;
import com.revhire.resumeservice.client.NotificationClient;
import com.revhire.resumeservice.client.UserClient;
import com.revhire.resumeservice.client.JobClient;
import com.revhire.resumeservice.dto.external.NotificationRequest;
import com.revhire.resumeservice.dto.external.UserDto;
import com.revhire.resumeservice.dto.external.JobPostDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavedResumeService {

    private final SavedResumeRepository savedResumeRepository;
    private final ResumeTextRepository resumeTextRepository;
    private final NotificationClient notificationClient;
    private final UserClient userClient;
    private final JobClient jobClient;

    @Transactional
    public void saveResume(Long seekerId, Long jobId, UserPrincipal employer) {
        log.info("Employer {} saving resume for job seeker {} and job {}", employer.getId(), seekerId, jobId);

        if (savedResumeRepository.existsByEmployerIdAndJobSeekerIdAndJobPostId(employer.getId(), seekerId, jobId)) {
            throw new RuntimeException("Resume already saved by this employer");
        }

        SavedResume savedResume = new SavedResume();
        savedResume.setEmployerId(employer.getId());
        savedResume.setJobSeekerId(seekerId);
        savedResume.setJobPostId(jobId);
        savedResumeRepository.save(savedResume);

        NotificationRequest notif = new NotificationRequest();
        notif.setUserId(seekerId); // Assuming seekerId is the userId or we need to find userId from seekerId
        notif.setMessage("An employer has favorited your profile!");
        try {
            notificationClient.createNotification(notif);
        } catch (Exception e) {
            log.error("Failed to send notification", e);
        }
    }

    @Transactional
    public void unsaveResume(Long seekerId, Long jobId, UserPrincipal employer) {
        SavedResume savedResume = savedResumeRepository
                .findByEmployerIdAndJobSeekerIdAndJobPostId(employer.getId(), seekerId, jobId)
                .orElseThrow(() -> new RuntimeException("Saved resume not found"));

        savedResumeRepository.delete(savedResume);
    }

    @Transactional(readOnly = true)
    public List<JobSeekerProfileResponse> getSavedResumes(UserPrincipal employer) {
        log.info("Fetching saved resumes for employer {}", employer.getId());

        return savedResumeRepository.findByEmployerId(employer.getId()).stream()
                .map(savedResume -> {
                    JobSeekerProfileResponse dto = new JobSeekerProfileResponse();
                    dto.setId(savedResume.getJobSeekerId());
                    dto.setJobId(savedResume.getJobPostId());

                    // Try to enrich from user-service
                    try {
                        UserDto user = userClient.getUserById(savedResume.getJobSeekerId());
                        if (user != null) {
                            dto.setName(user.getName());
                            dto.setEmail(user.getEmail());
                            dto.setPhone(user.getPhone());
                        }
                    } catch (Exception e) {
                        log.error("Failed to fetch user details for seeker {}", savedResume.getJobSeekerId());
                    }

                    // Try to enrich from job-service
                    try {
                        JobPostDto job = jobClient.getJobById(savedResume.getJobPostId());
                        if (job != null) {
                            dto.setAppliedRole(job.getTitle());
                            dto.setJobId(job.getId());
                        }
                    } catch (Exception e) {
                        log.error("Failed to fetch job details for job {}", savedResume.getJobPostId());
                        if (dto.getAppliedRole() == null) {
                            dto.setAppliedRole("Applied Candidate");
                        }
                    }

                    Optional<ResumeText> resumeTextOpt = resumeTextRepository.findByJobSeekerId(savedResume.getJobSeekerId());
                    if (resumeTextOpt.isPresent()) {
                        ResumeText resumeText = resumeTextOpt.get();
                        dto.setObjective(resumeText.getObjective());
                        dto.setEducation(resumeText.getEducationText());
                        dto.setExperience(resumeText.getExperienceText());
                        dto.setSkills(resumeText.getSkillsText());
                        dto.setProjects(resumeText.getProjectsText());
                        dto.setCertifications(resumeText.getCertificationsText());
                    }

                    return dto;
                })
                .toList();
    }
}
