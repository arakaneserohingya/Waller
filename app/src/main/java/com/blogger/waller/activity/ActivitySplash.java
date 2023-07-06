package com.blogger.waller.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.blogger.waller.AppConfig;
import com.blogger.waller.R;
import com.blogger.waller.data.ThisApp;
import com.blogger.waller.databinding.ActivitySplashBinding;
import com.blogger.waller.utils.AppConfigExt;
import com.blogger.waller.utils.Tools;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.List;

import dreamspace.blogger.sdk.listener.RequestListener;

public class ActivitySplash extends AppCompatActivity {

    // activity transition
    public static void navigate(Activity activity) {
        Intent i = new Intent(activity, ActivitySplash.class);
        activity.startActivity(i);
    }

    private ActivitySplashBinding binding;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_down_in);
        binding.title.startAnimation(animation);

        Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_up_in);
        binding.shimmer.startAnimation(animation2);

        if (AppConfig.USE_REMOTE_CONFIG) {
            requestRemoteConfig();
            // add timer to prevent too long waiting about 10 sec
            new Handler().postDelayed(() -> {
                if (ActivitySplash.active)
                    dialogFailedRemoteConfig(getString(R.string.message_failed_config));
            }, 10 * 1000);
        } else {
            requestCategoriesData();
        }

        Tools.fullScreen(this);
    }

    private void requestRemoteConfig() {
        Log.d("REMOTE_CONFIG", "requestRemoteConfig");
        FirebaseRemoteConfig firebaseRemoteConfig = ThisApp.get().getFirebaseRemoteConfig();
        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this, (OnCompleteListener<Boolean>) task -> {
            if (!ActivitySplash.active || (alertDialog != null && alertDialog.isShowing())) {
                return;
            }
            if (task.isSuccessful()) {
                Log.d("REMOTE_CONFIG", "SUCCESS");
                boolean updated = task.getResult();
                AppConfigExt.setFromRemoteConfig(firebaseRemoteConfig);
                requestCategoriesData();
            } else {
                Log.d("REMOTE_CONFIG", "FAILED");
                dialogFailedRemoteConfig(getString(R.string.message_failed_config));
            }
        });
    }

    public void dialogFailedRemoteConfig(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.failed);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.RETRY, (dialog, which) -> {
            dialog.dismiss();
            requestRemoteConfig();
        });
        alertDialog = builder.show();
    }

    public void dialogFailedCategories(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.failed);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.RETRY, (dialog, which) -> {
            dialog.dismiss();
            requestRemoteConfig();
        });
        alertDialog = builder.show();
    }

    private void requestCategoriesData() {
        ThisApp.get().initBloggerAPI();
        ThisApp.get().bloggerAPI.getCategories(AppConfig.general.blogger_url, new RequestListener<List<String>>() {
            @Override
            public void onSuccess(List<String> resp) {
                super.onSuccess(resp);
                ThisApp.get().setCategories(resp);
                startActivityMain();
            }

            @Override
            public void onFailed(String messages) {
                super.onFailed(messages);
                if (messages != null) Log.e("Error", messages);
                dialogFailedCategories(getString(R.string.message_failed_server));
            }
        });
    }

    private void startActivityMain() {
        if (alertDialog != null && alertDialog.isShowing()) alertDialog.dismiss();
        Intent i = new Intent(ActivitySplash.this, ActivityMain.class);
        startActivity(i);
        finish();
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
}