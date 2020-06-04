// AngeLzheimer 2
// Computação Móvel
// Sofia Dias & Tiago Rodrigues
// Engenharia Biomédica
// Universidade de Aveiro


package com.example.angelzheimer2;

import android.content.Intent;
import android.os.Bundle;

import com.example.angelzheimer2.comum.Activity_login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity implements fragment_logo.OnOptionClickListener {

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            // Inicia o Fragmento do Logo, enquanto aguarda a ligação à FireBase
            if (savedInstanceState == null) {
                fragmentManager.beginTransaction()
                        .add(R.id.container, new fragment_logo())
                        .commit();
            }
        }

    }

    @Override
    public void login() {
        Intent intent = new Intent(this, Activity_login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


}






