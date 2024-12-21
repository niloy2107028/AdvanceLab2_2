package com.example.kuetapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kuetapp.ui.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Make sure this layout includes your logo

        EdgeToEdge.enable(this);


/*        // Set the status bar color to white
        getWindow().setStatusBarColor(getResources().getColor(android.R.color.white));

        // Optionally: Set the status bar content to dark (for white background)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // This will set the icons to dark on white status bar
        }*/

        // Add a fade-in animation to the logo
        ImageView logo = findViewById(R.id.logo); // Assuming you have an ImageView with the id "logo"
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in); // Load fade_in animation
        logo.startAnimation(fadeIn); // Start the animation

        // Transition to LoginActivity after a delay (2 seconds)
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class); // Open LoginActivity
            startActivity(intent);
            finish(); // Close SplashActivity to prevent going back to it
        }, 2000); // Delay duration in milliseconds (2 seconds)
    }
}
