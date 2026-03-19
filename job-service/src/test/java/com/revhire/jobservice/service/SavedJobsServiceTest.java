package com.revhire.jobservice.service;

import com.revhire.jobservice.dto.response.JobPostResponse;
import com.revhire.jobservice.model.Company;
import com.revhire.jobservice.model.JobPost;
import com.revhire.jobservice.model.SavedJobs;
import com.revhire.jobservice.repository.JobPostRepository;
import com.revhire.jobservice.repository.SavedJobsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavedJobsServiceTest {

    @Mock
    private SavedJobsRepository savedJobsRepository;

    @Mock
    private JobPostRepository jobPostRepository;

    @InjectMocks
    private SavedJobsService savedJobsService;

    @Test
    void saveJobThrowsWhenAlreadySaved() {
        JobPost jobPost = new JobPost();
        jobPost.setId(11L);

        when(jobPostRepository.findById(11L)).thenReturn(Optional.of(jobPost));
        when(savedJobsRepository.findBySeekerIdAndJobPostId(21L, 11L)).thenReturn(Optional.of(new SavedJobs()));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> savedJobsService.saveJob(11L, 21L)
        );

        assertEquals("Job already saved", exception.getMessage());
    }

    @Test
    void getSavedJobsMapsResponseFields() {
        Company company = new Company();
        company.setId(3L);
        company.setName("RevHire");
        company.setLogo("logo.png");

        JobPost jobPost = new JobPost();
        jobPost.setId(11L);
        jobPost.setTitle("Java Developer");
        jobPost.setDescription("Build APIs");
        jobPost.setLocation("Chennai");
        jobPost.setSalaryMin(4_00_000.0);
        jobPost.setSalaryMax(8_00_000.0);
        jobPost.setJobType("Full-time");
        jobPost.setCreatedAt(LocalDateTime.of(2026, 3, 1, 10, 30));
        jobPost.setCompany(company);

        SavedJobs savedJobs = new SavedJobs();
        savedJobs.setSeekerId(21L);
        savedJobs.setJobPost(jobPost);

        when(savedJobsRepository.findBySeekerId(21L)).thenReturn(List.of(savedJobs));

        List<JobPostResponse> results = savedJobsService.getSavedJobs(21L);

        assertEquals(1, results.size());
        assertEquals("Java Developer", results.getFirst().getTitle());
        assertEquals("RevHire", results.getFirst().getCompanyName());
        assertEquals("400000.0 - 800000.0", results.getFirst().getSalary());
        assertEquals("logo.png", results.getFirst().getCompanyLogo());
        assertEquals(jobPost.getCreatedAt().toLocalDate(), results.getFirst().getPostedDate());
        verify(savedJobsRepository).findBySeekerId(21L);
    }
}
