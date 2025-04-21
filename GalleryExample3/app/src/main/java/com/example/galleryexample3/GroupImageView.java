package com.example.galleryexample3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.userinterface.GalleryImageGridAdapter;
import com.example.galleryexample3.userinterface.ItemClickSupporter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class GroupImageView extends Activity {
    String groupType;
    String groupName;
    ArrayList<String> imagesList;
    DatabaseHandler databaseHandler = DatabaseHandler.getInstance(this);
    boolean selectionEnabled = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_image_view);

        Context context = this;

        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();
        groupType = gotBundle.getString("groupType");
        groupName = gotBundle.getString("groupName");
        imagesList = databaseHandler.albums().getImagesOfAlbum(groupName);

        LinearLayout topBar = (LinearLayout) findViewById(R.id.topBar);
        TextView groupTitle = (TextView) findViewById(R.id.groupTitle);
        ImageButton albumEditButton = (ImageButton) findViewById(R.id.albumEditButton);

        RecyclerView gridRecyclerView = (RecyclerView) findViewById(R.id.gridRecyclerView);

        LinearLayout optionBars = (LinearLayout) findViewById(R.id.optionBars);
        Button cancelSelectionButton = (Button) findViewById(R.id.cancelSelectionButton);
        TextView selectionTextView = (TextView) findViewById(R.id.selectionTextView);
        ImageButton deleteButton = (ImageButton) findViewById(R.id.deleteButton);

        GalleryImageGridAdapter galleryAdapter = new GalleryImageGridAdapter(this, imagesList);
        gridRecyclerView.setAdapter(galleryAdapter);

        cancelSelectionButton.setOnClickListener(v -> {
            selectionEnabled = false;
            galleryAdapter.setSelectionMode(selectionEnabled);
            optionBars.setVisibility(View.GONE);
        });

        ItemClickSupporter.addTo(gridRecyclerView).setOnItemClickListener(new ItemClickSupporter.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                if (selectionEnabled) {
                    galleryAdapter.toggleSelection(position);
                    int selectedImagesCount = galleryAdapter.getSelectedImagesCount();
                    if (selectedImagesCount != 0)
                        selectionTextView.setText("Selected " + selectedImagesCount + " image" + (selectedImagesCount > 1 ? "s" : ""));
                    else
                        selectionTextView.setText("Select image");
                } else {
                    Log.e("Position", String.valueOf(position));
                    String imageUri = imagesList.get(position);
                    String dateAdded = ImageGalleryProcessing.getImageDateAdded(context, imageUri);
                    Intent intent = new Intent(context, SingleImageView.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("imageURI", imageUri);
                    bundle.putString("dateAdded", dateAdded);
                    bundle.putInt("position", position);
                    bundle.putString(SingleImageView.FLAG_ALBUM, groupName);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });

        ItemClickSupporter.addTo(gridRecyclerView).setOnItemLongClickListener(new ItemClickSupporter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
                if (!selectionEnabled) {
                    selectionEnabled = true;
                    galleryAdapter.setSelectionMode(selectionEnabled);
                    galleryAdapter.toggleSelection(position);
                    selectionTextView.setText("Selected 1 image");
                    optionBars.setVisibility(View.VISIBLE);
                    gridRecyclerView.scrollToPosition(position);
                }
                return true;
            }
        });
    }
}
