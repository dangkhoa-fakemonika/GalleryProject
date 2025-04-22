package com.example.galleryexample3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.userinterface.GalleryImageGridAdapter;
import com.example.galleryexample3.userinterface.ItemClickSupporter;
import com.example.galleryexample3.userinterface.SearchItemListAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class GroupImageView extends AppCompatActivity {
    String[] flagsForSingleView = {SingleImageView.FLAG_SEARCH_NAME, SingleImageView.FLAG_ALBUM, SingleImageView.FLAG_TAG};
    String[] groupTypeTitles = {"Name:", "Album:", "Tag:"};
    public static String BUKEY_GROUP_TYPE = "groupType";
    public static String BUKEY_GROUP_NAME = "groupName";
    public static String BUKEY_GROUP_COUNT = "groupCount";
    int groupType;
    Toolbar myToolbar;
    String groupName;
    int groupItemCounts;
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
        groupType = gotBundle.getInt(BUKEY_GROUP_TYPE, 0);
        groupName = gotBundle.getString(BUKEY_GROUP_NAME);
        groupItemCounts = gotBundle.getInt(BUKEY_GROUP_COUNT, 0);

        RecyclerView gridRecyclerView = (RecyclerView) findViewById(R.id.gridRecyclerView);

        LinearLayout optionBars = (LinearLayout) findViewById(R.id.optionBars);
        Button cancelSelectionButton = (Button) findViewById(R.id.cancelSelectionButton);
        TextView selectionTextView = (TextView) findViewById(R.id.selectionTextView);
        ImageButton deleteButton = (ImageButton) findViewById(R.id.deleteButton);
        myToolbar = (Toolbar) findViewById(R.id.myToolBar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(groupTypeTitles[groupType] + " " + groupName);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        setImageList();
        addMenuToToolBarForAlbum();
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
                    bundle.putString(flagsForSingleView[groupType], groupName);
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
    public void setImageList(){
        if (groupType == SearchItemListAdapter.MATCH_IMAGE_NAME){
            imagesList = ImageGalleryProcessing.getImagesByName(this, groupName, "DATE_ADDED", "ASC");
        } else if (groupType == SearchItemListAdapter.MATCH_ALBUM) {
            imagesList = databaseHandler.albums().getImagesOfAlbum(groupName);
        } else if (groupType == SearchItemListAdapter.MATCH_TAG) {
//            No logic for tags yet
            imagesList = ImageGalleryProcessing.getImagesByName(this, groupName, "DATE_ADDED", "ASC");

        }
    }
    public void addMenuToToolBarForAlbum(){
        if (groupType == SearchItemListAdapter.MATCH_ALBUM){
            addMenuProvider(new MenuProvider() {
                @Override
                public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                    menuInflater.inflate(R.menu.group_view_album_menu, menu);
                }

                @Override
                public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                    int id = menuItem.getItemId();
                    if (id == R.id.edit_button){
                        return true;
                    }
                    return false;
                }
            });
        }

    }
}
