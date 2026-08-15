package com.skillgapai.entity;

import com.skillgapai.model.AtsSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Phase 13: maps to the "ats_issues" table. One row per ATS
 * compatibility check run against a GapReport's resume - PASSED checks
 * get a row too, not just failures, so the report can show every check
 * that ran (see AtsAnalyzerService). fixSuggestion is null for a PASSED
 * row - there's nothing to fix.
 */
@Entity
@Table(name = "ats_issues")
public class AtsIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ats_issue_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private GapReport report;

    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "description", nullable = false, columnDefinition = "LONGTEXT")
    private String description;

    @Lob
    @Column(name = "fix_suggestion", columnDefinition = "LONGTEXT")
    private String fixSuggestion;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 10)
    private AtsSeverity severity;

    protected AtsIssue() {
        // required by JPA
    }

    public AtsIssue(GapReport report, String title, String description, String fixSuggestion, AtsSeverity severity) {
        this.report = report;
        this.title = title;
        this.description = description;
        this.fixSuggestion = fixSuggestion;
        this.severity = severity;
    }

    public Long getId() {
        return id;
    }

    public GapReport getReport() {
        return report;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getFixSuggestion() {
        return fixSuggestion;
    }

    public AtsSeverity getSeverity() {
        return severity;
    }
}
