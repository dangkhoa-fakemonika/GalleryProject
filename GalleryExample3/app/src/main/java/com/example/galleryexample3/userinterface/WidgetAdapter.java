package com.example.galleryexample3.userinterface;

import android.net.Uri;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.galleryexample3.R;

import java.io.File;
import java.util.ArrayList;

public class WidgetAdapter extends RecyclerView.Adapter<WidgetAdapter.WidgetViewHolder>{
    private ArrayList<String> imagesList;
    private SparseIntArray selectedImages = new SparseIntArray();
    private OnImageClickListener listener;
    private int selectedCount = 1;

    public interface OnImageClickListener {
        void onSelectionChanged(int selectedCount);
    }

    public WidgetAdapter(ArrayList<String> imagesList, OnImageClickListener listener) {
        this.imagesList = imagesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WidgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_item, parent, false);
        return new WidgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WidgetViewHolder holder, int position) {
        Glide.with(holder.widget_item.getContext())
                .load(imagesList.get(position))
                .into(holder.widget_item);

        holder.widget_checkbox.setChecked(selectedImages.get(position, 0) > 0);

        holder.widget_item.setOnClickListener(v -> {
            if (selectedImages.get(position, 0) > 0) {
                selectedImages.delete(position);
            } else {
                selectedImages.put(position, selectedCount);
                selectedCount += 1;
            }
            holder.widget_checkbox.setChecked(selectedImages.get(position, 0) > 0);
            listener.onSelectionChanged(selectedImages.size());
        });

        holder.widget_checkbox.setOnClickListener(v -> {
            if (selectedImages.get(position, 0) > 0) {
                selectedImages.delete(position);
            } else {
                selectedImages.put(position, selectedCount);
                selectedCount += 1;
            }
            holder.widget_checkbox.setChecked(selectedImages.get(position, 0) > 0);
            listener.onSelectionChanged(selectedImages.size());
        });
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    public ArrayList<String> getSelectedImages() {
        ArrayList<String> selected = new ArrayList<>();
        for (int i = 0; i < selectedImages.size(); i++) {
            int position = selectedImages.keyAt(i);
            try {
                selected.add(selectedImages.get(position) - 1, imagesList.get(position));
            } catch (IndexOutOfBoundsException e) {
                selected.add(imagesList.get(position));
            }
        }
        return selected;
    }

    class WidgetViewHolder extends RecyclerView.ViewHolder {
        ImageView widget_item;
        CheckBox widget_checkbox;

        public WidgetViewHolder(View itemView) {
            super(itemView);
            widget_item = itemView.findViewById(R.id.widget_item);
            widget_checkbox = itemView.findViewById(R.id.widget_checkbox);
        }
    }
}
