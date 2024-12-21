package com.example.kuetapp.ui.faculty;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.kuetapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class FacultyFragment extends Fragment {

    FloatingActionButton fab_fetchdata;
    private RecyclerView CSE, EEE, ME, CIVIL, others;
    private LinearLayout cseNoData, eeeNoData, meNoData, civilNoData, otherNoData;
    private List<TeacherData> list1, list2, list3, list4, list5;
    private TeacherAdapter teacherAdapter;

    private DatabaseReference reference, dbReff;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_faculty, container, false);


        CSE = view.findViewById(R.id.cseDepartment);
        EEE = view.findViewById(R.id.eeeDepartment);
        ME = view.findViewById(R.id.meDepartment);
        CIVIL = view.findViewById(R.id.civilDepartment);
        others = view.findViewById(R.id.otherDepartment);

        cseNoData = view.findViewById(R.id.cseNoData);
        eeeNoData = view.findViewById(R.id.eeeNoData);
        meNoData = view.findViewById(R.id.meNoData);
        civilNoData = view.findViewById(R.id.civilNoData);
        otherNoData = view.findViewById(R.id.otherNoData);


        reference = FirebaseDatabase.getInstance().getReference().child("Teachers");



        fab_fetchdata = view.findViewById(R.id.fab_fetchdata);




        fab_fetchdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CSE();
                EEE();
                ME();
                CIVIL();
                others();
                Toast.makeText(getContext(), "Data fetched successfully", Toast.LENGTH_SHORT).show();  //i want to show this toast after every thing is fetched successfully but this is showing without fetching
            }
        });

        return view;



    }



    private void CSE() {
        dbReff = reference.child("CSE");
        dbReff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list1 = new ArrayList<>();
                if (!snapshot.exists()) {
                    CSE.setVisibility(View.GONE);
                    cseNoData.setVisibility(View.VISIBLE);
                } else {
                    CSE.setVisibility(View.VISIBLE);
                    cseNoData.setVisibility(View.GONE);
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        TeacherData data = snapshot1.getValue((TeacherData.class));
                        list1.add(data);
                    }

                    CSE.setHasFixedSize(true);
                    CSE.setLayoutManager(new LinearLayoutManager(getContext()));
                    teacherAdapter = new TeacherAdapter(list1, getContext());
                    CSE.setAdapter(teacherAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void EEE() {
        dbReff = reference.child("EEE");
        dbReff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list2 = new ArrayList<>();
                if (!snapshot.exists()) {
                    EEE.setVisibility(View.GONE);
                    eeeNoData.setVisibility(View.VISIBLE);
                } else {
                    EEE.setVisibility(View.VISIBLE);
                    eeeNoData.setVisibility(View.GONE);
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        TeacherData data = snapshot1.getValue((TeacherData.class));
                        list2.add(data);
                    }

                    EEE.setHasFixedSize(true);
                    EEE.setLayoutManager(new LinearLayoutManager(getContext()));
                    teacherAdapter = new TeacherAdapter(list2, getContext());
                    EEE.setAdapter(teacherAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ME() {
        dbReff = reference.child("ME");
        dbReff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list3 = new ArrayList<>();
                if (!snapshot.exists()) {
                    ME.setVisibility(View.GONE);
                    meNoData.setVisibility(View.VISIBLE);
                } else {
                    ME.setVisibility(View.VISIBLE);
                    meNoData.setVisibility(View.GONE);
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        TeacherData data = snapshot1.getValue((TeacherData.class));
                        list3.add(data);
                    }

                    ME.setHasFixedSize(true);
                    ME.setLayoutManager(new LinearLayoutManager(getContext()));
                    teacherAdapter = new TeacherAdapter(list3, getContext());
                    ME.setAdapter(teacherAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void CIVIL() {
        dbReff = reference.child("CIVIL");
        dbReff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list4 = new ArrayList<>();
                if (!snapshot.exists()) {
                    CIVIL.setVisibility(View.GONE);
                    civilNoData.setVisibility(View.VISIBLE);
                } else {
                    CIVIL.setVisibility(View.VISIBLE);
                    civilNoData.setVisibility(View.GONE);
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        TeacherData data = snapshot1.getValue((TeacherData.class));
                        list4.add(data);
                    }

                    CIVIL.setHasFixedSize(true);
                    CIVIL.setLayoutManager(new LinearLayoutManager(getContext()));
                    teacherAdapter = new TeacherAdapter(list4, getContext());
                    CIVIL.setAdapter(teacherAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void others() {
        dbReff = reference.child("Other Departments");
        dbReff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list5 = new ArrayList<>();
                if (!snapshot.exists()) {
                    others.setVisibility(View.GONE);
                    otherNoData.setVisibility(View.VISIBLE);
                } else {
                    others.setVisibility(View.VISIBLE);
                    otherNoData.setVisibility(View.GONE);
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        TeacherData data = snapshot1.getValue((TeacherData.class));
                        list5.add(data);
                    }

                    others.setHasFixedSize(true);
                    others.setLayoutManager(new LinearLayoutManager(getContext()));
                    teacherAdapter = new TeacherAdapter(list5, getContext());
                    others.setAdapter(teacherAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}