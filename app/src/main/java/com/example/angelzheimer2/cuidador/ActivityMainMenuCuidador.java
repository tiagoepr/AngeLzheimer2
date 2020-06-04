package com.example.angelzheimer2.cuidador;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.example.angelzheimer2.R;
import com.example.angelzheimer2.comum.fragment_Message;
import com.example.angelzheimer2.comum.fragment_UserInfo;
import com.example.angelzheimer2.paciente.fragment_ToDo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

public class ActivityMainMenuCuidador extends AppCompatActivity implements fragment_MainMenuCuidador.OnOptionClickListener {
    public static final String EXTRA_SETTING_OPTION = "option";
    FragmentManager fragmentManager;
    String SHARED_PREFS = "sharedPrefs";
    String number;
    private boolean isTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_cuidador);
        fragmentManager = getSupportFragmentManager();

        isTwoPane = findViewById(R.id.FunctionContainer) != null;

        if (savedInstanceState == null) {
            // Inicia o Fragmento do Menu Principal
            if (savedInstanceState == null) {
                fragmentManager.beginTransaction()
                        .add(R.id.container, new fragment_MainMenuCuidador())
                        .commit();
            }
        }
    }

    // Seleção de opções
    @Override
    public void onOptionSelected(String option) {
        // Modo Tablet
        if (isTwoPane) {
            switch (option) {
                case "ToDo": {
                    fragmentManager.beginTransaction()
                            .replace(R.id.FunctionContainer, new fragment_ToDo())
                            .commit();
                    break;
                }
                case "InfoPaciente": {
                    fragmentManager.beginTransaction()
                            .replace(R.id.FunctionContainer, new fragment_InfoPaciente())
                            .commit();
                    Log.d("INFOPACIENTE", "Entrou no Info Paciente");
                    break;
                }
                case "Mens": {
                    fragmentManager.beginTransaction()
                            .replace(R.id.FunctionContainer, new fragment_Message())
                            .commit();
                    break;
                }
                case "Local": {
                    fragmentManager.beginTransaction()
                            .replace(R.id.FunctionContainer, new fragment_LocalPaciente())
                            .commit();
                    break;
                }
                case "Info": {
                    fragmentManager.beginTransaction()
                            .replace(R.id.FunctionContainer, new fragment_UserInfo())
                            .commit();
                    break;
                }
                case "Call": {
                    SharedPreferences sharedPreferences = this.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference documentReference = db.collection("/users")
                            .document(sharedPreferences.getString("IDHomologo", null));
                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    number = (String) document.get("Contacto");
                                    Log.d("number", number);
                                    chamar(number);
                                }
                            }
                        }

                    });
                    break;
                }

            }

        } else {
            Intent intent = new Intent(this, ActivityFunctionCuidador.class);
            intent.putExtra(ActivityFunctionCuidador.EXTRA_SETTING_OPTION, option);
            startActivity(intent);
        }
    }

    private void chamar(String number) {
        if (number != null) {
            Intent dialIntent = new Intent(Intent.ACTION_CALL);
            String dial = "tel:" + number;
            dialIntent.setData(Uri.parse(dial));
            if (dialIntent.resolveActivity(getPackageManager()) != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            1);
                    return;
                } else {
                    Log.d("CHAMADA", dial);
                    startActivity(dialIntent);
                }
            }
        }
    }


}
