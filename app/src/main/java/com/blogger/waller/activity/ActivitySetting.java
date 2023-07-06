package com.blogger.waller.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.blogger.waller.AppConfig;
import com.blogger.waller.R;
import com.blogger.waller.data.ThisApp;
import com.blogger.waller.databinding.ActivitySettingBinding;
import com.blogger.waller.room.DAO;
import com.blogger.waller.utils.Tools;
import com.google.android.material.snackbar.Snackbar;

public class ActivitySetting extends AppCompatActivity {

    public static void navigate(Activity activity) {
        Intent i = new Intent(activity, ActivitySetting.class);
        activity.startActivity(i);
    }

    public ActivitySettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
        initComponent();

        Tools.RTLMode(getWindow());
    }

    private void initToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_back);
        binding.toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.toolbarIconText), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    private void initComponent() {
        String cacheSize = Tools.getImageCacheSize(this);
        binding.subtitleCache.setText(String.format(getString(R.string.setting_subtitle_cache), cacheSize));

        binding.actionCache.setOnClickListener(v -> showDialogClearCache());

        binding.actionSwitchTheme.setOnClickListener(view -> actionTheme());

        binding.actionTheme.setOnClickListener(view -> actionTheme());

        binding.actionNotification.setOnClickListener(view -> ActivityNotification.navigate(this));

        binding.actionNotificationSetting.setOnClickListener(view -> Tools.goToNotificationSettings(this));

        binding.actionPrivacy.setOnClickListener(view -> Tools.directLinkCustomTab(this, AppConfig.general.privacy_policy_url));

        binding.actionMoreApp.setOnClickListener(view -> Tools.directLinkCustomTab(this, AppConfig.general.more_apps_url));

        binding.actionRate.setOnClickListener(view -> Tools.rateAction(this));

        binding.actionAboutApp.setOnClickListener(view -> Tools.aboutAction(this));

    }

    private void actionTheme() {
        dialogThemeConfirmation();
    }

    private void validateTheme() {
        if (ThisApp.pref().isDarkTheme()) {
            binding.actionSwitchTheme.setImageResource(R.drawable.ic_toggle_on);
        } else {
            binding.actionSwitchTheme.setImageResource(R.drawable.ic_toggle_off);
        }
    }

    private void showDialogClearCache() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_confirm_title));
        builder.setMessage(getString(R.string.message_clear_image_cache));
        builder.setPositiveButton(R.string.YES, (dialogInterface, i) -> {
            Tools.clearImageCacheOnBackground(getApplicationContext());
            binding.subtitleCache.setText(String.format(getString(R.string.setting_subtitle_cache), "0 Bytes"));
            Snackbar.make(binding.getRoot(), getString(R.string.message_after_clear_image_cache), Snackbar.LENGTH_SHORT).show();
        });
        builder.setNegativeButton(R.string.CANCEL, null);
        builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        DAO dao = ThisApp.dao();
        binding.notifCount.setText(dao.getNotificationCount() + "");
        int notifCount = ThisApp.dao().getNotificationUnreadCount();
        binding.read.setVisibility(notifCount > 0 ? View.VISIBLE : View.INVISIBLE);
    }

    public void dialogThemeConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.change_theme_title);
        builder.setMessage(R.string.change_theme_confirmation);
        builder.setPositiveButton(R.string.CONTINUE, (di, i) -> {
            di.dismiss();
            ThisApp.pref().setDarkTheme(!ThisApp.pref().isDarkTheme());
            Tools.applyTheme(ThisApp.pref().isDarkTheme());
            validateTheme();
            Tools.restartApp(ActivitySetting.this);
        });
        builder.setNegativeButton(R.string.CANCEL, null);
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

