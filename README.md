# CustomWebView Extension

CustomWebView is an extended, highly customizable WebView component for MIT App Inventor and its distributions (Kodular, Niotron, etc.), designed to provide maximum flexibility, native browser capabilities, and modern Android WebKit features.

**Latest Version:** 13 Beta  
**Minimum Android API:** 21 (Android 5.0 Lollipop)  

> [!NOTE]
> **Version 13 Architecture**: CustomWebView is now split into two modular extension packages:
> 1. `com.sunny.CustomWebView.aix` — Core WebView component and lifecycle manager.
> 2. `com.sunny.cwvutils.aix` — Modular companion helpers for content blocking, advanced web features, downloads, and intent handling.

---

## Extension Packages

### 1. CustomWebView (`com.sunny.CustomWebView.aix`)
The primary visible/non-visible component replacing standard WebViewer with advanced events, JavaScript interfaces, multi-tab support, and deep lifecycle controls.

### 2. CWV Utils (`com.sunny.cwvutils.aix`)
A companion bundle containing 4 modular helper components:
- **`AdBlockHelper`**: Fast, lightweight content blocker powered by AdBlock Plus filter syntax. Attaches directly to CustomWebView via `AttachTo`.
- **`AdvancedWebHelper`**: High-level controller for Dark Mode, process-wide Service Workers, and HTTP/HTTPS/SOCKS5 proxy configuration.
- **`DownloadHelper`**: Full-featured download manager using Android's native `DownloadManager` with real-time progress callbacks and cancel support.
- **`BrowserPromptHelper`**: Integrates with `BrowserActivity` to register your app in the Android system browser chooser for external links.

---

## Features

### Core CustomWebView
- **Dynamic Multi-Tab Management**: Create, switch, and cleanly destroy multiple WebView instances on demand without memory leaks.
- **JavaScript & DOM Tools**: Run asynchronous JavaScript with result callbacks (`EvaluateJs`), bind custom JavaScript interfaces, and capture console messages.
- **Window & Tab Events**: Handle `NewWindowRequest`, `CloseWindowRequest`, and popups seamlessly.
- **Full-Screen Media**: Native full-screen HTML5 video support with `OnShowCustomView` and `OnHideCustomView` events.
- **Page & State Navigation**: Forward/backward history traversal, zoom controls, pull-to-refresh, custom user-agent strings, and offline MHTML page saving/loading.
- **Interception & Cookies**: Modular request interception via `RequestInterceptor`, per-URL cookie management, and SSL certificate error handling.
- **Printing**: Built-in document printing integration using Android's `PrintManager`.
- **File Uploads**: HTML5 file input support with camera/gallery selectors.
- **Scroll & Touch**: Custom scroll listeners, programmatic scrolling, and long-click detection.

### Companion Helpers (`cwvutils`)

#### `AdBlockHelper`
- High-performance flat filter engine with sub-millisecond rule evaluation.
- Supports cosmetic filtering, domain whitelisting, and rule list parsing.
- Easy integration: Call `AttachTo(CustomWebView)` to activate blocking.

#### `AdvancedWebHelper`
- **Algorithmic Dark Mode**: Enable dark theme across modern Android versions via AndroidX WebKit.
- **Proxy Configuration**: Route WebView traffic through HTTP, HTTPS, or SOCKS proxies with customizable bypass lists.
- **Service Worker Controls**: Configure process-wide service worker cache mode, local file access, and network blocking.

#### `DownloadHelper`
- Native `DownloadManager` support with customized download paths and notification visibility.
- Events for `DownloadStarted`, `DownloadProgressChanged`, `DownloadCompleted`, and `DownloadFailed`.
- File utility helpers (`GuessFileName`, `GetMimeType`, `GetFileSize`, `OpenFile`).

#### `BrowserPromptHelper`
- Easily handle incoming browser view intents.
- Route external links directly to your application screens.

---

## Permissions

You need to ask for permissions only when the corresponding features are used in your application:

| Category | Permissions | Purpose |
|---|---|---|
| **Storage & Downloads** | `android.permission.WRITE_EXTERNAL_STORAGE`<br>`android.permission.ACCESS_DOWNLOAD_MANAGER` | Downloading files to public directories (`DownloadHelper`). |
| **Media & Audio Capture** | `android.permission.CAMERA`<br>`android.permission.RECORD_AUDIO`<br>`android.permission.MODIFY_AUDIO_SETTINGS`<br>`android.webkit.resource.VIDEO_CAPTURE`<br>`android.webkit.resource.AUDIO_CAPTURE` | WebRTC audio/video calls, camera inputs, and microphone access. |
| **Location** | `android.permission.ACCESS_FINE_LOCATION` | HTML5 Geolocation API requests. |
| **Shortcuts & Feedback** | `android.launcher.permission.INSTALL_SHORTCUT`<br>`android.permission.VIBRATE` | Adding website shortcuts to the home screen and haptic feedback. |

---

## Building from Source

This project builds using Apache Ant:

```bash
# Clean previous outputs
ant clean

# Compile and package both extensions
ant
```
---

## Frequently Asked Questions (FAQ)

> **Do I need to import both AIX files?**  
> `CustomWebView.aix` can be used standalone if you only need the browser engine. If you need Ad Blocking, Dark Mode, Proxy override, Download progress, or browser intent filters, import `cwvutils.aix` alongside it.

> **How do I enable Ad Blocking in v13?**  
> 1. Add both `CustomWebView` and `AdBlockHelper` to your screen.  
> 2. On screen initialize, call `AdBlockHelper.AttachTo(CustomWebView)`.

> **Does the Proxy setting apply to one WebView or all WebViews?**  
> `AdvancedWebHelper.SetProxy` operates at the Android process level and routes network requests for all WebViews in the app.

> **Will disabling `DeepLink` property prevent my app from appearing in the browsers list?**  
> No. `DeepLink` controls whether clicked deep links (like `tel:`, `whatsapp:`, `mailto:`) open external apps. To add your app to the system browser list, use `BrowserPromptHelper`.

> **How can I set a screen name as the activity launched from external links?**  
> Simply call the `BrowserPromptHelper.RegisterScreen(screenName, startValue)` block. It dynamically saves the target screen name and start value, so external links automatically route to your desired screen without editing Java source code or recompiling.

---

## Reference Links
- [Android WebView Documentation](https://developer.android.com/reference/android/webkit/WebView)
- [MIT App Inventor Community Thread](https://community.appinventor.mit.edu/t/customwebview-an-extended-form-of-web-viewer/9934/)
- [Kodular Community Thread](https://community.kodular.io/t/customwebview-an-extended-form-of-web-viewer/63037)

---

## Sponsors
Special thanks to our generous community sponsors:
- [Pablo A. Rod](https://community.kodular.io/u/almeidapablo)
- [Carlos Bieberach](https://community.appinventor.mit.edu/u/biesys)
- [Yash Kumar](https://community.kodular.io/u/yash_kumar1/activity)
- [Imre Erdelyi](https://community.appinventor.mit.edu/u/3dmixer/summary)