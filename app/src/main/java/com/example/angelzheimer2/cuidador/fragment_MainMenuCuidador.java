package com.example.angelzheimer2.cuidador;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.angelzheimer2.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class fragment_MainMenuCuidador extends Fragment {
    Button PhotoCamera, PhotoGallery;
    ImageView foto;
    LinearLayout changePhoto;
    int TAKE_IMAGE_CODE = 10001;
    int GALLERY_REQUEST = 10002;
    SharedPreferences sharedPreferences;
    String SHARED_PREFS = "sharedPrefs";
    private fragment_MainMenuCuidador.OnOptionClickListener mCallback;

    public fragment_MainMenuCuidador() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnOptionClickListener) context;
        } catch (Exception e) {
            throw new ClassCastException(context.toString() + " must implement OnOptionClickListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main_menu_cuidador, container, false);
        // Informações Pessoais
        sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        TextView txtUserName = rootView.findViewById(R.id.txtUserName);
        txtUserName.setText(sharedPreferences.getString("Nome", "Teste"));
        TextView txtUserType = rootView.findViewById(R.id.txtUserType);
//        txtUserType.setText(sharedPreferences.getString("UserType","Teste"));
        txtUserType.setText(getString(R.string.cuidador));
        // Fotografia do Utilizador
        foto = rootView.findViewById(R.id.user_photo);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user.getPhotoUrl() != null) {
            Glide.with(getContext()).load(user.getPhotoUrl())
                    .into(foto);
        }

        PhotoCamera = rootView.findViewById(R.id.bt_photoCamera);
        PhotoGallery = rootView.findViewById(R.id.bt_photoGallery);
        changePhoto = rootView.findViewById(R.id.changePhoto);
        changePhoto.setVisibility(View.GONE);


        foto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int visibility = changePhoto.getVisibility();
                if (visibility == View.VISIBLE) {
                    changePhoto.setVisibility(View.GONE);
                } else {
                    changePhoto.setVisibility(View.VISIBLE);
                }

                return false;
            }
        });
        PhotoCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePhoto.setVisibility(View.GONE);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(intent, TAKE_IMAGE_CODE);
                }
            }
        });
        PhotoGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePhoto.setVisibility(View.GONE);
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                intent.setType("image/*");
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(intent, GALLERY_REQUEST);
                }
            }
        });


        // Opção To Do
        CardView mToDo = rootView.findViewById(R.id.cvToDo);
        mToDo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onOptionSelected("ToDo");
            }
        });
        // Opção Informação Paciente
        CardView mInfoPaciente = rootView.findViewById(R.id.cvInfoPaciente);
        mInfoPaciente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onOptionSelected("InfoPaciente");
            }
        });
        // Opção Conversa
        CardView mMens = rootView.findViewById(R.id.cvMensagem);
        mMens.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onOptionSelected("Mens");
            }
        });
        // Opção Localização
        CardView mLocal = rootView.findViewById(R.id.cvLocal);
        mLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onOptionSelected("Local");
            }
        });
        // Opção Info pessoal
        CardView mInfo = rootView.findViewById(R.id.cvInfo);
        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onOptionSelected("Info");
            }
        });
        // Opção Chamada
        CardView mCall = rootView.findViewById(R.id.cvChamada);
        mCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onOptionSelected("Call");
            }
        });


        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_IMAGE_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    foto.setImageBitmap(bitmap);
                    handleUpload(bitmap);
            }
        }
        if (requestCode == GALLERY_REQUEST) {
            switch (resultCode) {
                case RESULT_OK:
                    Uri uri = data.getData();
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (bitmap != null) {
                        foto.setImageBitmap(bitmap);
                        handleUpload(bitmap);
                    } else {
                        Toast.makeText(getContext(), "Operação Cancelada", Toast.LENGTH_SHORT).show();
                    }

            }
        }
    }

    void handleUpload(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("profileImages")
                .child(UID + ".jpeg");
        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.w("IMAGE", "Imagem Carregada com sucesso");
                        getDownloadURL(reference);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("IMAGE", "onFailure: ", e);
                    }
                });
    }

    void getDownloadURL(StorageReference reference) {
        reference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.w("IMAGE", "Imagem Carregada com sucesso" + uri);
                        setUserProfileURL(uri);
                        Toast.makeText(getContext(), "Imagem Carregada com sucesso", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void setUserProfileURL(Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.w("IMAGE", "URL Carregado com sucesso");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("IMAGE", "onFailure: ", e);
                    }
                });

    }

    interface OnOptionClickListener {
        void onOptionSelected(String option);
    }
}
