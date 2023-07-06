package com.blogger.waller.notification;

import android.content.Context;

import com.blogger.waller.R;
import com.blogger.waller.room.AppDatabase;
import com.blogger.waller.room.DAO;
import com.blogger.waller.room.table.NotificationEntity;
import com.onesignal.OSMutableNotification;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationReceivedEvent;
import com.onesignal.OneSignal;

@SuppressWarnings("unused")
public class OneSignalServiceExtension implements OneSignal.OSRemoteNotificationReceivedHandler {

    @Override
    public void remoteNotificationReceived(Context context, OSNotificationReceivedEvent notificationReceivedEvent) {
        OneSignal.onesignalLog(OneSignal.LOG_LEVEL.VERBOSE, "OSRemoteNotificationReceivedHandler fired!" +
                " with OSNotificationReceived: " + notificationReceivedEvent.toString());

        OSNotification notification = notificationReceivedEvent.getNotification();
        saveToDatabase(context, notification);

        if (notification.getActionButtons() != null) {
            for (OSNotification.ActionButton button : notification.getActionButtons()) {
                OneSignal.onesignalLog(OneSignal.LOG_LEVEL.VERBOSE, "ActionButton: " + button.toString());
            }
        }

        OSMutableNotification mutableNotification = notification.mutableCopy();
        mutableNotification.setExtender(builder -> builder.setColor(context.getResources().getColor(R.color.primary)));

        // If complete isn't call within a time period of 25 seconds, OneSignal internal logic will show the original notification
        notificationReceivedEvent.complete(mutableNotification);
    }

    private static void saveToDatabase(Context context, OSNotification notification) {
        DAO dao = AppDatabase.getDb(context).get();
        NotificationEntity notificationEntity = dao.getNotification(notification.getSentTime());
        if (notificationEntity == null) notificationEntity = new NotificationEntity();
        notificationEntity.id = notification.getSentTime();
        notificationEntity.title = notification.getTitle();
        notificationEntity.content = notification.getBody();
        notificationEntity.image = notification.getBigPicture();
        notificationEntity.link = notification.getLaunchURL();
        notificationEntity.created_at = System.currentTimeMillis();
        dao.insertNotification(notificationEntity);
    }

}