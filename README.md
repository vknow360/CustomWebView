# CustomWebView
## Introduction
CustomWebView is an extended form of web viewer with more customization and flexibility (For MIT AI2 and its distros)

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

## Designer Properties

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/4/7/47aaaa83879e73d42ecc93935bfa83819e7ef69a.png">

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/9/a/9a46ff9628b1695817c56b1f8555c32106a423fd.png">

## Blocks
#### Events
<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/c/3/c3dc649571f81de591acd6d91f1cde7e25cd95dc.png">

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/e/b/ebd3525bba1f84800189545b9af733731d6bab09.png">

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/d/a/daa7b33c9a784a9ef5da85f19c292b3562c302ea.png">

#### Methods
<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/5/e/5eed2dfc182329af169cb3879b6f4b751d38eec3.png">

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/3/1/31ea76a6dcb53bcb194302a24593d4e3f0a455d1.png">

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/9/d/9d2ce4eebfe20ac57a27d31a2e37e393e87dd447.png">

#### Properties
<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/d/c/dc25b49f065626e300f3b6c7f96817b8ae7ccd53.png">

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/4/f/4f6dc91f4eca6e74b11dcf5fe4cc73c5a9ffb8dc.png">

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/b/1/b1d5f11b6e4ef1bad1e2dcf93d01bbe25fb18538.png">

<img src="https://kodular-community.s3.dualstack.eu-west-1.amazonaws.com/original/3X/a/0/a090a99c4daf7da535f97728b10924d6f8e349bf.png">

### Documentation
#### Events
>- **AfterJavaScriptEvaluated**
Event raised after evaluating Js with result
![component_event|386x85](upload://jWcoY2X6v8lpuQLtO78XVONTY3f.png)
result ~ text

>- **Cookies Removed**
Event raised after 'ClearCokies' method with result
![component_event (1)|341x85](upload://oI4JYSznL15vEDYdk7aBC6tZy4p.png) 
successful ~ boolean

>- **Find Result Received**
Event raised after 'Find' method with int 'activeMatchOrdinal','numberOfMatches' and 'isDoneCounting'
![component_event (2)|448x85](upload://xY3MHzsZXSQpVKz2I6KkCHxUScg.png) 
activeMatchOrdinal ~ int
numberOfMatches ~ int
isDoneCounting ~ boolean

>- **Got Ssl Certificate**
Event raised after getting SSL certificate of current displayed url/website with boolean 'isSecure' and Strings 'issuedBy','issuedTo' and 'validTill'.If 'isSecure' is false and other values are empty then assume that website is not secure
![component_event (3)|344x85](upload://bxKFHGfUwWtq1yjqSzgkkN5iM4D.png) 
isSecure ~ boolean
issuedBy ~ text
issuedTo ~ text
validTill ~ text

>- **Got Print Result**
Event raised after getting previus print's result
![component_event (4)|328x85](upload://j2N2c0zrCrd70x3Ca5cnKQngWjm.png)
id ~ text
isCompleted ~ boolean 
isFailed ~ boolean 
isBlocked ~ boolean 

>- **Long Clicked**
Event raised when something is long clicked in webview with item(image,string,empty,etc) and type(item type like 0,1,8,etc)
![component_event (5)|306x85](upload://5roi20FxZ0dJufn9NvaudGRxQMM.png)
item ~ text
type - int

>- **On Console Message**
Event raised after getting console message
![component_event (6)|362x85](upload://6CGMZioCSO1jrC1mfKJfNsrxlDb.png) 
message ~ text
lineNumber ~ int
sourceID ~ int
level ~ text

>- **On Download Needed**
Event raised when downloading is needed
![component_event (7)|357x85](upload://tnEV5o12ftkU3HcIgD5Jeh844O2.png) 
url ~ text
contentDisposition ~ text
mimeType ~ text
size ~ int (long)

>- **On Error Received**
Event raised when any error is received during loading url and returns message,error code and failing url
![component_event (8)|334x85](upload://g98eO3x1YLWYDT1yYHqIWIQgNA7.png) 
message ~ text
errorCode ~ int
url ~ text

>- **On Form Resubmission**
Event raised when resubmission of form is needed
![component_event (9)|367x60](upload://avf5RG8pnGXwgte2ucIXMpc8mAS.png) 

>- **On New Window Request**
Event raised when new window is requested by webview with target url ,boolean 'isDialog' and 'isPopup'
![component_event (10)|377x85](upload://gwVb9iofcPwWwC5DQq4NGGbzvon.png) 
url ~ text
isDialog ~ boolean
isPopUp ~ boolean

>- **On Permission Request**
Event raised when a website asks for specific permission(s)
![component_event (11)|368x85](upload://2gR170MfIBThXIoFpf06A7YDDmC.png) 
permissionsList ~ list < String >

>- **On Progress Changed**
Event raised when page loading progress has changed
![component_event (12)|359x85](upload://bB9PsjED5bIy5JQfE1wggsFR8VP.png) 
progress ~ int

>- **Page Loaded**
Event raised when page loading has finished
![component_event (13)|308x60](upload://sz3esH4VHJSImV6d0qdxBjwJXIX.png) 

>- **Web View String Change**
Event indicating change in webview string
![component_event (14)|384x85](upload://wTgoMEyUBqK8kxn2RJGCVTWLWsh.png) 
value ~ text

#### Methods
>- **Can Go Back**
Gets whether this WebView has a back history item
![component_method (3)|303x26](upload://doQHtzfRHIAndSt14It9q5i7NNZ.png) 
**Returns : boolean**

>- **Can Go Back Or Forward**
Gets whether the page can go back or forward the given number of steps
![component_method (4)|389x50](upload://hyEKwU7VkH974gWKgr4PL4YuulU.png) 
steps ~ int
**Returns : boolean**

>- **Can Go Forward**
Gets whether this WebView has a forward history item
![component_method (5)|324x26](upload://fbFCvT6tJnuLUEFOzlQihtUbtXL.png) 
**Returns : boolean**

>- **Cancel Printing**
Cancels current print job. You can request cancellation of a queued, started, blocked, or failed print job
![component_method (6)|303x30](upload://uMNXskDJ2hgjdCxeEs9gz2n90IA.png) 

>- **Clear Cache**
Clears the resource cache
![component_method (7)|285x30](upload://ejQOCHkOYJQ6x7GVgl7VSt82jN7.png) 

>- **Clear Internal History*
Tells this WebView to clear its internal back/forward list
![component_method (8)|337x30](upload://3wUdkvTKBJPKAHPennjLnlyBHyb.png) 

>- **Clear Location**
Clears all location preferences
![component_method (9)|298x30](upload://oY5je2ciBqAng3uQt7mk3iMybJX.png) 

>- **Clear Matches**
Clears the highlighting surrounding text matches
![component_method (10)|298x30](upload://4p3je7aKuTbOL79XIEtDLvdTtxa.png) 

>- **Content Height**
Returns content height of HTML content
![component_method (11)|310x26](upload://ahNyetSNrzCogoeiUUEDgnWQ7AH.png) 
**Returns : int**

>- **Create WebView**
Arrangement to create webview. Horizontal and Vertical Arrangements are recommended.
![component_method (12)|321x54](upload://fqgqXvNTOOu6HP0EGEv2tASVev4.png) 
container ~ arrangement

>- **Evaluate JavaScript**
Asynchronously evaluates JavaScript in the context of the currently displayed page
![component_method (13)|341x54](upload://zrv9o7vy5otuaDkn5dztrtzD8Y7.png)
script ~ text

>- **Find** 
Finds all instances of find on the page and highlights them, asynchronously. Successive calls to this will cancel any pending searches
![component_method (14)|244x54](upload://8qQ7LTUVo60LjVJ8pZGu6mQJwIS.png) 
string ~ text

>- **Find Next**
Highlights and scrolls to the next match if 'forward' is true else scrolls to previous match
![component_method (15)|275x54](upload://vQwrptoR9QzBrYJeYGYIQk4aKfN.png) 
forward ~ boolean

>- **Get Cookies**
Get cookies for specific url
![component_method (16)|300x50](upload://k9HFA6f2AAJppAlzp0vRmLKKSb1.png) 
url ~ text

>- **Get Ssl Certificate**
Gets the SSL certificate for the main top-level page and raises 'GotCertificate' event
![component_method (17)|318x30](upload://dGGsu2gfzKPHbD4aXonARGoFR0t.png)

>- **Go Back** 
Goes back in the history of this WebView
![component_method (18)|264x30](upload://t0f9i3bxl4YBTvfbLnANE2115pI.png) 

>- **Go Back Or Forward**
Goes to the history item that is the number of steps away from the current item. Steps is negative if backward and positive if forward
![component_method (19)|350x54](upload://z1QH6EwTYHanE5g0vcB3IyDOh4y.png)
steps ~ int

>- **Go Forward**
Goes forward in the history of this WebView
![component_method (20)|285x30](upload://rEiosEUcItNliyQs0VysV1iKMye.png) 

>- **Go To Url**
Loads the given URL
![component_method (21)|288x54](upload://r1bRfHJEO2h5mbUUe65ucgEzNQ2.png) 
url ~ text

>- **Grant Permission**
Grants permissions to webview.It accepts a list of permissions
![component_method (22)|325x54](upload://hXmwrEtL1VfrIw0tCPWRjiuXCwv.png) 
permissions ~ list < String >

>- **Load HTML**
Loads the given data into this WebView using a 'data' scheme URL
![component_method (23)|292x54](upload://LgEuKdMNXofdKBMdJyDqbHzogj.png) 
html ~ text

>- **Print Web Content**
Prints the content of webview with color mode(Use 2 for color scheme , 1 for monochrome scheme and 0 for default scheme
![component_method (24)|327x54](upload://yi2CJX7JjJD94XiTOkzexFIecov.png) 
colorMode ~ int

>- **Reload**
Reloads the current URL
![component_method (25)|254x30](upload://3YOvcOtN6atA3hCY1d5Nu43HqTQ.png) 

>- **Restart Printing**
Restarts current/previous print job. You can request restart of a failed print job
![component_method (26)|305x30](upload://qPfwP4266BCR6eW4vb3SrBljwww.png) 

>- **Resubmit Form**
Whether to resubmit form or not
![component_method (28)|312x54](upload://273gwKHSBGpE6OUpQqvUklWbnvw.png) 

>- **Stop Loading**
Stops the current load
![component_method (30)|294x30](upload://rTzvPNDhMBFFohefVkSKhRuA65k.png) 

### Credits
I would like to say thank you to following people for helping me in making CustomWebView:

<img src="https://community.kodular.io/user_avatar/community.kodular.io/ontstudios/45/55737_2.png">  Mateja Srejić aka OntStudios (<a href="https://community.kodular.io/u/ontstudios/summary">Contact him</a>)</img>
