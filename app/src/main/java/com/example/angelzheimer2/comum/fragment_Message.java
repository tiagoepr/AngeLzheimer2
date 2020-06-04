package com.example.angelzheimer2.comum;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.angelzheimer2.R;
import com.example.angelzheimer2.extras.Mensagem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class fragment_Message extends Fragment {

    String SHARED_PREF = "sharedPrefs";
    SharedPreferences sharedPreferences;
    ImageButton btSend;
    String TextToSend;
    RecyclerView recyclerView;

    private GroupAdapter adapter;
    private EditText texto;

    public fragment_Message() {
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
        View rootView = inflater.inflate(R.layout.fragment_mesage, container, false);
        adapter = new GroupAdapter();
        recyclerView = rootView.findViewById(R.id.rvChat);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        sharedPreferences = getContext().getSharedPreferences(SHARED_PREF, MODE_PRIVATE);

        // <<<<<<<<<<<<<< Receber Mensagens >>>>>>>>>>>>>>>>>
        FirebaseFirestore.getInstance().collection("/users")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("CHAT", "Algo deu errado no recebimento de mensagens", e);
                        Toast.makeText(getContext(), "Não foi possível obter o chat", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        criarChat();
//                        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                    }
                });

        // <<<<<<<<<<<<<< Enviar Mensagens >>>>>>>>>>>>>>>>>
        texto = rootView.findViewById(R.id.editChat);
        btSend = rootView.findViewById(R.id.btSend);
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextToSend = texto.getText().toString().trim();
                texto.setText("");
                String FromID = sharedPreferences.getString("UID", null);
                final String ToID = sharedPreferences.getString("IDHomologo", null);
                long timestamp = System.currentTimeMillis();
                Mensagem mensagem = new Mensagem();
                mensagem.setFromID(FromID);
                mensagem.setToID(ToID);
                mensagem.setTimestamp(timestamp);
                mensagem.setText(TextToSend);

                // Enviar para:
                if (!mensagem.getText().isEmpty()) {
                    FirebaseFirestore.getInstance().collection("/chat")
                            .document(FromID)
                            .collection(ToID)
                            .add(mensagem)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d("CHAT", documentReference.getId());

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("CHAT", "Algo não correu bem: ", e);
                                }
                            });
                    // Receber de:
                    FirebaseFirestore.getInstance().collection("/chat")
                            .document(ToID)
                            .collection(FromID)
                            .add(mensagem)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d("CHAT", documentReference.getId());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("CHAT", "Algo não correu bem: ", e);
                                }
                            });
                }
            }
        });

        // Enviar Token/Notificação
//        updateToken();

        return rootView;
    }

//    private void updateToken() {
//        String token = FirebaseInstanceId.getInstance().getToken();
//        String UID = FirebaseAuth.getInstance().getUid();
//
//        if (UID != null) {
//            FirebaseFirestore.getInstance().collection("users")
//                    .document(UID)
//                    .update("token", token);
//        }
//    }


    private void criarChat() {
        String FromID = sharedPreferences.getString("UID", null);
        String ToID = sharedPreferences.getString("IDHomologo", null);
        FirebaseFirestore.getInstance().collection("/chat")
                .document(FromID)
                .collection(ToID)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

                        if (documentChanges != null) {
                            for (DocumentChange doc : documentChanges) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    Mensagem mensagem = doc.getDocument().toObject(Mensagem.class);
                                    adapter.add(new MessageItem(mensagem));
                                }
                            }
                        }

                    }
                });

    }

    private class MessageItem extends Item<ViewHolder> {

        private final Mensagem mensagem;

        private MessageItem(Mensagem mensagem) {
            this.mensagem = mensagem;
        }


        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView texto = viewHolder.itemView.findViewById(R.id.userText);
            final ImageView imagem = viewHolder.itemView.findViewById(R.id.userImage);

            texto.setText(mensagem.getText());

            String UID = mensagem.getFromID();
            StorageReference reference = FirebaseStorage.getInstance().getReference()
                    .child("profileImages")
                    .child(UID + ".jpeg");
            reference.getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get()
                                    .load(uri)
                                    .into(imagem);
                            Log.w("CHAT", "Imagem Carregada com sucesso: " + uri);
                        }
                    });

        }

        @Override
        public int getLayout() {
            return mensagem.getToID().equals(FirebaseAuth.getInstance().getUid())
                    ? R.layout.mensagem_recebida // Caso Seja
                    : R.layout.mensagem_enviada; // Caso Não seja
        }
    }


}
