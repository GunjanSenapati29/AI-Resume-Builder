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
| Frontend | React, Tailwind CSS, Recharts |
| Backend | Java, Spring Boot, Spring Security (JWT), Spring Data JPA |
| Database | MySQL |
| Resume parsing | Apache PDFBox |
| Matching logic | Plain Java + string similarity (Levenshtein / Jaro-Winkler) |
| Build tools | Maven (backend), npm (frontend), Git/GitHub |

No Python. No external AI API. No embeddings/ML model for the MVP.

## System architecture

```text
User (Browser)
      |
      v
React Frontend (Upload / Processing / Report / History / Auth)
      |  REST API (JSON over HTTPS)
      v
Spring Boot Controller Layer
      |
      v
Service Layer
   +-- ResumeParsingService   (Apache PDFBox)
   +-- SkillMatchingService   (taxonomy + string similarity)
   +-- ReportService          (builds the ranked gap report)
      |
      v
Repository Layer (Spring Data JPA)
      |
      v
MySQL Database
```

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

Screens: Landing, Upload & Compare, Processing, Gap Report (the hero screen),
History, Auth.

## Build order (work through ONE phase at a time)

1. Plain Java matching logic (no Spring, no DB) — **DONE, see status below**
2. MySQL schema + Spring Boot skeleton — **DONE, see status below**
3. Wire matching logic into `POST /api/match`, tested via curl — no persistence yet — **DONE, see status below**
4. Persistence: save each match as a GapReport row, add `GET /api/reports/{id}` — **DONE, see status below**
5. Resume PDF parsing (PDFBox) with manual-paste fallback — **DONE, see status below**
6. React screens, one at a time: 6a Upload & Compare, 6b Processing, 6c Gap Report, 6d History, 6e Auth — **DONE, see status below**
7. Apply the visual design system consistently — no functional changes — **NEXT**
8. Accessibility pass + empty/error states
9. PDF export of the report
10. README + demo script, tag `v1.0`

Commit after each phase with a message describing what that phase delivered.

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

**Next up: Phase 7** — apply the visual design system consistently
across all screens. No functional changes in this phase; every screen
already works functionally as of Phase 6e (including real auth), so
this is purely visual/UX polish.

## Definition of "outstanding" — check every screen against this

- Understandable with zero explanation within 5 seconds
- Every result shows *why*, not just *that*
- Visible feedback for every user action
- I can explain, in my own words, why it looks and works this way
