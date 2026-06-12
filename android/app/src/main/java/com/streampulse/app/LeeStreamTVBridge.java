package com.streampulse.app;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class LeeStreamTVBridge {
    private Context context;

    public LeeStreamTVBridge(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public boolean isAvailable() {
        return true;
    }

    @JavascriptInterface
    public void playInVLC(String url, String title) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), "video/*");
            intent.setPackage("org.videolan.vlc");
            intent.putExtra("title", title);
            intent.putExtra("headers", new String[]{
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
            });
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "VLC Player is not installed. Opening with default chooser.", Toast.LENGTH_LONG).show();
            playInDefaultPlayer(url, title);
        }
    }

    @JavascriptInterface
    public void playInMXPlayer(String url, String title) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), "video/*");
            intent.setPackage("com.mxtech.videoplayer.ad");
            intent.putExtra("title", title);
            intent.putExtra("headers", new String[]{
                "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
            });
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            try {
                Intent intentPro = new Intent(Intent.ACTION_VIEW);
                intentPro.setDataAndType(Uri.parse(url), "video/*");
                intentPro.setPackage("com.mxtech.videoplayer.pro");
                intentPro.putExtra("title", title);
                intentPro.putExtra("headers", new String[]{
                    "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
                });
                intentPro.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentPro);
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(context, "MX Player is not installed. Opening with default chooser.", Toast.LENGTH_LONG).show();
                playInDefaultPlayer(url, title);
            }
        }
    }

    @JavascriptInterface
    public void playInJustPlayer(String url, String title) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), "video/*");
            intent.setPackage("com.brouken.player");
            intent.putExtra("title", title);
            Bundle bundle = new Bundle();
            bundle.putString("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            intent.putExtra("extra_headers", bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Just Player is not installed. Opening with default chooser.", Toast.LENGTH_LONG).show();
            playInDefaultPlayer(url, title);
        }
    }

    @JavascriptInterface
    public void playInDefaultPlayer(String url, String title) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), "video/*");
            intent.putExtra("title", title);
            
            Bundle bundle = new Bundle();
            bundle.putString("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            intent.putExtra("extra_headers", bundle);
            
            Intent chooser = Intent.createChooser(intent, "Play with");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(context, "No media player available to handle this stream.", Toast.LENGTH_LONG).show();
        }
    }

    @JavascriptInterface
    public void openYoutubeTrailer(String videoId) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videoId));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    @JavascriptInterface
    public void downloadAndInstallAPK(String apkUrl) {
        try {
            Toast.makeText(context, "Downloading update...", Toast.LENGTH_SHORT).show();
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
            request.setDescription("Updating StreamPulse Application");
            request.setTitle("StreamPulse Update");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "streampulse-update.apk");

            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                manager.enqueue(request);
            }
        } catch (Exception e) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
