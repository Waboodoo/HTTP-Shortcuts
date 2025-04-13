# Advanced Features

This page is a collection of some less commonly used or more advanced use cases, as well as some of the app's more hidden features.

<a name="headless-mode"></a>
## Run shortcuts in "headless mode"

Normally, executing an HTTP shortcut consists of sending a request, waiting for the response, and then processing the response. However, in some cases, the response isn't all that important, e.g., if you already know that it will be empty. In these cases, you can configure your shortcut to run in a fire-and-forget manner, called "headless mode", meaning that the shortcut will not wait for the response and finish immediately after sending the HTTP request.

Headless mode is automatically enabled for a shortcut if all of the following conditions are met:
- In the "Response Handling" settings, both "On Success" and "On Failure" are set to "Show nothing (run silently)", or the "Display Type" is set to "Toast Popup" or "Notification", which requires the app to have the Notification permission
- In the "Scripting" settings, the "Run on Success" and "Run on Failure" fields are empty
- The shortcut does not make use of file parameters of file request bodies
- The "Wait for connection when offline" checkbox is not ticked
- The "Require specific Wi-Fi" checkbox is not ticket
- Battery Saver and Data Saver modes are disabled, or the app is excluded from them
- The shortcut does not use any variables with large values (i.e., multiple KB)

<a name="share-text"></a>
## Share text into a shortcut

You might want to be able to share a piece of text (e.g. the URL of the current page in a browser) from an app and use it as part of an HTTP request. You can do this with the use of [variables](variables.md).

When you create or edit a variable, you'll find a checkbox labelled "Allow Receiving Value from Share Dialog". Enable this and save your changes. From now on you can share text from another app into the HTTP Shortcuts app, and it will use the shared text as the value of that variable, in whatever place you used the variable.

If you use this variable in multiple shortcuts, you will be prompted to choose which shortcut should be executed.

If you are on Android 11 or newer, you can enhance this by enabling a shortcut as a Direct Share target. You will find the checkbox for this in its "Trigger & Execution Settings". When this is enabled, the shortcut will appear in the Direct Share sheet, making it easier to quickly share text with that specific shortcut.

Note that the value that you share will not be stored into that variable, but will only be used temporarily for the execution of the shortcut, meaning that the next time you execute the shortcut that variable will still have its previous value.

See also the [variables documentation](variables.md#sharing) for more information.

<a name="share-files"></a>
## Share files into a shortcut

If you want to share a file, you can do so by opening the *Request Body / Parameters* section in the shortcut editor and there either set the *Request Body Type* to *File (Picker)* or set it to *Parameters (form-data)* and then add a parameter of type *Single File* or *Multiple Files*. After that save your changes. You should now be able to share files into the HTTP Shortcuts app (the option is called "Send to...") and it will allow you to pick the shortcut as a target. This will execute the shortcut and it will use the content of the shared file as the request body or as a form parameter.

If you have multiple shortcuts that use files in their body, you'll be prompted to select which shortcut should receive the shared file.

If you are on Android 11 or newer, you can enhance this by enabling a shortcut as a Direct Share target. You will find the checkbox for this in its "Trigger & Execution Settings". When this is enabled, the shortcut will appear in the Direct Share sheet, making it easier to quickly share files with that specific shortcut.

<a name="deep-link"></a>
## Trigger shortcut via deep-link

Each shortcut has an associated deep-link URL. You can use this URL to trigger the shortcut from outside the app, by invoking that URL. This is particularly useful if you want to trigger a shortcut by scanning a QR code or an NFC tag. Simply use the shortcut's deep-link URL as the payload for the QR code or NFC tag.

You can get a shortcut's deep-link URL by long-pressing the shortcut in the app's main screen and selecting "Show Info". This will open a dialog window which shows you the URL.

It is also possible to pass additional values to that shortcut, to temporarily override the values of variables used by those shortcuts (similar to how the ["Share into"](#share-text) feature works). Simply append them as query parameters, so e.g. if you have a variable called "myVariable" and you want to invoke a shortcut that uses it, you can do so and pass the value "Hello World" to it via a URL that might look like this:

```
http-shortcuts://f943652a-5f4b-47d9-a4dd-6588292e63dd?myVariable=Hello%20World
```

Make sure to properly URL-encode the value. You'll find tools online that help you achieve this.

<a name="secondary-launcher"></a>
## Trigger shortcut via secondary launcher app

In some cases you might not be able to use home screen shortcuts. In this case, as a workaround, the app supports a secondary launcher app, through which shortcuts can be triggered.

To enable this secondary launcher app, open the editor for one of your shortcuts and go to the "Trigger & Execution Settings" screen. There you'll find an "Allow triggering via secondary launcher app" checkbox. Enable this and save your changes. After this you should find the secondary launcher app in your device's list of apps under the name "Trigger shortcut".

If you enable this for multiple shortcuts you'll be prompted to select the shortcut you want to trigger every time you open this secondary app.

Unfortunately, due to technical limitations on Android, it is not possible to change the name or icon of this secondary launcher app.

<a name="quick-settings-tile"></a>
## Trigger shortcut via quick settings tile

On most Android devices you can pull down the status bar to reveal the quick settings area, e.g. to quickly toggle Wi-Fi or enable "Do not disturb" mode. You can edit this area and choose the tiles that are relevant to you and rearrange them. When you do you'll notice that there's also an HTTP Shortcuts tile called "Trigger shortcut". This tile allows you to quickly trigger a shortcut from anywhere.

To enable a shortcut to be accessible via this quick settings tile, open the editor for it and go to the "Trigger & Execution Settings" screen. There you'll find an "Allow triggering via Quick Settings Tile" checkbox. Enable this and save your changes.

If you enable this for multiple shortcuts you'll be prompted to select the shortcut you want to trigger every time.

<a name="app-launcher"></a>
## Trigger shortcut via app launcher

Similar to the quick settings tile, another quick way to trigger a shortcut is via the app launcher, i.e., by long-pressing the HTTP Shortcut app's main app icon on the home screen. This will open a menu which shows all the shortcuts which have been enabled to support this.

To enable a shortcut to be accessible via the app launcher, open the editor for it and go to the "Trigger & Execution Settings" screen. There you'll find a "Show as app shortcut on launcher" checkbox. Enable this and save your changes.

Please note that there is a limited number of shortcuts that can be shown on the app launcher. In most cases this limit is set to 5 but the exact number depends on your device's manufacturer.

<a name="integrate-with-tasker"></a>
## Integrating with Tasker

### Trigger a shortcut from Tasker
You can use [Tasker](https://play.google.com/store/apps/details?id=net.dinglisch.android.taskerm) to trigger a shortcut. To pass a value from Tasker to HTTP Shortcuts you need to create a variable of type *Static Variable* in HTTP Shortcuts and a global variable with the same name in Tasker. Make sure to do so BEFORE you select the shortcut from Tasker. All global variables that have matching variables in HTTP Shortcuts are automatically passed over.

You can use the [setResult()](scripting.md#set-result) function (part of the [Scripting feature](scripting.md)) to pass data back to Tasker.

### Trigger a Tasker task from a shortcut

See the [triggerTaskerTask documentation](scripting.md#trigger-tasker-task) for details about triggering a Tasker task.

<a name="certificate-pinning"></a>
## Certificate pinning

When you use HTTPS, your requests will be sent over a secure connection. "Secure" here mainly means that the connection is encrypted and that the app will check that the server it connects to has a valid SSL certificate. In some cases, you might want this check to be more restrictive, i.e., it should not only check that the certificate is valid but that it is a specific certificate. Most likely you will not need this, but if you think you do, I suggest you read more about the topic online first. Note that this is different from validating [self-signed certificates](#self-signed-certificates).

The HTTP Shortcuts supports basic certificate pinning. You'll find the option for it on the Settings screen. Each entry you add here consists of a hostname pattern and the certificate fingerprint. The hostname pattern defines for which domain name(s) the pinning should be used. The following formats are supported:

- Exact matching host names. E.g. `example.com` would match only that domain itself, no subdomains
- Wildcard for all subdomains (but not subdomains of those subdomains). Use the asterisks character for this. E.g. `*.example.com` would match `foo.example.com` and `bar.example.com`, but not `example.com` or `foo.bar.example.com`
- Wildcard for all subdomains and arbitrarily many subdomains of those. Use two asterisks characters for this. E.g. `**.example.com` would match `example.com`, `foo.example.com` as well as `foo.bar.example.com`

The fingerprint has to be either the SHA-1 or SHA-256 fingerprint of your server's certificate, e.g. `7B:50:2C:...:3F:5E`. One easy way to get this fingerprint (although, technically not secure, so you better know what you're doing) is to enter the wrong value first and find the correct value in the error message you get back after executing a shortcut that uses it.

Once you have configured a certificate pinning this way, all HTTP shortcuts that connect to a domain that matches its pattern will verify that the server's certificate matches the specified fingerprint. If this check fails, and error is displayed instead. To ensure that your hostname pattern actually matches, you can just temporarily modify the fingerprint and verify that the request fails, then change it back and verify that it now succeeds.

<a name="self-signed-certificates"></a>
## Using self-signed certificates

By default, HTTP requests to a server that uses a self-signed certificate will fail, as the trust chain can not be verified. In order to make such requests work, edit your shortcut and look for the "Advanced Technical Settings" section at the bottom. In there you will find the "Host Verification" option. Change it from "Secure Default" to "Check Certificate Fingerprint only" and then copy the SHA-1 or SHA-256 fingerprint of your certificate into the text field below. This will disable the normal host verification and instead only check the fingerprint of the certificate presented by the server. In either case your connection will be encrypted.

<a name="tables"></a>
## Displaying responses as a table

If your HTTP response body is a JSON array (or a JSON object with a single field which is a JSON array), you have the option to display it as a table instead of as raw JSON. To enable this, open the "Response Handling" screen from the shortcut editor. There, make sure that "Display Type" is set to "Fullscreen Window", then click the "Display Settings" button. On the screen that opens, set the "Response Type" to "JSON" and enable the "Display JSON array as table" checkbox (it's enabled by default).

You can also use this feature to display a custom table, by setting the "On Success" setting to "Show a custom message" instead of "Show the response", and then putting your custom JSON table into the "Message" field, e.g. via a [variable](variables.md) which is then set via the [setVariable](scripting.md#set-variable) Scripting function.

Here's an example code snippet that generates a JSON array from a JS list, such that it could then be displayed as a table:

```js
const myList = [
  {columnA: "A1", columnB: "B1"},
  {columnA: "A2", columnB: "B2"},
  {columnA: "A3", columnB: "B3"},
]

setVariable("myMessage", JSON.stringify(myList));
```
