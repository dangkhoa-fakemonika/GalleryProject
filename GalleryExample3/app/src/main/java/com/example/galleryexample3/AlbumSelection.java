package com.example.galleryexample3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.galleryexample3.datamanagement.AlbumsController;


public class AlbumSelection extends Activity {

    AlbumsController albumsController;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_list_activity);
        Button backButton = (Button) findViewById(R.id.goBackButton);

        backButton.setOnClickListener((l) ->{
            Intent intent = new Intent(AlbumSelection.this, MainActivity.class);
            startActivity(intent);
        });

        albumsController = new AlbumsController(this);

//
//        HashSet<String> hashSet = new HashSet<>();
//        SharedPreferences albums = getSharedPreferences("collective_data", Activity.MODE_PRIVATE);
//        if (albums != null && !albums.contains("albums_list")){
//            SharedPreferences.Editor editor = albums.edit();
//            editor.putStringSet("albums_list", new HashSet<>());
//            editor.apply();
//        }

//        if (albums != null)
//             hashSet = new HashSet<>(Objects.requireNonNull(albums.getStringSet("albums_list", null)));

        Object[] sArray =  albumsController.getAllAlbums().toArray();
        ListView theListView = (ListView) findViewById(R.id.albumListView);
        theListView.setAdapter(new ArrayAdapter<>(this, R.layout.album_item_view, R.id.textView2, sArray));

        theListView.setOnItemClickListener((arg0, arg1, position, arg3) ->{
            Intent intent = new Intent(AlbumSelection.this, Albums.class);
            Bundle bundle = new Bundle();
            bundle.putString("albumSavedName", (String) sArray[position]);
            intent.putExtras(bundle);
            startActivity(intent);
        });
    }
}
