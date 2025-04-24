package com.example.galleryexample3.imageediting;

import static java.lang.Integer.parseInt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.galleryexample3.MainActivity;
import com.example.galleryexample3.R;
import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.userinterface.ThemeManager;

import java.util.ArrayList;

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
        ThemeManager.setTheme(this);
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

        ImageButton eraserButton = findViewById(R.id.buttonErasure);
        eraserButton.setOnClickListener((l) -> {
            paintView.setEraser(true);
            eraserButton.setBackgroundColor(Color.LTGRAY);
        });
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

        ImageButton colorButton = (ImageButton) findViewById(R.id.buttonSelectColor);
        colorButton.setOnClickListener((l) -> {
            paintView.setEraser(false);
            eraserButton.setBackgroundColor(Color.TRANSPARENT);
            View dialogView = LayoutInflater.from(PaintingActivity.this).inflate(R.layout.color_picker, null);
            SeekBar redBar = dialogView.findViewById(R.id.redBar);
            SeekBar greenBar = dialogView.findViewById(R.id.greenBar);
            SeekBar blueBar = dialogView.findViewById(R.id.blueBar);

            EditText redValueEdit = dialogView.findViewById(R.id.redValueEdit);
            EditText greenValueEdit = dialogView.findViewById(R.id.greenValueEdit);
            EditText blueValueEdit = dialogView.findViewById(R.id.blueValueEdit);

            View previousColor = dialogView.findViewById(R.id.previousColor);
            View currentColor = dialogView.findViewById(R.id.choosingColor);

            int currentBrush = paintView.getBrushColor();

            previousColor.setBackgroundColor(currentBrush);
            currentColor.setBackgroundColor(currentBrush);

            ArrayList<Integer> rgbValue = new ArrayList<>();

            rgbValue.add(Color.red(currentBrush));
            rgbValue.add(Color.green(currentBrush));
            rgbValue.add(Color.blue(currentBrush));

            redBar.setProgress(Color.red(currentBrush));
            greenBar.setProgress(Color.green(currentBrush));
            blueBar.setProgress(Color.blue(currentBrush));

            redBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    rgbValue.set(0, i);
                    redValueEdit.setText(i + "");
                    currentColor.setBackgroundColor(Color.rgb(rgbValue.get(0),rgbValue.get(1),rgbValue.get(2)));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            greenBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    rgbValue.set(1, i);
                    greenValueEdit.setText(i + "");
                    currentColor.setBackgroundColor(Color.rgb(rgbValue.get(0),rgbValue.get(1),rgbValue.get(2)));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            blueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    rgbValue.set(2, i);
                    blueValueEdit.setText(i + "");
                    currentColor.setBackgroundColor(Color.rgb(rgbValue.get(0),rgbValue.get(1),rgbValue.get(2)));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });


            redValueEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.length() == 0) return;
                    int getRedValue = Integer.parseInt(charSequence.toString());
                    if (getRedValue >= 0 && getRedValue <= 255){
                        rgbValue.set(0, getRedValue);
                        redBar.setProgress(getRedValue);
                        currentColor.setBackgroundColor(Color.rgb(rgbValue.get(0),rgbValue.get(1),rgbValue.get(2)));
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            greenValueEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.length() == 0) return;
                    int getGreenValue = Integer.parseInt(charSequence.toString());
                    if (getGreenValue >= 0 && getGreenValue <= 255){
                        rgbValue.set(1, getGreenValue);
                        greenBar.setProgress(getGreenValue);
                        currentColor.setBackgroundColor(Color.rgb(rgbValue.get(0),rgbValue.get(1),rgbValue.get(2)));
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            blueValueEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.length() == 0) return;
                    int getBlueValue = Integer.parseInt(charSequence.toString());
                    if (getBlueValue >= 0 && getBlueValue <= 255){
                        rgbValue.set(2, getBlueValue);
                        blueBar.setProgress(getBlueValue);
                        currentColor.setBackgroundColor(Color.rgb(rgbValue.get(0),rgbValue.get(1),rgbValue.get(2)));
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            final int[] brushSize = {Math.round(paintView.getBrushSize())};

            SeekBar sizeBar = dialogView.findViewById(R.id.seekBarBrushSize);
            EditText sizeEditText = dialogView.findViewById(R.id.brushSizeEditText);
            sizeBar.setProgress(Math.round(paintView.getBrushSize()));
            sizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                   brushSize[0] = progress;
                   sizeEditText.setText(progress + "");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            sizeEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.length() == 0) return;
                    int sizeValue = Integer.parseInt(charSequence.toString());
                    if (sizeValue >= 10 && sizeValue <= 50){
                        sizeBar.setProgress(sizeValue);
                        brushSize[0] = sizeValue;
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });


            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Change Brush Color")
                    .setView(dialogView)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            paintView.setBrushColor(Color.rgb(rgbValue.get(0),rgbValue.get(1),rgbValue.get(2)));
                            colorButton.setBackgroundColor(Color.rgb(rgbValue.get(0),rgbValue.get(1),rgbValue.get(2)));
                            paintView.setBrushSize(brushSize[0]);
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create();
            alertDialog.show();
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
                builder.setMessage("Image no longer available.")
                        .setCancelable(false)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
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