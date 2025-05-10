package com.example.bff_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SavingGoalsActivity extends AppCompatActivity {
    AppCompatButton dashboardBtn, summaryBtn, goalsBtn, moreBtn;

    RecyclerView recyclerView;
    TextView noGoals;
    Button addGoalBtn;

    List<Goal> goalList;
    GoalManager adapter;

    FirebaseAuth mAuth;
    DatabaseReference userRef;
    String uid;

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
        setContentView(R.layout.activity_saving_goals);

        recyclerView = findViewById(R.id.goalsRecyclerView);
        noGoals = findViewById(R.id.noGoals);
        addGoalBtn = findViewById(R.id.addGoalButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        goalList = new ArrayList<>();
        adapter = new GoalManager(goalList);
        recyclerView.setAdapter(adapter);

        dashboardBtn = findViewById(R.id.dashboard_btn);
        summaryBtn = findViewById(R.id.summary_btn);
        goalsBtn = findViewById(R.id.goals_btn);
        moreBtn = findViewById(R.id.more_btn);
        addGoalBtn = findViewById(R.id.addGoalButton);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();


        if (uid == null) {
            Log.e("SetIncomes", "User ID is null!");
            return;
        }
        String userID = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userID == null) {
            Log.e("SavingGoals", "User not signed in.");
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("savinggoals");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                goalList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Goal goal = dataSnapshot.getValue(Goal.class);
                    if (goal != null) {
                        goalList.add(goal);
                    }
                    else{
                        Log.e("SavingGoals", "Goal is null!");
                    }
                }
                adapter.notifyDataSetChanged();

                if (goalList.isEmpty()) {
                    noGoals.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    noGoals.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SavingGoals", "Database error: " + error.getMessage());
            }
        });

        addGoalBtn.setOnClickListener(v -> {
                    Intent intent = new Intent(SavingGoalsActivity.this, AddGoalActivity.class);
                    startActivity(intent);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // ✅ Handle goal edit result (requestCode 1)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Goal updatedGoal = (Goal) data.getSerializableExtra("editedGoal");
            int position = data.getIntExtra("position", -1);

            if (position != -1 && updatedGoal != null) {
                goalList.set(position, updatedGoal);
                adapter.notifyItemChanged(position);
            }
        }

        // Handle goal deletion from detail view (requestCode 2)
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            Goal deletedGoal = (Goal) data.getSerializableExtra("deletedGoal");

            if (deletedGoal != null) {
                for (int i = 0; i < goalList.size(); i++) {
                    Goal g = goalList.get(i);
                    if (g.getTitle().equals(deletedGoal.getTitle())
                            && g.getAmountGoal() == deletedGoal.getAmountGoal()) {
                        goalList.remove(i);
                        adapter.notifyItemRemoved(i);
                        Toast.makeText(this, "Goal deleted", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        }
    }
}