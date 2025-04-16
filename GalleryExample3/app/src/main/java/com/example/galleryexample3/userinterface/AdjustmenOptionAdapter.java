package com.example.galleryexample3.userinterface;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.R;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AdjustmenOptionAdapter extends RecyclerView.Adapter<AdjustmenOptionAdapter.AdjustmentOptionViewHolder> {
    private Context context;
    private ArrayList<String> adjustmentList;

    public AdjustmenOptionAdapter(Context context, ArrayList<String> adjustmentList) {
        this.context = context;
        this.adjustmentList = adjustmentList;
    }

    public String getAdjustment(int position) {
        return adjustmentList.get(position);
    }

    @NonNull
    @Override
    public AdjustmentOptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adjustment_option, parent, false);
        return new AdjustmenOptionAdapter.AdjustmentOptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdjustmentOptionViewHolder holder, int position) {
        holder.optionName.setText(adjustmentList.get(position));
    }

    @Override
    public int getItemCount() {
        return adjustmentList.size();
    }

    public static class AdjustmentOptionViewHolder extends RecyclerView.ViewHolder {
        TextView optionName;

        public AdjustmentOptionViewHolder(View itemView) {
            super(itemView);
            this.optionName = itemView.findViewById(R.id.optionName);
        }
    }
}
