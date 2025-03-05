package com.example.galleryexample3.datamanagement;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Objects;

public class AlbumsController {
    SharedPreferences collectiveData;
    SharedPreferences albumData;

    /**
     *  Initialize the Albums handler
     * @param activity The current activity (just pass in "this" or the parent activity).
     */

    public AlbumsController(Activity activity){
        collectiveData = activity.getSharedPreferences("collective_data", Activity.MODE_PRIVATE);
        albumData = activity.getSharedPreferences("album_data", Activity.MODE_PRIVATE);

        if (collectiveData != null) {
            if (!collectiveData.contains("albums_list")) {
                SharedPreferences.Editor editor = collectiveData.edit();
                // test HashSet -> value
                HashSet<String> test = new HashSet<>();
                editor.putStringSet("albums_list", test);
                editor.apply();
            }
        }
    }

    /**
    * Get all albums currently in the system.
     */
    public HashSet<String> getAllAlbums(){
        return new HashSet<>(Objects.requireNonNull(collectiveData.getStringSet("albums_list", null)));
    }

    /**
     *
     * @param albumName The name of the album
     * @return a Set of String of the images
     */
    public HashSet<String> getImagesInAlbums(String albumName){
        return new HashSet<>(Objects.requireNonNull(albumData.getStringSet(albumName, null)));
    }

    public void createAlbum(String albumName){
        if (!albumData.contains(albumName)){
            albumData.edit().putStringSet(albumName, new HashSet<>()).apply();

            HashSet<String> hashSet = new HashSet<>(Objects.requireNonNull(collectiveData.getStringSet("albums_list", null)));
            hashSet.add(albumName);
            collectiveData.edit().putStringSet("albums_list", hashSet).apply();
        }
    }

    public void removeAlbum(String albumName){
        if (albumData.contains(albumName)){
            albumData.edit().remove(albumName).apply();
            HashSet<String> hashSet = new HashSet<>(Objects.requireNonNull(collectiveData.getStringSet("albums_list", null)));
            hashSet.remove(albumName);
            collectiveData.edit().putStringSet("albums_list", hashSet).apply();
        }
    }

    public void addImageToAlbum(String imageURI, String albumName){
        createAlbum(albumName);

        HashSet<String> hashSet = new HashSet<>(Objects.requireNonNull(albumData.getStringSet(albumName, null)));
        hashSet.add(imageURI);
        albumData.edit().putStringSet(albumName, hashSet).apply();

    }

    public void removeImageFromAlbum(String imageURI, String albumName){
        HashSet<String> hashSet = new HashSet<>(Objects.requireNonNull(albumData.getStringSet(albumName, null)));
        hashSet.remove(imageURI);
        albumData.edit().putStringSet(albumName, hashSet).apply();
    }

    public void clearData(){
        collectiveData.edit().clear().apply();
        albumData.edit().clear().apply();
    }
}
