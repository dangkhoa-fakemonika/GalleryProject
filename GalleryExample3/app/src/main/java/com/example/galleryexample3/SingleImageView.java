package com.example.galleryexample3;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image_view);

        SharedPreferences albums = getSharedPreferences("dummy", Activity.MODE_PRIVATE);
        if (albums != null && !albums.contains("dummy")){
            Log.i("SP check", "bad");
            SharedPreferences.Editor editor = albums.edit();
            editor.putStringSet("dummy", new HashSet<>());
            editor.apply();
        }
        else {
            Log.i("SP check", "good");
        }

        imgView = (ImageView) findViewById(R.id.imageView);
        backButton = (Button) findViewById(R.id.backButton);
        addDummyAlbum = (Button) findViewById(R.id.addAlbum);

        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();

        backButton.setOnClickListener((l) ->{
            Intent intent = new Intent(SingleImageView.this, MainActivity.class);
            startActivity(intent);
        });

        addDummyAlbum.setOnClickListener((l) -> {
            if (albums == null)
                return;
            SharedPreferences.Editor editor2 = albums.edit();
            HashSet<String> hashSet = new HashSet<>(Objects.requireNonNull(albums.getStringSet("dummy", null)));
            hashSet.add(imageURI);
            editor2.putStringSet("dummy", hashSet);
            hashSet.forEach((i) -> {Log.i("SP check", i);});

            editor2.apply();
            Toast.makeText(this, "added " + imageURI + " to dummy", Toast.LENGTH_SHORT).show();
        });

        if (gotBundle == null)
            return;

        imageURI = gotBundle.getString("imageURI");
        imgView.setImageURI(Uri.parse(imageURI));
    }
}
