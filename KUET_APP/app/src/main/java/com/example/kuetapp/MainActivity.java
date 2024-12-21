package com.example.kuetapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private NavController navController;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        navController = Navigation.findNavController(this, R.id.frame_layout);


        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigation_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.start, R.string.close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(this);

        NavigationUI.setupWithNavController(bottomNavigationView, navController);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        toggle.onOptionsItemSelected(item);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId(); // Store the ID in a variable

        if (itemId == R.id.navigation_developer) {
            Toast.makeText(this, "Developer section will be developed soon", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.navigation_video) {
            Toast.makeText(this, "Video Lectures section will be developed soon", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.navigation_news) {
            Toast.makeText(this, "KUET News section will be developed soon", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.navigation_notice) {
            Toast.makeText(this, "KUET Notice section will be developed soon", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.navigation_calender) {
            Toast.makeText(this, "KUET Calender section will be developed soon", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.navigation_transport) {
            Toast.makeText(this, "KUET Transport section will be developed soon", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.navigation_video) {
            Toast.makeText(this, "Video Lectures section will be developed soon", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.navigation_ebook) {
            Toast.makeText(this, "Ebooks section will be developed soon", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.navigation_theme) {
            Toast.makeText(this, "Themes section will be developed soon", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.navigation_website) {
            Toast.makeText(this, "Website section will be developed soon", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.navigation_about) {
            Toast.makeText(this, "About section will be developed soon", Toast.LENGTH_SHORT).show();
        }


        return true;

    }
}