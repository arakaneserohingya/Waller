package com.blogger.waller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blogger.waller.R;

import java.util.ArrayList;
import java.util.List;

public class AdapterCategory extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context ctx;
    private List<String> items = new ArrayList<>();

    private final int VIEW_ITEM = 2;
    private final int VIEW_PROG = 0;
    private boolean loading = true;
    private String status;

    private AdapterListener<String> listener;

    public void setListener(AdapterListener<String> listener) {
        this.listener = listener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterCategory(Context context) {
        ctx = context;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public View lytParent;

        public OriginalViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            lytParent = v.findViewById(R.id.lyt_parent);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progress_loading;
        public TextView status_loading;

        public ProgressViewHolder(View v) {
            super(v);
            progress_loading = v.findViewById(R.id.progress_loading);
            status_loading = v.findViewById(R.id.status_loading);
        }
    }

    public void insertData(List<String> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
            vh = new OriginalViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        int position = holder.getAdapterPosition();
        if (holder instanceof OriginalViewHolder) {
            final String category = items.get(position);
            OriginalViewHolder v = (OriginalViewHolder) holder;
            v.name.setText(category);
            v.lytParent.setOnClickListener(view -> {
                if (listener == null) return;
                listener.onClick(view, null, category, position);
            });
        } else {
            final ProgressViewHolder v = (ProgressViewHolder) holder;
            v.progress_loading.setVisibility(status == null ? View.VISIBLE : View.INVISIBLE);
            v.status_loading.setVisibility(status == null ? View.INVISIBLE : View.VISIBLE);

            if (status == null) return;
            v.status_loading.setText(status);
            v.status_loading.setOnClickListener(view -> {
                setLoaded();
                onLoadMore();
            });
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return this.items.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    public void setLoaded() {
        status = null;
        loading = false;
        int last_index = getItemCount() - 1;
        if (last_index > -1 && items.get(last_index) == null) {
            items.remove(last_index);
            notifyItemRemoved(last_index);
        }
    }

    public void setLoadingOrFailed(String status) {
        setLoaded();
        this.status = status;
        this.items.add(null);
        notifyItemInserted(getItemCount() - 1);
        loading = true;
    }

    public void resetListData() {
        this.items = new ArrayList<>();
        notifyDataSetChanged();
    }

    private void onLoadMore() {
        listener.onLoadMore(1);
        loading = true;
        status = null;
    }

}