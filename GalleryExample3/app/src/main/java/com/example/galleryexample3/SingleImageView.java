package com.example.galleryexample3;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.galleryexample3.datamanagement.AlbumsController;
import com.example.galleryexample3.imageediting.ImageFilters;

public class SingleImageView extends Activity {
    private String imageURI;
    private EditText albumName;
    private EditText tagName;


    private AlbumsController albumsController;


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image_view);
        albumsController = new AlbumsController(this);

        ImageView imgView = (ImageView) findViewById(R.id.imageView);
        Button backButton = (Button) findViewById(R.id.backButton);
        Button addDummyAlbum = (Button) findViewById(R.id.addAlbum);
        albumName = (EditText) findViewById(R.id.albumNameAdd);
        Button addDummyTag = (Button) findViewById(R.id.addTagButton);
        tagName = (EditText) findViewById(R.id.tagAdd);
        Button filterButton = (Button) findViewById(R.id.filterButton);

        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();

        backButton.setOnClickListener((l) ->{
            Intent intent = new Intent(SingleImageView.this, MainActivity.class);
            startActivity(intent);
        });


        addDummyAlbum.setOnClickListener((l) -> {
            String albumNameGot = albumName.getText().toString();
            albumsController.addImageToAlbum(imageURI, albumNameGot);

            // Clearing and finishing
            albumName.setText("");
            Toast.makeText(this, "added " + imageURI + " to " + albumNameGot, Toast.LENGTH_SHORT).show();
        });

        filterButton.setOnClickListener((l) -> {
            Intent intent = new Intent(SingleImageView.this, ImageFilters.class);
            Bundle bundle = new Bundle();
            bundle.putString("imageURI", imageURI);
            intent.putExtras(bundle);
            startActivity(intent);
        });



        // Image null so won't load
        if (gotBundle == null)
            return;

        // Set image
        imageURI = gotBundle.getString("imageURI");
        Glide.with(this).load(imageURI)
                .placeholder(R.drawable.uoh).centerCrop()
                .into(imgView);


    }
}
