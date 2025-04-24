package com.example.galleryexample3.businessclasses;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.galleryexample3.PrivateVaultLockScreen;
import com.example.galleryexample3.dataclasses.DatabaseHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class PrivateAlbum {
    private static final String folderName = ".klbd";

    public static void addPrivateAlbum(Context context, String URI) {
        String[] partPath = URI.split("/");
        String fileName = partPath[partPath.length - 1];
        String fileExtension = fileName.substring(fileName.lastIndexOf('.'));
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        partPath = Arrays.copyOfRange(partPath, 4, partPath.length - 1);
        String filePath = TextUtils.join("/", partPath);
        File pvFile = new File(context.getFilesDir(), folderName + "/" + filePath);
        if (!pvFile.exists() || !pvFile.isDirectory()) {
            boolean r = pvFile.mkdirs();
            if (!r) {
                return;
            }
        }
        File file = new File(pvFile, fileName + fileExtension);
        int cnt = 1;
        while (file.exists()) {
            file = new File(pvFile, fileName + " (" + cnt + ")" + fileExtension);
            cnt++;
        }
        try {
            Path path1 = Paths.get(URI);
            Path path2 = file.toPath();
            BasicFileAttributes attributes = Files.readAttributes(path1, BasicFileAttributes.class);
            Files.move(path1, path2);
            Files.setAttribute(path2, "basic:creationTime", attributes.creationTime());
            Files.setAttribute(path2, "basic:lastModifiedTime", attributes.lastModifiedTime());
            Files.setAttribute(path2, "basic:lastAccessTime", attributes.lastAccessTime());
            DatabaseHandler databaseHandler = DatabaseHandler.getInstance(context);
            databaseHandler.tags().deleteImage(URI);
            databaseHandler.albums().deleteImage(URI);
        } catch (IOException e) {
            return;
        }
    }

    public static boolean saveImage(Context context, Bitmap bitmap) {
        File directory = new File(context.getFilesDir(), folderName + "/Pictures/HeavensDoor");
        if (!directory.exists() || !directory.isDirectory()) {
            boolean r = directory.mkdirs();
            if (!r) {
                Toast.makeText(context, "Can't save image.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        File file = new File(directory, "image_" + System.currentTimeMillis() + ".png");
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            Toast.makeText(context, "Image saved!", Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception e){
            Toast.makeText(context, "Can't save image.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static boolean deleteImage(Context context, String URI) {
        File file = new File(URI);
        return file.delete();
    }

    public static ArrayList<String> getImages(Context context, String sort_type, String sort_order) {
        ArrayList<String> arrPath = queryImages(context, "");
        if (!Objects.equals(sort_type, "DATE_ADDED") && !Objects.equals(sort_type, "DISPLAY_NAME") && !Objects.equals(sort_type, "DATE_MODIFIED")) {
            sort_type = "DATE_ADDED";
        }
        if (!Objects.equals(sort_order, " DESC") && !Objects.equals(sort_order, " ASC")) {
            sort_order = " DESC";
        }
        String finalSort_type = sort_type;
        arrPath.sort(new Comparator<String>() {
            @Override
            public int compare(String p1, String p2) {
                try {
                    File f1 = new File(p1);
                    File f2 = new File(p2);
                    switch (finalSort_type) {
                        case "DATE_ADDED": {
                            BasicFileAttributes attr1 = Files.readAttributes(f1.toPath(), BasicFileAttributes.class);
                            BasicFileAttributes attr2 = Files.readAttributes(f2.toPath(), BasicFileAttributes.class);
                            return attr1.creationTime().compareTo(attr2.creationTime());
                        }
                        case "DISPLAY_NAME": {
                            return f1.getName().compareToIgnoreCase(f2.getName());
                        }
                        case "DATE_MODIFIED": {
                            return Long.compare(f1.lastModified(), f2.lastModified());
                        }
                    }
                } catch (IOException e) {
                    return 0;
                }
                return 0;
            }
        });
        if (sort_order.equals(" DESC")) {
            Collections.reverse(arrPath);
        }
        return arrPath;
    }

    public static ArrayList<String> queryImages(Context context, String subfolder) {
        ArrayList<String> arrPath = new ArrayList<>();
        if (subfolder.isBlank()) {
            subfolder = folderName;
        }
        File[] files = new File(context.getFilesDir(), subfolder).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String newsubfolder = subfolder + "/" + file.getName();

                    arrPath.addAll(queryImages(context, newsubfolder));
                } else {
                    arrPath.add(file.getAbsolutePath());
                }
            }
        }
        Log.v(PrivateAlbum.class.toString(), subfolder);

        Log.v(PrivateAlbum.class.toString(), String.valueOf(arrPath.size()));
        return arrPath;
    }

    public static String getImageDateAdded(Context context, String imageUri) {
        try {
            long dateAddedInMillis = Files.readAttributes(Paths.get(imageUri), BasicFileAttributes.class).creationTime().toMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date dateAdded = new Date(dateAddedInMillis * 1000L);
            return sdf.format(dateAdded);
        } catch (IOException e) {
            Log.e("Error", "Can not get image's date added", e);
        }
        return "null";
    }

    public static String getSize(Context context, String imageUri) {
        File file = new File(imageUri);
        return file.length() + " B";
    }

    public static String getResolution(Context context, String imageUri) {
        Bitmap bitmap = BitmapFactory.decodeFile(imageUri);
        return bitmap.getWidth() + "x" + bitmap.getHeight();
    }

    public static String getName(Context context, String imageUri) {
        File file = new File(imageUri);
        return file.getName();
    }

    public static void removeImage(Context context, String URI) {
        String[] partPath = URI.split("/");
        String fileName = partPath[partPath.length - 1];
        String fileExtension = fileName.substring(fileName.lastIndexOf('.'));
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        partPath = Arrays.copyOfRange(partPath, 7, partPath.length - 1);
        String filePath = TextUtils.join("/", partPath);
        File pvFile = new File("/storage/emulated/0/" + filePath);
        if (!pvFile.exists() || !pvFile.isDirectory()) {
            boolean r = pvFile.mkdirs();
            if (!r) {
                return;
            }
        }
        File file = new File(pvFile, fileName + fileExtension);
        int cnt = 1;
        while (file.exists()) {
            file = new File(pvFile, fileName + " (" + cnt + ")" + fileExtension);
            cnt++;
        }
        try {
            Path path1 = Paths.get(URI);
            Path path2 = file.toPath();
            BasicFileAttributes attributes = Files.readAttributes(path1, BasicFileAttributes.class);
            Files.move(path1, path2);
            Files.setAttribute(path2, "basic:creationTime", attributes.creationTime());
            Files.setAttribute(path2, "basic:lastModifiedTime", attributes.lastModifiedTime());
            Files.setAttribute(path2, "basic:lastAccessTime", attributes.lastAccessTime());
        } catch (IOException e) {
            return;
        }
    }

    public static void testQuery(Context context, String subfolder) {
        if (subfolder.isBlank()) {
            subfolder = folderName;
        }
        File[] files = new File(context.getFilesDir(), subfolder).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    Log.i("debug", file.getAbsolutePath() + "- Folder");
                    subfolder = subfolder + "/" + file.getName();
                    queryImages(context, subfolder);
                } else {
                    Log.i("debug", file.getAbsolutePath() + "- File");
                }
            }
        }
    }
}
