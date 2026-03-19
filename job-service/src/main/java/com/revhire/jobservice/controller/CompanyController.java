package com.revhire.jobservice.controller;

import com.revhire.jobservice.dto.request.CompanyRequest;
import com.revhire.jobservice.dto.response.CompanyResponse;
import com.revhire.jobservice.model.JobPost;
import com.revhire.jobservice.repository.JobPostRepository;
import com.revhire.jobservice.security.UserPrincipal;
import com.revhire.jobservice.service.CompanyService;
import com.revhire.jobservice.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company/register")
@RequiredArgsConstructor
@Slf4j
public class CompanyController {

    private final CompanyService companyService;
    private final JobPostRepository jobPostRepository;
    private final JobService jobService;

    @PostMapping
    public ResponseEntity<?> updateProfile(@Valid @RequestBody CompanyRequest companyDto,
                                           @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            companyDto.setId(null);
            CompanyResponse company = companyService.createOrUpdateCompanyProfile(companyDto, principal.getId());
            return ResponseEntity.ok(company);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/companies")
    public ResponseEntity<?> getMyCompanies(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        List<CompanyResponse> companies = companyService.getCompaniesForUser(principal.getId()).stream()
                .map(company -> companyService.getCompanyById(company.getId()))
                .toList();
        return ResponseEntity.ok(companies);
    }

    @GetMapping
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        CompanyResponse company = companyService.getCompanyProfile(principal.getId());
        if (company == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(company);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProfileById(@PathVariable("id") Long id) {
        try {
            CompanyResponse company = companyService.getCompanyById(id);
            return ResponseEntity.ok(company);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        List<JobPost> employerJobs = jobPostRepository.findByCreatedById(principal.getId());
        long totalJobs = employerJobs.size();
        long activeJobs = employerJobs.stream()
                .filter(j -> j.getStatus() == JobPost.JobStatus.ACTIVE)
                .count();

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalJobs", totalJobs);
        stats.put("activeJobs", activeJobs);
        // Application metrics should be fetched from Application Service
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCompany(@PathVariable("id") Long id, @Valid @RequestBody CompanyRequest companyDto,
                                           @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            companyDto.setId(id);
            CompanyResponse company = companyService.createOrUpdateCompanyProfile(companyDto, principal.getId());
            return ResponseEntity.ok(company);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable("id") Long id,
                                           @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            companyService.deleteCompany(id, principal.getId());
            return ResponseEntity.ok("Company deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/jobs")
    public ResponseEntity<?> getCompanyJobs(@PathVariable("id") Long id) {
        return ResponseEntity.ok(jobPostRepository.findByCompanyId(id).stream()
                .map(jobService::mapToDto)
                .toList());
    }
}
