package com.skillgapai.interview;

import com.skillgapai.dto.InterviewQuestionsView;
import com.skillgapai.dto.SkillInterviewQuestionsView;
import com.skillgapai.entity.GapReport;
import com.skillgapai.repository.GapReportRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Phase 19: turns the MATCHED skills from the user's MOST RECENT
 * GapReport into interview questions - matched skills are what the
 * resume actually claims, which is what an interviewer would probe, so
 * this deliberately reads the matched list, not missing/underemphasized
 * (that's Phase 17's Skill Roadmap instead).
 *
 * Same architecture as Phase 17/18: a static lookup table (skill name ->
 * exactly 3 questions) computed live on every call, nothing persisted -
 * this is fixed content, not a scored/historical value. Any matched
 * skill not in CURATED_QUESTIONS gets a generic 3-question fallback with
 * the skill name filled in, never an invented specific.
 */
@Service
public class InterviewQuestionService {

    private static final Map<String, List<String>> CURATED_QUESTIONS = Map.ofEntries(
            Map.entry("java", List.of(
                    "What is the difference between JDK, JRE, and JVM?",
                    "Explain the difference between an abstract class and an interface in Java.",
                    "What is the purpose of the 'static' keyword?")),
            Map.entry("spring boot", List.of(
                    "What is dependency injection, and how does Spring Boot implement it?",
                    "What is the difference between @Component, @Service, and @Repository?",
                    "How does Spring Boot auto-configuration work?")),
            Map.entry("javascript", List.of(
                    "What is the difference between '==' and '===' in JavaScript?",
                    "Explain closures with an example.",
                    "What is the event loop in JavaScript?")),
            Map.entry("python", List.of(
                    "What is the difference between a list and a tuple?",
                    "Explain Python's GIL (Global Interpreter Lock).",
                    "What are list comprehensions, and why are they useful?")),
            Map.entry("react", List.of(
                    "What is the difference between state and props?",
                    "Explain the virtual DOM and why it's used.",
                    "What are React hooks, and why were they introduced?")),
            Map.entry("mysql", List.of(
                    "What is the difference between INNER JOIN and LEFT JOIN?",
                    "What is normalization, and why is it important?",
                    "What is an index, and how does it improve query performance?")),
            Map.entry("git", List.of(
                    "What is the difference between 'git merge' and 'git rebase'?",
                    "How do you resolve a merge conflict?",
                    "What is the purpose of a .gitignore file?")),
            Map.entry("docker", List.of(
                    "What is the difference between a Docker image and a container?",
                    "What is the purpose of a Dockerfile?",
                    "How does Docker differ from a virtual machine?")),
            Map.entry("aws", List.of(
                    "What is the difference between EC2 and S3?",
                    "What is IAM, and why is it important?",
                    "What is the difference between scaling up and scaling out?")),
            Map.entry("rest apis", List.of(
                    "What makes an API RESTful?",
                    "What is the difference between PUT and PATCH?",
                    "What are HTTP status codes, and can you name a few common ones?")),
            Map.entry("junit", List.of(
                    "What is the difference between @Before/@BeforeEach and @BeforeClass/@BeforeAll?",
                    "What is mocking, and why is it used in unit tests?",
                    "What makes a good unit test?")));

    private final GapReportRepository gapReportRepository;
    private final ObjectMapper objectMapper;

    public InterviewQuestionService(GapReportRepository gapReportRepository, ObjectMapper objectMapper) {
        this.gapReportRepository = gapReportRepository;
        this.objectMapper = objectMapper;
    }

    public Optional<InterviewQuestionsView> buildForLatestReport(String userEmail) {
        Optional<GapReport> latestReport = gapReportRepository.findFirstByResume_User_EmailOrderByCreatedAtDesc(userEmail);
        if (latestReport.isEmpty()) {
            return Optional.empty();
        }
        GapReport report = latestReport.get();

        List<SkillInterviewQuestionsView> skills = skillNames(report.getMatchedSkillsJson()).stream()
                .map(skillName -> new SkillInterviewQuestionsView(skillName, questionsFor(skillName)))
                .toList();

        return Optional.of(new InterviewQuestionsView(report.getId(), skills));
    }

    private List<String> questionsFor(String skillName) {
        List<String> curated = CURATED_QUESTIONS.get(skillName.toLowerCase());
        if (curated != null) {
            return curated;
        }
        return List.of(
                "Can you describe a project where you used " + skillName + "?",
                "What challenges have you faced while working with " + skillName + ", and how did you overcome them?",
                "How would you explain " + skillName + " to someone who has never used it?");
    }

    private List<String> skillNames(String json) {
        JsonNode node = objectMapper.readTree(json);
        List<String> names = new ArrayList<>();
        for (JsonNode item : node) {
            names.add(item.path("skillName").asText(""));
        }
        return names;
    }
}
