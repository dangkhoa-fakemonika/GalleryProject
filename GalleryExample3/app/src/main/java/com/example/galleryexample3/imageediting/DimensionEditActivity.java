package com.example.galleryexample3.imageediting;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.galleryexample3.R;
import com.example.galleryexample3.fragment.DimensionEditResizeFragment;
import com.example.galleryexample3.fragment.DimensionEditRotateFragment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class DimensionEditActivity extends FragmentActivity {
    //Button
    Button rotateButton;
    Button resizeButton;
    Button backButton;
    Button saveButton;
//    View and layout
    FrameLayout imageFrame;
    ImageView editedImage;
//  Properties
    Bitmap storedImageBitMap;
    Uri storedUri;
    FragmentManager myFragmentManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dimension_edit_view);
        myFragmentManager = getSupportFragmentManager();
        rotateButton = (Button) findViewById(R.id.rotateButton);
        resizeButton = (Button) findViewById(R.id.resizeButton);
        backButton = (Button) findViewById(R.id.backButton);
        saveButton = (Button) findViewById(R.id.saveButton);
        editedImage = (ImageView) findViewById(R.id.editedImage);
        imageFrame = (FrameLayout) findViewById(R.id.editedImageFrame);
        Intent myIntent = getIntent();
        Bundle myBundle = myIntent.getBundleExtra("bitmap");
        if (myBundle != null){
            String storedUriFromBundle =  myBundle.getString("cached");
            try {
                InputStream inputStream = null;
                storedUri = Uri.parse(storedUriFromBundle);
                inputStream = getContentResolver().openInputStream(storedUri);
                storedImageBitMap = BitmapFactory.decodeStream(inputStream);
                assert inputStream != null;
                inputStream.close();
            } catch (IOException e) {
                Log.e("DimensionEdit", Objects.requireNonNull(e.getMessage()));
            }
        }
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new DimensionEditRotateFragment();
                loadFragment(fragment);
            }
        });
        resizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new DimensionEditResizeFragment();
                loadFragment(fragment);
            }
        });

    }
    public void loadFragment(Fragment fragment){
        if (fragment != null){
            myFragmentManager.beginTransaction().replace(R.id.editedImageFrame, fragment).commit();
        }else{
            Log.e("NoFragment", "No Fragment lmao");
        }
    }
    public Bitmap getEditingBitmap(){
        return storedImageBitMap;
    }
    public Uri getEditingBitmapUri(){
        return storedUri;
    }
}
