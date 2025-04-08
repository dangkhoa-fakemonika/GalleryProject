package com.example.galleryexample3.imageediting;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.galleryexample3.MainActivity;
import com.example.galleryexample3.R;
import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;

import java.io.OutputStream;

public class PaintingActivity extends AppCompatActivity {

    private PaintView paintView;
    private static final int STORAGE_PERMISSION_CODE = 100;
    private String imageURI;
    private Bitmap imageBitmap;
    private FrameLayout frameLayout;
    private MediaStoreObserver mediaStoreObserver;
    private AlertDialog alertDialog;

    private boolean osv = false;
    public class MediaStoreObserver extends ContentObserver {
        public MediaStoreObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            if (alertDialog != null) {
                alertDialog.dismiss();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(PaintingActivity.this);
            builder.setMessage("Ảnh không tồn tại hoặc đã bị sửa đổi.")
                    .setCancelable(false)
                    .setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(PaintingActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.paint_activity);

        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();

        imageURI = gotBundle.getString("imageURI");
        imageBitmap = BitmapFactory.decodeFile(imageURI);

        paintView = findViewById(R.id.paintView);
        frameLayout = findViewById(R.id.frameLayout);
        //configureSize();

        float fw = 387 * getResources().getDisplayMetrics().density + 0.5f;
        float fh = 600 * getResources().getDisplayMetrics().density + 0.5f;
        float iw = imageBitmap.getWidth();
        float ih = imageBitmap.getHeight();
        float sw = fw / iw;
        float sh = fh / ih;
        float s = Math.min(sw, sh);
        int nw = Math.round(iw * s);
        int nh = Math.round(ih * s);
        paintView.setLayoutParams(new FrameLayout.LayoutParams(nw, nh));
        paintView.setX((fw - nw) / 2);
        paintView.setY((fh - nh) / 2);
        paintView.setScale(nw, nh);

        findViewById(R.id.buttonRed).setOnClickListener((l) -> paintView.setBrushColor(Color.RED));
        findViewById(R.id.buttonBlue).setOnClickListener((l) -> paintView.setBrushColor(Color.BLUE));
        findViewById(R.id.buttonGreen).setOnClickListener((l) -> paintView.setBrushColor(Color.GREEN));
        findViewById(R.id.buttonBlack).setOnClickListener((l) -> paintView.setBrushColor(Color.BLACK));
        findViewById(R.id.buttonErasure).setOnClickListener((l) -> paintView.setEraser(true));
        findViewById(R.id.buttonReset).setOnClickListener((l) -> paintView.clearCanvas());
        findViewById(R.id.buttonUndo).setOnClickListener((l) -> paintView.undo());
        findViewById(R.id.buttonRedo).setOnClickListener((l) -> paintView.redo());
        findViewById(R.id.buttonSave).setOnClickListener((l) -> {
//            if (checkPermission()){
                saveDrawnImage();
//            }else{
//                requestPermission();
//            }
        });

        SeekBar seekBar = findViewById(R.id.seekBarBrushSize);
        seekBar.setProgress(10);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                paintView.setBrushSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        try (Cursor cursor = this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media.DATA + " = ?", new String[] {imageURI}, null)) {
            if (cursor == null || !cursor.moveToFirst()){
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Ảnh không tồn tại hoặc đã bị sửa đổi.")
                        .setCancelable(false)
                        .setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(PaintingActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
        if (!osv) {
            Handler handler = new Handler();
            mediaStoreObserver = new MediaStoreObserver(handler);
            try (Cursor cursor = this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media.DATA + " = ?", new String[] {imageURI}, null)) {
                if (cursor != null && cursor.moveToFirst()){
                    @SuppressLint("Range") Uri iuri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
                    this.getContentResolver().registerContentObserver(iuri, true, mediaStoreObserver);
                    osv = true;
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        this.getContentResolver().unregisterContentObserver(mediaStoreObserver);
        osv = false;
    }

    private void configureSize(){
        WindowManager mWinMgr = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        int SCREEN_WIDTH = displaymetrics.widthPixels;
        int SCREEN_HEIGHT = displaymetrics.heightPixels;

        float scalingX = (float) SCREEN_WIDTH / imageBitmap.getWidth();
        float scalingY = (float) SCREEN_HEIGHT / imageBitmap.getHeight();
        float scale = Math.min(scalingX, scalingY);

//        float scaling = Math.min(scalingX, scalingY);
        float scaling = scalingX;
//        Log.i("SCALE", scaling + "");

        int newWidth = Math.round(imageBitmap.getWidth() * scale);
        int newHeight = Math.round(imageBitmap.getHeight() * scale);

//        paintView.setLayoutParams(new FrameLayout.LayoutParams(imageBitmap.getWidth(), imageBitmap.getHeight()));
          paintView.setLayoutParams(new FrameLayout.LayoutParams(imageBitmap.getWidth(), imageBitmap.getHeight()));
//        paintView.setX((float) (SCREEN_WIDTH - imageBitmap.getWidth()) / 2);
//        paintView.setY(0);

    }

    private boolean checkPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    private void saveDrawnImage(){
        Bitmap bitmap = paintView.getBitmap();
        ImageGalleryProcessing.saveImage(this, bitmap);
    }


}