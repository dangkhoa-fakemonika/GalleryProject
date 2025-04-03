package com.example.galleryexample3;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.util.ArrayList;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.example.galleryexample3.datamanagement.AlbumsController;
import com.example.galleryexample3.datamanagement.ImageManager;
import com.example.galleryexample3.userinterface.ImageBaseAdapter;
import com.example.galleryexample3.userinterface.ScaleListener;

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

    private ImageBaseAdapter imageAdapter;

    final int PICK_FROM_GALLERY = 101; // This could be any non-0 number lol
    final int NUM_IMAGE_LOAD_LIMIT = 20;


    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    private AlbumsController albumsController;
    private int pageNum = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set permission to get images
        if (this.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{ android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
        }

        albumsController = new AlbumsController(this);

        // Element assignment
        GridView gallery = (GridView) findViewById(R.id.galleryGridView);
        rowNum = (EditText) findViewById(R.id.rowNum);
        changeColumns = (Button) findViewById(R.id.changeRowButton);
        goAlbums = (Button) findViewById(R.id.gotoAlbums);
        clearData = (Button) findViewById(R.id.clearData);
        previousPage = (Button) findViewById(R.id.previousPage);
        nextPage = (Button) findViewById(R.id.nextPage);
        pageNumber = (TextView) findViewById(R.id.pageNumber);
        useCamera = (Button) findViewById(R.id.useCamera);


        // Load adapter
        ArrayList<String> localImages = ImageManager.getImages(this);

        imageAdapter = new ImageBaseAdapter(this, NUM_IMAGE_LOAD_LIMIT, localImages);
        gallery.setAdapter(imageAdapter);
        mScaleDetector = new ScaleGestureDetector(gallery.getContext(), new ScaleListener(imageAdapter, gallery));


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

        // Clear all data
        clearData.setOnClickListener((l) ->{
            albumsController.clearData();
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
    public boolean onTouchEvent(MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);
        return true;
    }
}