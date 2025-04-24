package com.example.galleryexample3.dataclasses;

public class EditParams {
    public float brightness;
    public float contrast;
    public float saturation;
    public String filter;
    public int radius;

    public EditParams(float brightness, float contrast, float saturation, String filter, int radius) {
        this.brightness = brightness;
        this.contrast = contrast;
        this.saturation = saturation;
        this.filter = filter;
        this.radius = radius;
    }

    public EditParams copy() {
        return new EditParams(this.brightness, this.contrast, this.saturation, this.filter, this.radius);
    }
}
