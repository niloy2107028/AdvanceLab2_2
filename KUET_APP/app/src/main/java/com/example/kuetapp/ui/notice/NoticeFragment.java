package com.example.kuetapp.ui.notice;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.example.kuetapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class NoticeFragment extends Fragment {

    private RecyclerView newsfeedNoticeRecycler;
    private ProgressBar progressBar;
    private ArrayList<NoticeData> list;
    private NoticeAdapter adapter;

    private DatabaseReference reference;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notice, container, false);


        newsfeedNoticeRecycler = view.findViewById(R.id.newsfeedNoticeRecycler);
        progressBar = view.findViewById(R.id.progressBar);

        // Firebase reference
        reference = FirebaseDatabase.getInstance().getReference().child("Notice");


        newsfeedNoticeRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        newsfeedNoticeRecycler.setHasFixedSize(true);

        getNotice();

        return view; //importance for fragment
    }


    private void getNotice() {
        progressBar.setVisibility(View.VISIBLE); // Show progress bar while loading
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list = new ArrayList<>();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    NoticeData data = snapshot1.getValue(NoticeData.class);
                    if (data != null) {
                        list.add(0,data);
                    }
                }

                if (adapter == null) {
                    adapter = new NoticeAdapter(getContext(), list);
                    newsfeedNoticeRecycler.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged(); // Update the adapter if it already exists
                }

                progressBar.setVisibility(View.GONE); // Hide progress bar after loading
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}