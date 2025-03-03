package com.example.galleryexample3;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.lang.reflect.Array;
import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import com.bumptech.glide.Glide;

public class MainActivity extends Activity {

    /** The images. */
    private ArrayList<String> images;
    TextView tv;
    final int PICK_FROM_GALLERY = 101;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.theTextView);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
        }


        GridView gallery = (GridView) findViewById(R.id.galleryGridView);

        gallery.setAdapter(new ImageAdapter(this));

        gallery.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                if (null != images && !images.isEmpty()){
                    Toast.makeText(getApplicationContext(),"position " + position + " " + images.get(position), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, SingleImageView.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("imageURI", images.get(position));
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });

        tv.setText("Pics: " + gallery.getCount());
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

            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)){
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
                        Log.i("NOTI", pathGot);
                        arrPath.add(pathGot);
                        i++;
                    } while (cursor.moveToNext() && i < 10);
                }
            }

            return arrPath;
        }
    }
}