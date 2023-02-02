package com.kabc.desktop.database.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.kabc.desktop.unit.User;

public class FirebaseUsers extends FirebaseDB {

    private static final DatabaseReference REFERENCE = DATABASE.getReference("users");

    public static void checkLogin(String login, final Callback callback) {
        REFERENCE.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {}
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                callback.onCallback(dataSnapshot.child(login).exists());
            }
        });
    }

    public static void add(User user) {
        DatabaseReference userReference = REFERENCE.child(user.getLogin());
        userReference.child("birth_date").setValueAsync(user.getBirthDate());
        userReference.child("doctor_id").setValueAsync(user.getDoctorId());
    }

    public static void edit(User oldUser, User newUser) {
        if (newUser.equals(oldUser)) {
            return;
        }
        if (!newUser.getLogin().equals(oldUser.getLogin())) {
            delete(oldUser);
        }
        DatabaseReference userReference = REFERENCE.child(newUser.getLogin());
        userReference.child("birth_date").setValueAsync(newUser.getBirthDate());
        userReference.child("doctor_id").setValueAsync(newUser.getDoctorId());
    }

    public static void delete(User user) {
        REFERENCE.child(user.getLogin()).removeValueAsync();
    }

    public interface Callback {
        void onCallback(boolean value);
    }
}