package com.example.galleryexample3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.imageediting.ImageFilters;
import com.example.galleryexample3.imageediting.TagAnalyzerClass;
import com.example.galleryexample3.imageediting.TextRecognitionClass;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SingleImageView extends Activity {
    private String imageURI;
    private int shortAnimationDuration;

    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image_view);
        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        RelativeLayout screenLayout = (RelativeLayout) findViewById(R.id.screenLayout);
        ImageView imgView = (ImageView) findViewById(R.id.selectedImage);

        RelativeLayout utilityLayout = (RelativeLayout) findViewById(R.id.utilityLayout);
        ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
        TextView dateAddedText = (TextView) findViewById(R.id.dateAddedText);

        ImageButton editModeButton = (ImageButton) findViewById(R.id.editModeButton);
        ImageButton drawModeButton = (ImageButton) findViewById(R.id.drawModeButton);
        ImageButton deleteButton = (ImageButton) findViewById(R.id.deleteButton);
        ImageButton moreOptionButton = (ImageButton) findViewById(R.id.moreOptionButton);

        TextRecognitionClass textRecognitionClass = new TextRecognitionClass();
        TagAnalyzerClass tagAnalyzerClass = new TagAnalyzerClass();

        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();

        backButton.setOnClickListener(listener -> {
            Intent intent = new Intent(SingleImageView.this, MainActivity.class);
            startActivity(intent);
        });
        /*
        tagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dialogView = LayoutInflater.from(SingleImageView.this).inflate(R.layout.one_field_dialog_layout, null);
                TextInputLayout inputTextLayout = dialogView.findViewById(R.id.inputTextLayout);
                TextInputEditText editText = dialogView.findViewById(R.id.editText);
                inputTextLayout.setHint("Enter Tag Name");
                AlertDialog alertDialog = new AlertDialog.Builder(SingleImageView.this)
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
            }
        });

        albumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dialogView = LayoutInflater.from(SingleImageView.this).inflate(R.layout.one_field_dialog_layout, null);
                TextInputLayout inputTextLayout = dialogView.findViewById(R.id.inputTextLayout);
                TextInputEditText editText = dialogView.findViewById(R.id.editText);
                inputTextLayout.setHint("Enter Album Name");
                AlertDialog alertDialog = new AlertDialog.Builder(SingleImageView.this)
                        .setTitle("Add Album")
                        .setView(dialogView)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String albumName = editText.getText().toString();
                                Log.i("ALBUM", albumName);
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
            }
        });
        */

        editModeButton.setOnClickListener((l) -> {
            Intent intent = new Intent(SingleImageView.this, ImageFilters.class);
            Bundle bundle = new Bundle();
            bundle.putString("imageURI", imageURI);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        if (gotBundle == null)
            return;

        // View/Hide utility buttons
        screenLayout.setOnTouchListener((view, event) -> {
            view.performClick();
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
                return view.onTouchEvent(event);
        });

        moreOptionButton.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(this, view);
            popup.getMenuInflater().inflate(R.menu.single_image_view_menu, popup.getMenu());
            popup.show();
        });

        imageURI = gotBundle.getString("imageURI");
        DatabaseHandler databaseHandler = new DatabaseHandler(this);

        Glide.with(this).load(imageURI)
                .placeholder(R.drawable.uoh)
                .into(imgView);
    }

}
