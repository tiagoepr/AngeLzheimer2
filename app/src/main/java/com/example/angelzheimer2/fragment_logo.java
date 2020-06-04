package com.example.angelzheimer2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.angelzheimer2.cuidador.ActivityMainMenuCuidador;
import com.example.angelzheimer2.extras.Firestore;
import com.example.angelzheimer2.paciente.ActivityMainMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.content.Context.MODE_PRIVATE;


public class fragment_logo extends Fragment {

    String UserType;
    String SHARED_PREFS = "sharedPrefs";
    FirebaseUser user;
    //public GIFView syncGif;
    private FirebaseAuth mAuth;
    private OnOptionClickListener mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnOptionClickListener) context;
        } catch (Exception e) {
            throw new ClassCastException(context.toString() + " must implement login()");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_logo, container, false);
//        LinearLayout Imb = rootView.findViewById(R.id.bt1);

        int WaitingTime = 2000; // ms
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Context context = getContext();
            Firestore.ReadUserData(user.getUid(), context);
        }

//        Para aguardar a ligação ao Firebase e SharedPreferences
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (user != null) {
                    // User is signed in
                    Context context = getContext();
//                    String userID = mAuth.getCurrentUser().getUid();
//                    Firestore.ReadUserData(userID, context);
//                    FirebaseFirestore.getInstance().collection("/users")
//                            .document(user.getUid())
//                            .get()
//                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                @Override
//                                public void onSuccess(DocumentSnapshot documentSnapshot) {
//                                    UserType=documentSnapshot.getString("UserType");
//                                }
//                            });
                    SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                    UserType = sharedPreferences.getString("UserType", "");

                    if (UserType.equals("Cuidador")) {
                        Intent intent = new Intent(context, ActivityMainMenuCuidador.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, ActivityMainMenu.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                } else {
                    // No user is signed in
                    mCallback.login();

                }
            }
        }, WaitingTime);

//        Imb.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        return rootView;
    }

    interface OnOptionClickListener {
        void login();
    }


}


