package com.example.galleryexample3.imageediting;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class ImageClipboard {
    public static void getImageToClipBoard(Context context, String URI) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        try(Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media.DATA + " = ?", new String[] {URI}, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") Uri imageURI = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
                ClipData clipData = ClipData.newUri(context.getContentResolver(), "Image", imageURI);
                clipboardManager.setPrimaryClip(clipData);
            }
        }
    }
}
