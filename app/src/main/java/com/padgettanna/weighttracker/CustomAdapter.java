package com.padgettanna.weighttracker;

import com.padgettanna.weighttracker.model.WeightEntry;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Recycler view adapter for displaying weight log entries
 * - Uses structured WeightEntry objects to support algorithmic processing.
 * - Handles clicks on rows to launch Update/Delete screen
 */
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {
    // Data list to store weightEntry objects
    private List<WeightEntry> weightEntries;
    // Context for launching activity
    private Context context;
    private String userEmail;
    // Reference to parent activity
    Activity activity;

    // Constructor - initialize context, activity, and WeightEntries
    CustomAdapter(Activity activity, Context context, List<WeightEntry> weightEntries, String userEmail) {
        this.activity = activity;
        this.context = context;
        this.weightEntries = weightEntries;
        this.userEmail = userEmail;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout for each row
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.wt_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {

        WeightEntry entry = weightEntries.get(position);
        // Bind date and weight values to text fields
        holder.textEntryDate.setText(entry.getDate().toString());
        holder.textEntryValue.setText(String.valueOf(entry.getWeight()));

        // Click listener for each row to open Update/Delete screen
        holder.updateLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, UpdateDeleteActivity.class);
            intent.putExtra("id", entry.getId());
            intent.putExtra("date", entry.getDate().toString());
            intent.putExtra("weight", entry.getWeight());
            intent.putExtra("USER_EMAIL", userEmail);
            activity.startActivityForResult(intent, 1);
        });
    }

    @Override
    public int getItemCount() {

        return weightEntries == null ? 0 : weightEntries.size();
    }

    // Hold references to views in each row
    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textEntryDate, textEntryValue;
        LinearLayout updateLayout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textEntryDate = itemView.findViewById(R.id.textEntryDate);
            textEntryValue = itemView.findViewById(R.id.textEntryValue);
            updateLayout = itemView.findViewById(R.id.updateLayout);
        }
    }
}
