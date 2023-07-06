package com.blogger.waller.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blogger.waller.AppConfig;
import com.blogger.waller.model.Wallpaper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Downloader {

    private DownloadListener downloadListener = null;
    private Context context;
    private String file_name;

    public Downloader(Context context) {
        this.context = context;
    }

    public void setDownloadListener(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    public void startDownload(Context context, Wallpaper w) {
        file_name = getFileName(w.getFilename());
        Glide.with(context).asBitmap().load(w.image).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                ResultObj result = new ResultObj(false, "", null, null);
                try {
                    result = saveImageReturn(context, bitmap, file_name);
                } catch (Exception e) {
                    e.printStackTrace();
                    result.msg = e.getMessage();
                }
                // Add the image to the system gallery
                if (result.uri != null) galleryAddPic(result.uri);
                if (downloadListener == null) return;
                downloadListener.onFinish(result);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                if (downloadListener == null) return;
                downloadListener.onFinish(new ResultObj(false, "On Load Failed", null, null));
            }
        });
    }

    public void startDownload(Wallpaper w, final DownloadListener listener) {
        file_name = getFileName(w.getFilename());
        Glide.with(context).asBitmap().load(w.image).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                ResultObj result = new ResultObj(false, "", null, null);
                try {
                    result = saveImageReturn(context, bitmap, file_name);
                } catch (Exception e) {
                    e.printStackTrace();
                    result.msg = e.getMessage();
                }
                listener.onFinish(result);
                // Add the image to the system gallery
                if (result.uri != null) galleryAddPic(result.uri);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                listener.onFinish(new ResultObj(false, "On Load Failed", null, null));
            }
        });
    }

    /**
     * Source : https://stackoverflow.com/questions/63776744/save-bitmap-image-to-specific-location-of-gallery-android-10
     */
    private ResultObj saveImageReturn(Context context, Bitmap bitmap, String name) throws IOException {
        boolean saved;
        OutputStream imageOutStream;

        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + AppConfig.general.download_directory);
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            imageOutStream = resolver.openOutputStream(uri);
        } else {
            File imageFile = new File(getDirectory(context), name);
            imageOutStream = new FileOutputStream(imageFile);
            uri = Uri.fromFile(imageFile);
        }

        saved = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream);
        imageOutStream.flush();
        imageOutStream.close();
        return new ResultObj(saved, "", uri, name);

    }

    private void galleryAddPic(Uri uri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(uri);
        context.sendBroadcast(mediaScanIntent);
    }


    public static boolean isFileExist(Context context, String id) {
        File imageFile = new File(getDirectory(context), getFileName(id));
        return imageFile.exists();
    }

    public static File getFile(Context context, String id) {
        return new File(getDirectory(context), getFileName(id));
    }

    public static File getFilePlain(Context context, String name) {
        return new File(getDirectory(context), name);
    }

    public interface DownloadListener {
        void onFinish(ResultObj result);
    }

    private static String getFileName(String id) {
        return AppConfig.general.prefix_filename + id + ".jpg";
    }

    public static File getDirectory(Context context) {
        String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + AppConfig.general.download_directory;
        File storageDir = new File(imagesDir);
        if (!storageDir.exists()) storageDir.mkdirs();
        if (!storageDir.exists()) {
            storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        }
        return storageDir;
    }

    public class ResultObj {

        public boolean success;
        public Uri uri;
        public String msg;
        public String filename;

        public ResultObj(boolean success, String msg, Uri uri, String filename) {
            this.success = success;
            this.uri = uri;
            this.msg = msg;
            this.filename = filename;
        }
    }
}
