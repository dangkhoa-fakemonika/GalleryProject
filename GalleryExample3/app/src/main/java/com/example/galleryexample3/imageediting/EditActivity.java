package com.example.galleryexample3.imageediting;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
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
import com.example.galleryexample3.MainActivityNew;
import com.example.galleryexample3.R;
import com.example.galleryexample3.businessclasses.ImageEditingController;
import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.dataclasses.AdjustmentOption;
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
    CropOverlayView cropOverlay;
    SeekBar adjustmentSeekBar;
    private String mode = "Adjustment";
    private String submode = "Brightness";
    private String imageURI;
    Bitmap sourceBitmap;
    Bitmap editedBitmap;
    private EditParams currentParams;
    private ImageEditingController imageEditingController;
    float currentRotation = 0f;
    boolean isFlippedHorizontally = false;
    boolean isFlippedVertically = false;
    ArrayList<AdjustmentOption> adjustmentList;
    ArrayList<AdjustmentOption> cropList;
    AdjustmenOptionAdapter adjustmentOptionAdapter;
    ArrayList<FilterPreview> filterList;
    private MediaStoreObserver mediaStoreObserver;
    private AlertDialog alertDialog;
    Context context;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable applyAdjustmentRunnable = null;
    private boolean osv = false;
    private boolean effectOn = true;
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
                    .setPositiveButton("Xác nhận", (dialog, id) -> {
                        finish();
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Theme, Layout setting
        super.onCreate(savedInstanceState);
        ThemeManager.setTheme(this);
        setContentView(R.layout.edit_view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // View setting
        TextView modeTextView = findViewById(R.id.modeTextView);
        TextView subModeTextView = findViewById(R.id.subModeTextView);

        imageView = findViewById(R.id.imageView);
        cropOverlay = findViewById(R.id.cropOverlay);

        LinearLayout effectButtonBar = findViewById(R.id.effectButtonBar);
        ImageButton undoButton = findViewById(R.id.undoButton);
        ImageButton redoButton = findViewById(R.id.redoButton);
        ImageButton resetButton = findViewById(R.id.resetButton);

        LinearLayout transformButtonBar = findViewById(R.id.transformButtonBar);
        ImageButton rotateLeftButton = findViewById(R.id.rotateLeftButton);
        ImageButton rotateRightButton = findViewById(R.id.rotateRightButton);
        ImageButton flipButton = findViewById(R.id.flipButton);

        RecyclerView filterPreviewImage = findViewById(R.id.filterPreviewImage);
        RecyclerView adjustmentOption = findViewById(R.id.adjustmentOption);
        adjustmentSeekBar = findViewById(R.id.adjustmentSeekBar);

        Button adjustmentButton = findViewById(R.id.adjustmentButton);
        Button filterButton = findViewById(R.id.filterButton);
        Button transformButton = findViewById(R.id.transformButton);
        Button cancelButton = findViewById(R.id.cancelButton);
        Button saveButton = findViewById(R.id.saveButton);

        modeTextView.setText("Adjustment");
        subModeTextView.setText("Brightness");
        cropOverlay.setVisibility(View.INVISIBLE);

        // Initiate data
        context = this;

        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();
        imageURI = gotBundle.getString("imageURI");
        sourceBitmap = BitmapFactory.decodeFile(imageURI);
        editedBitmap = sourceBitmap.copy(sourceBitmap.getConfig() != null
                        ? sourceBitmap.getConfig()
                        : Bitmap.Config.ARGB_8888,
                true
        );

        filterList = new ArrayList<>();
        adjustmentList = new ArrayList<>();
        cropList = new ArrayList<>();

        adjustmentList.add(new AdjustmentOption(R.drawable.brightness_24dp, "Brightness", 0, 0));
        adjustmentList.add(new AdjustmentOption(R.drawable.contrast_24dp, "Contrast", 1, 1));
        adjustmentList.add(new AdjustmentOption(R.drawable.saturation_24dp, "Saturation", 1, 1));
        adjustmentList.add(new AdjustmentOption(R.drawable.blur_sharpen_24dp, "Blur | Sharp", 0, 0));

        cropList.add(new AdjustmentOption(R.drawable.crop_free_24dp, "Free Form", 0, 0));
        cropList.add(new AdjustmentOption(R.drawable.crop_square_24dp, "1:1", 1f, 1f));
        cropList.add(new AdjustmentOption(R.drawable.crop_16_9_24dp, "16:9", 16f / 9, 16f / 9));
        cropList.add(new AdjustmentOption(R.drawable.crop_5_4_24dp, "5:4", 5f / 4, 5f / 4));
        cropList.add(new AdjustmentOption(R.drawable.crop_3_2_24dp, "3:2", 3f / 2, 3f / 2));

        adjustmentOptionAdapter = new AdjustmenOptionAdapter(this, adjustmentList);
        adjustmentOption.setAdapter(adjustmentOptionAdapter);

        Glide.with(this)
                .asBitmap()
                .load(sourceBitmap)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        currentParams = new EditParams(0, 1, 1, "normal", 0);
                        imageEditingController = new ImageEditingController(currentParams);

                        filterList = imageEditingController.generateFilterPreviews(sourceBitmap);
                        FilterPreviewAdapter filterPreviewAdapter = new FilterPreviewAdapter(context, filterList);
                        filterPreviewImage.setAdapter(filterPreviewAdapter);
                        updateImage();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });

        ItemClickSupporter.addTo(filterPreviewImage).setOnItemClickListener((recyclerView, position, v) -> {
            FilterPreview fp = filterList.get(position);
            submode = fp.getFilterName();
            subModeTextView.setText(submode);

            EditParams newParams = imageEditingController.getCurrentParams();
            newParams.filter = submode.toLowerCase();

            imageEditingController.addNewParams(newParams);
            updateImage();
        });

        ItemClickSupporter.addTo(adjustmentOption).setOnItemClickListener((recyclerView, position, v) -> {
            if (mode.equals("Adjustment"))
                submode = adjustmentList.get(position).getName();
            else {
                submode = cropList.get(position).getName();
                cropOverlay.setRatio(cropList.get(position).getValue());
                cropOverlay.setBound(imageView, currentRotation);
            }

            subModeTextView.setText(submode);
            updateSeekBar();
        });

        imageView.setOnClickListener(l -> {
            if (effectOn) {
                effectOn = false;
                Glide.with(this).load(sourceBitmap).into(imageView);
            } else {
                effectOn = true;
                Glide.with(this).load(editedBitmap).into(imageView);
            }
        });

        cancelButton.setOnClickListener((l) -> {
            finish();
        });

        saveButton.setOnClickListener((l) -> {
            if (!mode.equals("Transform"))
                ImageGalleryProcessing.saveImage(this, editedBitmap);
            else {
                ImageGalleryProcessing.saveImage(this, getCroppedBitmap());
            }
            finish();
        });

        adjustmentSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;

                if (applyAdjustmentRunnable != null)
                    handler.removeCallbacks(applyAdjustmentRunnable);

                applyAdjustmentRunnable = () -> {
                    EditParams newParams = imageEditingController.getCurrentParams();
                    float value;

                    switch (submode) {
                        case "Brightness":
                            value = progress - 100f;
                            newParams.brightness = value;
                            adjustmentOptionAdapter.updateValue(0, value);
                            break;
                        case "Contrast":
                            value = (progress / 200f) + 0.5f;
                            newParams.contrast = value;
                            adjustmentOptionAdapter.updateValue(1, value);

                            break;
                        case "Saturation":
                            value = progress / 100f;
                            newParams.saturation = value;
                            adjustmentOptionAdapter.updateValue(2, value);
                            break;
                        case "Blur | Sharp":
                            value = progress - 5f;
                            newParams.radius = (int) value;
                            adjustmentOptionAdapter.updateValue(3, value);
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

        // Undo latest step
        undoButton.setOnClickListener(v -> {
            if (imageEditingController.canUndo()) {
                updateAdjustmentView(imageEditingController.undo());
                updateSeekBar();
                updateImage();
            }
        });

        // Redo last step
        redoButton.setOnClickListener(v -> {
            if (imageEditingController.canRedo()) {
                updateAdjustmentView(imageEditingController.redo());
                updateSeekBar();
                updateImage();
            }
        });

        // Reset effect
        resetButton.setOnClickListener(v -> {
            if (imageEditingController.canUndo()) {
                updateAdjustmentView(imageEditingController.reset());
                updateSeekBar();
                editedBitmap = sourceBitmap.copy(sourceBitmap.getConfig() != null
                        ? sourceBitmap.getConfig()
                        : Bitmap.Config.ARGB_8888,
                        true
                );
                Glide.with(this).load(sourceBitmap).into(imageView);
            }
        });

        // Rotate left
        rotateLeftButton.setOnClickListener(v -> {
            currentRotation -= 90f;
            imageView.animate()
                    .rotation(currentRotation)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            cropOverlay.animate()
                                    .alpha(0f)
                                    .setDuration(100)
                                    .start();
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            cropOverlay.setBound(imageView, currentRotation);
                            cropOverlay.animate()
                                    .alpha(1f)
                                    .setDuration(100)
                                    .start();
                        }
                    });
        });

        // Rotate right
        rotateRightButton.setOnClickListener(v -> {
            currentRotation += 90f;
            imageView.animate()
                    .rotation(currentRotation)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            cropOverlay.animate()
                                    .alpha(0f)
                                    .setDuration(100)
                                    .start();
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            cropOverlay.setBound(imageView, currentRotation);
                            cropOverlay.animate()
                                    .alpha(1f)
                                    .setDuration(100)
                                    .start();
                        }
                    });
        });

        // Flip image
        flipButton.setOnClickListener(v -> {
            float visualRotation = (currentRotation % 360 + 360) % 360;

            if (visualRotation == 90f || visualRotation == 270f) {
                isFlippedVertically = !isFlippedVertically;
                imageView.animate()
                        .scaleY(isFlippedVertically ? -1f : 1f)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                cropOverlay.animate()
                                        .alpha(0f)
                                        .setDuration(100)
                                        .start();
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                cropOverlay.setBound(imageView, currentRotation);
                                cropOverlay.animate()
                                        .alpha(1f)
                                        .setDuration(100)
                                        .start();
                            }
                        });
            } else {
                isFlippedHorizontally = !isFlippedHorizontally;
                imageView.animate()
                        .scaleX(isFlippedHorizontally ? -1f : 1f)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                cropOverlay.animate()
                                        .alpha(0f)
                                        .setDuration(100)
                                        .start();
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                cropOverlay.setBound(imageView, currentRotation);
                                cropOverlay.animate()
                                        .alpha(1f)
                                        .setDuration(100)
                                        .start();
                            }
                        });
            }

            if (isFlippedHorizontally && isFlippedVertically) {
                isFlippedHorizontally = false;
                isFlippedVertically = false;
            }
        });

        // Switch to adjustment mode
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

                adjustmentOptionAdapter.replaceOptionList(adjustmentList);
                adjustmentOption.setVisibility(View.VISIBLE);
                adjustmentSeekBar.setVisibility(View.VISIBLE);

                filterPreviewImage.setVisibility(View.INVISIBLE);

                cropOverlay.setVisibility(View.INVISIBLE);

            }
        });

        // Switch to filter mode
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

                adjustmentOption.setVisibility(View.INVISIBLE);
                adjustmentSeekBar.setVisibility(View.INVISIBLE);

                filterList = imageEditingController.generateFilterPreviews(sourceBitmap);
                FilterPreviewAdapter filterPreviewAdapter = (FilterPreviewAdapter) filterPreviewImage.getAdapter();
                if (filterPreviewAdapter != null)
                    filterPreviewAdapter.updateFilterList(filterList);
                filterPreviewImage.setVisibility(View.VISIBLE);

                cropOverlay.setVisibility(View.INVISIBLE);

            }
        });

        // Switch to transform mode
        transformButton.setOnClickListener(l -> {
            if (!mode.equals("Transform")) {
                adjustmentButton.setTextColor(getResources().getColor(R.color.button_noselected, getTheme()));
                filterButton.setTextColor(getResources().getColor(R.color.button_noselected, getTheme()));
                transformButton.setTextColor(Color.WHITE);

                effectButtonBar.setVisibility(View.INVISIBLE);
                transformButtonBar.setVisibility(View.VISIBLE);

                mode = "Transform";
                modeTextView.setText(mode);
                submode = "Free Form";
                subModeTextView.setText(submode);

                adjustmentOptionAdapter.replaceOptionList(cropList);
                adjustmentOption.setVisibility(View.VISIBLE);
                adjustmentSeekBar.setVisibility(View.INVISIBLE);

                filterPreviewImage.setVisibility(View.INVISIBLE);

                cropOverlay.setBound(imageView, currentRotation);
                cropOverlay.setVisibility(View.VISIBLE);
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
                        .setPositiveButton("Xác nhận", (dialog, id) -> {
                            Intent intent = new Intent(EditActivity.this, MainActivityNew.class);
                            startActivity(intent);
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
        editedBitmap = imageEditingController.applyEdit(sourceBitmap);
        Glide.with(this).load(editedBitmap).into(imageView);
    }

    private void updateSeekBar() {
        switch (submode) {
            case "Brightness":
                adjustmentSeekBar.setMin(0);
                adjustmentSeekBar.setMax(200);
                adjustmentSeekBar.setProgress((int) (imageEditingController.getCurrentParams().brightness + 100));
                break;
            case "Contrast":
                adjustmentSeekBar.setMin(0);
                adjustmentSeekBar.setMax(200);
                adjustmentSeekBar.setProgress((int) ((imageEditingController.getCurrentParams().contrast - 0.5) * 200));
                break;
            case "Saturation":
                adjustmentSeekBar.setMin(0);
                adjustmentSeekBar.setMax(200);
                adjustmentSeekBar.setProgress((int) (imageEditingController.getCurrentParams().saturation * 100));
                break;
            case "Blur | Sharp":
                adjustmentSeekBar.setMin(0);
                adjustmentSeekBar.setMax(10);
                adjustmentSeekBar.setProgress(imageEditingController.getCurrentParams().radius + 5);
        }
    }

    private void updateAdjustmentView(EditParams currentParams) {
        adjustmentOptionAdapter.updateValue(0, currentParams.brightness);
        adjustmentOptionAdapter.updateValue(1, currentParams.contrast);
        adjustmentOptionAdapter.updateValue(2, currentParams.saturation);
        adjustmentOptionAdapter.updateValue(3, currentParams.radius);
    }

    public Bitmap getTransformedBitmap() {
        Matrix matrix = new Matrix();
        matrix.preScale(isFlippedHorizontally ? -1f : 1f, isFlippedVertically ? -1f : 1f);

        float visualRotation = (currentRotation % 360 + 360) % 360;
        matrix.postRotate(visualRotation);

        return Bitmap.createBitmap(
                editedBitmap, 0, 0,
                editedBitmap.getWidth(), editedBitmap.getHeight(),
                matrix, true
        );
    }

    public Bitmap getCroppedBitmap() {
        Bitmap transformedBitmap = getTransformedBitmap();
        RectF cropRect = cropOverlay.getCropRect();
        RectF boundRect = cropOverlay.getBoundRect();

        float scaleX = (float) transformedBitmap.getWidth() / boundRect.width();
        float scaleY = (float) transformedBitmap.getHeight() / boundRect.height();

        Rect scaledRect = new Rect(
                (int) ((cropRect.left - boundRect.left) * scaleX),
                (int) ((cropRect.top - boundRect.top) * scaleY),
                (int) ((cropRect.right - boundRect.left) * scaleX),
                (int) ((cropRect.bottom - boundRect.top) * scaleY)
        );

        return Bitmap.createBitmap(
                transformedBitmap,
                scaledRect.left,
                scaledRect.top,
                scaledRect.width(),
                scaledRect.height()
        );
    }
}