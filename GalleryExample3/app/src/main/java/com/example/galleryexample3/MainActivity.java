package com.example.galleryexample3;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import com.bumptech.glide.Glide;
import com.example.galleryexample3.datamanagement.AlbumsController;

public class MainActivity extends Activity {

    private ArrayList<String> images;
    private EditText rowNum;
    private Button changeRows;
    private Button goAlbums;
    private Button clearData;

    final int PICK_FROM_GALLERY = 101; // This could be any non-0 number lol
    final int NUM_IMAGE_LOAD_LIMIT = 20; // Number of images per load

    private AlbumsController albumsController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set permission to get images
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
        }

        albumsController = new AlbumsController(this);

        // Element assignment
        GridView gallery = (GridView) findViewById(R.id.galleryGridView);
        rowNum = (EditText) findViewById(R.id.rowNum);
        changeRows = (Button) findViewById(R.id.changeRowButton);
        goAlbums = (Button) findViewById(R.id.gotoAlbums);
        clearData = (Button) findViewById(R.id.clearData);

        // Load adapter
        gallery.setAdapter(new ImageAdapter(this, NUM_IMAGE_LOAD_LIMIT));

        // Change number of columns of grid view (no restriction of data types)
        changeRows.setOnClickListener((l) -> {
            String val = String.valueOf(rowNum.getText());
            int numVal = Integer.parseInt(val);

            gallery.setNumColumns(numVal);
        });

        goAlbums.setOnClickListener((l) ->{
            Intent intent = new Intent(MainActivity.this, AlbumSelection.class);
            startActivity(intent);
        });


        // Clear all data
        clearData.setOnClickListener((l) ->{
            albumsController.clearData();
        });

        // Single image view
        gallery.setOnItemClickListener((arg0, arg1, position, arg3) -> {
            if (null != images && !images.isEmpty()){
                Intent intent = new Intent(MainActivity.this, SingleImageView.class);
                Bundle bundle = new Bundle();
                bundle.putString("imageURI", images.get(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

    }

    private class ImageAdapter extends BaseAdapter {
        private final Activity context;

        public ImageAdapter(Activity localContext, int setLoadLimit) {
            context = localContext;
            images = getAllShownImagesPath(context, setLoadLimit);
        }

        public int getCount() {
            return images.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            ImageView picturesView;
            if (convertView == null) {
                picturesView = new ImageView(context);
                picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                picturesView
                        .setLayoutParams(new GridView.LayoutParams(360, 360));

            } else {
                picturesView = (ImageView) convertView;
            }

            Glide.with(context).load(images.get(position))
                    .placeholder(R.drawable.uoh).centerCrop()
                    .into(picturesView);

            return picturesView;
        }


        private ArrayList<String> getAllShownImagesPath(Activity activity, int loadLimit) {
            ArrayList<String> arrPath = new ArrayList<>();

            ContentResolver contentResolver = getContentResolver();
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            // use MediaStore.Images.Media.<Attribute> to query and stuff
            // contentResolver is the sqlite database

            try (Cursor cursor = contentResolver.query(uri, null, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC")){
                if (cursor == null) {
                    // query failed, handle error
                } else if (!cursor.moveToFirst()) {
                    // no media on the device
                } else {
                    int i = 0;
//                    How to set attribute of
//                    int titleColumn = cursor.getColumnIndex(MediaStore.Images.Media.TITLE);
//                    int idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

                    do {
//                        How to get attributes
//                        long thisId = cursor.getLong(idColumn);
//                        String thisTitle = cursor.getString(titleColumn);
                        String pathGot = cursor.getString(dataColumnIndex);
                        arrPath.add(pathGot);
                        i++;
                    } while (cursor.moveToNext() && i < loadLimit); // Load limit
                }
            }

            return arrPath;
        }
    }
}