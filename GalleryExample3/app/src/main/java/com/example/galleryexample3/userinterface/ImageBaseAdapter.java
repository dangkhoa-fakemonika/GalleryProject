package com.example.galleryexample3.userinterface;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.galleryexample3.R;

import java.util.ArrayList;

public class ImageBaseAdapter extends BaseAdapter {
    private final Activity context;
    private int columns;
    private int page;
    private final int maxPage;
    private final int loadLimit;
    private final ArrayList<String> images;
    int SCREEN_WIDTH; // Number of images per load
    int SCREEN_HEIGHT;

    public ImageBaseAdapter(Activity localContext, int setLoadLimit, ArrayList<String> images) {
        context = localContext;
        this.images = images;
        columns = 2;
        page = 0;
        maxPage = images.size() / setLoadLimit;
        loadLimit = setLoadLimit;

        WindowManager mWinMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        SCREEN_WIDTH = displaymetrics.widthPixels;
        SCREEN_HEIGHT = displaymetrics.heightPixels;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public int getColumns() {
        return columns;
    }

    public int getCount() {
        return Math.min(loadLimit, images.size());
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        ImageView picturesView;
        if (convertView == null) {
            picturesView = new ImageView(context);
            picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            picturesView
                    .setLayoutParams(new GridView.LayoutParams(SCREEN_WIDTH / columns, SCREEN_WIDTH / columns));

        } else {
            picturesView = (ImageView) convertView;
        }

        ArrayList<String> pageImages = new ArrayList<>(images.subList(page * loadLimit, Math.min((page + 1) * loadLimit, images.size())));

        Glide.with(context).load(pageImages.get(position))
                .placeholder(R.drawable.uoh).centerCrop()
                .into(picturesView);

        return picturesView;
    }
}