package com.example.galleryexample3;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.userinterface.ImageBaseAdapter;

public class MainActivity extends Activity {

//    private ArrayList<String> images;
    private EditText rowNum;
    private Button changeColumns;
    private Button goAlbums;
    private Button clearData;
    private Button previousPage;
    private Button nextPage;
    private Button useCamera;
    private TextView pageNumber;
    private GridView gallery;

    private ImageBaseAdapter imageAdapter;

    final int PICK_FROM_GALLERY = 101; // This could be any non-0 number lol
    final int REQUEST_MANAGE_EXTERNAL_STORAGE = 100;
    final int REQUEST_WRITE_EXTERNAL_STORAGE = 101;
    final int REQUEST_READ_EXTERNAL_STORAGE = 102;
    final int REQUEST_CAMERA = 103;
    final int REQUEST_RECORD_AUDIO = 104;
    final int NUM_IMAGE_LOAD_LIMIT = 20;

    private int pageNum = 0;
    private MediaStoreObserver mediaStoreObserver;
    private ArrayList<String> localImages;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MANAGE_EXTERNAL_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "Ứng dụng cần cấp quyền để hoạt động bình thường.", Toast.LENGTH_SHORT).show();
                    finishAffinity();
                }
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE || requestCode == REQUEST_READ_EXTERNAL_STORAGE || requestCode == REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //
            } else {
                Log.i("debug", "else");
                Toast.makeText(this, "Ứng dụng cần cấp quyền để hoạt động bình thường.", Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        }
    }

    private boolean osv = false;
    public class MediaStoreObserver extends ContentObserver {
        public MediaStoreObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            localImages = ImageGalleryProcessing.getImages(MainActivity.this, "DATE_ADDED", " DESC");

            imageAdapter = new ImageBaseAdapter(MainActivity.this, NUM_IMAGE_LOAD_LIMIT, localImages);
            gallery.setAdapter(imageAdapter);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Set permission to get images
//        if (this.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            Log.i("debug", "1");
//            this.requestPermissions(new String[]{ android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }
//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            this.requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
//        }


        // Element assignment
        gallery = (GridView) findViewById(R.id.galleryGridView);
        rowNum = (EditText) findViewById(R.id.rowNum);
        changeColumns = (Button) findViewById(R.id.changeRowButton);
        goAlbums = (Button) findViewById(R.id.gotoAlbums);
        clearData = (Button) findViewById(R.id.clearData);
        previousPage = (Button) findViewById(R.id.previousPage);
        nextPage = (Button) findViewById(R.id.nextPage);
        pageNumber = (TextView) findViewById(R.id.pageNumber);
        useCamera = (Button) findViewById(R.id.useCamera);


        // Load adapter
        localImages = ImageGalleryProcessing.getImages(this, "DATE_ADDED", " DESC");

        imageAdapter = new ImageBaseAdapter(this, NUM_IMAGE_LOAD_LIMIT, localImages);
        gallery.setAdapter(imageAdapter);
//        mScaleDetector = new ScaleGestureDetector(gallery.getContext(), new ScaleListener(imageAdapter, gallery));


        // Change number of columns of grid view (no restriction of data types)
        changeColumns.setOnClickListener((l) -> {
            String val = String.valueOf(rowNum.getText());
            int numVal = Integer.parseInt(val);

            gallery.setNumColumns(numVal);
            imageAdapter.setColumns(numVal);
        });

        goAlbums.setOnClickListener((l) ->{
            Intent intent = new Intent(MainActivity.this, AlbumSelection.class);
            startActivity(intent);
        });

        previousPage.setOnClickListener((l) -> {
            pageNum = (pageNum > 0) ? pageNum - 1 : pageNum;
            pageNumber.setText("Page " + (pageNum + 1));
            imageAdapter.setPage(pageNum);
            imageAdapter.notifyDataSetChanged();
        });

        nextPage.setOnClickListener((l) -> {
            pageNum = (pageNum < imageAdapter.getMaxPage()) ? pageNum + 1 : pageNum;
            pageNumber.setText("Page " + (pageNum + 1));
            imageAdapter.setPage(pageNum);
            imageAdapter.notifyDataSetChanged();
        });

        useCamera.setOnClickListener((l) -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        });


        // Single image view
        gallery.setOnItemClickListener((arg0, arg1, position, arg3) -> {
            if (!localImages.isEmpty()){
                Intent intent = new Intent(MainActivity.this, SingleImageView.class);
                Bundle bundle = new Bundle();
                bundle.putString("imageURI", localImages.get(position + pageNum * NUM_IMAGE_LOAD_LIMIT));
                bundle.putInt("position", position + pageNum * NUM_IMAGE_LOAD_LIMIT);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        localImages = ImageGalleryProcessing.getImages(this, "DATE_ADDED", " DESC");

        imageAdapter = new ImageBaseAdapter(this, NUM_IMAGE_LOAD_LIMIT, localImages);
        gallery.setAdapter(imageAdapter);

        pageNum = 0;
        pageNumber.setText("Page 1");
        imageAdapter.notifyDataSetChanged();

        if (!osv) {
            Handler handler = new Handler();
            mediaStoreObserver = new MediaStoreObserver(handler);

            ContentResolver contentResolver = this.getContentResolver();
            contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, mediaStoreObserver);
            osv = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        this.getContentResolver().unregisterContentObserver(mediaStoreObserver);
        osv = false;
    }

//
//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        // Let the ScaleGestureDetector inspect all events.
//        mScaleDetector.onTouchEvent(ev);
//        return true;
//    }
}