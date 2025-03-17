package com.example.galleryexample3.datamanagement;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

public class ImageManager {

    public static ArrayList<String> getImages(Context context){
        ArrayList<String> arrPath = new ArrayList<>();

        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        // use MediaStore.Images.Media.<Attribute> to query and stuff
        // contentResolver is the sqlite database

        try (Cursor cursor = contentResolver.query(uri, null, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC")){
            if (cursor == null) {
                // query failed, handle error
            } else if (!cursor.moveToFirst()) {
                // no media on the device
            } else {
                int i = 0;
//                    How to set attribute of
//                    int titleColumn = cursor.getColumnIndex(MediaStore.Images.Media.TITLE);
//                    int idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                do {
//                        How to get attributes
//                        long thisId = cursor.getLong(idColumn);
//                        String thisTitle = cursor.getString(titleColumn);
                    String pathGot = cursor.getString(dataColumnIndex);
                    arrPath.add(pathGot);
                    i++;
                } while (cursor.moveToNext()); // Load limit
            }
        }

        return arrPath;
    }
}
