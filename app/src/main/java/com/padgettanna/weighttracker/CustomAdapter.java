package com.padgettanna.weighttracker;

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

import java.util.ArrayList;

/**
 * Recycler view adapter for displaying weight log entries
 * - Binds database data to list rows
 * - Handles clicks on rows to launch Update/Delete screen
 */
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {
    // Context fro launching activity
    private Context context;
    // Reference to parent activity
    Activity activity;
    // Data lists to store log entry information
    private ArrayList entryId, entryDate, entryValue, userEmail;

    // Constructor - initialize context, activity, and data lists
    CustomAdapter(Activity activity, Context context, ArrayList entryId, ArrayList entryDate,
                  ArrayList entryValue, ArrayList userEmail) {
        this.activity = activity;
        this.context = context;
        this.entryId = entryId;
        this.entryDate = entryDate;
        this.entryValue = entryValue;
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
        // Bind date and weight values to text fields
        holder.textEntryDate.setText(String.valueOf(entryDate.get(position)));
        holder.textEntryValue.setText(String.valueOf(entryValue.get(position)));
        // Click listener for each row to open Update/Delete screen
        holder.updateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch Update/Delete activity, passing selected entry data
                Intent intent = new Intent(context, UpdateDeleteActivity.class);
                intent.putExtra("id", Integer.parseInt(entryId.get(position).toString()));
                intent.putExtra("date", String.valueOf(entryDate.get(position)));
                intent.putExtra("weight",  Integer.parseInt(entryValue.get(position).toString()));
                intent.putExtra("USER_EMAIL", String.valueOf(userEmail.get(position)));
                activity.startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entryId.size();
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
