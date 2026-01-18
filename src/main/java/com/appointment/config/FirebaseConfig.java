package com.appointment.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Base64;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.config.base64}")
    private String firebaseBase64;

//    @PostConstruct
//    public void initialize() {
//        try {
//            InputStream serviceAccount = getClass()
//                    .getClassLoader()
//                    .getResourceAsStream("lawyers-appointment-firebase.json");
//
//            FirebaseOptions options = new FirebaseOptions.Builder()
//                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                    .build();
//
//            if (FirebaseApp.getApps().isEmpty()) {
//                FirebaseApp.initializeApp(options);
//            }
//
//            ServiceAccountCredentials sac =
//                    (ServiceAccountCredentials) credentials;
//
//            log.info("Firebase SA email = {}", sac.getClientEmail());
//            log.info("Firebase key id = {}", sac.getPrivateKeyId());
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to initialize Firebase", e);
//        }
//    }

    @PostConstruct
    public void initialize() {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(firebaseBase64);
            InputStream serviceAccount = new ByteArrayInputStream(decodedBytes);

            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized from base64-encoded service account.");
            }

            ServiceAccountCredentials sac = (ServiceAccountCredentials) credentials;
            log.info("Firebase SA email = {}", sac.getClientEmail());
            log.info("Firebase key id = {}", sac.getPrivateKeyId());

        } catch (Exception e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }
}