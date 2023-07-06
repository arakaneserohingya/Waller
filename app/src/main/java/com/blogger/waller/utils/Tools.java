package com.blogger.waller.utils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import com.blogger.waller.AppConfig;
import com.blogger.waller.BuildConfig;
import com.blogger.waller.R;
import com.blogger.waller.activity.ActivitySplash;
import com.blogger.waller.activity.ActivityWebView;
import com.blogger.waller.data.ThisApp;
import com.blogger.waller.model.Wallpaper;
import com.blogger.waller.room.table.NotificationEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dreamspace.blogger.sdk.model.Listing;

public class Tools {

    public static boolean needRequestPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public static void RTLMode(Window window) {
        if (AppConfig.RTL_LAYOUT) {
            window.getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    public static void darkNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.getWindow().setNavigationBarColor(Color.BLACK);
            activity.getWindow().setStatusBarColor(Color.BLACK);
            activity.getWindow().getDecorView().setSystemUiVisibility(0);
        }
    }

    public static void restartApp(Activity activity) {
        activity.finishAffinity();
        activity.startActivity(new Intent(activity, ActivitySplash.class));
    }

    public static Wallpaper parseListingToWallpaper(Listing listing) {
        Document htmlData = Jsoup.parse(listing.content);

        Wallpaper p = new Wallpaper();
        p.id = listing.id;
        p.type = listing.type;
        p.title = listing.title;
        p.published = listing.published;
        p.updated = listing.updated;
        p.content = listing.content;
        p.link = listing.link;
        p.images = new ArrayList<>();
        if (!TextUtils.isEmpty(p.content)) {
            p.images = Tools.findImagesFromContent(p.content);
            p.multiple = p.images.size() > 1;
        }

        Element e = htmlData.select("img").first();
        if (e != null && e.hasAttr("src")) {
            p.image = e.attr("src");
        } else {
            p.image = null;
        }

        p.category = listing.category;
        return p;
    }

    public static void fullScreen(Activity activity) {
        Window w = activity.getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static void displayImage(Context ctx, ImageView img, String url) {
        try {
            Glide.with(ctx.getApplicationContext()).load(url)
                    .transition(withCrossFade())
                    .diskCacheStrategy(ThisApp.pref().getImageCache() ? DiskCacheStrategy.ALL : DiskCacheStrategy.NONE)
                    .into(img);
        } catch (Exception e) {
            Log.e("tag", "error img => " + e.getMessage());
        }
    }

    public static void displayImageThumb(Context ctx, ImageView img, String url, float thumb) {
        try {
            Glide.with(ctx.getApplicationContext()).load(url)
                    .transition(withCrossFade())
                    .diskCacheStrategy(ThisApp.pref().getImageCache() ? DiskCacheStrategy.ALL : DiskCacheStrategy.NONE)
                    .thumbnail(thumb)
                    .into(img);
        } catch (Exception e) {
        }
    }

    public static void changeMenuIconColor(Menu menu, @ColorInt int color) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable == null) continue;
            drawable.mutate();
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static void changeOverflowMenuIconColor(Toolbar toolbar, @ColorInt int color) {
        try {
            Drawable drawable = toolbar.getOverflowIcon();
            drawable.mutate();
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        } catch (Exception e) {
        }
    }

    public static boolean isConnect(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                if (activeNetworkInfo.isConnected() || activeNetworkInfo.isConnectedOrConnecting()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static int getStatusBarHeight(Context context) {
        int height;
        Resources myResources = context.getResources();
        int idStatusBarHeight = myResources.getIdentifier("status_bar_height", "dimen", "android");
        if (idStatusBarHeight > 0) {
            height = context.getResources().getDimensionPixelSize(idStatusBarHeight);
        } else {
            height = 0;
        }
        return height;
    }

    public static String getFormattedDateSimple(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("dd MMM yy hh:mm");
        return newFormat.format(new Date(dateTime));
    }

    public static void rateAction(Activity activity) {
        String general_market_android = AppConfig.general.non_playstore_market_android;
        if (TextUtils.isEmpty(general_market_android)) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
        } else {
            Tools.directLinkCustomTab(activity, general_market_android);
        }
    }

    public static void checkNotification(Activity activity) {
        NotificationEntity notification = ThisApp.get().getNotification();
        if (notification == null) return;

        if (!TextUtils.isEmpty(notification.link)) {
            Tools.directLinkCustomTab(activity, notification.link);
        }

        ThisApp.get().setNotification(null);
    }

    public static String getHostName(String url) {
        try {
            URI uri = new URI(url);
            String new_url = uri.getHost();
            if (!new_url.startsWith("www.")) new_url = "www." + new_url;
            return new_url;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return url;
        }
    }

    public static int dpToPx(Context c, int dp) {
        Resources r = c.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static int getGridSpanCount(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        float screenWidth = displayMetrics.widthPixels;
        float cellWidth = activity.getResources().getDimension(R.dimen.listing_width);
        return Math.round(screenWidth / cellWidth);
    }

    public static void directLinkCustomTab(Activity activity, String url) {
        if (!URLUtil.isValidUrl(url)) {
            Toast.makeText(activity, "Ops, Cannot open url", Toast.LENGTH_LONG).show();
            return;
        }
        if (!AppConfig.general.open_link_in_app) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            activity.startActivity(browserIntent);
            return;
        }
        int color = ResourcesCompat.getColor(activity.getResources(), R.color.primary, null);
        int secondaryColor = ResourcesCompat.getColor(activity.getResources(), R.color.accent, null);

        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
        CustomTabColorSchemeParams defaultColors = new CustomTabColorSchemeParams.Builder()
                .setToolbarColor(color)
                .setSecondaryToolbarColor(secondaryColor)
                .build();
        intentBuilder.setDefaultColorSchemeParams(defaultColors);
        intentBuilder.setShowTitle(true);
        intentBuilder.setUrlBarHidingEnabled(true);

        CustomTabsHelper.openCustomTab(activity, intentBuilder.build(), Uri.parse(url), (activity1, uri) -> {
            ActivityWebView.navigate(activity1, uri.toString());
        });
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static void goToNotificationSettings(Activity activity) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID);
        } else {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", BuildConfig.APPLICATION_ID);
            intent.putExtra("app_uid", activity.getApplicationInfo().uid);
        }
        activity.startActivity(intent);
    }

    public static void aboutAction(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.dialog_about_title));
        builder.setMessage(Html.fromHtml(activity.getString(R.string.about_text)));
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    public static void applyTheme(boolean dark) {
        if (dark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static List<String> findImagesFromContent(String content) {
        if (TextUtils.isEmpty(content)) return new ArrayList<>();
        Document htmlData = Jsoup.parse(content);
        List<String> images = new ArrayList<>();
        Elements elements = htmlData.select("img");
        for (Element e : elements) {
            if (e.hasAttr("src")) {
                images.add(e.attr("src"));
            }
        }
        return images;
    }

    public static boolean checkMultipleImages(String content) {
        if (TextUtils.isEmpty(content)) return false;
        Document htmlData = Jsoup.parse(content);
        Elements elements = htmlData.select("img");
        return elements.size() > 1;
    }

    private static AppUpdateManager appUpdateManager;
    private static InstallStateUpdatedListener installStateUpdatedListener;

    public static void checkGooglePlayUpdateStopListener() {
        if (appUpdateManager != null)
            appUpdateManager.unregisterListener(installStateUpdatedListener);
    }

    public static void checkGooglePlayUpdate(Activity activity) {
        if (BuildConfig.DEBUG) return;
        installStateUpdatedListener = state -> {
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), R.string.update_ready, Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.install, view -> {
                    if (appUpdateManager != null) appUpdateManager.completeUpdate();
                });
                snackbar.show();
            } else if (state.installStatus() == InstallStatus.INSTALLED) {
                if (appUpdateManager != null && installStateUpdatedListener != null) {
                    appUpdateManager.unregisterListener(installStateUpdatedListener);
                }
            } else {

            }
        };

        appUpdateManager = AppUpdateManagerFactory.create(activity);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateManager.registerListener(installStateUpdatedListener);
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, activity, 200);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void showToastCenter(Context ctx, @StringRes int res_id) {
        Toast toast = Toast.makeText(ctx, ctx.getResources().getString(res_id), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void methodShare(AppCompatActivity act, File file) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        share.putExtra(Intent.EXTRA_TITLE, act.getString(R.string.app_name));
        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        Uri uri = FileProvider.getUriForFile(act, BuildConfig.APPLICATION_ID + ".provider", file);
        share.setClipData(ClipData.newUri(act.getContentResolver(), act.getString(R.string.app_name), uri));
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        share.putExtra(Intent.EXTRA_TEXT, act.getString(R.string.app_name) + " : " + "http://play.google.com/store/apps/details?id=" + act.getPackageName());
        act.startActivity(Intent.createChooser(share, "Share"));
    }

    public static void displayImageWallpaperDetails(Context ctx, final ImageView img, String url) {
        try {
            RequestBuilder<Bitmap> builder = Glide.with(ctx).asBitmap().load(url);
            builder.into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                    img.setImageBitmap(bitmap);
                    img.setScaleType(ImageView.ScaleType.CENTER_CROP);

                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                }
            });
        } catch (Exception e) {
        }
    }

    public static void clearImageCacheOnBackground(final Context ctx) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Glide.get(ctx).clearDiskCache();
                }
            }).start();
        } catch (Exception e) {
        }
    }

    private static long getDirSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += getDirSize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    public static String getImageCacheSize(Context context) {
        long size = getDirSize(context.getCacheDir());
        if (context.getExternalCacheDir() != null) {
            size = size + getDirSize(context.getExternalCacheDir());
        }
        if (size <= 0) {
            return "0 Bytes";
        }
        String[] units = new String[]{"Bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10((double) size) / Math.log10(1024.0d));
        StringBuilder stringBuilder = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.#");
        double d = (double) size;
        double pow = Math.pow(1024.0d, (double) digitGroups);
        Double.isNaN(d);
        stringBuilder.append(decimalFormat.format(d / pow));
        stringBuilder.append(" ");
        stringBuilder.append(units[digitGroups]);
        return stringBuilder.toString();
    }
}
