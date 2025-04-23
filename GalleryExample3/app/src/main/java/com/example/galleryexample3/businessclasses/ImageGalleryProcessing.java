package com.example.galleryexample3.businessclasses;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.userinterface.SearchItemListAdapter;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.text.SimpleDateFormat;

public class ImageGalleryProcessing {
    public static boolean saveImage(Context context, Bitmap bitmap){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "image_" + System.currentTimeMillis() + ".png");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/HeavensDoor");

        Uri resultURI = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if (resultURI != null){
            try {
                OutputStream outputStream = context.getContentResolver().openOutputStream(resultURI, "w");
                if (outputStream != null){
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.close();
                    Toast.makeText(context, "Image saved!", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
            catch (Exception e){
                Toast.makeText(context, "Can't save image.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        Toast.makeText(context, "Can't save image.", Toast.LENGTH_SHORT).show();
        return false;
    }

    public static boolean deleteImage(Context context, String URI) {
        int res = context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + " = ?", new String[]{URI});
        try (DatabaseHandler databaseHandler = DatabaseHandler.getInstance(context)) {
            databaseHandler.tags().deleteImage(URI);
            databaseHandler.albums().deleteImage(URI);
        }
        return (res > 0);
    }

    public static ArrayList<String> getImages(Context context, String sort_type, String sort_order){
        ArrayList<String> arrPath = new ArrayList<>();

        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//        Log.i("getimg", String.valueOf(uri));
        String[] projection ={MediaStore.Images.Media.DATA};
        // use MediaStore.Images.Media.<Attribute> to query and stuff
        // contentResolver is the sqlite database
        HashMap<String, String> sort_type_map = new HashMap<>();
        sort_type_map.put("DATE_ADDED", MediaStore.Images.Media.DATE_ADDED);
        sort_type_map.put("DISPLAY_NAME", MediaStore.Images.Media.DISPLAY_NAME);
        sort_type_map.put("DATE_MODIFIED", MediaStore.Images.Media.DATE_MODIFIED);
        if (!Objects.equals(sort_type, "DATE_ADDED") && !Objects.equals(sort_type, "DISPLAY_NAME") && !Objects.equals(sort_type, "DATE_MODIFIED")) {
            sort_type = "DATE_ADDED";
        }
        if (!Objects.equals(sort_order, " DESC") && !Objects.equals(sort_order, " ASC")) {
            sort_order = " DESC";
        }
        try (Cursor cursor = contentResolver.query(uri, projection, null, null, sort_type_map.get(sort_type) + sort_order)){
            if (cursor == null) {
                // query failed, handle error
            } else if (!cursor.moveToFirst()) {
                // no media on the device
            } else {
                int i = 0;
                int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                do {
                    String pathGot = cursor.getString(dataColumnIndex);
                    arrPath.add(pathGot);
                    i++;
                } while (cursor.moveToNext()); // Load limit
            }
        }

        return arrPath;
    }
    public static ArrayList<String> getImagesByName(Context context, String name, String sort_type, String sort_order){
        ArrayList<String> arrPath = new ArrayList<>();

        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//        Log.i("getimg", String.valueOf(uri));
        String[] projection ={MediaStore.Images.Media.DATA};
        String selection =  MediaStore.Images.Media.DISPLAY_NAME + " like ? ";
        String[] args = {"%"+name+"%"};
        // use MediaStore.Images.Media.<Attribute> to query and stuff
        // contentResolver is the sqlite database
        HashMap<String, String> sort_type_map = new HashMap<>();
        sort_type_map.put("DATE_ADDED", MediaStore.Images.Media.DATE_ADDED);
        sort_type_map.put("DISPLAY_NAME", MediaStore.Images.Media.DISPLAY_NAME);
        sort_type_map.put("DATE_MODIFIED", MediaStore.Images.Media.DATE_MODIFIED);
        sort_type_map.put("SIZE", MediaStore.Images.Media.SIZE);
        if (!Objects.equals(sort_type, "DATE_ADDED") && !Objects.equals(sort_type, "DISPLAY_NAME") && !Objects.equals(sort_type, "DATE_MODIFIED") && !Objects.equals(sort_type, "SIZE")) {
            sort_type = "DATE_ADDED";
        }
        if (!Objects.equals(sort_order, " DESC") && !Objects.equals(sort_order, " ASC")) {
            sort_order = " DESC";
        }
        try (Cursor cursor = contentResolver.query(uri, projection, selection, args, sort_type_map.get(sort_type) + " " + sort_order)){
            if (cursor == null) {
                // query failed, handle error
            } else if (!cursor.moveToFirst()) {
                // no media on the device
            } else {
                int i = 0;
                int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                do {
                    String pathGot = cursor.getString(dataColumnIndex);
                    arrPath.add(pathGot);
                    i++;
                } while (cursor.moveToNext()); // Load limit
            }
        }

        return arrPath;
    }

    public static String getImageDateAdded(Context context, String imageUri) {
        String[] projection = {
                MediaStore.Images.Media.DATE_ADDED
        };

        String selection = MediaStore.Images.Media.DATA + " = ?";
        String[] selectionArgs = new String[]{imageUri};

        Uri externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();

        try (Cursor cursor = contentResolver.query(externalContentUri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int dateAddedColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
                long dateAddedInMillis = cursor.getLong(dateAddedColumnIndex);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date dateAdded = new Date(dateAddedInMillis * 1000L);
                return sdf.format(dateAdded);
            }
        } catch (Exception e) {
            Log.e("Error", "Can not get image's date added", e);
        }
        return "null";
    }

    public static String getSize(Context context, String imageUri) {
        String[] projection = {
                MediaStore.Images.Media.SIZE
        };

        String selection = MediaStore.Images.Media.DATA + " = ?";
        String[] selectionArgs = new String[]{imageUri};

        Uri externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();

        try (Cursor cursor = contentResolver.query(externalContentUri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int dateAddedColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                long fileSize = cursor.getLong(dateAddedColumnIndex);

                return fileSize + " B";
            }
        } catch (Exception e) {
            Log.e("Error", "Can not get image's size", e);
        }
        return "null";
    }

    public static String getResolution(Context context, String imageUri) {
        String[] projection = {
                MediaStore.Images.Media.RESOLUTION
        };

        String selection = MediaStore.Images.Media.DATA + " = ?";
        String[] selectionArgs = new String[]{imageUri};

        Uri externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();

        try (Cursor cursor = contentResolver.query(externalContentUri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int dateAddedColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.RESOLUTION);
                return cursor.getString(dateAddedColumnIndex);
            }
        } catch (Exception e) {
            Log.e("Error", "Can not get image's resolution", e);
        }
        return "null";
    }

    public static String getName(Context context, String imageUri) {
        String[] projection = {
                MediaStore.Images.Media.DISPLAY_NAME
        };

        String selection = MediaStore.Images.Media.DATA + " = ?";
        String[] selectionArgs = new String[]{imageUri};

        Uri externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();

        try (Cursor cursor = contentResolver.query(externalContentUri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int dateAddedColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                return cursor.getString(dateAddedColumnIndex);
            }
        } catch (Exception e) {
            Log.e("Error", "Can not get image's resolution", e);
        }
        return "null";
    }

    public static SearchItemListAdapter.MatchItem getMatchImageItem(Context context, String name){
        int count = 0;
        int type = SearchItemListAdapter.MATCH_IMAGE_NAME;
        String thumbnail = "";
        String[] displayNames = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME
        };
        Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String sortOrder = " DESC ";
        String selection  = MediaStore.Images.Media.DISPLAY_NAME + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + name + "%"};
        ContentResolver resolver = context.getContentResolver();
        try(Cursor cursor = resolver.query(collection, displayNames, selection, selectionArgs, MediaStore.Images.Media.DATE_ADDED + sortOrder)){
            if (cursor != null && cursor.moveToFirst()){
                Log.v("Cursor Alive", "cursor alive");
                int idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Uri retrievedURI = ContentUris.withAppendedId(collection, cursor.getLong(idIndex));
                thumbnail = retrievedURI.toString();
                count = cursor.getCount();
            }
        }catch (Exception e){
            Log.e("MatchName count", e.toString());
        }
        Log.v("Search count", String.valueOf(count));
        if (count == 0) return null;
        return new SearchItemListAdapter.MatchItem(name, String.valueOf(count), thumbnail, type);
    }

    @SuppressLint("Range")
    public static boolean changeNameImage(Context context, String URI, String newName){
        if (newName == null || newName.isEmpty() || newName.isBlank()){
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, newName);
        //values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        try(Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media.DATA + " = ?", new String[] {URI}, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                Uri uri2 = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
                int row = context.getContentResolver().update(uri2, values, null, null);
                return row > 0;
            }
        }
        return false;
    }
}
