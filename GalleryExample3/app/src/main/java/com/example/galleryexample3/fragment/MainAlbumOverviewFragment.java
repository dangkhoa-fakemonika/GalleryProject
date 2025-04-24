package com.example.galleryexample3.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.MediaStore;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class MainAlbumOverviewFragment extends Fragment {
    private ArrayList<String> albumsList;
    private ArrayList<String> albumThumbnailsList;
    boolean selectionEnabled = false;
    DatabaseHandler databaseHandler;
    private final String[] sortType = {"albums.name", " ASC"};
    GalleryAlbumGridAdapter albumAdapter;
    RecyclerView gridRecyclerView;
    TextView noAlbumText;

    private MediaStoreObserver mediaStoreObserver;
    private boolean osv = false;
    public class MediaStoreObserver extends ContentObserver {
        public MediaStoreObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            albumsList = databaseHandler.albums().getAllAlbumsWithFilters(sortType[0], sortType[1]);
            if (albumsList.isEmpty()) {
                albumThumbnailsList.clear();
                gridRecyclerView.setVisibility(View.GONE);
                noAlbumText.setVisibility(View.VISIBLE);
            } else {
                albumThumbnailsList.clear();
                for (String album : albumsList) {
                    String thumbnail = databaseHandler.albums().getAlbumThumbnail(album);
                    albumThumbnailsList.add(thumbnail);
                }
                albumAdapter.updateDataList(albumsList, albumThumbnailsList);
                noAlbumText.setVisibility(View.GONE);
                gridRecyclerView.setVisibility(View.VISIBLE);
                gridRecyclerView.scrollToPosition(albumsList.size() - 1);
            }
            requireContext().getContentResolver().unregisterContentObserver(mediaStoreObserver);
            osv = false;
            if (!osv) {
                Handler handler = new Handler();
                mediaStoreObserver = new MediaStoreObserver(handler);

                ContentResolver contentResolver = requireContext().getContentResolver();
                for (String thumb : albumThumbnailsList) {
                    contentResolver.registerContentObserver(ImageGalleryProcessing.getUriFromPath(requireContext(),thumb), true, mediaStoreObserver);
                }
                osv = true;
            }
        }
    }

    public MainAlbumOverviewFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set up data
        View view = inflater.inflate(R.layout.main_album_overview_fragment, container, false);
        databaseHandler = DatabaseHandler.getInstance(requireContext());
        albumsList = databaseHandler.albums().getAllAlbumsWithFilters(sortType[0], sortType[1]);

        albumThumbnailsList = new ArrayList<>();
        gridRecyclerView = view.findViewById(R.id.gridRecyclerView);
        noAlbumText = view.findViewById(R.id.noAlbumText);

        if (albumsList.isEmpty()) {
            gridRecyclerView.setVisibility(View.GONE);
            noAlbumText.setVisibility(View.VISIBLE);
        } else {
            for (String album : albumsList) {
                String thumbnail = databaseHandler.albums().getAlbumThumbnail(album);
                albumThumbnailsList.add(thumbnail);
            }
            albumAdapter = new GalleryAlbumGridAdapter(requireContext(), albumsList, albumThumbnailsList);
            gridRecyclerView.setAdapter(albumAdapter);
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
                                sortType[0] = "albums.name";
                                sortType[1] = " ASC";
                            }
                            else if (id == R.id.name_desc){
                                sortType[0] = "albums.name";
                                sortType[1] = " DESC";
                            }
                            else if (id == R.id.date_asc){
                                sortType[0] = "albums_info.time_create";
                                sortType[1] = " ASC";
                            }
                            else if (id == R.id.date_desc){
                                sortType[0] = "albums_info.time_create";
                                sortType[1] = " DESC";
                            }
                            else if (id == R.id.size_asc){
                                sortType[0] = "totals";
                                sortType[1] = " ASC";
                            }
                            else if (id == R.id.size_desc){
                                sortType[0] = "totals";
                                sortType[1] = " DESC";
                            }

                            albumsList = databaseHandler.albums().getAllAlbumsWithFilters(sortType[0], sortType[1]);
                            albumThumbnailsList.clear();
                            for (String album : albumsList) {
                                String thumbnail = databaseHandler.albums().getAlbumThumbnail(album);
                                albumThumbnailsList.add(thumbnail);
                            }
                                Log.i("ALBUM DEBUG", albumThumbnailsList.size() + "");
                            albumAdapter.updateDataList(albumsList, albumThumbnailsList);
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
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        albumsList = databaseHandler.albums().getAllAlbumsWithFilters(sortType[0], sortType[1]);
        if (albumsList.isEmpty()) {
            albumThumbnailsList.clear();
            gridRecyclerView.setVisibility(View.GONE);
            noAlbumText.setVisibility(View.VISIBLE);
        } else {
            albumThumbnailsList.clear();
            for (String album : albumsList) {
                String thumbnail = databaseHandler.albums().getAlbumThumbnail(album);
                albumThumbnailsList.add(thumbnail);
            }
            albumAdapter.updateDataList(albumsList, albumThumbnailsList);
            noAlbumText.setVisibility(View.GONE);
            gridRecyclerView.setVisibility(View.VISIBLE);
            gridRecyclerView.scrollToPosition(albumsList.size() - 1);
        }

        if (osv) {
            requireContext().getContentResolver().unregisterContentObserver(mediaStoreObserver);
            osv = false;
        }

        if (!osv) {
            Handler handler = new Handler();
            mediaStoreObserver = new MediaStoreObserver(handler);
            ContentResolver contentResolver = requireContext().getContentResolver();
            for (String thumb : albumThumbnailsList) {
                contentResolver.registerContentObserver(ImageGalleryProcessing.getUriFromPath(requireContext(),thumb), true, mediaStoreObserver);
            }
            osv = true;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        requireContext().getContentResolver().unregisterContentObserver(mediaStoreObserver);
        osv = false;
    }
}