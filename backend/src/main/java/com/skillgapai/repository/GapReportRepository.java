package com.skillgapai.repository;

import com.skillgapai.entity.GapReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GapReportRepository extends JpaRepository<GapReport, Long> {

    List<GapReport> findByResume_User_EmailOrderByCreatedAtDesc(String email);
}
