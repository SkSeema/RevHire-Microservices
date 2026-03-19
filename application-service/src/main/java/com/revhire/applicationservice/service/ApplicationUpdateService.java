package com.revhire.applicationservice.service;

import com.revhire.applicationservice.model.Application;
import com.revhire.applicationservice.model.ApplicationNotes;
import com.revhire.applicationservice.security.UserPrincipal;
import com.revhire.applicationservice.repository.ApplicationNotesRepository;
import com.revhire.applicationservice.repository.ApplicationRepository;
import com.revhire.applicationservice.client.*;
import com.revhire.applicationservice.dto.external.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationUpdateService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationNotesRepository notesRepository;
    private final JobClient jobClient;

    @Transactional
    public void addNoteToApplication(Long applicationId, String noteText, UserPrincipal employer) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        JobPostDto jobPost = jobClient.getJobById(application.getJobPostId());
        if (!jobPost.getCreatedById().equals(employer.getId())) {
            throw new RuntimeException("Unauthorized to add note to this application");
        }

        ApplicationNotes note = new ApplicationNotes();
        note.setApplication(application);
        note.setNoteText(noteText);
        note.setCreatedById(employer.getId());

        notesRepository.save(note);
    }
}
