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
import android.view.MenuInflater;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class fragment_Memo extends Fragment {
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    boolean doubleTap = false;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private FloatingActionButton fab;
    private FirebaseUser curUser;
    private RecyclerView mRecyclerView;
    private DatabaseReference mRef;
    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private ProgressDialog mProgress;
    private CircleImageView headerImage;

    public fragment_Memo() {
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
        View rootView = inflater.inflate(R.layout.fragment_memo, container, false);
        mAuth = FirebaseAuth.getInstance();
        curUser = mAuth.getCurrentUser();
        mProgress = new ProgressDialog(getContext());
        mProgress.setTitle("Getting your Books!");
        mProgress.setMessage("Please Wait ...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        mToolbar = rootView.findViewById(R.id.main_app_bar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        // Fica melhor sem a ActionBar
//        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();


        drawerLayout = rootView.findViewById(R.id.main_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView = rootView.findViewById(R.id.main_navigation_view);
        fab = rootView.findViewById(R.id.main_floatingActionButton);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddNotesActivity.class);
                intent.putExtra("uniqueKey", "from_create");
                startActivity(intent);
            }
        });

        mRecyclerView = rootView.findViewById(R.id.main_recyclerView);
//        mRecyclerView.setHasFixedSize(true);

        gridLayoutManager = new GridLayoutManager(getContext(), 2);
        //gridLayoutManager.setReverseLayout(true);
        //gridLayoutManager.setStackFromEnd(true);

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(linearLayoutManager);

        setHasOptionsMenu(true);

        return rootView;
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onStart() {
        super.onStart();
        mRef = FirebaseDatabase.getInstance().getReference().child("notes").child(curUser.getUid());


        if (!isNetworkAvailable()) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());

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


        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Model>()
                .setQuery(mRef, Model.class)
                .build();


        FirebaseRecyclerAdapter<Model, NotesViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Model, NotesViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull NotesViewHolder holder, int position, @NonNull Model model) {
                final String postKey = getRef(position).getKey();

                holder.setTitle(model.getTitle());
                holder.setNote(model.getNote());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), AddNotesActivity.class);
                        intent.putExtra("postKey", postKey);
                        intent.putExtra("uniqueKey", "from_main");
                        startActivity(intent);
                    }
                });

//                holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        Intent intent = new Intent(getActivity(), ReaderActivity.class);
//                        startActivity(intent);
//                        return false;
//                    }
//                });
            }

            @NonNull
            @Override
            public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                mProgress.dismiss();

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_layout, parent, false);
                NotesViewHolder viewHolder = new NotesViewHolder(view);
                return viewHolder;
            }
        };

        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
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
            mView = itemView;
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
