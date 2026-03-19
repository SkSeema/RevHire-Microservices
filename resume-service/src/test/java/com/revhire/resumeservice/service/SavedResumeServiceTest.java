package com.revhire.resumeservice.service;

import com.revhire.resumeservice.client.JobClient;
import com.revhire.resumeservice.client.NotificationClient;
import com.revhire.resumeservice.client.UserClient;
import com.revhire.resumeservice.dto.external.NotificationRequest;
import com.revhire.resumeservice.repository.ResumeTextRepository;
import com.revhire.resumeservice.repository.SavedResumeRepository;
import com.revhire.resumeservice.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavedResumeServiceTest {

    @Mock
    private SavedResumeRepository savedResumeRepository;

    @Mock
    private ResumeTextRepository resumeTextRepository;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private UserClient userClient;

    @Mock
    private JobClient jobClient;

    @InjectMocks
    private SavedResumeService savedResumeService;

    @Test
    void saveResumeThrowsWhenAlreadySaved() {
        UserPrincipal employer = new UserPrincipal(5L, "employer@revhire.com", "EMPLOYER");
        when(savedResumeRepository.existsByEmployerIdAndJobSeekerIdAndJobPostId(5L, 12L, 33L)).thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> savedResumeService.saveResume(12L, 33L, employer)
        );

        assertEquals("Resume already saved by this employer", exception.getMessage());
        verify(savedResumeRepository, never()).save(any());
    }

    @Test
    void saveResumePersistsAndSendsNotification() {
        UserPrincipal employer = new UserPrincipal(5L, "employer@revhire.com", "EMPLOYER");
        when(savedResumeRepository.existsByEmployerIdAndJobSeekerIdAndJobPostId(5L, 12L, 33L)).thenReturn(false);

        savedResumeService.saveResume(12L, 33L, employer);

        ArgumentCaptor<NotificationRequest> notificationCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(savedResumeRepository).save(any());
        verify(notificationClient).createNotification(notificationCaptor.capture());
        assertEquals(12L, notificationCaptor.getValue().getUserId());
        assertEquals("An employer has favorited your profile!", notificationCaptor.getValue().getMessage());
    }
}
