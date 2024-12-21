package com.example.adminkuetapp.faculty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adminkuetapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UpdateFaculty extends AppCompatActivity implements View.OnClickListener {

    FloatingActionButton fab;
    FloatingActionButton fab_fetchdata;
    private RecyclerView CSE, EEE, ME, CIVIL, others;
    private LinearLayout cseNoData, eeeNoData, meNoData, civilNoData, otherNoData;
    private List<TeacherData> list1, list2, list3, list4, list5;
    private TeacherAdapter teacherAdapter;

    private DatabaseReference reference, dbReff;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //EdgeToEdge.enable(this);

        setContentView(R.layout.activity_update_faculty);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        CSE = findViewById(R.id.cseDepartment);
        EEE = findViewById(R.id.eeeDepartment);
        ME = findViewById(R.id.meDepartment);
        CIVIL = findViewById(R.id.civilDepartment);
        others = findViewById(R.id.otherDepartment);

        cseNoData = findViewById(R.id.cseNoData);
        eeeNoData = findViewById(R.id.eeeNoData);
        meNoData = findViewById(R.id.meNoData);
        civilNoData = findViewById(R.id.civilNoData);
        otherNoData = findViewById(R.id.otherNoData);


        reference = FirebaseDatabase.getInstance().getReference().child("Teachers");


        fab = findViewById(R.id.fab);
        fab_fetchdata = findViewById(R.id.fab_fetchdata);



        fab.setOnClickListener(this);
        fab_fetchdata.setOnClickListener(this);


    }

    @Override  //Method does not override method from its superclass
    public void onClick(View view) {
        int id = view.getId();
        Intent intent;

        if (id == R.id.fab) {
            intent = new Intent(UpdateFaculty.this, AddFaculty.class);
            startActivity(intent);
        } else if (id == R.id.fab_fetchdata) {
            CSE();
            EEE();
            ME();
            CIVIL();
            others();
            Toast.makeText(this, "Data fetched successfully", Toast.LENGTH_SHORT).show();
        }
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
                    CSE.setLayoutManager(new LinearLayoutManager(UpdateFaculty.this));
                    teacherAdapter = new TeacherAdapter(list1, UpdateFaculty.this,"CSE");
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
                    EEE.setLayoutManager(new LinearLayoutManager(UpdateFaculty.this));
                    teacherAdapter = new TeacherAdapter(list2, UpdateFaculty.this,"EEE");
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
                    ME.setLayoutManager(new LinearLayoutManager(UpdateFaculty.this));
                    teacherAdapter = new TeacherAdapter(list3, UpdateFaculty.this,"ME");
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
                    CIVIL.setLayoutManager(new LinearLayoutManager(UpdateFaculty.this));
                    teacherAdapter = new TeacherAdapter(list4, UpdateFaculty.this,"CIVIL");
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
                    others.setLayoutManager(new LinearLayoutManager(UpdateFaculty.this));
                    teacherAdapter = new TeacherAdapter(list5, UpdateFaculty.this,"others");
                    others.setAdapter(teacherAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}