package com.example.galleryexample3;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class PrivateVaultActivity extends AppCompatActivity {
    private Toolbar myToolBar;
    private LinearLayout myLinearLayout;
    private RecyclerView myRecyclerView;
    private GalleryImageGridAdapter myAdapter;
    private ArrayList<String> imageList;
    private DatabaseHandler myDatabaseHandler;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.private_vault_layout);
        myToolBar = findViewById(R.id.myToolBar);
        myLinearLayout = findViewById(R.id.optionBars);
        myRecyclerView = findViewById(R.id.imageRecyclerView);

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

        ItemClickSupporter.addTo(myRecyclerView).setOnItemClickListener(new ItemClickSupporter.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Intent myIntent = new Intent(PrivateVaultActivity.this, SingleImageViewPrivate.class);
                Bundle myBundle = new Bundle();
                myBundle.putString("imageURI", imageList.get(position));
                myBundle.putString("dateAdded", PrivateAlbum.getImageDateAdded(PrivateVaultActivity.this, imageList.get(position)));
                myBundle.putInt("position", position);
                myIntent.putExtras(myBundle);
                startActivity(myIntent);
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
