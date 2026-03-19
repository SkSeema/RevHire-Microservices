package com.revhire.applicationservice.repository;

import com.revhire.applicationservice.model.ApplicationNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationNotesRepository extends JpaRepository<ApplicationNotes, Long> {
    List<ApplicationNotes> findByApplicationId(Long applicationId);

    List<ApplicationNotes> findByCreatedById(Long authorId);
}
