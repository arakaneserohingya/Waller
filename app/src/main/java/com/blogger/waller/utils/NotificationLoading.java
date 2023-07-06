package com.blogger.waller.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.blogger.waller.BuildConfig;
import com.blogger.waller.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

public class NotificationLoading {

    private Context ctx;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder builder;
    private int id;

    public NotificationLoading(Context context, int id) {
        this.ctx = context;
        this.id = id;
        String channelId = ctx.getResources().getString(R.string.notification_channel_server);
        mNotifyManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(ctx, channelId);
        builder.setSmallIcon(R.drawable.logo).setColor(ctx.getResources().getColor(R.color.primary));
        builder.setDefaults(Notification.DEFAULT_LIGHTS).setDefaults(Notification.DEFAULT_ALL);
        builder.setAutoCancel(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setPriority(Notification.PRIORITY_HIGH);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_LOW);
            mNotifyManager.createNotificationChannel(channel);
        }
    }

    public void start(String title) {
        builder.setContentTitle(title);
        builder.setProgress(0, 0, true);
        mNotifyManager.notify(id, builder.build());
    }

    public void stop(String title, Downloader.ResultObj resultObj) {
        builder.setContentText(title);
        builder.setProgress(0, 0, false);
        if (resultObj.success) {
            // set notification preview
            try {
                Bitmap bitmap = getBitmapFromUri(resultObj.uri);
                if (bitmap != null) {
                    builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));
                }
            } catch (Exception e) {

            }

            // set notification on click
            PendingIntent pendingIntent;
            File file = Downloader.getFilePlain(ctx.getApplicationContext(), resultObj.filename);
            Uri uri = FileProvider.getUriForFile(ctx, BuildConfig.APPLICATION_ID + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivity(ctx.getApplicationContext(), (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_IMMUTABLE);
            } else {
                pendingIntent = PendingIntent.getActivity(ctx.getApplicationContext(), (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
            }

            builder.setContentIntent(pendingIntent);
        }
        mNotifyManager.notify(id, builder.build());
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = ctx.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
}
