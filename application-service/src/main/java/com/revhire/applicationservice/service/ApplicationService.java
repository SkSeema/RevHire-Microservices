package com.revhire.applicationservice.service;

import com.revhire.applicationservice.client.*;
import com.revhire.applicationservice.dto.external.*;
import com.revhire.applicationservice.dto.response.ApplicationResponse;
import com.revhire.applicationservice.dto.response.ApplicationSummaryResponse;
import com.revhire.applicationservice.model.Application;
import com.revhire.applicationservice.model.ApplicationStatusHistory;
import com.revhire.applicationservice.repository.ApplicationRepository;
import com.revhire.applicationservice.repository.ApplicationStatusHistoryRepository;
import com.revhire.applicationservice.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final UserClient userClient;
    private final JobClient jobClient;
    private final ResumeClient resumeClient;
    private final NotificationClient notificationClient;
    private final AuditLogClient auditLogClient;

    @Transactional
    public ApplicationResponse applyForJob(Long jobId, UserPrincipal user, Long resumeFileId, String coverLetter) {
        log.info("User {} applying for job {}", user.getEmail(), jobId);
        
        JobSeekerProfileDto profile = userClient.getProfileByUserId(user.getId());
        JobPostDto jobPost = jobClient.getJobById(jobId);

        if (jobPost.getDeadline() != null && LocalDate.now().isAfter(jobPost.getDeadline())) {
            throw new RuntimeException("Registration closed");
        }

        Application existing = applicationRepository.findTopByJobSeekerIdAndJobPostIdOrderByAppliedAtDesc(profile.getId(), jobId)
                .orElse(null);

        if (existing != null && existing.getStatus() != Application.ApplicationStatus.WITHDRAWN) {
            throw new RuntimeException("You have already applied for this job");
        }

        Application application = (existing != null) ? existing : new Application();
        Application.ApplicationStatus oldStatus = (existing != null) ? existing.getStatus() : null;
        
        application.setJobPostId(jobId);
        application.setJobSeekerId(profile.getId());
        application.setCoverLetter(coverLetter);
        application.setStatus(Application.ApplicationStatus.APPLIED);
        application.setResumeFileId(resumeFileId);
        application.setAppliedAt(LocalDateTime.now());
        application.setWithdrawReason(null);

        Application savedApp = applicationRepository.save(application);

        logHistory(savedApp, oldStatus != null ? oldStatus.name() : "", "APPLIED", user.getId(), 
                (existing != null) ? "Re-applied after withdrawal" : "Initial application");
        sendNotifications(user, jobPost, (existing != null) ? "re-applied for" : "applied for", 
                (existing != null) ? "Re-application received" : "New application received");
        logAudit(savedApp.getId(), (existing != null) ? "APPLICATION_REAPPLIED" : "APPLICATION_SUBMITTED", 
                oldStatus != null ? oldStatus.name() : null, "APPLIED", user.getId());

        return mapToDto(savedApp);
    }

    private void logHistory(Application app, String oldStatus, String newStatus, Long changedById, String comment) {
        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplication(app);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedById(changedById);
        history.setComment(comment);
        statusHistoryRepository.save(history);
    }

    private void sendNotifications(UserPrincipal user, JobPostDto jobPost, String seekerAction, String employerSubject) {
        try {
            // Seeker Notification
            NotificationRequest seekerNotif = new NotificationRequest();
            seekerNotif.setUserId(user.getId());
            seekerNotif.setUserEmail(user.getEmail());
            seekerNotif.setMessage("You have " + seekerAction + " the " + jobPost.getTitle() + " position at " + jobPost.getCompanyName());
            notificationClient.createNotification(seekerNotif);

            // Employer Notification
            NotificationRequest employerNotif = new NotificationRequest();
            employerNotif.setUserId(jobPost.getCreatedById());
            employerNotif.setMessage(employerSubject + " for " + jobPost.getTitle() + " from " + user.getUsername());
            notificationClient.createNotification(employerNotif);
        } catch (Exception e) {
            log.error("Failed to send notifications", e);
        }
    }

    private void logAudit(Long entityId, String action, String oldValue, String newValue, Long userId) {
        AuditLogRequest audit = new AuditLogRequest();
        audit.setEntityType("Application");
        audit.setEntityId(entityId);
        audit.setAction(action);
        audit.setOldValue(oldValue);
        audit.setNewValue(newValue);
        audit.setChangedById(userId);
        try {
            auditLogClient.logAction(audit);
        } catch (Exception e) {
            log.error("Failed to log audit action", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByUserId(Long userId) {
        JobSeekerProfileDto profile = userClient.getProfileByUserId(userId);
        return applicationRepository.findByJobSeekerId(profile.getId()).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getMyApplications(UserPrincipal user) {
        JobSeekerProfileDto profile = userClient.getProfileByUserId(user.getId());
        return applicationRepository.findByJobSeekerId(profile.getId()).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsForEmployer(UserPrincipal employer) {
        return applicationRepository.findAll().stream()
                .filter(app -> {
                    try {
                        JobPostDto job = jobClient.getJobById(app.getJobPostId());
                        return job.getCreatedById().equals(employer.getId());
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsForJob(Long jobId, UserPrincipal employer) {
        JobPostDto jobPost = jobClient.getJobById(jobId);
        if (!jobPost.getCreatedById().equals(employer.getId())) {
            throw new RuntimeException("Unauthorized to view these applications");
        }
        return applicationRepository.findByJobPostId(jobId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId, Application.ApplicationStatus status,
            UserPrincipal employer) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        JobPostDto jobPost = jobClient.getJobById(application.getJobPostId());
        if (!jobPost.getCreatedById().equals(employer.getId())) {
            throw new RuntimeException("Unauthorized to update this application");
        }

        Application.ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(status);
        Application savedApp = applicationRepository.save(application);

        logHistory(savedApp, oldStatus != null ? oldStatus.name() : "", status.name(), employer.getId(), "Status updated by employer");

        try {
            JobSeekerProfileDto profile = userClient.getProfileById(application.getJobSeekerId());
            UserDto receiver = userClient.getUserById(profile.getUserId());

            NotificationRequest notif = new NotificationRequest();
            notif.setUserId(receiver.getId());
            notif.setUserEmail(receiver.getEmail());
            notif.setMessage("Your application for " + jobPost.getTitle() + " has been updated to " + status);
            notificationClient.createNotification(notif);
        } catch (Exception e) {
            log.error("Failed to send status update notification", e);
        }

        return mapToDto(savedApp);
    }

    @Transactional
    public List<ApplicationResponse> updateBulkStatus(List<Long> applicationIds,
            Application.ApplicationStatus status,
            UserPrincipal employer) {
        return applicationIds.stream()
                .map(id -> updateApplicationStatus(id, status, employer))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> searchApplicantsForJob(Long jobId, String name, String skill,
            String experience, String education, String appliedAfter,
            Application.ApplicationStatus status, UserPrincipal employer) {
        
        JobPostDto jobPost = jobClient.getJobById(jobId);
        if (!jobPost.getCreatedById().equals(employer.getId())) {
            throw new RuntimeException("Unauthorized to view these applications");
        }

        List<Application> applications = applicationRepository.findByJobPostId(jobId);

        return applications.stream()
                .map(this::mapToDto)
                .filter(dto -> {
                    if (name != null && !name.trim().isEmpty()) {
                        String seekerName = dto.getJobSeekerName() == null ? "" : dto.getJobSeekerName().toLowerCase();
                        if (!seekerName.contains(name.toLowerCase())) return false;
                    }
                    if (skill != null && !skill.trim().isEmpty()) {
                        String skills = dto.getJobSeekerSkills() == null ? "" : dto.getJobSeekerSkills().toLowerCase();
                        if (!skills.contains(skill.toLowerCase())) return false;
                    }
                    if (experience != null && !experience.trim().isEmpty()) {
                        String exp = dto.getJobSeekerExperience() == null ? "" : dto.getJobSeekerExperience().toLowerCase();
                        if (!exp.contains(experience.toLowerCase())) return false;
                    }
                    if (education != null && !education.trim().isEmpty()) {
                        String edu = dto.getJobSeekerEducation() == null ? "" : dto.getJobSeekerEducation().toLowerCase();
                        if (!edu.contains(education.toLowerCase())) return false;
                    }
                    if (status != null && dto.getStatus() != status) return false;
                    if (appliedAfter != null && !appliedAfter.trim().isEmpty()) {
                        try {
                            LocalDateTime afterDate = LocalDateTime.parse(appliedAfter, DateTimeFormatter.ISO_DATE_TIME);
                            if (dto.getAppliedAt() == null || dto.getAppliedAt().isBefore(afterDate)) return false;
                        } catch (Exception e) {
                            log.warn("Invalid date format: {}", appliedAfter);
                        }
                    }
                    return true;
                })
                .toList();
    }

    @Transactional
    public void deleteApplication(Long applicationId, UserPrincipal user) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        JobSeekerProfileDto profile = userClient.getProfileById(application.getJobSeekerId());
        JobPostDto jobPost = jobClient.getJobById(application.getJobPostId());

        boolean isSeeker = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_JOB_SEEKER"))
                && profile.getUserId().equals(user.getId());
        boolean isEmployer = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYER"))
                && jobPost.getCreatedById().equals(user.getId());

        if (!isSeeker && !isEmployer) {
            throw new IllegalStateException("Unauthorized to delete this application");
        }

        applicationRepository.delete(application);
        logAudit(applicationId, "APPLICATION_DELETED", application.getStatus().name(), null, user.getId());
    }

    @Transactional(readOnly = true)
    public ApplicationSummaryResponse getApplicationSummary(Long jobId, UserPrincipal employer) {
        JobPostDto jobPost = jobClient.getJobById(jobId);
        if (!jobPost.getCreatedById().equals(employer.getId())) {
            throw new IllegalStateException("Unauthorized to view this summary");
        }

        List<Application> applications = applicationRepository.findByJobPostId(jobId);
        Map<String, Long> statusCounts = applications.stream()
                .collect(Collectors.groupingBy(app -> app.getStatus().name(), Collectors.counting()));

        return ApplicationSummaryResponse.builder()
                .jobId(jobId)
                .jobTitle(jobPost.getTitle())
                .totalApplications((long) applications.size())
                .statusCounts(statusCounts)
                .build();
    }

    public org.springframework.http.ResponseEntity<byte[]> downloadResume(Long applicationId, UserPrincipal employer) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        JobPostDto jobPost = jobClient.getJobById(application.getJobPostId());
        if (!jobPost.getCreatedById().equals(employer.getId())) {
            throw new RuntimeException("Unauthorized to download resume for this application");
        }

        Long seekerId = application.getJobSeekerId();
        JobSeekerProfileDto profile = resolveSeekerProfile(seekerId);
        if (profile != null && profile.getId() != null) {
            seekerId = profile.getId();
        }

        if (application.getResumeFileId() != null) {
            try {
                return resumeClient.downloadResumeById(application.getResumeFileId());
            } catch (Exception e) {
                log.warn("Failed to download resume by file id {}", application.getResumeFileId());
            }
        }

        try {
            ResumeFileDto resume = resumeClient.getResumeByJobSeekerId(seekerId);
            if (resume != null && resume.getId() != null) {
                return resumeClient.downloadResumeById(resume.getId());
            }
        } catch (Exception e) {
            log.warn("Failed to lookup resume by seeker {}", seekerId);
        }

        try {
            return resumeClient.downloadResumeBySeeker(seekerId);
        } catch (Exception e) {
            throw new RuntimeException("Resume file not found for this applicant.");
        }
    }

    public ApplicationResponse mapToDto(Application app) {
        ApplicationResponse dto = new ApplicationResponse();
        dto.setId(app.getId());
        dto.setJobId(app.getJobPostId());
        dto.setJobSeekerId(app.getJobSeekerId());
        dto.setStatus(app.getStatus());
        dto.setCoverLetter(app.getCoverLetter());
        dto.setAppliedAt(app.getAppliedAt());
        dto.setWithdrawReason(app.getWithdrawReason());

        try {
            JobPostDto jobPost = jobClient.getJobById(app.getJobPostId());
            dto.setJobTitle(jobPost.getTitle());
            dto.setCompanyName(jobPost.getCompanyName());
            dto.setCompanyLogo(jobPost.getCompanyLogo());
        } catch (Exception e) {
            log.warn("Failed to fetch job info for application {}", app.getId());
        }

        JobSeekerProfileDto profile = resolveSeekerProfile(app.getJobSeekerId());
        if (profile != null) {
            String seekerName = profile.getUser() != null ? profile.getUser().getName() : profile.getName();
            String seekerEmail = profile.getUser() != null ? profile.getUser().getEmail() : profile.getEmail();
            dto.setJobSeekerName(seekerName);
            dto.setJobSeekerEmail(seekerEmail);
            dto.setJobSeekerProfileImage(profile.getProfileImage());
            if (profile.getId() != null) {
                dto.setJobSeekerId(profile.getId());
            }
        } else {
            try {
                UserDto user = userClient.getUserById(app.getJobSeekerId());
                if (user != null) {
                    dto.setJobSeekerName(user.getName());
                    dto.setJobSeekerEmail(user.getEmail());
                }
            } catch (Exception ignored) {
                // Keep DTO usable even if user lookup fails.
            }
            log.warn("Failed to fetch seeker info for application {}", app.getId());
        }

        Long resumeSeekerId = profile != null && profile.getId() != null ? profile.getId() : app.getJobSeekerId();
        try {
            ResumeDetailsDto resumeDetails = resumeClient.getResumeDetails(resumeSeekerId);
            if (resumeDetails != null) {
                dto.setJobSeekerSkills(resumeDetails.getSkills());
                dto.setJobSeekerExperience(resumeDetails.getExperience());
                dto.setJobSeekerEducation(resumeDetails.getEducation());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch resume details for application {}", app.getId());
        }

        return dto;
    }

    private JobSeekerProfileDto resolveSeekerProfile(Long seekerId) {
        try {
            return userClient.getProfileById(seekerId);
        } catch (Exception e) {
            try {
                return userClient.getProfileByUserId(seekerId);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    @Transactional(readOnly = true)
    public Optional<ApplicationResponse> getApplicationById(Long applicationId) {
        return applicationRepository.findById(applicationId).map(this::mapToDto);
    }
}
