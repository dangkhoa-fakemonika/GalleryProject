package com.example.galleryexample3;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.Objects;

public class SingleImageView extends Activity {
    private ImageView imgView;
    private Button backButton;
    private String imageURI;
    private Button addDummyAlbum;
    private EditText albumName;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image_view);

        imgView = (ImageView) findViewById(R.id.imageView);
        backButton = (Button) findViewById(R.id.backButton);
        addDummyAlbum = (Button) findViewById(R.id.addAlbum);
        albumName = (EditText) findViewById(R.id.albumNameAdd);

        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();

        backButton.setOnClickListener((l) ->{
            Intent intent = new Intent(SingleImageView.this, MainActivity.class);
            startActivity(intent);
        });



        SharedPreferences collectiveData = getSharedPreferences("collective_data", Activity.MODE_PRIVATE);
        if (collectiveData != null && !collectiveData.contains("albums_list")){
            Log.i("SP check", "bad");
            SharedPreferences.Editor editor = collectiveData.edit();
            HashSet<String> test = new HashSet<>();
            editor.putStringSet("albums_list", test);

            editor.apply();

        }
        else {
            Log.i("SP check", "good");
        }

        addDummyAlbum.setOnClickListener((l) -> {

            String albumNameGot = albumName.getText().toString();

            // Find the list of albums
            SharedPreferences albumSet = getSharedPreferences("collective_data", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = albumSet.edit();
            HashSet<String> temp;

            if (albumSet.contains("albums_list")){
                temp = new HashSet<>(Objects.requireNonNull(albumSet.getStringSet("albums_list", null)));
            }
            else {
                temp = new HashSet<>();
            }

            temp.add(albumNameGot);
            editor.putStringSet("albums_list", temp);
            editor.apply();

            SharedPreferences albums = getSharedPreferences("dummy", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor2 = albums.edit();
            HashSet<String> hashSet;

            // Find list of images in the albums
            if (albums.contains(albumNameGot)){
                hashSet = new HashSet<>(Objects.requireNonNull(albums.getStringSet(albumNameGot, null)));
            }
            else {
                hashSet = new HashSet<>();
            }

            hashSet.add(imageURI);
            editor2.putStringSet(albumNameGot, hashSet);
            editor2.apply();

            albumName.setText("");
            Toast.makeText(this, "added " + imageURI + " to " + albumNameGot, Toast.LENGTH_SHORT).show();
        });

        if (gotBundle == null)
            return;

        imageURI = gotBundle.getString("imageURI");
        imgView.setImageURI(Uri.parse(imageURI));
    }
}
