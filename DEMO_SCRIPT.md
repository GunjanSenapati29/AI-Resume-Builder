# SkillGap AI — Demo Script

A rehearsed walkthrough for a viva or interview, roughly **3–5 minutes**.
Covers signup, the skill checklist, the Gap Report, PDF export, and History.

## Before you start

1. Have the backend and frontend both running (`./mvnw spring-boot:run` in
   `backend/`, `npm run dev` in `frontend/`), and `http://localhost:5173`
   open in a browser.
2. Have this resume and JD text ready in a notes app or scratch file so you
   can paste them instead of typing live — it's the exact pair used to
   verify this script, so the numbers below are real, not approximate:

   **Resume text:**
   > Software developer with hands-on experience in Java and Spring Boot,
   > using MySQL for persistence and Git for version control. Comfortable
   > writing REST APIs. Some exposure to JavaScript on the frontend.

   **Job description text:**
   > We are looking for a backend engineer skilled in Java, Spring Boot,
   > Spring Security, MySQL, REST APIs, Docker, React, and JUnit testing.
   > Prior experience with Git is a plus.

3. On the checklist, you'll tick these nine skills — mark **Java, Spring
   Boot, Spring Security, MySQL, REST APIs** as **core**, and leave
   **Docker, React, JUnit, Git** as nice-to-have.

With this exact input, the report comes out to **56% match**, **5 matched**,
**4 missing** (ranked JUnit → React → Docker → Spring Security), and
**4 underemphasized**. If you deviate from the script text, your numbers
will differ — that's fine, just don't promise a specific number you haven't
re-verified.

---

## 1. The pitch (30 seconds)

**Say:** "SkillGap AI compares a resume against a specific job description
and tells you exactly which required skills are matched, which are
missing, and which you have but didn't state clearly enough. Unlike a
generic AI resume checker, the matching here is fully rule-based, hand-
written Java — every result on screen traces back to a specific,
explainable rule, not a black-box model call."

## 2. Sign up (30 seconds)

**Do:** Land on the Auth screen. Click **Sign up**, fill in a name, email,
and an 8+ character password, click **Create account**.

**Say:** "Accounts are real — passwords are hashed with BCrypt, and every
request after login carries a JWT. Reports and history are scoped per
account, not shared or guest-based."

## 3. Upload & Compare (60 seconds)

**Do:** You're on the Upload & Compare screen. Point out the two resume
input modes — **Upload PDF** (parsed server-side with Apache PDFBox) and
**Paste text**. Stay on **Paste text**, paste the resume text from the prep
step. Paste the JD text into the Job description box.

**Say:** "I can either upload an actual PDF — it gets parsed server-side —
or paste text directly, which is what I'll do here to keep things quick."

**Do:** Scroll to the skill checklist. Tick the nine skills listed in the
prep step, marking Java, Spring Boot, Spring Security, MySQL, and REST APIs
as **core**.

**Say:** "This checklist comes from a real taxonomy on the backend, not
something I'm hardcoding per JD. Marking a skill 'core' matters later — it
drives the underemphasized check."

**Do:** Click **Compare**.

## 4. Processing (10 seconds)

**Do:** Let the Processing screen show briefly.

**Say:** "This is a real network call to `POST /api/match` — the spinner is
an actual loading state, not decoration."

## 5. The Gap Report (75 seconds)

**Do:** The report loads at **56% match**.

**Say:** "Five of nine required skills were found. Each one below states
*how* it was found — exact term, a known synonym, or a fuzzy/typo match —
never just a checkmark."

**Do:** Scroll to **Missing (4)**.

**Say:** "These are ranked easiest to close first — JUnit, then React, then
Docker and Spring Security, which are rated harder to pick up. That
ranking is the point: it tells you what to learn *first*, not just what's
missing."

**Do:** Scroll to **Underemphasized (4)**.

**Say:** "These four — Java, Spring Boot, MySQL, REST APIs — were found,
but only mentioned once each, even though I marked them core. On a real
resume, that's a prompt to say more about them, not just list them once."

## 6. PDF export (30 seconds)

**Do:** Click **Download PDF** near the top of the report. Open the
downloaded file.

**Say:** "The PDF is generated server-side with the same PDFBox library
that parses uploaded resumes — same content as the screen, laid out with
manual pagination since PDFBox doesn't do flowed text on its own."

## 7. History (30 seconds)

**Do:** Click **History** in the nav. Click into the report you just
created.

**Say:** "Every report is persisted and scoped to my account — this is the
same Gap Report screen, reopened from `GET /api/reports/{id}`, not a
separate view."

## 8. Close (15 seconds)

**Say:** "End to end: Spring Boot and Spring Security on the backend, JWT
auth, JPA/MySQL for persistence, PDFBox for both parsing and export, and a
rule-based matcher I wrote by hand rather than calling an external AI
model — so every decision the app makes is one I can actually explain."

---

## If something goes wrong live

- **Backend not responding / "Could not reach the server"** — check the
  backend terminal is still running; restart with `./mvnw spring-boot:run`.
- **Blank skill checklist** — the backend seeds its taxonomy from
  `SkillsTaxonomy.sampleTaxonomy()` on startup; if it's empty, the backend
  didn't start cleanly. Restart it.
- **Wrong numbers in the report** — you likely typed the resume/JD text
  slightly differently, or ticked a different set of skills. Re-paste from
  the prep step exactly, or just narrate the categories generically
  ("here's what matched, here's what's missing, ranked by difficulty")
  instead of quoting this script's exact percentage.
