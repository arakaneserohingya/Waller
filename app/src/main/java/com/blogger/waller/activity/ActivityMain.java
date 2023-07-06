package com.blogger.waller.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.blogger.waller.AppConfig;
import com.blogger.waller.R;
import com.blogger.waller.advertise.AdNetworkHelper;
import com.blogger.waller.data.ThisApp;
import com.blogger.waller.databinding.ActivityMainBinding;
import com.blogger.waller.fragment.FragmentCategory;
import com.blogger.waller.fragment.FragmentFavorite;
import com.blogger.waller.fragment.FragmentWallpaper;
import com.blogger.waller.utils.PermissionUtil;
import com.blogger.waller.utils.Tools;

public class ActivityMain extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FragmentWallpaper fragmentWallpapers;
    private FragmentCategory fragmentCategories;
    private FragmentFavorite fragmentFavorites;
    private Fragment selectedFragment;
    private FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ThisApp.get().initAds();

        initComponent();

        // set default fragment
        selectedFragment = fragmentWallpapers;
        displayFragment(R.id.nav_wallpapers, getString(R.string.menu_wallpapers));

        prepareAds();

        Tools.checkNotification(this);
        PermissionUtil.checkAndRequestNotification(this);
        Tools.RTLMode(getWindow());
    }

    private void initComponent() {
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        Tools.changeOverflowMenuIconColor(binding.toolbar, Color.WHITE);

        fm = getSupportFragmentManager();

        fragmentWallpapers = FragmentWallpaper.instance();
        fragmentCategories = FragmentCategory.instance();
        fragmentFavorites = FragmentFavorite.instance();

        fm.beginTransaction().add(R.id.frame_layout, fragmentWallpapers, getString(R.string.menu_wallpapers)).commit();
        fm.beginTransaction().add(R.id.frame_layout, fragmentCategories, getString(R.string.menu_category)).hide(fragmentCategories).commit();
        fm.beginTransaction().add(R.id.frame_layout, fragmentFavorites, getString(R.string.menu_favorite)).hide(fragmentFavorites).commit();

        binding.navigation.setOnItemSelectedListener(item -> {
            displayFragment(item.getItemId(), item.getTitle().toString());
            return true;
        });

        // listener for hide toolbar and bottom nav
        binding.appbarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            int offset = Math.abs(verticalOffset), range = appBarLayout.getTotalScrollRange();
            int heightMax = binding.lytBarContent.getHeight();
            if (range <= 0) return;
            int translationY = (offset * heightMax) / range;
            binding.lytBar.setTranslationY(translationY);
        });
    }


    public void displayFragment(int id, String title) {
        if (id == R.id.nav_wallpapers) {
            fm.beginTransaction().hide(selectedFragment).show(fragmentWallpapers).commit();
            selectedFragment = fragmentWallpapers;
        } else if (id == R.id.nav_categories) {
            fm.beginTransaction().hide(selectedFragment).show(fragmentCategories).commit();
            selectedFragment = fragmentCategories;
        } else if (id == R.id.nav_favorites) {
            fm.beginTransaction().hide(selectedFragment).show(fragmentFavorites).commit();
            fragmentFavorites.updateData();
            selectedFragment = fragmentFavorites;
        }
        binding.title.setText(title);
    }

    @Override
    public void onBackPressed() {
        doExitApp();
    }

    private long exitTime = 0;

    public void doExitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, R.string.press_again_exit_app, Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    static boolean active = false;

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        active = false;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Tools.checkGooglePlayUpdate(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Tools.checkGooglePlayUpdateStopListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            ActivitySearch.navigate(this);
        } else if (item.getItemId() == R.id.action_setting) {
            ActivitySetting.navigate(this);
        } else if (item.getItemId() == R.id.action_more_app) {
            Tools.directLinkCustomTab(this, AppConfig.general.more_apps_url);
        } else if (item.getItemId() == R.id.action_rate_app) {
            Tools.rateAction(this);
        } else if (item.getItemId() == R.id.action_about) {
            Tools.aboutAction(this);
        }
        return super.onOptionsItemSelected(item);
    }

    private AdNetworkHelper adNetworkHelper;

    private void prepareAds() {
        adNetworkHelper = new AdNetworkHelper(this);
        adNetworkHelper.updateConsentStatus();
        adNetworkHelper.loadBannerAd(AppConfig.ads.ad_main_banner);
        adNetworkHelper.loadInterstitialAd(AppConfig.ads.ad_main_interstitial);
    }

    public void showInterstitialAd() {
        adNetworkHelper.showInterstitialAd(AppConfig.ads.ad_main_interstitial);
    }
}