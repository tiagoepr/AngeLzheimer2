package com.example.angelzheimer2.comum;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.angelzheimer2.MainActivity;
import com.example.angelzheimer2.R;
import com.example.angelzheimer2.extras.Firestore;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

import androidx.fragment.app.Fragment;

import static android.content.Context.MODE_PRIVATE;

public class fragment_UserInfo extends Fragment {
    Button btLogOut, btedit;
    TextView nome, morada, contacto, contactoEmergencia;
    Map<String, Object> User = new HashMap<>();
    String SHARED_PREFS = "sharedPrefs";


    public fragment_UserInfo() {
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
        View rootView = inflater.inflate(R.layout.fragment_user_info, container, false);
        nome = rootView.findViewById(R.id.nome);
        morada = rootView.findViewById(R.id.morada);
        contacto = rootView.findViewById(R.id.contacto);
        contactoEmergencia = rootView.findViewById(R.id.contactoEmergencia);

        // Ler as infos do shared preferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        User.put("UID", sharedPreferences.getString("UID", null));
        User.put("UserType", sharedPreferences.getString("UserType", null));
        User.put("Nome", sharedPreferences.getString("Nome", null));
        User.put("Email", sharedPreferences.getString("Email", null));
        User.put("Morada", sharedPreferences.getString("Morada", null));
        User.put("Contacto", sharedPreferences.getString("Contacto", null));
        User.put("ContactoEmergencia", sharedPreferences.getString("ContactoEmergencia", null));
        User.put("IDHomologo", sharedPreferences.getString("IDHomologo", null));
        Log.w("FIRESTORE", User.toString());

        // Mostrar as informações
        nome.setText((String) User.get("Nome"));
        morada.setText((String) User.get("Morada"));
        contacto.setText((String) User.get("Contacto"));
        contactoEmergencia.setText((String) User.get("ContactoEmergencia"));

        btLogOut = rootView.findViewById(R.id.bt_LogOut);
        btLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Context context = getContext();
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
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
                //Update no SharedPreferences
                SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("UID", (String) User.get("UID"))
                        .putString("UserType", (String) User.get("UserType"))
                        .putString("Nome", nome.getText().toString())
                        .putString("Morada", morada.getText().toString())
                        .putString("Contacto", contacto.getText().toString())
                        .putString("ContactoEmergencia", contactoEmergencia.getText().toString())
                        .putString("IDHomologo", (String) User.get("IDHomologo"));
                editor.apply();
                Toast.makeText(getContext(), "Informações Atualizadas com Sucesso", Toast.LENGTH_LONG).show();
//                Context context = getContext();
//                Intent intent = new Intent(context, ActivityMainMenu.class);
//                startActivity(intent);
            }
        });

        return rootView;
    }
}
