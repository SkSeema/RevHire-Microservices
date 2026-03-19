package com.revhire.applicationservice.service;

import com.revhire.applicationservice.client.JobClient;
import com.revhire.applicationservice.client.NotificationClient;
import com.revhire.applicationservice.client.UserClient;
import com.revhire.applicationservice.dto.external.JobPostDto;
import com.revhire.applicationservice.dto.external.JobSeekerProfileDto;
import com.revhire.applicationservice.dto.external.NotificationRequest;
import com.revhire.applicationservice.dto.response.ApplicationResponse;
import com.revhire.applicationservice.model.Application;
import com.revhire.applicationservice.model.ApplicationStatusHistory;
import com.revhire.applicationservice.repository.ApplicationRepository;
import com.revhire.applicationservice.repository.ApplicationStatusHistoryRepository;
import com.revhire.applicationservice.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationWithdrawalServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationStatusHistoryRepository statusHistoryRepository;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private UserClient userClient;

    @Mock
    private JobClient jobClient;

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private ApplicationWithdrawalService applicationWithdrawalService;

    @Test
    void withdrawApplicationThrowsWhenUserDoesNotOwnApplication() {
        Application application = new Application();
        application.setId(8L);
        application.setJobSeekerId(22L);

        JobSeekerProfileDto profile = new JobSeekerProfileDto();
        profile.setUserId(99L);

        UserPrincipal user = new UserPrincipal(11L, "seeker@revhire.com", "JOB_SEEKER");

        when(applicationRepository.findById(8L)).thenReturn(Optional.of(application));
        when(userClient.getProfileById(22L)).thenReturn(profile);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> applicationWithdrawalService.withdrawApplication(8L, "Changed my mind", user)
        );

        assertEquals("Unauthorized to withdraw this application", exception.getMessage());
        verify(statusHistoryRepository, never()).save(any());
    }

    @Test
    void withdrawApplicationUpdatesStatusCreatesHistoryAndSendsNotifications() {
        Application application = new Application();
        application.setId(8L);
        application.setJobSeekerId(22L);
        application.setJobPostId(44L);
        application.setStatus(Application.ApplicationStatus.APPLIED);

        JobSeekerProfileDto profile = new JobSeekerProfileDto();
        profile.setUserId(11L);

        JobPostDto jobPost = new JobPostDto();
        jobPost.setId(44L);
        jobPost.setTitle("Backend Engineer");
        jobPost.setCompanyName("RevHire");
        jobPost.setCreatedById(77L);

        UserPrincipal user = new UserPrincipal(11L, "seeker@revhire.com", "JOB_SEEKER");
        ApplicationResponse response = new ApplicationResponse();
        response.setId(8L);
        response.setStatus(Application.ApplicationStatus.WITHDRAWN);

        when(applicationRepository.findById(8L)).thenReturn(Optional.of(application));
        when(userClient.getProfileById(22L)).thenReturn(profile);
        when(applicationRepository.save(application)).thenReturn(application);
        when(jobClient.getJobById(44L)).thenReturn(jobPost);
        when(applicationService.mapToDto(application)).thenReturn(response);

        ApplicationResponse result = applicationWithdrawalService.withdrawApplication(8L, "Found another role", user);

        assertEquals(Application.ApplicationStatus.WITHDRAWN, application.getStatus());
        assertEquals("Found another role", application.getWithdrawReason());
        assertEquals(response, result);

        ArgumentCaptor<ApplicationStatusHistory> historyCaptor = ArgumentCaptor.forClass(ApplicationStatusHistory.class);
        ArgumentCaptor<NotificationRequest> notificationCaptor = ArgumentCaptor.forClass(NotificationRequest.class);

        verify(statusHistoryRepository).save(historyCaptor.capture());
        verify(notificationClient, times(2)).createNotification(notificationCaptor.capture());

        assertEquals("APPLIED", historyCaptor.getValue().getOldStatus());
        assertEquals("WITHDRAWN", historyCaptor.getValue().getNewStatus());
        assertEquals(11L, historyCaptor.getValue().getChangedById());

        assertEquals(11L, notificationCaptor.getAllValues().get(0).getUserId());
        assertEquals(77L, notificationCaptor.getAllValues().get(1).getUserId());
    }
}
