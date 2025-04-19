package com.example.galleryexample3.businessclasses;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.IOException;
import java.util.Objects;

public class ImageWallpaperManager {
    public static void setWallpaper(Context context, String URI, int type) {
        // gọi UI của điện thoại để set
//        try(Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media.DATA + " = ?", new String[] {URI}, null)) {
//            if (cursor != null && cursor.moveToFirst()) {
//                @SuppressLint("Range") Uri imageURI = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
//                Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
//                intent.setDataAndType(imageURI, "image/*");
//                intent.putExtra("mimeType", "image/*");
//                context.startActivity(Intent.createChooser(intent, "Đặt làm hình nền"));
//            }
//        }
        // tự set bằng code (không UI)
        Bitmap imageBitmap = BitmapFactory.decodeFile(URI);
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context.getApplicationContext());
        try {
            //set màn hình chính
            if (type == 0){
                wallpaperManager.setBitmap(imageBitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                Toast.makeText(context, "Main background changed", Toast.LENGTH_SHORT).show();
            }
            //set màn hình khoá
            else if (type == 1){
                wallpaperManager.setBitmap(imageBitmap, null, true, WallpaperManager.FLAG_LOCK);
                Toast.makeText(context, "Lock screen background changed", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
