package com.revhire.jobservice.service;

import com.revhire.jobservice.dto.response.JobPostResponse;
import com.revhire.jobservice.model.JobPost;
import com.revhire.jobservice.repository.JobPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final JobPostRepository jobPostRepository;

    public List<JobPostResponse> searchJobs(String keyword) {
        log.info("Searching jobs with keyword: {}", keyword);
        return jobPostRepository.searchByKeyword(keyword).stream()
                .map(this::mapToJobDto)
                .collect(Collectors.toList());
    }

    private JobPostResponse mapToJobDto(JobPost jobPost) {
        JobPostResponse dto = new JobPostResponse();
        dto.setId(jobPost.getId());
        dto.setTitle(jobPost.getTitle());
        dto.setDescription(jobPost.getDescription());
        dto.setLocation(jobPost.getLocation());
        dto.setSalary(jobPost.getSalaryMin() + " - " + jobPost.getSalaryMax());
        dto.setJobType(jobPost.getJobType());
        if (jobPost.getCompany() != null) {
            dto.setCompanyName(jobPost.getCompany().getName());
            dto.setCompanyLogo(jobPost.getCompany().getLogo());
        }
        dto.setPostedDate(jobPost.getCreatedAt() != null ? jobPost.getCreatedAt().toLocalDate() : LocalDate.now());
        // applicantCount should be fetched from Application Service if needed
        return dto;
    }
}
