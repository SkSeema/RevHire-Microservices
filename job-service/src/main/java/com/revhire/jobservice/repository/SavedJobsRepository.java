package com.revhire.jobservice.repository;

import com.revhire.jobservice.model.SavedJobs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobsRepository extends JpaRepository<SavedJobs, Long> {
    List<SavedJobs> findBySeekerId(Long seekerId);

    Optional<SavedJobs> findBySeekerIdAndJobPostId(Long seekerId, Long jobPostId);

    List<SavedJobs> findByJobPostId(Long jobPostId);
}
