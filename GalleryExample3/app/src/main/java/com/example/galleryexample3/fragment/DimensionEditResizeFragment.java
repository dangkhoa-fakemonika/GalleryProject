package com.example.galleryexample3.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.galleryexample3.R;
import com.example.galleryexample3.imageediting.DimensionEditActivity;

public class DimensionEditResizeFragment extends Fragment {
    ImageView editingImageView;
    Bitmap editingBitmap;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dimension_edit_resize_fragment, container, false);
        editingImageView = view.findViewById(R.id.editingImageView);
        DimensionEditActivity parentActivity = (DimensionEditActivity) getActivity();
        if (parentActivity != null){
            editingBitmap = parentActivity.getEditingBitmap();
            if (editingBitmap != null){
                Glide.with(this).load(editingBitmap).diskCacheStrategy(DiskCacheStrategy.NONE).into(editingImageView);
            } else {
                Glide.with(this).load(R.drawable.uoh).diskCacheStrategy(DiskCacheStrategy.NONE).into(editingImageView);
            }
        }
        return view;
    }
}
