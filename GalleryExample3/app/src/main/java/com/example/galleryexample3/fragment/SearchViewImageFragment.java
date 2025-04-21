package com.example.galleryexample3.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.R;
import com.example.galleryexample3.SearchActivity;
import com.example.galleryexample3.SingleImageView;
import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.userinterface.GalleryImageGridAdapter;
import com.example.galleryexample3.userinterface.ItemClickSupporter;
import com.example.galleryexample3.userinterface.SearchItemListAdapter;

import java.util.ArrayList;

public class SearchViewImageFragment extends Fragment {
    RecyclerView myRecyclerView;
    String[] flagsForSingleView = {SingleImageView.FLAG_SEARCH_NAME, SingleImageView.FLAG_ALBUM, SingleImageView.FLAG_TAG};
    SearchActivity parentActivity;
//    Toolbar myToolBar;
    SearchItemListAdapter.MatchItem matchItem;
    ArrayList<String> imagesList;
    Toolbar myToolBar;
    public SearchViewImageFragment(SearchItemListAdapter.MatchItem matchItem){
        this.matchItem = matchItem;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_search_img_fragment, container, false);
        myRecyclerView = (RecyclerView) view.findViewById(R.id.imageSearchRecyclerView);
        myToolBar = (Toolbar) view.findViewById(R.id.imageFragmentToolBar);
        parentActivity = (SearchActivity) requireActivity();
        myToolBar.setNavigationIcon(R.drawable.baseline_arrow_back_24);
        myToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.onFragmentBackPressed();
            }
        });
        setImageAndTitle();
        if (!imagesList.isEmpty()){
            GalleryImageGridAdapter adapter = new GalleryImageGridAdapter(getContext(), imagesList);
            myRecyclerView.setAdapter(adapter);
            myRecyclerView.scrollToPosition(imagesList.size()-1);
            myRecyclerView.setVisibility(RecyclerView.VISIBLE);
        }

        ItemClickSupporter.addTo(myRecyclerView).setOnItemClickListener(new ItemClickSupporter.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                String imageUri = imagesList.get(position);
                String dateAdded = ImageGalleryProcessing.getImageDateAdded(parentActivity.getApplicationContext(), imageUri);
                Intent intent = new Intent(parentActivity.getApplicationContext(), SingleImageView.class);
                Bundle bundle = new Bundle();
                bundle.putString("imageURI", imageUri);
                bundle.putString("dateAdded", dateAdded);
                bundle.putInt("position", position);
                bundle.putString(flagsForSingleView[matchItem.getMatchType()], matchItem.getMatchName());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        //        myToolBar =(Toolbar) view.findViewById(R.id.imageFragmentToolBar);
//        myToolBar.setbAck
        return view;
    }

    public void setImageAndTitle(){
        if (matchItem.getMatchType() == SearchItemListAdapter.MATCH_IMAGE_NAME){
            myToolBar.setTitle("Image: " + matchItem.getMatchName());
            imagesList = ImageGalleryProcessing.getImagesByName(requireContext(), matchItem.getMatchName(), "DATE_ADDED", "ASC");
        } else if (matchItem.getMatchType() == SearchItemListAdapter.MATCH_ALBUM) {
            myToolBar.setTitle("Album: " + matchItem.getMatchName());
            imagesList = parentActivity.getDatabaseHandler().albums().getImagesOfAlbum(matchItem.getMatchName());

        } else if (matchItem.getMatchType() == SearchItemListAdapter.MATCH_TAG) {
            myToolBar.setTitle("Tag: " + matchItem.getMatchName());
//            No logic for tags yet
            imagesList = ImageGalleryProcessing.getImagesByName(requireContext(), matchItem.getMatchName(), "DATE_ADDED", "ASC");

        }
    }
}
