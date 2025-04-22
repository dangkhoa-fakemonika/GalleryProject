package com.example.galleryexample3;

import android.content.Intent;
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
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.userinterface.ItemClickSupporter;
import com.example.galleryexample3.userinterface.SearchItemListAdapter;

import java.util.ArrayList;
import java.util.HashSet;


public class SearchActivity extends AppCompatActivity implements MenuProvider {
    SearchItemListAdapter searchFragmentAdapter;
    DatabaseHandler databaseHandler;
    OnBackPressedCallback onBackPressedCallback;
    RecyclerView searchedItemLayout;
    TextView noResultTextView;
    Toolbar myToolBar;
    HashSet<SearchItemListAdapter.MatchItem> matchItems;
    String searchText;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
//      View bindings
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_image_album_view);
        databaseHandler = DatabaseHandler.getInstance(getApplicationContext());
        searchedItemLayout = (RecyclerView) findViewById(R.id.searchRecyclerView);
        noResultTextView = (TextView) findViewById(R.id.noResultRecyclerView);
        myToolBar = (Toolbar) findViewById(R.id.searchToolBar);
        myToolBar.setTitle("Search for somethings");
        matchItems = new HashSet<>();

        setSupportActionBar(myToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24);
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
        myToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        addMenuProvider(this, this, Lifecycle.State.CREATED);

        searchedItemLayout.setVisibility(RecyclerView.GONE);
        noResultTextView.setVisibility(TextView.VISIBLE);
        ItemClickSupporter.addTo(searchedItemLayout).setOnItemClickListener(new ItemClickSupporter.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                SearchItemListAdapter.MatchItem item = (SearchItemListAdapter.MatchItem) searchFragmentAdapter.getSelectedMatchItem(position);
                Intent myIntent = new Intent(SearchActivity.this, GroupImageView.class);
                Bundle myBundle = new Bundle();
                myBundle.putInt(GroupImageView.BUKEY_GROUP_TYPE, item.getMatchType());
                myBundle.putString(GroupImageView.BUKEY_GROUP_NAME, item.getMatchName());
                myBundle.putString(GroupImageView.BUKEY_GROUP_COUNT, item.getMatchCount());
                myIntent.putExtras(myBundle);
                startActivity(myIntent);
            }
        });
//      Listener adding

//      Recycler View Item click listener

    }


    public DatabaseHandler getDatabaseHandler() {
        return databaseHandler;
    }


    public HashSet<SearchItemListAdapter.MatchItem> getMatchItems(String name){
        HashSet<SearchItemListAdapter.MatchItem> l =  new HashSet<>();
        if (!name.trim().isEmpty()){
            SearchItemListAdapter.MatchItem matchItem = ImageGalleryProcessing.getMatchImageItem(this, name);
            if (matchItem != null){
                l.add(matchItem);
            }
            ArrayList<SearchItemListAdapter.MatchItem> matchAlbums = getDatabaseHandler().albums().getMatchAlbumItems(name);
            l.addAll(matchAlbums);
        }
        return l;
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
    protected void onStop() {
        super.onStop();
     }


    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.search_bar, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) menuItem.getActionView();
        assert searchView != null;
        searchView.setQueryHint("File/Album/Tag name");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (s != null){
                    searchText = s;
                    matchItems = getMatchItems(s);
                } else return false;
                if(! matchItems.isEmpty()){
                    searchFragmentAdapter = new SearchItemListAdapter(SearchActivity.this, new ArrayList<>(matchItems));
                    searchedItemLayout.setAdapter(searchFragmentAdapter);
                    searchedItemLayout.setVisibility(RecyclerView.VISIBLE);
                    noResultTextView.setVisibility(TextView.GONE);
                    return true;
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                matchItems.clear();

                if (s != null){
                    searchText = s;
                    matchItems = getMatchItems(s);
                }
                if(! matchItems.isEmpty()){
                    searchFragmentAdapter = new SearchItemListAdapter(SearchActivity.this, new ArrayList<>(matchItems));
                    searchedItemLayout.setAdapter(searchFragmentAdapter);
                    searchedItemLayout.setVisibility(RecyclerView.VISIBLE);
                    noResultTextView.setVisibility(TextView.GONE);
                }else {
                    searchedItemLayout.setVisibility(RecyclerView.GONE);
                    noResultTextView.setVisibility(TextView.VISIBLE);
                }
                return true;
            }
        });
    }


    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

}
