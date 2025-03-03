package com.example.galleryexample3;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class SingleImageView extends Activity {
    private ImageView imgView;
    private Button backButton;
    private String imageURI;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image_view);

        imgView = (ImageView) findViewById(R.id.imageView);
        backButton = (Button) findViewById(R.id.backButton);

        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();

        backButton.setOnClickListener((l) ->{
            Intent intent = new Intent(SingleImageView.this, MainActivity.class);
            startActivity(intent);
        });

        if (gotBundle == null)
            return;

        imageURI = gotBundle.getString("imageURI");
        imgView.setImageURI(Uri.parse(imageURI));
    }
}
