package com.revhire.applicationservice.controller;

import com.revhire.applicationservice.dto.response.ApplicationNoteResponse;
import com.revhire.applicationservice.model.Application;
import com.revhire.applicationservice.model.ApplicationNotes;
import com.revhire.applicationservice.repository.ApplicationNotesRepository;
import com.revhire.applicationservice.repository.ApplicationRepository;
import com.revhire.applicationservice.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications/{applicationId}/notes")
@RequiredArgsConstructor
@Slf4j
public class ApplicationNotesController {

    private static final String UNAUTHORIZED_MSG = "Unauthorized";

    private final ApplicationNotesRepository notesRepository;
    private final ApplicationRepository applicationRepository;

    @GetMapping
    public ResponseEntity<?> getNotesForApplication(@PathVariable("applicationId") Long applicationId,
                                                    @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null || !userPrincipal.getRole().equals("EMPLOYER")) {
            return ResponseEntity.status(403).body(UNAUTHORIZED_MSG);
        }

        Application application = applicationRepository.findById(applicationId).orElse(null);
        if (application == null) {
            return ResponseEntity.badRequest().body("Application not found");
        }
        
        // Simple security check: any employer can view notes for now, or you could add job ownership check here
        // If you need job ownership check, you'd need JobClient to check who created the job
        
        List<ApplicationNoteResponse> notes = notesRepository.findByApplicationId(applicationId).stream()
                .map(this::mapToDto)
                .toList();

        return ResponseEntity.ok(notes);
    }

    @PostMapping
    public ResponseEntity<?> addNote(@PathVariable("applicationId") Long applicationId,
                                     @RequestBody String noteText,
                                     @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null || !userPrincipal.getRole().equals("EMPLOYER")) {
            return ResponseEntity.status(403).body(UNAUTHORIZED_MSG);
        }
        
        Application application = applicationRepository.findById(applicationId).orElse(null);
        if (application == null) {
            return ResponseEntity.badRequest().body("Application not found");
        }
        
        ApplicationNotes note = new ApplicationNotes();
        note.setApplication(application);
        note.setNoteText(noteText);
        note.setCreatedById(userPrincipal.getId());
        return ResponseEntity.ok(mapToDto(notesRepository.save(note)));
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<?> getNoteById(@PathVariable("noteId") Long noteId) {
        return notesRepository.findById(noteId)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<?> updateNote(@PathVariable("noteId") Long noteId,
                                        @RequestBody String noteText,
                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) return ResponseEntity.status(403).body(UNAUTHORIZED_MSG);
        
        return notesRepository.findById(noteId).map(note -> {
            if (!note.getCreatedById().equals(userPrincipal.getId())) {
                return ResponseEntity.status(403).body(UNAUTHORIZED_MSG);
            }
            note.setNoteText(noteText);
            return ResponseEntity.ok(mapToDto(notesRepository.save(note)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<?> deleteNote(@PathVariable("noteId") Long noteId,
                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) return ResponseEntity.status(403).body(UNAUTHORIZED_MSG);
        
        return notesRepository.findById(noteId).map(note -> {
            if (!note.getCreatedById().equals(userPrincipal.getId())) {
                return ResponseEntity.status(403).body(UNAUTHORIZED_MSG);
            }
            notesRepository.delete(note);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    private ApplicationNoteResponse mapToDto(ApplicationNotes note) {
        ApplicationNoteResponse dto = new ApplicationNoteResponse();
        dto.setId(note.getId());
        dto.setNoteText(note.getNoteText());
        dto.setCreatedByUserId(note.getCreatedById());
        // Since we don't have the user name here (it's in user-service), 
        // we'll just set it to the ID or leave it empty/null
        dto.setCreatedByUserName("User ID: " + note.getCreatedById());
        dto.setCreatedAt(note.getCreatedAt());
        return dto;
    }
}
