package com.example.angelzheimer2.comum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.angelzheimer2.MainActivity;
import com.example.angelzheimer2.R;
import com.example.angelzheimer2.extras.Firestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class fragment_register extends Fragment {
    final String TAG = "MyActivity";
    // https://firebase.google.com/docs/auth/android/start/
    EditText utilizadorCuidador, passwordCuidador, utilizadorPaciente, passwordPaciente;
    Button RegisterBtn;
    ProgressBar ProgressBar;
    String userIDP;
    String userIDC;

    private FirebaseAuth mAuth;

    public fragment_register() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_register, null);
        mAuth = FirebaseAuth.getInstance();

        utilizadorCuidador = rootView.findViewById(R.id.et_utilizadorcuidador);
        passwordCuidador = rootView.findViewById(R.id.et_passwordcuidador);
        utilizadorPaciente = rootView.findViewById(R.id.et_utilizadorpaciente);
        passwordPaciente = rootView.findViewById(R.id.et_passwordpaciente);
        ProgressBar = rootView.findViewById((R.id.progressBar));

        //Botão de Registo
        RegisterBtn = rootView.findViewById(R.id.btn_register);
        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressBar.setVisibility(View.VISIBLE);
                register(rootView);
            }
        });
        return rootView;
    }

    void register(View rootView) {
        Context context = getContext();

        //Paciente
        final String emailP = utilizadorPaciente.getText().toString();
        String passP = passwordPaciente.getText().toString();

        //Validação
        if (emailP.equals(getString(R.string.utilizadorpaciente)) || emailP.isEmpty()) {
            utilizadorPaciente.setError("Email is Required");
            utilizadorPaciente.requestFocus();
            ProgressBar.setVisibility(View.GONE);
            return;
        }
        if (passP.equals(getString(R.string.passwordpaciente)) || passP.isEmpty()) {
            passwordPaciente.setError("Password is Required");
            passwordPaciente.requestFocus();
            ProgressBar.setVisibility(View.GONE);
            return;
        }
        if (passP.length() < 6) {
            passwordPaciente.setError("Password need at least 6 digits");
            passwordPaciente.requestFocus();
            ProgressBar.setVisibility(View.GONE);
            return;
        }


        mAuth.createUserWithEmailAndPassword(emailP, passP).addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail Paciente:success");
                    userIDP = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                    FirebaseAuth.getInstance().signOut();

                } else {
                    // If sign in fails, display a message to the user.
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getContext(), "Email já em utilização", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(getContext(), "Algo Correu Mal! Tente Novamente", Toast.LENGTH_LONG).show();
                    }
                    return;
                }

            }
        });


        //Cuidador:
        final String emailC = utilizadorCuidador.getText().toString();
        final String passC = passwordCuidador.getText().toString();
        //Validação
        if (emailC.equals(getString(R.string.utilizadorcuidador)) || emailC.isEmpty()) {
            utilizadorCuidador.setError("Email is Required");
            utilizadorCuidador.requestFocus();
            ProgressBar.setVisibility(View.GONE);
            return;
        }

        if (passC.equals(getString(R.string.passwordcuidador)) || passC.isEmpty()) {
            passwordCuidador.setError("Password is Required");
            passwordCuidador.requestFocus();
            ProgressBar.setVisibility(View.GONE);
            return;
        }
        if (passC.length() < 6) {
            passwordCuidador.setError("Password need at least 6 digits");
            passwordCuidador.requestFocus();
            ProgressBar.setVisibility(View.GONE);
            return;
        }
        mAuth.createUserWithEmailAndPassword(emailC, passC).addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail Cuidador:success");
                    Toast.makeText(getContext(), "Utilizadores Registados", Toast.LENGTH_SHORT).show();
                    userIDC = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                    userData(userIDP, emailP, userIDC, emailC);
                    Context context = getContext();
                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                } else {
                    // If sign in fails, display a message to the user.
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getContext(), "Email já em utilização", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(getContext(), "Algo Correu Mal! Tente Novamente", Toast.LENGTH_LONG).show();
                    }
                    return;
                }
            }
        });

        ProgressBar.setVisibility(View.GONE);
    }

    void userData(String userIDP, String EmailP, String userIDC, String EmailC) {


        // <<<< Guardar os dados do Paciente na FireStore: >>>>
        Map<String, Object> Paciente = new HashMap<>();
        Paciente.put("UID", userIDP);
        Paciente.put("UserType", "Paciente");
        Paciente.put("Nome", getString(R.string.utilizador));
        Paciente.put("Email", EmailP);
        Paciente.put("Morada", getString(R.string.Address));
        Paciente.put("Contacto", getString(R.string.Contact));
        Paciente.put("ContactoEmergencia", getString(R.string.Contact));
        Paciente.put("IDHomologo", userIDC);
        Firestore.SaveUserData(Paciente, userIDP);

        Map<String, Object> Cuidador = new HashMap<>();
        Cuidador.put("UID", userIDC);
        Cuidador.put("UserType", "Cuidador");
        Cuidador.put("Nome", getString(R.string.utilizador));
        Cuidador.put("Email", EmailC);
        Cuidador.put("Morada", getString(R.string.Address));
        Cuidador.put("Contacto", getString(R.string.Contact));
        Cuidador.put("ContactoEmergencia", getString(R.string.Contact));
        Cuidador.put("IDHomologo", userIDP);
        Firestore.SaveUserData(Cuidador, userIDC);
        Firestore.ReadUserData(userIDC, getContext());


    }


}
