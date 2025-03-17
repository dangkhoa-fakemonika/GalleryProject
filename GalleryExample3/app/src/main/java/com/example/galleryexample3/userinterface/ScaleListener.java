package com.example.galleryexample3.userinterface;

import android.util.Log;
import android.view.ScaleGestureDetector;
import android.widget.GridView;

public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    private final ImageBaseAdapter imageAdapter;
    private final GridView gridView;

    public ScaleListener(ImageBaseAdapter imageAdapter, GridView gridView){
        super();
        this.imageAdapter = imageAdapter;
        this.gridView = gridView;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        int col = imageAdapter.getColumns();
        Log.i("SCALE", "" + detector.getScaleFactor());

        if (detector.getScaleFactor() < 0.98f && col > 2)
            col--;
        else if (detector.getScaleFactor() > 1.02f && col < 6)
            col++;

        imageAdapter.setColumns(col);
        gridView.setNumColumns(col);
    }
}
