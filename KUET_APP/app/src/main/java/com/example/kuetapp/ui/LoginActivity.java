package com.example.kuetapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kuetapp.MainActivity;
import com.example.kuetapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {


    private EditText logEmail, logPass;
    private Button loginButton;
    private TextView register;

    private String email, pass;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Remove ActionBar programmatically
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        //EdgeToEdge.enable(this);   //for white status bar


        setContentView(R.layout.activity_login);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth=FirebaseAuth.getInstance();


        logEmail = findViewById(R.id.loginEmail);
        logPass = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        register = findViewById(R.id.openRegister);


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        loginButton.setOnClickListener((view) -> {
            validateUser();
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        if(auth.getCurrentUser()!=null){

            startActivity(new Intent(this, MainActivity.class));
            finish();

        }

    }


    private void validateUser() {
        email = logEmail.getText().toString();
        pass = logPass.getText().toString();
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        } else {
            loginUser();
        }

    }

    private void loginUser() {
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                    Toast.makeText(LoginActivity.this, "Login Unsuccessful. Reason : " + errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e("LoginError", "Login failed: " + errorMessage);
                }
            }
        });
    }

}