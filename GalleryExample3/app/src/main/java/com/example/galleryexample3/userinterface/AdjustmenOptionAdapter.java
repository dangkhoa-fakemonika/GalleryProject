package com.example.galleryexample3.userinterface;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.R;
import com.example.galleryexample3.dataclasses.AdjustmentOption;
import com.example.galleryexample3.dataclasses.FilterPreview;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AdjustmenOptionAdapter extends RecyclerView.Adapter<AdjustmenOptionAdapter.AdjustmentOptionViewHolder> {
    private Context context;
    private ArrayList<AdjustmentOption> adjustmentList;

    public AdjustmenOptionAdapter(Context context, ArrayList<AdjustmentOption> adjustmentList) {
        this.context = context;
        this.adjustmentList = adjustmentList;
    }

    public void updateValue(int position, float value) {
        adjustmentList.get(position).setValue(value);
        notifyItemChanged(position);
    }

    public void replaceOptionList(ArrayList<AdjustmentOption> adjustmentList) {
        this.adjustmentList = adjustmentList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdjustmentOptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adjustment_option, parent, false);
        return new AdjustmenOptionAdapter.AdjustmentOptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdjustmentOptionViewHolder holder, int position) {
        AdjustmentOption adjustmentOption = adjustmentList.get(position);
        holder.optionName.setText(adjustmentOption.getName());
        holder.icon.setImageResource(adjustmentOption.getIconResourceId());
        float value = adjustmentOption.getValue();
        float defaultValue = adjustmentOption.getDefaultValue();
        if (value != defaultValue) { holder.showValue(value); }
        else { holder.showIcon(); }
    }

    @Override
    public int getItemCount() {
        return adjustmentList.size();
    }

    public static class AdjustmentOptionViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView value;
        TextView optionName;

        public AdjustmentOptionViewHolder(View itemView) {
            super(itemView);
            this.icon = itemView.findViewById(R.id.icon);
            this.value = itemView.findViewById(R.id.value);
            this.optionName = itemView.findViewById(R.id.optionName);
        }

        public void showValue(float value) {
            this.value.setText(String.valueOf(value));
            this.icon.setVisibility(View.GONE);
            this.value.setVisibility(View.VISIBLE);
        }

        public void showIcon() {
            this.icon.setVisibility(View.VISIBLE);
            this.value.setVisibility(View.GONE);
        }
    }
}
