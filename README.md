# Placement Preparation Platform

A mobile-friendly placement preparation website for students from second year onward. Spring Boot serves the web interface and REST API from one origin. Student accounts and learning progress are stored in MySQL, so a student can sign in from another device and continue their progress.

## Run locally with Docker Desktop

1. Install and start Docker Desktop.
2. From the project root, copy `.env.example` to `.env` and set the database passwords. Generate a long JWT secret in PowerShell:

   ```powershell
   $bytes = New-Object byte[] 48
   [System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
   [Convert]::ToBase64String($bytes)
   ```

   Paste the generated value for `JWT_SECRET` in `.env`. Keep `.env` private; it is ignored by Git.

3. Start the app and MySQL:

   ```powershell
   docker compose up --build
   ```

4. Open `http://localhost:8080`. Stop the services with `Ctrl+C`; the MySQL volume keeps student data between restarts. To stop the containers later, run `docker compose down` from the project root.

## Publish online for access from any device

This project has a root `Dockerfile`, uses a hosted MySQL-compatible database, and reads the port and secrets from environment variables. One supported route is Railway. Its current documentation describes a MySQL service, private service-to-service variables, and generated public domains. Hosting can incur charges; check the provider's current pricing before deploying.

1. Create a GitHub repository and upload this project folder. Do **not** upload `.env` or put real passwords or API keys in source files.
2. In Railway, create a project from the GitHub repository. The root `Dockerfile` builds the Spring Boot application.
3. Add a MySQL service to the same Railway project. Keep the database private.
4. In the app service's Variables, set the following. If your database service has a different name than `MySQL`, select it in Railway's variable picker and use that name in the references.

   ```text
   DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?allowPublicKeyRetrieval=true&useSSL=false
   DB_USERNAME=${{MySQL.MYSQLUSER}}
   DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
   JWT_SECRET=<paste a unique random value generated locally>
   ```

   Railway supplies `PORT`. `OPENAI_API_KEY` is optional and only needed for AI interview feedback. Keep it as a server-side variable.

5. Deploy the app. In its service settings, choose **Networking → Generate Domain**. Open the generated HTTPS URL on your computer or phone and sign in to the same account to load saved profile and learning progress.

6. Keep the Railway account credentials and database access private. Set up database backups before relying on the site for real student records.

## Run without Docker

Requirements: Java 21+ and MySQL 8+. Create a MySQL database and user for `placement_db`. From PowerShell in `backend\placement-backend`, set `DB_USERNAME`, `DB_PASSWORD`, and a `JWT_SECRET` of at least 32 characters, then run:

```powershell
$env:DB_USERNAME = 'placement_app'
$env:DB_PASSWORD = 'your-local-database-password'
$env:JWT_SECRET = 'paste-a-random-secret-of-at-least-32-characters'
.\mvnw.cmd spring-boot:run
```

Open `http://localhost:8080`.

## Student features

- Registration, login, and a student profile with year, college, branch, graduation year, and selected domain.
- Learning paths and year-by-year roadmaps for Java Full Stack, Frontend, Backend, Data Analytics, and QA Automation.
- Study notes and examples for Java, collections, SQL, REST, aptitude, reasoning, and Big O, plus video lesson search links.
- Coding prompts, Java starter drafts, examples, and hints. The platform does not execute submitted code.
- Aptitude, reasoning, and computer science practice questions with explanations.
- Daily and weekly practice tests with saved results.
- HR and manager interview prompts. AI answer feedback is available when `OPENAI_API_KEY` is configured on the server.
- Profile and learning progress saved to the student's MySQL account and loaded after sign-in on another device.

## API endpoints used by the web app

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create student account |
| POST | `/api/auth/login` | Public | Get a JWT |
| GET / PUT | `/api/students/me` | Bearer token | Read or update profile |
| GET / PUT | `/api/students/me/progress` | Bearer token | Load or save cross-device learning progress |
| POST | `/api/ai/interview-feedback` | Bearer token | Optional AI interview coaching |
| GET | `/api/test` | Public | Health check |

## Current limits

The question bank and lesson notes are bundled with the app and are not admin-editable yet. The coding area saves drafts but does not compile or run student code; connect an isolated sandboxed judge before enabling execution. AI interview feedback requires a server API key and may incur provider charges.
