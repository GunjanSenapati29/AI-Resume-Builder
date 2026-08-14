package com.skillgapai.web;

import com.skillgapai.dto.SkillSummaryResponse;
import com.skillgapai.taxonomy.SkillsTaxonomy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Phase 6a: exposes the taxonomy's skill names so the Upload & Compare
 * screen can render a real checklist instead of hardcoding a duplicate
 * copy of the backend's taxonomy in the frontend.
 */
@RestController
@RequestMapping("/api/skills")
public class SkillsController {

    private final SkillsTaxonomy taxonomy;

    public SkillsController(SkillsTaxonomy taxonomy) {
        this.taxonomy = taxonomy;
    }

    @GetMapping
    public List<SkillSummaryResponse> listSkills() {
        return taxonomy.getSkills().stream()
                .map(skill -> new SkillSummaryResponse(skill.getCanonicalName(), skill.getDifficulty()))
                .toList();
    }
}
