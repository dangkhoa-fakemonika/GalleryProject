package com.example.galleryexample3.imageediting;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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

import com.example.galleryexample3.R;

import java.io.OutputStream;

public class PaintingActivity extends AppCompatActivity {

    private PaintView paintView;
    private static final int STORAGE_PERMISSION_CODE = 100;
    private String imageURI;
    private Bitmap imageBitmap;
    private FrameLayout frameLayout;

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
        configureSize();

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

    private void configureSize(){
        WindowManager mWinMgr = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        int SCREEN_WIDTH = displaymetrics.widthPixels;
        int SCREEN_HEIGHT = displaymetrics.heightPixels;

        float scalingX = (float) SCREEN_WIDTH / imageBitmap.getWidth();
//        float scalingY = (float) SCREEN_HEIGHT / imageBitmap.getHeight();

//        float scaling = Math.min(scalingX, scalingY);
        float scaling = scalingX;
//        Log.i("SCALE", scaling + "");

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
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "image_" + System.currentTimeMillis() + ".png");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/HeavensDoor");

        Uri result = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if (result != null){
            try {
                OutputStream outputStream = getContentResolver().openOutputStream(result, "w");
                if (outputStream != null){
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.close();
                    Toast.makeText(this, "Image saved!", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e){
                Toast.makeText(this, "Can't save image.", Toast.LENGTH_SHORT).show();
            }
        }
    }


}