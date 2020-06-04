package com.example.angelzheimer2.paciente;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.angelzheimer2.R;
import com.example.angelzheimer2.extras.Tasks;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class EditNotesActivity extends AppCompatActivity {
    String titulo;
    String descricao;
    String data;
    String hora;
    Boolean enabled;
    EditText et_titulo;
    EditText et_descricao;
    TextView tv_data;
    TextView tv_hora;
    DatePickerDialog datepicker;
    TimePickerDialog timepicker;
    CardView cv_save;
    CardView cv_delete;

    String SHARED_PREF = "sharedPrefs";
    SharedPreferences sharedPreferences;

    String ID, IDtask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_notes);
        et_titulo = findViewById(R.id.et_titulo);
        et_descricao = findViewById(R.id.et_descricao);
        tv_data = findViewById(R.id.tv_data);
        tv_hora = findViewById(R.id.tv_hora);
        cv_save = findViewById(R.id.cv_save);
        cv_delete = findViewById(R.id.cv_delete);

        sharedPreferences = this.getSharedPreferences(SHARED_PREF, MODE_PRIVATE);

        if (getIntent().hasExtra("IDtask")) {
            IDtask = getIntent().getStringExtra("IDtask");
        }
//        Toast.makeText(EditNotesActivity.this, ID, Toast.LENGTH_LONG).show();

        // Leitura da Firestore
        String userType = sharedPreferences.getString("UserType", null);

        if (userType != null) {
            if (userType.equals("Paciente")) {
                ID = sharedPreferences.getString("IDHomologo", null);
            } else if (userType.equals("Cuidador")) {
                ID = sharedPreferences.getString("UID", null);
            }

        }

        if (ID != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentReference = db.collection("/ToDo")
                    .document(ID)
                    .collection("/ToDo")
                    .document(IDtask);
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            mostrarinfos(document);
                            Log.d("TODO", "DocumentSnapshot data: " + document.getData());
                        } else {
                            Log.d("TODO", "No such document");
                        }
                    } else {
                        Log.d("TODO", "get failed with ", task.getException());
                    }
                }
            });
        }


        tv_hora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                tv_hora.setText(picker.getHo.getDayOfMonth()+"/"+ (picker.getMonth() + 1)+"/"+picker.getYear());
                final Calendar cldr = Calendar.getInstance();
                int hour = cldr.get(Calendar.HOUR_OF_DAY);
                int minutes = cldr.get(Calendar.MINUTE);
                // time picker dialog
                timepicker = new TimePickerDialog(EditNotesActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
                                tv_hora.setText(sHour + ":" + sMinute);
                            }
                        }, hour, minutes, true);
                timepicker.show();
            }
        });

        cv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                titulo = et_titulo.getText().toString();
                descricao = et_descricao.getText().toString();
                hora = tv_hora.getText().toString();
                data = tv_data.getText().toString();

//                Validações
                if (titulo.isEmpty()) {
                    et_titulo.setError("Sem Título");
                    et_titulo.requestFocus();
                    return;
                }
                if (hora.isEmpty()) {
                    tv_hora.setError("É necessária uma hora");
                    tv_hora.requestFocus();
                    return;
                }
                if (data.isEmpty()) {
                    tv_data.setError("É necessária uma data");
                    tv_data.requestFocus();
                    return;
                }

//                Colocação na Firebase
                Tasks tarefa = new Tasks();
                tarefa.setTitulo(titulo);
                tarefa.setDescricao(descricao);
                tarefa.setData(data);
                tarefa.setHora(hora);
                tarefa.setEnabled(false);

                if (ID != null) {
                    FirebaseFirestore.getInstance().collection("/ToDo")
                            .document(ID)
                            .collection("/ToDo")
                            .document(IDtask)
                            .set(tarefa)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("TODO", IDtask);
                                    Toast.makeText(EditNotesActivity.this, getString(R.string.tarefaadicionada), Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("TODO", "Algo não correu bem: ", e);
                                    Toast.makeText(EditNotesActivity.this, getString(R.string.algocorreumau), Toast.LENGTH_LONG).show();

                                }
                            });
                }

            }
        });

        cv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ID != null) {
                    FirebaseFirestore.getInstance().collection("/ToDo")
                            .document(ID)
                            .collection("/ToDo")
                            .document(IDtask)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("TODO", IDtask);
                                    Toast.makeText(EditNotesActivity.this, getString(R.string.tarefaapagada), Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("TODO", "Algo não correu bem: ", e);
                                    Toast.makeText(EditNotesActivity.this, getString(R.string.algocorreumau), Toast.LENGTH_LONG).show();
                                }
                            });
                }

            }
        });


    }

    private void mostrarinfos(DocumentSnapshot document) {
        et_titulo.setText(document.getString("titulo"));
        et_descricao.setText(document.getString("descricao"));
        tv_data.setText(document.getString("data"));
        tv_hora.setText(document.getString("hora"));
//        Toast.makeText(EditNotesActivity.this, document.getString("titulo"), Toast.LENGTH_LONG).show();

    }
}
