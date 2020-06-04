package com.example.angelzheimer2.paciente;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.angelzheimer2.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

public class NotificationActivity extends AppCompatActivity {

    private TextView tv_activity, tv_close, tv_data, tv_hora;
    private EditText et_descricao;
    private String message, titulo, descricao, datahora, data, hora;
    private Ringtone ringtone;
    private Vibrator vibrator;
    private SharedPreferences preferences;
    private int selectedTone;
    private boolean vibrated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        preferences = getSharedPreferences("settings", MODE_PRIVATE);
        selectedTone = preferences.getInt("tone", 0);
        vibrated = preferences.getBoolean("vibrate", true);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        tv_activity = findViewById(R.id.tv_activity_notif);
        tv_close = findViewById(R.id.tv_close);
        tv_data = findViewById(R.id.tv_data);
        tv_hora = findViewById(R.id.tv_hora);
        et_descricao = findViewById(R.id.et_descricao);

        if (getIntent().hasExtra("titulo")) {
            titulo = getIntent().getStringExtra("titulo");
            datahora = getIntent().getStringExtra("datahora");
            descricao = getIntent().getStringExtra("descricao");
        }

        if (titulo != null) {
            message = "Tarefa: " + titulo + "!";
        }
        tv_activity.setText(message);
//        Log.d("NOTIFICACAO", datahora);
        List<String> listdatahora = Arrays.asList(datahora.split(" - "));
//        Log.d("NOTIFICACAO", hora);
        tv_data.setText(listdatahora.get(0));
        tv_hora.setText(listdatahora.get(1));
        et_descricao.setText(descricao);

        List<String> tones = new ArrayList<>(getRingtones().keySet());


        Uri toneUri = Uri.parse(getRingtones().get(tones.get(selectedTone)));

        ringtone = RingtoneManager.getRingtone(getApplicationContext(), toneUri);
        ringtone.play();

        long[] pattern = new long[]{0, 400, 800, 600, 800, 800, 800, 1000};

        if (vibrated) {
            vibrator.vibrate(pattern, 0);
        }

        tv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        ringtone.stop();
        vibrator.cancel();
    }

    private Map<String, String> getRingtones() {
        RingtoneManager manager = new RingtoneManager(this);
        manager.setType(RingtoneManager.TYPE_RINGTONE);
        Cursor cursor = manager.getCursor();

        Map<String, String> ringtones = new HashMap<>();
        while (cursor.moveToNext()) {
            String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + cursor.getString(RingtoneManager.ID_COLUMN_INDEX);

            ringtones.put(title, uri);
        }
        return ringtones;
    }

}
