package com.skillgapai.repository;

import com.skillgapai.entity.ResumeVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, Long> {

    List<ResumeVersion> findByUser_EmailOrderByUpdatedAtDesc(String email);
}
