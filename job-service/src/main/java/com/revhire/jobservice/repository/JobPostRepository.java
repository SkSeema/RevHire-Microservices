package com.revhire.jobservice.repository;

import com.revhire.jobservice.model.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobPostRepository extends JpaRepository<JobPost, Long> {

    List<JobPost> findByCreatedById(Long createdById);

    @Query("SELECT j FROM JobPost j WHERE j.status = 'ACTIVE' AND " +
           "(:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:experience IS NULL OR j.experienceYears >= :experience) AND " +
           "(:company IS NULL OR LOWER(j.company.name) LIKE LOWER(CONCAT('%', :company, '%'))) AND " +
           "(:salary IS NULL OR j.salaryMin <= :salary) AND " +
           "(:useTypeFilter = false OR j.jobType IN :jobTypes) AND " +
           "(:startDate IS NULL OR j.createdAt >= :startDate)")
    List<JobPost> findByFilters(@Param("title") String title,
                                @Param("location") String location,
                                @Param("experience") Integer experience,
                                @Param("company") String company,
                                @Param("salary") Double salary,
                                @Param("jobTypes") List<String> jobTypes,
                                @Param("useTypeFilter") boolean useTypeFilter,
                                @Param("startDate") LocalDateTime startDate);

    @Query("SELECT j FROM JobPost j WHERE j.status = 'ACTIVE' AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.company.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<JobPost> searchByKeyword(@Param("keyword") String keyword);

    List<JobPost> findByCompanyId(Long companyId);

    long countByStatus(JobPost.JobStatus status);
}
