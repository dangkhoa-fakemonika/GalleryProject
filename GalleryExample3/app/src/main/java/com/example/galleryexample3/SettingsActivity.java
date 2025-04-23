package com.example.galleryexample3;

import static android.app.PendingIntent.getActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends Activity {
    boolean isPrivateAlbumEnabled;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        SwitchCompat enablePrivateAlbum = findViewById(R.id.enablePrivateAlbum);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
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
