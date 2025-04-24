package com.example.galleryexample3;

import static android.app.PendingIntent.getActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.example.galleryexample3.userinterface.ThemeManager;

public class SettingsActivity extends Activity {
    boolean isPrivateAlbumEnabled;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.setTheme(this);
        setContentView(R.layout.settings_activity);

        SwitchCompat enablePrivateAlbum = findViewById(R.id.enablePrivateAlbum);

        SharedPreferences sharedPref = this.getSharedPreferences("appSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        isPrivateAlbumEnabled = sharedPref.getBoolean("isPrivateAlbumEnabled", false);

        enablePrivateAlbum.setChecked(isPrivateAlbumEnabled);
        enablePrivateAlbum.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isPrivateAlbumEnabled = b;
            }
        });

        findViewById(R.id.backButton).setOnClickListener((l) -> {
            Intent intent = new Intent(SettingsActivity.this, MainActivityNew.class);
            startActivity(intent);
        });

        ((Button) findViewById(R.id.changeAppTheme)).setOnClickListener((l) -> {
            int currentTheme = sharedPref.getInt("theme", 0);
            if (currentTheme == 0){
                ThemeManager.switchToTheme(this, 1);
            }
            else if (currentTheme == 1){
                ThemeManager.switchToTheme(this, 0);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPref = this.getSharedPreferences("appSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isPrivateAlbumEnabled", isPrivateAlbumEnabled);
        editor.apply();
        Log.i("PRIVATE SET", sharedPref.getBoolean("isPrivateAlbumEnabled", false) + "");
    }
}
