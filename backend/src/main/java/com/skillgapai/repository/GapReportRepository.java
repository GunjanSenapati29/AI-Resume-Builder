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

    // Phase 24: Progress trend plots left-to-right, so it needs the
    // opposite (oldest-first) order from every other report listing.
    List<GapReport> findByResume_User_EmailOrderByCreatedAtAsc(String email);

    long countByResume_User_Email(String email);
}
