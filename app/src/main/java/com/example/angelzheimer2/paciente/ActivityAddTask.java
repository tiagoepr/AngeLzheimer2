package com.example.angelzheimer2.paciente;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.angelzheimer2.R;
import com.example.angelzheimer2.extras.AlarmeToDo;
import com.example.angelzheimer2.extras.Tasks;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ActivityAddTask extends AppCompatActivity {

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
    String SHARED_PREF = "sharedPrefs";
    SharedPreferences sharedPreferences;
    String ID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        et_titulo = findViewById(R.id.et_titulo);
        et_descricao = findViewById(R.id.et_descricao);
        tv_data = findViewById(R.id.tv_data);
        tv_hora = findViewById(R.id.tv_hora);
        cv_save = findViewById(R.id.cv_save);

        sharedPreferences = this.getSharedPreferences(SHARED_PREF, MODE_PRIVATE);


        tv_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                tv_data.setText(picker.getDayOfMonth()+"/"+ (picker.getMonth() + 1)+"/"+picker.getYear());
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                datepicker = new DatePickerDialog(ActivityAddTask.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                tv_data.setText(year + "/" + (monthOfYear + 1) + "/" + dayOfMonth);
                            }
                        }, year, month, day);
                datepicker.show();
            }
        });

        tv_hora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                tv_hora.setText(picker.getHo.getDayOfMonth()+"/"+ (picker.getMonth() + 1)+"/"+picker.getYear());
                final Calendar cldr = Calendar.getInstance();
                int hour = cldr.get(Calendar.HOUR_OF_DAY);
                int minutes = cldr.get(Calendar.MINUTE);
                // time picker dialog
                timepicker = new TimePickerDialog(ActivityAddTask.this,
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
                String userType = sharedPreferences.getString("UserType", null);

                if (userType != null) {
                    if (userType.equals("Paciente")) {
                        ID = sharedPreferences.getString("IDHomologo", null);
                    } else if (userType.equals("Cuidador")) {
                        ID = sharedPreferences.getString("UID", null);
                    }

                }

                if (ID != null) {
                    FirebaseFirestore.getInstance().collection("/ToDo")
                            .document(ID)
                            .collection("/ToDo")
                            .add(tarefa)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d("TODO", documentReference.getId());
                                    Toast.makeText(ActivityAddTask.this, getString(R.string.tarefaadicionada), Toast.LENGTH_LONG).show();

                                    // Agendar a Notificação apenas no Paciente
                                    notificationChanel();
                                    Intent intent = new Intent(ActivityAddTask.this, AlarmeToDo.class);
                                    intent.putExtra("titulo", titulo);
                                    String datahora = data + " - " + hora;
                                    Log.d("NOTIFICACAO", datahora);
                                    intent.putExtra("datahora", datahora);
                                    intent.putExtra("descricao", descricao);
                                    PendingIntent pendingIntent = PendingIntent
                                            .getBroadcast(ActivityAddTask.this, 0, intent, 0);

                                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                                    Calendar calendar = Calendar.getInstance();
                                    List HoraMinuto = Arrays.asList(hora.split(":"));
                                    int Hora = Integer.parseInt((String) HoraMinuto.get(0));
                                    int Minuto = Integer.parseInt((String) HoraMinuto.get(1));
                                    List Data = Arrays.asList(data.split("/"));
                                    int Ano = Integer.parseInt((String) Data.get(0));
                                    int Mes = Integer.parseInt((String) Data.get(1)) - 1;
                                    int Dia = Integer.parseInt((String) Data.get(2));
                                    calendar.set(Calendar.DAY_OF_MONTH, Dia);
                                    calendar.set(Calendar.MONTH, Mes);
                                    calendar.set(Calendar.YEAR, Ano);
                                    calendar.set(Calendar.HOUR_OF_DAY, Hora);
                                    calendar.set(Calendar.MINUTE, Minuto);
                                    calendar.set(Calendar.SECOND, 0);
                                    long millis = calendar.getTimeInMillis() - 1000 * 5;

                                    if (millis > 0) {
                                        Log.d("NOTIFICACAO", String.valueOf(millis));


                                        alarmManager.set(AlarmManager.RTC_WAKEUP,
                                                millis,
                                                pendingIntent);
                                        finish();
                                    } else {
                                        Toast.makeText(ActivityAddTask.this, "Data e Hora Passadas", Toast.LENGTH_LONG).show();
                                    }

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("TODO", "Algo não correu bem: ", e);
                                    Toast.makeText(ActivityAddTask.this, getString(R.string.algocorreumau), Toast.LENGTH_LONG).show();

                                }
                            });

                }


            }
        });

    }

    private void notificationChanel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence title = titulo;
            String description = descricao;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("AngeLzheimer", title, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}
