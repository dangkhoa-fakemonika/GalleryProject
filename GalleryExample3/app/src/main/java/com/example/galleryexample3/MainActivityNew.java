package com.example.galleryexample3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.userinterface.GalleryImageGridAdapter;
import com.example.galleryexample3.userinterface.ItemClickSupporter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.ArrayList;

public class MainActivityNew extends Activity implements PopupMenu.OnMenuItemClickListener {
    private ArrayList<String> imagesList;
    private Context context;
    boolean selectionEnabled = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        context = this;
        imagesList = ImageGalleryProcessing.getImages(this, "DATE_ADDED", " ASC");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        Button cancelSelectionButton = (Button) findViewById(R.id.cancelSelectionButton);
        ImageButton deleteButton = (ImageButton) findViewById(R.id.deleteButton);
        ImageButton moreOptionButton = (ImageButton) findViewById(R.id.moreOptionButton);
        TextView selectionTextView = (TextView) findViewById(R.id.selectionTextView);
        LinearLayout optionBars = (LinearLayout) findViewById(R.id.optionBars);
        BottomNavigationView bottomNavView = (BottomNavigationView) findViewById(R.id.bottomNavView);
        RecyclerView gridRecyclerView = findViewById(R.id.gridRecyclerView);
        GridLayoutManager gridLayoutManager = (GridLayoutManager) gridRecyclerView.getLayoutManager();
        GalleryImageGridAdapter galleryAdapter = new GalleryImageGridAdapter(this, imagesList);
        gridRecyclerView.setAdapter(galleryAdapter);
        gridRecyclerView.scrollToPosition(imagesList.size() - 1);

        moreOptionButton.setOnClickListener(this::showMenu);

        cancelSelectionButton.setOnClickListener(v -> {
            selectionEnabled = false;
            galleryAdapter.setSelectionMode(selectionEnabled, -1);
            optionBars.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) gridRecyclerView.getLayoutParams();
            params.addRule(RelativeLayout.ABOVE, R.id.bottomNavView);
            bottomNavView.setVisibility(View.VISIBLE);
        });

        // Select image or enter details view
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
                    String imageUri = imagesList.get(position);
                    String dateAdded = ImageGalleryProcessing.getImageDateAdded(context, Uri.parse(imageUri));
                    Intent intent = new Intent(MainActivityNew.this, SingleImageView.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("imageURI", imageUri);
                    bundle.putString("dateAdded", dateAdded);
                    bundle.putInt("position", position);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });

        // Enter multiple selection mode
        ItemClickSupporter.addTo(gridRecyclerView).setOnItemLongClickListener(new ItemClickSupporter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
                if (!selectionEnabled) {
                    selectionEnabled = true;
                    galleryAdapter.setSelectionMode(selectionEnabled, position);
                    optionBars.setVisibility(View.VISIBLE);
                    bottomNavView.setVisibility(View.GONE);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) gridRecyclerView.getLayoutParams();
                    params.addRule(RelativeLayout.ABOVE, R.id.optionBars);
                    gridRecyclerView.scrollToPosition(position);
                }
                return true;
            }
        });
    }

    private void showMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.main_gallery_menu);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.addAlbum){
            View dialogView = LayoutInflater.from(MainActivityNew.this).inflate(R.layout.one_field_dialog_layout, null);
            TextInputLayout inputTextLayout = dialogView.findViewById(R.id.inputTextLayout);
            TextInputEditText editText = dialogView.findViewById(R.id.editText);
            inputTextLayout.setHint("Enter Album Name");
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Add Album")
                    .setView(dialogView)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String albumName = editText.getText().toString();

                            Toast.makeText(context, "Added to " + albumName, Toast.LENGTH_LONG).show();
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
            return true;
        }
        else if (id == R.id.addTag) {
            View dialogView = LayoutInflater.from(MainActivityNew.this).inflate(R.layout.one_field_dialog_layout, null);
            TextInputLayout inputTextLayout = dialogView.findViewById(R.id.inputTextLayout);
            TextInputEditText editText = dialogView.findViewById(R.id.editText);
            inputTextLayout.setHint("Enter Tag Name");
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Add Tag")
                    .setView(dialogView)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String tagName = editText.getText().toString();

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
            return true;
        } else
            return false;
    }
}
