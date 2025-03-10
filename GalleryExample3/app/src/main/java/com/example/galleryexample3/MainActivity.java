package com.example.galleryexample3;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import com.bumptech.glide.Glide;
import com.example.galleryexample3.datamanagement.AlbumsController;

public class MainActivity extends Activity {

    private ArrayList<String> images;
    private EditText rowNum;
    private Button changeColumns;
    private Button goAlbums;
    private Button clearData;
    private Button previousPage;
    private Button nextPage;
    private TextView pageNumber;

    private ImageAdapter imageAdapter;

    final int PICK_FROM_GALLERY = 101; // This could be any non-0 number lol
    final int NUM_IMAGE_LOAD_LIMIT = 20;

    int SCREEN_WIDTH = 1; // Number of images per load
    int SCREEN_HEIGHT = 1;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    private AlbumsController albumsController;
    private int pageNum = 0;

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
        changeColumns = (Button) findViewById(R.id.changeRowButton);
        goAlbums = (Button) findViewById(R.id.gotoAlbums);
        clearData = (Button) findViewById(R.id.clearData);
        previousPage = (Button) findViewById(R.id.previousPage);
        nextPage = (Button) findViewById(R.id.nextPage);
        pageNumber = (TextView) findViewById(R.id.pageNumber);

        WindowManager mWinMgr = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        SCREEN_WIDTH = displaymetrics.widthPixels;
        SCREEN_HEIGHT = displaymetrics.heightPixels;

        // Load adapter


        imageAdapter = new ImageAdapter(this, NUM_IMAGE_LOAD_LIMIT);
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

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);
        return true;
    }


    private class ImageAdapter extends BaseAdapter {
        private final Activity context;
        private int columns;
        private int page;
        private final int maxPage;
        private final int loadLimit;

        private ArrayList<String> pageImages;

        public ImageAdapter(Activity localContext, int setLoadLimit) {
            context = localContext;
            images = getAllShownImagesPath(context);
            columns = 2;
            page = 0;
            maxPage = images.size() / setLoadLimit;
            loadLimit = setLoadLimit;
        }

        public void setColumns(int columns) {
            this.columns = columns;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getMaxPage() {
            return maxPage;
        }

        public int getColumns() {
            return columns;
        }

        public int getCount() {
            return loadLimit;
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
                        .setLayoutParams(new GridView.LayoutParams(SCREEN_WIDTH / columns, SCREEN_WIDTH / columns));

            } else {
                picturesView = (ImageView) convertView;
            }

            pageImages = new ArrayList<>(images.subList(page * loadLimit, Math.min((page + 1) * loadLimit, images.size())));

            Glide.with(context).load(pageImages.get(position))
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
                    } while (cursor.moveToNext()); // Load limit
                }
            }

            return arrPath;
        }
    }

    private static class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private final ImageAdapter imageAdapter;
        private final GridView gridView;

        public ScaleListener(ImageAdapter imageAdapter, GridView gridView){
            super();
            this.imageAdapter = imageAdapter;
            this.gridView = gridView;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            int col = imageAdapter.getColumns();
            Log.i("SCALE", "" + detector.getScaleFactor());

            if (detector.getScaleFactor() < 0.98f && col > 2)
                col--;
            else if (detector.getScaleFactor() > 1.02f && col < 6)
                col++;

            imageAdapter.setColumns(col);
            gridView.setNumColumns(col);

            return true;
        }
    }
}