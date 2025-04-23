package com.example.galleryexample3.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.galleryexample3.GroupImageView;
import com.example.galleryexample3.MainActivityNew;
import com.example.galleryexample3.R;
import com.example.galleryexample3.SingleImageView;
import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.userinterface.GalleryAlbumGridAdapter;
import com.example.galleryexample3.userinterface.GalleryImageGridAdapter;
import com.example.galleryexample3.userinterface.ItemClickSupporter;
import com.example.galleryexample3.userinterface.SearchItemListAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Objects;

public class MainAlbumOverviewFragment extends Fragment {
    private ArrayList<String> albumsList;
    private ArrayList<String> albumThumbnailsList;
    boolean selectionEnabled = false;
    DatabaseHandler databaseHandler;
    private String sortType;

    public MainAlbumOverviewFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sortType = "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set up data
        View view = inflater.inflate(R.layout.main_album_overview_fragment, container, false);
        DatabaseHandler databaseHandler = DatabaseHandler.getInstance(requireContext());
        Intent gotIntent = requireActivity().getIntent();
        Bundle gotBundle = gotIntent.getExtras();
        sortType = gotBundle == null ? "Date - Ascending" : gotBundle.getString("sortType", "Date - Ascending");
        switch (Objects.requireNonNull(sortType)) {
            case "Name - Ascending":
                albumsList = databaseHandler.albums().getAllAlbumsWithFilters("albums.name", " ASC");
                break;
            case "Name - Descending":
                albumsList = databaseHandler.albums().getAllAlbumsWithFilters("albums.name", " DESC");
                break;
            case "Date - Ascending":
                albumsList = databaseHandler.albums().getAllAlbumsWithFilters("albums_info.time_create", " ASC");
                break;
            case "Date - Descending":
                albumsList = databaseHandler.albums().getAllAlbumsWithFilters("albums_info.time_create", " DESC");
                break;
            case "Size - Ascending":
                albumsList = databaseHandler.albums().getAllAlbumsWithFilters("totals", " ASC");
                break;
            case "Size - Descending":
                albumsList = databaseHandler.albums().getAllAlbumsWithFilters("totals", " DESC");
                break;
        }

//        albumsList = databaseHandler.albums().getAllAlbums();


        albumThumbnailsList = new ArrayList<>();
        RecyclerView gridRecyclerView = view.findViewById(R.id.gridRecyclerView);
        TextView noAlbumText = view.findViewById(R.id.noAlbumText);
        final GalleryAlbumGridAdapter[] albumAdapter = new GalleryAlbumGridAdapter[1];

        if (albumsList.isEmpty()) {
            gridRecyclerView.setVisibility(View.GONE);
            noAlbumText.setVisibility(View.VISIBLE);
        } else {
            for (String album : albumsList) {
                String thumbnail = databaseHandler.albums().getAlbumThumbnail(album);
                albumThumbnailsList.add(thumbnail);
            }
            albumAdapter[0] = new GalleryAlbumGridAdapter(requireContext(), albumsList, albumThumbnailsList);
            gridRecyclerView.setAdapter(albumAdapter[0]);
            noAlbumText.setVisibility(View.GONE);
            gridRecyclerView.setVisibility(View.VISIBLE);
            gridRecyclerView.scrollToPosition(albumsList.size() - 1);
        }

        Toolbar myToolbar = (Toolbar) requireActivity().findViewById(R.id.toolBar);
        myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int selectedId = item.getItemId();
                if (selectedId == R.id.filterButton && !albumsList.isEmpty()) {
                    PopupMenu popup = new PopupMenu(requireContext(), myToolbar, Gravity.END);
                    popup.inflate(R.menu.filter_menu_main);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            int id = menuItem.getItemId();
                            if (id == R.id.name_asc){
                                albumsList = databaseHandler.albums().getAllAlbumsWithFilters("albums.name", " ASC");
                                sortType = "Name - Ascending";
                            }
                            else if (id == R.id.name_desc){
                                albumsList = databaseHandler.albums().getAllAlbumsWithFilters("albums.name", " DESC");
                                sortType = "Name - Descending";
                            }
                            else if (id == R.id.date_asc){
                                albumsList = databaseHandler.albums().getAllAlbumsWithFilters("albums_info.time_create", " ASC");
                                sortType = "Date - Ascending";
                            }
                            else if (id == R.id.date_desc){
                                albumsList = databaseHandler.albums().getAllAlbumsWithFilters("albums_info.time_create", " DESC");
                                sortType = "Date - Descending";
                            }
                            else if (id == R.id.size_asc){
                                albumsList = databaseHandler.albums().getAllAlbumsWithFilters("totals", " ASC");
                                sortType = "Size - Ascending";
                            }
                            else if (id == R.id.size_desc){
                                albumsList = databaseHandler.albums().getAllAlbumsWithFilters("totals", " DESC");
                                sortType = "Size - Descending";
                            }
                            albumThumbnailsList.clear();
                            for (String album : albumsList) {
                                String thumbnail = databaseHandler.albums().getAlbumThumbnail(album);
                                albumThumbnailsList.add(thumbnail);
                            }
                            albumAdapter[0] = new GalleryAlbumGridAdapter(requireContext(), albumsList, albumThumbnailsList);
                            gridRecyclerView.setAdapter(albumAdapter[0]);
                            gridRecyclerView.scrollToPosition(albumsList.size() - 1);
                            return true;
                        }
                    });

                    popup.show();
                }
                return true;
            }
        });


        // Select album or enter album view
        ItemClickSupporter.addTo(gridRecyclerView).setOnItemClickListener(new ItemClickSupporter.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Context context = requireContext();
                String albumName = albumsList.get(position);
                Intent intent = new Intent(context, GroupImageView.class);
                Bundle bundle = new Bundle();
                bundle.putInt(GroupImageView.BUKEY_GROUP_TYPE, SearchItemListAdapter.MATCH_ALBUM);
                bundle.putString(GroupImageView.BUKEY_GROUP_NAME, albumName);
                bundle.putString("sortType", sortType);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        return view;
    }
}