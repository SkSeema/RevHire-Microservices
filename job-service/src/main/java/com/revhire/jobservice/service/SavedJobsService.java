package com.revhire.jobservice.service;

import com.revhire.jobservice.dto.response.JobPostResponse;
import com.revhire.jobservice.model.JobPost;
import com.revhire.jobservice.model.SavedJobs;
import com.revhire.jobservice.repository.JobPostRepository;
import com.revhire.jobservice.repository.SavedJobsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavedJobsService {

    private final SavedJobsRepository savedJobsRepository;
    private final JobPostRepository jobPostRepository;

    @Transactional
    public void saveJob(Long jobId, Long userId) {
        JobPost jobPost = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (savedJobsRepository.findBySeekerIdAndJobPostId(userId, jobId).isPresent()) {
            throw new RuntimeException("Job already saved");
        }

        SavedJobs savedJobs = new SavedJobs();
        savedJobs.setSeekerId(userId);
        savedJobs.setJobPost(jobPost);
        savedJobsRepository.save(savedJobs);
    }

    @Transactional
    public void unsaveJob(Long jobId, Long userId) {
        SavedJobs savedJob = savedJobsRepository.findBySeekerIdAndJobPostId(userId, jobId)
                .orElseThrow(() -> new RuntimeException("Saved job not found"));

        savedJobsRepository.delete(savedJob);
    }

    public List<JobPostResponse> getSavedJobs(Long userId) {
        return savedJobsRepository.findBySeekerId(userId).stream()
                .map(sj -> mapToDto(sj.getJobPost()))
                .collect(Collectors.toList());
    }

    private JobPostResponse mapToDto(JobPost jobPost) {
        JobPostResponse dto = new JobPostResponse();
        dto.setId(jobPost.getId());
        dto.setTitle(jobPost.getTitle());
        dto.setDescription(jobPost.getDescription());
        dto.setLocation(jobPost.getLocation());
        dto.setSalary(jobPost.getSalaryMin() + " - " + jobPost.getSalaryMax());
        dto.setJobType(jobPost.getJobType());
        dto.setPostedDate(jobPost.getCreatedAt() != null ? jobPost.getCreatedAt().toLocalDate() : LocalDate.now());
        if (jobPost.getCompany() != null) {
            dto.setCompanyName(jobPost.getCompany().getName());
            dto.setCompanyLogo(jobPost.getCompany().getLogo());
        }
        return dto;
    }
}
