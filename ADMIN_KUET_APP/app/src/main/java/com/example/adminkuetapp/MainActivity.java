package com.example.adminkuetapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adminkuetapp.faculty.UpdateFaculty;
import com.example.adminkuetapp.notice.DeleteNoticeActivity;
import com.example.adminkuetapp.notice.UploadNotice;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialCardView uploadNotice = findViewById(R.id.addNotice);
        MaterialCardView uploadGallery = findViewById(R.id.addImage);
        MaterialCardView uploadEbook = findViewById(R.id.addEbook);
        MaterialCardView uploadFaculty = findViewById(R.id.addFaculty);
        MaterialCardView deleteNotice = findViewById(R.id.deleteNotice);


        uploadNotice.setOnClickListener(this);
        uploadGallery.setOnClickListener(this);
        uploadEbook.setOnClickListener(this);
        uploadFaculty.setOnClickListener(this);
        deleteNotice.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Intent intent;

        if (id == R.id.addNotice) {
            intent = new Intent(MainActivity.this, UploadNotice.class);
            startActivity(intent);
        } else if (id == R.id.addImage) {
            intent = new Intent(MainActivity.this, UploadImage.class);
            startActivity(intent);
        } else if (id == R.id.addEbook) {
            intent = new Intent(MainActivity.this, UploadEbook.class);
            startActivity(intent);
        }else if (id == R.id.addFaculty) {
            intent = new Intent(MainActivity.this, UpdateFaculty.class);
            startActivity(intent);
        }else if (id == R.id.deleteNotice) {
            intent = new Intent(MainActivity.this, DeleteNoticeActivity.class);
            startActivity(intent);
        }
    }
}
