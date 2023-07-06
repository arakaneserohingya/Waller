package com.blogger.waller.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.blogger.waller.R;
import com.blogger.waller.data.SharedPref;

import java.util.ArrayList;
import java.util.List;

public abstract class PermissionUtil {

    public static final String STORAGE = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ? Manifest.permission.WRITE_EXTERNAL_STORAGE : Manifest.permission.ACCESS_MEDIA_LOCATION;

    /* Permission required for application */
    public static final String[] PERMISSION_ALL = {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ? Manifest.permission.WRITE_EXTERNAL_STORAGE : Manifest.permission.ACCESS_MEDIA_LOCATION
            // add more permission here
    };

    /* Permission required for application */
    public static final String[] PERMISSION_STORAGE = {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ? Manifest.permission.WRITE_EXTERNAL_STORAGE : Manifest.permission.ACCESS_MEDIA_LOCATION
    };

    public static void showDialog(final AppCompatActivity act) {
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setTitle(act.getString(R.string.dialog_title_permission));
        builder.setMessage(act.getString(R.string.dialog_content_permission));
        builder.setPositiveButton("SETTINGS", (dialogInterface, i) -> goToPermissionSettingScreen(act));
        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }

    public static void goToPermissionSettingScreen(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity.getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static boolean isAllPermissionGranted(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permission = PERMISSION_ALL;
            if (permission.length == 0) return false;
            for (String s : permission) {
                if (ActivityCompat.checkSelfPermission(activity, s) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String[] getDeniedPermission(Activity act) {
        List<String> permissions = new ArrayList<>();
        for (int i = 0; i < PERMISSION_ALL.length; i++) {
            int status = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                status = act.checkSelfPermission(PERMISSION_ALL[i]);
            }
            if (status != PackageManager.PERMISSION_GRANTED) {
                permissions.add(PERMISSION_ALL[i]);
            }
        }

        return permissions.toArray(new String[permissions.size()]);
    }


    public static boolean isGranted(Activity act, String permission) {
        if (!Tools.needRequestPermission()) return true;
        return (act.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean isStorageGranted(Activity act) {
        return isGranted(act, STORAGE);
    }

    public static void resetRequestNotification(Activity act) {
        SharedPref sharedPref = new SharedPref(act);
        sharedPref.setNeverAskAgain(Manifest.permission.POST_NOTIFICATIONS, false);
        checkAndRequestNotification(act);
    }

    public static void checkAndRequestNotification(Activity act) {
        SharedPref sharedPref = new SharedPref(act);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(act, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED && !sharedPref.getNeverAskAgain(Manifest.permission.POST_NOTIFICATIONS)) {
                String[] permissions = {Manifest.permission.POST_NOTIFICATIONS};
                act.requestPermissions(permissions, 200);
                sharedPref.setNeverAskAgain(Manifest.permission.POST_NOTIFICATIONS, true);
            }
        }
    }

    public static void showSystemDialogPermission(Fragment fragment, String perm) {
        fragment.requestPermissions(new String[]{perm}, 200);
    }

    public static void showSystemDialogPermission(Activity act, String perm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            act.requestPermissions(new String[]{perm}, 200);
        }
    }

    public static void showSystemDialogPermission(Activity act, String perm, int code) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            act.requestPermissions(new String[]{perm}, code);
        }
    }
}
