package com.andframe.adapter.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.andframe.api.adapter.ListItem;
import com.andframe.api.adapter.ListItemHolder;

/**
 * ListItem 和 ViewHolder 的结合
 * Created by SCWANG on 2016/9/10.
 */
public class ViewHolderItem<T> extends RecyclerView.ViewHolder implements ListItemHolder<T> {

    private final ListItem<T> item;

    public ViewHolderItem(ListItem<T> item, View view) {
        super(view);
        this.item = item;
    }

    public ListItem<T> getItem() {
        return item;
    }
}
