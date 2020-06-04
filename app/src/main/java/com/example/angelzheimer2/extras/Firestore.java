package com.example.angelzheimer2.extras;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import androidx.annotation.NonNull;

import static android.content.Context.MODE_PRIVATE;

public class Firestore {

    public static void SaveUserData(Map<String, Object> user, String userID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        final DocumentReference documentReference = db.collection("users")
                .document(userID);
        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("FIRESTORE", "DocumentSnapshot added with ID: " + documentReference.getId());
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("FIRESTORE", "Error adding document", e);
                    }
                });

    }

    public synchronized static void ReadUserData(String userID, final Context context) {
        // SharedPreferences
        final String SHARED_PREFS = "sharedPrefs";

        //Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("users")
                .document(userID);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        shared(document); // Guardar internamente com SharedPreferences
                        Log.d("FIRESTORE", "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d("FIRESTORE", "No such document");
                    }
                } else {
                    Log.d("FIRESTORE", "get failed with ", task.getException());
                }
            }

            private void shared(DocumentSnapshot document) {
                SharedPreferences sharedPrefereces = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefereces.edit();
                editor.putString("UID", document.getString("UID"))
                        .putString("UserType", document.getString("UserType"))
                        .putString("Email", document.getString("Email"))
                        .putString("Nome", document.getString("Nome"))
                        .putString("Morada", document.getString("Morada"))
                        .putString("Contacto", document.getString("Contacto"))
                        .putString("ContactoEmergencia", document.getString("ContactoEmergencia"))
                        .putString("IDHomologo", document.getString("IDHomologo"));
                editor.apply();
                Log.w("FIRESTORE", editor.getClass().toString() + " || " + document.getString("Nome"));
            }
        });
    }

}
