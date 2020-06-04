package com.example.angelzheimer2.paciente;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.angelzheimer2.R;
import com.example.angelzheimer2.extras.Model;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityMemo extends AppCompatActivity {

    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    boolean doubleTap = false;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private FloatingActionButton fab;
    private FirebaseUser curUser;
    private CircleImageView headerImage;
    private TextView headerUserName;
    private RecyclerView mRecyclerView;
    private DatabaseReference mRef;
    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo);

        mAuth = FirebaseAuth.getInstance();
        curUser = mAuth.getCurrentUser();
        mProgress = new ProgressDialog(this);

        mProgress.setTitle("Procurando os Blocos de Notas");
        mProgress.setMessage("Por favor Espere");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();


        mToolbar = findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(" ");

        drawerLayout = findViewById(R.id.main_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        fab = findViewById(R.id.main_floatingActionButton);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddNotesActivity.class);
                intent.putExtra("uniqueKey", "from_create");
                startActivity(intent);
            }
        });

        mRecyclerView = findViewById(R.id.main_recyclerView);
//        mRecyclerView.setHasFixedSize(true);

        gridLayoutManager = new GridLayoutManager(this, 2);
        //gridLayoutManager.setReverseLayout(true);
        //gridLayoutManager.setStackFromEnd(true);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(gridLayoutManager);
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mRef = FirebaseDatabase.getInstance().getReference().child("notes").child(curUser.getUid());

        if (!isNetworkAvailable()) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            alertDialog.setTitle("Info");
            alertDialog.setMessage("Internet not available, Cross check your internet connectivity and try again");
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog ad = alertDialog.create();
            ad.show();
        }

        // >>>
        // FirebaseRecyclerOptions, FirebaseRecyclerAdapter e View Holder //
        // >>>

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Model>()
                .setQuery(mRef, Model.class)
                .build();
        FirebaseRecyclerAdapter<Model, SelectedBooks.NotesViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Model, SelectedBooks.NotesViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull SelectedBooks.NotesViewHolder holder, int position, @NonNull Model model) {
                final String postKey = getRef(position).getKey();

                holder.setTitle(model.getTitle());
                holder.setNote(model.getNote());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), AddNotesActivity.class);
                        intent.putExtra("postKey", postKey);
                        intent.putExtra("uniqueKey", "from_main");
                        startActivity(intent);
                    }
                });

//                holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        Intent intent=new Intent(getApplicationContext(),ReaderActivity.class);
//                        startActivity(intent);
//                        return false;
//                    }
//                });
            }

            @NonNull
            @Override
            public SelectedBooks.NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                mProgress.dismiss();

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_layout, parent, false);
                SelectedBooks.NotesViewHolder viewHolder = new SelectedBooks.NotesViewHolder(view);
                return viewHolder;
            }
        };

        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    /// >>> Fim

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getItemId() == R.id.main_menu_layout) {
            Drawable drawable = item.getIcon();


            if (drawable.getConstantState().equals(getResources().getDrawable(R.drawable.ic_grid_on_black_24dp).getConstantState())) {
                mRecyclerView.setLayoutManager(gridLayoutManager);
                item.setIcon(R.drawable.ic_linear_black_24dp);

            } else if (drawable.getConstantState().equals(getResources().getDrawable(R.drawable.ic_linear_black_24dp).getConstantState())) {
                mRecyclerView.setLayoutManager(linearLayoutManager);
                item.setIcon(R.drawable.ic_grid_on_black_24dp);

            }

        }

        return super.onOptionsItemSelected(item);
    }

    public static class NotesViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public NotesViewHolder(View itemView) {
            super(itemView);
            this.mView = itemView;
        }

        public void setTitle(String t) {
            TextView title = mView.findViewById(R.id.note_title_text);
            title.setText(t);
        }

        public void setNote(String n) {
            TextView note = mView.findViewById(R.id.notes_text);
            note.setText(n);
        }

    }

}
