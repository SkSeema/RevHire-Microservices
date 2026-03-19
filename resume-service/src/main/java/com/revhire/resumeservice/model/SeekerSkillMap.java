package com.revhire.resumeservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seeker_skill_map")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeekerSkillMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seeker_id", nullable = false)
    private Long jobSeekerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private SkillsMaster skill;

    @Enumerated(EnumType.STRING)
    private SkillLevel level;

    public enum SkillLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}
