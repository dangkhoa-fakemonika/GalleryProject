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

public class ImageWallpaperManager {
    public static void setWallpaper(Context context, String URI) {
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
            wallpaperManager.setBitmap(imageBitmap, null, true, WallpaperManager.FLAG_SYSTEM);
            //set màn hình khoá
            wallpaperManager.setBitmap(imageBitmap, null, true, WallpaperManager.FLAG_LOCK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Toast.makeText(context, "Đã thay hình nền.", Toast.LENGTH_SHORT).show();
    }
}
