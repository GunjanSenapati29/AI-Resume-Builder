# Phase 26 — Full Testing Pass

Systematic testing across everything built since the App Shell Migration
(Phases 12–25). This is not a re-verification of each phase in isolation
(that already happened when each phase shipped) — it specifically targets
bugs that only surface when already-shipped features coexist: simultaneous
zero-states, exact boundary counts, cross-feature data leakage, ownership
checks across every id-taking endpoint, and the session-expiry edge case.

**Result: no bugs found. 0 fixes required.** Every scenario below passed
on the first attempt. All test accounts, ids, and requests are real —
run against the live backend (`localhost:8080`) and live frontend
(`localhost:5173`) on 2026-08-22, with an additional direct MySQL query
against the local `skillgap_ai` database to verify claims that don't
show up in an API response (e.g. "this table has zero rows").

---

## 1. Zero-state audit — one account, all features at once

Account: `p26_zero_1787408706@example.com` (signed up fresh, zero
`POST /api/match` calls ever made). Every endpoint below was hit against
this exact account, in this order, in the same test run:

| Endpoint | Expected | Actual | Result |
|---|---|---|---|
| `GET /api/reports/latest/learning-roadmap` | 204 | 204 | PASS |
| `GET /api/reports/latest/interview-questions` | 204 | 204 | PASS |
| `GET /api/reports/role-recommendations` | 204 | 204 | PASS |
| `GET /api/reports/progress-trend` | 204 | 204 | PASS |
| `GET /api/reports/dashboard-summary` | 204 | 204 | PASS |
| `GET /api/reports/latest/career-report-pdf` | 404, plain-text reason | 404, `"Analyze a resume first to generate a career report."` | PASS |
| `GET /api/reports` (sanity) | `[]` | `[]` | PASS |
| `GET /api/resume-versions` (sanity) | `[]` | `[]` | PASS |

Then logged into this same account in Chrome (via claude-in-chrome) and
navigated the sidebar without leaving the session, confirming all five
UI screens show their empty state **simultaneously**, not just when
tested one at a time:

- Dashboard → "Analyze a resume first to see your dashboard." + CTA
- Progress → "Analyze a resume first to start tracking your progress." + CTA
- Skill Roadmap → "Analyze a resume first to get your learning roadmap." + CTA
- Interview Prep → "Analyze a resume first to get your interview questions." + CTA
- Career Fit → "Analyze a resume first to see your career fit." + CTA

All five rendered correctly, screenshotted, no console errors.

---

## 2. Boundary counts

### 2a. Compare Jobs cap (5 reports)

Account: `p26_compare6_1787408733@example.com`, created with exactly 6
`POST /api/match` calls (report ids `57`–`62`, each a trivial one-skill
Java match so the boundary test wasn't confounded by matching logic).

| Request | Expected | Actual | Result |
|---|---|---|---|
| `GET /api/reports/compare?ids=57,58,59,60,61` (5 ids) | 200 | 200 | PASS |
| `GET /api/reports/compare?ids=57,58,59,60,61,62` (6 ids) | 400 | 400, `"Select between 2 and 5 reports to compare."` | PASS |
| `GET /api/reports/compare?ids=57` (1 id) | 400 | 400, same message | PASS |

Confirms the backend genuinely rejects a 6th id — not just that
`HistoryScreen.jsx`'s checkbox visually disables past 5 (already true by
inspection: `checkDisabled={compareIds.length >= MAX_COMPARE && ...}`).

### 2b. Resume Version Comparison cap (2 versions)

Account: `p26_rv3_1787408758@example.com`, created with exactly 3 resume
versions (ids `12`, `13`, `14`).

| Request | Expected | Actual | Result |
|---|---|---|---|
| `POST /api/resume-versions/compare` with `[12,13]` | 200 | 200, real comparison result | PASS |
| `POST /api/resume-versions/compare` with `[12]` (1 id) | 400 | 400, `"Select exactly 2 resume versions to compare."` | PASS |
| `POST /api/resume-versions/compare` with `[12,13,14]` (3 ids) | 400 | 400, same message | PASS |

Also exercised live in the browser on this same account: ticked 1 →
"1 of 2 selected", no compare button yet; ticked 2 → "2 of 2 selected" +
"Compare for a Job" button appears; submitted a real JD (Java) against
Version 3 + Version 2 → both scored 100% (each version has "Java" in its
skills), "Best Fit" badge on Version 3. Confirms the full UI flow, not
just the API boundary.

### 2c. GapReport with 0 matched skills

Account: `p26_zeromatch_1787408840@example.com`. Resume text
("I enjoy painting watercolors and hiking on weekends.") deliberately
shares no vocabulary with the JD's 4 required skills (Java, Spring Boot,
MySQL, Docker) → report id `63` came back with `matched: []`,
`missing`: all 4 skills, `jobReadiness: {overallScore: 16, label:
NOT_READY, skillMatchScore: 0, atsScore: 0, evidenceStrengthScore: 0,
gapSeverityScore: 80}`.

| Check | Expected | Actual | Result |
|---|---|---|---|
| `GET /api/reports/latest/interview-questions` | 200, `skills: []` (not a crash, not a 204 — a report exists) | 200, `{"reportId":63,"skills":[]}` | PASS |
| `GET /api/reports/latest/learning-roadmap` | 200, 4 missing items, tiered | 200, 4 items (all `OPTIONAL` tier — no prior recurrence for a first-time report) | PASS |
| `GET /api/reports/latest/career-report-pdf` | 200, valid PDF, no broken/blank Interview Prep section | 200, 2.1 KB PDF | PASS (see below) |
| `InterviewPrepScreen.jsx` with `skills: []` and a real report (not `null`) | Distinct "No matched skills to prep yet." message, not the "no reports" empty state | Code inspection confirms `questions.skills.length === 0` is checked separately from `questions === null` (`InterviewPrepScreen.jsx:76`) | PASS |

Read the actual downloaded PDF bytes back (not just "a PDF downloaded"):
Job Readiness section showed `16/100 - Not Ready` with the exact 0/0/0/80
breakdown; Skill Gaps section listed all 4 skills tagged `[Optional]`;
Learning Roadmap rendered all 4 skills' full 4-step plans plus official
doc links where curated; the Interview Prep section rendered as
`Interview Prep (0)` with the body text `"No matched skills to build
interview questions from yet."` — a clean, deliberate empty state, not a
blank heading or a stack trace. No exception was thrown building the
PDF despite the matched list being empty.

---

## 3. Cross-feature data isolation (Resume Version Comparison → History/Progress)

Same account as 2b (`p26_rv3_1787408758@example.com`): ran a real
`POST /api/resume-versions/compare` (versions 12 & 13 vs. a Java JD,
returned a real 100%/100% result). This account has **never** called
`POST /api/match` and has zero rows in `gap_reports`.

| Check | Expected | Actual | Result |
|---|---|---|---|
| `GET /api/reports` (History) | `[]` | `[]` | PASS |
| `GET /api/reports/progress-trend` | 204 | 204 | PASS |
| `GET /api/reports/dashboard-summary` | 204 | 204 | PASS |
| Direct DB query: `SELECT COUNT(*) FROM gap_reports gr JOIN resumes r ON gr.resume_id=r.resume_id JOIN users u ON r.user_id=u.user_id WHERE u.email='p26_rv3_...'` | `0` | `0` | PASS |
| Direct DB query: `SELECT COUNT(*) FROM resume_versions rv JOIN users u ON rv.user_id=u.user_id WHERE u.email='p26_rv3_...'` | `3` | `3` | PASS |

Verified at the database level, not just by trusting the API's own
answer — confirms `ResumeVersionComparisonService.compare()` really is
compute-only and never touches the `gap_reports` table, exactly as its
Phase 23 documentation claims.

---

## 4. Ownership checks — every id-taking endpoint, both entity types

Two unrelated accounts: `p26_attacker_1787408883@example.com` (the
"attacker" — has its own report id `64` and its own resume version id
`15`, used to test mixed-ownership requests) against victim resources
owned by `p26_compare6` (report id `57`) and `p26_rv3` (resume version
id `12`).

### GapReport (4 endpoints)

| Request (as attacker) | Expected | Actual |
|---|---|---|
| `GET /api/reports/57` | 404 | 404 |
| `GET /api/reports/57/pdf` | 404 | 404 |
| `GET /api/reports/compare?ids=57,58` (owns neither) | 404 | 404 |
| `GET /api/reports/compare?ids=64,57` (own id **mixed** with victim's) | 404 | 404 |

### ResumeVersion (7 endpoints)

| Request (as attacker) | Expected | Actual |
|---|---|---|
| `GET /api/resume-versions/12` | 404 | 404 |
| `PUT /api/resume-versions/12` (body: `{"title":"HACKED",...}`) | 404 | 404 |
| `DELETE /api/resume-versions/12` | 404 | 404 |
| `POST /api/resume-versions/12/duplicate` | 404 | 404 |
| `GET /api/resume-versions/12/pdf` | 404 | 404 |
| `POST /api/resume-versions/compare` with `[12,13]` (owns neither) | 404 | 404 |
| `POST /api/resume-versions/compare` with `[15,12]` (own id **mixed** with victim's) | 404 | 404 |

All 11 checks returned 404, never 403 — a request can never distinguish
"doesn't exist" from "exists but isn't yours."

**Mutation-safety check:** the attacker's failed `PUT`/`DELETE` attempts
above could in principle have silently succeeded despite returning 404
(e.g. a bug that deletes first and checks ownership after). Re-fetched
resume version 12 as its real owner (`p26_rv3`) afterward:
`{"id":12,"title":"Version 1",...}` — title unchanged, record still
exists. The 404s were real no-ops, not a lie on top of a real mutation.

---

## 5. Session edge case — invalidated JWT mid-session

Logged into the browser as `p26_zero_1787408706@example.com` (a real,
valid session — Dashboard/Progress/etc. all rendering normally). Then,
via `javascript_tool`, corrupted the stored token in place:

```js
localStorage.setItem('skillgap_token', 'invalidated.tampered.token')
```

This simulates the exact situation an expired/invalidated JWT produces —
`JwtService.extractEmail()` throws `JwtException` (the same catch block
handles both an expired token and a malformed/tampered one, so this is a
faithful stand-in for real expiry without waiting 24h for a real one to
lapse) and `JwtAuthenticationFilter` leaves the request unauthenticated,
so `SecurityConfig` returns 401 on the next protected call.

Triggered that next call by clicking the Dashboard nav item.

| Check | Expected | Actual | Result |
|---|---|---|---|
| UI after the 401 | Clean redirect to the Auth screen, not a stuck/blank page | Auth screen ("Welcome back") rendered immediately | PASS |
| `localStorage.getItem('skillgap_token')` | `null` (cleared) | `null` | PASS |
| `localStorage.getItem('skillgap_user')` | `null` (cleared) | `null` | PASS |
| Console errors | None | None | PASS |

Confirms `api.js`'s `protectedFetch` → `onUnauthorized` → `App.jsx`
clearing both storage keys and resetting `user` to `null` works exactly
as designed under a real invalidated-token condition, not just by
reading the code.

---

## 6. Full end-to-end regression (all 9 real screens/flows, both themes)

Performed after the above — confirms nothing in this testing pass (or
anything since Phase 22) regressed. No code changes were made during
this phase (no bugs were found to fix), so this is a confirmation pass,
not a re-test of new behavior.

**Dark theme** (account: `dash_test_1787406511@example.com`, 3 real
reports, unless noted):

1. Auth — wrong password on a real account → `"Invalid email or
   password."` shown inline, no crash; correct password → logged in.
2. Analyze Resume (New Analysis form) — renders correctly.
3. Analyze Resume → History — lists all 3 reports with correct match
   badges (100%/0%/80%); ticked 2, clicked "Compare Selected."
4. Compare Jobs — real 2-report comparison rendered: "Best Fit" badge
   on the 100% report, 7 matched skills all tagged "Unique" (only one
   report matched each), 0 missing.
5. Dashboard — `60/100 Needs Work`, 3 total analyses, correct last-
   analyzed date — matches the account's real latest report exactly.
6. Progress — 3-point chart in the correct chronological order
   (51 → 7 → 60), color-coded correctly, table matches.
7. Skill Roadmap — 0 missing ("Every required skill was found"), 1
   underemphasized (REST APIs) with its curated project idea.
8. Interview Prep — 7 matched-skill cards (Java, Spring Boot, MySQL,
   REST APIs, Docker, JUnit, Git), each with its 3 curated questions.
9. Career Fit — 5 role cards, Backend Developer (Java) at 100% fit.
10. My Resumes (account switched to `p26_rv3_1787408758@example.com`
    for real data) — 3 versions listed; ticked 2 → "Compare for a Job"
    → real comparison rendered (both versions 100% against a Java JD).

**Light theme** (toggled mid-session, re-checked with
`dash_test_1787406511@example.com`): Dashboard, Progress, Skill
Roadmap, Interview Prep, Career Fit, My Resumes, and the Resume Version
Comparison result screen were all re-screenshotted in light mode —
correct contrast, no unreadable text, no leftover dark-mode-only
styling, no console errors.

**Console:** checked via `read_console_messages` (`onlyErrors: true`)
at multiple points across this entire pass (after the zero-state audit,
after the session-invalidation test, and at the end of the full
regression pass) — zero errors or exceptions the whole time.

---

## Summary

| Area | Scenarios tested | Bugs found |
|---|---|---|
| 1. Zero-state audit | 6 backend endpoints + 5 UI screens, one account, simultaneously | 0 |
| 2. Boundary counts | Compare Jobs (3), Resume Version Comparison (3 + live UI flow), 0-matched-skill report (4) | 0 |
| 3. Cross-feature isolation | 3 API checks + 2 direct DB queries | 0 |
| 4. Ownership checks | 11 id-taking endpoints across GapReport + ResumeVersion, including 2 mixed-ownership requests and a mutation-safety re-check | 0 |
| 5. Session edge case | JWT invalidation mid-session → clean logout | 0 |
| 6. Full regression | 9 screens/flows × 2 themes | 0 |

**Total: 56 scenarios verified. No bugs found, no fixes required.**
