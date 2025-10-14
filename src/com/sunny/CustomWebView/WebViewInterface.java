package com.sunny.CustomWebView;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.google.appinventor.components.runtime.util.AsynchUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;


public class WebViewInterface {

    private final CustomWebView customWebView;

    private final Activity activity;
    private final Context context;


    private String webViewString;

    WebViewInterface(CustomWebView customWebView) {
        this.customWebView = customWebView;
        this.activity = customWebView.getActivity();
        this.context = customWebView.getContext();
        webViewString = "";
    }

    @JavascriptInterface
    public String getWebViewString() {
        return webViewString;
    }

    @JavascriptInterface
    public void setWebViewString(final String newString) {
        webViewString = newString;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                customWebView.WebViewStringChanged(newString);
            }
        });
    }

    public void setWebViewStringFromBlocks(final String newString) {
        webViewString = newString;
    }

    @JavascriptInterface
    public void gotBase64FromBlobData(final String base64String,final String fileName,final String downloadDir) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                final File dFile;
                if (downloadDir.startsWith("~")) {
                    dFile = new File(context.getExternalFilesDir(downloadDir.substring(1)),fileName);
                } else {
                    dFile = new File(Environment.getExternalStoragePublicDirectory(downloadDir),fileName);
                }

                File parent = dFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    boolean ignored = parent.mkdirs();
                }

                try {
                    FileOutputStream fos = new FileOutputStream(dFile);
                    ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decode(base64String.split(",")[1],Base64.DEFAULT));
                    byte[] bytes = new byte[1024];
                    int c;
                    while((c = bis.read(bytes)) != -1){
                        fos.write(bytes,0,c);
                    }
                    bis.close();
                    fos.close();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            customWebView.BlobFileDownloaded(dFile.getPath());
                        }
                    });
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
    }

    public String getBase64StringFromBlobUrl(String blobUrl, String mimeType, String fileName, String downloadDir) {
        return "javascript: var xhr = new XMLHttpRequest();" +
                "xhr.open('GET', '" + blobUrl + "', true);" +
                "xhr.setRequestHeader('Content-type','" + mimeType + ";charset=UTF-8');" +
                "xhr.responseType = 'blob';" +
                "xhr.onload = function(e) {" +
                "    console.log(this.status);" +
                "    if (this.status == 200) {" +
                "        var blobFile = this.response;" +
                "        var reader = new FileReader();" +
                "        reader.readAsDataURL(blobFile);" +
                "        reader.onloadend = function() {" +
                "            var base64data = reader.result;" +
                "            window.AppInventor.gotBase64FromBlobData(base64data,'" + fileName + "','" + downloadDir + "');" +
                "        }" +
                "    }" +
                "};" +
                "xhr.send();";
    }

    @JavascriptInterface
    public void print() {
        android.util.Log.d("WebViewInterface", "Print function called from JavaScript");
        
        try {
            final WebView webView = customWebView.getWebView();
            if (webView == null) {
                android.util.Log.e("WebViewInterface", "ERROR: WebView is null");
                return;
            }
            android.util.Log.d("WebViewInterface", "WebView obtained successfully");
            
            Handler handler = webView.getHandler();
            if (handler == null) {
                android.util.Log.e("WebViewInterface", "ERROR: Handler is null");
                return;
            }
            android.util.Log.d("WebViewInterface", "Handler obtained successfully");
            
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        android.util.Log.d("WebViewInterface", "Starting print process on main thread");
                        
                        PrintManager manager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
                        if (manager == null) {
                            android.util.Log.e("WebViewInterface", "ERROR: PrintManager is null");
                            return;
                        }
                        android.util.Log.d("WebViewInterface", "PrintManager obtained successfully");
                        
                        String jobName = webView.getTitle();
                        if (jobName == null || jobName.isEmpty()) {
                            jobName = "WebView Document";
                            android.util.Log.d("WebViewInterface", "Using default job name: " + jobName);
                        } else {
                            android.util.Log.d("WebViewInterface", "Using page title as job name: " + jobName);
                        }
                        
                        android.util.Log.d("WebViewInterface", "Creating print document adapter...");
                        PrintDocumentAdapter adapter = webView.createPrintDocumentAdapter(jobName);
                        if (adapter == null) {
                            android.util.Log.e("WebViewInterface", "ERROR: PrintDocumentAdapter is null");
                            return;
                        }
                        android.util.Log.d("WebViewInterface", "PrintDocumentAdapter created successfully");
                        
                        android.util.Log.d("WebViewInterface", "Initiating print job...");
                        PrintJob job = manager.print(jobName, adapter, new PrintAttributes.Builder().build());
                        
                        if (job != null) {
                            android.util.Log.d("WebViewInterface", "Print job created successfully: " + job.getInfo().getLabel());
                            android.util.Log.d("WebViewInterface", "Print job state: " + job.getInfo().getState());
                        } else {
                            android.util.Log.e("WebViewInterface", "ERROR: Print job is null");
                        }
                        
                    } catch (Exception e) {
                        android.util.Log.e("WebViewInterface", "ERROR in print thread: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            
            android.util.Log.d("WebViewInterface", "Print handler.post() completed");
            
        } catch (Exception e) {
            android.util.Log.e("WebViewInterface", "ERROR in print function: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
