package com.example.galleryexample3.userinterface;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.galleryexample3.R;

import java.util.ArrayList;


public class SearchItemListAdapter extends RecyclerView.Adapter<SearchItemListAdapter.SearchItemViewHolder> {
    public static int MATCH_IMAGE_NAME = 0;
    public static int MATCH_ALBUM = 1;
    public static int MATCH_TAG = 2;
    public static String[] matchTypeLabel = {"Images", "Albums", "Has tags"};
    private Context context;
    private ArrayList<SearchItemListAdapter.MatchItem> matchItems;

    public SearchItemListAdapter(Context context, ArrayList<SearchItemListAdapter.MatchItem> matchItems){
        this.context = context;
        this.matchItems = matchItems;
    }

    public ArrayList<MatchItem> getMatchItems() {
        return matchItems;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }


    @NonNull
    @Override
    public SearchItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_list_item, parent, false);
        return new SearchItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchItemListAdapter.SearchItemViewHolder holder, int position) {
        holder.matchName.setText(matchItems.get(position).getMatchName());
        holder.matchCount.setText(matchItems.get(position).getMatchCount());
        holder.matchType.setText(matchTypeLabel[matchItems.get(position).getMatchType()]);
        Glide.with(context)
                .load(matchItems.get(position).getMatchThumbnail())
                .error(R.drawable.uoh)
                .centerCrop()
                .into(holder.imageView);

    }
    @Override
    public int getItemCount() {
        return matchItems.size();
    }

    public static class SearchItemViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView matchName;
        TextView matchCount;
        TextView matchType;
        public SearchItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = (ImageView) itemView.findViewById(R.id.itemImage);
            this.matchName = (TextView) itemView.findViewById(R.id.matchName);
            this.matchCount = (TextView) itemView.findViewById(R.id.matchCount);
            this.matchType = (TextView) itemView.findViewById(R.id.matchType);
        }

    }
    public static class MatchItem {
        int matchType;
        String matchName;
        String matchCount;
        String matchThumbnail;
        public MatchItem(String matchName, String matchCount, String matchThumbnail ,int MATCH_TYPE){
            this.matchType = MATCH_TYPE;
            this.matchName = matchName;
            this.matchCount = matchCount;
            this.matchThumbnail = matchThumbnail;
        }

        public String getMatchThumbnail() {
            return matchThumbnail;
        }

        public String getMatchName() {
            return matchName;
        }

        public String getMatchCount() {
            return matchCount;
        }

        public int getMatchType() {
            return matchType;
        }
    }
}
