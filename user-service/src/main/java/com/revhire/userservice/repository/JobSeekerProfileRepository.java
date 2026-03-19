package com.revhire.userservice.repository;

import com.revhire.userservice.model.JobSeekerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobSeekerProfileRepository extends JpaRepository<JobSeekerProfile, Long> {
    Optional<JobSeekerProfile> findByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query(
            "SELECT p FROM JobSeekerProfile p WHERE LOWER(p.user.name) LIKE LOWER(CONCAT('%', :keyword, '%'))"
    )
    List<JobSeekerProfile> searchByKeyword(
            @org.springframework.data.repository.query.Param("keyword") String keyword);
}
