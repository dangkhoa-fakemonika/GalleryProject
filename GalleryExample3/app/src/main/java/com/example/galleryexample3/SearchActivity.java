package com.example.galleryexample3;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.userinterface.SearchItemListAdapter;

import java.util.ArrayList;
import java.util.HashSet;


public class SearchActivity extends AppCompatActivity {
    LinearLayout searchBarLayout;
    RecyclerView searchedItemLayout;
    TextView noResultTextView;
    ImageButton searchImageButton;
    EditText searchBarText;
    DatabaseHandler databaseHandler;
    HashSet<SearchItemListAdapter.MatchItem> matchItems;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
//      View bindings
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_image_album_view);
        searchBarLayout = (LinearLayout) findViewById(R.id.searchLinearLayout);
        searchedItemLayout = (RecyclerView) findViewById(R.id.searchRecyclerView);
        noResultTextView = (TextView) findViewById(R.id.noResultRecyclerView);
        searchImageButton = (ImageButton) findViewById(R.id.searchImageButton) ;
        searchBarText = (EditText) findViewById(R.id.searchBarEditText);
        matchItems = new HashSet<>();
//      Initial State
        searchedItemLayout.setVisibility(RecyclerView.GONE);
        noResultTextView.setVisibility(TextView.VISIBLE);
        databaseHandler = DatabaseHandler.getInstance(getApplicationContext());
//      Listener adding
        searchImageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String text = searchBarText.getText().toString();
                if (!text.trim().isEmpty()){
                    SearchItemListAdapter.MatchItem matchItem = getMatchImageItem(text);
                    if (matchItem != null){
                        matchItems.add(matchItem);
                    }
                    ArrayList<SearchItemListAdapter.MatchItem> matchAlbums = databaseHandler.albums().getMatchAlbumItems(text);
                    Log.e("saldsadjlksakj", String.valueOf(matchAlbums.size()));
                    matchItems.addAll(matchAlbums);
                }
                if(! matchItems.isEmpty()){
                    SearchItemListAdapter adapter = new SearchItemListAdapter(getApplicationContext(), new ArrayList<>(matchItems));
                    searchedItemLayout.setAdapter(adapter);
                    searchedItemLayout.setVisibility(RecyclerView.VISIBLE);
                    noResultTextView.setVisibility(TextView.GONE);
                }
            }
        });
    }
    public SearchItemListAdapter.MatchItem getMatchImageItem(String name){
        return ImageGalleryProcessing.getMatchImageItem(getApplicationContext(), name);
    }
}
