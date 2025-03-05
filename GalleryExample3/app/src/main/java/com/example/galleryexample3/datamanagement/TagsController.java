package com.example.galleryexample3.datamanagement;

import android.app.Activity;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Objects;

public class TagsController {
    SharedPreferences collectiveData;
    SharedPreferences tagData;
    SharedPreferences imageTagData;

    /**
     *  Initialize the Tags handler
     * @param activity The current activity (just pass in "this" or the parent activity).
     */

    public TagsController(Activity activity){
        collectiveData = activity.getSharedPreferences("collective_data", Activity.MODE_PRIVATE);
        tagData = activity.getSharedPreferences("tag_data", Activity.MODE_PRIVATE);
        imageTagData = activity.getSharedPreferences("image_tag_data", Activity.MODE_PRIVATE);

        if (collectiveData != null) {
            if (!collectiveData.contains("tags_list")) {
                SharedPreferences.Editor editor = collectiveData.edit();
                // test HashSet -> value
                HashSet<String> test = new HashSet<>();
                editor.putStringSet("tags_list", test);
                editor.apply();
            }
        }
    }

    /**
     * Get all albums currently in the system.
     */
    public HashSet<String> getAllTags(){
        return new HashSet<>(Objects.requireNonNull(collectiveData.getStringSet("tags_list", null)));
    }

    /**
     *
     * @param tagName The name of the tag
     * @return a Set of String of the images
     */
    public HashSet<String> getImagesInTags(String tagName){
        return new HashSet<>(Objects.requireNonNull(tagData.getStringSet(tagName, null)));
    }

    public HashSet<String> getTagInImages(String imageURI){
        return new HashSet<>(Objects.requireNonNull(imageTagData.getStringSet(imageURI, null)));
    }

    public void createTag(String tagName){
        if (!tagData.contains(tagName)){
            tagData.edit().putStringSet(tagName, new HashSet<>()).apply();

            HashSet<String> hashSet = new HashSet<>(Objects.requireNonNull(collectiveData.getStringSet("tags_list", null)));
            hashSet.add(tagName);
            collectiveData.edit().putStringSet("tags_list", hashSet).apply();
        }
    }

    public void removeTag(String tagName){
        if (tagData.contains(tagName)){
            tagData.edit().remove(tagName).apply();

            HashSet<String> hashSet = new HashSet<>(Objects.requireNonNull(collectiveData.getStringSet("tags_list", null)));
            hashSet.remove(tagName);
            collectiveData.edit().putStringSet("tags_list", hashSet).apply();
        }
    }

    public void addImageToTag(String imageURI, String tagName){
        createTag(tagName);

        HashSet<String> hashSet = new HashSet<>(Objects.requireNonNull(tagData.getStringSet(tagName, null)));
        hashSet.add(imageURI);
        tagData.edit().putStringSet(tagName, hashSet).apply();

    }

    public void removeImageFromTag(String imageURI, String tagName){
        HashSet<String> hashSet = new HashSet<>(Objects.requireNonNull(tagData.getStringSet(tagName, null)));
        hashSet.remove(imageURI);
        tagData.edit().putStringSet(tagName, hashSet).apply();
    }

    public void clearData(){
        collectiveData.edit().clear().apply();
        tagData.edit().clear().apply();
    }

}
