package com.example.bff_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.List;

public class GoalManager extends RecyclerView.Adapter<GoalManager.GoalViewHolder> {
    private List<Goal> goalList;

    public GoalManager(List<Goal> goals) {
        this.goalList = goals;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.saving_goal, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference goalsRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("savinggoals");
        Goal goal = goalList.get(position);
        holder.goalTitle.setText(goal.getTitle());
        holder.goalAmount.setText("Goal: $" + goal.getAmountGoal());
        holder.amountSaved.setText("Saved: $" + goal.getAmountSaved());

        holder.editGoalButton.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, EditGoalActivity.class);
            intent.putExtra("goal", (Serializable) goalList.get(holder.getAdapterPosition()));
            intent.putExtra("position", holder.getAdapterPosition());
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, 1); // edit request
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, GoalDetailActivity.class);
            intent.putExtra("goal", (Serializable) goalList.get(holder.getAdapterPosition()));

            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, 2);
            }
        });
    }

    @Override
    public int getItemCount() {
        return goalList.size();
    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView goalTitle, goalAmount, amountSaved;
        Button editGoalButton;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            goalTitle = itemView.findViewById(R.id.goalTitle);
            goalAmount = itemView.findViewById(R.id.goalAmount);
            amountSaved = itemView.findViewById(R.id.amountSaved);
            editGoalButton = itemView.findViewById(R.id.editGoalButton);
        }
    }
}