package com.example.galleryexample3.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.R;
import com.example.galleryexample3.SearchActivity;
import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.userinterface.ItemClickSupporter;
import com.example.galleryexample3.userinterface.SearchItemListAdapter;

import java.util.ArrayList;
import java.util.HashSet;

public class SearchBarFragment extends Fragment implements MenuProvider {
    SearchActivity parentActivity;
    RecyclerView searchedItemLayout;
    TextView noResultTextView;
    Toolbar myToolBar;
    HashSet<SearchItemListAdapter.MatchItem> matchItems;
    SearchItemListAdapter searchFragmentAdapter;
    String searchText;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_bar_fragment, container, false);
        searchedItemLayout = (RecyclerView) view.findViewById(R.id.searchRecyclerView);
        noResultTextView = (TextView) view.findViewById(R.id.noResultRecyclerView);
        myToolBar = (Toolbar) view.findViewById(R.id.searchToolBar);
        myToolBar.setTitle("Search for somethings");
        matchItems = new HashSet<>();
        parentActivity = (SearchActivity) getActivity();
        assert parentActivity != null;
        parentActivity.setSupportActionBar(myToolBar);
        parentActivity.addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.CREATED);
//      Initial State
        searchedItemLayout.setVisibility(RecyclerView.GONE);
        noResultTextView.setVisibility(TextView.VISIBLE);
//        searchImageButton.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                String text = searchBarText.getText().toString();
//                if (text != null){
//                    matchItems = getMatchItems(text);
//                }
//                if(! matchItems.isEmpty()){
//                    searchFragmentAdapter = new SearchItemListAdapter(requireContext(), new ArrayList<>(matchItems));
//                    searchedItemLayout.setAdapter(searchFragmentAdapter);
//                    searchedItemLayout.setVisibility(RecyclerView.VISIBLE);
//                    noResultTextView.setVisibility(TextView.GONE);
//                }
//            }
//        });
        ItemClickSupporter.addTo(searchedItemLayout).setOnItemClickListener(new ItemClickSupporter.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                SearchItemListAdapter.MatchItem item = (SearchItemListAdapter.MatchItem) searchFragmentAdapter.getSelectedMatchItem(position);
                if (parentActivity != null){
                    ((SearchViewFragmentListener) parentActivity).switchBetweenFragment("search", item);
                }
            }
        });
        return view;
    }



    public HashSet<SearchItemListAdapter.MatchItem> getMatchItems(String name){
        HashSet<SearchItemListAdapter.MatchItem> l =  new HashSet<>();
        if (!name.trim().isEmpty()){
            SearchItemListAdapter.MatchItem matchItem = ImageGalleryProcessing.getMatchImageItem(getContext(), name);
            if (matchItem != null){
                l.add(matchItem);
            }
            ArrayList<SearchItemListAdapter.MatchItem> matchAlbums = parentActivity.getDatabaseHandler().albums().getMatchAlbumItems(name);
            l.addAll(matchAlbums);
        }
        return l;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parentActivity.addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.search_bar, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
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
                    searchFragmentAdapter = new SearchItemListAdapter(requireContext(), new ArrayList<>(matchItems));
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
                    searchFragmentAdapter = new SearchItemListAdapter(requireContext(), new ArrayList<>(matchItems));
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
    public void onResume() {
        super.onResume();
        if (searchFragmentAdapter != null){
            searchedItemLayout.setAdapter(searchFragmentAdapter);
            searchedItemLayout.setVisibility(RecyclerView.VISIBLE);
            noResultTextView.setVisibility(TextView.GONE);
        }else if(matchItems.isEmpty()){
            if (searchText != null){
                matchItems = getMatchItems(searchText);
            }
            if(! matchItems.isEmpty()){
                searchFragmentAdapter = new SearchItemListAdapter(requireContext(), new ArrayList<>(matchItems));
                searchedItemLayout.setAdapter(searchFragmentAdapter);
                searchedItemLayout.setVisibility(RecyclerView.VISIBLE);
                noResultTextView.setVisibility(TextView.GONE);
            }
        }
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

//    @Override
//    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.search_bar, menu);
//        MenuItem menuItem = menu.findItem(R.id.action_search);
//        SearchView searchView = (SearchView) menuItem.getActionView();
//        assert searchView != null;
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String s) {
//                if (s != null){
//                    matchItems = getMatchItems(s);
//                } else return false;
//                if(! matchItems.isEmpty()){
//                    searchFragmentAdapter = new SearchItemListAdapter(requireContext(), new ArrayList<>(matchItems));
//                    searchedItemLayout.setAdapter(searchFragmentAdapter);
//                    searchedItemLayout.setVisibility(RecyclerView.VISIBLE);
//                    noResultTextView.setVisibility(TextView.GONE);
//                    return true;
//                }
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String s) {
//                return false;
//            }});
//    }
}
