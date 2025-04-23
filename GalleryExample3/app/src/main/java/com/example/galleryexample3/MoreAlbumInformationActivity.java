package com.example.galleryexample3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.userinterface.TagListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.ArrayList;
import java.util.List;

public class MoreAlbumInformationActivity extends Activity {
    public static String BUKEY_GROUP_TYPE = "groupType";
    public static String BUKEY_GROUP_NAME = "groupName";
    public static String BUKEY_GROUP_COUNT = "groupCount";
    private DatabaseHandler databaseHandler;
    Context context;
    int groupType;
    String groupName;
    int groupItemCounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_more_information_activity);

        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView timeTextView = findViewById(R.id.timeTextView);
        TextView sizeTextView = findViewById(R.id.sizeTextView);
        ImageButton backButton = findViewById(R.id.backButton);
        Button deleteAlbum = findViewById(R.id.deleteAlbum);
        Button renameAlbum = findViewById(R.id.renameAlbum);

        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();

        if (gotBundle == null) return;

        groupName = gotBundle.getString(BUKEY_GROUP_NAME);
        databaseHandler = DatabaseHandler.getInstance(this);
        context = this;

        titleTextView.setText(groupName);
        timeTextView.setText("Placeholder Text");
        sizeTextView.setText("Placeholder Text");

        backButton.setOnClickListener(listener -> {
            Intent intent = new Intent(MoreAlbumInformationActivity.this, MainActivityNew.class);
            intent.putExtras(gotBundle);
            startActivity(intent);
        });


        deleteAlbum.setOnClickListener(listener -> {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Delete this album?")
                    .setMessage("This will not delete any images in the album")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            databaseHandler.albums().deleteAlbum(groupName);
                            dialogInterface.dismiss();
                            Intent intent = new Intent(MoreAlbumInformationActivity.this, MainActivityNew.class);
                            startActivity(intent);
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

        renameAlbum.setOnClickListener((l) -> {
            View dialogView = LayoutInflater.from(MoreAlbumInformationActivity.this).inflate(R.layout.one_field_dialog_layout, null);
            TextInputLayout inputTextLayout = dialogView.findViewById(R.id.inputTextLayout);
            TextInputEditText editText = dialogView.findViewById(R.id.editText);
            inputTextLayout.setHint("New name");
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Rename Image")
                    .setView(dialogView)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String albumNewName = editText.getText().toString().trim();
//                            databaseHandler.albums().renameAlbum();
                            Toast.makeText(context, "Image renamed", Toast.LENGTH_LONG).show();
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
