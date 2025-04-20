package com.example.galleryexample3.fragment;

import com.example.galleryexample3.userinterface.SearchItemListAdapter;

public interface SearchViewFragmentListener {
    public void switchBetweenFragment(String fromFragment, SearchItemListAdapter.MatchItem item);
}
