package com.sunny.CustomWebView;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

/**
 * Loads and manages filter lists from assets and external URLs
 * Supports compressed formats for mobile optimization
 */
public class FilterListLoader {
    
    private static final String TAG = "FilterListLoader";
    private static final int BUFFER_SIZE = 8192;
    private static final int MAX_RULES_PER_LIST = 100000; // Safety limit
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    private final Context context;
    
    public FilterListLoader(Context context) {
        this.context = context;
    }
    
    /**
     * Callback for filter list loading
     */
    public interface LoadCallback {
        void onSuccess(List<String> rules, String source);
        void onError(String error, String source);
        void onProgress(int loaded, String source);
    }
    
    /**
     * Load filter list from asset file
     */
    public void loadFromAsset(final String assetPath, final LoadCallback callback) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = context.getAssets().open(assetPath);
                    List<String> rules = readRulesFromStream(inputStream, assetPath, callback);
                    inputStream.close();
                    
                    if (callback != null) {
                        callback.onSuccess(rules, assetPath);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error loading from asset: " + assetPath, e);
                    if (callback != null) {
                        callback.onError(e.getMessage(), assetPath);
                    }
                }
            }
        });
    }
    
    /**
     * Load filter list from asset file synchronously
     */
    public List<String> loadFromAssetSync(String assetPath) throws IOException {
        InputStream inputStream = context.getAssets().open(assetPath);
        List<String> rules = readRulesFromStream(inputStream, assetPath, null);
        inputStream.close();
        return rules;
    }
    
    /**
     * Load filter list from external URL
     */
    public void loadFromUrl(final String urlString, final LoadCallback callback) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(30000);
                    connection.setRequestProperty("Accept-Encoding", "gzip");
                    
                    int responseCode = connection.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        throw new IOException("HTTP " + responseCode + " " + connection.getResponseMessage());
                    }
                    
                    InputStream inputStream = connection.getInputStream();
                    
                    // Handle gzip compression
                    String contentEncoding = connection.getContentEncoding();
                    if ("gzip".equalsIgnoreCase(contentEncoding)) {
                        inputStream = new GZIPInputStream(inputStream);
                    }
                    
                    List<String> rules = readRulesFromStream(inputStream, urlString, callback);
                    inputStream.close();
                    
                    if (callback != null) {
                        callback.onSuccess(rules, urlString);
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error loading from URL: " + urlString, e);
                    if (callback != null) {
                        callback.onError(e.getMessage(), urlString);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        });
    }
    
    /**
     * Load filter list from file
     */
    public void loadFromFile(final String filePath, final LoadCallback callback) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(filePath);
                    if (!file.exists() || !file.canRead()) {
                        throw new IOException("File not found or not readable: " + filePath);
                    }
                    
                    InputStream inputStream = new FileInputStream(file);
                    
                    // Check if file is gzipped
                    if (filePath.endsWith(".gz")) {
                        inputStream = new GZIPInputStream(inputStream);
                    }
                    
                    List<String> rules = readRulesFromStream(inputStream, filePath, callback);
                    inputStream.close();
                    
                    if (callback != null) {
                        callback.onSuccess(rules, filePath);
                    }
                    
                } catch (IOException e) {
                    Log.e(TAG, "Error loading from file: " + filePath, e);
                    if (callback != null) {
                        callback.onError(e.getMessage(), filePath);
                    }
                }
            }
        });
    }
    
    /**
     * Load multiple filter lists
     */
    public void loadMultiple(final String[] sources, final boolean fromAssets, final LoadCallback callback) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                List<String> allRules = new ArrayList<>();
                int successCount = 0;
                StringBuilder errors = new StringBuilder();
                
                for (String source : sources) {
                    try {
                        List<String> rules;
                        if (fromAssets) {
                            rules = loadFromAssetSync(source);
                        } else {
                            rules = loadFromFileSync(source);
                        }
                        allRules.addAll(rules);
                        successCount++;
                        
                        if (callback != null) {
                            callback.onProgress(successCount, source);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error loading source: " + source, e);
                        errors.append(source).append(": ").append(e.getMessage()).append("\n");
                    }
                }
                
                if (successCount > 0 && callback != null) {
                    callback.onSuccess(allRules, "Multiple sources (" + successCount + "/" + sources.length + ")");
                }
                
                if (errors.length() > 0 && callback != null) {
                    callback.onError(errors.toString(), "Multiple sources");
                }
            }
        });
    }
    
    /**
     * Load from file synchronously
     */
    private List<String> loadFromFileSync(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || !file.canRead()) {
            throw new IOException("File not found or not readable: " + filePath);
        }
        
        InputStream inputStream = new FileInputStream(file);
        
        if (filePath.endsWith(".gz")) {
            inputStream = new GZIPInputStream(inputStream);
        }
        
        List<String> rules = readRulesFromStream(inputStream, filePath, null);
        inputStream.close();
        return rules;
    }
    
    /**
     * Read rules from input stream
     */
    private List<String> readRulesFromStream(InputStream inputStream, String source, LoadCallback callback) throws IOException {
        List<String> rules = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), BUFFER_SIZE);
        
        String line;
        int count = 0;
        int progressCounter = 0;
        
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            
            // Skip empty lines and comments (but not AdBlock comments which are parsed by FilterRule)
            if (line.isEmpty()) {
                continue;
            }
            
            // Basic validation
            if (line.length() > 1000) {
                // Skip suspiciously long lines
                continue;
            }
            
            rules.add(line);
            count++;
            progressCounter++;
            
            // Progress callback every 1000 rules
            if (callback != null && progressCounter >= 1000) {
                callback.onProgress(count, source);
                progressCounter = 0;
            }
            
            // Safety limit
            if (count >= MAX_RULES_PER_LIST) {
                Log.w(TAG, "Reached maximum rules limit for: " + source);
                break;
            }
        }
        
        reader.close();
        Log.i(TAG, "Loaded " + count + " rules from: " + source);
        return rules;
    }
    
    /**
     * Parse rules string (comma or newline separated)
     */
    public static List<String> parseRulesString(String rulesString) {
        List<String> rules = new ArrayList<>();
        
        if (rulesString == null || rulesString.trim().isEmpty()) {
            return rules;
        }
        
        // Check if newline separated (filter list format) or comma separated (old format)
        String[] lines;
        if (rulesString.contains("\n")) {
            lines = rulesString.split("\n");
        } else {
            lines = rulesString.split(",");
        }
        
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                rules.add(line);
            }
        }
        
        return rules;
    }
    
    /**
     * Convert old format (comma-separated hosts) to new format (AdBlock rules)
     */
    public static List<String> convertLegacyHosts(String hostsString) {
        List<String> rules = new ArrayList<>();
        
        if (hostsString == null || hostsString.trim().isEmpty()) {
            return rules;
        }
        
        String[] hosts = hostsString.split(",");
        for (String host : hosts) {
            host = host.trim();
            if (!host.isEmpty()) {
                // Convert to AdBlock domain anchor format: ||example.com^
                rules.add("||" + host + "^");
            }
        }
        
        return rules;
    }
    
    /**
     * Create a minimal default filter list (for testing)
     */
    public static List<String> createDefaultRules() {
        List<String> rules = new ArrayList<>();
        
        // Common ad domains
        rules.add("||doubleclick.net^");
        rules.add("||googlesyndication.com^");
        rules.add("||googleadservices.com^");
        rules.add("||google-analytics.com^");
        rules.add("||googletagmanager.com^");
        rules.add("||facebook.com/tr^");
        rules.add("||amazon-adsystem.com^");
        rules.add("||adnxs.com^");
        rules.add("||adsystem.com^");
        rules.add("||outbrain.com^");
        rules.add("||taboola.com^");
        rules.add("||criteo.com^");
        rules.add("||pubmatic.com^");
        rules.add("||openx.net^");
        rules.add("||scorecardresearch.com^");
        
        // Common ad paths
        rules.add("/ads/*");
        rules.add("/ad/*");
        rules.add("/banner/*");
        rules.add("/tracking/*");
        rules.add("/analytics/*");
        
        // Common ad parameters
        rules.add("?ad=*");
        rules.add("&ad=*");
        rules.add("?ads=*");
        rules.add("&ads=*");
        
        return rules;
    }
    
    /**
     * Validate filter list format
     */
    public static boolean validateFilterList(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        // Check for common filter list markers
        return content.contains("[Adblock") || 
               content.contains("||") || 
               content.contains("##") ||
               content.contains("@@") ||
               content.trim().startsWith("!");
    }
    
    /**
     * Get file size estimate for memory planning
     */
    public static long estimateMemoryUsage(int ruleCount) {
        // Rough estimate: ~100 bytes per rule (including overhead)
        return ruleCount * 100L;
    }
}
