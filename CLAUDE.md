# SkillGap AI — Project Memory

This file is read automatically by Claude Code at the start of every session
in this folder. It tells Claude how to work on this project so you don't
have to re-explain the rules every time.

## Who's building this

Gunjan Senapati, B.Tech CSE (2023–2027), KIST Bhubaneswar. Comfortable with
Java fundamentals, OOP, basic Spring Boot, HTML/CSS/JS/React, and MySQL —
but still developing these skills, not an expert. Treat me as a
beginner-to-developing programmer who wants to understand and defend this
project in a college viva and a job interview, not just have it built for me.

## Working agreement — read this before writing any code

- Build in the phases listed below, ONE AT A TIME. Never generate multiple
  phases at once, even if I say "just finish it" — confirm with me first.
- After each phase, actually run the relevant build/test command and show
  me the real output. Do not assume something works — prove it.
- Explain the reasoning behind each significant decision before moving to
  the next phase.
- Prefer the simple approach over the clever one. If a cleverer alternative
  exists, mention it briefly rather than using it by default.
- Do not introduce tools/frameworks outside the tech stack below without
  asking first and explaining why.
- When my code fails, don't immediately rewrite everything: identify the
  error, explain what it means, find the root cause, apply the smallest
  fix, explain why it works, then re-test.
- Preserve existing working code/structure unless a change is genuinely
  necessary — explain before doing a major redesign.

## Project overview

**One-line idea:** Upload a resume (PDF) and paste a job description; get
back a report showing which required skills are matched, which are missing
(ranked by how easy each is to close), and which skills the user has but
didn't clearly state on the resume.

**Problem it solves:** Students apply to roles without knowing how well
their resume actually matches a specific JD. Generic online "resume
checkers" give vague advice instead of a structured comparison against a
real, specific JD.

**Why it's built this way:** Every technology is deliberately Java, and the
skill-matching logic is transparent, rule-based Java code — not a black-box
AI model. A rule-based system is fully explainable in a viva, and a
fresher's resume value here comes from demonstrable Spring Boot/JPA/MySQL
proficiency.

## Tech stack (non-negotiable — ask before adding anything else)

| Layer | Technology |
|---|---|
| Frontend | React, Tailwind CSS |
| Backend | Java, Spring Boot, Spring Security (JWT), Spring Data JPA |
| Database | MySQL |
| Resume parsing | Apache PDFBox |
| Matching logic | Plain Java + hand-written Jaro-Winkler string similarity |
| PDF export | Apache PDFBox (server-side report rendering) |
| Build tools | Maven (backend, via the Maven Wrapper), npm (frontend), Git/GitHub |

No Python. No external AI API. No embeddings/ML model for the MVP.

## System architecture

```text
User (Browser)
      |
      v
React Frontend (Auth / Upload & Compare / Processing / Gap Report / History)
      |  REST API (JSON over HTTPS), JWT in the Authorization header
      v
Spring Security — JwtAuthenticationFilter
      |  rejects anything without a valid token, except /api/auth/**
      v
Spring Boot Controller Layer
      |
      v
Service Layer
   +-- AuthService            (signup/login, BCrypt hashing, JWT issuing)
   +-- ResumeParsingService    (Apache PDFBox: PDF -> plain text)
   +-- SkillMatchingService    (taxonomy + string similarity)
   +-- ReportService           (builds the ranked gap report, persists it)
   +-- GapReportPdfService     (Apache PDFBox: report -> downloadable PDF)
      |
      v
Repository Layer (Spring Data JPA)
      |
      v
MySQL Database
```

(Also documented, with more detail, in `README.md`.)

## Database schema

```text
User (user_id, name, email, password_hash)
Resume (resume_id, user_id, extracted_text, uploaded_at)
SkillsTaxonomy (skill_id, canonical_name, synonyms)
GapReport (report_id, resume_id, jd_text, matched_skills_json, missing_skills_json,
           underemphasized_skills_json, match_percentage, created_at)
```

## UI/UX principles for every screen

- Clarity over decoration — every screen answers "what do I do here" in 2 seconds.
- Visible reasoning — never show a match result without showing *why*.
- Calm confidence — muted, professional palette, not a gradient-heavy "AI startup" look.
- Responsive feedback — every action gets a visible state change.
- Accessibility is non-negotiable: WCAG AA contrast, full keyboard nav, alt text, labeled fields.

Screens: Auth, Upload & Compare, Processing, Gap Report (the hero screen),
History. (No separate Landing screen was ever built - an unauthenticated
visitor lands straight on Auth.)

## Build order (work through ONE phase at a time)

1. Plain Java matching logic (no Spring, no DB) — **DONE, see status below**
2. MySQL schema + Spring Boot skeleton — **DONE, see status below**
3. Wire matching logic into `POST /api/match`, tested via curl — no persistence yet — **DONE, see status below**
4. Persistence: save each match as a GapReport row, add `GET /api/reports/{id}` — **DONE, see status below**
5. Resume PDF parsing (PDFBox) with manual-paste fallback — **DONE, see status below**
6. React screens, one at a time: 6a Upload & Compare, 6b Processing, 6c Gap Report, 6d History, 6e Auth — **DONE, see status below**
7. Apply the visual design system consistently — no functional changes — **DONE, see status below**
8. Accessibility pass + empty/error states — **DONE, see status below**
9. PDF export of the report — **DONE, see status below**
10. README + demo script, tag `v1.0` — **DONE, see status below**

Commit after each phase with a message describing what that phase delivered.
All 10 phases are complete and tagged `v1.0` - this list is now a build
history, not a queue. Any further work is a new ask beyond the original
scope, not "the next phase."

## Current status (read this first in every session)

**Phases 1-6d are complete.** Condensed history (full detail is in git
log + the comments in each file):

- **Phase 1** — plain Java skill-matching logic, no Spring/DB:
  `model/`, `taxonomy/SkillsTaxonomy.java`, `matching/SkillMatchingService.java`
  (exact → synonym → fuzzy matching, in that order),
  `matching/StringSimilarity.java` (hand-written Jaro-Winkler). Runnable
  via `Phase1Demo.java`.
- **Phase 2** — real Spring Boot app on the Maven Wrapper (`./mvnw`, no
  system-wide Maven needed), connected to a local MySQL database called
  `skillgap_ai`. `entity/` + `repository/` for the four schema tables;
  `spring.jpa.hibernate.ddl-auto=update` so Hibernate creates/updates
  tables from the `@Entity` classes directly. Every `@Lob` `String`
  column is explicitly `LONGTEXT`/`TEXT` (Hibernate's MySQL default for
  `@Lob` is `TINYTEXT` - 255 bytes - which is nowhere near enough for
  resume text, JD text, or JSON skill lists).
- **Phase 3** — `POST /api/match` (`web/MatchController.java`) wires
  `SkillMatchingService` up over HTTP, no persistence yet.
- **Phase 4** — persistence: each match is saved as a `GapReport` row
  (`service/ReportService.java`), plus `GET /api/reports/{id}`.
- **Phase 5** — resume PDF parsing via PDFBox
  (`parsing/ResumeParsingService.java`,
  `POST /api/resumes/extract-text`), with manual-paste as the fallback
  path when extraction fails or the PDF has no text layer.
- **Phase 6a-6d** — the React screens: Upload & Compare, Processing, Gap
  Report (the hero screen), and History (`GET /api/reports` for the
  list, `GET /api/reports/{id}` for detail).

MySQL credentials AND (as of Phase 6e) the JWT signing secret live in
`application-local.properties`, gitignored;
`application-local.properties.example` is the committed template - copy
it and fill in your own values to run this locally.

**Phase 6e is complete.** Real auth (BCrypt + JWT), replacing the
guest-user placeholder every report was previously attached to:

- `security/JwtService.java` issues/verifies HMAC-SHA256 JWTs (subject =
  email, 24h expiry, configured via `jwt.expiration-ms`).
  `security/JwtAuthenticationFilter.java` reads the
  `Authorization: Bearer <token>` header on every request and populates
  the security context from it.
- `config/SecurityConfig.java` requires a valid JWT on every `/api/**`
  route except `/api/auth/**` (signup/login); sessions are stateless;
  CSRF is disabled (not needed for bearer-token auth - a cross-site page
  can't set our Authorization header the way a browser auto-attaches
  cookies). An explicit `AuthenticationEntryPoint` returns 401 for a
  missing/invalid token, since the frontend relies on 401 specifically
  to trigger a logout.
- `service/AuthService.java` + `web/AuthController.java` — signup hashes
  the password with `BCryptPasswordEncoder` (never stored/compared raw);
  login checks the submitted password against the stored hash. Both
  return a token on success; `/api/auth/signup` returns 409 for a
  duplicate email, `/api/auth/login` returns 401 for a wrong
  email/password.
- `service/ReportService.java`, `web/MatchController.java`,
  `web/ReportController.java` — the old fixed `guest@skillgap.local`
  user is gone; every method now takes the logged-in user's email (read
  from `SecurityContextHolder`, populated by the JWT filter).
  `GET /api/reports/{id}` checks the report belongs to that user and
  returns 404 (not 403) otherwise, so it never confirms or denies that a
  given report id belongs to someone else's account.
- Frontend: `components/AuthScreen.jsx` (login/signup, one screen, two
  modes) gates the whole app in `App.jsx` - no auth, no access, no more
  guest fallback. The JWT lives in `localStorage`; `api.js`'s
  `protectedFetch` attaches it to every request and, on a 401, clears it
  and forces the user back to the Auth screen.

Two real issues were caught by actually running the app and testing with
curl, not just trusting the code:

- Unauthenticated requests to a protected route were returning 403, not
  401 - Spring Security's default fallback entry point when no
  `httpBasic()`/`formLogin()` is configured. Fixed with an explicit
  `HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)`.
- `spring.autoconfigure.exclude` for `UserDetailsServiceAutoConfiguration`
  (added to stop Spring Boot auto-configuring an unused in-memory user
  and logging a "generated security password" warning on every startup)
  initially pointed at its Spring Boot 3 package
  (`org.springframework.boot.autoconfigure.security.servlet...`), which
  doesn't exist on this project's Spring Boot 4. The class moved to
  `org.springframework.boot.security.autoconfigure...`.

Verified live in the browser (not just curl): signup, login, a Compare
run tied to the logged-in account, logout, re-login, wrong-password
rejection, and duplicate-email rejection all confirmed working.

**Phases 7-10 are complete - v1.0 is tagged and pushed.**

- **Phase 7** (visual design system) - audited all five screens against
  the UI/UX principles above; found the system already consistent
  screen-to-screen (each was built reusing the same conventions since
  Phase 6a), so this fixed two real drift points rather than a broad
  rewrite: the header's Log out button was smaller than every other
  compact button, and the good/warning/serious status hex colors were
  hand-typed in two places (`matchSeverity.js`, `SkillSection.jsx`) -
  now a single source in `statusColors.js`.
- **Phase 8** (accessibility + empty/error states) - bumped
  `text-slate-400` informational text (fails WCAG AA at ~2.6:1) to
  `text-slate-500` (~4.76:1); added a visible focus ring to every button
  that lacked one; replaced an incomplete ARIA `role="tab"` pattern (no
  arrow-key support) in the Auth and Resume-input mode switches with a
  correct `role="group"` + `aria-pressed` toggle pattern; `api.js` now
  catches raw network failures (backend down) and turns them into a
  plain-language message instead of a browser error string;
  `SkillsChecklist` handles a successfully-loaded-but-empty taxonomy
  instead of rendering nothing; `ResumeInput` validates PDF file type
  client-side before upload.
- **Phase 9** (PDF export) - `export/GapReportPdfService.java` renders a
  `GapReportView` as a PDF with PDFBox (already a dependency, reused
  rather than adding a frontend PDF library) - manual word-wrap and
  pagination, since PDFBox has no flowed-text support of its own.
  `GET /api/reports/{id}/pdf` reuses `ReportService`'s existing
  ownership check. Verified by generating real reports and reading the
  actual PDF bytes back (not just checking a file downloaded): content
  matched the on-screen data exactly, a 100%-match report confirmed the
  empty-state text, and a 30-skill report caught and fixed a real
  pagination bug (a skill's title could be stranded alone at the bottom
  of a page with its reason on the next one). Also confirmed live in
  the browser: clicked Download PDF on a real report, Chrome downloaded
  and opened a valid PDF with correct content.
- **Phase 10** (README + demo script) - `README.md` rewritten from the
  Phase 1 stub into a full doc (overview, tech stack, architecture,
  setup, features). `DEMO_SCRIPT.md` is a rehearsed 3-5 minute viva/
  interview walkthrough built around a specific resume/JD/checklist
  combination actually run through the live backend first, so its
  quoted numbers (56% match, 5 matched, 4 missing ranked easiest-first,
  4 underemphasized) are real, not estimated.

**Phase 13 (ATS Compatibility Analyzer) complete, committed as
43561a2, manually verified including regression check on pre-existing
History reports.**

**Phase 14 (Skill Evidence Analyzer) complete, committed as e6d04a4,
verified live in-browser (light/dark theme, evidence badges, old-report
compatibility, no console errors).**

**Phase 15 (Skill Gap Priority Classification) complete, committed as
b86e794, verified live in-browser (cross-report recurrence scoring,
priority badges scoped only to missing skills, light/dark theme,
old-report compatibility, no console errors).**

**Phase 16 (Job Readiness Score) complete, committed as 8801888.**
Combines four signals already produced by earlier phases into one
composite 0-100 score via `JobReadinessService`, without changing any of
their underlying logic:

- Skill Match Score (40%) - the existing overall match percentage,
  reused as-is.
- ATS Compatibility Score (20%) - Phase 13's score, reused as-is.
- Evidence Strength Score (20%) - the average, over every matched skill,
  of its Phase 14 evidence level mapped to STRONG=100/MODERATE=67/
  WEAK=33/NO_EVIDENCE=0. A report with no matched skills scores 0 here
  rather than being skipped.
- Gap Severity Score (20%) - starts at 100, subtracts per missing skill
  by its Phase 15 priority tier (CRITICAL -30, IMPORTANT -15, OPTIONAL
  -5), floored at 0.

Final score = round(weighted sum); label bands 85-100 Excellent, 70-84
Strong, 50-69 Needs Work, 0-49 Not Ready. The four component scores are
persisted on `GapReport` (not just computed transiently) alongside the
final score/label, so a re-viewed report always shows the exact same
breakdown - same DB-level-default pattern as `ats_score` for reports
created before this phase existed (they read back 0/Not Ready rather
than erroring).

Verified by hand-checking the weighted math against a real match run
through the live backend (0.40×80 + 0.20×25 + 0.20×41.5 + 0.20×95 = 64.3
→ 64, Needs Work) and confirming `GET /api/reports/{id}` returns an
identical breakdown to the freshly-created report. Verified live
in-browser (score panel renders correctly in light/dark theme, all four
component rows visible with correct color-coded bars, no console
errors).

Added a "Future PDF Redesign Reference (build only after all phases
complete)" section with the visual reference image and locked-in
decisions for the eventual Gap Report PDF redesign — not to be built
until Phases 13-27 are all done.

**Phase 18 (Project Recommendations) complete, committed as b1f3c9d.**
Enriches Phase 17's Skill Roadmap by replacing the generic step 2
("Practice with a small project using X") with a concrete, curated
project idea, for a fixed list of 11 skills — not a new page or sidebar
item.

Same architecture and content guardrail as Phase 17's `OFFICIAL_DOCS`:
a second static lookup table (`PROJECT_IDEAS`, skill name -> one
plain-text project idea) inside `LearningRoadmapService`, computed live
alongside the rest of the roadmap — no new DB table, no invented tools/
libraries beyond the skill itself and things everyone already has (a
database, the command line). Any skill not in the list keeps the
original generic step 2 text unchanged, so this only ever adds
specificity, never removes coverage.

Verified via live API calls: a report with Docker (in the curated list)
missing showed its specific project idea in step 2; the same report's
Spring Security (not in the list) kept the exact original generic
phrasing; a curated skill (Java) appearing in the underemphasized list
also got its curated idea, confirming the swap applies through the same
`buildSteps` path regardless of missing vs. underemphasized.

## Feature Roadmap (Phase 13-27)

- **Phase 13 - ATS Compatibility Analyzer**: rule-based checks for
  resume formatting/parseability issues (contact info format, section
  headers, tables/graphics that break parsers), with plain-language
  fixes and a 0-100 score.
- **Phase 14 - Skill Evidence Analyzer**: for each matched skill,
  classify evidence as Strong/Moderate/Weak/No Evidence based on
  whether it appears in Skills only, or also in Projects and/or
  Experience.
- **Phase 15 - Skill Gap Priority Classification**: classify each
  missing skill as Critical/Important/Optional and Learn
  First/Next/Later, based on how often it appears across the user's
  analyzed JDs and how central it is to the target role.
- **Phase 16 - Job Readiness Score**: one composite, explainable score
  combining match %, ATS score, skill evidence, and gap severity, with
  a visible weighted breakdown.
- **Phase 17 - Personalized Learning Roadmap**: turns prioritized skill
  gaps into a week-by-week study plan with topics and a practice task
  per skill.
- **Phase 18 - Project Recommendations**: suggests small project ideas
  that would generate real evidence for the user's top skill gaps.
- **Phase 19 - Interview Question Bank**: categorized practice
  questions (Technical/Skill-Gap/Project/HR) with a stated reason for
  each, drawn from matched skills, gaps, and the user's own projects.
- **Phase 20 - Career Role Recommendation**: suggests which role
  categories the user's skill profile is strongest for, based on
  aggregate match history.
- **Phase 21 - Multi-Job/Multi-JD Comparison**: runs one resume against
  multiple job descriptions at once and ranks them by fit.
- **Phase 22 - Resume Builder**: create/edit/duplicate multiple resume
  versions with structured sections and exportable PDF templates.
- **Phase 23 - Resume Version Comparison**: compares two saved resume
  versions against the same job description side by side.
- **Phase 24 - Career Progress Dashboard**: aggregates Job Readiness
  Score history into a trend view plus a home-screen summary.
- **Phase 25 - Smart PDF Career Report**: exports a combined report
  (score, gaps, roadmap, interview prep) as one polished PDF.
- **Phase 26 - Full Testing Pass**: systematic manual and edge-case
  testing across all Phase 12-25 features.
- **Phase 27 - Polish, Docs, Tag v2.0**: UI polish pass, README/demo
  script update, final commit tagged v2.0.

## Design System v2 (Phase 12+)

This section documents the target design system for the App Shell
Migration and the phases after it. It is a spec to build toward, not
something already implemented — Phase 12 is where this starts landing.

**Application shell** — replace the current tab-bar layout with:
- A collapsible sidebar for primary navigation.
- A top bar containing search, notifications, and a theme toggle.
- A main content area where each existing screen renders.

**Theming** — dark is the default theme; light is available via the
top-bar toggle. Both palettes are fixed token sets, not computed:

*Dark tokens:*
| Token | Hex |
|---|---|
| bg | #0a0a0c |
| sidebar | #0c0c0f |
| surface | #111114 |
| surface-hover | #17171b |
| surface-raised | #1a1a1f |
| border (subtle) | #212127 |
| border (strong) | #2c2c34 |
| text primary | #f2f2f4 |
| text secondary | #9a9aa4 |
| text muted | #65656e |
| accent | #3b82f6 |
| accent hover | #2f6fe0 |

*Light tokens:*
| Token | Hex |
|---|---|
| bg / surface | #ffffff |
| sidebar | #fafafb |
| surface-hover | #f4f4f6 |
| surface-raised | #f6f6f8 |
| border (subtle) | #e5e5ea |
| border (strong) | #d3d3da |
| text primary | #17171a |
| text secondary | #57575f |
| text muted | #84848c |
| accent | #2563eb |
| accent hover | #1d4ed8 |

*Status colors (same in both themes)* — reserved for meaning only,
always paired with a label, never used decoratively:
| Status | Hex |
|---|---|
| good | #0e9f6e |
| warning | #c2850c |
| critical | #dc2626 |

**Accent discipline** — one accent color, used only for primary buttons,
active nav state, links, and focus rings. Nowhere else.

**Typography** — Inter as the only font family (drop Plus Jakarta Sans).
JetBrains Mono used selectively, only for numeric stats/scores/dates.

**Spacing & shape** — fixed spacing scale: 4/8/12/16/24/32/48px. Small
border radius (6-10px). Subtle 1px borders. Minimal shadows. No
gradients, glow, or glassmorphism.

**Layout pattern** — prefer bordered panels and row-lists over stacking
every item in its own floating card.

**Badge vocabulary** — a consistent, fixed set of badge labels across the
app: Strong Match, Good Match, Needs Improvement, Critical Gap,
Completed, In Progress, Pending.

## Definition of "outstanding" — check every screen against this

- Understandable with zero explanation within 5 seconds
- Every result shows *why*, not just *that*
- Visible feedback for every user action
- I can explain, in my own words, why it looks and works this way

## Future PDF Redesign Reference (build only after all phases complete)

Do NOT build this yet. Once Phases 13-27 are all complete, use
docs/pdf-redesign-reference/gap-report-pdf-redesign-reference.png as the visual
reference for the final Gap Report PDF redesign.

Decisions already locked in for that future work:
- WORDING: The reference image uses "AI-powered analysis" and "AI Recommendations."
  This app is 100% rule-based, not AI — remove all "AI" language when implementing.
  Use accurate wording instead, e.g. "Skill Match Analysis" and "Recommendations."
- SCOPE: The reference includes both new visual style (progress ring, stat cards,
  icon-tagged skill cards) AND new content (per-skill recommendation text, a
  priority badge, a 4-step workflow). Build both together at that point, not as a
  partial style-only pass.
- TARGET: PDF export only. Any buttons/arrows in the reference image are static,
  non-clickable visual elements in the PDF — this reference is not for the
  on-screen web Gap Report screen.
