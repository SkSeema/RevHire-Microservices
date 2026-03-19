package com.revhire.resumeservice.repository;

import com.revhire.resumeservice.model.SkillsMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkillsMasterRepository extends JpaRepository<SkillsMaster, Long> {
    Optional<SkillsMaster> findBySkillNameIgnoreCase(String skillName);
}
