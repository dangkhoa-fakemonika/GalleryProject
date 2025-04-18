package com.example.galleryexample3;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.userinterface.SearchItemListAdapter;

import java.util.ArrayList;


public class SearchActivity extends AppCompatActivity {
    LinearLayout searchBarLayout;
    RecyclerView searchedItemLayout;
    TextView noResultTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_image_album_view);
        searchBarLayout = (LinearLayout) findViewById(R.id.searchLinearLayout);
        searchedItemLayout = (RecyclerView) findViewById(R.id.searchRecyclerView);
        noResultTextView = (TextView) findViewById(R.id.noResultRecyclerView);
        DatabaseHandler handler = DatabaseHandler.getInstance(getApplicationContext());
        ArrayList<String> albumName = handler.albums().getAllAlbums();
        ArrayList<SearchItemListAdapter.MatchItem> matchItems = new ArrayList<>();
        for (String s : albumName){
            matchItems.add(new SearchItemListAdapter.MatchItem(s, String.valueOf(0), handler.albums().getAlbumThumbnail(s), SearchItemListAdapter.MATCH_ALBUM));
        }
        searchedItemLayout.setAdapter(new SearchItemListAdapter(getApplicationContext(), matchItems));
        searchedItemLayout.setVisibility(RecyclerView.VISIBLE);
        noResultTextView.setVisibility(TextView.GONE);
    }
}
