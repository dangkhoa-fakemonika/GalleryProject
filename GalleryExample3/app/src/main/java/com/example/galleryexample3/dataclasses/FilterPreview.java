package com.example.galleryexample3.dataclasses;

import android.graphics.Bitmap;

public class FilterPreview {
    private String filterName;
    private Bitmap bitmap;

    public FilterPreview(String name, Bitmap imageUrl) {
        this.filterName = name;
        this.bitmap = imageUrl;
    }

    public String getFilterName() {
        return filterName;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}