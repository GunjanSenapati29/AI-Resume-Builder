package com.skillgapai.dto;

import java.util.List;

/** Phase 22: one entry in a ResumeVersion's Experience section. */
public record ExperienceEntry(String company, String role, String dates, List<String> bullets) {
}
