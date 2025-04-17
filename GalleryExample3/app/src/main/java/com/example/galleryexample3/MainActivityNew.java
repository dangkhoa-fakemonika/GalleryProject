package com.example.galleryexample3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.fragment.MainGalleryFragment;
import com.example.galleryexample3.userinterface.GalleryImageGridAdapter;
import com.example.galleryexample3.userinterface.ItemClickSupporter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class MainActivityNew extends AppCompatActivity {
    private Context context;
    private Toolbar myToolbar;
    private AppBarLayout myAppBarLayout;
    private TextView toolBarTitle;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        context = this;

        myToolbar = (Toolbar) findViewById(R.id.toolBar);
//        myAppBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        setSupportActionBar(myToolbar);

        MainGalleryFragment galleryFragment = new MainGalleryFragment();

        BottomNavigationView bottomNavView = (BottomNavigationView) findViewById(R.id.bottomNavigationBar);

        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, galleryFragment).commit();
    }

    public void showBottomNavigation() {
        BottomNavigationView bottomNavView = (BottomNavigationView) findViewById(R.id.bottomNavigationBar);
        bottomNavView.setVisibility(View.VISIBLE);
    }

    public void hideBottomNavigation() {
        BottomNavigationView bottomNavView = (BottomNavigationView) findViewById(R.id.bottomNavigationBar);
        bottomNavView.setVisibility(View.GONE);
    }
}
