package com.blogger.waller.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.blogger.waller.AppConfig;
import com.blogger.waller.R;
import com.blogger.waller.advertise.AdNetworkHelper;
import com.blogger.waller.data.ThisApp;
import com.blogger.waller.databinding.ActivityListingDetailBinding;
import com.blogger.waller.model.Wallpaper;
import com.blogger.waller.model.type.ApplyType;
import com.blogger.waller.room.table.EntityListing;
import com.blogger.waller.utils.Downloader;
import com.blogger.waller.utils.NotificationLoading;
import com.blogger.waller.utils.PermissionUtil;
import com.blogger.waller.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.flexbox.FlexboxLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActivityListingDetail extends AppCompatActivity {

    private static final String EXTRA_OBJECT = "key.EXTRA_OBJECT";
    private static final String EXTRA_POSITION = "key.EXTRA_POSITION";
    // extra obj
    private Wallpaper wallpaper;
    private List<Wallpaper> wallpapers = new ArrayList<>();
    private int position = -1;

    private Downloader downloader;

    private ActivityListingDetailBinding binding;


    // activity transition
    public static void navigate(Activity activity, Wallpaper wallpaper) {
        ThisApp.itemsWallpaper = new ArrayList<>();
        Intent i = new Intent(activity, ActivityListingDetail.class);
        i.putExtra(EXTRA_OBJECT, wallpaper);
        activity.startActivity(i);
    }

    public static void navigate(Activity activity, int position) {
        Intent i = new Intent(activity, ActivityListingDetail.class);
        i.putExtra(EXTRA_POSITION, position);
        activity.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListingDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        position = getIntent().getIntExtra(EXTRA_POSITION, -1);
        if (position == -1) {
            wallpaper = (Wallpaper) getIntent().getSerializableExtra(EXTRA_OBJECT);
            wallpapers.add(wallpaper);
        } else {
            wallpapers = ThisApp.itemsWallpaper;
            wallpaper = wallpapers.get(position);
        }
        downloader = new Downloader(this);

        initComponent();
        initToolbar();
        displayWallpaperData();
        prepareAds();

        Tools.RTLMode(getWindow());
    }

    private void initComponent() {
        if (position == -1) {
            binding.counter.setVisibility(View.GONE);
        } else {
            String counterText = (position + 1) + "/" + wallpapers.size();
            binding.counter.setText(counterText);
        }
        binding.btInfo.setOnClickListener(v -> showDialogInfo());
        binding.lytApply.setOnClickListener(v -> actionSetWallpaper());
    }

    private void initToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(null);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void displayWallpaperData() {
        binding.title.setText(wallpaper.title);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.view_pager);
        ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(this, wallpapers);
        mViewPager.setAdapter(mViewPagerAdapter);
        if (position != -1) {
            mViewPager.setCurrentItem(position);
        } else {
            binding.counter.setVisibility(View.GONE);
        }
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                wallpaper = wallpapers.get(position);
                binding.title.setText(wallpaper.title);
                refreshFavorite();
                String counterText = (position + 1) + "/" + wallpapers.size();
                binding.counter.setText(counterText);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        refreshFavorite();
    }

    public void detailsActionClick(View v) {
        int id = v.getId();
        if (id == R.id.lyt_share) {
            shareAction();
        } else if (id == R.id.lyt_download) {
            downloadAction();
        } else if (id == R.id.lyt_favorite) {
            if (!is_favorite) {
                ThisApp.dao().insertListing(EntityListing.entity(wallpaper));
            } else {
                ThisApp.dao().deleteListing(wallpaper.id);
            }
            refreshFavorite();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void actionSetWallpaper() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            showDialogSetWallpaper(applyType -> {
                binding.progressLoading.setVisibility(View.VISIBLE);
                new Handler(Looper.getMainLooper()).postDelayed(() -> loadBitmapAndSetWallpaper(applyType), 200);
            });
        } else {
            binding.progressLoading.setVisibility(View.VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(() -> loadBitmapAndSetWallpaper(ApplyType.BOTH), 200);
        }

    }

    @TargetApi(Build.VERSION_CODES.N)
    private void loadBitmapAndSetWallpaper(final ApplyType type) {
        Glide.with(this).asBitmap().load(wallpaper.image).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(ActivityListingDetail.this);
                wallpaperManager.setWallpaperOffsetSteps(0, 0);
                try {
                    if (type == ApplyType.HOME) {
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                    } else if (type == ApplyType.LOCK) {
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                    } else {
                        //wallpaperManager.setBitmap(bitmap);
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                    }
                    Tools.showToastCenter(ActivityListingDetail.this, R.string.set_success);
                } catch (IOException e) {
                    e.printStackTrace();
                    Tools.showToastCenter(ActivityListingDetail.this, R.string.set_failed);
                }
                binding.progressLoading.setVisibility(View.GONE);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                Tools.showToastCenter(ActivityListingDetail.this, R.string.set_failed);
                binding.progressLoading.setVisibility(View.GONE);
            }
        });
    }

    private void showDialogSetWallpaper(final SetWallpaperDialogCallback callback) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_set_wallpaper);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(true);
        (dialog.findViewById(R.id.lyt_home_screen)).setOnClickListener(v -> {
            callback.onSet(ApplyType.HOME);
            dialog.dismiss();
        });
        (dialog.findViewById(R.id.lyt_lock_screen)).setOnClickListener(v -> {
            callback.onSet(ApplyType.LOCK);
            dialog.dismiss();
        });
        (dialog.findViewById(R.id.lyt_both)).setOnClickListener(v -> {
            callback.onSet(ApplyType.BOTH);
            dialog.dismiss();
        });

        dialog.show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void downloadAction() {
        if (!PermissionUtil.isStorageGranted(this)) {
            if (ThisApp.pref().getNeverAskAgain(PermissionUtil.STORAGE)) {
                PermissionUtil.showDialog(this);
            } else {
                requestPermissions(PermissionUtil.PERMISSION_STORAGE, 500);
            }
            return;
        }

        if (Downloader.isFileExist(getApplicationContext(), wallpaper.getFilename())) {
            Tools.showToastCenter(ActivityListingDetail.this, R.string.already_download);
            return;
        }

        final NotificationLoading notificationLoading = new NotificationLoading(this, Integer.parseInt(wallpaper.getFilename()));
        notificationLoading.start(getString(R.string.wallpaper_download));
        downloader.setDownloadListener(new Downloader.DownloadListener() {
            @Override
            public void onFinish(Downloader.ResultObj result) {
                notificationLoading.stop(getString(result.success ? R.string.download_complete : R.string.download_failed), result);
            }
        });
        new Handler(Looper.getMainLooper()).postDelayed(() -> downloader.startDownload(getApplicationContext(), wallpaper), 200);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void shareAction() {
        if (!PermissionUtil.isStorageGranted(this)) {
            requestPermissions(PermissionUtil.PERMISSION_STORAGE, 500);
            if (ThisApp.pref().getNeverAskAgain(PermissionUtil.STORAGE)) {
                Tools.showToastCenter(ActivityListingDetail.this, R.string.storage_permission_denied);
            }
            return;
        }
        if (Downloader.isFileExist(getApplicationContext(), wallpaper.getFilename())) {
            Tools.methodShare(this, Downloader.getFile(getApplicationContext(), wallpaper.getFilename()));
        } else {
            binding.progressLoading.setVisibility(View.VISIBLE);
            downloader.startDownload(wallpaper, result -> {
                binding.progressLoading.setVisibility(View.GONE);
                if (!result.success) {
                    Tools.showToastCenter(ActivityListingDetail.this, R.string.failed_prepare_share);
                    return;
                }
                Tools.methodShare(ActivityListingDetail.this, Downloader.getFile(getApplicationContext(), wallpaper.getFilename()));
            });
        }
    }

    private boolean is_favorite = false;

    private void refreshFavorite() {
        if (ThisApp.dao().getListing(wallpaper.id) != null) {
            binding.imgFavorite.setImageResource(R.drawable.ic_favorite_added);
            is_favorite = true;
        } else {
            binding.imgFavorite.setImageResource(R.drawable.ic_favorite);
            is_favorite = false;
        }
    }

    private AdNetworkHelper adNetworkHelper;

    private void prepareAds() {
        adNetworkHelper = new AdNetworkHelper(this);
        adNetworkHelper.updateConsentStatus();
        adNetworkHelper.loadBannerAd(AppConfig.ads.ad_listing_details_banner);
    }

    private void showDialogInfo() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_more_info);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(true);

        final FlexboxLayout category_flex_box = dialog.findViewById(R.id.category_flex_box);

        if (wallpaper.category == null || wallpaper.category.size() == 0) {
            category_flex_box.setVisibility(View.GONE);
            return;
        }
        category_flex_box.removeAllViews();
        for (String cat : wallpaper.category) {
            TextView textView = new TextView(this);
            // set margin
            int marginHorizontal = getResources().getDimensionPixelOffset(R.dimen.spacing_4);
            int marginVertical = getResources().getDimensionPixelOffset(R.dimen.spacing_4);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(marginHorizontal, marginVertical + 5, marginHorizontal, marginVertical);
            textView.setLayoutParams(params);

            // set padding
            int paddingHorizontal = getResources().getDimensionPixelOffset(R.dimen.spacing_15);
            int paddingVertical = textView.getPaddingTop();
            textView.setClickable(true);
            textView.setFocusable(true);
            textView.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
            textView.setGravity(Gravity.CENTER);

            // set font and color
            textView.setBackgroundResource(R.drawable.button_category);
            textView.setText(cat);
            textView.setTextColor(getResources().getColor(R.color.textIconPrimary));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.txt_small));
            textView.setOnClickListener(view -> ActivityCategoryDetail.navigate(this, cat));

            category_flex_box.addView(textView);
        }

        ((TextView) dialog.findViewById(R.id.source_link)).setText(wallpaper.link);

        dialog.show();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 500) {
            for (String perm : permissions) {
                boolean rationale = shouldShowRequestPermissionRationale(perm);
                ThisApp.pref().setNeverAskAgain(perm, !rationale);
            }
            if (PermissionUtil.isStorageGranted(this)) {
                downloadAction();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private interface SetWallpaperDialogCallback {
        void onSet(ApplyType applyType);
    }


    class ViewPagerAdapter extends PagerAdapter {

        Context context;
        List<Wallpaper> wallpapers;
        LayoutInflater mLayoutInflater;

        public ViewPagerAdapter(Context context, List<Wallpaper> wallpapers) {
            this.context = context;
            this.wallpapers = wallpapers;
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return wallpapers.size();
        }

        public void addItem(Wallpaper item) {
            wallpapers.add(item);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == ((LinearLayout) object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            View itemView = mLayoutInflater.inflate(R.layout.item_image_wallpaper_details, container, false);
            ImageView main_image = itemView.findViewById(R.id.main_image);
            Tools.displayImageWallpaperDetails(context, main_image, wallpapers.get(position).image);
            Objects.requireNonNull(container).addView(itemView);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }

}