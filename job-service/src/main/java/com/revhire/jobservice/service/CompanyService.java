package com.revhire.jobservice.service;

import com.revhire.jobservice.dto.request.CompanyRequest;
import com.revhire.jobservice.dto.response.CompanyResponse;
import com.revhire.jobservice.model.Company;
import com.revhire.jobservice.repository.CompanyRepository;
import com.revhire.jobservice.repository.JobPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private static final String COMPANY_NOT_FOUND = "Company not found";

    private final CompanyRepository companyRepository;
    private final JobPostRepository jobPostRepository;

    @Transactional
    public CompanyResponse createOrUpdateCompanyProfile(CompanyRequest companyDto, Long userId) {
        log.info("Creating/Updating company: {} for user: {}", companyDto.getName(), userId);

        Company company;
        if (companyDto.getId() != null) {
            company = companyRepository.findById(companyDto.getId())
                    .orElseThrow(() -> new RuntimeException(COMPANY_NOT_FOUND));

            if (company.getCreatedById() != null && !company.getCreatedById().equals(userId)) {
                throw new RuntimeException("Unauthorized to update this company");
            }
        } else {
            List<Company> existingCompanies = companyRepository.findByCreatedById(userId);
            if (!existingCompanies.isEmpty()) {
                throw new RuntimeException("You can only have one company profile");
            }
            company = new Company();
            company.setCreatedById(userId);
        }

        company.setName(companyDto.getName());
        company.setDescription(companyDto.getDescription());
        company.setWebsite(companyDto.getWebsite());
        company.setLocation(companyDto.getLocation());
        company.setIndustry(companyDto.getIndustry());
        company.setSize(companyDto.getSize() != null ? companyDto.getSize() : "");
        if (companyDto.getLogo() != null) {
            company.setLogo(companyDto.getLogo());
        }

        company = companyRepository.save(company);

        return mapToDto(company);
    }

    public List<Company> getCompaniesForUser(Long userId) {
        return companyRepository.findByCreatedByIdOrderByNameAsc(userId);
    }

    public CompanyResponse getCompanyProfile(Long userId) {
        // Since we removed EmployerProfile in job-service, we find company by createdById
        List<Company> companies = companyRepository.findByCreatedById(userId);
        return companies.isEmpty() ? null : mapToDto(companies.get(0));
    }

    public CompanyResponse getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(COMPANY_NOT_FOUND));
        return mapToDto(company);
    }

    @Transactional
    public void deleteCompany(Long id, Long userId) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(COMPANY_NOT_FOUND));

        if (company.getCreatedById() != null && !company.getCreatedById().equals(userId)) {
            throw new IllegalStateException("Unauthorized to delete this company");
        }

        if (!jobPostRepository.findByCompanyId(id).isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete company with active job posts. Please delete the jobs first.");
        }

        companyRepository.delete(company);
        log.info("Company deleted: {} by user: {}", id, userId);
    }

    private CompanyResponse mapToDto(Company company) {
        CompanyResponse dto = new CompanyResponse();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setDescription(company.getDescription());
        dto.setWebsite(company.getWebsite());
        dto.setLocation(company.getLocation());
        dto.setIndustry(company.getIndustry());
        dto.setSize(company.getSize());
        dto.setLogo(company.getLogo());
        // User details are now managed by User Service
        return dto;
    }
}
