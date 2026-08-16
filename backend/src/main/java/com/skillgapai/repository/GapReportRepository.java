package com.skillgapai.repository;

import com.skillgapai.entity.GapReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GapReportRepository extends JpaRepository<GapReport, Long> {

    List<GapReport> findByResume_User_EmailOrderByCreatedAtDesc(String email);

    // Phase 17: the Learning Roadmap only ever covers the user's single
    // most recent report - this is a LIMIT-1-at-the-DB-level version of
    // the query above rather than fetching the full list and taking the
    // first element.
    Optional<GapReport> findFirstByResume_User_EmailOrderByCreatedAtDesc(String email);
}
