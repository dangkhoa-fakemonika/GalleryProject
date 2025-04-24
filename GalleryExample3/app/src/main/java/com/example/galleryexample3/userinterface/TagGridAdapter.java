package com.example.galleryexample3.userinterface;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.galleryexample3.R;
import com.example.galleryexample3.dataclasses.DatabaseHandler;

import java.util.ArrayList;

public class TagGridAdapter extends RecyclerView.Adapter<TagGridAdapter.TagGridViewHolder> {
    Context context;
    ArrayList<String> tagList;
    DatabaseHandler databaseHandler;

    public TagGridAdapter(Context context, ArrayList<String> tagList) {
        this.context = context;
        this.tagList = tagList;
        this.databaseHandler = DatabaseHandler.getInstance(context);
    }

    public String getTagName (int position) {
        return tagList.get(position);
    }

    public void updateDataList(ArrayList<String> tagList){
        this.tagList = tagList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TagGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tag_item_display, parent, false);
        return new TagGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagGridViewHolder holder, int position) {
//        Glide.with(context)
//                .load(tagList.get(position))
//                .centerCrop()
//                .into(holder.gridImage);
        int pos = position;
        holder.tagName.setText(tagList.get(pos));
        holder.deleteButton.setOnClickListener((l) -> {
            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setTitle("Delete tag \"" + tagList.get(pos) + "\"?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            databaseHandler.tags().deleteTag(tagList.get(pos));
                            Toast.makeText(context, "Deleted tag " + tagList.get(pos), Toast.LENGTH_LONG).show();
                            tagList = databaseHandler.tags().getAllTags();
                            notifyDataSetChanged();
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create();
            alertDialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

    public static class TagGridViewHolder extends RecyclerView.ViewHolder {
        public TextView tagName;
        public ImageButton deleteButton;

        public TagGridViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tagName = itemView.findViewById(R.id.tagName);
            this.deleteButton = itemView.findViewById(R.id.deleteTag);

//            this.gridImage = itemView.findViewById(R.id.gridImage);
//            this.darkenLayout = itemView.findViewById(R.id.darkenLayout);
//            this.selectionIndicator = itemView.findViewById(R.id.selectionIndicator);

        }
    }
}
