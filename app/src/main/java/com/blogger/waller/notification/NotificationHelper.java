package com.blogger.waller.notification;

import android.content.Context;
import android.content.Intent;

import com.blogger.waller.AppConfig;
import com.blogger.waller.BuildConfig;
import com.blogger.waller.R;
import com.blogger.waller.activity.ActivityDialogNotification;
import com.blogger.waller.activity.ActivitySplash;
import com.blogger.waller.room.AppDatabase;
import com.blogger.waller.room.DAO;
import com.blogger.waller.room.table.NotificationEntity;
import com.onesignal.OSNotification;
import com.onesignal.OneSignal;

public class NotificationHelper {

    public static void init(Context context) {
        oneSignalInit(context);
    }

    public static void oneSignalInit(Context context) {
        if (BuildConfig.DEBUG) {
            OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        }

        // init one signal with client data
        OneSignal.initWithContext(context);
        OneSignal.setAppId(AppConfig.notification.notif_one_signal_appid);
        OneSignal.sendTag("APP", context.getResources().getString(R.string.app_name));

        // handle when use click notification
        onOneSignalOpenNotification(context);

    }

    public static void onOneSignalOpenNotification(Context context) {
        DAO dao = AppDatabase.getDb(context).get();
        OneSignal.setNotificationOpenedHandler(result -> {
            OSNotification notification = result.getNotification();
            NotificationEntity notificationEntity = dao.getNotification(notification.getSentTime());
            Intent intent = new Intent(context, ActivitySplash.class);
            if (notificationEntity != null) {
                intent = ActivityDialogNotification.navigateBase(context, notificationEntity, true);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        });

    }


}