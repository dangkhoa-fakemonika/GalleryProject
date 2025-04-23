package com.example.galleryexample3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashSet;

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
        ImageButton moreOptionButton = (ImageButton) findViewById(R.id.deleteButton);
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

        myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.edit_button){
                    Intent intent = new Intent(GroupImageView.this, MoreAlbumInformationActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt(GroupImageView.BUKEY_GROUP_TYPE, SearchItemListAdapter.MATCH_ALBUM);
                    bundle.putString(GroupImageView.BUKEY_GROUP_NAME, groupName);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                return false;
            }
        });

        cancelSelectionButton.setOnClickListener(v -> {
            selectionEnabled = false;
            galleryAdapter.setSelectionMode(selectionEnabled);
            optionBars.setVisibility(View.GONE);
        });

        moreOptionButton.setOnClickListener((l) -> {
            HashSet<Integer> positions = galleryAdapter.getSelectedPositions();
            PopupMenu popup = new PopupMenu(this, moreOptionButton, Gravity.END);
            popup.inflate(R.menu.delete_selection_menu);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    int id = menuItem.getItemId();
                    if (id == R.id.removeSelection){
                        if (galleryAdapter.getSelectedImagesCount() == 0){
                            Toast.makeText(context, "Select an image first", Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setTitle("Remove these from the album?")
                                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        HashSet<Integer> positions = galleryAdapter.getSelectedPositions();
                                        positions.forEach((pos) -> {
//                                            ImageGalleryProcessing.deleteImage(context, imagesList.get(pos));
//                                            databaseHandler.tags().deleteImage(imagesList.get(pos));
                                            databaseHandler.albums().removeImageFromAlbum(groupName, imagesList.get(pos));
                                        });
                                        Toast.makeText(context, "All images removed from album.", Toast.LENGTH_LONG).show();
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
                    }
                    else if (id == R.id.deleteSelection){
                        if (galleryAdapter.getSelectedImagesCount() == 0){
                            Toast.makeText(context, "Select an image first", Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setTitle("Delete all selected images?")
                                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        HashSet<Integer> positions = galleryAdapter.getSelectedPositions();
                                        positions.forEach((pos) -> {
                                            ImageGalleryProcessing.deleteImage(context, imagesList.get(pos));
                                            databaseHandler.tags().deleteImage(imagesList.get(pos));
                                            databaseHandler.albums().deleteImage(imagesList.get(pos));
                                        });
                                        Toast.makeText(context, "All images deleted.", Toast.LENGTH_LONG).show();
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
                    }
                    else if (id == R.id.addTagSelection) {
                        View dialogView = LayoutInflater.from(context).inflate(R.layout.one_field_dialog_layout, null);
                        TextInputLayout inputTextLayout = dialogView.findViewById(R.id.inputTextLayout);
                        TextInputEditText editText = dialogView.findViewById(R.id.editText);
                        inputTextLayout.setHint("Enter Tag Name");
                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setTitle("Add Tag")
                                .setView(dialogView)
                                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        String tagName = editText.getText().toString();
                                        HashSet<Integer> positions = galleryAdapter.getSelectedPositions();
                                        positions.forEach((pos) -> {
                                            databaseHandler.tags().addTagsToImage(tagName, imagesList.get(pos));
                                        });
                                        Toast.makeText(context, "Added to " + tagName, Toast.LENGTH_LONG).show();
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
                    }
                    return false;
                }
            });
            popup.show();
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
