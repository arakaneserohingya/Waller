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
import com.blogger.waller.activity.ActivityMain;
import com.blogger.waller.adapter.AdapterCategory;
import com.blogger.waller.adapter.AdapterListener;
import com.blogger.waller.data.ThisApp;
import com.blogger.waller.databinding.FragmentCategoryBinding;
import com.blogger.waller.utils.Tools;

import java.util.List;

import dreamspace.blogger.sdk.listener.RequestListener;

public class FragmentCategory extends Fragment {

    public static FragmentCategory instance() {
        return new FragmentCategory();
    }

    private FragmentCategoryBinding binding;
    private AdapterCategory adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        initComponent();

        if (ThisApp.get().getCategories().size() == 0) {
            requestAction();
        } else {
            displayApiResult(ThisApp.get().getCategories());
        }
        return binding.getRoot();
    }

    private void initComponent() {
        binding.recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));

        //set data and list adapter
        adapter = new AdapterCategory(getActivity());
        binding.recyclerView.setAdapter(adapter);

        // detect when scroll reach bottom
        adapter.setListener(new AdapterListener<String>() {
            @Override
            public void onClick(View view, String type, String obj, int position) {
                super.onClick(view, type, obj, position);
                ActivityCategoryDetail.navigate(getActivity(), obj);
                try {
                    ((ActivityMain) getActivity()).showInterstitialAd();
                } catch (Exception e) {
                }
            }

            @Override
            public void onLoadMore(int page) {
                super.onLoadMore(page);
                requestAction();
            }
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            adapter.resetListData();
            requestAction();
        });
    }

    private void requestAction() {
        showNoItemView(false);
        swipeProgress(true);
        new Handler(Looper.getMainLooper()).postDelayed(() -> request(), 200);
    }

    private void request() {
        ThisApp.get().bloggerAPI.getCategories(AppConfig.general.blogger_url, new RequestListener<List<String>>() {
            @Override
            public void onSuccess(List<String> resp) {
                super.onSuccess(resp);
                displayApiResult(resp);
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

    private void displayApiResult(final List<String> items) {
        ThisApp.get().setCategories(items);
        adapter.insertData(ThisApp.get().getCategories());
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
