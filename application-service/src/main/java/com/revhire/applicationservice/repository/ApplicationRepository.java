package com.revhire.applicationservice.repository;

import com.revhire.applicationservice.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByJobPostId(Long jobId);

    Long countByJobPostId(Long jobId);

    List<Application> findByJobSeekerId(Long seekerId);

    java.util.Optional<Application> findTopByJobSeekerIdAndJobPostIdOrderByAppliedAtDesc(Long seekerId, Long jobId);

    long countByStatus(Application.ApplicationStatus status);
}
