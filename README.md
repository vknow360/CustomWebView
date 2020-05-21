# CustomWebView
## Introduction
CustomWebView is an extended form of web viewer with more customization and flexibility (For MIT AI2 and its distros)<br>
**Latest Version : 3**
aix
## Features
- More customization options than normal web viewer
- Long Click event
- New window request event
- Load local files and content using Html and Js
- Find words and phrases in webview
- Evaluate Js and get result
- Block network loads to save data and load faster
- Upload files to websites(requires Android 5.0)
- Error occurred event
- Get output from console
- FormResubmission event
- Get content height
- Get SSL Certificate of website
- Get cookies for particular url/website
- Event for permission(s) request
- Grant permission(s) manually
- Print web content
- A wide range of tools for working with JavaScript

## Designer Properties

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/4/7/47aaaa83879e73d42ecc93935bfa83819e7ef69a.png">

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/9/a/9a46ff9628b1695817c56b1f8555c32106a423fd.png">

## Blocks
#### Events
<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/c/3/c3dc649571f81de591acd6d91f1cde7e25cd95dc.png">

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/e/b/ebd3525bba1f84800189545b9af733731d6bab09.png">

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/d/2/d261afc2dd1471d13b1019c81e8001554db90711.png">

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/4/1/417a3d7a12225b4028b98351d4c93a3e558eb2d0.png">

#### Methods
<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/5/e/5eed2dfc182329af169cb3879b6f4b751d38eec3.png">

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/2/5/25a2fb4f0986adc4145d7007dd0c8b99875639d4.png">

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/7/c/7c525389ba1dcbba13bf403770516f80c40d4966.png">

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/6/c/6c8c176fc3eff46a6948890cfda4cadb5b9a5969.png">

#### Properties
<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/d/c/dc25b49f065626e300f3b6c7f96817b8ae7ccd53.png">

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/4/f/4f6dc91f4eca6e74b11dcf5fe4cc73c5a9ffb8dc.png">

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/b/1/b1d5f11b6e4ef1bad1e2dcf93d01bbe25fb18538.png">

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/a/0/a090a99c4daf7da535f97728b10924d6f8e349bf.png">

### Documentation
#### Events
>- **AfterJavaScriptEvaluated**<br>
Event raised after evaluating Js with result<br>
<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/8/b/8bbd83e08a6fd64f29325ffcf19017dfdc928091.png"><br>result ~ text

>- **Cookies Removed**<br>
Event raised after 'ClearCokies' method with result<br>
<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/a/d/ad2fac53aba7a32bb3b6996bf7b8c7f7d39af0c1.png"><br>successful ~ boolean

>- **Find Result Received**<br>
Event raised after 'Find' method with int 'activeMatchOrdinal','numberOfMatches' and 'isDoneCounting'<br>
<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/e/e/ee11ce248b7c1da84ab1c3aba841a90163d3b640.png"><br>activeMatchOrdinal ~ int<br>numberOfMatches ~ int<br>isDoneCounting ~ boolean

>- **Got Ssl Certificate**<br>
Event raised after getting SSL certificate of current displayed url/website with boolean 'isSecure' and Strings 'issuedBy','issuedTo' and 'validTill'.If 'isSecure' is false and other values are empty then assume that website is not secure<br>
<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/5/0/50e8a959b0a8475154314bdadb6b959cf8ab2a7f.png"><br>isSecure ~ boolean<br>issuedBy ~ text<br>issuedTo ~ text<br>validTill ~ text

>- **Got Print Result**<br>
Event raised after getting previus print's result<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/8/5/857a11afada7f35b29d846db53c0dff86716a808.png"><br>id ~ text<br>isCompleted ~ boolean <br>isFailed ~ boolean <br>isBlocked ~ boolean 

>- **Long Clicked**<br>
Event raised when something is long clicked in webview with item(image,string,empty,etc) and type(item type like 0,1,8,etc)<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/2/6/26238a95392665c5fcb3a0d418f8d846640374e8.png"><br>item ~ text<br>type - int

>- **On Console Message**<br>
Event raised after getting console message<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/2/e/2e6cabc5498ec197fc427395a61aec6eaf7beeb9.png"><br>message ~ text<br>lineNumber ~ int<br>sourceID ~ int<br>level ~ text

>- **On Download Needed**<br>
Event raised when downloading is needed<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/c/d/cdebb9b304e2222e57ee94a659ad04571d1f5a6e.png"><br>url ~ text<br>contentDisposition ~ text<br>mimeType ~ text<br>size ~ int (long)

>- **On Error Received**<br>
Event raised when any error is received during loading url and returns message,error code and failing url<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/7/1/712b12d48e896960a4cd68ff87ac61953ed49243.png"><br>message ~ text<br>errorCode ~ int<br>url ~ text

>- **On Form Resubmission**<br>
Event raised when resubmission of form is needed<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/4/9/499ddf3d6fc35bf525e8b024231671b7b7977726.png">

>- **On Js Alert**<br>
Event raised when Js have to show an alert to user with message to be shown<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/5/3/534be11bca8affdc0f67f681976cee8e6cc0fedd.png"><br>url ~ text<br>message ~ text

>- **On Js Confirm**<br>
Tells to display a confirm dialog to the user<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/a/f/aff1918ef03036ac3ebdda7cb16f2353c5b97beb.png"><br>url ~ text<br>message ~ text

>- **On Js Prompt**<br>
Event raised when JavaScript needs input from user<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/6/f/6ff1aafe37d32766d82937e7ae279fc43665f7eb.png"><br>url ~ text<br>message ~ text<br> defaultValue ~ text

>- **On New Window Request**<br>
Event raised when new window is requested by webview with target url ,boolean 'isDialog' and 'isPopup'<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/7/3/73db7f4d362abf3afac5bfd9c04090fe579384bb.png"><br>url ~ text<br>isDialog ~ boolean<br>isPopUp ~ boolean

>- **On Permission Request**<br>
Event raised when a website asks for specific permission(s)<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/0/f/0fec1b4536fd6e6905ac27770cfc677899afb5be.png"><br>permissionsList ~ list < String >

>- **On Progress Changed**<br>
Event raised when page loading progress has changed<br>
<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/5/1/514b37ee05051f5b0996171f7ee47ab21c5b0c89.png"><br>progress ~ int

>- **Page Loaded**<br>
Event raised when page loading has finished<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/c/8/c833386b8a67f372897b013c5171db586599cbc7.png">

>- **Web View String Change**<br>
Event indicating change in webview string<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/e/6/e684d3d556edc00c2863806a97204f286fc6e149.png"><br>value ~ text

#### Methods
>- **Can Go Back**<br>
Gets whether this WebView has a back history item<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/5/d/5deb60f0f2c2b86804c96b5db0ba3236bc48e427.png"><br>**Returns : boolean**

>- **Can Go Back Or Forward**<br>
Gets whether the page can go back or forward the given number of steps<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/7/b/7b0fe125a515d65df34c46d426be704dcaef4456.png"><br>steps ~ int<br>**Returns : boolean**

>- **Can Go Forward**<br>
Gets whether this WebView has a forward history item<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/6/a/6a725b765d0449af4e120b6902938c55246647b5.png"><br>**Returns : boolean**

>- **Cancel Printing**<br>
Cancels current print job. You can request cancellation of a queued, started, blocked, or failed print job<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/d/7/d7c5933df5fc1ec0ecda73e35789e6da40b33874.png">

>- **Clear Cache**<br>
Clears the resource cache<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/6/4/645cea3d953f4e5cc75ece1b8171a65e19aac601.png">

>- **Clear Internal History**<br>
Tells this WebView to clear its internal back/forward list<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/1/8/18bec90c94bbb65ba9703c4914a5a36bf6df3e8b.png">

>- **Clear Location**<br>
Clears all location preferences<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/a/e/aefef37c461d8d38b27a1918c16b0c0b44d500bd.png">

>- **Clear Matches**<br>
Clears the highlighting surrounding text matches<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/1/e/1eddb2b757f546cd774996b1ef2c75991d5b8cf4.png">

>- **Confirm Js**<br>
Whether to proceed JavaScript originated request<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/e/2/e29e8f03477671bbba48d69df114b2311abda764.png"><br>confirm ~ boolean

>- **Content Height**<br>
Returns content height of HTML content<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/4/8/4818d1a479d81014c9e69bbe08c25b414ea7719f.png"><br>**Returns : int**

>- **Continue Js**<br>
Inputs a confirmation response to Js<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/f/5/f583e0c1bf5902e191f8994b3f8bfe93f1670f5c.png"><br>confirm ~ boolean

>- **Create WebView**<br>
Arrangement to create webview. Horizontal and Vertical Arrangements are recommended<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/6/c/6c18ad0c917ce7e36e94f9416525526271c04f16.png"><br>container ~ arrangement

>- **Dismiss Js Alert**<br>
Dismiss previously requested Js alert<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/f/7/f748611cbf88aa4c359d7037f7144a4c12929e73.png">

>- **Evaluate JavaScript**<br>
Asynchronously evaluates JavaScript in the context of the currently displayed page<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/f/8/f867f66a8ee328217145e304b0c8c3d417c82247.png"><br>script ~ text

>- **Find**<br>
Finds all instances of find on the page and highlights them, asynchronously. Successive calls to this will cancel any pending searches<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/3/b/3b1a1ddb0ce9db4046273b2e9697b3ac057a9ff6.png"><br>string ~ text

>- **Find Next**<br>
Highlights and scrolls to the next match if 'forward' is true else scrolls to previous match<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/d/f/df33538bc419788635a1cad19f6a7d1ca7d770fb.png"><br>forward ~ boolean

>- **Get Cookies**<br>
Get cookies for specific url<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/8/d/8d444e9ffdb8dab7b3df29959cdd15047d99a513.png"><br>url ~ text

>- **Get Ssl Certificate**<br>
Gets the SSL certificate for the main top-level page and raises 'GotCertificate' event<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/5/f/5fef7cd0d475632278424ef566291b43492ba829.png">

>- **Go Back** <br>
Goes back in the history of this WebView<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/c/b/cb461dc20cc0b19a7cf9e1d257437df22611df96.png">

>- **Go Back Or Forward**<br>
Goes to the history item that is the number of steps away from the current item. Steps is negative if backward and positive if forward<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/f/5/f5819f5f07fa0dc5bd686718d485ae1c5b72457e.png"><br>steps ~ int

>- **Go Forward**<br>
Goes forward in the history of this WebView<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/c/1/c1c8d07918553fa79281c8eec0679182230946da.png">

>- **Go To Url**<br>
Loads the given URL<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/b/d/bd5d2ba9fbf1720ee00fef60520888ddcc9019c6.png"><br>url ~ text

>- **Grant Permission**<br>
Grants permissions to webview.It accepts a list of permissions<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/7/d/7ddad44a0f1facbf999ab54d28202866b3a6977f.png"><br>permissions ~ list < String >

>- **Load HTML**<br>
Loads the given data into this WebView using a 'data' scheme URL<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/0/5/0557df57665eb92c8c6bd97e02c9d3cd7444e88b.png"><br>html ~ text

>- **Print Web Content**<br>
Prints the content of webview  <br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/c/0/c0e73f0f8ff52ef1ba82347d766ab335de908029.png">

>- **Reload**<br>
Reloads the current URL<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/1/b/1be6643cfd756efdfde28f329c665c3d6427b946.png">

>- **Restart Printing**<br>
Restarts current/previous print job. You can request restart of a failed print job<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/b/c/bc039fd059fbe13551700b27b3a819e2fb071e38.png">

>- **Resubmit Form**<br>
Whether to resubmit form or not<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/0/e/0ed070cd64e6aaee28761457a03fd2ea92ae12a6.png"><br>reSubmit ~ boolean

>- **Stop Loading**<br>
Stops the current load<br><img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/c/3/c382e0ff39e983e222d0badd69b134cffd490cc2.png">

### Credits
I would like to say thank you to following people for helping me in making CustomWebView:

<img src="https://community.kodular.io/user_avatar/community.kodular.io/ontstudios/45/55737_2.png">  Mateja Srejić aka OntStudios (<a href="https://community.kodular.io/u/ontstudios/summary">Contact him</a>)</img>

### Reference Links
1.WebView Docs from Google (<a href="https://developer.android.com/reference/android/webkit/WebView">Click here</a>)<br>
2.Extension release on Kodular Community (<a href="https://community.kodular.io/t/customwebview-an-extended-form-of-web-viewer/63037">Click here</a>)
