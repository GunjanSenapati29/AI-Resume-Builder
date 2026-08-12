package com.skillgapai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * Maps to the "skills_taxonomy" table (Section: Database schema in
 * CLAUDE.md, documented there as "SkillsTaxonomy").
 *
 * Named "SkillTaxonomyEntry" rather than "SkillsTaxonomy" to avoid
 * clashing with the existing plain-Java com.skillgapai.taxonomy.SkillsTaxonomy
 * class from Phase 1, which holds the whole in-memory list of skills, not
 * one database row. A future phase will likely need both classes imported
 * in the same file, so they can't share a simple name.
 *
 * synonyms is stored as one comma-separated string for now (e.g.
 * "js,java script") rather than a separate table or JSON, to keep this
 * phase's schema simple - no business logic reads this table yet.
 */
@Entity
@Table(name = "skills_taxonomy")
public class SkillTaxonomyEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_id")
    private Long id;

    @Column(name = "canonical_name", nullable = false, unique = true)
    private String canonicalName;

    @Lob
    @Column(name = "synonyms", columnDefinition = "TEXT")
    private String synonyms;

    protected SkillTaxonomyEntry() {
        // required by JPA
    }

    public SkillTaxonomyEntry(String canonicalName, String synonyms) {
        this.canonicalName = canonicalName;
        this.synonyms = synonyms;
    }

    public Long getId() {
        return id;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public String getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(String synonyms) {
        this.synonyms = synonyms;
    }
}
