package com.example.galleryexample3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.MediaExtractor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.imageediting.ImageEditActivity;
import com.bumptech.glide.Glide;
import com.example.galleryexample3.datamanagement.ImageManager;
import com.example.galleryexample3.imageediting.EditView;
import com.example.galleryexample3.imageediting.PaintingActivity;
import com.example.galleryexample3.imageediting.TagAnalyzerClass;
import com.example.galleryexample3.imageediting.TextRecognitionClass;
import com.example.galleryexample3.userinterface.ImageBaseAdapter;
import com.example.galleryexample3.userinterface.SwipeImageAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class SingleImageView extends Activity implements PopupMenu.OnMenuItemClickListener {
    private String imageURI;
    private int position;
    private int shortAnimationDuration;
    private DatabaseHandler databaseHandler;
    private Context context;
    private MediaStoreObserver mediaStoreObserver;
    private AlertDialog alertDialog;
    private ArrayList<String> imagesList;
    private ViewPager2 viewPager;
    private View.OnClickListener toggleUtility;
    private boolean osv = false;
    public class MediaStoreObserver extends ContentObserver {
        public MediaStoreObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            if (alertDialog != null) {
                alertDialog.dismiss();
            }

            imagesList = ImageGalleryProcessing.getImages(SingleImageView.this, "DATE_ADDED", " DESC");
            SwipeImageAdapter swipeImageAdapter = new SwipeImageAdapter(SingleImageView.this, imagesList);
            viewPager.setAdapter(swipeImageAdapter);
            if (!imagesList.contains(imageURI)) {
                position = Math.min(imagesList.size() - 1, Math.max(0, position - 1));
                viewPager.setCurrentItem(position, true);
            } else {
                position = imagesList.indexOf(imageURI);
                viewPager.setCurrentItem(position, false);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image_view);
        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        imagesList = ImageGalleryProcessing.getImages(this, "DATE_ADDED", " DESC");

        RelativeLayout screenLayout = (RelativeLayout) findViewById(R.id.screenLayout);
        viewPager = (ViewPager2) findViewById(R.id.imageViewPager);

        RelativeLayout utilityLayout = (RelativeLayout) findViewById(R.id.utilityLayout);
        ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
        TextView dateAddedText = (TextView) findViewById(R.id.dateAddedText);

        ImageButton editModeButton = (ImageButton) findViewById(R.id.editModeButton);
        ImageButton drawModeButton = (ImageButton) findViewById(R.id.drawModeButton);
        ImageButton deleteButton = (ImageButton) findViewById(R.id.deleteButton);
        ImageButton moreOptionButton = (ImageButton) findViewById(R.id.moreOptionButton);

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

        TextRecognitionClass textRecognitionClass = new TextRecognitionClass();
        TagAnalyzerClass tagAnalyzerClass = new TagAnalyzerClass();

        // Get bundle from previous screen
        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();

        databaseHandler = new DatabaseHandler(this);
        context = this;
        if (gotBundle == null)
            return;

        imageURI = gotBundle.getString("imageURI");
        position = gotBundle.getInt("position");
        viewPager.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                page.setOnClickListener(toggleUtility);
            }
        });
        // Set up swiping between images
        SwipeImageAdapter swipeImageAdapter = new SwipeImageAdapter(this, imagesList);
        viewPager.setAdapter(swipeImageAdapter);
        viewPager.setCurrentItem(position, true);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int pos) {
                super.onPageSelected(pos);
                imageURI = imagesList.get(pos);
                position = pos;
            }
        });

        backButton.setOnClickListener(listener -> {
            Intent intent = new Intent(SingleImageView.this, MainActivity.class);
            startActivity(intent);
        });

        editModeButton.setOnClickListener(listener -> {
            Intent intent = new Intent(SingleImageView.this, EditView.class);
            Bundle bundle = new Bundle();
            bundle.putString("imageURI", imageURI);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        drawModeButton.setOnClickListener(listener -> {
            Intent intent = new Intent(SingleImageView.this, PaintingActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("imageURI", imageURI);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        deleteButton.setOnClickListener((l) -> {
            boolean r = ImageGalleryProcessing.deleteImage(this, imageURI);
            //boolean r = ImageGalleryProcessing.changeNameImage(this, imageURI, "newtest1.png");
            if (r){
                Toast.makeText(this, "Image deleted.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(SingleImageView.this, MainActivity.class);
                startActivity(intent);
            }
            else{
                Toast.makeText(this, "Image can't be deleted.", Toast.LENGTH_LONG).show();
            }
        });

        // View/Hide utility buttons
//        screenLayout.setOnClickListener((view) -> {
//            if (utilityLayout.getVisibility() == View.VISIBLE)
//                utilityLayout.animate()
//                        .alpha(0f)
//                        .setDuration(shortAnimationDuration)
//                        .setListener(new AnimatorListenerAdapter() {
//                            @Override
//                            public void onAnimationEnd(Animator animation) {
//                                utilityLayout.setVisibility(View.GONE);
//                            }
//                        });
//            else {
//                utilityLayout.setAlpha(0f);
//                utilityLayout.setVisibility(View.VISIBLE);
//                utilityLayout.animate()
//                        .alpha(1f)
//                        .setDuration(shortAnimationDuration)
//                        .setListener(null);
//            }
//        });

        // Show menu
        moreOptionButton.setOnClickListener(this::showMenu);
    }

    @Override
    protected void onResume() {
        super.onResume();

        imagesList = ImageGalleryProcessing.getImages(SingleImageView.this, "DATE_ADDED", " DESC");
        SwipeImageAdapter swipeImageAdapter = new SwipeImageAdapter(SingleImageView.this, imagesList);
        viewPager.setAdapter(swipeImageAdapter);
        if (!imagesList.contains(imageURI)) {
            position = Math.min(imagesList.size() - 1, Math.max(0, position - 1));
            viewPager.setCurrentItem(position, true);
        } else {
            position = imagesList.indexOf(imageURI);
            viewPager.setCurrentItem(position, false);
        }

        if (!osv) {
            Handler handler = new Handler();
            mediaStoreObserver = new MediaStoreObserver(handler);

            ContentResolver contentResolver = this.getContentResolver();
            contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, mediaStoreObserver);
            osv = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        this.getContentResolver().unregisterContentObserver(mediaStoreObserver);
        osv = false;
    }

    private void showMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.single_image_view_menu);
        popup.show();
    }

    // Handle menu item click
    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.addAlbum){
            View dialogView = LayoutInflater.from(SingleImageView.this).inflate(R.layout.one_field_dialog_layout, null);
            TextInputLayout inputTextLayout = dialogView.findViewById(R.id.inputTextLayout);
            TextInputEditText editText = dialogView.findViewById(R.id.editText);
            inputTextLayout.setHint("Enter Album Name");
            alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Add Album")
                    .setView(dialogView)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String albumName = editText.getText().toString();
//                            Log.i("ALBUM", albumName);
                            databaseHandler.albums().addImageToAlbum(albumName, imageURI);
                            Toast.makeText(context, "Added to " + albumName, Toast.LENGTH_LONG).show();

                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.i("ALBUM", "Cancel");
                            dialogInterface.dismiss();
                        }
                    }).create();
            alertDialog.show();
            return true;
        }
        else if (id == R.id.addTag) {
            View dialogView = LayoutInflater.from(SingleImageView.this).inflate(R.layout.one_field_dialog_layout, null);
            TextInputLayout inputTextLayout = dialogView.findViewById(R.id.inputTextLayout);
            TextInputEditText editText = dialogView.findViewById(R.id.editText);
            inputTextLayout.setHint("Enter Tag Name");
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Add Tag")
                    .setView(dialogView)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String tagName = editText.getText().toString();
                            Log.i("TAG", tagName);
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.i("TAG", "Cancel");
                            dialogInterface.dismiss();
                        }
                    }).create();
            alertDialog.show();
            return true;
        }
        else if (id == R.id.copy) {
            return true;
        }
        else if (id == R.id.analyzeText) {
            TextRecognitionClass.getTextFromImage(this, imageURI);
            return true;
        }
        else if (id == R.id.moreInfo) {
            return true;
        }
        else
            return false;
    }
}
