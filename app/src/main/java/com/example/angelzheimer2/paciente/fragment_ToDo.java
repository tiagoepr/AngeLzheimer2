package com.example.angelzheimer2.paciente;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.angelzheimer2.R;
import com.example.angelzheimer2.extras.Tasks;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class fragment_ToDo extends Fragment {
    Button bt_add;
    CardView cv_sync;
    String SHARED_PREF = "sharedPrefs";
    SharedPreferences sharedPreferences;
    String ID;
    private GroupAdapter adapter;
    private View recyclerView;


    public fragment_ToDo() {
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
        View rootView = inflater.inflate(R.layout.fragment_to_do, container, false);
        sharedPreferences = getContext().getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        cv_sync = rootView.findViewById(R.id.cv_sync);
        cv_sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter != null) {
                    adapter.clear();
                    criarLista();
                }


            }
        });

        // Carregar a lista de tarefas do firestore
        adapter = new GroupAdapter();
        recyclerView = rootView.findViewById(R.id.rv);
        ((RecyclerView) recyclerView).setLayoutManager(new LinearLayoutManager(getContext()));
        ((RecyclerView) recyclerView).setAdapter(adapter);
//        criarLista();


        bt_add = rootView.findViewById(R.id.btn_add);
        bt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Novo fragmento/atividade
                Intent intent = new Intent(getActivity(), ActivityAddTask.class);
                startActivity(intent);


            }
        });

        // Atualização da recyclerview
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
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w("TODO", "Listen failed.", e);
                                return;
                            } else {
                                Log.d("TODO", "Houve alteração: " + ID);
                                criarLista();
                            }
                        }
                    });
        }


        return rootView;
    }


    private void criarLista() {

        if (ID != null) {

            FirebaseFirestore.getInstance().collection("/ToDo")
                    .document(ID)
                    .collection("/ToDo")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                adapter.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Tasks tarefa = document.toObject(Tasks.class);
                                    tarefa.setID(document.getId());
                                    adapter.add(new TaskItem(tarefa));
                                    Log.d("TODO", document.getId() + ": " + tarefa.getTitulo());

                                }

                            }
                        }
                    });

        }


    }


    private class TaskItem extends Item<ViewHolder> {

        private final Tasks task;

        private TaskItem(Tasks task) {
            this.task = task;
        }


        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            final String IDtask = task.getID();
            TextView titulo = viewHolder.itemView.findViewById(R.id.tv_activity);
            TextView DataHora = viewHolder.itemView.findViewById(R.id.tv_time); // dd/mm/aaaa\n/hh:mm
            final Switch cheked = viewHolder.itemView.findViewById(R.id.sw_status);

            titulo.setText(task.getTitulo());

//            SimpleDateFormat SDPdata = new SimpleDateFormat("dd/MM/YYYY");
//            String data = SDPdata.format(task.getDataHora());
//            SimpleDateFormat SDPhora = new SimpleDateFormat("kk:mm");
//            String hora = SDPhora.format(task.getDataHora());

            String data = task.getData();
            String hora = task.getHora();

            DataHora.setText(data + "\n" + hora);

            cheked.setChecked(task.isEnabled());
            cheked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    final Tasks tarefa = new Tasks();
                    tarefa.setTitulo(task.getTitulo());
                    tarefa.setDescricao(task.getDescricao());
                    tarefa.setData(task.getData());
                    tarefa.setHora(task.getHora());
                    tarefa.setEnabled(cheked.isChecked());

                    if (ID != null) {
                        FirebaseFirestore.getInstance().collection("/ToDo")
                                .document(ID)
                                .collection("/ToDo")
                                .document(IDtask)
                                .set(tarefa)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.d("TODO", IDtask + "- Status: " + tarefa.isEnabled());
                                        String texto;
                                        if (tarefa.isEnabled()) {
                                            texto = getString(R.string.tarefaconcluida);
                                        } else {
                                            texto = getString(R.string.tarefaprconcluir);
                                        }
                                        Toast.makeText(getContext(), texto, Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("TODO", "Algo não correu bem: ", e);
                                        Toast.makeText(getContext(), getString(R.string.algocorreumau), Toast.LENGTH_LONG).show();

                                    }
                                });
                    }


                }
            });

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                                       @Override
                                                       public void onClick(View v) {
                                                           Intent intent = new Intent(getContext(), EditNotesActivity.class);
                                                           intent.putExtra("IDtask", IDtask);
                                                           startActivity(intent);
                                                       }
                                                   }
            );
        }

        @Override
        public int getLayout() {
            return R.layout.task;
        }
    }


}
