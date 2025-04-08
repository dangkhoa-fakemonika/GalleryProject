// Source: https://www.littlerobots.nl/blog/Handle-Android-RecyclerView-Clicks/
// License: Unlicensed
package com.example.galleryexample3.userinterface;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryexample3.R;

public class ItemClickSupporter {
    private final RecyclerView recyclerView;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(v);
                onItemClickListener.onItemClicked(recyclerView, holder.getAdapterPosition(), v);
            }
        }
    };
    private final View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (onItemLongClickListener != null) {
                RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(v);
                return onItemLongClickListener.onItemLongClicked(recyclerView, holder.getAdapterPosition(), v);
            }
            return false;
        }
    };
    private final RecyclerView.OnChildAttachStateChangeListener attachListener
            = new RecyclerView.OnChildAttachStateChangeListener() {
        @Override
        public void onChildViewAttachedToWindow(@NonNull View view) {
            if (onItemClickListener != null) {
                view.setOnClickListener(onClickListener);
            }
            if (onItemLongClickListener != null) {
                view.setOnLongClickListener(onLongClickListener);
            }
        }

        @Override
        public void onChildViewDetachedFromWindow(@NonNull View view) {

        }
    };

    private ItemClickSupporter(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        this.recyclerView.setTag(R.id.item_click_supported, this);
        this.recyclerView.addOnChildAttachStateChangeListener(attachListener);
    }

    @NonNull
    public static ItemClickSupporter addTo(RecyclerView view) {
        ItemClickSupporter support = (ItemClickSupporter) view.getTag(R.id.item_click_supported);
        if (support == null) {
            support = new ItemClickSupporter(view);
        }
        return support;
    }

    public static ItemClickSupporter removeFrom(@NonNull RecyclerView view) {
        ItemClickSupporter support = (ItemClickSupporter) view.getTag(R.id.item_click_supported);
        if (support != null) {
            support.detach(view);
        }
        return support;
    }

    public ItemClickSupporter setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
        return this;
    }

    public ItemClickSupporter setOnItemLongClickListener(OnItemLongClickListener listener) {
        onItemLongClickListener = listener;
        return this;
    }

    private void detach(@NonNull RecyclerView view) {
        view.removeOnChildAttachStateChangeListener(attachListener);
        view.setTag(R.id.item_click_supported, null);
    }

    public interface OnItemClickListener {
        void onItemClicked(RecyclerView recyclerView, int position, View v);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClicked(RecyclerView recyclerView, int position, View v);
    }
}