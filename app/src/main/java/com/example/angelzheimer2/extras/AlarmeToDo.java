package com.example.angelzheimer2.extras;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.example.angelzheimer2.R;
import com.example.angelzheimer2.paciente.NotificationActivity;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class AlarmeToDo extends BroadcastReceiver {
    String titulo, descricao, datahora;

    @Override
    public void onReceive(Context context, Intent intent) {
        titulo = intent.getStringExtra("titulo");
        descricao = intent.getStringExtra("descricao");
        datahora = intent.getStringExtra("datahora");

        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.angelzheimerround);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
//        Icon icon = Icon.createWithBitmap(bitmap);

        // Notificação simples
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "AngeLzheimer")
                .setSmallIcon(R.drawable.angelzheimerround_white)
                .setLargeIcon(bitmap)
                .setContentTitle(titulo)
                .setContentText(datahora)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(200, builder.build());

        // Alarme
        Intent i = new Intent(context, NotificationActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("titulo", titulo);
        i.putExtra("descricao", descricao);
        i.putExtra("datahora", datahora);
        context.startActivity(i);
    }
}
