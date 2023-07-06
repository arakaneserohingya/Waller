package com.blogger.waller.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.blogger.waller.AppConfig;
import com.blogger.waller.R;
import com.blogger.waller.activity.ActivityListingDetail;
import com.blogger.waller.activity.ActivityMain;
import com.blogger.waller.adapter.AdapterListener;
import com.blogger.waller.adapter.AdapterListing;
import com.blogger.waller.data.ThisApp;
import com.blogger.waller.databinding.FragmentFavoriteBinding;
import com.blogger.waller.model.Wallpaper;
import com.blogger.waller.room.table.EntityListing;
import com.blogger.waller.utils.Tools;

import java.util.ArrayList;
import java.util.List;

import dreamspace.blogger.sdk.model.ParamList;

public class FragmentFavorite extends Fragment {

    private FragmentFavoriteBinding binding;
    private AdapterListing adapter;
    private boolean allLoaded = false;
    private static int last_count = -1;

    public static FragmentFavorite instance() {
        return new FragmentFavorite();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false);
        initComponent();
        return binding.getRoot();
    }

    private void initComponent() {
        binding.recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), Tools.getGridSpanCount(getActivity())));

        //set data and list adapter
        adapter = new AdapterListing(getActivity(), binding.recyclerView, AppConfig.general.listing_pagination_count);
        binding.recyclerView.setAdapter(adapter);

        // detect when scroll reach bottom
        adapter.setListener(new AdapterListener<Object>() {
            @Override
            public void onClick(View view, String type, Object obj, int position) {
                super.onClick(view, type, obj, position);
                Wallpaper w = (Wallpaper) obj;
                ActivityListingDetail.navigate((ActivityMain) getActivity(), w);
            }

            @Override
            public void onLoadMore(int page) {
                super.onLoadMore(page);
                if (allLoaded) {
                    adapter.setLoaded();
                } else {
                    int next_page = page + 1;
                    requestAction(next_page);
                }
            }

        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            allLoaded = false;
            adapter.resetListData();
            requestAction(1);
        });
    }

    private void requestAction(final int page_no) {
        if (page_no == 1) {
            adapter.resetListData();
            showNoItemView(false);
            swipeProgress(true);
        } else {
            adapter.setLoadingOrFailed(null);
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> request(page_no), 200);
    }

    private void request(Integer pageNo) {
        ParamList param = new ParamList();
        param.page = pageNo.toString();
        param.count = AppConfig.general.listing_pagination_count.toString();

        Integer count = Integer.parseInt(param.count);
        int offset = (pageNo - 1) * count;
        List<EntityListing> entityListings = ThisApp.dao().getAllListingByPage(count, offset);
        List<Wallpaper> items = new ArrayList<>();
        for (EntityListing e : entityListings) {
            items.add(e.original());
        }
        displayApiResultPlace(items);
        allLoaded = (adapter.getItemCount() >= ThisApp.dao().getListingCount());
    }

    private void showNoItemView(boolean show) {
        ((TextView) binding.lytFailed.findViewById(R.id.failed_subtitle)).setText(getString(R.string.empty_state_no_data));
        if (show) {
            binding.lytFailed.setVisibility(View.VISIBLE);
        } else {
            binding.lytFailed.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        binding.swipeRefresh.post(() -> binding.swipeRefresh.setRefreshing(show));
        if (!show) {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.shimmer.setVisibility(View.GONE);
            binding.shimmer.stopShimmer();
            return;
        }
        binding.recyclerView.setVisibility(View.GONE);
        binding.shimmer.setVisibility(View.VISIBLE);
        binding.shimmer.startShimmer();
    }

    private void displayApiResultPlace(final List<Wallpaper> items) {
        adapter.insertData(items);
        swipeProgress(false);
        showNoItemView(adapter.getItemCount() == 0);
    }

    public void updateData(){
        int listing_count = ThisApp.dao().getListingCount();
        if(listing_count != last_count){
            last_count = listing_count;
            requestAction(1);
        }
    }
}