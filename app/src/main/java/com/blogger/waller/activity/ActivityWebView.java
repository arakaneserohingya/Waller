package com.blogger.waller.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.blogger.waller.R;
import com.blogger.waller.databinding.ActivityWebViewBinding;
import com.blogger.waller.utils.Tools;

public class ActivityWebView extends AppCompatActivity {

    private static final String EXTRA_OBJC = "key.EXTRA_OBJC";

    public static void navigate(Activity activity, String url) {
        Intent intent = new Intent(activity, ActivityWebView.class);
        intent.putExtra(EXTRA_OBJC, url);
        activity.startActivity(intent);
    }

    private ActivityWebViewBinding binding;

    private ActionBar actionBar;
    private String url;
    private MenuItem menuBack, menuForward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWebViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // get extra object
        url = getIntent().getStringExtra(EXTRA_OBJC);

        initComponent();
        initToolbar();
        loadWebFromUrl();

        Tools.RTLMode(getWindow());

    }

    private void initComponent() {
        binding.progressBar.getProgressDrawable().setColorFilter(Color.parseColor("#37474F"), PorterDuff.Mode.SRC_IN);
        binding.progressBar.setBackgroundColor(Color.parseColor("#1A000000"));
    }

    private void initToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_close);
        binding.toolbar.setBackgroundColor(getResources().getColor(R.color.white));
        binding.toolbar.getNavigationIcon().setColorFilter(Color.parseColor("#37474F"), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(null);
        Tools.changeOverflowMenuIconColor(binding.toolbar, Color.parseColor("#37474F"));
    }

    private void loadWebFromUrl() {
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setBuiltInZoomControls(true);
        binding.webView.getSettings().setDisplayZoomControls(false);
        binding.webView.getSettings().setDefaultTextEncodingName("utf-8");
        binding.webView.setBackgroundColor(Color.TRANSPARENT);
        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                actionBar.setTitle(null);
                actionBar.setSubtitle(Tools.getHostName(url));
                binding.progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                actionBar.setTitle(view.getTitle());
                binding.progressBar.setVisibility(View.INVISIBLE);
                refreshMenu();
            }
        });
        binding.webView.loadUrl(url);
        binding.webView.setWebChromeClient(new MyChromeClient());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_refresh) {
            loadWebFromUrl();
        } else if (item.getItemId() == R.id.action_browser) {
            if (URLUtil.isValidUrl(url)) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        } else if (item.getItemId() == R.id.action_copy_link) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("clipboard", binding.webView.getUrl());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, R.string.url_copied_clipboard, Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.action_back) {
            onBackPressed();
        } else if (item.getItemId() == R.id.action_forward) {
            if (binding.webView.canGoForward()) binding.webView.goForward();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_web_view, menu);
        Tools.changeMenuIconColor(menu, Color.parseColor("#37474F"));
        menuBack = menu.findItem(R.id.action_back);
        menuForward = menu.findItem(R.id.action_forward);
        refreshMenu();
        return true;
    }

    private void refreshMenu() {
        if (menuBack != null) {
            menuBack.setEnabled(binding.webView.canGoBack());
        }
        if (menuForward != null) {
            menuForward.setEnabled(binding.webView.canGoForward());
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private class MyChromeClient extends WebChromeClient {

        private View mCustomView;
        private CustomViewCallback mCustomViewCallback;
        protected FrameLayout mFullscreenContainer;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        MyChromeClient() {
        }

        @Override
        public void onProgressChanged(WebView view, int progress) {
            super.onProgressChanged(view, progress);
            binding.progressBar.setProgress(progress + 10);
            if (progress >= 100) actionBar.setTitle(view.getTitle());
        }

        public Bitmap getDefaultVideoPoster() {
            if (mCustomView == null) {
                return null;
            }
            return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
        }

        public void onHideCustomView() {
            ((FrameLayout) getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
            binding.appbarLayout.setVisibility(View.VISIBLE);
        }

        public void onShowCustomView(View paramView, CustomViewCallback paramCustomViewCallback) {
            if (this.mCustomView != null) {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mCustomView.setBackgroundColor(Color.BLACK);
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout) getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            getWindow().getDecorView().setSystemUiVisibility(3846);
            binding.appbarLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        binding.webView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        binding.webView.onPause();
        super.onPause();
    }
}