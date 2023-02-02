package com.kabc.desktop.database.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import java.io.FileInputStream;
import java.io.IOException;

public abstract class FirebaseDB {

    protected static final FirebaseDatabase DATABASE = initializeFirebase();

    private static FirebaseDatabase initializeFirebase() {
        try {
            FileInputStream serviceAccount = new FileInputStream("kabc_firebase.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://kabc-app-default-rtdb.firebaseio.com/")
                    .build();
            FirebaseApp.initializeApp(options);
            return FirebaseDatabase.getInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}