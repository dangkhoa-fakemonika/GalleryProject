package com.example.galleryexample3;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.fragment.SearchBarFragment;
import com.example.galleryexample3.fragment.SearchViewFragmentListener;
import com.example.galleryexample3.fragment.SearchViewImageFragment;
import com.example.galleryexample3.userinterface.ItemClickSupporter;
import com.example.galleryexample3.userinterface.SearchItemListAdapter;

import java.util.ArrayList;
import java.util.HashSet;


public class SearchActivity extends AppCompatActivity implements SearchViewFragmentListener {
    DatabaseHandler databaseHandler;
    FragmentManager myFragmentManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
//      View bindings
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_image_album_view);
        databaseHandler = DatabaseHandler.getInstance(getApplicationContext());
//      Listener adding
        SearchBarFragment searchBarFragment = new SearchBarFragment();
        myFragmentManager = getSupportFragmentManager();
        loadFragmentToView(searchBarFragment, "search");
//      Recycler View Item click listener

    }
    public void loadFragmentToView(Fragment fragment, String fragmentName){
        if (fragment != null){
            myFragmentManager.beginTransaction().replace(R.id.frameLayout, fragment).addToBackStack(fragmentName).commit();}else{
            Log.e("NoFragment", "No Fragment lmao");
        }
    }

    public DatabaseHandler getDatabaseHandler() {
        return databaseHandler;
    }
    public void onFragmentBackPressed(){
        myFragmentManager.popBackStack();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void switchBetweenFragment(String fromFragment, SearchItemListAdapter.MatchItem item) {
        if (fromFragment.equals("search") && item != null){
            loadFragmentToView(new SearchViewImageFragment(item.getMatchName()), item.getMatchName());
        }
    }

}
