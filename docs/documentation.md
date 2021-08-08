> <h2> Docs for: BrowserPromptHelper</h2> 

> <h2> Events </h2> 

 > <h3>OnResume</h3>Event raised when app gets resumed and gives the url which started this activity/screen if there is any else empty string
Params           |  []()       
---------------- | ------- 

```` url | text````

 ____________________________________

> <h2> Methods </h2> 

 > <h3>GetStartUrl</h3>Returns the url which started the current activity
<i>Return type : text</i>

____________________________________

> <h2> Docs for: CustomWebView</h2> 

> <h2> Events </h2> 

 > <h3>AfterArchiveSaved</h3>Event raised after 'SaveArchive' method.If 'success' is true then returns file path else empty string.
Params           |  []()       
---------------- | ------- 

```` success | boolean````
```` filePath | text````

 ____________________________________

> <h3>AfterJavaScriptEvaluated</h3>Event raised after evaluating Js and returns result.
Params           |  []()       
---------------- | ------- 

```` result | text````

 ____________________________________

> <h3>CookiesRemoved</h3>Event raised after 'ClearCokies' method with result
Params           |  []()       
---------------- | ------- 

```` successful | boolean````

 ____________________________________

> <h3>FileUploadNeeded</h3>Event raised when file uploading is needed
Params           |  []()       
---------------- | ------- 

```` id | number````
```` mimeType | text````
```` isCaptureEnabled | boolean````

 ____________________________________

> <h3>FindResultReceived</h3>Event raised after 'Find' method with int 'activeMatchOrdinal','numberOfMatches' and 'isDoneCounting'
Params           |  []()       
---------------- | ------- 

```` id | number````
```` activeMatchOrdinal | number````
```` numberOfMatches | number````
```` isDoneCounting | boolean````

 ____________________________________

> <h3>GotCertificate</h3>Event raised after getting SSL certificate of current displayed url/website with boolean 'isSecure' and Strings 'issuedBy','issuedTo' and 'validTill'.If 'isSecure' is false and other values are empty then assume that website is not secure
Params           |  []()       
---------------- | ------- 

```` isSecure | boolean````
```` issuedBy | text````
```` issuedTo | text````
```` validTill | text````

 ____________________________________

> <h3>GotPrintResult</h3>Event raised after getting previus print's result.
Params           |  []()       
---------------- | ------- 

```` printId | text````
```` isCompleted | boolean````
```` isFailed | boolean````
```` isBlocked | boolean````

 ____________________________________

> <h3>LongClicked</h3>Event raised when something is long clicked in webview with item(image,string,empty,etc) and type(item type like 0,1,8,etc)
Params           |  []()       
---------------- | ------- 

```` id | number````
```` item | text````
```` secondaryUrl | text````
```` type | number````

 ____________________________________

> <h3>OnCloseWindowRequest</h3>Event triggered when a window needs to be closed
Params           |  []()       
---------------- | ------- 

```` id | number````

 ____________________________________

> <h3>OnConsoleMessage</h3>Event raised after getting console message.
Params           |  []()       
---------------- | ------- 

```` message | text````
```` lineNumber | number````
```` sourceID | number````
```` level | text````

 ____________________________________

> <h3>OnDownloadNeeded</h3>Event raised when downloading is needed.
Params           |  []()       
---------------- | ------- 

```` id | number````
```` url | text````
```` contentDisposition | text````
```` mimeType | text````
```` size | number````

 ____________________________________

> <h3>OnErrorReceived</h3>Event raised when any error is received during loading url and returns message,error code and failing url
Params           |  []()       
---------------- | ------- 

```` id | number````
```` message | text````
```` errorCode | number````
```` url | text````

 ____________________________________

> <h3>OnFormResubmission</h3>Event raised when resubmission of form is needed
Params           |  []()       
---------------- | ------- 

```` id | number````

 ____________________________________

> <h3>OnGeolocationRequested</h3>Event for OnGeolocationRequested
Params           |  []()       
---------------- | ------- 

```` origin | text````

 ____________________________________

> <h3>OnHideCustomView</h3>Event raised when current page exits from full screen mode
 ____________________________________

> <h3>OnJsAlert</h3>Event raised when Js have to show an alert to user
Params           |  []()       
---------------- | ------- 

```` id | number````
```` url | text````
```` message | text````

 ____________________________________

> <h3>OnJsConfirm</h3>Tells to display a confirm dialog to the user.
Params           |  []()       
---------------- | ------- 

```` id | number````
```` url | text````
```` message | text````

 ____________________________________

> <h3>OnJsPrompt</h3>Event raised when JavaScript needs input from user
Params           |  []()       
---------------- | ------- 

```` id | number````
```` url | text````
```` message | text````
```` defaultValue | text````

 ____________________________________

> <h3>OnNewWindowRequest</h3>Event raised when new window is requested by webview with boolean 'isDialog' and 'isPopup'
Params           |  []()       
---------------- | ------- 

```` id | number````
```` isDialog | boolean````
```` isPopup | boolean````

 ____________________________________

> <h3>OnPermissionRequest</h3>Event raised when a website asks for specific permission(s) in list format.
Params           |  []()       
---------------- | ------- 

```` permissionsList | list````

 ____________________________________

> <h3>OnProgressChanged</h3>Event raised when page loading progress has changed.
Params           |  []()       
---------------- | ------- 

```` id | number````
```` progress | number````

 ____________________________________

> <h3>OnReceivedHttpAuthRequest</h3>Notifies that the WebView received an HTTP authentication request.
Params           |  []()       
---------------- | ------- 

```` id | number````
```` host | text````
```` realm | text````

 ____________________________________

> <h3>OnReceivedSslError</h3>Event for OnReceivedSslError
Params           |  []()       
---------------- | ------- 

```` errorCode | number````

 ____________________________________

> <h3>OnScrollChanged</h3>Event raised when webview gets scrolled
Params           |  []()       
---------------- | ------- 

```` id | number````
```` scrollX | number````
```` scrollY | number````
```` oldScrollX | number````
```` oldScrollY | number````
```` canGoLeft | boolean````
```` canGoRight | boolean````

 ____________________________________

> <h3>OnShowCustomView</h3>Event raised when current page enters in full screen mode
 ____________________________________

> <h3>PageLoaded</h3>Event raised when page loading has finished.
Params           |  []()       
---------------- | ------- 

```` id | number````

 ____________________________________

> <h3>PageStarted</h3>Event indicating that page loading has started in web view.
Params           |  []()       
---------------- | ------- 

```` id | number````
```` url | text````

 ____________________________________

> <h3>WebViewStringChanged</h3>When the JavaScript calls AppInventor.setWebViewString this event is run.
Params           |  []()       
---------------- | ------- 

```` value | text````

 ____________________________________

> <h2> Methods </h2> 

 > <h3>AllowGeolocationAccess</h3>Method for AllowGeolocationAccess
Params           |  []()       
---------------- | ------- 

```` allow | boolean````<br>
```` remember | boolean````<br>

____________________________________

> <h3>CanGoBack</h3>Gets whether this WebView has a back history item
<i>Return type : boolean</i>

____________________________________

> <h3>CanGoBackOrForward</h3>Gets whether the page can go back or forward the given number of steps.
Params           |  []()       
---------------- | ------- 

```` steps | number````<br>

<i>Return type : boolean</i>

____________________________________

> <h3>CanGoForward</h3>Gets whether this WebView has a forward history item.
<i>Return type : boolean</i>

____________________________________

> <h3>CancelPrinting</h3>Cancels current print job. You can request cancellation of a queued, started, blocked, or failed print job.
____________________________________

> <h3>ClearCache</h3>Clears the resource cache.
____________________________________

> <h3>ClearCookies</h3>Removes all cookies and raises 'CookiesRemoved' event
____________________________________

> <h3>ClearInternalHistory</h3>Tells this WebView to clear its internal back/forward list.
____________________________________

> <h3>ClearLocation</h3>Clear all location preferences.
____________________________________

> <h3>ClearMatches</h3>Clears the highlighting surrounding text matches.
____________________________________

> <h3>ConfirmJs</h3>Whether to proceed JavaScript originated request
Params           |  []()       
---------------- | ------- 

```` confirm | boolean````<br>

____________________________________

> <h3>ContentHeight</h3>Gets height of HTML content
<i>Return type : number</i>

____________________________________

> <h3>ContinueJs</h3>Inputs a confirmation response to Js
Params           |  []()       
---------------- | ------- 

```` input | text````<br>

____________________________________

> <h3>CreateShortcut</h3>Creates a shortcut of given website on home screen
Params           |  []()       
---------------- | ------- 

```` url | text````<br>
```` iconPath | text````<br>
```` title | text````<br>

____________________________________

> <h3>CreateWebView</h3>Creates the webview in given arrangement with id
Params           |  []()       
---------------- | ------- 

```` container | component````<br>
```` id | number````<br>

____________________________________

> <h3>CurrentId</h3>Returns current id
<i>Return type : number</i>

____________________________________

> <h3>DismissJsAlert</h3>Dismiss previously requested Js alert
____________________________________

> <h3>EvaluateJavaScript</h3>Asynchronously evaluates JavaScript in the context of the currently displayed page.
Params           |  []()       
---------------- | ------- 

```` script | text````<br>

____________________________________

> <h3>Find</h3>Finds all instances of find on the page and highlights them, asynchronously. Successive calls to this will cancel any pending searches.
Params           |  []()       
---------------- | ------- 

```` string | text````<br>

____________________________________

> <h3>FindNext</h3>Highlights and scrolls to the next match if 'forward' is true else scrolls to previous match.
Params           |  []()       
---------------- | ------- 

```` forward | boolean````<br>

____________________________________

> <h3>GetCookies</h3>Get cookies for specific url
Params           |  []()       
---------------- | ------- 

```` url | text````<br>

<i>Return type : text</i>

____________________________________

> <h3>GetIds</h3>Returns a list of used ids
<i>Return type : list</i>

____________________________________

> <h3>GetInternalHistory</h3>Get internal history of given webview.
Params           |  []()       
---------------- | ------- 

```` id | number````<br>

<i>Return type : list</i>

____________________________________

> <h3>GetProgress</h3>Gets the progress for the given webview
Params           |  []()       
---------------- | ------- 

```` id | number````<br>

<i>Return type : number</i>

____________________________________

> <h3>GetScrollX</h3>Return the scrolled left position of the webview
<i>Return type : number</i>

____________________________________

> <h3>GetScrollY</h3>Return the scrolled top position of the webview
<i>Return type : number</i>

____________________________________

> <h3>GetSslCertificate</h3>Gets the SSL certificate for the main top-level page and raises 'GotCertificate' event
____________________________________

> <h3>GetWebView</h3>Returns webview object from id
Params           |  []()       
---------------- | ------- 

```` id | number````<br>

<i>Return type : any</i>

____________________________________

> <h3>GoBack</h3>Goes back in the history of this WebView.
____________________________________

> <h3>GoBackOrForward</h3>Goes to the history item that is the number of steps away from the current item. Steps is negative if backward and positive if forward.
Params           |  []()       
---------------- | ------- 

```` steps | number````<br>

____________________________________

> <h3>GoForward</h3>Goes forward in the history of this WebView.
____________________________________

> <h3>GoToUrl</h3>Loads the given URL.
Params           |  []()       
---------------- | ------- 

```` url | text````<br>

____________________________________

> <h3>GrantPermission</h3>Grants given permissions to webview.Use empty list to deny the request.
Params           |  []()       
---------------- | ------- 

```` permissions | text````<br>

____________________________________

> <h3>HideCustomView</h3>Hides previously shown custom view
____________________________________

> <h3>InvokeZoomPicker</h3>Invokes the graphical zoom picker widget for this WebView. This will result in the zoom widget appearing on the screen to control the zoom level of this WebView.Note that it does not checks whether zoom is enabled or not.
____________________________________

> <h3>LoadHtml</h3>Loads the given data into this WebView using a 'data' scheme URL.
Params           |  []()       
---------------- | ------- 

```` html | text````<br>

____________________________________

> <h3>LoadInNewWindow</h3>Loads requested url in given webview
Params           |  []()       
---------------- | ------- 

```` id | number````<br>

____________________________________

> <h3>LoadWithHeaders</h3>Loads the given URL with the specified additional HTTP headers defined is list of lists.
Params           |  []()       
---------------- | ------- 

```` url | text````<br>
```` headers | list````<br>

____________________________________

> <h3>PageDown</h3>Scrolls the contents of the WebView down by half the page size
Params           |  []()       
---------------- | ------- 

```` bottom | boolean````<br>

____________________________________

> <h3>PageUp</h3>Scrolls the contents of the WebView up by half the page size
Params           |  []()       
---------------- | ------- 

```` top | boolean````<br>

____________________________________

> <h3>PauseWebView</h3>Does a best-effort attempt to pause any processing that can be paused safely, such as animations and geolocation. Note that this call does not pause JavaScript.
Params           |  []()       
---------------- | ------- 

```` id | number````<br>

____________________________________

> <h3>PostData</h3>Loads the URL with postData using 'POST' method into active WebView.
Params           |  []()       
---------------- | ------- 

```` url | text````<br>
```` data | text````<br>

____________________________________

> <h3>PrintWebContent</h3>Prints the content of webview with given document name
Params           |  []()       
---------------- | ------- 

```` documentName | text````<br>

____________________________________

> <h3>ProceedHttpAuthRequest</h3>Instructs the WebView to proceed with the authentication with the given credentials.If both parameters are empty then it will cancel the request.
Params           |  []()       
---------------- | ------- 

```` username | text````<br>
```` password | text````<br>

____________________________________

> <h3>ProceedSslError</h3>Method for ProceedSslError
Params           |  []()       
---------------- | ------- 

```` proceed | boolean````<br>

____________________________________

> <h3>Reload</h3>Reloads the current URL.
____________________________________

> <h3>RemoveWebView</h3>Destroys the webview and removes it completely from view system
Params           |  []()       
---------------- | ------- 

```` id | number````<br>

____________________________________

> <h3>RestartPrinting</h3>Restarts current/previous print job. You can request restart of a failed print job.
____________________________________

> <h3>ResubmitForm</h3>Whether to resubmit form or not.
Params           |  []()       
---------------- | ------- 

```` reSubmit | boolean````<br>

____________________________________

> <h3>ResumeWebView</h3>Resumes the previously paused WebView.
Params           |  []()       
---------------- | ------- 

```` id | number````<br>

____________________________________

> <h3>SaveArchive</h3>Saves the current site as a web archive
Params           |  []()       
---------------- | ------- 

```` dir | text````<br>

____________________________________

> <h3>ScrollTo</h3>Scrolls the webview to given position
Params           |  []()       
---------------- | ------- 

```` x | number````<br>
```` y | number````<br>

____________________________________

> <h3>SetCookies</h3>Sets cookies for given url
Params           |  []()       
---------------- | ------- 

```` url | text````<br>
```` cookieString | text````<br>

____________________________________

> <h3>SetVisibility</h3>Sets the visibility of webview by id
Params           |  []()       
---------------- | ------- 

```` id | number````<br>
```` visibility | boolean````<br>

____________________________________

> <h3>SetWebView</h3>Set specific webview to current webview by id
Params           |  []()       
---------------- | ------- 

```` id | number````<br>

____________________________________

> <h3>StopLoading</h3>Stops the current load.
____________________________________

> <h3>UploadFile</h3>Uploads the given file from content uri.Use empty string to cancel the upload request.
Params           |  []()       
---------------- | ------- 

```` contentUri | text````<br>

____________________________________

> <h3>ZoomBy</h3>Performs a zoom operation in the WebView by given zoom percent
Params           |  []()       
---------------- | ------- 

```` zoomP | number````<br>

____________________________________

> <h3>ZoomIn</h3>Performs zoom in in the WebView
____________________________________

> <h3>ZoomOut</h3>Performs zoom out in the WebView
____________________________________

> <h2> Properties </h2> 

 > <h3>AdHosts</h3>Sets the ad hosts which will be blocked
<i>Property Type : write-only</i><br><i>Accepts : text</i>
____________________________________

> <h3>AutoLoadImages</h3>Sets whether the WebView should load image resources
<i>Property Type : read-write</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>Autofill</h3>Specifies whether webview should autofill saved credentials or not
<i>Property Type : write-only</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>AutoplayMedia</h3>Sets whether the WebView requires a user gesture to play media
<i>Property Type : read-write</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>BackgroundColor</h3>Sets background color of webview
<i>Property Type : write-only</i><br><i>Accepts : number</i>
____________________________________

> <h3>BlockAds</h3>Sets whether to block ads or not
<i>Property Type : write-only</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>BlockNetworkLoads</h3>Sets whether the WebView should not load resources from the network.Use this to save data.
<i>Property Type : read-write</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>CacheMode</h3>Sets cache mode for active webview
<i>Property Type : read-write</i><br><i>Accepts : number</i>
____________________________________

> <h3>CurrentPageTitle</h3>Title of the page currently viewed
<i>Property Type : read-only</i><br><i>Accepts : text</i>
____________________________________

> <h3>CurrentUrl</h3>URL of the page currently viewed
<i>Property Type : read-only</i><br><i>Accepts : text</i>
____________________________________

> <h3>DeepLinks</h3>Sets whether to enable deep links or not i.e. tel: , whatsapp: , sms: , etc.
<i>Property Type : read-write</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>DesktopMode</h3>Sets whether to load content in desktop mode
<i>Property Type : read-write</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>DisplayZoom</h3>Sets whether the WebView should display on-screen zoom controls
<i>Property Type : read-write</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>EnableJS</h3>Tells the WebView to enable JavaScript execution.
<i>Property Type : read-write</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>FileAccess</h3>Sets whether webview can access local files.Use this to enable file uploading and loading files using HTML
<i>Property Type : read-write</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>FollowLinks</h3>Determines whether to follow links when they are tapped in the WebViewer.If you follow links, you can use GoBack and GoForward to navigate the browser history
<i>Property Type : read-write</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>FontSize</h3>Sets the default font size of text. The default is 16.
<i>Property Type : read-write</i><br><i>Accepts : number</i>
____________________________________

> <h3>InitialScale</h3>Sets the initial scale for active WebView. 0 means default. If initial scale is greater than 0, WebView starts with this value as initial scale.
<i>Property Type : write-only</i><br><i>Accepts : number</i>
____________________________________

> <h3>LayerType</h3>Property for LayerType
<i>Property Type : read-write</i><br><i>Accepts : number</i>
____________________________________

> <h3>LoadWithOverviewMode</h3>Sets whether the WebView loads pages in overview mode, that is, zooms out the content to fit on screen by width. This setting is taken into account when the content width is greater than the width of the WebView control.
<i>Property Type : read-write</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>LongClickable</h3>Sets whether to enable text selection and context menu
<i>Property Type : read-write</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>OverScrollMode</h3>Property for OverScrollMode
<i>Property Type : read-write</i><br><i>Accepts : number</i>
____________________________________

> <h3>PromptForPermission</h3>Returns whether webview will prompt for permission and raise 'OnPermissionRequest' event or not
<i>Property Type : read-write</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>RotationAngle</h3>Property for RotationAngle
<i>Property Type : read-write</i><br><i>Accepts : number</i>
____________________________________

> <h3>ScrollBar</h3>Whether to display horizonatal and vertical scrollbars or not
<i>Property Type : write-only</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>ScrollBarStyle</h3>Property for ScrollBarStyle
<i>Property Type : read-write</i><br><i>Accepts : number</i>
____________________________________

> <h3>SupportMultipleWindows</h3>Sets whether the WebView supports multiple windows
<i>Property Type : read-write</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>UseWideViewPort</h3>Sets whether the WebView should enable support for the 'viewport' HTML meta tag or should use a wide viewport.
<i>Property Type : read-write</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>UserAgent</h3>Get webview user agent
<i>Property Type : read-write</i><br><i>Accepts : text</i>
____________________________________

> <h3>UsesLocation</h3>Whether or not to give the application permission to use the Javascript geolocation API
<i>Property Type : write-only</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>Visible</h3>Returns the visibility of current webview
<i>Property Type : read-only</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>WebViewString</h3>Set webview string
<i>Property Type : read-write</i><br><i>Accepts : text</i>
____________________________________

> <h3>ZoomEnabled</h3>Sets whether the WebView should support zooming using its on-screen zoom controls and gestures
<i>Property Type : read-write</i><br><i>Accepts : boolean</i>
____________________________________

> <h3>ZoomPercent</h3>Sets the zoom of the page in percent. The default is 100
<i>Property Type : read-write</i><br><i>Accepts : number</i>
____________________________________

> <h2> Docs for: DownloadHelper</h2> 

> <h2> Events </h2> 

 > <h3>DownloadCompleted</h3>Event invoked when downloading gets completed
 ____________________________________

> <h3>DownloadProgressChanged</h3>Event invoked when downloading progress changes
Params           |  []()       
---------------- | ------- 

```` progress | number````

 ____________________________________

> <h2> Methods </h2> 

 > <h3>Cancel</h3>Cancels the current download request
____________________________________

> <h3>Download</h3>Downloads the given file
Params           |  []()       
---------------- | ------- 

```` url | text````<br>
```` mimeType | text````<br>
```` contentDisposition | text````<br>
```` fileName | text````<br>
```` downloadDir | text````<br>

____________________________________

> <h3>OpenFile</h3>Tries to open the last downloaded file
____________________________________

