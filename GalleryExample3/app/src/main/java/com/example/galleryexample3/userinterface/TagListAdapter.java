package com.example.galleryexample3.userinterface;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.R;
import com.example.galleryexample3.dataclasses.DatabaseHandler;

import java.util.ArrayList;

public class TagListAdapter extends RecyclerView.Adapter<TagListAdapter.ViewHolder> {
    private ArrayList<String> localDataSet;
    private String imageUri;
    DatabaseHandler databaseHandler;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageButton imageButton;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textView = (TextView) view.findViewById(R.id.tagName);
            imageButton = (ImageButton) view.findViewById(R.id.removeTag);
        }

        public TextView getTextView() {
            return textView;
        }

        public ImageButton getImageButton(){
            return imageButton;
        }

    }

    public TagListAdapter(Context context, ArrayList<String> dataSet, String imageUri) {
        localDataSet = dataSet;
        this.imageUri = imageUri;
        databaseHandler = new DatabaseHandler(context);
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tag_element, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getTextView().setText(localDataSet.get(position));
        viewHolder.getImageButton().setOnClickListener((listener) -> {
            databaseHandler.tags().removeTag(localDataSet.get(position), imageUri);
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
