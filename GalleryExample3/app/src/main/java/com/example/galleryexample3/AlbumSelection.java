package com.example.galleryexample3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.galleryexample3.dataclasses.DatabaseHandler;

import java.util.ArrayList;
import java.util.Arrays;

@Deprecated
public class AlbumSelection extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_list_activity);
        Button backButton = (Button) findViewById(R.id.goBackButton);
        Button addAlbum = (Button) findViewById(R.id.newAlbum);
        EditText albumNameText = (EditText) findViewById(R.id.albumeNameText);

        DatabaseHandler databaseHandler = DatabaseHandler.getInstance(this);
        backButton.setOnClickListener((l) ->{
            Intent intent = new Intent(AlbumSelection.this, MainActivity.class);
            startActivity(intent);
        });

        Object[] sArray = databaseHandler.albums().getAllAlbums().toArray();
        String[] temp = new String[sArray.length];
        for (int i = 0; i < sArray.length; i++)
            temp[i] = sArray[i].toString();

        Log.i("Albums", Arrays.toString(sArray));
        GridView theGridView = (GridView) findViewById(R.id.albumGridView);
//        theGridView.setAdapter(new ArrayAdapter<>(this, R.layout.album_item_view, R.id.albumName, sArray));
        MyAlbumGridAdapter gridAdapter = new MyAlbumGridAdapter((Context) this, temp, databaseHandler);
        theGridView.setAdapter(gridAdapter);

        theGridView.setOnItemClickListener((arg0, arg1, position, arg3) ->{
            Intent intent = new Intent(AlbumSelection.this, AlbumDisplay.class);
            Bundle bundle = new Bundle();
            bundle.putString("albumSavedName", (String) sArray[position]);
            intent.putExtras(bundle);
            startActivity(intent);
        });


        addAlbum.setOnClickListener((l) -> {
            String temp2 = albumNameText.getText().toString();
            databaseHandler.albums().createAlbum(temp2);
            Toast.makeText(this, "Album created: " + temp2, Toast.LENGTH_SHORT).show();
            albumNameText.setText("");
            
            gridAdapter.notifyDataSetChanged();
            theGridView.invalidate();
        });
    }

    @Override
    protected void onResume() {

        super.onResume();
    }
}
@Deprecated
class MyAlbumGridAdapter extends BaseAdapter {
    String[] albumNames;
    Context context;
    DatabaseHandler handler;
    public MyAlbumGridAdapter(@NonNull Context context, String[] arr, DatabaseHandler databaseHandler) {
        albumNames = arr;
        this.context = context;
        handler = databaseHandler;
    }

    @Override
    public int getCount() {
        return albumNames.length;
    }

    @Override
    public Object getItem(int i) {
        return albumNames[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View col = inflater.inflate(R.layout.album_item_view, null);
        TextView albumName = (TextView) col.findViewById(R.id.albumName);
        TextView albumCount = (TextView) col.findViewById(R.id.imageCount);
        ImageView albumImage = (ImageView) col.findViewById(R.id.albumImage);

        ArrayList<String> albumImages = handler.albums().getImagesOfAlbum(albumNames[position]);

        if (albumImages.isEmpty()){
            Glide.with(context).load(R.drawable.uoh).centerCrop().into(albumImage);
        }
        else {
            String albumImageURI = albumImages.get(albumImages.size() - 1);
            Glide.with(context).load(albumImageURI)
                    .placeholder(R.drawable.uoh).centerCrop()
                    .into(albumImage);
        }

//        albumImage.setImageResource(R.drawable.uoh);
        albumName.setText(albumNames[position]);
        albumCount.setText(albumImages.size() + " images");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        albumImage.setLayoutParams(new CardView.LayoutParams( width / 2, width / 2));

        return col;
    }
}
