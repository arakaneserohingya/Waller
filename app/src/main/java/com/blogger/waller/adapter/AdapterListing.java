package com.blogger.waller.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogger.waller.R;
import com.blogger.waller.model.SectionCategory;
import com.blogger.waller.model.Wallpaper;
import com.blogger.waller.utils.Tools;

import java.util.ArrayList;
import java.util.List;

public class AdapterListing extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public enum ActionType {
        ITEM, CATEGORY
    }

    private final Context ctx;
    private List<Object> items = new ArrayList<>();

    private final int VIEW_ITEM = 200;
    private final int VIEW_TOP_TAB = 300;
    private final int VIEW_PROG = 0;
    private boolean loading = true;
    private String status;
    public String selectedCategory = null;
    private TextView previousCategory = null;
    private boolean categoryInitiated = false;
    private int page = 0;

    private AdapterListener<Object> listener;

    public void setListener(AdapterListener<Object> listener) {
        this.listener = listener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterListing(Context context, RecyclerView view, int page) {
        this.page = page;
        ctx = context;
        lastItemViewDetector(view);
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView image;
        public View lytParent;
        public View ic_multiple;

        public OriginalViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            image = v.findViewById(R.id.image_post);
            ic_multiple = v.findViewById(R.id.ic_multiple);
            lytParent = v.findViewById(R.id.lyt_parent);
        }
    }

    public class SectionTabViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout lytParent;

        public SectionTabViewHolder(View v) {
            super(v);
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listing, parent, false);
            vh = new OriginalViewHolder(v);
        } else if (viewType == VIEW_TOP_TAB) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section_tab, parent, false);
            vh = new SectionTabViewHolder(v);
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
            final Wallpaper obj = (Wallpaper) items.get(position);
            OriginalViewHolder v = (OriginalViewHolder) holder;

            v.title.setText(obj.title);
            v.title.setVisibility(obj.child ? View.GONE : View.VISIBLE);
            Tools.displayImage(ctx, v.image, obj.image);
            v.ic_multiple.setVisibility(obj.multiple ? View.VISIBLE : View.GONE);

            v.lytParent.setOnClickListener(view -> {
                if (listener == null) return;
                listener.onClick(view, ActionType.ITEM.name(), obj, position);
            });
        } else if (holder instanceof SectionTabViewHolder) {
            if (categoryInitiated) return;
            final SectionCategory obj = (SectionCategory) items.get(position);
            SectionTabViewHolder v = (SectionTabViewHolder) holder;
            v.lytParent.removeAllViews();
            for (String cat : obj.categories) {
                TextView textView = new TextView(ctx);
                // set margin
                int marginHorizontal = ctx.getResources().getDimensionPixelOffset(R.dimen.spacing_2);
                int marginVertical = ctx.getResources().getDimensionPixelOffset(R.dimen.spacing_4);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(marginHorizontal, marginVertical * 4, marginHorizontal * 4, 0);
                textView.setLayoutParams(params);

                // set padding
                int paddingHorizontal = ctx.getResources().getDimensionPixelOffset(R.dimen.spacing_15);
                int padding1 = ctx.getResources().getDimensionPixelOffset(R.dimen.spacing_1);
                int paddingVertical = textView.getPaddingTop();
                textView.setClickable(true);
                textView.setFocusable(true);
                textView.setPadding(paddingHorizontal, paddingVertical + padding1, paddingHorizontal, paddingVertical);
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(ctx.getResources().getColor(R.color.textIconPrimary));

                // set font and color
                textView.setBackgroundResource(R.drawable.button_tab_category);
                textView.setText(cat);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, ctx.getResources().getDimension(R.dimen.txt_small));
                textView.setOnClickListener(view -> {
                    if (listener == null) return;
                    if (selectedCategory != null && selectedCategory.equals(cat)) {
                        selectedCategory = null;
                        previousCategory = null;
                        textView.setSelected(false);
                        textView.setTextColor(ctx.getResources().getColor(R.color.textIconPrimary));
                    } else {
                        selectedCategory = cat;
                        textView.setSelected(true);
                        textView.setTextColor(ctx.getResources().getColor(R.color.cardClearBg));
                        if (previousCategory != null) {
                            previousCategory.setSelected(false);
                            previousCategory.setTextColor(ctx.getResources().getColor(R.color.textIconPrimary));
                        }
                        previousCategory = textView;
                    }
                    listener.onClick(view, ActionType.CATEGORY.name(), cat, position);
                });

                v.lytParent.addView(textView);
            }
            categoryInitiated = true;
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
        Object obj = items.get(position);
        if (obj instanceof Wallpaper) {
            return VIEW_ITEM;
        } else if (obj instanceof SectionCategory) {
            return VIEW_TOP_TAB;
        } else {
            return VIEW_PROG;
        }
    }

    public void insertCategory(SectionCategory category) {
        this.items.add(category);
        notifyItemInserted(0);
    }

    public void insertData(List<Wallpaper> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
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
        Object obj = this.items.size() == 0 ? null : this.items.get(0);
        int size = this.items.size();
        if (obj instanceof SectionCategory) {
            this.items = this.items.subList(0, 1);
            notifyItemRangeRemoved(1, size);
        } else {
            this.items = new ArrayList<>();
            notifyDataSetChanged();
        }
    }

    boolean scrollDown = false;

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            final GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE || !scrollDown) return;
                    int lastPos = layoutManager.findLastVisibleItemPosition();
                    boolean bottom = lastPos >= getItemCount() - page;
                    if (!loading && bottom && listener != null) {
                        onLoadMore();
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    scrollDown = dy > 0;
                }
            });

            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int type = getItemViewType(position);
                    int spanCount = layoutManager.getSpanCount();
                    return type == VIEW_ITEM ? 1 : spanCount;
                }
            });
        }
    }

    private void onLoadMore() {
        int current_page = getItemCount() / page;
        listener.onLoadMore(current_page);
        loading = true;
        status = null;
    }

}
