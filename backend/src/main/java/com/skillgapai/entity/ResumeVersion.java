package com.skillgapai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Maps to the "resume_versions" table (Phase 22). Separate from Resume -
 * that entity stays dedicated to the extracted-text/matching flow. One
 * row per resume the user builds/edits in the Resume Builder, with every
 * section other than contact stored as JSON text (same @Lob/LONGTEXT
 * pattern GapReport already uses for its skill-list columns), since this
 * is a brand-new table there's no ALTER-TABLE-on-existing-rows concern -
 * every JSON column is simply NOT NULL, with the service layer always
 * writing "[]" for an empty list rather than null.
 */
@Entity
@Table(name = "resume_versions")
public class ResumeVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resume_version_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "contact_json", nullable = false, columnDefinition = "LONGTEXT")
    private String contactJson;

    @Lob
    @Column(name = "summary", columnDefinition = "LONGTEXT")
    private String summary;

    @Lob
    @Column(name = "skills_json", nullable = false, columnDefinition = "LONGTEXT")
    private String skillsJson;

    @Lob
    @Column(name = "projects_json", nullable = false, columnDefinition = "LONGTEXT")
    private String projectsJson;

    @Lob
    @Column(name = "education_json", nullable = false, columnDefinition = "LONGTEXT")
    private String educationJson;

    @Lob
    @Column(name = "experience_json", nullable = false, columnDefinition = "LONGTEXT")
    private String experienceJson;

    @Lob
    @Column(name = "certifications_json", nullable = false, columnDefinition = "LONGTEXT")
    private String certificationsJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ResumeVersion() {
        // required by JPA
    }

    public ResumeVersion(User user, String title, String contactJson, String summary, String skillsJson,
                          String projectsJson, String educationJson, String experienceJson,
                          String certificationsJson, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.user = user;
        this.title = title;
        this.contactJson = contactJson;
        this.summary = summary;
        this.skillsJson = skillsJson;
        this.projectsJson = projectsJson;
        this.educationJson = educationJson;
        this.experienceJson = experienceJson;
        this.certificationsJson = certificationsJson;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContactJson() {
        return contactJson;
    }

    public void setContactJson(String contactJson) {
        this.contactJson = contactJson;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSkillsJson() {
        return skillsJson;
    }

    public void setSkillsJson(String skillsJson) {
        this.skillsJson = skillsJson;
    }

    public String getProjectsJson() {
        return projectsJson;
    }

    public void setProjectsJson(String projectsJson) {
        this.projectsJson = projectsJson;
    }

    public String getEducationJson() {
        return educationJson;
    }

    public void setEducationJson(String educationJson) {
        this.educationJson = educationJson;
    }

    public String getExperienceJson() {
        return experienceJson;
    }

    public void setExperienceJson(String experienceJson) {
        this.experienceJson = experienceJson;
    }

    public String getCertificationsJson() {
        return certificationsJson;
    }

    public void setCertificationsJson(String certificationsJson) {
        this.certificationsJson = certificationsJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
