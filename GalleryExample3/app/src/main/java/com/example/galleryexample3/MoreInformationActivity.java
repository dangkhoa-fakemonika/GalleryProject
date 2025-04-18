package com.example.galleryexample3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.userinterface.TagListAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

        titleTextView.setText(ImageGalleryProcessing.getName(this, Uri.parse(imageURI)));
        timeTextView.setText(ImageGalleryProcessing.getImageDateAdded(this, Uri.parse(imageURI)));
        resolutionTextView.setText(ImageGalleryProcessing.getResolution(this, Uri.parse(imageURI)));
        sizeTextView.setText(ImageGalleryProcessing.getSize(this, Uri.parse(imageURI)));
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

        addTagsButton.setOnClickListener(listener -> {
            View dialogView = LayoutInflater.from(MoreInformationActivity.this).inflate(R.layout.one_field_dialog_layout, null);
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
            alertDialog.show();;
        });


    }
}
