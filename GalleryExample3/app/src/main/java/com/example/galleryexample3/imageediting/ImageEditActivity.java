package com.example.galleryexample3.imageediting;

import static com.example.galleryexample3.businessclasses.ImageFiltersProcessing.adjustTemperature;
import static com.example.galleryexample3.businessclasses.ImageFiltersProcessing.applyNegative;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.galleryexample3.R;
import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;


public class ImageEditActivity extends Activity {

    ImageView imageView;
    Bitmap imageBitmap;
    Bitmap displayBitmap;

    String imageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image_edit);

        imageView = (ImageView) findViewById(R.id.imageView);
        Button normalButton = (Button) findViewById(R.id.normalButton);
        Button grayscaleButton = (Button) findViewById(R.id.grayscaleButton);
        Button sepiaButton = (Button) findViewById(R.id.sepiaButton);
        Button saveButton = (Button) findViewById(R.id.saveButton);

        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();

        normalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayBitmap = imageBitmap;
                imageView.setImageBitmap(displayBitmap);
            }
        });

        grayscaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayBitmap = applyNegative(imageBitmap);
                imageView.setImageBitmap(displayBitmap);
            }
        });

        sepiaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayBitmap = adjustTemperature(imageBitmap);
                imageView.setImageBitmap(displayBitmap);
            }
        });

        saveButton.setOnClickListener((l) -> {
            ImageGalleryProcessing.saveImage(this, displayBitmap);
        });



        if (gotBundle == null)
            return;

        imageURI = gotBundle.getString("imageURI");

//        imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample);
        imageBitmap = BitmapFactory.decodeFile(imageURI);
        displayBitmap = imageBitmap;

        imageView.setImageBitmap(displayBitmap);
    }
}
