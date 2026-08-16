package com.skillgapai.evidence;

import com.skillgapai.ats.AtsAnalyzerService;
import com.skillgapai.model.EvidenceLevel;
import com.skillgapai.model.MatchResult;
import com.skillgapai.model.ResumeSectionBlocks;
import com.skillgapai.model.Skill;
import com.skillgapai.model.SkillEvidenceResult;
import com.skillgapai.taxonomy.SkillsTaxonomy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Phase 14: for each skill SkillMatchingService already found somewhere
 * in the resume, checks which of the resume's Skills/Projects/Experience
 * section blocks (reused from AtsAnalyzerService.extractSectionBlocks(),
 * not rebuilt) actually mention that skill, and turns "how many of the
 * three sections" into a Strong/Moderate/Weak/No Evidence label. A skill
 * only listed under Skills reads very differently from one also backed
 * up by a Projects or Experience entry - this makes that difference
 * visible instead of collapsing every matched skill into one flat list.
 *
 * Runs entirely off the same resumeText and the same matched-skill list
 * SkillMatchingService already produced - it doesn't re-run or alter
 * that matching logic at all.
 */
@Service
public class SkillEvidenceService {

    private final SkillsTaxonomy taxonomy;
    private final AtsAnalyzerService atsAnalyzerService;

    public SkillEvidenceService(SkillsTaxonomy taxonomy, AtsAnalyzerService atsAnalyzerService) {
        this.taxonomy = taxonomy;
        this.atsAnalyzerService = atsAnalyzerService;
    }

    public List<SkillEvidenceResult> analyze(String resumeText, List<MatchResult> matchedSkills) {
        ResumeSectionBlocks blocks = atsAnalyzerService.extractSectionBlocks(resumeText);
        String skillsText = blocks.skillsText().toLowerCase();
        String projectsText = blocks.projectsText().toLowerCase();
        String experienceText = blocks.experienceText().toLowerCase();

        List<SkillEvidenceResult> results = new ArrayList<>();
        for (MatchResult matched : matchedSkills) {
            List<String> namesToCheck = namesFor(matched.getSkillName());

            boolean inSkills = containsAny(skillsText, namesToCheck);
            boolean inProjects = containsAny(projectsText, namesToCheck);
            boolean inExperience = containsAny(experienceText, namesToCheck);

            int sectionsFound = (inSkills ? 1 : 0) + (inProjects ? 1 : 0) + (inExperience ? 1 : 0);
            EvidenceLevel level = switch (sectionsFound) {
                case 3 -> EvidenceLevel.STRONG;
                case 2 -> EvidenceLevel.MODERATE;
                case 1 -> EvidenceLevel.WEAK;
                default -> EvidenceLevel.NO_EVIDENCE;
            };

            results.add(new SkillEvidenceResult(matched.getSkillName(), inSkills, inProjects, inExperience, level));
        }
        return results;
    }

    // Checks every known name (canonical + synonyms) for the skill, same
    // as SkillMatchingService does for the whole-resume match - a skill
    // mentioned under its synonym in one section should still count as
    // evidence for that section.
    private List<String> namesFor(String skillName) {
        Skill knownSkill = taxonomy.findSkillByName(skillName);
        return knownSkill != null ? knownSkill.allNames() : List.of(skillName);
    }

    private boolean containsAny(String lowerCaseSectionText, List<String> names) {
        if (lowerCaseSectionText.isBlank()) {
            return false;
        }
        for (String name : names) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(name.toLowerCase()) + "\\b");
            if (pattern.matcher(lowerCaseSectionText).find()) {
                return true;
            }
        }
        return false;
    }
}
