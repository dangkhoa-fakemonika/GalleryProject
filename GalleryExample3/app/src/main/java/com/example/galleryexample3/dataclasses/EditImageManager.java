package com.example.galleryexample3.dataclasses;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EditImageManager {
    public static Uri cacheBitmapToUri(Context context, Bitmap bitmap) throws IOException {
        File cachePath = new File(context.getCacheDir(), "images");
        if (!cachePath.exists()) cachePath.mkdirs();
        File imageFile = new File(cachePath, "temp_edit_image.png");
        FileOutputStream outputFile = new FileOutputStream(imageFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputFile);
        outputFile.close();
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", imageFile);
    }
}
