package com.blogger.waller.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blogger.waller.R;
import com.blogger.waller.adapter.AdapterNotification;
import com.blogger.waller.data.ThisApp;
import com.blogger.waller.databinding.ActivityNotificationBinding;
import com.blogger.waller.room.table.NotificationEntity;
import com.blogger.waller.utils.Tools;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ActivityNotification extends AppCompatActivity {
    public static void navigate(Activity activity) {
        Intent i = new Intent(activity, ActivityNotification.class);
        activity.startActivity(i);
    }

    public ActivityNotificationBinding binding;

    public AdapterNotification adapter;
    static ActivityNotification activityNotification;

    public static ActivityNotification getInstance() {
        return activityNotification;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        activityNotification = this;

        initToolbar();
        iniComponent();

        Tools.RTLMode(getWindow());
    }

    private void initToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_back);
        binding.toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.toolbarIconText), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        binding.toolbarMenuDelete.setOnClickListener(v -> {
            if (adapter.getItemCount() == 0) {
                Snackbar.make(binding.getRoot(), R.string.msg_notif_empty, Snackbar.LENGTH_SHORT).show();
                return;
            }
            dialogDeleteConfirmation();
        });

    }

    private void iniComponent() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //set data and list adapter
        adapter = new AdapterNotification(this, binding.recyclerView, new ArrayList<>());
        binding.recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener((view, obj, pos) -> {
            obj.read = true;
            ActivityDialogNotification.navigate(ActivityNotification.this, obj, false, pos);
        });

        startLoadMoreAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    public void dialogDeleteConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_delete_confirm);
        builder.setMessage(getString(R.string.content_delete_confirm) + getString(R.string.title_activity_notification));
        builder.setPositiveButton(R.string.YES, (di, i) -> {
            di.dismiss();
            ThisApp.dao().deleteAllNotification();
            startLoadMoreAdapter();
            Snackbar.make(binding.getRoot(), R.string.delete_success, Snackbar.LENGTH_SHORT).show();
        });
        builder.setNegativeButton(R.string.CANCEL, null);
        builder.show();
    }

    private void startLoadMoreAdapter() {
        adapter.resetListData();
        List<NotificationEntity> items = ThisApp.dao().getNotificationByPage(10, 0);
        adapter.insertData(items);
        showNoItemView(false);
        final int item_count = ThisApp.dao().getNotificationCount();
        showNoItemView(item_count == 0);
        // detect when scroll reach bottom
        adapter.setOnLoadMoreListener(current_page -> {
            if (item_count > adapter.getItemCount() && current_page != 0) {
                displayDataByPage(current_page);
            } else {
                adapter.setLoaded();
            }
        });
    }

    private void displayDataByPage(final int next_page) {
        adapter.setLoading();
        new Handler().postDelayed(() -> {
            List<NotificationEntity> items = ThisApp.dao().getNotificationByPage(10, (next_page * 10));
            adapter.insertData(items);
        }, 500);
    }

    private void showNoItemView(boolean show) {
        ((TextView) findViewById(R.id.failed_subtitle)).setText(getString(R.string.empty_state_no_data));
        binding.lytFailed.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}

