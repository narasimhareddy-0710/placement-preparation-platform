# Build roadmap

Build one module at a time, in this order. Each step should end with something you can click through in the browser.

## Phase 0: Foundation (backend done in this repo)
- [x] Student registration, login, JWT, profile
- [ ] Frontend project in `frontend/` (React + Vite): login, register, dashboard shell, protected routes

## Phase 1: Learning content (the core of the site)
| Module | Main tables |
|---|---|
| Domains / courses (Java Full Stack, Python, Data Analyst...) | `domain` |
| Roadmaps | `roadmap_step` (domain_id, order, title, description) |
| Topics and reading content | `topic` (domain_id, title, content as Markdown) |
| Learning videos | `video` (topic_id, title, youtube_url or file_url, duration) |
| Progress tracking | `student_progress` (student_id, topic_id, status) |

Tip: store videos on YouTube (unlisted) and save only the link. Hosting video files yourself is expensive and slow.

## Phase 2: Aptitude and reasoning
- `aptitude_category` (Quantitative, Logical, Verbal...), concept page + video per category
- `question` (category_id, text, options, correct_option, explanation, difficulty)
- Practice sets and a results page

## Phase 3: Tests
- `test` (type DAILY/WEEKLY, start, end), `test_question`, `test_attempt`, `attempt_answer`
- Server-side timer and scoring (never trust the score sent by the browser)
- Leaderboard

## Phase 4: Coding practice
- `coding_problem` (title, statement, difficulty, tags), `test_case` (input, expected_output, hidden)
- Code editor in the browser (Monaco editor)
- Running student code is dangerous: do **not** execute it inside your Spring Boot process.
  Use a sandbox such as Judge0 (self-hosted, free) or Piston, called from the backend.

## Phase 5: AI mock interviews (HR and manager rounds)
- Backend calls an LLM API: the question list per round, the student's answers, then feedback and a score
- Keep API keys on the server only, add per-student rate limits, store transcripts in `interview_session` / `interview_turn`
- Start with text answers; add voice later

## Phase 6: Admin panel and polish
- `ADMIN` role manages domains, content, questions and tests (`/api/admin/**` is already protected)
- Email verification and password reset, rate limiting on login, Flyway migrations, Docker, deployment
