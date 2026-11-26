package com.placement.placement_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.firebase.auth.FirebaseAuth;
import com.placement.placement_backend.exception.EmailAlreadyExistsException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Uses Firebase Authentication's REST API for email/password signup, sign-in, and verification emails. */
@Service
public class FirebaseIdentityService {

    private static final String IDENTITY_TOOLKIT = "https://identitytoolkit.googleapis.com/v1/accounts:";

    private final String webApiKey;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<FirebaseAuth> firebaseAuthProvider;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public FirebaseIdentityService(
            @Value("${FIREBASE_WEB_API_KEY:}") String webApiKey,
            ObjectMapper objectMapper,
            ObjectProvider<FirebaseAuth> firebaseAuthProvider) {
        this.webApiKey = webApiKey;
        this.objectMapper = objectMapper;
        this.firebaseAuthProvider = firebaseAuthProvider;
    }

    public RegisteredAccount createAccount(String email, String password) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("email", email);
        request.put("password", password);
        request.put("returnSecureToken", true);
        JsonNode response = post("signUp", request, false);
        return new RegisteredAccount(response.path("localId").asText(), response.path("idToken").asText());
    }

    public SignedInAccount signIn(String email, String password) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("email", email);
        request.put("password", password);
        request.put("returnSecureToken", true);
        JsonNode response = post("signInWithPassword", request, true);
        return new SignedInAccount(
                response.path("localId").asText(),
                response.path("email").asText(email),
                response.path("emailVerified").asBoolean(false),
                response.path("idToken").asText());
    }

    public void sendVerificationEmail(String idToken) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("requestType", "VERIFY_EMAIL");
        request.put("idToken", idToken);
        post("sendOobCode", request, false);
    }

    public String resendVerificationEmail(String email, String password) {
        SignedInAccount account = signIn(email, password);
        if (account.emailVerified()) return "This email is already verified. You can sign in.";

        sendVerificationEmail(account.idToken());
        return "A new verification email has been sent. Check your inbox and spam folder.";
    }

    public void deleteAccount(String uid) {
        try {
            firebaseAuthProvider.getObject().deleteUser(uid);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not roll back the Firebase Authentication account", ex);
        }
    }

    private JsonNode post(String action, ObjectNode payload, boolean invalidCredentialsAreExpected) {
        if (webApiKey.isBlank()) {
            throw new IllegalStateException("FIREBASE_WEB_API_KEY must be set for email verification");
        }

        try {
            String url = IDENTITY_TOOLKIT + action + "?key=" + URLEncoder.encode(webApiKey, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(response.body());
            if (response.statusCode() >= 200 && response.statusCode() < 300) return json;

            String code = json.path("error").path("message").asText("FIREBASE_AUTH_ERROR");
            if (code.contains("EMAIL_EXISTS")) throw new EmailAlreadyExistsException();
            if (invalidCredentialsAreExpected && (code.contains("INVALID_LOGIN_CREDENTIALS")
                    || code.contains("EMAIL_NOT_FOUND") || code.contains("INVALID_PASSWORD"))) {
                throw new BadCredentialsException("Invalid email or password");
            }
            if (code.contains("TOO_MANY_ATTEMPTS")) {
                throw new IllegalStateException("Too many attempts. Wait a while, then try again.");
            }
            if (code.contains("WEAK_PASSWORD")) {
                throw new IllegalArgumentException("Choose a stronger password with at least 6 characters.");
            }
            if (code.contains("INVALID_EMAIL")) {
                throw new IllegalArgumentException("Enter a valid email address.");
            }
            if (code.contains("API_KEY_INVALID") || code.contains("CONFIGURATION_NOT_FOUND")) {
                throw new IllegalStateException("Check the Firebase Web API key and enable Email/Password sign-in in Firebase.");
            }
            throw new IllegalStateException("Firebase Authentication request failed: " + code);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Firebase Authentication request was interrupted", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not contact Firebase Authentication", ex);
        }
    }

    public record RegisteredAccount(String uid, String idToken) { }
    public record SignedInAccount(String uid, String email, boolean emailVerified, String idToken) { }
}
