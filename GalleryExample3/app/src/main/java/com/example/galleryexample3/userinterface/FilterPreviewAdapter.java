package com.example.galleryexample3.userinterface;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.example.galleryexample3.R;

import java.util.ArrayList;

public class FilterPreviewAdapter extends RecyclerView.Adapter<FilterPreviewAdapter.FilterPreviewViewHolder> {
    private Context context;
    private ArrayList<FilterPreview> filterList;

    public FilterPreviewAdapter(Context context, ArrayList<FilterPreview> filterList) {
        this.context = context;
        this.filterList = filterList;
    }

    public FilterPreview getFilter(int position) {
        return filterList.get(position);
    }

    @NonNull
    @Override
    public FilterPreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.filter_preview, parent, false);
        return new FilterPreviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterPreviewViewHolder holder, int position) {
        FilterPreview filter = filterList.get(position);
        holder.filterName.setText(filter.filterName);
        Glide.with(context)
                .load(filter.bitmap)
                .centerCrop()
                .into(holder.previewImage);
    }

    @Override
    public int getItemCount() {
        return filterList.size();
    }

    public static class FilterPreviewViewHolder extends RecyclerView.ViewHolder {
        ImageView previewImage;
        TextView filterName;

        public FilterPreviewViewHolder(View itemView) {
            super(itemView);
            previewImage = itemView.findViewById(R.id.previewImage);
            filterName = itemView.findViewById(R.id.filterName);
        }
    }

    public static class FilterPreview {
        private String filterName;
        private Bitmap bitmap;

        public FilterPreview(String name, Bitmap imageUrl) {
            this.filterName = name;
            this.bitmap = imageUrl;
        }

        public String getFilterName() {
            return filterName;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }
    }
}