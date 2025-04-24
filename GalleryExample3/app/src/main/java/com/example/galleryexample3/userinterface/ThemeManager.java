package com.example.galleryexample3.userinterface;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.galleryexample3.R;

public class ThemeManager {
    public static void setTheme(Activity activity){
        SharedPreferences preferences = activity.getSharedPreferences("appSettings", Context.MODE_PRIVATE);
        int themeID = preferences.getInt("theme", 1);
        switch (themeID){
            case 0:
                activity.setTheme(R.style.SimpleBrightTheme);
                break;
            case 1:
                activity.setTheme(R.style.SimpleDarkTheme);
                break;
            case 2:
                activity.setTheme(R.style.SimpleBlueTheme);
                break;
            case 3:
                activity.setTheme(R.style.SimplePinkTheme);
                break;
            default:
                activity.setTheme(R.style.SimpleBrightTheme);
                break;
        }
    }

    public static void switchToTheme(Activity activity, int theme){
        SharedPreferences preferences = activity.getSharedPreferences("appSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("theme", theme);
        editor.apply();

        switch (theme){
            case 0:
                activity.setTheme(R.style.SimpleBrightTheme);
                break;
            case 1:
                activity.setTheme(R.style.SimpleDarkTheme);
                break;
            case 2:
                activity.setTheme(R.style.SimpleBlueTheme);
                break;
            case 3:
                activity.setTheme(R.style.SimplePinkTheme);
                break;

            default:
                activity.setTheme(R.style.SimpleBrightTheme);
                break;
        }
        activity.recreate();
    }
}
