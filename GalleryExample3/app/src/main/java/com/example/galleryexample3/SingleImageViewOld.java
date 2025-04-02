package com.example.galleryexample3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.imageediting.ImageFilters;
import com.example.galleryexample3.imageediting.TagAnalyzerClass;
import com.example.galleryexample3.imageediting.TextRecognitionClass;

@Deprecated
public class SingleImageViewOld extends Activity {
    private String imageURI;
    private EditText albumName;
    private EditText tagName;


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image_view_old);


        ImageView imgView = (ImageView) findViewById(R.id.imageView);
        Button backButton = (Button) findViewById(R.id.backButton);
        Button addAlbum = (Button) findViewById(R.id.addAlbum);
        albumName = (EditText) findViewById(R.id.albumNameAdd);
        Button addTag = (Button) findViewById(R.id.addTagButton);
        tagName = (EditText) findViewById(R.id.tagAdd);
        TextView tagTextView = (TextView) findViewById(R.id.tagTextView);
        Button filterButton = (Button) findViewById(R.id.filterButton);
        Button scanText = (Button) findViewById(R.id.scanText);

        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();

        TextRecognitionClass textRecognitionClass = new TextRecognitionClass();
        TagAnalyzerClass tagAnalyzerClass = new TagAnalyzerClass();

        backButton.setOnClickListener((l) ->{
            Intent intent = new Intent(SingleImageViewOld.this, MainActivity.class);
            startActivity(intent);
        });


        // Image null so won't load
        if (gotBundle == null)
            return;

        // Set image
        imageURI = gotBundle.getString("imageURI");

        DatabaseHandler databaseHandler = new DatabaseHandler(this);

        StringBuilder tagList = new StringBuilder("Tags: ");
        databaseHandler.tags().getTagsOfImage(imageURI).forEach((i) -> {tagList.append(i).append(" ");});
        tagTextView.setText(tagList.toString());

        addAlbum.setOnClickListener((l) -> {
            String albumNameGot = albumName.getText().toString();
            databaseHandler.albums().addImageToAlbum(albumNameGot, imageURI);
            albumName.setText("");
            Toast.makeText(this, "added " + imageURI + " to " + albumNameGot, Toast.LENGTH_SHORT).show();
        });

        addTag.setOnClickListener((l) -> {
            String tagNameGot = tagName.getText().toString();
            databaseHandler.tags().addTagsToImage(tagNameGot, imageURI);
            tagName.setText("");

            StringBuilder newTagList = new StringBuilder("Tags: ");
            databaseHandler.tags().getTagsOfImage(imageURI).forEach((i) -> {Log.i("TAG CHECK", i); newTagList.append(i).append(" ");});
            tagTextView.setText(newTagList.toString());

            Toast.makeText(this, "added tag " + tagNameGot, Toast.LENGTH_SHORT).show();
        });

        filterButton.setOnClickListener((l) -> {
            Intent intent = new Intent(SingleImageViewOld.this, ImageFilters.class);
            Bundle bundle = new Bundle();
            bundle.putString("imageURI", imageURI);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        scanText.setOnClickListener((l) -> {
//            textRecognitionClass.getTextFromImage(this, imageURI);
            tagAnalyzerClass.getTags(this, imageURI);
        });


        Glide.with(this).load(imageURI)
                .placeholder(R.drawable.uoh).centerCrop()
                .into(imgView);
    }
}
