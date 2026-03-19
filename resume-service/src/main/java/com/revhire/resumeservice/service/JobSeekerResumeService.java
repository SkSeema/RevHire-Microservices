package com.revhire.resumeservice.service;

import com.revhire.resumeservice.dto.request.ResumeTextRequest;
import com.revhire.resumeservice.dto.response.SeekerResumeDetailsResponse;
import com.revhire.resumeservice.dto.response.SkillSummaryResponse;
import com.revhire.resumeservice.model.ResumeFiles;
import com.revhire.resumeservice.model.ResumeText;
import com.revhire.resumeservice.model.SeekerSkillMap;
import com.revhire.resumeservice.model.SkillsMaster;
import com.revhire.resumeservice.repository.ResumeFilesRepository;
import com.revhire.resumeservice.repository.ResumeTextRepository;
import com.revhire.resumeservice.repository.SeekerSkillMapRepository;
import com.revhire.resumeservice.repository.SkillsMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobSeekerResumeService {

    private final ResumeFilesRepository resumeFilesRepository;
    private final ResumeTextRepository resumeTextRepository;
    private final SeekerSkillMapRepository seekerSkillMapRepository;
    private final SkillsMasterRepository skillsMasterRepository;
    private final Path fileStorageLocation = Paths.get("uploads/resumes").toAbsolutePath().normalize();

    public void init() {
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public ResumeFiles getResumeFile(Long jobSeekerId) {
        List<ResumeFiles> files = resumeFilesRepository.findByJobSeekerId(jobSeekerId);
        if (files != null && !files.isEmpty()) {
            return files.get(0);
        }
        return null;
    }

    public Optional<ResumeFiles> getResumeFileById(Long id) {
        return resumeFilesRepository.findById(id);
    }

    public ResumeFiles storeFile(MultipartFile file, Long jobSeekerId) {
        try {
            if (Files.notExists(fileStorageLocation)) {
                init();
            }
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            ResumeFiles resumeFile = new ResumeFiles();
            resumeFile.setJobSeekerId(jobSeekerId);
            resumeFile.setFileName(originalFileName);
            resumeFile.setFileType(fileExtension.replace(".", "").toUpperCase());
            resumeFile.setFileSize(file.getSize());
            resumeFile.setFilePath(fileName);
            resumeFile.setActive(true);

            return resumeFilesRepository.save(resumeFile);
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }

    public ResumeText getResumeText(Long seekerId) {
        return resumeTextRepository.findByJobSeekerId(seekerId).orElse(null);
    }

    @Transactional
    public ResumeText saveResumeText(Long seekerId, ResumeTextRequest request) {
        ResumeText resumeText = resumeTextRepository.findByJobSeekerId(seekerId).orElseGet(ResumeText::new);
        resumeText.setJobSeekerId(seekerId);
        resumeText.setTitle(request.getTitle());
        resumeText.setObjective(request.getObjective());
        resumeText.setEducationText(request.getEducation());
        resumeText.setExperienceText(request.getExperience());
        resumeText.setSkillsText(request.getSkills());
        resumeText.setProjectsText(request.getProjects());
        resumeText.setCertificationsText(request.getCertifications());
        ResumeText saved = resumeTextRepository.save(resumeText);
        syncSkills(seekerId, request.getSkills());
        return saved;
    }

    public List<SkillSummaryResponse> getSkills(Long seekerId) {
        return seekerSkillMapRepository.findByJobSeekerId(seekerId).stream()
                .map(map -> new SkillSummaryResponse(map.getSkill().getId(), map.getSkill().getSkillName()))
                .toList();
    }

    public SeekerResumeDetailsResponse getResumeDetails(Long seekerId) {
        ResumeText resumeText = getResumeText(seekerId);
        SeekerResumeDetailsResponse details = new SeekerResumeDetailsResponse();
        if (resumeText != null) {
            details.setTitle(resumeText.getTitle());
            details.setObjective(resumeText.getObjective());
            details.setEducation(resumeText.getEducationText());
            details.setExperience(resumeText.getExperienceText());
            details.setSkills(resumeText.getSkillsText());
            details.setProjects(resumeText.getProjectsText());
            details.setCertifications(resumeText.getCertificationsText());
        }

        List<SkillSummaryResponse> skills = getSkills(seekerId);
        details.setSkillsList(skills);

        boolean hasResumeFile = getResumeFile(seekerId) != null;
        boolean hasSummary = hasText(details.getObjective());
        boolean hasSkills = hasText(details.getSkills()) || !skills.isEmpty();
        boolean hasExperience = hasText(details.getExperience());
        boolean hasEducation = hasText(details.getEducation());

        int completedTasks = 0;
        if (hasResumeFile) completedTasks++;
        if (hasSummary) completedTasks++;
        if (hasSkills) completedTasks++;
        if (hasExperience) completedTasks++;
        if (hasEducation) completedTasks++;

        details.setResumeUploaded(hasResumeFile);
        details.setProfileSummarySet(hasSummary);
        details.setSkillsSet(hasSkills);
        details.setExperienceSet(hasExperience);
        details.setEducationSet(hasEducation);
        details.setCompletionPercentage((completedTasks * 100) / 5);
        return details;
    }

    public Resource loadResumeResource(Long seekerId) throws Exception {
        ResumeFiles resumeFile = getResumeFile(seekerId);
        if (resumeFile == null) {
            throw new RuntimeException("Resume not found");
        }
        Path filePath = fileStorageLocation.resolve(resumeFile.getFilePath()).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("Resume file not found on server");
        }
        return resource;
    }

    private void syncSkills(Long seekerId, String rawSkills) {
        List<SeekerSkillMap> existing = new ArrayList<>(seekerSkillMapRepository.findByJobSeekerId(seekerId));
        if (!existing.isEmpty()) {
            seekerSkillMapRepository.deleteAll(existing);
        }
        if (!hasText(rawSkills)) {
            return;
        }

        for (String skillName : rawSkills.split(",")) {
            String normalized = skillName.trim();
            if (normalized.isEmpty()) {
                continue;
            }
            SkillsMaster skill = skillsMasterRepository.findBySkillNameIgnoreCase(normalized)
                    .orElseGet(() -> skillsMasterRepository.save(new SkillsMaster(null, normalized)));
            SeekerSkillMap map = new SeekerSkillMap();
            map.setJobSeekerId(seekerId);
            map.setSkill(skill);
            map.setLevel(SeekerSkillMap.SkillLevel.INTERMEDIATE);
            seekerSkillMapRepository.save(map);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
