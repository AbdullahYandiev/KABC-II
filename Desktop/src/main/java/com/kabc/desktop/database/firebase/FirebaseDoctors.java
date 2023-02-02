package com.kabc.desktop.database.firebase;

import com.google.firebase.database.*;

public class FirebaseDoctors extends FirebaseDB {

    private static final DatabaseReference REFERENCE = DATABASE.getReference("doctors");

    public static void getNewId(final Callback callback) {
        REFERENCE.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer doctorId = mutableData.getValue(Integer.class);
                if (doctorId == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(doctorId + 1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                callback.onCallback(dataSnapshot.getValue(Integer.class));
            }
        });
    }

    public interface Callback {
        void onCallback(int value);
    }
}