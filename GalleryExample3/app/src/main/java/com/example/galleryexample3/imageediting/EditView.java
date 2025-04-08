package com.example.galleryexample3.imageediting;

import static com.example.galleryexample3.businessclasses.ImageFiltersProcessing.adjustBrightness;
import static com.example.galleryexample3.businessclasses.ImageFiltersProcessing.applyGrayscale;
import static com.example.galleryexample3.businessclasses.ImageFiltersProcessing.applySepia;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.icu.util.Freezable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.galleryexample3.MainActivity;
import com.example.galleryexample3.R;
import com.example.galleryexample3.userinterface.FilterPreviewAdapter;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Objects;

public class EditView extends AppCompatActivity {
    private String mode = "Adjustment";
    private String imageURI;
    Bitmap imageBitmap;
    Bitmap imageSrc;
    Bitmap displayBitmap;
    ArrayList<FilterPreviewAdapter.FilterPreview> filterList = new ArrayList<>();
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

            AlertDialog.Builder builder = new AlertDialog.Builder(EditView.this);
            builder.setMessage("Ảnh không tồn tại hoặc đã bị sửa đổi.")
                    .setCancelable(false)
                    .setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(EditView.this, MainActivity.class);
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
        setContentView(R.layout.edit_view);
        EdgeToEdge.enable(this);

        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();

        imageURI = gotBundle.getString("imageURI");
        imageBitmap = BitmapFactory.decodeFile(imageURI);
        imageSrc = imageBitmap.copy(Objects.requireNonNull(imageBitmap.getConfig()), true);
        filterList.add(new FilterPreviewAdapter.FilterPreview("Normal", imageBitmap));
        filterList.add(new FilterPreviewAdapter.FilterPreview("Gray Scale", applyGrayscale(imageBitmap)));
        filterList.add(new FilterPreviewAdapter.FilterPreview("Sepia", applySepia(imageBitmap)));

        TextView modeTextView = (TextView) findViewById(R.id.modeTextView);
        Button adjustmentButton = (Button) findViewById(R.id.adjustmentButton);
        Button filterButton = (Button) findViewById(R.id.filterButton);
        SeekBar adjustmentSeekBar = (SeekBar) findViewById(R.id.adjustmentSeekBar);
        ImageView editedImage = (ImageView) findViewById(R.id.editedImage);
        RecyclerView filterPreviewImage = (RecyclerView) findViewById(R.id.filterPreviewImage);
        View topLeftPoint = (View) findViewById(R.id.topLeftPoint);
        View topRightPoint = (View) findViewById(R.id.topRightPoint);
        View botLeftPoint = (View) findViewById(R.id.botLeftPoint);
        View botRightPoint = (View) findViewById(R.id.botRightPoint);
        RectangleView rectangleCrop = (RectangleView) findViewById(R.id.rectangleCrop);
        Button undoButton = (Button) findViewById(R.id.undoButton);
        Button cropButton = (Button) findViewById(R.id.cropButton);
        topLeftPoint.setOnTouchListener(new View.OnTouchListener() {
            float dXo, dYo;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dXo = event.getRawX();
                        dYo = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dXn = event.getRawX();
                        float dYn = event.getRawY();
                        float dXc = dXn - dXo;
                        float dYc = dYn - dYo;
                        int[] iXY = new int[2];
                        editedImage.getLocationOnScreen(iXY);
                        float iX = iXY[0], iY = iXY[1];
                        float iW = iX + editedImage.getWidth();
                        float iH = iY + editedImage.getHeight();
                        int[] rXY = new int[2];
                        rectangleCrop.getLocationOnScreen(rXY);
                        float rX = rXY[0], rY = rXY[1];
                        float rW = rX + rectangleCrop.getWidth();
                        float rH = rY + rectangleCrop.getHeight();
                        if (rX + dXc < iX || rY + dYc < iY) {
                            dXo = dXn;
                            dYo = dYn;
                            return true;
                        }
                        if (botLeftPoint.getY() - (v.getY() + dYc) < 100 || topRightPoint.getX() - (v.getX() + dXc) < 100) {
                            dXo = dXn;
                            dYo = dYn;
                            return true;
                        }
                        v.setX(v.getX() + dXc);
                        botLeftPoint.setX(botLeftPoint.getX() + dXc);
                        v.setY(v.getY() + dYc);
                        topRightPoint.setY(topRightPoint.getY() + dYc);
                        rectangleCrop.setRect(topLeftPoint.getX() + topLeftPoint.getWidth() / 2f, topLeftPoint.getY() + topLeftPoint.getHeight() / 2f, botRightPoint.getX() + botRightPoint.getWidth() / 2f, botRightPoint.getY() + botRightPoint.getHeight() / 2f);
                        dXo = dXn;
                        dYo = dYn;
                        changeImageBrightness(editedImage, imageSrc, rectangleCrop);
                        return true;
                }
                return false;
            }
        });

        topRightPoint.setOnTouchListener(new View.OnTouchListener() {
            float dXo, dYo;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dXo = event.getRawX();
                        dYo = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dXn = event.getRawX();
                        float dYn = event.getRawY();
                        float dXc = dXn - dXo;
                        float dYc = dYn - dYo;
                        int[] iXY = new int[2];
                        editedImage.getLocationOnScreen(iXY);
                        float iX = iXY[0], iY = iXY[1];
                        float iW = iX + editedImage.getWidth();
                        float iH = iY + editedImage.getHeight();
                        int[] rXY = new int[2];
                        rectangleCrop.getLocationOnScreen(rXY);
                        float rX = rXY[0], rY = rXY[1];
                        float rW = rX + rectangleCrop.getWidth();
                        float rH = rY + rectangleCrop.getHeight();
                        if (rW + dXc > iW || rY + dYc < iY) {
                            dXo = dXn;
                            dYo = dYn;
                            return true;
                        }
                        if (botRightPoint.getY() - (v.getY() + dYc) < 100 || (v.getX() + dXc) - topLeftPoint.getX() < 100) {
                            dXo = dXn;
                            dYo = dYn;
                            return true;
                        }
                        v.setX(v.getX() + dXc);
                        botRightPoint.setX(botRightPoint.getX() + dXc);
                        v.setY(v.getY() + dYc);
                        topLeftPoint.setY(topLeftPoint.getY() + dYc);
                        rectangleCrop.setRect(topLeftPoint.getX() + topLeftPoint.getWidth() / 2f, topLeftPoint.getY() + topLeftPoint.getHeight() / 2f, botRightPoint.getX() + botRightPoint.getWidth() / 2f, botRightPoint.getY() + botRightPoint.getHeight() / 2f);
                        dXo = dXn;
                        dYo = dYn;
                        changeImageBrightness(editedImage, imageSrc, rectangleCrop);
                        return true;
                }
                return false;
            }
        });

        botLeftPoint.setOnTouchListener(new View.OnTouchListener() {
            float dXo, dYo;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dXo = event.getRawX();
                        dYo = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dXn = event.getRawX();
                        float dYn = event.getRawY();
                        float dXc = dXn - dXo;
                        float dYc = dYn - dYo;
                        int[] iXY = new int[2];
                        editedImage.getLocationOnScreen(iXY);
                        float iX = iXY[0], iY = iXY[1];
                        float iW = iX + editedImage.getWidth();
                        float iH = iY + editedImage.getHeight();
                        int[] rXY = new int[2];
                        rectangleCrop.getLocationOnScreen(rXY);
                        float rX = rXY[0], rY = rXY[1];
                        float rW = rX + rectangleCrop.getWidth();
                        float rH = rY + rectangleCrop.getHeight();
                        if (rX + dXc < iX || rH + dYc > iH) {
                            dXo = dXn;
                            dYo = dYn;
                            return true;
                        }
                        if ((v.getY() + dYc) - topLeftPoint.getY() < 100 || botRightPoint.getX() - (v.getX() + dXc) < 100) {
                            dXo = dXn;
                            dYo = dYn;
                            return true;
                        }
                        v.setX(v.getX() + dXc);
                        topLeftPoint.setX(topLeftPoint.getX() + dXc);
                        v.setY(v.getY() + dYc);
                        botRightPoint.setY(botRightPoint.getY() + dYc);
                        rectangleCrop.setRect(topLeftPoint.getX() + topLeftPoint.getWidth() / 2f, topLeftPoint.getY() + topLeftPoint.getHeight() / 2f, botRightPoint.getX() + botRightPoint.getWidth() / 2f, botRightPoint.getY() + botRightPoint.getHeight() / 2f);
                        dXo = dXn;
                        dYo = dYn;
                        changeImageBrightness(editedImage, imageSrc, rectangleCrop);
                        return true;
                }
                return false;
            }
        });

        botRightPoint.setOnTouchListener(new View.OnTouchListener() {
            float dXo, dYo;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dXo = event.getRawX();
                        dYo = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dXn = event.getRawX();
                        float dYn = event.getRawY();
                        float dXc = dXn - dXo;
                        float dYc = dYn - dYo;
                        int[] iXY = new int[2];
                        editedImage.getLocationOnScreen(iXY);
                        float iX = iXY[0], iY = iXY[1];
                        float iW = iX + editedImage.getWidth();
                        float iH = iY + editedImage.getHeight();
                        int[] rXY = new int[2];
                        rectangleCrop.getLocationOnScreen(rXY);
                        float rX = rXY[0], rY = rXY[1];
                        float rW = rX + rectangleCrop.getWidth();
                        float rH = rY + rectangleCrop.getHeight();
                        if (rW + dXc > iW || rH + dYc > iH) {
                            dXo = dXn;
                            dYo = dYn;
                            return true;
                        }
                        if ((v.getY() + dYc) - topRightPoint.getY() < 100 || (v.getX() + dXc) - botLeftPoint.getX() < 100) {
                            dXo = dXn;
                            dYo = dYn;
                            return true;
                        }
                        v.setX(v.getX() + dXc);
                        topRightPoint.setX(topRightPoint.getX() + dXc);
                        v.setY(v.getY() + dYc);
                        botLeftPoint.setY(botLeftPoint.getY() + dYc);
                        rectangleCrop.setRect(topLeftPoint.getX() + topLeftPoint.getWidth() / 2f, topLeftPoint.getY() + topLeftPoint.getHeight() / 2f, botRightPoint.getX() + botRightPoint.getWidth() / 2f, botRightPoint.getY() + botRightPoint.getHeight() / 2f);
                        dXo = dXn;
                        dYo = dYn;
                        changeImageBrightness(editedImage, imageSrc, rectangleCrop);
                        return true;
                }
                return false;
            }
        });

        rectangleCrop.setOnTouchListener(new View.OnTouchListener() {
            float dXo, dYo;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dXo = event.getRawX();
                        dYo = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dXn = event.getRawX();
                        float dYn = event.getRawY();
                        float dXc = dXn - dXo;
                        float dYc = dYn - dYo;
                        int[] iXY = new int[2];
                        editedImage.getLocationOnScreen(iXY);
                        float iX = iXY[0], iY = iXY[1];
                        float iW = iX + editedImage.getWidth();
                        float iH = iY + editedImage.getHeight();
                        int[] rXY = new int[2];
                        v.getLocationOnScreen(rXY);
                        float rX = rXY[0], rY = rXY[1];
                        float rW = rX + v.getWidth();
                        float rH = rY + v.getHeight();
                        if (rX + dXc < iX || rW + dXc > iW || rY + dYc < iY || rH + dYc > iH) {
                            dXo = dXn;
                            dYo = dYn;
                            return true;
                        }
                        v.setX(v.getX() + dXc);
                        topLeftPoint.setX(topLeftPoint.getX() + dXc);
                        topRightPoint.setX(topRightPoint.getX() + dXc);
                        botLeftPoint.setX(botLeftPoint.getX() + dXc);
                        botRightPoint.setX(botRightPoint.getX() + dXc);
                        v.setY(v.getY() + dYc);
                        topLeftPoint.setY(topLeftPoint.getY() + dYc);
                        topRightPoint.setY(topRightPoint.getY() + dYc);
                        botLeftPoint.setY(botLeftPoint.getY() + dYc);
                        botRightPoint.setY(botRightPoint.getY() + dYc);
                        dXo = dXn;
                        dYo = dYn;
                        changeImageBrightness(editedImage, imageSrc, rectangleCrop);
                        return true;
                }
                return false;
            }
        });

        adjustmentButton.setOnClickListener(l -> {
            if (!mode.equals("Adjustment")) {
                adjustmentButton.setTextColor(Color.WHITE);
                filterButton.setTextColor(getResources().getColor(R.color.button_noselected, getTheme()));

                mode = "Adjustment";
                modeTextView.setText(mode);

                adjustmentSeekBar.setVisibility(View.VISIBLE);
                filterPreviewImage.setVisibility(View.GONE);
            }
        });

        filterButton.setOnClickListener(l -> {
            if (!mode.equals("Filter")) {
                filterButton.setTextColor(Color.WHITE);
                adjustmentButton.setTextColor(getResources().getColor(R.color.button_noselected, getTheme()));

                mode = "Filter";
                modeTextView.setText(mode);

                filterPreviewImage.setVisibility(View.VISIBLE);
                adjustmentSeekBar.setVisibility(View.GONE);
            }
        });

        FilterPreviewAdapter filterPreviewAdapter = new FilterPreviewAdapter(this, filterList);
        filterPreviewImage.setAdapter(filterPreviewAdapter);
        Glide.with(this).load(imageBitmap).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                editedImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        editedImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        float left = editedImage.getX();
                        float top = editedImage.getY();
                        float right = left + editedImage.getWidth();
                        float bottom = top + editedImage.getHeight();

                        topLeftPoint.setX(left - topLeftPoint.getWidth() / 2f);
                        topLeftPoint.setY(top - topLeftPoint.getHeight() / 2f);

                        topRightPoint.setX(right - topRightPoint.getWidth() / 2f);
                        topRightPoint.setY(top - topRightPoint.getHeight() / 2f);

                        botLeftPoint.setX(left - botLeftPoint.getWidth() / 2f);
                        botLeftPoint.setY(bottom - botLeftPoint.getHeight() / 2f);

                        botRightPoint.setX(right - botRightPoint.getWidth() / 2f);
                        botRightPoint.setY(bottom - botRightPoint.getHeight() / 2f);

                        rectangleCrop.setRect(topLeftPoint.getX() + topLeftPoint.getWidth() / 2f, topLeftPoint.getY() + topLeftPoint.getHeight() / 2f, botRightPoint.getX() + botRightPoint.getWidth() / 2f, botRightPoint.getY() + botRightPoint.getHeight() / 2f);
                    }
                });
                return false;
            }
        }).into(editedImage);

        undoButton.setOnClickListener(listener -> {
            imageSrc = imageBitmap.copy(imageBitmap.getConfig(), true);
            Glide.with(this).load(imageSrc).override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                    editedImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            editedImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            float left = editedImage.getX();
                            float top = editedImage.getY();
                            float right = left + editedImage.getWidth();
                            float bottom = top + editedImage.getHeight();

                            topLeftPoint.setX(left - topLeftPoint.getWidth() / 2f);
                            topLeftPoint.setY(top - topLeftPoint.getHeight() / 2f);

                            topRightPoint.setX(right - topRightPoint.getWidth() / 2f);
                            topRightPoint.setY(top - topRightPoint.getHeight() / 2f);

                            botLeftPoint.setX(left - botLeftPoint.getWidth() / 2f);
                            botLeftPoint.setY(bottom - botLeftPoint.getHeight() / 2f);

                            botRightPoint.setX(right - botRightPoint.getWidth() / 2f);
                            botRightPoint.setY(bottom - botRightPoint.getHeight() / 2f);

                            rectangleCrop.setRect(topLeftPoint.getX() + topLeftPoint.getWidth() / 2f, topLeftPoint.getY() + topLeftPoint.getHeight() / 2f, botRightPoint.getX() + botRightPoint.getWidth() / 2f, botRightPoint.getY() + botRightPoint.getHeight() / 2f);
                            Log.i("debug", ((BitmapDrawable) editedImage.getDrawable()).getBitmap().getWidth() + " " + ((BitmapDrawable) editedImage.getDrawable()).getBitmap().getHeight());
                        }
                    });
                    return false;
                }
            }).into(editedImage);
        });

        cropButton.setOnClickListener(listener -> {
            Bitmap temp = Bitmap.createBitmap(imageSrc, (int)((rectangleCrop.getX() - editedImage.getX()) / editedImage.getWidth() * imageSrc.getWidth()), (int)((rectangleCrop.getY() - editedImage.getY()) / editedImage.getHeight() * imageSrc.getHeight()), (int)((float) rectangleCrop.getWidth() / editedImage.getWidth() * imageSrc.getWidth()), (int)((float) rectangleCrop.getHeight() / editedImage.getHeight() * imageSrc.getHeight()));
            imageSrc = temp.copy(Objects.requireNonNull(temp.getConfig()), true);
            Glide.with(this).load(imageSrc).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                    editedImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            editedImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            float left = editedImage.getX();
                            float top = editedImage.getY();
                            float right = left + editedImage.getWidth();
                            float bottom = top + editedImage.getHeight();

                            topLeftPoint.setX(left - topLeftPoint.getWidth() / 2f);
                            topLeftPoint.setY(top - topLeftPoint.getHeight() / 2f);

                            topRightPoint.setX(right - topRightPoint.getWidth() / 2f);
                            topRightPoint.setY(top - topRightPoint.getHeight() / 2f);

                            botLeftPoint.setX(left - botLeftPoint.getWidth() / 2f);
                            botLeftPoint.setY(bottom - botLeftPoint.getHeight() / 2f);

                            botRightPoint.setX(right - botRightPoint.getWidth() / 2f);
                            botRightPoint.setY(bottom - botRightPoint.getHeight() / 2f);

                            rectangleCrop.setRect(topLeftPoint.getX() + topLeftPoint.getWidth() / 2f, topLeftPoint.getY() + topLeftPoint.getHeight() / 2f, botRightPoint.getX() + botRightPoint.getWidth() / 2f, botRightPoint.getY() + botRightPoint.getHeight() / 2f);
                        }
                    });
                    return false;
                }
            }).into(editedImage);
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

                AlertDialog.Builder builder = new AlertDialog.Builder(EditView.this);
                builder.setMessage("Ảnh không tồn tại hoặc đã bị sửa đổi.")
                        .setCancelable(false)
                        .setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(EditView.this, MainActivity.class);
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

    private void changeImageBrightness(ImageView image, Bitmap bitmap, RectangleView rectangle) {
        float left = rectangle.getX() - image.getX();
        float top = rectangle.getY() - image.getY();
        float right = left + rectangle.getWidth();
        float bottom = top + rectangle.getHeight();
        Bitmap resultBitmap = bitmap.copy(Objects.requireNonNull(bitmap.getConfig()), true);
        Bitmap cropBitmap = Bitmap.createBitmap(resultBitmap, (int) (left * bitmap.getWidth() / image.getWidth()), (int) (top * bitmap.getHeight() / image.getHeight()), (int) ((right - left) * bitmap.getWidth()) / image.getWidth(), (int) ((bottom - top) * bitmap.getHeight() / image.getHeight()));
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(adjustBrightness(resultBitmap, 0.5), 0, 0, null);
        //canvas.drawBitmap(resultBitmap, 0, 0, null);
        canvas.drawBitmap(cropBitmap, (int) (left * bitmap.getWidth() / image.getWidth()), (int) (top * bitmap.getHeight() / image.getHeight()), null);
        image.setImageBitmap(Bitmap.createScaledBitmap(resultBitmap, image.getWidth(), image.getHeight(), true));
//        Glide.with(this).load(resultBitmap).into(image);
    }

}