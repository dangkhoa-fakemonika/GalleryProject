package com.example.galleryexample3;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.businessclasses.PrivateAlbum;
import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.userinterface.GalleryImageGridAdapter;
import com.example.galleryexample3.userinterface.ItemClickSupporter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;

public class PrivateVaultActivity extends AppCompatActivity {
    private Toolbar myToolBar;
    private LinearLayout myLinearLayout;
    private RecyclerView myRecyclerView;
    private GalleryImageGridAdapter myAdapter;
    private ArrayList<String> imageList;
    private DatabaseHandler myDatabaseHandler;
    private Button cancleButton;
    private ImageButton deleteButton;
    private ImageButton removeFromPrivateButton;
    private TextView selectedImage;
    boolean selectMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.private_vault_layout);
        myToolBar = findViewById(R.id.myToolBar);
        myLinearLayout = findViewById(R.id.optionBars);
        myRecyclerView = findViewById(R.id.imageRecyclerView);
        cancleButton = findViewById(R.id.cancelSelectionButton);
        deleteButton = findViewById(R.id.deleteButton);
        selectedImage = findViewById(R.id.selectionTextView);
        removeFromPrivateButton = findViewById(R.id.removeFromPrivateButton);
        setSupportActionBar(myToolBar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        addMenuForAlbum();
        getOnBackPressedDispatcher().addCallback(backPressedCallback);

        myDatabaseHandler = DatabaseHandler.getInstance(this);
        imageList = PrivateAlbum.getImages(this, "DATE_ADDED", "DESC");
        myAdapter = new GalleryImageGridAdapter(this, imageList);
        Log.v(PrivateVaultActivity.class.toString(), String.valueOf(imageList.size()));
        myRecyclerView.setAdapter(myAdapter);
        myToolBar.setTitle("The Heaven");
        myToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        myLinearLayout.setVisibility(LinearLayout.GONE);
        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectMode = false;
                myAdapter.setSelectionMode(selectMode);
                myLinearLayout.setVisibility(View.GONE);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashSet<Integer> selectedImageList = myAdapter.getSelectedPositions();
                Toast.makeText(PrivateVaultActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                for(int pos : selectedImageList){
                    PrivateAlbum.deleteImage(PrivateVaultActivity.this, imageList.get(pos));
                }
            }
        });

        removeFromPrivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashSet<Integer> selectedImageList = myAdapter.getSelectedPositions();
                Toast.makeText(PrivateVaultActivity.this, "Removed", Toast.LENGTH_SHORT).show();
                for(int pos : selectedImageList){
                    PrivateAlbum.removeImage(PrivateVaultActivity.this, imageList.get(pos));
                }
            }
        });
        selectedImage = findViewById(R.id.selectionTextView);
        ItemClickSupporter.addTo(myRecyclerView).setOnItemClickListener(new ItemClickSupporter.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                if(selectMode){
                    myAdapter.toggleSelection(position);
                    int selectedImagesCount = myAdapter.getSelectedImagesCount();
                    if (selectedImagesCount != 0)
                        selectedImage.setText("Selected " + selectedImagesCount + " image" + (selectedImagesCount > 1 ? "s" : ""));
                    else
                        selectedImage.setText("Select image");
                }else{
                    Log.v("Private Position", String.valueOf(position));
                    Intent myIntent = new Intent(PrivateVaultActivity.this, SingleImageViewPrivate.class);
                    Bundle myBundle = new Bundle();
                    myBundle.putString("imageURI", imageList.get(position));
                    myBundle.putString("dateAdded", PrivateAlbum.getImageDateAdded(PrivateVaultActivity.this, imageList.get(position)));
                    myBundle.putInt("position", position);
                    myIntent.putExtras(myBundle);
                    startActivity(myIntent);
                }

            }
        });
        ItemClickSupporter.addTo(myRecyclerView).setOnItemLongClickListener(new ItemClickSupporter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
                if (!selectMode) {
                    selectMode = true;
                    myAdapter.setSelectionMode(selectMode);
                    myAdapter.toggleSelection(position);
                    selectedImage.setText("Selected 1 image");
                    myLinearLayout.setVisibility(View.VISIBLE);
                    myRecyclerView.scrollToPosition(position);
                    return true;
                }
                return false;
            }
        });

    }
    public void addMenuForAlbum(){
        addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.group_view_album_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.edit_button){
                    Intent myIntent = new Intent(PrivateVaultActivity.this, PrivateVaultCodeSettings.class);
                    myIntent.putExtra("settingMode", true);
                    startActivity(myIntent);
                    return true;
                }
                return false;
            }
        });
    }
}
