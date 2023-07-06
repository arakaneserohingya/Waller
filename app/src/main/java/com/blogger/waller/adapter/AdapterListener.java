package com.blogger.waller.adapter;

import android.view.View;

public class AdapterListener<T> {

    public void onClick(View view, String type, T obj, int position) {

    }

    public void onSavedClick(View view, boolean saved, T obj, int position) {

    }

    public void onLoadMore(int page) {

    }


}