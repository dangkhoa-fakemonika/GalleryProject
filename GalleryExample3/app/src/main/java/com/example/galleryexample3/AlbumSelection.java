package com.example.galleryexample3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.datamanagement.AlbumsController;


public class AlbumSelection extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_list_activity);
        Button backButton = (Button) findViewById(R.id.goBackButton);
        Button addAlbum = (Button) findViewById(R.id.newAlbum);
        EditText albumNameText = (EditText) findViewById(R.id.albumeNameText);

        DatabaseHandler databaseHandler = new DatabaseHandler(this);
        backButton.setOnClickListener((l) ->{
            Intent intent = new Intent(AlbumSelection.this, MainActivity.class);
            startActivity(intent);
        });

        addAlbum.setOnClickListener((l) -> {
            String temp = albumNameText.getText().toString();
            databaseHandler.albums().createAlbum(temp);
            Toast.makeText(this, "Album created: " + temp, Toast.LENGTH_SHORT).show();
            albumNameText.setText("");
        });


        Object[] sArray = databaseHandler.albums().getAllAlbums().toArray();
        ListView theListView = (ListView) findViewById(R.id.albumListView);
        theListView.setAdapter(new ArrayAdapter<>(this, R.layout.album_item_view, R.id.textView2, sArray));

        theListView.setOnItemClickListener((arg0, arg1, position, arg3) ->{
            Intent intent = new Intent(AlbumSelection.this, AlbumDisplay.class);
            Bundle bundle = new Bundle();
            bundle.putString("albumSavedName", (String) sArray[position]);
            intent.putExtras(bundle);
            startActivity(intent);
        });
    }
}
