package com.example.galleryexample3.userinterface;

import android.content.Context;
import android.util.Log;
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

public class GalleryAlbumGridAdapter extends RecyclerView.Adapter<GalleryAlbumGridAdapter.GalleryAlbumViewHolder> {
    Context context;
    ArrayList<String> albumsList;
    ArrayList<String> albumThumbnailsList;
    HashSet<Integer> selectedPositions = new HashSet<>();
    boolean selectionEnabled = false;

    public GalleryAlbumGridAdapter(Context context, ArrayList<String> albumsList, ArrayList<String> albumThumbnailsList) {
        this.context = context;
        this.albumsList = albumsList;
        this.albumThumbnailsList = albumThumbnailsList;
    }

    public int getSelectedAlbumsCount() {
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

    public void updateDataList(ArrayList<String> newAlbumList, ArrayList<String> newThumbnailList){
        albumsList.clear();
        albumsList.addAll(newAlbumList);
        albumThumbnailsList = newThumbnailList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GalleryAlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_album_item, parent, false);
        return new GalleryAlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryAlbumViewHolder holder, int position) {
        holder.albumName.setText(albumsList.get(position));
//        Log.e("In BindView", String.valueOf(albumThumbnailsList.size()));
//        Log.e("In BindView", albumThumbnailsList.get(position));
        Glide.with(context)
                .load(albumThumbnailsList.get(position))
                .error(R.drawable.uoh)
                .centerCrop()
                .into(holder.thumbnailImage);

//        if (selectionEnabled && selectedPositions.contains(position)) { holder.setSelected(); }
//        else { holder.setUnselected(); }
    }

    @Override
    public int getItemCount() {
        return albumsList.size();
    }

    public static class GalleryAlbumViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImage;
        TextView albumName;

        public GalleryAlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.i("debug", "debug");
            this.thumbnailImage = itemView.findViewById(R.id.thumbnailImage);
            this.albumName = itemView.findViewById(R.id.albumName);
        }

//        public void setSelected() {
//            darkenLayout.setVisibility(View.VISIBLE);
//            selectionIndicator.setVisibility(View.VISIBLE);
//        }
//
//        public void setUnselected() {
//            darkenLayout.setVisibility(View.GONE);
//            selectionIndicator.setVisibility(View.GONE);
//        }
    }
}
