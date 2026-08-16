package com.skillgapai.repository;

import com.skillgapai.entity.SkillEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillEvidenceRepository extends JpaRepository<SkillEvidence, Long> {

    List<SkillEvidence> findByReport_IdOrderById(Long reportId);
}
