package com.skillgapai.repository;

import com.skillgapai.entity.SkillGapPriority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillGapPriorityRepository extends JpaRepository<SkillGapPriority, Long> {

    List<SkillGapPriority> findByReport_IdOrderById(Long reportId);
}
