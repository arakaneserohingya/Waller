package com.blogger.waller.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.blogger.waller.activity.ActivityCategoryDetail;
import com.blogger.waller.activity.ActivityListingDetail;
import com.blogger.waller.activity.ActivityMain;
import com.blogger.waller.adapter.AdapterListener;
import com.blogger.waller.adapter.AdapterListing;
import com.blogger.waller.data.ThisApp;
import com.blogger.waller.databinding.FragmentWallpaperBinding;
import com.blogger.waller.model.SectionCategory;
import com.blogger.waller.model.Wallpaper;
import com.blogger.waller.utils.Tools;

import java.util.ArrayList;
import java.util.List;

import dreamspace.blogger.sdk.listener.RequestListener;
import dreamspace.blogger.sdk.model.Listing;
import dreamspace.blogger.sdk.model.ParamList;
import dreamspace.blogger.sdk.model.RespPosts;

public class FragmentWallpaper extends Fragment {

    private FragmentWallpaperBinding binding;
    private AdapterListing adapter;
    private boolean allLoaded = false;

    public static FragmentWallpaper instance() {
        return new FragmentWallpaper();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWallpaperBinding.inflate(inflater, container, false);
        initComponent();

        requestAction(1);
        return binding.getRoot();
    }

    private void initComponent() {
        binding.recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), Tools.getGridSpanCount(getActivity())));

        //set data and list adapter
        adapter = new AdapterListing(getActivity(), binding.recyclerView, AppConfig.general.listing_pagination_count);
        adapter.insertCategory(new SectionCategory(ThisApp.get().getCategories()));
        binding.recyclerView.setAdapter(adapter);

        // detect when scroll reach bottom
        adapter.setListener(new AdapterListener<Object>() {
            @Override
            public void onClick(View view, String type, Object obj, int position) {
                super.onClick(view, type, obj, position);
                if (type.equals(AdapterListing.ActionType.ITEM.name())) {
                    Wallpaper w = (Wallpaper) obj;
                    if (w.multiple) {
                        ActivityCategoryDetail.navigate((ActivityMain) getActivity(), w);
                    } else {
                        ActivityListingDetail.navigate((ActivityMain) getActivity(), w);
                    }
                    try {
                        ((ActivityMain) getActivity()).showInterstitialAd();
                    } catch (Exception e) {
                    }
                } else if (type.equals(AdapterListing.ActionType.CATEGORY.name())) {
                    adapter.resetListData();
                    requestAction(1);
                }
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
            showNoItemView(false);
            swipeProgress(true);
        } else {
            adapter.setLoadingOrFailed(null);
        }
        request(page_no);
    }

    private void request(Integer pageNo) {
        ParamList param = new ParamList();
        param.url = AppConfig.general.blogger_url;
        param.page = pageNo.toString();
        param.count = AppConfig.general.listing_pagination_count.toString();
        param.category = adapter.selectedCategory;

        ThisApp.get().bloggerAPI.getPosts(param, new RequestListener<RespPosts>() {
            @Override
            public void onSuccess(RespPosts resp) {
                super.onSuccess(resp);
                allLoaded = resp.list.size() < AppConfig.general.listing_pagination_count || resp.list.size() == 0;
                displayApiResult(resp.list);
            }

            @Override
            public void onFailed(String messages) {
                super.onFailed(messages);
                Log.e("Error", messages);
                onFailRequest();
            }
        });
    }

    private void onFailRequest() {
        swipeProgress(false);
        if (Tools.isConnect(getActivity())) {
            adapter.setLoadingOrFailed(getString(R.string.failed_text));
        } else {
            adapter.setLoadingOrFailed(getString(R.string.no_internet_text));
        }
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

    private void displayApiResult(final List<Listing> items) {
        List<Wallpaper> wallpapers = new ArrayList<>();
        for (Listing l : items) {
            wallpapers.add(Tools.parseListingToWallpaper(l));
        }
        adapter.insertData(wallpapers);
        swipeProgress(false);
        showNoItemView(adapter.getItemCount() == 0);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}