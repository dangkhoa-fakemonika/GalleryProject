package com.example.galleryexample3;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import com.bumptech.glide.Glide;

public class MainActivity extends Activity {

    /** The images. */
    private ArrayList<String> images;
    final int PICK_FROM_GALLERY = 101;
    private EditText rowNum;
    private Button changeRows;
    private Button goAlbums;
    private Button clearData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
        }

        SharedPreferences albums = getSharedPreferences("collective_data", Activity.MODE_PRIVATE);
        if (albums != null && !albums.contains("albums_list")){
            Log.i("SP check", "bad");
            SharedPreferences.Editor editor = albums.edit();
            HashSet<String> test = new HashSet<>();
            test.add("actual dummy");

            editor.putStringSet("albums_list", test);

            editor.apply();

        }
        else {
            Log.i("SP check", "good");
        }

        GridView gallery = (GridView) findViewById(R.id.galleryGridView);
        rowNum = (EditText) findViewById(R.id.rowNum);
        changeRows = (Button) findViewById(R.id.changeRowButton);
        goAlbums = (Button) findViewById(R.id.gotoAlbums);
        clearData = (Button) findViewById(R.id.clearData);

        gallery.setAdapter(new ImageAdapter(this));

        // Change columns of the thing (not restricted yet)
        changeRows.setOnClickListener((l) -> {
            String val = String.valueOf(rowNum.getText());
            int numVal = Integer.parseInt(val);

            gallery.setNumColumns(numVal);
        });

        goAlbums.setOnClickListener((l) ->{
            Intent intent = new Intent(MainActivity.this, AlbumSelection.class);
            startActivity(intent);
        });

        clearData.setOnClickListener((l) ->{
            if (albums != null){
                SharedPreferences.Editor editor = albums.edit();
                editor.clear();
                editor.apply();
            }
            SharedPreferences albumEditor = getSharedPreferences("dummy", Activity.MODE_PRIVATE);
            if (albumEditor != null){
                SharedPreferences.Editor editor = albumEditor.edit();
                editor.clear();
                editor.apply();
            }
        });


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

    /**
     * The Class ImageAdapter.
     */
    private class ImageAdapter extends BaseAdapter {

        private Activity context;
        public ImageAdapter(Activity localContext) {
            context = localContext;
            images = getAllShownImagesPath(context);
//            images = new ArrayList<String>();
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

        private ArrayList<String> getAllShownImagesPath(Activity activity) {
            ArrayList<String> arrPath = new ArrayList<>();

            ContentResolver contentResolver = getContentResolver();
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            // use MediaStore.Images.Media.<Attribute> to query and stuff
            // contentResolver is the sqlite database

            try (Cursor cursor = contentResolver.query(uri, null, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC")){
                if (cursor == null) {
                    // query failed, handle error.
                } else if (!cursor.moveToFirst()) {
                    // no media on the device
                } else {
                    int i = 0;
                    int titleColumn = cursor.getColumnIndex(MediaStore.Images.Media.TITLE);
                    int idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

                    do {
                        long thisId = cursor.getLong(idColumn);
                        String thisTitle = cursor.getString(titleColumn);
                        // ...process entry...
                        String pathGot = cursor.getString(dataColumnIndex);
//                        Log.i("NOTI", pathGot);
                        arrPath.add(pathGot);
                        i++;
                    } while (cursor.moveToNext() && i < 20);
                }
            }

            return arrPath;
        }
    }
}