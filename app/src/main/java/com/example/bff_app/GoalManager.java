package com.example.bff_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.res.ColorStateList;
import android.text.Html;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class GoalManager extends RecyclerView.Adapter<GoalManager.GoalViewHolder> {
    private List<Goal> goalList;
    private Context context;
    private OnEditClickListener editClickListener;
    private OnUpdateSavedClickListener updateSavedClickListener;


    public GoalManager(Context context, List<Goal> goals, OnEditClickListener editListener, OnUpdateSavedClickListener updateListener) {
        this.context = context;
        this.goalList = goals;
        this.editClickListener = editListener;
        this.updateSavedClickListener = updateListener;
    }

    public interface OnEditClickListener {
        void onEditClick(Goal goal, int position);
    }

    public interface OnUpdateSavedClickListener {
        void onUpdateSavedClick(Goal goal, int position);
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

        double saved = goal.getAmountSaved();
        double total = goal.getAmountGoal();
        double percent = total > 0 ? (saved / total) * 100 : 0;

        holder.goalTitle.setText(goal.getTitle());

        holder.goalDate.setText("Goal Date: " + goal.getTargetDate());

        String amountText;

        if (total == 0) {
            amountText = "Amount Saved: $" + saved + " / $" + total + "  ⚠️ Goal amount is $0";
        } else {
            amountText = "Amount Saved: $" + saved + " / $" + total;
        }

        holder.amountProgressText.setText(amountText);
        holder.amountProgressText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0); // clear icon if it was set before

        holder.progressBar.setProgress((int) percent);

        if (total == 0 || percent > 100) {
            // Goal amount is 0 or goal exceeded – treat as warning
            holder.progressBar.setProgressTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.budgetRed))
            );
        } else {
            // Normal progress or completed goal
            holder.progressBar.setProgressTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.forestGreen))
            );
        }

        // Set consistent background track color
        holder.progressBar.setProgressBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.soft))
        );


        holder.editGoalButton.setOnClickListener(v -> {
            if (editClickListener != null) {
                editClickListener.onEditClick(goalList.get(holder.getAdapterPosition()), holder.getAdapterPosition());
            }
        });

        holder.updateSavedButton.setOnClickListener(v -> {
            if (updateSavedClickListener != null) {
                updateSavedClickListener.onUpdateSavedClick(goalList.get(holder.getAdapterPosition()), holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, GoalDetailActivity.class);
            Goal selectedGoal = goalList.get(holder.getAdapterPosition());
            intent.putExtra("goal", selectedGoal);

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
        TextView goalTitle, goalDate, amountProgressText;
        ProgressBar progressBar;
        Button editGoalButton, updateSavedButton;


        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            goalTitle = itemView.findViewById(R.id.goalTitle);
            goalDate = itemView.findViewById(R.id.goalDate);
            amountProgressText = itemView.findViewById(R.id.amountProgressText);
            progressBar = itemView.findViewById(R.id.progressBar);
            editGoalButton = itemView.findViewById(R.id.editGoalButton);
            updateSavedButton = itemView.findViewById(R.id.updateSavedButton);

        }
    }
}