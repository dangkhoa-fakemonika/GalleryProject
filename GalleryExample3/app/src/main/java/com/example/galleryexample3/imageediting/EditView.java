package com.example.galleryexample3.imageediting;

import static com.example.galleryexample3.businessclasses.ImageFiltersProcessing.applyGrayscale;
import static com.example.galleryexample3.businessclasses.ImageFiltersProcessing.applySepia;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.galleryexample3.MainActivity;
import com.example.galleryexample3.R;
import com.example.galleryexample3.userinterface.FilterPreviewAdapter;

import java.util.ArrayList;

public class EditView extends AppCompatActivity {
    private String mode = "Adjustment";
    private String imageURI;
    Bitmap imageBitmap;
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
        filterList.add(new FilterPreviewAdapter.FilterPreview("Normal", imageBitmap));
        filterList.add(new FilterPreviewAdapter.FilterPreview("Gray Scale", applyGrayscale(imageBitmap)));
        filterList.add(new FilterPreviewAdapter.FilterPreview("Sepia", applySepia(imageBitmap)));

        TextView modeTextView = (TextView) findViewById(R.id.modeTextView);
        Button adjustmentButton = (Button) findViewById(R.id.adjustmentButton);
        Button filterButton = (Button) findViewById(R.id.filterButton);
        SeekBar adjustmentSeekBar = (SeekBar) findViewById(R.id.adjustmentSeekBar);
        ImageView editedImage = (ImageView) findViewById(R.id.editedImage);
        RecyclerView filterPreviewImage = (RecyclerView) findViewById(R.id.filterPreviewImage);

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

        Glide.with(this).load(imageURI).into(editedImage);
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

//    private Bitmap applyGrayscale(){
//        int width = imageBitmap.getWidth();
//        int height = imageBitmap.getHeight();
//        Bitmap.Config cf = imageBitmap.getConfig();
//
//        int[] pixels = new int[width * height];
//        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
//
//        for (int i = 0; i < width * height; i++){
//            int pixel = pixels[i];
//            int average =  (int) (Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114) ;
//
//            pixels[i] = Color.rgb(average, average, average);
//        }
//
//        return Bitmap.createBitmap(pixels, width, height, cf);
//    }
//
//    private Bitmap applySepia(){
//        int width = imageBitmap.getWidth();
//        int height = imageBitmap.getHeight();
//        Bitmap.Config cf = imageBitmap.getConfig();
//
//        int[] pixels = new int[width * height];
//        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
//
//        for (int i = 0; i < width * height; i++){
//            int pixel = pixels[i];
//            int red = Color.red(pixel);
//            int green = Color.green(pixel);
//            int blue = Color.blue(pixel);
//
//            double tr = 0.393 * red + 0.769 * green + 0.189 * blue;
//            double tg = 0.349 * red + 0.686 * green + 0.168 * blue;
//            double tb = 0.272 * red + 0.534 * green + 0.131 * blue;
//
//            tr = tr >= 255 ? 255 : tr;
//            tg = tg >= 255 ? 255 : tg;
//            tb = tb >= 255 ? 255 : tb;
//
//            pixels[i] = Color.rgb((int) tr, (int) tg, (int) tb);
//        }
//
//
//        return Bitmap.createBitmap(pixels, width, height, cf);
//    }
}