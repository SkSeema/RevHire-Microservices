package com.revhire.jobservice.repository;

import com.revhire.jobservice.model.SkillsMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkillsMasterRepository extends JpaRepository<SkillsMaster, Long> {
    Optional<SkillsMaster> findBySkillNameIgnoreCase(String name);
}
