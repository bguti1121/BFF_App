package com.example.bff_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgotPass extends AppCompatActivity {
    TextInputEditText editTextEmail;
    Button resetPassword, backToLogin;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_forgot_pass);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        editTextEmail = findViewById(R.id.email);
        resetPassword = findViewById(R.id.resetPass);
        progressBar = findViewById(R.id.progressBar);
        backToLogin = findViewById(R.id.backToLogin);

        backToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        resetPassword.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String email;
            email = String.valueOf(editTextEmail.getText());

            if(TextUtils.isEmpty(email)){
                Toast.makeText(ForgotPass.this, "Enter email", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPass.this, "Reset link sent to your email", Toast.LENGTH_LONG).show();
                            // Optionally, navigate back to login screen:
                            Intent intent = new Intent(ForgotPass.this, Login.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ForgotPass.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        });
    }

    @Override
    public void onStart() {
        super.onStart();
        //Checks if user is logged in already
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), Dashboard.class);
            startActivity(intent);
            finish();
        }
    }
}