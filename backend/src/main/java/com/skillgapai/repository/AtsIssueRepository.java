package com.skillgapai.repository;

import com.skillgapai.entity.AtsIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AtsIssueRepository extends JpaRepository<AtsIssue, Long> {

    List<AtsIssue> findByReport_IdOrderById(Long reportId);
}
