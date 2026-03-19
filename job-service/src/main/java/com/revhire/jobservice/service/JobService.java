package com.revhire.jobservice.service;

import com.revhire.jobservice.dto.request.JobPostRequest;
import com.revhire.jobservice.dto.response.JobPostResponse;
import com.revhire.jobservice.dto.response.SkillResponse;
import com.revhire.jobservice.model.*;
import com.revhire.jobservice.repository.*;
import com.revhire.jobservice.security.UserPrincipal;
import com.revhire.jobservice.client.AuditLogClient;
import com.revhire.jobservice.dto.external.AuditLogRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobPostRepository jobPostRepository;
    private final CompanyRepository companyRepository;
    private final SkillsMasterRepository skillsMasterRepository;
    private final JobSkillMapRepository jobSkillMapRepository;
    private final AuditLogClient auditLogClient;

    @Transactional
    public JobPostResponse createJob(JobPostRequest jobPostDto, UserPrincipal user) {
        Company company;
        if (jobPostDto.getCompanyId() != null) {
            company = companyRepository.findById(jobPostDto.getCompanyId())
                    .orElseThrow(() -> new IllegalArgumentException("Company not found"));
            if (company.getCreatedById() != null && !company.getCreatedById().equals(user.getId())) {
                throw new IllegalStateException("Unauthorized: Company does not belong to the current user");
            }
        } else {
            List<Company> companies = companyRepository.findByCreatedById(user.getId());
            if (companies.isEmpty()) {
                throw new IllegalStateException("Company profile not found. Please create it first.");
            }
            company = companies.get(0);
        }

        JobPost jobPost = new JobPost();
        jobPost.setTitle(jobPostDto.getTitle());
        jobPost.setDescription(jobPostDto.getDescription());
        jobPost.setLocation(jobPostDto.getLocation());
        jobPost.setSalaryMin(parseSalary(jobPostDto.getSalary(), true));
        jobPost.setSalaryMax(parseSalary(jobPostDto.getSalary(), false));
        jobPost.setJobType(jobPostDto.getJobType());
        jobPost.setDeadline(jobPostDto.getDeadline() != null ? jobPostDto.getDeadline() : LocalDate.now().plusDays(30));
        jobPost.setExperienceYears(jobPostDto.getExperienceYears());
        jobPost.setEducation(jobPostDto.getEducation());
        jobPost.setOpenings(jobPostDto.getOpenings() != null ? jobPostDto.getOpenings() : 1);
        jobPost.setCompany(company);
        jobPost.setCreatedById(user.getId());
        jobPost.setStatus(JobPost.JobStatus.ACTIVE);

        JobPost savedJob = jobPostRepository.save(jobPost);

        if (jobPostDto.getSkills() != null && !jobPostDto.getSkills().trim().isEmpty()) {
            saveJobSkills(savedJob, jobPostDto.getSkills());
        }

        logAudit(savedJob.getId(), "JOB_CREATED", null, "Title: " + savedJob.getTitle(), user.getId());

        return mapToDto(savedJob);
    }

    private void saveJobSkills(JobPost job, String skillsStr) {
        List<String> skillNames = Arrays.stream(skillsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        for (String skillName : skillNames) {
            SkillsMaster skillMaster = skillsMasterRepository.findBySkillNameIgnoreCase(skillName)
                    .orElseGet(() -> {
                        SkillsMaster master = new SkillsMaster();
                        master.setSkillName(skillName);
                        return skillsMasterRepository.save(master);
                    });
            JobSkillMap skillMap = new JobSkillMap();
            skillMap.setJobPost(job);
            skillMap.setSkill(skillMaster);
            skillMap.setMandatory(true);
            jobSkillMapRepository.save(skillMap);
        }
    }

    private void logAudit(Long entityId, String action, String oldValue, String newValue, Long userId) {
        AuditLogRequest req = new AuditLogRequest();
        req.setEntityName("JobPost");
        req.setEntityId(entityId);
        req.setAction(action);
        req.setOldValue(oldValue);
        req.setNewValue(newValue);
        req.setChangedById(userId);
        try {
            auditLogClient.logAction(req);
        } catch (Exception e) {
            log.error("Failed to log audit action", e);
        }
    }

    private Double parseSalary(String salary, boolean isMin) {
        if (salary == null || salary.isEmpty()) return 0.0;
        try {
            String[] parts = salary.split("-");
            String targetPart = parts[isMin ? 0 : (parts.length > 1 ? 1 : 0)].replaceAll("[^0-9.]", "");
            return targetPart.isEmpty() ? 0.0 : Double.parseDouble(targetPart);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public List<JobPostResponse> getAllJobs() {
        return jobPostRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public JobPost getJobById(Long id) {
        return jobPostRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Job not found"));
    }

    public List<JobPostResponse> getMyJobs(UserPrincipal user) {
        return jobPostRepository.findByCreatedById(user.getId()).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<JobPostResponse> getRecommendedJobs(UserPrincipal user) {
        return jobPostRepository.findAll().stream()
                .filter(j -> j.getStatus() == JobPost.JobStatus.ACTIVE)
                .limit(10)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public JobPostResponse updateJob(Long id, JobPostRequest jobDto, UserPrincipal user) {
        JobPost job = getJobById(id);
        if (!job.getCreatedById().equals(user.getId())) {
            throw new IllegalStateException("Unauthorized");
        }

        String oldTitle = job.getTitle();
        job.setTitle(jobDto.getTitle());
        job.setDescription(jobDto.getDescription());
        job.setLocation(jobDto.getLocation());
        job.setSalaryMin(parseSalary(jobDto.getSalary(), true));
        job.setSalaryMax(parseSalary(jobDto.getSalary(), false));
        job.setJobType(jobDto.getJobType());
        if (jobDto.getDeadline() != null) job.setDeadline(jobDto.getDeadline());
        
        JobPost updatedJob = jobPostRepository.save(job);

        if (jobDto.getSkills() != null) {
            jobSkillMapRepository.deleteAll(jobSkillMapRepository.findByJobPostId(updatedJob.getId()));
            saveJobSkills(updatedJob, jobDto.getSkills());
        }

        logAudit(updatedJob.getId(), "JOB_UPDATED", "Old Title: " + oldTitle, "New Title: " + updatedJob.getTitle(), user.getId());

        return mapToDto(updatedJob);
    }

    @Transactional
    public JobPostResponse updateJobStatus(Long id, JobPost.JobStatus status, UserPrincipal user) {
        JobPost job = getJobById(id);
        if (!job.getCreatedById().equals(user.getId())) throw new IllegalStateException("Unauthorized");

        String oldStatus = job.getStatus().name();
        job.setStatus(status);
        JobPost updatedJob = jobPostRepository.save(job);

        logAudit(updatedJob.getId(), "JOB_STATUS_UPDATED", "Old: " + oldStatus, "New: " + status.name(), user.getId());

        return mapToDto(updatedJob);
    }

    @Transactional
    public void deleteJob(Long id, UserPrincipal user) {
        JobPost job = getJobById(id);
        if (!job.getCreatedById().equals(user.getId())) throw new IllegalStateException("Unauthorized");

        jobSkillMapRepository.deleteAll(jobSkillMapRepository.findByJobPostId(id));
        // Note: Applications should be handled by application-service
        
        logAudit(id, "JOB_DELETED", "Title: " + job.getTitle(), null, user.getId());
        jobPostRepository.delete(job);
    }

    public List<SkillResponse> getJobSkills(Long id) {
        return jobSkillMapRepository.findByJobPostId(id).stream()
                .map(map -> {
                    SkillResponse res = new SkillResponse();
                    res.setId(map.getSkill().getId());
                    res.setName(map.getSkill().getSkillName());
                    return res;
                })
                .toList();
    }

    public JobPostResponse mapToDto(JobPost jobPost) {
        JobPostResponse dto = new JobPostResponse();
        dto.setId(jobPost.getId());
        dto.setTitle(jobPost.getTitle());
        dto.setDescription(jobPost.getDescription());
        dto.setLocation(jobPost.getLocation());
        dto.setSalary(jobPost.getSalaryMin() + " - " + jobPost.getSalaryMax());
        dto.setJobType(jobPost.getJobType());
        dto.setDeadline(jobPost.getDeadline());
        dto.setExperienceYears(jobPost.getExperienceYears());
        dto.setEducation(jobPost.getEducation());
        dto.setOpenings(jobPost.getOpenings());
        dto.setStatus(jobPost.getStatus() != null ? jobPost.getStatus().name() : null);
        dto.setPostedDate(jobPost.getCreatedAt() != null ? jobPost.getCreatedAt().toLocalDate() : LocalDate.now());
        dto.setCompanyId(jobPost.getCompany().getId());
        dto.setCompanyName(jobPost.getCompany().getName());
        dto.setCompanyLogo(jobPost.getCompany().getLogo());
        dto.setCreatedById(jobPost.getCreatedById());
        
        List<JobSkillMap> skillMaps = jobSkillMapRepository.findByJobPostId(jobPost.getId());
        if (!skillMaps.isEmpty()) {
            dto.setSkills(skillMaps.stream().map(s -> s.getSkill().getSkillName()).collect(Collectors.joining(", ")));
        }
        
        return dto;
    }
}
