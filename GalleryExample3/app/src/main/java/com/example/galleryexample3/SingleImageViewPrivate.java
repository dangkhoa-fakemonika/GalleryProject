package com.example.galleryexample3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.businessclasses.PrivateAlbum;
import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.userinterface.SwipeImageAdapter;

import java.util.ArrayList;

public class SingleImageViewPrivate extends AppCompatActivity {
    private String imageURI;
    private int position;
    private String dateAdded;
    private int shortAnimationDuration;
    private DatabaseHandler databaseHandler;
    private Context context;
    private SingleImageView.MediaStoreObserver mediaStoreObserver;
    private AlertDialog alertDialog;
    private ArrayList<String> imagesList;
    private ViewPager2 viewPager;
    private View.OnClickListener toggleUtility;
    private boolean osv = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image_view_private);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.screenLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        imagesList = new ArrayList<>();
        RelativeLayout screenLayout = (RelativeLayout) findViewById(R.id.screenLayout);
        viewPager = (ViewPager2) findViewById(R.id.imageViewPager);

        RelativeLayout utilityLayout = (RelativeLayout) findViewById(R.id.utilityLayout);
        ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
        TextView dateAddedText = (TextView) findViewById(R.id.dateAddedText);

        ImageButton deleteButton = (ImageButton) findViewById(R.id.deleteButton);
        ImageButton moreOptionButton = (ImageButton) findViewById(R.id.removeFromPrivate);
        imagesList = PrivateAlbum.getImages(this, "DATE_ADDED", "DESC");
        toggleUtility = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (utilityLayout.getVisibility() == View.VISIBLE)
                    utilityLayout.animate()
                            .alpha(0f)
                            .setDuration(shortAnimationDuration)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    utilityLayout.setVisibility(View.GONE);
                                }
                            });
                else {
                    utilityLayout.setAlpha(0f);
                    utilityLayout.setVisibility(View.VISIBLE);
                    utilityLayout.animate()
                            .alpha(1f)
                            .setDuration(shortAnimationDuration)
                            .setListener(null);
                }
            }
        };
        Intent myIntent = getIntent();
        Bundle myBundle = myIntent.getExtras();
        imageURI = myBundle.getString("imageURI");
        dateAdded = myBundle.getString("dateAdded");
        position = myBundle.getInt("position");
        dateAddedText.setText("Private Images");

        viewPager.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                page.setOnClickListener(toggleUtility);
            }
        });

        // Set up swiping between images
        Log.e("Received Position", String.valueOf(position) + " " + String.valueOf(imagesList.size()));

        SwipeImageAdapter swipeImageAdapter = new SwipeImageAdapter(this, imagesList);
        Log.v("From adapter", String.valueOf(swipeImageAdapter.getItemCount()));

        viewPager.setAdapter(swipeImageAdapter);
//        viewPager.setCurrentItem(imagesList.size()-1, false);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int pos) {
                super.onPageSelected(pos);
                imageURI = imagesList.get(pos);
                position = pos;
            }
        });

        backButton.setOnClickListener(listener -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        deleteButton.setOnClickListener((l) -> {
            alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Delete Image?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            boolean r = ImageGalleryProcessing.deleteImage(context, imageURI);
                            databaseHandler.tags().deleteImage(imageURI);
                            databaseHandler.albums().deleteImage(imageURI);
                            //boolean r = ImageGalleryProcessing.changeNameImage(this, imageURI, "newtest1.png");
                            if (r){
                                Toast.makeText(context, "Image deleted.", Toast.LENGTH_LONG).show();
                                getOnBackPressedDispatcher().onBackPressed();
                            }
                            else{
                                Toast.makeText(context, "Image can't be deleted.", Toast.LENGTH_LONG).show();
                            }
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
        });
    }
}
