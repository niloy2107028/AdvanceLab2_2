package com.example.kuetapp.ui.gallery;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kuetapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {

    RecyclerView convocationRecycler, otherRecycler, independentRecycler;
    Gallery_Adapter adapter;
    DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        // Initialize RecyclerViews
        convocationRecycler = view.findViewById(R.id.convocationRecycler);
        otherRecycler = view.findViewById(R.id.otherRecycler);
        independentRecycler = view.findViewById(R.id.independentRecycler);

        // Initialize Firebase Database reference
        reference = FirebaseDatabase.getInstance().getReference().child("Gallery");

        // Fetch data for each category
        getConvocationImage();
        getIndependentImage();
        getOtherImage();

        return view;
    }

    private void getIndependentImage() {
        reference.child("Independence Day").addValueEventListener(new ValueEventListener() {
            List<String> imageList = new ArrayList<>();

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageList.clear(); // Clear previous data
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String data = (String) dataSnapshot.getValue();
                    if (data != null) {
                        imageList.add(data);
                    }
                }
                setupRecyclerView(independentRecycler, imageList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void getOtherImage() {
        reference.child("Other Events").addValueEventListener(new ValueEventListener() {
            List<String> imageList = new ArrayList<>();

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String data = (String) dataSnapshot.getValue();
                    if (data != null) {
                        imageList.add(data);
                    }
                }
                setupRecyclerView(otherRecycler, imageList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void getConvocationImage() {
        reference.child("Convocation").addValueEventListener(new ValueEventListener() {
            List<String> imageList = new ArrayList<>();

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String data = (String) dataSnapshot.getValue();
                    if (data != null) {
                        imageList.add(data);
                    }
                }
                setupRecyclerView(convocationRecycler, imageList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void setupRecyclerView(RecyclerView recyclerView, List<String> imageList) {
        Context context = getContext();
        if (context == null || imageList == null || imageList.isEmpty()) {
            return;
        }
        adapter = new Gallery_Adapter(context, imageList);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 3));
        recyclerView.setAdapter(adapter);
    }
}
