package com.example.galleryexample3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image_view);

        ImageView imgView = (ImageView) findViewById(R.id.selectedImage);
        Button backButton = (Button) findViewById(R.id.backButton);
        Button albumButton = (Button) findViewById(R.id.albumButton);
        Button tagButton = (Button) findViewById(R.id.tagButton);
        Button editModeButton = (Button) findViewById(R.id.editModeButton);
        TextView dateAddedText = (TextView) findViewById(R.id.dateAddedText);

        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();

        TextRecognitionClass textRecognitionClass = new TextRecognitionClass();
        TagAnalyzerClass tagAnalyzerClass = new TagAnalyzerClass();

        backButton.setOnClickListener((l) -> {
            Intent intent = new Intent(SingleImageView.this, MainActivity.class);
            startActivity(intent);
        });

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

        editModeButton.setOnClickListener((l) -> {
            Intent intent = new Intent(SingleImageView.this, ImageFilters.class);
            Bundle bundle = new Bundle();
            bundle.putString("imageURI", imageURI);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        if (gotBundle == null)
            return;

        imageURI = gotBundle.getString("imageURI");
        DatabaseHandler databaseHandler = new DatabaseHandler(this);

        Glide.with(this).load(imageURI)
                .placeholder(R.drawable.uoh)
                .into(imgView);
    }
}
