package com.example.galleryexample3;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.galleryexample3.userinterface.ThemeManager;

public class SettingsActivity extends AppCompatActivity {
    boolean isPrivateAlbumEnabled;
    Context context;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.setTheme(this);
        setContentView(R.layout.settings_activity);
        context = getApplicationContext();
        SwitchCompat enablePrivateAlbum = findViewById(R.id.enablePrivateAlbum);

        SharedPreferences sharedPref = this.getSharedPreferences("appSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        isPrivateAlbumEnabled = sharedPref.getBoolean("isPrivateAlbumEnabled", false);
        ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if (o.getResultCode() == AppCompatActivity.RESULT_OK){
                            Toast.makeText(context,"Heaven's door is opened", Toast.LENGTH_SHORT).show();
                            enablePrivateAlbum.setChecked(isPrivateAlbumEnabled);

                        }else {
                            Toast.makeText(context,"See you soon", Toast.LENGTH_SHORT).show();
                            isPrivateAlbumEnabled = false;
                            enablePrivateAlbum.setChecked(isPrivateAlbumEnabled);

                        }
                    }
                });
        enablePrivateAlbum.setChecked(isPrivateAlbumEnabled);
        enablePrivateAlbum.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isPrivateAlbumEnabled = b;
                if (b){
                    if (sharedPref.contains("heaven_key")){
                        Toast.makeText(context,"Heaven's door is opened", Toast.LENGTH_SHORT).show();
                        enablePrivateAlbum.setChecked(isPrivateAlbumEnabled);
                    }else{
                        Intent myIntent = new Intent(SettingsActivity.this, PrivateVaultCodeSettings.class);
                        myActivityResultLauncher.launch(myIntent);
                    }

                }
            }
        });

        findViewById(R.id.backButton).setOnClickListener((l) -> {
//            finish();
            Intent intent = new Intent(SettingsActivity.this, MainActivityNew.class);
            startActivity(intent);
        });

        ((Button) findViewById(R.id.changeAppTheme1)).setOnClickListener((l) -> {
            ThemeManager.switchToTheme(this, 1);
        });

        ((Button) findViewById(R.id.changeAppTheme2)).setOnClickListener((l) -> {
            ThemeManager.switchToTheme(this, 0);
        });

        ((Button) findViewById(R.id.changeAppTheme3)).setOnClickListener((l) -> {
            ThemeManager.switchToTheme(this, 2);
        });

        ((Button) findViewById(R.id.changeAppTheme4)).setOnClickListener((l) -> {
            ThemeManager.switchToTheme(this, 3);
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
