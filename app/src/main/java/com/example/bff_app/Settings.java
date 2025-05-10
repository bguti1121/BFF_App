package com.example.bff_app;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Settings extends AppCompatActivity {
    AppCompatButton dashboardBtn, summaryBtn, goalsBtn, moreBtn;
    TextView changeUsernameBtn, changePasswordBtn, updateEmailBtn;
    SwitchCompat darkModeSwitch;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        // Dark mode if-else
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //References to database
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        // Setting buttons
        changeUsernameBtn = findViewById(R.id.changeUsername);
        changePasswordBtn = findViewById(R.id.changePassword);
        updateEmailBtn = findViewById(R.id.updateEmail);
        darkModeSwitch = findViewById(R.id.darkMode);
        // Image buttons
        dashboardBtn = findViewById(R.id.dashboard_btn);
        summaryBtn = findViewById(R.id.summary_btn);
        goalsBtn = findViewById(R.id.goals_btn);
        moreBtn = findViewById(R.id.more_btn);

        // changeUsername button onClick event handler
        changeUsernameBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
            builder.setTitle("Change Username");
            // Initializes popup elements
            final EditText input = new EditText(Settings.this);
            input.setHint("Enter new username");
            builder.setView(input);

            builder.setPositiveButton("Update", (dialog, which) -> {
                String newUsername = input.getText().toString().trim();
                if (!newUsername.isEmpty()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String uid = user.getUid();
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
                        userRef.child("name").setValue(newUsername)
                                .addOnSuccessListener(aVoid -> Toast.makeText(Settings.this, "Username updated", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(Settings.this, "Failed to update username", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Toast.makeText(Settings.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });

        // Change password onClick event handler
        changePasswordBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
            builder.setTitle("Change Password");
            // Initializes popup elements
            View viewInflated = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
            final EditText currentPassword = viewInflated.findViewById(R.id.currentPassword);
            final EditText newPassword = viewInflated.findViewById(R.id.newPassword);
            final EditText confirmPassword = viewInflated.findViewById(R.id.confirmPassword);

            builder.setView(viewInflated);

            builder.setPositiveButton("Update", (dialog, which) -> {
                String currPass = currentPassword.getText().toString();
                String newPass = newPassword.getText().toString();
                String confirmPass = confirmPassword.getText().toString();

                if (!newPass.equals(confirmPass)) {
                    Toast.makeText(Settings.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currPass);
                // Attempts to update user password
                currentUser.reauthenticate(credential).addOnSuccessListener(unused -> {
                    currentUser.updatePassword(newPass)
                            .addOnSuccessListener(aVoid -> Toast.makeText(Settings.this, "Password updated!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(Settings.this, "Failed to update password.", Toast.LENGTH_SHORT).show());
                }).addOnFailureListener(e -> {
                    Toast.makeText(Settings.this, "Current password incorrect.", Toast.LENGTH_SHORT).show();
                });
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });

        //Update Email onClick event handler
        updateEmailBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
            builder.setTitle("Change Email");
            // Initializes popup elements
            View viewInflated = getLayoutInflater().inflate(R.layout.dialog_change_email, null);
            final EditText currentEmail = viewInflated.findViewById(R.id.currentEmail);
            final EditText newEmail = viewInflated.findViewById(R.id.newEmail);
            final EditText confirmEmail = viewInflated.findViewById(R.id.confirmEmail);
            final EditText passwordField = viewInflated.findViewById(R.id.passwordField);

            builder.setView(viewInflated);

            builder.setPositiveButton("Update", (dialog, which) -> {
                String currEmail = currentEmail.getText().toString();
                String newE = newEmail.getText().toString();
                String confirmE = confirmEmail.getText().toString();
                String password = passwordField.getText().toString().trim();

                if (currEmail.isEmpty() || newE.isEmpty() || confirmE.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Settings.this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newE.equals(confirmE)) {
                    Toast.makeText(Settings.this, "Emails do not match!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!currEmail.equals(currentUser.getEmail())) {
                    Toast.makeText(Settings.this, "Current email incorrect.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Re-authenticates user in order to update email
                AuthCredential credential = EmailAuthProvider.getCredential(currEmail, password);
                currentUser.reauthenticate(credential)
                        .addOnCompleteListener(authTask -> {
                            if (authTask.isSuccessful()) {
                                currentUser.updateEmail(newE)
                                        .addOnSuccessListener(aVoid -> {
                                            // Successfully updated email in Auth
                                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
                                            userRef.child("email").setValue(newE)
                                                    .addOnSuccessListener(a -> {
                                                        Toast.makeText(Settings.this, "Email updated successfully!", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(dbError -> {
                                                        Log.e("DatabaseUpdate", "Failed to update email in database: " + dbError.getMessage());
                                                        Toast.makeText(Settings.this, "Email updated in Auth but failed in Database.", Toast.LENGTH_LONG).show();
                                                    });
                                        })
                                        .addOnFailureListener(updateError -> {
                                            Log.e("UpdateEmail", "Failed to update email: " + updateError.getMessage());
                                            Toast.makeText(Settings.this, "Failed to update email: " + updateError.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                            } else {
                                Log.e("Re-auth", "Re-authentication failed: " + authTask.getException().getMessage());
                                Toast.makeText(Settings.this, "Re-authentication failed: " + authTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });

        // Dark mode button onClick event handler
        darkModeSwitch.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences("SettingsPrefs", MODE_PRIVATE).edit();
            boolean isCurrentlyDark = (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);

            if (isCurrentlyDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean("dark_mode", false);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean("dark_mode", true);
            }
            editor.apply();
        });
        //Dashboard button onClick event handler
        dashboardBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Dashboard.class);
            startActivity(intent);
            finish();
        });

        //Summary button onClick event handler
        summaryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Summary.class);
            startActivity(intent);
            finish();
        });

        //Goals button onClick event handler
        goalsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SavingGoalsActivity.class);
            startActivity(intent);
            finish();
        });

        //More button onClick event handler
        moreBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Extra.class);
            startActivity(intent);
            finish();
        });
    }
}