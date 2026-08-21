package com.skillgapai.resumebuilder;

import com.skillgapai.dto.CertificationEntry;
import com.skillgapai.dto.ContactInfo;
import com.skillgapai.dto.EducationEntry;
import com.skillgapai.dto.ExperienceEntry;
import com.skillgapai.dto.ProjectEntry;
import com.skillgapai.dto.ResumeVersionRequest;
import com.skillgapai.dto.ResumeVersionSummary;
import com.skillgapai.dto.ResumeVersionView;
import com.skillgapai.entity.ResumeVersion;
import com.skillgapai.entity.User;
import com.skillgapai.repository.ResumeVersionRepository;
import com.skillgapai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Phase 22: CRUD + duplicate for ResumeVersion, following the same
 * per-user ownership pattern ReportService already uses for GapReport -
 * every lookup is scoped to the requesting user's email, and a version
 * that exists but belongs to someone else is reported as "not found"
 * (empty Optional / false), never "forbidden", so a caller can never
 * tell the two cases apart.
 *
 * Validation (title required, contact.name required) happens in
 * ResumeVersionController before this service is ever called, same
 * division of responsibility AuthController/AuthService already use.
 */
@Service
public class ResumeVersionService {

    private static final String COPY_SUFFIX = " (Copy)";

    private final UserRepository userRepository;
    private final ResumeVersionRepository resumeVersionRepository;
    private final ObjectMapper objectMapper;

    public ResumeVersionService(UserRepository userRepository, ResumeVersionRepository resumeVersionRepository,
                                 ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.resumeVersionRepository = resumeVersionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ResumeVersionView create(String userEmail, ResumeVersionRequest request) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        LocalDateTime now = LocalDateTime.now();

        ResumeVersion version = new ResumeVersion(
                user,
                request.title(),
                toJson(request.contact()),
                request.summary(),
                toJson(nullToEmpty(request.skills())),
                toJson(nullToEmpty(request.projects())),
                toJson(nullToEmpty(request.education())),
                toJson(nullToEmpty(request.experience())),
                toJson(nullToEmpty(request.certifications())),
                now,
                now);

        return toView(resumeVersionRepository.save(version));
    }

    @Transactional(readOnly = true)
    public List<ResumeVersionSummary> list(String userEmail) {
        return resumeVersionRepository.findByUser_EmailOrderByUpdatedAtDesc(userEmail).stream()
                .map(v -> new ResumeVersionSummary(v.getId(), v.getTitle(), v.getUpdatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ResumeVersionView> get(Long id, String userEmail) {
        return findOwned(id, userEmail).map(this::toView);
    }

    @Transactional
    public Optional<ResumeVersionView> update(Long id, String userEmail, ResumeVersionRequest request) {
        return findOwned(id, userEmail).map(version -> {
            version.setTitle(request.title());
            version.setContactJson(toJson(request.contact()));
            version.setSummary(request.summary());
            version.setSkillsJson(toJson(nullToEmpty(request.skills())));
            version.setProjectsJson(toJson(nullToEmpty(request.projects())));
            version.setEducationJson(toJson(nullToEmpty(request.education())));
            version.setExperienceJson(toJson(nullToEmpty(request.experience())));
            version.setCertificationsJson(toJson(nullToEmpty(request.certifications())));
            version.setUpdatedAt(LocalDateTime.now());
            return toView(version);
        });
    }

    @Transactional
    public boolean delete(Long id, String userEmail) {
        Optional<ResumeVersion> version = findOwned(id, userEmail);
        version.ifPresent(resumeVersionRepository::delete);
        return version.isPresent();
    }

    @Transactional
    public Optional<ResumeVersionView> duplicate(Long id, String userEmail) {
        return findOwned(id, userEmail).map(original -> {
            LocalDateTime now = LocalDateTime.now();
            ResumeVersion copy = new ResumeVersion(
                    original.getUser(),
                    original.getTitle() + COPY_SUFFIX,
                    original.getContactJson(),
                    original.getSummary(),
                    original.getSkillsJson(),
                    original.getProjectsJson(),
                    original.getEducationJson(),
                    original.getExperienceJson(),
                    original.getCertificationsJson(),
                    now,
                    now);
            return toView(resumeVersionRepository.save(copy));
        });
    }

    private Optional<ResumeVersion> findOwned(Long id, String userEmail) {
        return resumeVersionRepository.findById(id)
                .filter(v -> v.getUser().getEmail().equals(userEmail));
    }

    private ResumeVersionView toView(ResumeVersion v) {
        return new ResumeVersionView(
                v.getId(),
                v.getTitle(),
                v.getCreatedAt(),
                v.getUpdatedAt(),
                fromJson(v.getContactJson(), ContactInfo.class),
                v.getSummary(),
                readList(v.getSkillsJson(), new TypeReference<List<String>>() { }),
                readList(v.getProjectsJson(), new TypeReference<List<ProjectEntry>>() { }),
                readList(v.getEducationJson(), new TypeReference<List<EducationEntry>>() { }),
                readList(v.getExperienceJson(), new TypeReference<List<ExperienceEntry>>() { }),
                readList(v.getCertificationsJson(), new TypeReference<List<CertificationEntry>>() { }));
    }

    private <T> List<T> nullToEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private String toJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    private <T> T fromJson(String json, Class<T> type) {
        return objectMapper.readValue(json, type);
    }

    private <T> List<T> readList(String json, TypeReference<List<T>> type) {
        return objectMapper.readValue(json, type);
    }
}
