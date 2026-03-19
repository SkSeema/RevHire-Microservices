package com.revhire.jobservice.service;

import com.revhire.jobservice.dto.response.JobPostResponse;
import com.revhire.jobservice.model.JobPost;
import com.revhire.jobservice.repository.JobPostRepository;
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
public class JobSearchService {

    private final JobPostRepository jobPostRepository;

    @Transactional(readOnly = true)
    public List<JobPostResponse> searchJobs(String title, String location, Integer experience, String company,
            Double salary,
            List<String> jobTypes, Integer daysAgo) {
        java.time.LocalDateTime startDate = null;
        if (daysAgo != null) {
            startDate = java.time.LocalDateTime.now().minusDays(daysAgo);
        }
        boolean useTypeFilter = (jobTypes != null && !jobTypes.isEmpty());
        return jobPostRepository
                .findByFilters(title, location, experience, company, salary, jobTypes, useTypeFilter, startDate)
                .stream()
                .map(this::mapToDto)
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
        dto.setCompanyId(jobPost.getCompany().getId());
        dto.setCompanyName(jobPost.getCompany().getName());
        dto.setCompanyLogo(jobPost.getCompany().getLogo());
        dto.setExperienceYears(jobPost.getExperienceYears());
        dto.setEducation(jobPost.getEducation());
        dto.setOpenings(jobPost.getOpenings());
        dto.setDeadline(jobPost.getDeadline());
        dto.setStatus(jobPost.getStatus() != null ? jobPost.getStatus().name() : null);
        dto.setApplicantCount(0L); // Applicant count would be fetched from application-service if needed
        return dto;
    }
}
