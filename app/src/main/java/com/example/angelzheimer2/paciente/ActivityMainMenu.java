package com.example.angelzheimer2.paciente;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import com.example.angelzheimer2.R;
import com.example.angelzheimer2.comum.fragment_Message;
import com.example.angelzheimer2.comum.fragment_UserInfo;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import static java.lang.Double.isFinite;
import static java.lang.Math.sqrt;

public class ActivityMainMenu extends AppCompatActivity
        implements fragment_MainMenu.OnOptionClickListener,
        LocationListener,
        SensorEventListener {


    public static final String EXTRA_SETTING_OPTION = "option";
    // Lançar os Processos em Background
    public static boolean isRecursionEnable = true;
    final float lowThreshold = (float) 7.5;
    final float highThreshold = (float) 15.6;
    LocationManager locationManager;
    LatLng Newlocation;
    FragmentManager fragmentManager;
    String SHARED_PREFS = "sharedPrefs";
    String number;
    boolean fallDetect = false;
    boolean cancel = false;
    List a = new ArrayList();
    int n;
    private boolean isTwoPane;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        fragmentManager = getSupportFragmentManager();
        runInBackground();
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);


        isTwoPane = findViewById(R.id.FunctionContainer) != null;

        if (savedInstanceState == null) {
            // Inicia o Fragmento do Menu Principal
            if (savedInstanceState == null) {
                fragmentManager.beginTransaction()
                        .add(R.id.container, new fragment_MainMenu())
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
                case "Memo": {
//                fragmentManager.beginTransaction()
//                        .replace(R.id.container, new fragment_Memo())
//                        .commit();
//                     o objeto FirebaseRecyclerAdapter não funciona bem em fragmentos
                    Intent intent = new Intent(this, ActivityMemo.class);
                    startActivity(intent);
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
                            .replace(R.id.FunctionContainer, new fragment_Location())
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
            Intent intent = new Intent(this, ActivityFunction.class);
            intent.putExtra(ActivityFunction.EXTRA_SETTING_OPTION, option);
            startActivity(intent);
        }
    }


    ///
    // >>> Prevenção de queda >>>
    ///

    public void chamar(String number) {
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

    void runInBackground() {

        if (!isRecursionEnable)
            // Handle not to start multiple parallel threads
            return;

        // isRecursionEnable = false; when we want to stop
        // on exception on thread make it true again
        new Thread(new Runnable() {
            @Override
            public void run() {
                getlocation();
            }
        }).start();
    }

    private void getlocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, 10);
        }
        Looper.prepare();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                10000,
                100, this);
        Looper.loop();
    }

    public void onResume() {
        super.onResume();
        isLocationEnabled();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void isLocationEnabled() {
        if (locationManager != null) {
            return;
        }

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = alertDialog.create();
            alert.show();
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Confirm Location");
            alertDialog.setMessage("Your Location is enabled, please enjoy");
            alertDialog.setNegativeButton("Back to interface", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = alertDialog.create();
//            alert.show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            final double latitude = location.getLatitude();
            final double longitude = location.getLongitude();
            final Map<String, Object> locationMap = new HashMap<>();
            locationMap.put("Time", java.text.DateFormat.getDateTimeInstance().format(new Date()));
            locationMap.put("latitude", latitude);
            locationMap.put("longitude", longitude);
            // Escrever na Firebase
            FirebaseFirestore.getInstance().collection("/Location")
                    .document(FirebaseAuth.getInstance().getUid())
                    .set(locationMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("FIRESTORE Location", "DocumentSnapshot added: " + locationMap.toString());
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("FIRESTORE Location", "Error adding document", e);
                        }
                    });

        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
    // Valores de referência e lógica de: https://www.researchgate.net/publication/312266196_Automatic_Fall_Detection_using_Smartphone_Acceleration_Sensor

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            float ac = (float) sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

            long curTime = System.currentTimeMillis();

            if (a != null && a.size() > 5) {
                // Caso a tenha tamanho = 6
                // Deteção da queda
                Log.d("QUEDA", a.toString());
                for (int i = 0; i < n; i++) {
                    if ((float) a.get(i) < lowThreshold) {
                        for (int j = i + 1; j < n; j++) {
                            if ((float) a.get(j) > highThreshold) {
                                fallDetect = true;
                                SharedPreferences sharedPreferences = this.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                                String numero = sharedPreferences.getString("ContactoEmergencia", "112");
                                chamar(numero);
                            } else {
                                fallDetect = false;
                            }
                        }
                    } else {
                        fallDetect = false;
                    }
                }
                a.removeAll(a); // Torna a lista vazia

            } else {
                if ((curTime - lastUpdate) > 300 && isFinite(ac)) {
                    long diffTime = (curTime - lastUpdate);
                    lastUpdate = curTime;
                    a.add(ac);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }


}
