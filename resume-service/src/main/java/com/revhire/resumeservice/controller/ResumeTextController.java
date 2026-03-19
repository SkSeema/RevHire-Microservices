package com.revhire.resumeservice.controller;

import com.revhire.resumeservice.dto.request.ResumeTextRequest;
import com.revhire.resumeservice.model.ResumeText;
import com.revhire.resumeservice.repository.ResumeTextRepository;
import com.revhire.resumeservice.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seeker/profile")
@RequiredArgsConstructor
@Slf4j
public class ResumeTextController {

    private final ResumeTextRepository resumeTextRepository;

    @GetMapping("/resume-text")
    public ResponseEntity<?> getResumeText(@AuthenticationPrincipal UserPrincipal user) {
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return resumeTextRepository.findByJobSeekerId(user.getId())
                .map(rt -> {
                    ResumeTextRequest resp = new ResumeTextRequest();
                    resp.setTitle(rt.getTitle());
                    resp.setObjective(rt.getObjective());
                    resp.setEducation(rt.getEducationText());
                    resp.setExperience(rt.getExperienceText());
                    resp.setSkills(rt.getSkillsText());
                    resp.setProjects(rt.getProjectsText());
                    resp.setCertifications(rt.getCertificationsText());
                    return ResponseEntity.ok(resp);
                })
                .orElse(ResponseEntity.ok(new ResumeTextRequest()));
    }

    @GetMapping("/{seekerId}/skills")
    public ResponseEntity<?> getSeekerSkills(@PathVariable("seekerId") Long seekerId) {
        return ResponseEntity.ok(resumeTextRepository.findByJobSeekerId(seekerId)
                .map(rt -> {
                    String skills = rt.getSkillsText();
                    if (skills == null || skills.isEmpty()) return new String[0];
                    return skills.split(",");
                })
                .orElse(new String[0]));
    }

    @PostMapping("/resume-text")
    public ResponseEntity<?> updateResumeText(@RequestBody ResumeTextRequest textDto,
                                             @AuthenticationPrincipal UserPrincipal user) {
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        ResumeText rt = resumeTextRepository.findByJobSeekerId(user.getId())
                .orElse(new ResumeText());
        
        rt.setJobSeekerId(user.getId());
        rt.setTitle(textDto.getTitle());
        rt.setObjective(textDto.getObjective());
        rt.setEducationText(textDto.getEducation());
        rt.setExperienceText(textDto.getExperience());
        rt.setSkillsText(textDto.getSkills());
        rt.setProjectsText(textDto.getProjects());
        rt.setCertificationsText(textDto.getCertifications());
        
        return ResponseEntity.ok(resumeTextRepository.save(rt));
    }
}
