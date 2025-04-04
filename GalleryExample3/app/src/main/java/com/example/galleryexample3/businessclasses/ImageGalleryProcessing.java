package com.example.galleryexample3.businessclasses;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.OutputStream;

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

    public static boolean deleteImage(Context context, String URI){

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DATA, URI);

        int res = context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", new String[]{URI});
        return (res > 0);
    }

}
