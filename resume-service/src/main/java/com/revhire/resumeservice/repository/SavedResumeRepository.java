package com.revhire.resumeservice.repository;

import com.revhire.resumeservice.model.SavedResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedResumeRepository extends JpaRepository<SavedResume, Long> {

    List<SavedResume> findByEmployerId(Long employerId);

    @Query("SELECT s FROM SavedResume s WHERE s.employerId = :employerId AND s.jobSeekerId = :seekerId AND s.jobPostId = :jobId")
    Optional<SavedResume> findByEmployerIdAndJobSeekerIdAndJobPostId(
            @Param("employerId") Long employerId,
            @Param("seekerId") Long seekerId,
            @Param("jobId") Long jobId);

    @Query("SELECT COUNT(s) > 0 FROM SavedResume s WHERE s.employerId = :employerId AND s.jobSeekerId = :seekerId AND s.jobPostId = :jobId")
    boolean existsByEmployerIdAndJobSeekerIdAndJobPostId(
            @Param("employerId") Long employerId,
            @Param("seekerId") Long seekerId,
            @Param("jobId") Long jobId);
}
