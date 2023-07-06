package com.blogger.waller.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.blogger.waller.R;
import com.blogger.waller.data.ThisApp;
import com.blogger.waller.databinding.ActivityDialogNotificationBinding;
import com.blogger.waller.room.table.NotificationEntity;
import com.blogger.waller.utils.Tools;

public class ActivityDialogNotification extends AppCompatActivity {

    private static final String EXTRA_OBJECT = "key.EXTRA_OBJECT";
    private static final String EXTRA_FROM_NOTIF = "key.EXTRA_FROM_NOTIF";
    private static final String EXTRA_POSITION = "key.EXTRA_FROM_POSITION";

    // activity transition
    public static void navigate(Activity activity, NotificationEntity obj, Boolean from_notif, int position) {
        Intent i = navigateBase(activity, obj, from_notif);
        i.putExtra(EXTRA_POSITION, position);
        activity.startActivity(i);
    }

    public static Intent navigateBase(Context context, NotificationEntity obj, Boolean from_notif) {
        Intent i = new Intent(context, ActivityDialogNotification.class);
        i.putExtra(EXTRA_OBJECT, obj);
        i.putExtra(EXTRA_FROM_NOTIF, from_notif);
        return i;
    }

    private ActivityDialogNotificationBinding binding;
    private Boolean fromNotif;
    private NotificationEntity notification;
    private Intent intent;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDialogNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        notification = (NotificationEntity) getIntent().getSerializableExtra(EXTRA_OBJECT);
        fromNotif = getIntent().getBooleanExtra(EXTRA_FROM_NOTIF, false);
        position = getIntent().getIntExtra(EXTRA_POSITION, -1);

        if (notification == null) {
            finish();
            intent = new Intent(this, ActivitySplash.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return;
        }

        // set notification as read
        notification.read = true;
        ThisApp.dao().insertNotification(notification);

        initComponent();

        Tools.RTLMode(getWindow());
    }

    private void initComponent() {
        binding.title.setText(notification.title);
        binding.content.setText(notification.content);
        binding.date.setText(Tools.getFormattedDateSimple(notification.created_at));

        String image_url = notification.image;
        intent = new Intent(this, ActivitySplash.class);

        if (fromNotif) {
            binding.btDelete.setVisibility(View.GONE);
        } else {
            binding.logo.setVisibility(View.GONE);
            binding.dialogTitle.setVisibility(View.GONE);
            binding.viewSpace.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(image_url)) {
            binding.lytImage.setVisibility(View.VISIBLE);
            Tools.displayImage(this, findViewById(R.id.image), image_url);
        } else {
            binding.lytImage.setVisibility(View.GONE);
        }

        binding.imgClose.setOnClickListener(view -> finish());

        binding.btOpen.setOnClickListener(v -> {
            finish();
            if (fromNotif) {
                ThisApp.get().setNotification(notification);
                if (ActivityMain.active) {
                    Tools.checkNotification(this);
                } else {
                    ActivitySplash.navigate(this);
                }
            } else {
                if (!TextUtils.isEmpty(notification.link)) {
                    Tools.directLinkCustomTab(this, notification.link);
                }
            }
        });

        binding.btDelete.setOnClickListener(view -> {
            finish();
            if (!fromNotif && position != -1) {
                ThisApp.dao().deleteNotification(notification.id);
                try {
                    ActivityNotification.getInstance().adapter.removeItem(position);
                    ActivityNotification.getInstance().adapter.notifyDataSetChanged();
                } catch (Exception e) {

                }

            }
        });
    }
}