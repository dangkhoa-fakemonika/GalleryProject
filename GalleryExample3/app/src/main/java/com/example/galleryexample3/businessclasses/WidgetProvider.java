package com.example.galleryexample3.businessclasses;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

import com.example.galleryexample3.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class WidgetProvider extends AppWidgetProvider {
    private static final String ACTION_CLICKED = "CLICKED";
    private static final String ACTION_SWIPE_LEFT = "SWIPE_LEFT";
    private static final String ACTION_SWIPE_RIGHT = "SWIPE_RIGHT";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE);
        String imagePathsJson = prefs.getString("imagePaths_" + appWidgetId, "[]");
        int currentIndex = prefs.getInt("currentIndex_" + appWidgetId, 0);

        try {
            JSONArray imagePaths = new JSONArray(imagePathsJson);
            if (imagePaths.length() == 0) {
                Log.i("debug", "khong co anh");
                return;
            }

            currentIndex %= imagePaths.length();
            String currentImagePath = imagePaths.getString(currentIndex);

            Bitmap bitmap = BitmapFactory.decodeFile(currentImagePath);

            if (bitmap == null) {
                imagePaths.remove(currentIndex);
                currentIndex = currentIndex % imagePaths.length();

                prefs.edit()
                        .putString("imagePaths_" + appWidgetId, imagePaths.toString())
                        .putInt("currentIndex_" + appWidgetId, currentIndex)
                        .apply();

                updateWidget(context, appWidgetManager, appWidgetId);
                return;
            }

            float h = bitmap.getHeight();
            float w = bitmap.getWidth();
            float scale = h > w ? h > 2000 ? h / 2000 : 1 : w > 2000 ? w / 2000 : 1;

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            views.setImageViewBitmap(R.id.widget_image, Bitmap.createScaledBitmap(bitmap, (int)(w / scale), (int)(h / scale), true));

            setupSwipeActions(context, appWidgetId, views);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        } catch (JSONException e) {
            Log.e("debug", "Error parsing JSON", e);
        }
    }

    private void setupSwipeActions(Context context, int appWidgetId, RemoteViews views) {
        Intent swipeLeft = new Intent(context, WidgetProvider.class)
                .setAction(ACTION_SWIPE_LEFT)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        Intent swipeRight = new Intent(context, WidgetProvider.class)
                .setAction(ACTION_SWIPE_RIGHT)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        Intent clicked = new Intent(context, WidgetProvider.class)
                .setAction(ACTION_CLICKED)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        PendingIntent leftPI = PendingIntent.getBroadcast(context, appWidgetId, swipeLeft,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent rightPI = PendingIntent.getBroadcast(context, appWidgetId, swipeRight,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent clickPI = PendingIntent.getBroadcast(context, appWidgetId, clicked,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        views.setOnClickPendingIntent(R.id.widget_left, leftPI);
        views.setOnClickPendingIntent(R.id.widget_right, rightPI);
        views.setOnClickPendingIntent(R.id.widget_image, clickPI);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction() != null) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            switch (intent.getAction()) {
                case ACTION_SWIPE_LEFT:
                    updateImageIndex(context, appWidgetId, -1);
                    break;
                case ACTION_SWIPE_RIGHT:
                    updateImageIndex(context, appWidgetId, 1);
                    break;
                case ACTION_CLICKED:
                    Intent launchIntent = context.getPackageManager()
                            .getLaunchIntentForPackage(context.getPackageName());
                    if (launchIntent != null) {
                        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(launchIntent);
                    }
                    break;
            }
        }
    }

    private void updateImageIndex(Context context, int appWidgetId, int delta) {
        SharedPreferences prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE);
        int currentIndex = prefs.getInt("currentIndex_" + appWidgetId, 0);
        String imagePathsJson = prefs.getString("imagePaths_" + appWidgetId, "[]");

        try {
            JSONArray imagePaths = new JSONArray(imagePathsJson);
            if (imagePaths.length() == 0) return;

            currentIndex = (currentIndex + delta + imagePaths.length()) % imagePaths.length();
            prefs.edit()
                    .putInt("currentIndex_" + appWidgetId, currentIndex)
                    .apply();

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            updateWidget(context, appWidgetManager, appWidgetId);
        } catch (JSONException e) {
            Log.e("debug", "Error parsing JSON", e);
        }
    }
}