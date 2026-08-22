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

**Phase 19 (Interview Question Bank) complete, committed as c281608.**
Turns the MATCHED skills (not missing/underemphasized - that's Phase
17's job) on the user's most recent GapReport into interview practice
questions - `interview/InterviewQuestionService.java` has a static
`CURATED_QUESTIONS` table (3 questions per skill, same architecture as
Phase 17/18's lookup tables) for a fixed list of skills, and any matched
skill outside that list gets a generic 3-question fallback with the
skill name filled in. `GET /api/reports/latest/interview-questions`,
same 204-means-no-reports-yet convention as Learning Roadmap. Frontend:
`InterviewPrepScreen.jsx`, wired into the sidebar as a real nav item the
same way Skill Roadmap was. Verified live in-browser (matched-skill
cards render with their 3 questions each, light/dark theme, old-report
compatibility, no console errors).

**Phase 20 (Career Role Recommendation) complete.** Scores the user's
aggregate skill profile against a fixed
catalog of 5 role categories (Backend Developer (Java), Full Stack
Developer, Frontend Developer, DevOps-leaning Engineer, QA / SDET) -
each defined as a short required-skill list in a static lookup table,
same architecture as Phase 17-19's fixed content tables, no DB table, no
ML.

The one deliberate departure from Phase 17-19: those all read only the
user's single MOST RECENT GapReport. Career Fit reads EVERY report the
user has ever run (`findByResume_User_EmailOrderByCreatedAtDesc`) and
unions every skill that has ever appeared as MATCHED, on any report,
into one set - a role fit is a question about the user's overall
demonstrated skill set, not one specific JD comparison. Score per role =
(required skills ever matched) / (total required) x 100, rounded,
roles sorted by score descending. Computed live on every call, nothing
persisted, same reasoning as Phase 17-19.

Backend: `dto/RoleFitView.java`, `dto/RoleRecommendationsView.java`,
`career/CareerRoleService.java`, `GET /api/reports/role-recommendations`
on `ReportController` (same 204-means-no-reports-yet convention, but
outside the `/latest/...` route group since it isn't a single-report
view). Frontend: `components/CareerFitScreen.jsx` (loading / empty-state
with a "Go to Analyze Resume" CTA / real ranked role list, same
three-state pattern as `SkillRoadmapScreen`/`InterviewPrepScreen`), a
`fetchRoleRecommendations()` in `api.js` with the same 204-returns-null
convention, a `Career Fit` entry in `Sidebar.jsx` styled like the other
real (non-"Soon") nav items, and a `career` page in `App.jsx`. Each role
card shows its fit percentage as a color-coded bar (same 80/50
good/warning/critical thresholds `JobReadinessSection` uses) plus which
required skills are matched vs. missing, so the score is never shown
without the reasoning behind it.

Verified for real, not assumed: `mvn compile`/`mvn test` clean, `npm run
build` clean. Ran the live backend and proved the "reads ALL reports"
behavior specifically - two separate `/api/match` calls with disjoint
skill sets (Java/Spring Boot/MySQL/REST APIs/JUnit in one,
JavaScript/React/Git/Docker in the other) made "Full Stack Developer"
(which needs skills from both) score 100%, which is only possible if
every report is being read, not just the latest. A second test user
with only Java+Git matched produced the hand-checked fractions exactly
(67/33/33/25/17, correctly sorted descending). Live in-browser: signed
up a fresh account, confirmed the empty state and its CTA render with
zero reports, ran a real Analyze Resume submission, then confirmed
Career Fit renders the real ranked roles with correct matched/missing
chips and score-bar colors, in both dark and light theme, with no
browser console errors.

**Phase 21 (Multi-Job/Multi-JD Comparison) complete.** Lets the user
pick 2-5 of their own past GapReports from History and see them ranked
side by side by match %, with matched/missing skills per report and
which matched skills are common across the whole set vs. unique to one
job - "which job am I better suited for and why," not just a bare
number. Option A from the design discussion: reuses data every
GapReport already stores, no new submission flow and no new scoring
logic anywhere in this phase.

Backend: `dto/ReportComparisonItemView.java`,
`GET /api/reports/compare?ids=1,2,3` on `ReportController`. Validates
2-5 ids (400 otherwise, including a malformed ids param) and reuses
`ReportService`'s existing per-report ownership check for every id - if
ANY id doesn't resolve to one of the logged-in user's own reports, the
whole request 404s rather than revealing which id failed, same
never-confirm-or-deny reasoning `getReport` already uses. Response is
sorted by match percentage descending regardless of the order ids were
requested in. `ReportService.compareReports` is the only new backend
logic; everything it returns (label, matchedSkills, missingSkills,
matchPercentage) is a reshaping of columns already on `GapReport`.

Frontend: `HistoryScreen.jsx` grows an optional checkbox column (only
when an `onCompare` prop is passed) capped at 5 selections, with a
live "X of 5 selected" count and a "Compare Selected" button once 2+
are checked. `components/CompareJobsScreen.jsx` renders the ranked
cards - the top-scoring report gets a "Best Fit" badge and accent
border, each report shows a match-% bar (reusing `matchSeverity`'s
existing 70/40 thresholds, same ones `MatchMeter`/`HistoryScreen`
already use) plus matched skills (tagged "All" if every compared report
matched that skill, "Unique" if only this one did) and missing skills.
That All/Unique tagging is the one piece of client-side computation in
this phase (`countMatchesAcrossReports` in `CompareJobsScreen.jsx`) -
everything else is a direct render of what the backend returned.
`fetchReportComparison()` added to `api.js`. "Compare Jobs" activated
in `Sidebar.jsx` (moved out of the disabled `PLANNED_NAV` list) and
wired into `App.jsx` as a `compareJobs` page the same way every other
real nav item is; reached directly from the sidebar with nothing
selected yet, it shows an instructional empty state with a "Go to
History" CTA instead of attempting a request the backend would 400 on.

Verified for real: `mvn compile`/`mvn test` and `npm run build` all
clean. Live backend testing covered every edge the design called for -
2 reports (200), 5 reports (200), 1 report (400), 6 reports (400), a
malformed ids param (400), and a request mixing one of the logged-in
user's own report ids with another user's report id (404) - plus a
targeted check that two reports submitted as `low,high` still came back
sorted `high,low`, proving the sort isn't just an artifact of insertion
order. Live in-browser: the sidebar's direct-entry empty state, the
History checkbox flow (selecting up to 5, confirming the 6th checkbox
is actually disabled, not just visually capped), and the real
comparison view (3 reports at 100%/67%/25%, "Best Fit" on the top card,
Java correctly tagged "All" since every report matched it, MySQL
correctly tagged "Unique" since only the 100% report did) - all
confirmed in both light and dark theme, no browser console errors.

**Phase 22 (Resume Builder) complete.** A new `ResumeVersion` entity -
separate from the existing `Resume` entity, which stays dedicated to the
extracted-text/matching flow - lets a user build/edit multiple resume
versions from scratch: id, title, timestamps, plus `contact_json`
(required - at least a name) and six optional JSON-text sections
(summary is plain text; skills/projects/education/experience/
certifications are JSON arrays), same `@Lob`/LONGTEXT pattern GapReport
already uses. Since this is a brand-new table (not an ALTER on rows that
already exist), every JSON column is a plain NOT NULL - no DB-level-
default trick needed - with the service layer always writing `"[]"` for
an empty list rather than null.

Backend: new `resumebuilder/` package -
`ResumeVersionService` (CRUD + duplicate, same per-user ownership-check
pattern as `ReportService`: a version that exists but belongs to another
user is reported as 404, never 403, so a request can never confirm or
deny that an id belongs to someone else) and `ResumeVersionPdfService`
(one clean, single-column, ATS-friendly PDF template via PDFBox - same
manual word-wrap/pagination `Writer` approach as `GapReportPdfService`,
kept as its own copy since the two documents' layouts don't overlap).
`ResumeVersionController` exposes
`POST/GET /api/resume-versions`, `GET/PUT/DELETE /api/resume-versions/{id}`,
`POST /api/resume-versions/{id}/duplicate`, and
`GET /api/resume-versions/{id}/pdf`; title and `contact.name` are
validated at the controller (400 if blank), same division of
responsibility `AuthController`/`AuthService` already use. Every
section but Contact is skipped entirely when empty, both in the JSON
response and in the PDF, so a mostly-blank resume renders as a short,
clean document instead of empty headings or stranded whitespace.

Frontend: `components/ResumeBuilderScreen.jsx` - a list view (title,
last-updated date, Edit/Duplicate/Delete/Download PDF) and a form view,
each section (Contact, Summary, Skills, Projects, Education, Experience,
Certifications) as its own bordered block the user can add/remove
entries in, all optional except Contact. Skills render as removable
chips with an Enter-to-add input; Experience entries have their own
nested add/remove list of bullet points. Delete uses an inline "Delete
this resume? Confirm/Cancel" row state instead of a native
`window.confirm()`, consistent with the rest of the app never using
browser dialogs. "My Resumes" moved out of `Sidebar.jsx`'s disabled
`PLANNED_NAV` list (it was already anticipated there, same as Compare
Jobs was for Phase 21) and wired into `App.jsx` as a `resumes` page the
same way every other real nav item is.

Verified for real: `mvn compile`/`mvn test` and `npm run build` both
clean. Live backend testing via curl covered every edge the design
called for - create with only Contact filled in (succeeds, all other
sections come back as empty arrays), missing title / missing
`contact.name` (400 with a clear message), a full multi-section resume
(all seven sections round-trip exactly), duplicate (copies every field,
appends " (Copy)" to the title), PDF generation for both the full
resume and the Contact-only one (`pdftotext -layout` confirmed correct
section ordering and line breaks, and confirmed the minimal PDF has no
broken/blank sections - just the name and email), and cross-user access
to every route (`GET`/`PUT`/`DELETE`/duplicate/PDF) all returning 404
for another user's resume id. Live in-browser: created a full resume by
hand through the real form (including the Skills chip-add/remove flow
and Experience's nested bullet add/remove, verified removing one bullet
correctly leaves its sibling intact), edited it and confirmed every
section reloads with the exact data that was saved, duplicated it,
downloaded and opened the real PDF (content matched what was in the
form), and deleted the duplicate via the inline confirm row - all
confirmed in both light and dark theme, no browser console errors.
Regression-checked Analyze Resume, History, Skill Roadmap, Interview
Prep, Career Fit, and Compare Jobs afterward since `Sidebar.jsx` and
`App.jsx` are shared files this phase also edited - all six still work
exactly as before.

**Phase 23 (Resume Version Comparison) complete.** Lets the user pick 2
of their own Resume Builder versions and see how each would score
against one ad-hoc job description - "which version of my resume should
I send for this job," computed live and never saved as a History entry
(unlike `POST /api/match`, which always persists a `GapReport`).

The one new piece of matching-adjacent logic: `ResumeVersionService.
flatten()` turns one version's structured JSON sections (contact,
summary, skills, projects, education, experience, certifications) into
plain text - name, summary, "Skills: ..." line, then each project/
education/experience/certification entry as its own line (bullets get
their own lines too) - by reusing `toView()`'s existing JSON parsing and
just re-rendering it as text instead of a JSON response. That plain text
is handed to the existing, completely unchanged `SkillMatchingService.
analyze()` - Phase 1's matching logic doesn't know or care whether the
text it's given came from a parsed PDF or a flattened Resume Builder
version.

Backend: `resumebuilder/FlattenedResumeVersion.java` (a small title+text
carrier between services, not an API shape) and
`resumebuilder/ResumeVersionComparisonService.java` (loops the exactly-2
requested ids, flattens each via the ownership-checked
`ResumeVersionService.flatten()`, runs `SkillMatchingService.analyze()`
against the same `requiredSkills` for each, sorts by match percentage
descending - same best-fit-first convention Phase 21's `compareReports`
uses). `POST /api/resume-versions/compare` on `ResumeVersionController`
validates exactly 2 ids (400 otherwise), non-blank `jdText` (400), and a
non-empty `requiredSkills` (400); if either id doesn't resolve to one of
the logged-in user's own resume versions, the whole request 404s rather
than revealing which id failed - same never-confirm-or-deny reasoning
every other ownership check in this codebase already uses. `jdText` is
echoed back in the response for display context only - nothing from
this endpoint is written to the database.

Frontend: `ResumeBuilderScreen.jsx`'s list view grows a checkbox column
(same pattern `HistoryScreen` uses for Compare Jobs, capped at exactly 2
here instead of 2-5) with a "Compare for a Job" button once 2 are
ticked. That opens a JD-paste + `SkillsChecklist` form (reusing the same
component Analyze Resume's New Analysis form uses), and submitting
renders both versions ranked side by side - "Best Fit" badge and accent
border on the top card, a color-coded match-% bar, and matched (green
check)/missing (gray dash) skill chips per version - the same visual
language Phase 21's Compare Jobs already established, just comparing
resume versions instead of past reports. "Back to My Resumes" resets
both the selection and the result and returns to the list.

Verified for real: `mvn compile`/`mvn test` and `npm run build` all
clean. Live backend testing via curl proved the actual differentiation
the design called for - a Java/Spring-heavy resume version and a
JavaScript/React-heavy one, compared against a Java-focused JD, scored
100% and 0% respectively with correct per-skill reasoning (not just a
plausible-looking number) - plus every validation edge: 1 id (400), 3
ids (400), missing `jdText` (400), empty `requiredSkills` (400), and a
request mixing the logged-in user's own id with another user's id
(404), confirmed with a second test account. Live in-browser: built a
real second resume version by hand, selected 2 via the checkbox flow,
ran two different real comparisons (a Java JD and a React JD) and
watched the ranking and matched/missing chips flip correctly between
them - including one comparison where a match came from a project's
"tech" field rather than the Skills section, confirming flatten() picks
up all sections, not just the obvious one - all confirmed in both light
and dark theme, no browser console errors. Regression-checked Analyze
Resume, History, Skill Roadmap, Interview Prep, Career Fit, Compare
Jobs, and My Resumes afterward since this phase edited shared files
again - all seven still work exactly as before.

**Phase 24 (Career Progress Dashboard) complete.** Activates the two
sidebar items ("Dashboard" and "Progress") that had been sitting in
`Sidebar.jsx`'s disabled `PLANNED_NAV` list since Phase 12 - Analyze
Resume stays the default landing page after login; Dashboard/Progress
are pages you navigate to. Both are pure aggregations of columns
`GapReport` already stores (reusing `jobReadinessScore`/
`jobReadinessLabel` from Phase 16) - no new entity, no new scoring logic.

Backend: two new read-only `GapReportRepository` queries
(`findByResume_User_EmailOrderByCreatedAtAsc` for the chronological
trend, `countByResume_User_Email` for the total-analyses count) plus
`ReportService.getDashboardSummary`/`getProgressTrend`, exposed as
`GET /api/reports/dashboard-summary` and `GET /api/reports/progress-trend`
on `ReportController` - same 204-means-no-reports-yet convention as
Learning Roadmap/Interview Prep/Career Fit. `dashboard-summary` returns
the latest report's Job Readiness Score/label plus the total report
count and latest `createdAt`; `progress-trend` returns every report's
`{reportId, createdAt, jobReadinessScore, jobReadinessLabel}` sorted
oldest to newest (the one deliberate reversal of this codebase's usual
newest-first convention, since this is for plotting left to right).

Frontend: `DashboardScreen.jsx` (hero score + label, total-analyses and
last-analyzed stat tiles, quick links to Analyze Resume/History) and
`ProgressScreen.jsx` (a hand-rolled inline SVG line chart plotting Job
Readiness Score 0-100 over time, with dashed reference lines at the
85/70/50 score bands and a plain HTML data table below it as the
screen-reader-friendly alternative to the chart) - no charting library
added, staying within the existing React + Tailwind stack per the
tech-stack constraint. Both use the same `undefined`/`null`/real-payload
three-state loading pattern every other real page already uses. The
chart explicitly branches on point count: 0 renders the shared empty
state, exactly 1 renders a single centered point with no connecting
line, 2+ renders the full polyline - each verified as its own case, not
inferred from the multi-report path. `fetchDashboardSummary()`/
`fetchProgressTrend()` added to `api.js` with the same 204-returns-null
convention as the other "latest report" fetchers.

Verified for real: `mvn compile`/`mvn test` and `npm run build` both
clean. Live backend testing via curl proved every case the design called
for - a fresh user with 0 reports got 204 from both endpoints; after one
`/api/match` call, both endpoints returned that single report's exact
numbers (hand-verified against `GET /api/reports/{id}`'s own
`jobReadiness` block); after three reports with deliberately different
scores (51, 7, 60), `progress-trend` came back in the correct
oldest-to-newest order regardless of insertion order and
`dashboard-summary` reflected the latest (60/NEEDS_WORK) with the
correct total (3). Live in-browser (Chrome, via claude-in-chrome):
logged into the 3-report account and confirmed the Dashboard hero score/
label/stat tiles and the Progress chart/table match those exact
numbers; confirmed the light-theme palette on both screens; logged into
a fresh 0-report account and confirmed both screens' empty states and
their "Go to Analyze Resume" CTAs; logged into a 1-report account and
confirmed the chart renders a single point (not a broken line) with a
matching one-row table; console was clean (no errors) throughout.
Regression-checked all 8 existing real screens (Analyze Resume + its
History tab, Skill Roadmap, Interview Prep, Career Fit, Compare Jobs'
empty state, and My Resumes) afterward since `Sidebar.jsx` and
`App.jsx` are shared files this phase also edited - all still work
exactly as before.

**Phase 25 (Smart PDF Career Report) complete.** A new
`export/CareerReportPdfService.java` combines three EXISTING per-report
views - the persisted Job Readiness Score/breakdown (Phase 16, read
straight off the `GapReport` row), `LearningRoadmapService`'s live
roadmap (Phase 17/18), and `InterviewQuestionService`'s live interview
questions (Phase 19) - into one PDF, all three already scoped to the
user's single most recent report. None of those three services' own
logic changed; this only reads what they already produce and lays it
out with the same manual word-wrap/pagination PDFBox technique
`GapReportPdfService` established in Phase 9 - kept as its own copy
(same reasoning Phase 22's `ResumeVersionPdfService` already used) since
this document's section structure (score breakdown, then skill gaps,
then roadmap, then interview questions) doesn't overlap with the
single-report Gap Report layout. This is a new, separate combined
document - NOT the "Future PDF Redesign Reference" visual redesign of
the single Gap Report PDF that stays gated until all of Phases 13-27 are
done.

`GET /api/reports/latest/career-report-pdf` on `ReportController`
follows the same `/latest/...` convention as the roadmap and
interview-questions endpoints, but since it returns bytes rather than
JSON, "no reports yet" is a 404 with a plain-text reason ("Analyze a
resume first to generate a career report.") instead of the 204 those
other routes use - `protectedFetch`'s existing error-message handling in
`api.js` surfaces that text as a normal thrown error, so no new
frontend error-handling path was needed.

Frontend: a "Download Career Report" button added to `DashboardScreen.jsx`
(Phase 24) alongside the existing quick links - bordered/secondary style
like "View History", deliberately not the primary accent button (that
stays "Analyze Resume"), with a real spinning-ring loading state inside
the button while the PDF generates. Reuses the same fetch-as-blob +
synthetic-`<a download>` pattern `GapReportScreen`'s existing single-report
PDF button already uses. `GapReportScreen` itself was NOT touched - it
keeps its existing PDF button unchanged, so the two differently-scoped
downloads (one report vs. this account-wide career snapshot) never
compete on the same screen.

Verified for real: `mvn compile`/`mvn test` and `npm run build` both
clean. Backend: generated the PDF for a user with zero reports (404,
`Analyze a resume first...`) and for a real two-report account crafted
to exercise every section at once - 4 matched skills, 3 missing skills
recurring across both reports (so Phase 15 gave them a real IMPORTANT
priority tier, not just a single-report default), and 1 underemphasized
skill - then read the actual PDF bytes back (`Read` on the downloaded
file, not just "a PDF downloaded") and hand-checked every section's
numbers against that same report's `GET /api/reports/{id}` JSON: the
34/100 Not Ready score and its four weighted components matched exactly,
all three missing skills appeared in Skill Gaps tagged Important in the
same order, the Learning Roadmap showed the correct curated project idea
and official-docs link per skill (and the correct generic fallback for
Kubernetes, which has neither), and Interview Prep listed exactly the 4
matched skills with their real curated questions - plus pagination broke
cleanly onto a second page with no orphaned section title. Live
in-browser (Chrome): confirmed the button renders in the bordered/
secondary style next to "Analyze Resume"/"View History" in both light
and dark theme, clicked it and confirmed the PDF actually downloaded
(network request returned 200, Chrome opened the file), and confirmed a
clean console throughout. Regression-checked Analyze Resume, Progress,
Skill Roadmap, Interview Prep, Career Fit, Compare Jobs, and My Resumes
afterward since this phase edited the shared `api.js` - all still work
exactly as before, and the Skill Roadmap/Interview Prep screens'
on-screen data visibly matched the PDF's content for the same report.

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
