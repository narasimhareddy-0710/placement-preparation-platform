package com.placement.placement_backend.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.placement.placement_backend.entity.Student;
import com.placement.placement_backend.exception.EmailAlreadyExistsException;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/** Firestore access for student records. A hashed normalized email is the document key,
 * which gives registration an atomic uniqueness check without exposing email in the path. */
@Repository
public class StudentRepository {

    private static final String COLLECTION = "students";
    private final Firestore firestore;

    public StudentRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Optional<Student> findByEmail(String email) {
        DocumentSnapshot snapshot = getDocument(email);
        return snapshot.exists() ? Optional.ofNullable(snapshot.toObject(Student.class)) : Optional.empty();
    }

    public boolean existsByEmail(String email) {
        return getDocument(email).exists();
    }

    /** Reads a password hash from accounts created before Firebase Authentication was enabled. */
    public Optional<String> findLegacyPasswordHash(String email) {
        String hash = getDocument(email).getString("password");
        return Optional.ofNullable(hash).filter(value -> !value.isBlank());
    }

    /** Creates the document only if its key is unused, so concurrent duplicate registrations cannot overwrite it. */
    public Student create(Student student) {
        DocumentReference reference = documentFor(student.getEmail());
        try {
            reference.create(student).get();
            return student;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while saving student to Firestore", ex);
        } catch (ExecutionException ex) {
            if (referenceExists(reference)) throw new EmailAlreadyExistsException();
            throw new IllegalStateException("Could not save student to Firestore", ex.getCause());
        }
    }

    public Student save(Student student) {
        try {
            documentFor(student.getEmail()).set(student).get();
            return student;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while saving student to Firestore", ex);
        } catch (ExecutionException ex) {
            throw new IllegalStateException("Could not save student to Firestore", ex.getCause());
        }
    }

    public void deleteByEmail(String email) {
        try {
            documentFor(email).delete().get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while removing student from Firestore", ex);
        } catch (ExecutionException ex) {
            throw new IllegalStateException("Could not remove student from Firestore", ex.getCause());
        }
    }

    private DocumentSnapshot getDocument(String email) {
        try {
            return documentFor(email).get().get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while reading student from Firestore", ex);
        } catch (ExecutionException ex) {
            throw new IllegalStateException("Could not read student from Firestore", ex.getCause());
        }
    }

    private boolean referenceExists(DocumentReference reference) {
        try {
            return reference.get().get().exists();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while checking student in Firestore", ex);
        } catch (ExecutionException ex) {
            return false;
        }
    }

    private DocumentReference documentFor(String email) {
        return firestore.collection(COLLECTION).document(emailKey(email));
    }

    private String emailKey(String email) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(email.trim().toLowerCase(java.util.Locale.ROOT).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
