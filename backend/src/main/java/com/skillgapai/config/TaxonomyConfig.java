package com.skillgapai.config;

import com.skillgapai.taxonomy.SkillsTaxonomy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the Phase 1 hardcoded taxonomy as a Spring bean so
 * SkillMatchingService can be constructor-injected with it. A later
 * phase can swap this for a bean that loads SkillTaxonomyEntry rows
 * from MySQL instead, without SkillMatchingService itself changing.
 */
@Configuration
public class TaxonomyConfig {

    @Bean
    public SkillsTaxonomy skillsTaxonomy() {
        return SkillsTaxonomy.sampleTaxonomy();
    }
}
