# Placement Preparation Platform

A mobile-friendly placement preparation website for students from second year onward. Spring Boot serves the website and REST API. Student accounts and learning progress are stored in Cloud Firestore so students can sign in from another device and continue their progress.

## Firebase setup

1. Create a Firebase project in the [Firebase Console](https://console.firebase.google.com/).
2. Open **Build → Firestore Database**, create a **Standard** database, choose its location, and start in **Production mode**. The backend uses the Firebase Admin SDK, so do not open database access to web clients.
3. In **Project settings → Service accounts**, generate a private key JSON file. Keep this file outside the repository. Never commit or share it.
4. Copy `.env.example` to `.env` and fill in `FIREBASE_PROJECT_ID`, `FIREBASE_SERVICE_ACCOUNT_FILE` (an absolute path to that JSON file), and a random `JWT_SECRET` of at least 32 characters. `.env` is ignored by Git.

The backend reads local credentials through `GOOGLE_APPLICATION_CREDENTIALS` and reads the project ID from `FIREBASE_PROJECT_ID`. On Render, provide the service-account JSON content through the private `FIREBASE_SERVICE_ACCOUNT_JSON` environment variable instead of uploading the key file.

## Run locally

Requirements: Java 21+ and Firebase credentials configured as above.

From PowerShell in `backend\placement-backend`, set the service-account file path and project ID. `GOOGLE_APPLICATION_CREDENTIALS` must be a PowerShell environment variable for the Google credential library to find the file. You may keep `FIREBASE_PROJECT_ID` and `JWT_SECRET` in the local `.env` file. Then run:

```powershell
$env:FIREBASE_PROJECT_ID = 'your-firebase-project-id'
$env:GOOGLE_APPLICATION_CREDENTIALS = 'C:\path\to\firebase-service-account.json'
$env:JWT_SECRET = 'paste-a-random-secret-of-at-least-32-characters'
.\mvnw.cmd spring-boot:run
```

Open `http://localhost:8080`.

To run with Docker Compose, copy the root `.env.example` to `.env`, set `FIREBASE_SERVICE_ACCOUNT_FILE` to the absolute service-account JSON path and fill in the other values, then run `docker compose up --build` from the project root. Stop it with `Ctrl+C`.

## Deploy online

The included Render Blueprint builds the root `Dockerfile` and runs a free Render web service. A free service may sleep after inactivity, so the first request after a quiet period can take about a minute. Free plans are for learning and demos; limits can change. See [Render free service details](https://render.com/docs/free) and [Firestore pricing and quotas](https://firebase.google.com/docs/firestore/pricing).

1. Push this project to your GitHub repository. Do not upload `.env` or any Firebase service-account key.
2. In Render, create a Blueprint from the repository so Render reads `render.yaml`, or create a Docker Web Service using the root `Dockerfile`.
3. Choose the **Free** service plan.
4. Add `FIREBASE_PROJECT_ID` and `FIREBASE_SERVICE_ACCOUNT_JSON` in the Render service's environment settings. Paste the full JSON key contents into the secret value field. Add a strong `JWT_SECRET` (at least 32 characters). Do not put these secrets in GitHub.
5. Deploy. When Render reports the service is live, open its `onrender.com` address in Chrome on a computer or phone.

The hosted Firestore database starts empty. Existing student rows in a local MySQL database are not automatically copied; students will need to register again unless you perform a separate data migration.

## Student features

- Registration, login, and a profile with year, college, branch, graduation year, and selected domain.
- Learning paths and year-by-year roadmaps for Java Full Stack, Frontend, Backend, Data Analytics, and QA Automation.
- Study notes and examples for Java, collections, SQL, REST, aptitude, reasoning, and Big O, plus video lesson search links.
- Coding prompts, Java starter drafts, examples, and hints. The coding area does not execute submitted code.
- Aptitude, reasoning, and computer science practice questions with explanations.
- Daily and weekly practice tests with saved results.
- HR and manager interview prompts. AI interview feedback requires `OPENAI_API_KEY` and may incur provider charges.
- Profile and learning progress saved to a student's Firestore account and loaded after sign-in on another device.

## API endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create student account |
| POST | `/api/auth/login` | Public | Get a JWT |
| GET / PUT | `/api/students/me` | Bearer token | Read or update profile |
| GET / PUT | `/api/students/me/progress` | Bearer token | Load or save cross-device learning progress |
| POST | `/api/ai/interview-feedback` | Bearer token | Optional AI interview coaching |
| GET | `/api/test` | Public | Health check |
