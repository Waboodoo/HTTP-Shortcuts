# Scripting

When a shortcut is executed it is possible to run JavaScript code snippets before or after execution. You'll find the corresponding settings in the shortcut editor in the *"Scripting"* section.

![Scripting Option in Shortcut Editor](../assets/documentation/scripting/01.png)
![Scripting Editor](../assets/documentation/scripting/02.png)

Additionally, you'll find an option in the app's settings that allows you to run code before the execution of every shortcut, which can be used to define shared functions.

Using these code blocks, there are a number of things you can achieve. See the [examples](#examples) below for inspiration.

<a name="handle-response"></a>
## Handling the Response

You can access the response of your HTTP request via the `response` object. 

### Getting the Response Body

The response body is available as a string via `response.body`.

```js
const myBody = response.body;
```

### Getting Response Headers

The response headers are available as an object (key-value-pairs) via `response.headers`. Each key corresponds to the name of the header and its value is an array of strings of all the headers with that name.

There is also a convenience function `response.getHeader(name)` which can be used to get the value of a specific header by its name. If there are multiple headers with that name, the last one is returned, and if there are none, `null` is returned.

```js
const contentType = response.headers['Content-Type'][0];

const contentLength = response.getHeader('Content-Length');
```

### Getting the Status Code

The response's HTTP status code is available as an integer via `response.statusCode`.

```js
const isNotFound = response.statusCode == 404;
```

### Getting Cookies

The response's cookies are available as an object (key-value-pairs) via `response.cookies`. Each key corresponds to the name of the cookie and its value is an array of strings of all the cookies with that name.

There is also a convenience function `response.getCookie(name)` which can be used to get the value of a specific cookie by its name. If there are multiple cookies with that name, the last one is returned, and if there are none, `null` is returned.

```js
const myCookieValue = response.cookies['MyCookieName'][0];

const myCookieValue2 = response.getCookie('MyCookieName2');
```

If you need more details about a cookie (e.g. it's expiration timestamp) you can use `response.headers['Set-Cookie']` to read out the cookie headers directly.

### Errors

Please note that the `response` object will be `null` if there was no response from the server, i.e., in case of a network error. In that case, you can inspect the `networkError` to get a string describing the error.

<a name="variables"></a>
## Reading & Writing Variables

In the app you can create variables to be used in your shortcuts, e.g. to dynamically insert values for a URL, a query parameter or part of the request body. To do so use the variable editor found by clicking the *"{}"* button in the main screen of the app.

This section explains how you can interact with these variables from a script.

### getVariable

You can access the value of any of your variables via the `getVariable()` function. Simply pass the variable's name or ID as a parameter.

```js
const myValue = getVariable('myVariable');
```

Please note that the returned value will always be a string. If the variable does not exist an error is raised.

### setVariable

You can store a value as a string into a variable via the `setVariable()` function. Simply pass the variable's name or ID as the first parameter and the value you want to store as the second parameter.

```js
setVariable('myVariable', 'Hello World');
```

Please note that there is a size limit of 30000 characters. If the variable does not exist an error is raised.

<a name="shortcut-info"></a>
## Getting Information about the Current Shortcut

You can easily retrieve information about the current shortcut from the `shortcut` object. Currently this only includes the shortcut's ID, name and description.

```js
shortcut.id;
shortcut.name;
shortcut.description;
```


<a name="files"></a>
## Selected Files

If your shortcut makes use of file parameters or uses the content of a file as the request body then you can access information about these files using the `selectedFiles` array. Each selected file has an entry, allowing you to read out its file name, size (in bytes) and media type.

```js
const numberOfFiles = selectedFiles.length;

selectedFiles[0].name;
selectedFiles[0].size;
selectedFiles[0].type;
```

<a name="user-interaction"></a>
## User Interaction

This section describes how you can interact with the user (i.e., you), during the execution of a shortcut, e.g., to ask for additional input, to confirm an action or to display information.

### showToast

With this function you can display a toast message on the screen. Simply pass your message as the first parameter.

```js
showToast('Hello World');
```

Please note that no toast will be displayed if the string you pass is empty.

### showDialog

With this function you can display a dialog window on the screen. Simply pass your message as the first parameter, and optionally a title for the dialog as the second paramter. The dialog will be displayed until its *"OK"* button is pressed.

```js
showDialog('My Message', 'My Title');
```

Please note that no dialog will be displayed if the string you pass is empty.

### prompt, confirm

Similar to how JavaScript works in a browser, you can use `prompt()` and `confirm()` to ask the user for input as part of a workflow.

```js
if (confirm('Are you sure?')) {
    // Do something only if the user clicked 'OK'
}
```

```js
const myName = prompt('What is your name?');
```

### showSelection

This function allows you to display a multiple-choice dialog from which an option can be picked. It takes one argument, which must be either an object consisting of key-value string pairs, or a list of strings. It returns the selected value as a string, or `null` if the dialog is closed without a selection (e.g. by pressing the back button).

```js
// Using an array of strings
const starterPokemon = showSelection(['Bulbasaur', 'Charmander', 'Squirtle']);

// Using an object
const favoriteColor = showSelection({
    '#ff0000': 'Red',
    '#00ff00': 'Green',
    '#0000ff': 'Blue',
});
```

<a name="play-sound"></a>
### playSound

With this function you can play a notification sound. If no argument is passed, it will use the system's default notification sound. You can use the Code Snippet picker inside the app to select a different sound.

```js
playSound();
```

<a name="speak"></a>
### speak

With this function you can have a piece of text be read out loud, using the device's text-to-speech engine. Simply pass the text you want to read as the first parameter, and optionally a language identifier as the second parameter. Please note that only the first 400 characters will be read. Please also note that the second parameter is ignored if the language is not supported.

```js
speak('Hello World');

speak('Dieser Text ist deutsch', 'de');
```

This function may not be supported by all devices.

### vibrate

With this function you can cause the device to vibrate (if supported). As an optional first parameter, you can pass the number of the vibration pattern you want to use, and as an optional second paramter you can pass a boolean denoting whether the execution should wait for the vibration pattern to finish or not.

Vibration patterns:

- 1 means *"1 short vibration"*
- 2 means *"1 long vibration"*
- 3 means *"3 short vibrations"*

```js
vibrate(2, true);
```

<a name="modify-shortcuts"></a>
## Modify Shortcuts

This section lists all the built-in functions which you can use to modify existing shortcuts programmatically.

### renameShortcut

With this function you can rename a shortcut. Simply pass the name or ID of a shortcut as the first parameter and the new name as the second one. You can also pass an empty string as the first parameter to target the current shortcut.

```js
renameShortcut('Old Name', 'New Name');
```

### changeIcon

With this function you can change the icon of a shortcut. Simply pass the name or ID of a shortcut as the first parameter and the name of the icon as the second one. You can also pass an empty string as the first parameter to target the current shortcut. This only works with built-in icons. Use the *"Add Code Snippet"* in the app to select an icon.

```js
changeIcon('My Shortcut', 'bitsies_lightbulb');
```

<a name="control-flow"></a>
## Control Flow

This section lists some of the options you have to control the execution flow of your script.

### wait

The `wait` function allows you to delay execution by waiting (also called sleeping) for a specified number of milliseconds before continueing with the execution of the script.

```js
wait(3000); // delay execution by 3 seconds
```

### abort

With the `abort` function you can abort the execution of the shortcut.

```js
abort();
```

<a name="text-processing"></a>
## Text Processing

This section lists some of the built-in text processing functions.

### base64encode and base64decode

With the `base64encode` and `base64decode` functions you can encode or decode a given string using Base64.

```js
const encoded = base64encode('Hello world');
const decoded = base64decode(encoded);
```

The return type of `base64encode` is a string, the returned value of `base64decode` is a `Uint8Array`. You can use `toString()` to convert it to a string if needed.

### hash

With the `hash` function you can compute the hash of a given string. The first parameter denotes the hashing algorithm to use (supported algorithms are `MD5`, `SHA-1`, `SHA-256`, and `SHA-512`) and the second one the string to hash. The return value is in hex format.

```js
const hashed = hash('SHA-256', 'Hello world');
// the value of `hashed` is '64ec88ca00b268e5ba1a35678a1b5316d212f4f366b2477232534a8aeca37f3c' now.
```

<a name="hmac"></a>
### hmac

With the `hmac` function you can compute the [HMAC](https://en.wikipedia.org/wiki/HMAC) of a given message. The first parameter denotes the hashing algorithm to use (supported algorithms are `MD5`, `SHA-1`, `SHA-256`, and `SHA-512`), the second one the secret key, and the third one the message string for which to compute the HMAC. The returned value is a `Uint8Array`.

```js
const myHMAC = hmac('SHA-256', 'my_key123', 'Hello world');
const myHMACasHex = toHexString(myHMAX);
// the value of `myHMACasHex` is '34d60d40202ae16ae3dd70c9715b1900f9fe30cf10af483e74ea8f6bef18bd09' now.
```

### toString and toHexString

The functions `toString` and `toHexString` can be used to convert a `Uint8Array` to a string, which is particularly useful in combination with the `hmac` and `base64decode` functions.

```js
const myValue = base64decode('SGVsbG8=');
const result = toString(myValue);
// the value of `result` is 'Hello' now.
```

<a name="misc"></a>
## Miscellaneous Built-In Functions

This section lists all of the built-in functions which do not fall into a specific category.

<a name="trigger-shortcut"></a>
### triggerShortcut

With this function you can trigger a shortcut to execute after the current one. Simply pass the name or ID of a shortcut as the first parameter.

```js
triggerShortcut('My Other Shortcut');
```

Optionally you can pass an object as the second parameter to provide values for variables. This will not change the stored value of the variable but they will assume the specified value when the other shortcut is executed. This is particularly useful for dynamic variable types (such as *"Text Input"* or *"Multiple Choice Selection"*).

```js
triggerShortcut('My Other Shortcut', {
    'My_Variable1': 'Hello World',
    'My_Variable2': ':D',
});
```

As an optional third parameter, you can pass the number of milliseconds by which to delay the execution. This way you can schedule a shortcut to run at a later point in time. Please note that the delay will not be exact.

```js
triggerShortcut('My Other Shortcut', null, 10 * 60 * 1000); // runs in 10 minutes
```

Note that triggering a shortcut means its execution is enqueued. This means that it will only be executed once the current shortcut (and all shortcuts that have been enqueued before it) has finished executing. It will *not* execute it immediately.

<a name="copy-to-clipboard"></a>
### copyToClipboard

With this function you can copy a value to the device's clipboard. Simply pass the value you want to copy as the first parameter.

```js
copyToClipboard('Hello World');
```

<a name="get-wifi-ip-address"></a>
### getWifiIPAddress

With this function you can retrieve the IPv4 address of the device on the current wifi. It will return `null` if there is currently no wifi connection.

```js
const myIP = getWifiIPAddress();
```

<a name="send-intent"></a>
### Send Intent
With this function you can send an [Intent](https://developer.android.com/guide/components/intents-filters). It takes an object as its only parameter, where the object should have one or more of the following properties:

|Parameter|Description|Type / Values|
|---|---|---|
|type|Defines how the intent should be sent.|`'broadcast'` (default), `'activity'` or `'service'`|
|action|A string that specifies the generic action to perform (such as view or pick).|string|
|category|A string containing additional information about the kind of component that should handle the intent.|string|
|categories|Same as `category` but allows specifying multiple values.|list of strings|
|dataUri|A URI that references the data to be acted on|string|
|dataType|The MIME type of the data|string|
|className|The full name of a class that is to be started by the Intent|string|
|packageName|The name of an application package that is to be started by the Intent|string|
|extras|A list of extras, i.e., additional parameters to be sent|list of objects (see below)|
|clearTask|Whether to set the [`FLAG_ACTIVITY_CLEAR_TASK`](https://developer.android.com/reference/android/content/Intent#FLAG_ACTIVITY_CLEAR_TASK) flag|boolean|
|excludeFromRecents|Whether to set the [`FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS`](https://developer.android.com/reference/android/content/Intent#FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS) flag|boolean|
|newTask|Whether to set the [`FLAG_ACTIVITY_NEW_TASK`](https://developer.android.com/reference/android/content/Intent#FLAG_ACTIVITY_NEW_TASK) flag|boolean|
|noHistory|Whether to set the [`FLAG_ACTIVITY_NO_HISTORY`](https://developer.android.com/reference/android/content/Intent#FLAG_ACTIVITY_NO_HISTORY) flag|boolean|

Each extra consists of the following properties:

|Parameter|Description|Type / Values|
|---|---|---|
|name|The name of the extra|string|
|type|The type of the extra|`'string'` (default), `'boolean'`, `'int'`, `'long'`, `'double'`, `'float'`|
|value|The value of the extra|depends on the `type`|


Here is an example of how this function can be used:

```js
sendIntent({
    type: 'activity',
    action: 'my.special.action',
    packageName: 'com.example.foobar',
    className: 'com.example.foobar.MainActivity',
    extras: [
        {
            name: 'favorite_number',
            type: 'int',
            value: 42,
        },
    ],
});
```

Please note that it is unfortunately *not* possible to send intents that require the app to hold a specific permission, as there is no way to dynamically add such a permission to the app. This is a technical limitation that the app itself cannot address. The recommended workaround is to use a 3rd-party automation app such as Tasker to perform such actions and trigger their tasks/workflows either via an intent or via the `triggerTaskerTask` function (see below). Another possibility would be to fork the app, add the required permission to it and build it yourself.

<a name="trigger-tasker-task"></a>
### Trigger Tasker Task
If you have [Tasker](https://play.google.com/store/apps/details?id=net.dinglisch.android.taskerm) installed on your device, you can use this function to trigger one of its tasks. Pass in the name of the task as the first parameter, and optionally an object containing some key-value pairs to pass along as local variables as the second argument.

```js
triggerTaskerTask('doStuff');

triggerTaskerTask('mytask', {
    myLocalVariable: 'hello',
    andAnother: 'world',
});
```

<a name="wol"></a>
### Wake-on-LAN

You can use the `wakeOnLan` function to send a magic packet to turn on another device on your network. The first parameter has to be the MAC-address of the device. As the optional second parameter, you can pass the network/broadcast address to be used, and as the third parameter you can define the port.

```js
wakeOnLan('01-23-45-67-89-ab');

wakeOnLan('01-23-45-67-89-ab', '255.255.255.255', 9);
```


<a name="examples"></a>
## Examples

This section lists some examples for typical use-cases of the code execution feature.

### Generate values for use in the request

Sometimes you need to generate a value, for example the current date, or a random number, to be used in your request. You can do this by generating the value and then storing it into a variable that you then use in your request. Here's an example:

```js
const randomNumber = Math.floor(Math.random() * 10);
setVariable('myNumber', randomNumber);

const currentDate = (new Date()).toDateString();
setVariable('today', currentDate);
```

### Parse a JSON response

Here's an example on how to parse the response of your shortcut, and display the result or store it into a variable for later user.

```js
const temperature = JSON.parse(response.body).temperature;
setVariable('temperature', temperature);
showDialog('The current temperature is '+temperature+'°C', 'Temperature');
```

### Change icon and label based on response

This example shows how the shortcut icon and label can be changed based on the received response. The example assumes that the server returns 'OK' if the request was a success.

```js
if (response.body == 'OK') {
    renameShortcut('', 'Success');
    changeIcon('', 'freepik_check'); // changes the icon of the current shortcut to a green checkmark
} else {
    renameShortcut('', 'Failure');
    changeIcon('', 'freepik_close'); // changes the icon of the current shortcut to a red cross
}
```

### Ask for confirmation before execution shortcut

This example shows how you can show a custom confirmation message before the shortcut executes and only execute it if the user confirms by clicking 'OK'.

```js
if (!confirm('Should I do the thing?')) {
    showToast('Not doing the thing.');
    abort();
}
```

### Read the response out loud

This example shows how you can have the received response be read out loud (using text-to-speech). The example assumes that the response is in plain-text (i.e., not HTML, JSON, ...).

```js
speak(response.body);
```

### Open another application

This example shows how you can use the [sendIntent](#send-intent) function to open another application, in this case a browser to display a website:

```js
sendIntent({
    type: 'activity',
    action: 'android.intent.action.VIEW',
    dataUri: 'https://example.com',
});
```

If you want to just open a specific app without sending any data to it, you can do so by specifying the app's package name and using the `android.intent.action.MAIN` action. The following example will just open Google Chrome:

```js
sendIntent({
    type: 'activity',
    action: 'android.intent.action.MAIN',
    packageName: 'com.android.chrome',
});
```

