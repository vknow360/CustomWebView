package com.sunny.cwvutils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.OnDestroyListener;
import com.google.appinventor.components.runtime.util.AsynchUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

@DesignerComponent(version = 13,
        versionName = "13",
        description ="Helper class of CustomWebView extension for downloading files <br> Developed by Sunny Gupta",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "https://i.ibb.co/4wLNN1Hs/ktvu4bapylsvnykoyhdm-c-fill-w-20-h-20.png",
        helpUrl="https://github.com/vknow360/CustomWebView",
        androidMinSdk = 21)
@SimpleObject(external=true)
public class DownloadHelper extends AndroidNonvisibleComponent implements OnDestroyListener{
    private final Context context;
    private final DownloadManager downloadManager;
    private long lastRequestId;
    private int nVisibility = DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
    private boolean isCancelled = false;
    public BroadcastReceiver completed = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == lastRequestId){
                DownloadCompleted();
            }
        }
    };

    public DownloadHelper(ComponentContainer container){
        super(container.$form());
        context = container.$context();
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            context.registerReceiver(completed, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),Context.RECEIVER_NOT_EXPORTED);
        }else {
            context.registerReceiver(completed, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
    }
    @SimpleProperty(description = "Sets download notification visibility")
    public void NotificationVisibility(int i){
        this.nVisibility = i;
    }
    @SimpleFunction(description = "Returns guessed file name")
    public String GuessFileName(String url, String mimeType, String contentDisposition){
        return URLUtil.guessFileName(url, contentDisposition, mimeType);
    }
    @SimpleFunction()
    public String GetUriString(long id){
        try {
            return downloadManager.getUriForDownloadedFile(id).toString();
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
    @SimpleFunction()
    public String GetMimeType(long id){
        return downloadManager.getMimeTypeForDownloadedFile(id);
    }
    @SimpleFunction(description = "Tries to get file size")
    public void GetFileSize(final String url){
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                final long[] size = new long[1];
                try {
                    HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                    con.setRequestProperty("Accept-Encoding", "identity");
                    int statusCode = con.getResponseCode();
                    size[0] = con.getContentLengthLong();
                } catch (IOException e) {
                    e.printStackTrace();
                    size[0] = -1;
                }
                form.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GotFileSize(size[0]);
                    }
                });
            }
        });
    }
    @SimpleFunction(description = "Downloads the given file")
    public void Download(String url, String mimeType, String fileName, String downloadDir) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setMimeType(mimeType);
        String cookies = CookieManager.getInstance().getCookie(url);
        request.addRequestHeader("cookie", cookies);
        request.setDescription("Downloading file...");
        request.setTitle(fileName);
        request.setNotificationVisibility(nVisibility);
        request.setTitle(fileName);
        if (downloadDir.startsWith("~")) {
            request.setDestinationInExternalFilesDir(context, downloadDir.substring(1), fileName);
        } else {
            request.setDestinationInExternalPublicDir(downloadDir, fileName);
        }
        lastRequestId = downloadManager.enqueue(request);
        DownloadStarted(lastRequestId);
        isCancelled = false;
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                final Timer timer = new Timer();
                final TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        DownloadManager.Query downloadQuery = new DownloadManager.Query();
                        downloadQuery.setFilterById(lastRequestId);
                        Cursor cursor = downloadManager.query(downloadQuery);
                        if (cursor.moveToFirst()){
                            final int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                            if(status != DownloadManager.STATUS_FAILED && !isCancelled) {
                                int downloadedSize = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                                final int totalSize = cursor.getInt(cursor
                                        .getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                                cursor.close();
                                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                    timer.cancel();
                                    form.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            DownloadCompleted();
                                        }
                                    });
                                }
                                final int progress = (int) ((((long) downloadedSize)*100) / ((long) totalSize)) ;
                                form.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DownloadProgressChanged(progress);
                                    }
                                });
                            }else{
                                timer.cancel();
                                isCancelled = true;
                                form.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DownloadFailed();
                                    }
                                });
                            }
                        }
                    }
                };
                timer.schedule(timerTask, 0, 1000);
            }
        });
    }
    @SimpleEvent(description = "Event invoked when downloading starts")
    public void DownloadStarted(long id){
        EventDispatcher.dispatchEvent(this,"DownloadStarted",id);
    }
    @SimpleEvent(description = "Event invoked after getting file size")
    public void GotFileSize(long fileSize){
        EventDispatcher.dispatchEvent(this,"GotFileSize",fileSize);
    }
    @SimpleEvent(description = "Event invoked when downloading gets completed")
    public void DownloadCompleted(){
        EventDispatcher.dispatchEvent(this,"DownloadCompleted");
    }
    @SimpleEvent(description = "Event invoked when downloading gets failed")
    public void DownloadFailed(){
        lastRequestId = 0L;
        EventDispatcher.dispatchEvent(this,"DownloadFailed");
    }
    @SimpleEvent(description = "Event invoked when downloading progress changes")
    public void DownloadProgressChanged(int progress){
        EventDispatcher.dispatchEvent(this,"DownloadProgressChanged",progress);
    }

    @SimpleFunction(description = "Tries to open the downloaded file from id")
    public void OpenFile(int id){
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = downloadManager.getUriForDownloadedFile(id);
            String mimeType = downloadManager.getMimeTypeForDownloadedFile(id);
            intent.setDataAndType(uri,mimeType);
            form.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SimpleFunction(description = "Cancels the current download request")
    public void Cancel(){
        downloadManager.remove(lastRequestId);
        isCancelled = true;
        DownloadFailed();
    }
    @Override
    public void onDestroy() {
        context.unregisterReceiver(completed);
    }

}
