package com.example.galleryexample3.userinterface;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.galleryexample3.R;

import java.util.ArrayList;
import java.util.HashSet;

public class GalleryImageGridAdapter extends RecyclerView.Adapter<GalleryImageGridAdapter.GalleryImageViewHolder> {
    Context context;
    ArrayList<String> imagesList;
    HashSet<Integer> selectedPositions = new HashSet<>();
    boolean selectionEnabled = false;

    public GalleryImageGridAdapter(Context context, ArrayList<String> imagesList) {
        this.context = context;
        this.imagesList = imagesList;
    }

    public String getImage (int position) {
        return imagesList.get(position);
    }

    public int getSelectedImagesCount() {
        return selectedPositions.size();
    }

    public void toggleSelection(int position) {
        if (selectedPositions.contains(position)) { selectedPositions.remove(position); }
        else { selectedPositions.add(position); }
        notifyItemChanged(position);
    }

    public void setSelectionMode(boolean selectionEnabled) {
        this.selectionEnabled = selectionEnabled;
        if (!selectionEnabled) {
            for (int position : selectedPositions)
                notifyItemChanged(position);
            selectedPositions.clear();
        }
    }

//    public ArrayList<String> getSelectedItems() {
//        ArrayList<String> selectedImages = new ArrayList<>();
//        for (int pos : selectedPositions) {
//            selectedImages.add(imagesList.get(pos));
//        }
//        return selectedImages;
//    }

    @NonNull
    @Override
    public GalleryImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_image_item, parent, false);
        return new GalleryImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryImageViewHolder holder, int position) {
        Glide.with(context)
                .load(imagesList.get(position))
                .centerCrop()
                .into(holder.gridImage);

        if (selectionEnabled && selectedPositions.contains(position)) { holder.setSelected(); }
        else { holder.setUnselected(); }
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    public static class GalleryImageViewHolder extends RecyclerView.ViewHolder {
        ImageView gridImage;
        View darkenLayout;
        TextView selectionIndicator;

        public GalleryImageViewHolder(@NonNull View itemView) {
            super(itemView);
            this.gridImage = itemView.findViewById(R.id.gridImage);
            this.darkenLayout = itemView.findViewById(R.id.darkenLayout);
            this.selectionIndicator = itemView.findViewById(R.id.selectionIndicator);
        }

        public void setSelected() {
            darkenLayout.setVisibility(View.VISIBLE);
            selectionIndicator.setVisibility(View.VISIBLE);
        }

        public void setUnselected() {
            darkenLayout.setVisibility(View.GONE);
            selectionIndicator.setVisibility(View.GONE);
        }
    }
}