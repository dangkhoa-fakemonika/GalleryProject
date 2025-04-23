package com.example.galleryexample3.businessclasses;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.R;
import com.example.galleryexample3.userinterface.WidgetAdapter;

import org.json.JSONArray;

import java.util.ArrayList;

public class WidgetConfigActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WidgetAdapter widgetAdapter;
    private Button button;
    private ArrayList<String> imagesList;
    private int appWidgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
        setContentView(R.layout.widget_config);

        recyclerView = findViewById(R.id.widget_recyler);
        button = findViewById(R.id.widget_button);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        imagesList = ImageGalleryProcessing.getImages(this, "DATE_ADDED", " ASC");
        //widgetAdapter.notifyDataSetChanged();

        widgetAdapter = new WidgetAdapter(imagesList, selectedCount -> {
            button.setEnabled(selectedCount > 0);
        });
        recyclerView.setAdapter(widgetAdapter);

        button.setOnClickListener(v -> returnSelectedImages());
        button.setEnabled(false);
    }

    private void saveSelectedImages(ArrayList<String> selectedImages) {
        SharedPreferences prefs = getSharedPreferences("WidgetPrefs", MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();

        for (String URI : selectedImages) {
            jsonArray.put(URI);
        }

        prefs.edit()
                .putString("imagePaths_" + appWidgetId, jsonArray.toString())
                .putInt("currentIndex_" + appWidgetId, 0)
                .apply();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        WidgetProvider widgetProvider = new WidgetProvider();
        widgetProvider.onUpdate(this, appWidgetManager, new int[]{appWidgetId});

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    private void returnSelectedImages() {
        ArrayList<String> selectedImages = widgetAdapter.getSelectedImages();
        if (selectedImages.isEmpty()) return;

        saveSelectedImages(selectedImages);
    }

}
