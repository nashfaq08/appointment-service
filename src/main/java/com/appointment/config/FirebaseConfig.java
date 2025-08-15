//package com.appointment.config;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.FirebaseOptions;
//import jakarta.annotation.PostConstruct;
//import org.springframework.context.annotation.Configuration;
//
//import java.io.InputStream;
//
//@Configuration
//public class FirebaseConfig {
//
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
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to initialize Firebase", e);
//        }
//    }
//}
//
