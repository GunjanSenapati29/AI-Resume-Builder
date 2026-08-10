# SkillGap AI

Upload a resume (PDF) and paste a job description; get back a report showing
which required skills are matched, which are missing (ranked by how easy each
is to close), and which skills you have but didn't clearly state on your
resume.

Full spec: see the project's master build prompt (kept by the project owner).
This README will grow into full setup/usage instructions as later phases add
the database, backend API, and frontend.

## Status

**Phase 1 complete** - plain Java skill-matching logic (`backend/`), proven
correct with hardcoded sample resume/JD text. No Spring Boot, no database,
no frontend yet - those come in later phases.

## Project layout

```text
skillgap-ai/
├── backend/
│   └── src/main/java/com/skillgapai/
│       ├── model/        - Skill, RequiredSkill, MatchResult, etc. (plain data classes)
│       ├── taxonomy/      - SkillsTaxonomy: canonical skill names + synonyms + difficulty
│       ├── matching/       - SkillMatchingService: the core matching logic
│       └── Phase1Demo.java - runnable demo proving the logic works
└── README.md
```

## Running Phase 1's demo

This phase uses plain Java only (no Maven, no external libraries - see the
note in `StringSimilarity.java` for why). From `backend/`:

```bash
mkdir -p out
javac -d out $(find src/main/java -name "*.java")
java -cp out com.skillgapai.Phase1Demo
```
