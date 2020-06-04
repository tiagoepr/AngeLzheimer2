package com.example.angelzheimer2.comum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.angelzheimer2.R;
import com.example.angelzheimer2.cuidador.ActivityMainMenuCuidador;
import com.example.angelzheimer2.extras.Firestore;
import com.example.angelzheimer2.paciente.ActivityMainMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import static android.content.Context.MODE_PRIVATE;

public class fragment_login extends Fragment {
    final String TAG = "MyActivity";
    Button loginBtn;
    EditText etEmail;
    EditText etPass;
    String email;
    String pass;
    ProgressBar ProgressBar;
    String UserType;
    String SHARED_PREFS = "sharedPrefs";
    Context context;
    SharedPreferences sharedPreferences;
    FirebaseUser user;
    private FirebaseAuth mAuth;


    public fragment_login() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        loginBtn = rootView.findViewById(R.id.btn_login);
        etEmail = rootView.findViewById(R.id.et_email);
        etPass = rootView.findViewById(R.id.et_passwordpaciente);

        mAuth = FirebaseAuth.getInstance();
        ProgressBar = rootView.findViewById((R.id.progressBar));

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Login do User
                // https://firebase.google.com/docs/auth/android/start/
                ProgressBar.setVisibility(View.VISIBLE);
                clickLogin(rootView);

            }
        });
        return rootView;
    }

    void clickLogin(View rootView) {
        email = etEmail.getText().toString().trim();
        pass = etPass.getText().toString().trim();
        if (email.equals(getString(R.string.utilizador)) || email.isEmpty()) {
            etEmail.setError("Email is Required");
            etEmail.requestFocus();
            ProgressBar.setVisibility(View.GONE);
            return;
        }

        if (pass.equals(getString(R.string.passwordcuidador))) {
            etPass.setError("Password is Required");
            etPass.requestFocus();
            ProgressBar.setVisibility(View.GONE);
            return;
        }
        if (pass.length() < 6) {
            etPass.setError("Password need at least 6 digits");
            etPass.requestFocus();
            ProgressBar.setVisibility(View.GONE);
            return;
        }

        context = getContext();
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success");
                    user = mAuth.getCurrentUser();
                    String email = user.getEmail();
                    String userID = mAuth.getCurrentUser().getUid();
                    Firestore.ReadUserData(userID, context);

                    Toast.makeText(context, "Login com:" + email, Toast.LENGTH_LONG).show();

                    ProgressBar.setVisibility(View.VISIBLE);
                    final Handler handler = new Handler();
                    // Post Delayed para que tenha tempo de ler do Shared Preferences
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                            UserType = sharedPreferences.getString("UserType", null);
                            if (UserType == null) {
                                FirebaseFirestore.getInstance().collection("/users")
                                        .document(user.getUid())
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                UserType = documentSnapshot.getString("UserType");
                                            }
                                        });
                            }

                            if (UserType.equals("Cuidador")) {
                                Intent intent = new Intent(context, ActivityMainMenuCuidador.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(context, ActivityMainMenu.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    }, 2000);

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    Toast.makeText(getContext(), "Combinação User+Pass Errada", Toast.LENGTH_SHORT).show();
                    etEmail.setError("Combinação User+Pass Errada");
                    etEmail.requestFocus();
                }
                ProgressBar.setVisibility(View.GONE);
            }
        });
        ProgressBar.setVisibility(View.GONE);
    }


}
