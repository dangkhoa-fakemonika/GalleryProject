package com.example.galleryexample3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashSet;

public class SingleImageViewPrivate extends AppCompatActivity {
    private Context context;
    private String imageURI;
    private int position;
    private String dateAdded;
    private int shortAnimationDuration;
    private DatabaseHandler databaseHandler;
    private SingleImageView.MediaStoreObserver mediaStoreObserver;
    private AlertDialog alertDialog;
    private ArrayList<String> imagesList;
    private ViewPager2 viewPager;
    private View.OnClickListener toggleUtility;
    private ImageButton moreInformation;
    private ImageButton removeFrom;
    private ImageButton deleteButton;
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
        context = this;
        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        imagesList = new ArrayList<>();
        RelativeLayout screenLayout = (RelativeLayout) findViewById(R.id.screenLayout);
        viewPager = (ViewPager2) findViewById(R.id.imageViewPager);

        RelativeLayout utilityLayout = (RelativeLayout) findViewById(R.id.utilityLayout);
        ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
        TextView dateAddedText = (TextView) findViewById(R.id.dateAddedText);

        deleteButton = (ImageButton) findViewById(R.id.deleteButton);
        removeFrom = findViewById(R.id.removeFromPrivate);
        moreInformation = (ImageButton) findViewById(R.id.moreInfomation);
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
        Log.v("Private View Position", String.valueOf(position));

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
        viewPager.setCurrentItem(position, false);

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
                            PrivateAlbum.deleteImage(context, imageURI);
                            Toast.makeText(context, "Image is deleted", Toast.LENGTH_SHORT).show();
                            getOnBackPressedDispatcher().onBackPressed();
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

        moreInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dialogView = LayoutInflater.from(context).inflate(R.layout.more_information_dialog, null);
                TextView infoPathText = dialogView.findViewById(R.id.infoPath);
                TextView infoSizeText = dialogView.findViewById(R.id.infoSize);
                TextView infoResolutionText = dialogView.findViewById(R.id.infoResolution);
                String infoPath = PrivateAlbum.getName(context, imageURI);
                String infoSize = PrivateAlbum.getSize(context, imageURI);
                String infoResolution = PrivateAlbum.getResolution(context, imageURI);
                infoPathText.setText("Name: " + infoPath);
                infoSizeText.setText("Size: " + infoSize);
                infoResolutionText.setText("Resolution: " +  infoResolution);
                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setTitle("Information")
                        .setView(dialogView)
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create();
                alertDialog.show();
            }
        });
        removeFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog = new AlertDialog.Builder(context)
                        .setTitle("Delete Image?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                PrivateAlbum.removeImage(context, imageURI);
                                Toast.makeText(context, "Removed image from Private.", Toast.LENGTH_SHORT).show();
                                getOnBackPressedDispatcher().onBackPressed();
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
            }
        });
    }
}
