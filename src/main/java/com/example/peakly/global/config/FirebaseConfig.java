package com.example.peakly.global.config;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials.path}")
    private Resource serviceAccount;

    /**
     * Initializes the FirebaseApp using the configured service account credentials.
     *
     * This method loads credentials from the injected `serviceAccount` resource and initializes
     * Firebase only if no FirebaseApp instances are already registered to avoid duplicate initialization.
     *
     * @throws IOException if the service account resource cannot be read
     */
    @PostConstruct
    public void init() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) return; // 중복 초기화 방지

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount.getInputStream()))
                .build();

        FirebaseApp.initializeApp(options);
    }
}