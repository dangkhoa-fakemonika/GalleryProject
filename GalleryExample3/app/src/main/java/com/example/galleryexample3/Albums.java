package com.example.galleryexample3;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class Albums extends Activity {
    /** The images. */
    private ArrayList<String> images;
    final int PICK_FROM_GALLERY = 101;
    private EditText rowNum;
    private Button changeRows;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_view);

        if (ActivityCompat.checkSelfPermission(Albums.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Albums.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
        }


        GridView gallery = (GridView) findViewById(R.id.galleryGridView);
        rowNum = (EditText) findViewById(R.id.rowNum);
        changeRows = (Button) findViewById(R.id.changeRowButton);

        gallery.setAdapter(new ImageAdapter(this));

        // Change columns of the thing (not restricted yet)
        changeRows.setOnClickListener((l) -> {
            String val = String.valueOf(rowNum.getText());
            int numVal = Integer.parseInt(val);

            gallery.setNumColumns(numVal);
        });

        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                if (null != images && !images.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "position " + position + " " + images.get(position), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Albums.this, SingleImageView.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("imageURI", images.get(position));
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
    }

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

            SharedPreferences albums = getSharedPreferences("dummy", Activity.MODE_PRIVATE);
            if (albums == null)
                return arrPath;

            HashSet<String> hashSet = new HashSet<>(Objects.requireNonNull(albums.getStringSet("dummy", null)));
            arrPath.addAll(hashSet);

            return arrPath;
        }
    }
}




