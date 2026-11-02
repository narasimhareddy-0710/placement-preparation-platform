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

## Deploy online at no charge

This project can use Render's Free web service and a TiDB Cloud Starter Free MySQL-compatible database. Do not choose paid plans or enable paid usage. Keep the TiDB spending limit at $0 and do not add a payment method. The free tiers have limits: Render sleeps the web service after 15 minutes without traffic, so its first request after sleeping can take about a minute. TiDB includes 5 GiB row storage and 50 million Request Units each month for a free MySQL-compatible instance; after a quota is reached, database requests are restricted until the quota resets. These tiers suit a learning/demo site, not a production service with uptime guarantees.

1. Create a **private** GitHub repository named `placement-preparation-platform`. Do not upload any `.env` file. The root `.gitignore` excludes local secrets and build output.
2. Push this project to the private repository.
3. Create a TiDB Cloud Starter instance using **MySQL Compatible**. Keep its spending limit at **$0**. In its Connect panel, copy the host, port, username, password, and database name. TiDB's public connection requires TLS.
4. In Render, create a Blueprint from the GitHub repository. Render reads the root `render.yaml` and builds the root `Dockerfile`. Choose the **Free** web plan. When asked for environment values, enter:
   - `DB_URL`: `jdbc:mysql://HOST:PORT/DATABASE?sslMode=VERIFY_IDENTITY&tlsVersions=TLSv1.2` (replace the three uppercase placeholders with the TiDB Connect values).
   - `DB_USERNAME`: the exact TiDB username from Connect, including any required prefix.
   - `DB_PASSWORD`: the TiDB password. Do not put it in GitHub or this README.
   - Render generates `JWT_SECRET` for the app.
5. Wait for the Blueprint deployment to become live, then open its `onrender.com` URL on your phone or another device. Create a new account there; your local MySQL accounts are not copied to TiDB.

Free-plan details can change. Check the current [Render free service limits](https://render.com/docs/free) and [TiDB Cloud Starter quotas](https://docs.pingcap.com/tidbcloud/serverless-limitations/) before deploying. Render documents that free services spin down after inactivity; TiDB documents free quotas and that over-quota instances stop accepting new connections until reset.
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
