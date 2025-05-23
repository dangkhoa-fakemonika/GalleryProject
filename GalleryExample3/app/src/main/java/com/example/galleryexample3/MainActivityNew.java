package com.example.galleryexample3;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.fragment.MainAlbumOverviewFragment;
import com.example.galleryexample3.fragment.MainGalleryFragment;
import com.example.galleryexample3.userinterface.ThemeManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashSet;

public class MainActivityNew extends AppCompatActivity{
    NavigationView navigationView;
    private Context context;
    private Toolbar myToolbar;
    private AppBarLayout myAppBarLayout;
    private TextView toolBarTitle;
    final int REQUEST_MANAGE_EXTERNAL_STORAGE = 100;
    final int REQUEST_WRITE_EXTERNAL_STORAGE = 101;
    final int REQUEST_READ_EXTERNAL_STORAGE = 102;
    final int REQUEST_CAMERA = 103;
    private ActivityResultLauncher<Intent> launcher;

    private FloatingActionButton cameraFAB;
    private FloatingActionButton addAlbumFAB;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MANAGE_EXTERNAL_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "App require permissions to function.", Toast.LENGTH_SHORT).show();
                    finishAffinity();
                }
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE || requestCode == REQUEST_READ_EXTERNAL_STORAGE || requestCode == REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //
            } else {
                Toast.makeText(this, "App require permissions to function.", Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        DrawerLayout drawerLayout = findViewById(R.id.drawer_nav_view);
        super.onCreate(savedInstanceState);
        ThemeManager.setTheme(this);
        setContentView(R.layout.activity_main_new);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }
        context = this;
        myToolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(myToolbar);
        cameraFAB = (FloatingActionButton) findViewById(R.id.fab_camera);
        addAlbumFAB = (FloatingActionButton) findViewById(R.id.fab_new_album);
        addAlbumFAB.hide();
        cameraFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivityNew.this, CameraActivity.class);
                startActivity(myIntent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        MainGalleryFragment galleryFragment = new MainGalleryFragment();
        MainAlbumOverviewFragment albumOverviewFragment = new MainAlbumOverviewFragment();

        BottomNavigationView bottomNavView = (BottomNavigationView) findViewById(R.id.bottomNavigationBar);

        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, galleryFragment).commit();

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.mainDrawerLayout);
        navigationView = (NavigationView) findViewById(R.id.drawer_nav_view);

        SharedPreferences sharedPref = this.getSharedPreferences("appSettings", Context.MODE_PRIVATE);
        boolean privateAlbum = sharedPref.getBoolean("isPrivateAlbumEnabled", false);
        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.navPrivateAlbum);
        Log.i("PRIVATE", privateAlbum + "");
        menuItem.setVisible(privateAlbum);
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if (o.getResultCode() ==  RESULT_OK){
                            Intent myIntent = new Intent(MainActivityNew.this, PrivateVaultActivity.class);
                            startActivity(myIntent);
                        }else {
                            Toast.makeText(MainActivityNew.this, "Not this time bro", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
//        myToolbar.setNavigationIcon(R.drawable.menu_24px);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        bottomNavView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.gallery){
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, galleryFragment).commit();
                    cameraFAB.show();
                    bottomNavView.getMenu().getItem(0).setChecked(true);
                }
                else{
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, albumOverviewFragment).commit();
                    cameraFAB.hide();
                    bottomNavView.getMenu().getItem(1).setChecked(true);
                }

                return false;
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navClose){
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }

                Intent intent;
                if (item.getItemId() == R.id.navManageTags)
                    intent = new Intent(MainActivityNew.this, TagManagementActivity.class);
                else if (item.getItemId() == R.id.navToCamera)
                    intent = new Intent(MainActivityNew.this, CameraActivity.class);
                else if (item.getItemId() == R.id.navSearchImage)
                    intent = new Intent(MainActivityNew.this, SearchActivity.class);
                else if (item.getItemId() == R.id.navSettings)
                    intent = new Intent(MainActivityNew.this, SettingsActivity.class);
                else if (item.getItemId() == R.id.navPrivateAlbum){
                    intent = new Intent(MainActivityNew.this, PrivateVaultLockScreen.class);
                }
                else{
                    intent = new Intent(MainActivityNew.this, MainActivityNew.class);
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                if (item.getItemId() == R.id.navPrivateAlbum){
                    launcher.launch(intent);
                }else startActivity(intent);
                return true;
            }
        });
    }

    public void showBottomNavigation() {
        BottomNavigationView bottomNavView = (BottomNavigationView) findViewById(R.id.bottomNavigationBar);
        bottomNavView.setVisibility(View.VISIBLE);
    }

    public void hideBottomNavigation() {
        BottomNavigationView bottomNavView = (BottomNavigationView) findViewById(R.id.bottomNavigationBar);
        bottomNavView.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topbar_action_buttons, menu);
        return true;
    }

    public FloatingActionButton getCameraFAB() {
        return cameraFAB;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("appSettings", Context.MODE_PRIVATE);
        Menu menu = navigationView.getMenu();;
        MenuItem menuItem = menu.findItem(R.id.navPrivateAlbum);
        menuItem.setVisible(sharedPreferences.getBoolean("isPrivateAlbumEnabled", false));
    }
}
