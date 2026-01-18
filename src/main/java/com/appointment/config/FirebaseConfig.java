package com.appointment.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

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
            String jsonPath = "/home/apps/deployments/lawyers-appointment-firebase.json";
            FileInputStream serviceAccount = new FileInputStream(jsonPath);

            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            ServiceAccountCredentials sac = (ServiceAccountCredentials) credentials;
            log.info("Firebase SA email = {}", sac.getClientEmail());
            log.info("Firebase key id = {}", sac.getPrivateKeyId());

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }
}