package com.example.angelzheimer2.cuidador;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.angelzheimer2.R;
import com.example.angelzheimer2.extras.Firestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import static android.content.Context.MODE_PRIVATE;

public class fragment_InfoPaciente extends Fragment {
    Button btLogOut, btedit;
    TextView nome, morada, contacto, contactoEmergencia;
    Map<String, Object> User = new HashMap<>();
    String SHARED_PREFS = "sharedPrefs";


    public fragment_InfoPaciente() {
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
        View rootView = inflater.inflate(R.layout.fragment_info_paciente, container, false);
        nome = rootView.findViewById(R.id.nome);
        morada = rootView.findViewById(R.id.morada);
        contacto = rootView.findViewById(R.id.contacto);
        contactoEmergencia = rootView.findViewById(R.id.contactoEmergencia);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        // Recolha dos dados do Paciente (ID Homologo)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("/users")
                .document(sharedPreferences.getString("IDHomologo", null));
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        User.put("UID", document.get("UID"));
                        User.put("UserType", document.get("UserType"));
                        User.put("Nome", document.get("Nome"));
                        User.put("Email", document.get("Email"));
                        User.put("Morada", document.get("Morada"));
                        User.put("Contacto", document.get("Contacto"));
                        User.put("ContactoEmergencia", document.get("ContactoEmergencia"));
                        User.put("IDHomologo", document.get("IDHomologo"));
                        Log.d("FIRESTORE", "DocumentSnapshot data: " + document.getData());
                        // Mostrar as informações
                        nome.setText((String) User.get("Nome"));
                        morada.setText((String) User.get("Morada"));
                        contacto.setText((String) User.get("Contacto"));
                        contactoEmergencia.setText((String) User.get("ContactoEmergencia"));
                    } else {
                        Log.d("FIRESTORE", "No such document");
                    }
                } else {
                    Log.d("FIRESTORE", "get failed with ", task.getException());
                }

            }
        });
        btedit = rootView.findViewById(R.id.edit);
        btedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Update no Firestore
                String UID = (String) User.get("UID");
                User.put("Nome", nome.getText().toString());
                User.put("Morada", morada.getText().toString());
                User.put("Contacto", contacto.getText().toString());
                User.put("ContactoEmergencia", contactoEmergencia.getText().toString());

                Firestore.SaveUserData(User, UID);
                Toast.makeText(getContext(), "Informações Atualizadas com Sucesso", Toast.LENGTH_LONG).show();
//                Context context = getContext();
//                Intent intent = new Intent(context, ActivityMainMenu.class);
//                startActivity(intent);
            }
        });

        return rootView;
    }
}
