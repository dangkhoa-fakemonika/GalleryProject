package com.example.galleryexample3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.userinterface.TagGridAdapter;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class TagManagementActivity extends Activity {
    Context context;
    DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_management_activity);

        RecyclerView tagGridView = (RecyclerView) findViewById(R.id.tagGrid);
        databaseHandler = DatabaseHandler.getInstance(this);
        ArrayList<String> tagList = databaseHandler.tags().getAllTags();

        TagGridAdapter tagGridAdapter = new TagGridAdapter(this, tagList);
        tagGridView.setAdapter(tagGridAdapter);

        Button addTagsButton = (Button) findViewById(R.id.addTagsButton);
        findViewById(R.id.backButton).setOnClickListener((l) -> {
            Intent intent = new Intent(TagManagementActivity.this, MainActivityNew.class);
            startActivity(intent);
        });
        context = this;

        addTagsButton.setOnClickListener(listener -> {
            View dialogView = LayoutInflater.from(TagManagementActivity.this).inflate(R.layout.one_field_dialog_tag_layout, null);
            TextInputLayout inputTextLayout = dialogView.findViewById(R.id.inputTextLayout);

            AutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.autoCompleteTextView);
//            ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>(this, R.layout.auto_complete_option, new String[]{});
//            autoCompleteTextView.setAdapter(autoCompleteAdapter);

            inputTextLayout.setHint("Enter Tag Name");
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Add Tag")
                    .setView(dialogView)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String tagName = autoCompleteTextView.getText().toString().trim();
                            if (tagName.length() < 4 || tagName.length() > 20){
                                Toast.makeText(context, "Tag names' length must be at least 4 and at most 20 characters.", Toast.LENGTH_LONG).show();
                            } else if (databaseHandler.tags().checkTagExisted(tagName)) {
                                Toast.makeText(context, "Tag already exists.", Toast.LENGTH_LONG).show();
                            }
                            else {
                                databaseHandler.tags().createNewTag(tagName);
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
    }
}
