package com.example.galleryexample3.imageediting;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.galleryexample3.MainActivity;
import com.example.galleryexample3.MainActivityNew;
import com.example.galleryexample3.R;
import com.example.galleryexample3.businessclasses.ImageEditingController;
import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.dataclasses.EditParams;
import com.example.galleryexample3.dataclasses.FilterPreview;
import com.example.galleryexample3.userinterface.AdjustmenOptionAdapter;
import com.example.galleryexample3.userinterface.FilterPreviewAdapter;
import com.example.galleryexample3.userinterface.ItemClickSupporter;
import com.example.galleryexample3.userinterface.ThemeManager;

import java.util.ArrayList;
import java.util.Objects;

public class EditActivity extends AppCompatActivity {
    ImageView imageView;
    SeekBar adjustmentSeekBar;
    private String mode = "Adjustment";
    private String submode = "Brightness";
    private String imageURI;
    Bitmap srcBitmap;
    Bitmap editedBitmap;
    private EditParams currentParams;
    private ImageEditingController imageEditingController;
    float currentRotation = 0f;
    boolean isFlippedHorizontally = false;
    boolean isFlippedVertically = false;
    ArrayList<String> adjustmentList;
    ArrayList<FilterPreview> filterList;
    private MediaStoreObserver mediaStoreObserver;
    private AlertDialog alertDialog;
    Context context;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable applyAdjustmentRunnable = null;
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

            AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
            builder.setMessage("Ảnh không tồn tại hoặc đã bị sửa đổi.")
                    .setCancelable(false)
                    .setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(EditActivity.this, MainActivityNew.class);
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
        setContentView(R.layout.edit_view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        context = this;

        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();

        imageURI = gotBundle.getString("imageURI");
        srcBitmap = BitmapFactory.decodeFile(imageURI);
//        modBitmap = srcBitmap.copy(Objects.requireNonNull(srcBitmap.getConfig()), true);

        filterList = new ArrayList<>();
        adjustmentList = new ArrayList<>();
        adjustmentList.add("Brightness");
        adjustmentList.add("Contrast");
        adjustmentList.add("Saturation");

        TextView modeTextView = (TextView) findViewById(R.id.modeTextView);
        TextView subModeTextView = (TextView) findViewById(R.id.subModeTextView);
        Button adjustmentButton = (Button) findViewById(R.id.adjustmentButton);
        Button filterButton = (Button) findViewById(R.id.filterButton);
        Button transformButton = (Button) findViewById(R.id.transformButton);
        adjustmentSeekBar = findViewById(R.id.adjustmentSeekBar);
        imageView = findViewById(R.id.imageView);
        RecyclerView adjustmentOption = (RecyclerView) findViewById(R.id.adjustmentOption);
        RecyclerView filterPreviewImage = (RecyclerView) findViewById(R.id.filterPreviewImage);
        LinearLayout effectButtonBar = (LinearLayout) findViewById(R.id.effectButtonBar);
        LinearLayout transformButtonBar = (LinearLayout) findViewById(R.id.transformButtonBar);

        CropOverlayView cropOverlay = (CropOverlayView) findViewById(R.id.cropOverlay);
        ImageButton undoButton = (ImageButton) findViewById(R.id.undoButton);
        ImageButton redoButton = (ImageButton) findViewById(R.id.redoButton);
        ImageButton rotateLeftButton = (ImageButton) findViewById(R.id.rotateLeftButton);
        ImageButton rotateRightButton = (ImageButton) findViewById(R.id.rotateRightButton);
        ImageButton flipButton = (ImageButton) findViewById(R.id.flipButton);

        Button saveButton = (Button) findViewById(R.id.saveButton);

        saveButton.setOnClickListener((l) -> {
            if (Objects.equals(mode, "Adjustment")) {
                ImageGalleryProcessing.saveImage(this, editedBitmap);
            } else if (Objects.equals(mode, "Filter")) {
                ImageGalleryProcessing.saveImage(this, editedBitmap);
            } else {
//                Bitmap temp = Bitmap.createBitmap(modBitmap, (int)((rectangleCrop.getX() - editedImage.getX()) / editedImage.getWidth() * modBitmap.getWidth()), (int)((rectangleCrop.getY() - editedImage.getY()) / editedImage.getHeight() * modBitmap.getHeight()), (int)((float) rectangleCrop.getWidth() / editedImage.getWidth() * modBitmap.getWidth()), (int)((float) rectangleCrop.getHeight() / editedImage.getHeight() * modBitmap.getHeight()));
                ImageGalleryProcessing.saveImage(this, editedBitmap);
            }
        });

        adjustmentButton.setOnClickListener(l -> {
            if (!mode.equals("Adjustment")) {
                adjustmentButton.setTextColor(Color.WHITE);
                filterButton.setTextColor(getResources().getColor(R.color.button_noselected, getTheme()));
                transformButton.setTextColor(getResources().getColor(R.color.button_noselected, getTheme()));

                effectButtonBar.setVisibility(View.VISIBLE);
                transformButtonBar.setVisibility(View.INVISIBLE);

                mode = "Adjustment";
                modeTextView.setText(mode);
                submode = "Brightness";
                subModeTextView.setText(submode);
                subModeTextView.setVisibility(View.VISIBLE);

                adjustmentOption.setVisibility(View.VISIBLE);
                adjustmentSeekBar.setVisibility(View.VISIBLE);

                filterPreviewImage.setVisibility(View.INVISIBLE);

                cropOverlay.setVisibility(View.INVISIBLE);

                updateImage();
            }
        });

        filterButton.setOnClickListener(l -> {
            if (!mode.equals("Filter")) {
                adjustmentButton.setTextColor(getResources().getColor(R.color.button_noselected, getTheme()));
                filterButton.setTextColor(Color.WHITE);
                transformButton.setTextColor(getResources().getColor(R.color.button_noselected, getTheme()));

                effectButtonBar.setVisibility(View.VISIBLE);
                transformButtonBar.setVisibility(View.INVISIBLE);

                mode = "Filter";
                modeTextView.setText(mode);
                submode = "Normal";
                subModeTextView.setText(submode);
                subModeTextView.setVisibility(View.VISIBLE);

                adjustmentOption.setVisibility(View.INVISIBLE);
                adjustmentSeekBar.setVisibility(View.INVISIBLE);

                filterPreviewImage.setVisibility(View.VISIBLE);

                cropOverlay.setVisibility(View.INVISIBLE);

                updateImage();
            }
        });

        transformButton.setOnClickListener(l -> {
            if (!mode.equals("Transform")) {
                adjustmentButton.setTextColor(getResources().getColor(R.color.button_noselected, getTheme()));
                filterButton.setTextColor(getResources().getColor(R.color.button_noselected, getTheme()));
                transformButton.setTextColor(Color.WHITE);

                effectButtonBar.setVisibility(View.INVISIBLE);
                transformButtonBar.setVisibility(View.VISIBLE);

                mode = "Transform";
                modeTextView.setText(mode);
                submode = "Crop";
                subModeTextView.setVisibility(View.INVISIBLE);

                adjustmentOption.setVisibility(View.INVISIBLE);
                adjustmentSeekBar.setVisibility(View.INVISIBLE);

                filterPreviewImage.setVisibility(View.INVISIBLE);

                cropOverlay.setBound(imageView, 0f);
                cropOverlay.setVisibility(View.VISIBLE);
            }
        });

        AdjustmenOptionAdapter adjustmentOptionAdapter = new AdjustmenOptionAdapter(this, adjustmentList);
        adjustmentOption.setAdapter(adjustmentOptionAdapter);

        Glide.with(this)
                .asBitmap()
                .load(srcBitmap)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        currentParams = new EditParams(0, 1, 1, "normal");
                        imageEditingController = new ImageEditingController(currentParams);

                        filterList = imageEditingController.generateFilterPreviews(srcBitmap);
                        FilterPreviewAdapter filterPreviewAdapter = new FilterPreviewAdapter(context, filterList);
                        filterPreviewImage.setAdapter(filterPreviewAdapter);
                        updateImage();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        return;
                    }

                });

        ItemClickSupporter.addTo(filterPreviewImage).setOnItemClickListener(new ItemClickSupporter.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                FilterPreview fp = filterList.get(position);
                submode = fp.getFilterName();
                subModeTextView.setText(submode);

                EditParams newParams = imageEditingController.getCurrentParams();
                newParams.filter = submode.toLowerCase();

                imageEditingController.addNewParams(newParams);
                updateImage();
            }
        });

        ItemClickSupporter.addTo(adjustmentOption).setOnItemClickListener(new ItemClickSupporter.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                submode = adjustmentList.get(position);
                subModeTextView.setText(submode);
                updateSeekBar();
            }
        });

        adjustmentSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;

                if (applyAdjustmentRunnable != null)
                    handler.removeCallbacks(applyAdjustmentRunnable);

                applyAdjustmentRunnable = () -> {
                    EditParams newParams = imageEditingController.getCurrentParams();
                    float value = progress;

                    switch (submode) {
                        case "Brightness":
                            newParams.brightness = value - 100;
                            break;
                        case "Contrast":
                            newParams.contrast = (value / 200f) + 0.5f;
                            break;
                        case "Saturation":
                            newParams.saturation = value / 100f;
                            break;
                    }

                    imageEditingController.addNewParams(newParams);
                    updateImage();
                };
                handler.postDelayed(applyAdjustmentRunnable, 500);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        undoButton.setOnClickListener(v -> {
            if (imageEditingController.canUndo()) {
                imageEditingController.undo();
                updateSeekBar();
                updateImage();
            }
        });

        redoButton.setOnClickListener(v -> {
            if (imageEditingController.canRedo()) {
                imageEditingController.redo();
                updateSeekBar();
                updateImage();
            }
        });

        rotateLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentRotation -= 90f;
                imageView.animate()
                        .rotation(currentRotation)
                        .setDuration(300)
                        .start();
                cropOverlay.setBound(imageView, currentRotation);
            }
        });

        rotateRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentRotation += 90f;
                imageView.animate()
                        .rotation(currentRotation)
                        .setDuration(300)
                        .start();
                cropOverlay.setBound(imageView, currentRotation);
            }
        });

        flipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float visualRotation = (currentRotation % 360 + 360) % 360;

                if (visualRotation == 90f || visualRotation == 270f) {
                    isFlippedVertically = !isFlippedVertically;
                    imageView.animate().scaleY(isFlippedVertically ? -1f : 1f).setDuration(300).start();
                } else {
                    isFlippedHorizontally = !isFlippedHorizontally;
                    imageView.animate().scaleX(isFlippedHorizontally ? -1f : 1f).setDuration(300).start();
                }
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

                AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
                builder.setMessage("Ảnh không tồn tại hoặc đã bị sửa đổi.")
                        .setCancelable(false)
                        .setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(EditActivity.this, MainActivity.class);
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

    private void updateImage() {
        editedBitmap = imageEditingController.applyEdit(srcBitmap);
        Glide.with(this).load(editedBitmap).into(imageView);
    }

    private void updateSeekBar() {
        switch (submode) {
            case "Brightness":
                adjustmentSeekBar.setProgress((int) (imageEditingController.getCurrentParams().brightness + 100));
                break;
            case "Contrast":
                adjustmentSeekBar.setProgress((int) ((imageEditingController.getCurrentParams().contrast - 0.5) * 200));
                break;
            case "Saturation":
                adjustmentSeekBar.setProgress((int) (imageEditingController.getCurrentParams().saturation * 100));
                break;
        }
    }
}