package com.example.galleryexample3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.userinterface.ThemeManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MoreAlbumInformationActivity extends Activity {
    public static String BUKEY_GROUP_TYPE = "groupType";
    public static String BUKEY_GROUP_NAME = "groupName";
    public static String BUKEY_GROUP_COUNT = "groupCount";
    private DatabaseHandler databaseHandler;
    Context context;
    int groupType;
    String groupName;
    int groupItemCounts;
    String groupDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.setTheme(this);
        setContentView(R.layout.album_more_information_activity);

        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView timeTextView = findViewById(R.id.timeTextView);
        TextView sizeTextView = findViewById(R.id.sizeTextView);
        TextView description = findViewById(R.id.albumDescription);
        ImageButton backButton = findViewById(R.id.backButton);
        Button deleteAlbum = findViewById(R.id.deleteAlbum);
        Button renameAlbum = findViewById(R.id.renameAlbum);
        Button setDescription = findViewById(R.id.changeDescription);

        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();

        if (gotBundle == null) return;

        groupName = gotBundle.getString(BUKEY_GROUP_NAME);
        groupItemCounts = gotBundle.getInt(BUKEY_GROUP_COUNT);
        databaseHandler = DatabaseHandler.getInstance(this);
        context = this;
        groupDescription = databaseHandler.albums().getAlbumDescription(groupName);

        titleTextView.setText(groupName);
        timeTextView.setText(databaseHandler.albums().getAlbumCreateTime(groupName));
        sizeTextView.setText(groupItemCounts + "");
        description.setText(groupDescription);

        backButton.setOnClickListener(listener -> {
            finish();
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
            editText.setText(groupName);
            inputTextLayout.setHint("New name");
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Rename Album")
                    .setView(dialogView)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String albumNewName = editText.getText().toString().trim();
                            if (databaseHandler.albums().checkAlbumExisted(albumNewName)){
                                Toast.makeText(context, "An album already existed with that name", Toast.LENGTH_LONG).show();
                            }
                            else {
                                databaseHandler.albums().changeName(groupName, albumNewName);
                                titleTextView.setText(albumNewName);
                                Toast.makeText(context, "Album renamed", Toast.LENGTH_LONG).show();
                                dialogInterface.dismiss();
                            }
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

        setDescription.setOnClickListener((l) -> {
            View dialogView = LayoutInflater.from(MoreAlbumInformationActivity.this).inflate(R.layout.one_field_dialog_layout, null);
            TextInputLayout inputTextLayout = dialogView.findViewById(R.id.inputTextLayout);
            TextInputEditText editText = dialogView.findViewById(R.id.editText);
            editText.setText(groupDescription);
            inputTextLayout.setHint("New description");
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("New Description")
                    .setView(dialogView)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String newDescription = editText.getText().toString().trim();

                            databaseHandler.albums().changeDescription(groupName, newDescription);
                            description.setText(newDescription);
                            Toast.makeText(context, "Description changed", Toast.LENGTH_LONG).show();
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
