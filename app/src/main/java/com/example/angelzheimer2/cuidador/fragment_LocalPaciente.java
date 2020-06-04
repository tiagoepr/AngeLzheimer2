package com.example.angelzheimer2.cuidador;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.angelzheimer2.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.content.Context.MODE_PRIVATE;

public class fragment_LocalPaciente extends Fragment {
    GoogleMap mMap;
    Context mContext = getContext();
    String SHARED_PREFS = "sharedPrefs";
    SharedPreferences sharedPreferences;
    FirebaseUser user;
    ImageButton imBtLocal;
    LatLng lastPosition;
    String IDHomologo;
    DocumentReference docRef;
    ImageButton Local;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            // Add a marker in Sydney and move the camera
            mMap = googleMap;
            Log.d("MAPA", "mMap Criado");
//            LatLng india = new LatLng(40.64427, -8.64554);
//            mMap.addMarker(new MarkerOptions().position(india).title("Marker in Aveiro"));
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(india));
            posicaoinicial();

        }
    };

    public fragment_LocalPaciente() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void listener() {

        if (IDHomologo != null && mMap != null) {
            docRef = FirebaseFirestore.getInstance().collection("/Location")
                    .document(IDHomologo);
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w("MAPA", "Listen failed.", e);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        Log.d("MAPA", "Current data: " + snapshot.getData());
                        docRef.get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        double latitude = documentSnapshot.getDouble("latitude");
                                        double longitude = documentSnapshot.getDouble("longitude");
                                        String datahora = documentSnapshot.getString("Time");
                                        String title = getString(R.string.UltimaLocalizacao);
                                        String Tag = title + ": " + datahora;
                                        lastPosition = new LatLng(latitude, longitude);
                                        mMap.clear();
                                        mMap.addMarker(new MarkerOptions().position(lastPosition).title(Tag));
                                        CameraPosition cameraPosition = new CameraPosition.Builder().target(lastPosition).zoom(16.0f).build();
                                        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                                        mMap.moveCamera(cameraUpdate);
                                    }
                                });


                    } else {
                        Log.d("MAPA", "Current data: null");
                    }
                }
            });
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_local_paciente, container, false);
        sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        IDHomologo = sharedPreferences.getString("IDHomologo", null);

        Local = rootView.findViewById(R.id.imBtMyLocation);
        Local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                posicaoinicial();
            }
        });

        listener();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }


        return rootView;
    }

    private void posicaoinicial() {
        sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        IDHomologo = sharedPreferences.getString("IDHomologo", null);
        if (IDHomologo != null && mMap != null) {
            FirebaseFirestore.getInstance().collection("/Location")
                    .document(IDHomologo)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            double latitude = documentSnapshot.getDouble("latitude");
                            double longitude = documentSnapshot.getDouble("longitude");
                            String datahora = documentSnapshot.getString("Time");
                            String title = getString(R.string.UltimaLocalizacao);
                            String Tag = title + ": " + datahora;
                            lastPosition = new LatLng(latitude, longitude);
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions().position(lastPosition).title(Tag));
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(lastPosition).zoom(16.0f).build();
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                            mMap.moveCamera(cameraUpdate);

                        }
                    });


        } else {
            Log.d("MAPA", "Current data: null");
        }
    }
}
