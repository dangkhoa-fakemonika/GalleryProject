package com.example.galleryexample3.dataclasses;

public class AdjustmentOption {
    private int iconResourceId;
    private String name;
    private float value;
    private float defaultValue;

    public AdjustmentOption(int iconResourceId, String name, float value, float defaultValue) {
        this.iconResourceId = iconResourceId;
        this.name = name;
        this.value = value;
        this.defaultValue = defaultValue;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public String getName() {
        return name;
    }

    public float getValue() {
        return value;
    }

    public float getDefaultValue() {
        return defaultValue;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
