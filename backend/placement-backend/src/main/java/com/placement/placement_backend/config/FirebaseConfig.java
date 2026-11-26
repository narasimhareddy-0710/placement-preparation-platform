package com.placement.placement_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    private static final String APP_NAME = "placement-preparation-platform";

    @Bean
    @Lazy
    public Firestore firestore(
            @Value("${FIREBASE_PROJECT_ID:}") String projectId,
            @Value("${FIREBASE_SERVICE_ACCOUNT_JSON:}") String serviceAccountJson) throws IOException {
        if (projectId.isBlank()) {
            throw new IllegalStateException("FIREBASE_PROJECT_ID must be set");
        }

        GoogleCredentials credentials = serviceAccountJson.isBlank()
                ? GoogleCredentials.getApplicationDefault()
                : GoogleCredentials.fromStream(new ByteArrayInputStream(
                        serviceAccountJson.getBytes(StandardCharsets.UTF_8)));

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build();

        FirebaseApp app = FirebaseApp.getApps().stream()
                .filter(existing -> APP_NAME.equals(existing.getName()))
                .findFirst()
                .orElseGet(() -> FirebaseApp.initializeApp(options, APP_NAME));

        return FirestoreClient.getFirestore(app);
    }
}
