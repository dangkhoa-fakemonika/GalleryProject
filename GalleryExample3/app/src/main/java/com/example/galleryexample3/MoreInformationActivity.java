package com.example.galleryexample3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.userinterface.TagListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.ArrayList;
import java.util.List;

public class MoreInformationActivity extends Activity {
    private String imageURI;
    private DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_information_activity);

        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView timeTextView = findViewById(R.id.timeTextView);
        TextView resolutionTextView = findViewById(R.id.resolutionTextView);
        TextView sizeTextView = findViewById(R.id.sizeTextView);
        TextView locationTextView = findViewById(R.id.locationTextView);
        ImageButton backButton = findViewById(R.id.backButton);
        Button addTagsButton = findViewById(R.id.addTagsButton);

        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();

        if (gotBundle == null) return;

        imageURI = gotBundle.getString("imageURI");
        databaseHandler = new DatabaseHandler(this);

        titleTextView.setText(ImageGalleryProcessing.getName(this, imageURI));
        timeTextView.setText(ImageGalleryProcessing.getImageDateAdded(this, imageURI));
        resolutionTextView.setText(ImageGalleryProcessing.getResolution(this, imageURI));
        sizeTextView.setText(ImageGalleryProcessing.getSize(this, imageURI));
        locationTextView.setText(imageURI);

        TagListAdapter tagListAdapter = new TagListAdapter(this, databaseHandler.tags().getTagsOfImage(imageURI), imageURI);

        RecyclerView tagRecyclerView = findViewById(R.id.tagRecyclerView);
//        tagRecyclerView.layoutManager = new LinearLayoutManager(this);
        tagRecyclerView.setAdapter(tagListAdapter);

        backButton.setOnClickListener(listener -> {
            Intent intent = new Intent(MoreInformationActivity.this, SingleImageView.class);
            intent.putExtras(gotBundle);
            startActivity(intent);
        });

        Context thisContext = this;

        addTagsButton.setOnClickListener(listener -> {
            View dialogView = LayoutInflater.from(MoreInformationActivity.this).inflate(R.layout.one_field_dialog_tag_layout, null);
            TextInputLayout inputTextLayout = dialogView.findViewById(R.id.inputTextLayout);

            AutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.autoCompleteTextView);
            ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>(this, R.layout.auto_complete_option, new String[]{});
            autoCompleteTextView.setAdapter(autoCompleteAdapter);

            ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

            InputImage image = InputImage.fromBitmap(BitmapFactory.decodeFile(imageURI), 0);


            labeler.process(image).addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                        @Override
                        public void onSuccess(List<ImageLabel> labels) {
                            // Task completed successfully
                            // ...
                            ArrayList<String> suggestions = new ArrayList<>();
                            for (ImageLabel label : labels) {
                                String text = label.getText();
                                float confidence = label.getConfidence();
                                int index = label.getIndex();

                                suggestions.add(text);
                            }
                            ArrayAdapter<String> tempAdapter = new ArrayAdapter<String>(thisContext, R.layout.auto_complete_option, suggestions.toArray(new String[]{}));
                            autoCompleteTextView.setAdapter(tempAdapter);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                        }
                    });

            autoCompleteTextView.setThreshold(0);
            autoCompleteTextView.setOnClickListener((event) -> {
                autoCompleteTextView.showDropDown();
            });

            inputTextLayout.setHint("Enter Tag Name");
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Add Tag")
                    .setView(dialogView)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String tagName = autoCompleteTextView.getText().toString();
                            databaseHandler.tags().addTagsToImage(tagName, imageURI);
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
        });


    }
}
