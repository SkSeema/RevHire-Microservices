package com.revhire.applicationservice.service;

import com.revhire.applicationservice.dto.response.ApplicationResponse;
import com.revhire.applicationservice.model.Application;
import com.revhire.applicationservice.security.UserPrincipal;
import com.revhire.applicationservice.model.ApplicationStatusHistory;
import com.revhire.applicationservice.repository.ApplicationRepository;
import com.revhire.applicationservice.repository.ApplicationStatusHistoryRepository;
import com.revhire.applicationservice.client.*;
import com.revhire.applicationservice.dto.external.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationWithdrawalService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final NotificationClient notificationClient;
    private final UserClient userClient;
    private final JobClient jobClient;
    private final ApplicationService applicationService;

    @Transactional
    public ApplicationResponse withdrawApplication(Long applicationId, String reason, UserPrincipal user) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        JobSeekerProfileDto profile = userClient.getProfileById(application.getJobSeekerId());
        if (!profile.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to withdraw this application");
        }

        Application.ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(Application.ApplicationStatus.WITHDRAWN);
        application.setWithdrawReason(reason);
        Application savedApp = applicationRepository.save(application);

        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplication(savedApp);
        history.setOldStatus(oldStatus != null ? oldStatus.name() : "");
        history.setNewStatus(Application.ApplicationStatus.WITHDRAWN.name());
        history.setChangedById(user.getId());
        history.setComment("Application withdrawn by job seeker: " + reason);
        statusHistoryRepository.save(history);

        try {
            JobPostDto jobPost = jobClient.getJobById(application.getJobPostId());

            NotificationRequest seekerNotif = new NotificationRequest();
            seekerNotif.setUserId(user.getId());
            seekerNotif.setUserEmail(user.getEmail());
            seekerNotif.setMessage("You have withdrawn your application for the " + jobPost.getTitle() + " position at " + jobPost.getCompanyName());
            notificationClient.createNotification(seekerNotif);

            NotificationRequest employerNotif = new NotificationRequest();
            employerNotif.setUserId(jobPost.getCreatedById());
            employerNotif.setMessage("Application withdrawn for " + jobPost.getTitle() + " by " + user.getUsername());
            notificationClient.createNotification(employerNotif);
        } catch (Exception e) {
            log.error("Failed to send withdrawal notifications", e);
        }

        return applicationService.mapToDto(savedApp);
    }
}
