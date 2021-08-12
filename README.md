## Introduction
CustomWebView is an extended form of web viewer with more customization and flexibility (For MIT AI2 and its distros)<br>
**Latest Version : 11**<br>
**Required Api : 21** <br>
**Permissions:** `android.permission.WRITE_EXTERNAL_STORAGE,android.permission.ACCESS_DOWNLOAD_MANAGER,android.permission.ACCESS_FINE_LOCATION,android.permission.RECORD_AUDIO, android.permission.MODIFY_AUDIO_SETTINGS, android.permission.CAMERA,android.permission.VIBRATE,android.webkit.resource.VIDEO_CAPTURE,android.webkit.resource.AUDIO_CAPTURE,android.launcher.permission.INSTALL_SHORTCUT`

## Features
- More customization options than normal web viewer
- Long Click event
- New window request event
- Close window request event
- Load local files and content using Html and Js
- Find words and phrases in webview
- Evaluate Js and get result
- Upload files to websites
- Error occurred event
- Get output from console
- Form Resubmission event
- Get content height
- Get SSL Certificate of website
- Get/Set cookies for particular url
- Event for permission(s) request and grant permission(s) manually
- Print web content
- A wide range of tools for working with JavaScript
- Accepts external links/Adds your app in browsers list when you use `BrowserPromptHelper` extension
- Download files using built-in download method
- Scroll Changed event and function to scroll to particular position
- Create and Remove webviews dynamically
- Ad blocker 
- Full screen video feature (OnShowCustomView and OnHideCustomView)
- Pause and Resume webview
- Get internal history
- Create webpage shortcuts
- Download offline pages and load them without internet connection

## FAQ
> Will disabling `DeepLink` property not add my app in browsers list?<br>
Ans:- No, that's entirely a different thing.It specifies whether deep urls (such as `tel:`, `whatsapp:`) should open respective apps or not.

> What is BrowserPromptHelper extension? <br>
Ans:- It is an helper class/extension which you can use if you want to add your app in browsers list.

> How can I set a screen name as activity to be launched after clicking external link? <br>
Ans:- You have two methods to do that- <br>
i) Change activity name from `BrowserPromptHelper` class manually and compile the sources. <br>
ii) Get extension from here: <a href="https://sunnythedeveloper.xyz/customwebview/">CustomWebView extension generator</a>

### Reference Links
1.WebView Docs (<a href="https://developer.android.com/reference/android/webkit/WebView">Click here</a>)<br>
2.MIT AI2 Community (<a href="https://community.appinventor.mit.edu/t/customwebview-an-extended-form-of-web-viewer/9934/">Click here</a>)<br>
3.Kodular Community (<a href="https://community.kodular.io/t/customwebview-an-extended-form-of-web-viewer/63037">Click here</a>)

## Donators
- <a href="https://community.kodular.io/u/almeidapablo">Pablo A. Rod</a>
- <a href="https://community.appinventor.mit.edu/u/biesys">Carlos Bieberach</a>
