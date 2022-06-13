## Version 2.18.0

### Improvements
- It is now possible to integrate with the 'QR Droid', 'Barcode Scanner' or 'Binary Eye' app to scan barcodes and use their contents in Scripting, via the new `scanBarcode()` function

### Bugfixes
- All webviews (e.g. for displaying responses) are not properly stopped when their window is closed, preventing them from using resources in the background
- In Scripting, the `response` variable is always defined. When no response is available, it is set to `null` instead of being undefined.
- Headers without a space between the name and value are now correctly imported from cURL

## Version 2.17.0

### Improvements
- Slider variables now support floating point numbers
- You can now use the `uuidv4()` function in Scripting to generate random UUIDs

### Bugfixes
- Triggering a shortcut via a deep-link from an NFC tag now works as expected (thanks [@granoeste](https://github.com/granoeste))
- When switching between categories, their background is now always correctly displayed
- The changelog dialog no longer shows up again unexpectedly after being dismissed
- The horizontal separator lines in menu dialogs are no longer clickable

## Version 2.16.0

### Scripting
- There is now a `parseXML` function available
- The Code Snippet Picker now contains an entry for `JSON.parse()` for easier access
- The Code Snippet Picker now allows to quickly jump to the documentation of each entry
- The `enqueueShortcut` function now allows to forward selected files to other shortcuts (experimental)
- Variables used in Scripting are now evaluated lazily, which means a dynamic variable (e.g. a text input prompt) will only try to resolve its value if that value is actually needed

### Improvements
- If the app crashes or is unintentionally closed while creating or editing a shortcut, the app will offer to recover the unsaved changes the next time it is opened
- Added a button to the Troubleshooting section which allows Xiaomi users to more easily reach the permission screen, to fix the issue of shortcuts not working properly when triggered from the home screen
- Long running shortcuts can now be cancelled with the back button

### Bugfixes
- Dialog windows no longer disappear when switching away from and then back to the app
- Javascript errors from a Scripting shortcut are now displayed in a dialog window instead of a toast, and the line number is no longer skewed by use of the Global Scripting feature
- Choosing a different language in the Settings screen now more reliably applies that language to the app
- When selecting Italian as the app's language, the app is now actually set to Italian instead of Indonesian

## Version 2.15.1

### Bugfixes
- Fixed error when importing categories that use grid layout

## Version 2.15.0

### Improvements
- The Code Snippet Picker was redesigned for clarity and easier use
- You now have the option to use the camera to take a picture and use it as the request body or as a form parameter
- In a Browser Shortcut you can now choose which of the installed browsers should be used
- When using the Grid layout for a category, it is now possible to choose from 3 different types of grid densities
- It is now possible to override the shortcut click behavior on a per category basis
- Categories can now have a custom background color
- The `changeDescription` function was added to allow programmatically updating a shortcut's description

### Miscellaneous
- The "Client Certificate" option was moved to the "Authentication" section in the shortcut editor
- In categories that use the "Wallpaper" background, shortcut labels now have a drop shadow for better readability
- Various small tweaks to performance, app stability and UX improvements

### Bugfixes
- When using the "-F" parameter in a cURL import, the method is now correctly set to POST instead of GET
- When the window displaying a response is closed, the app's main screen should now no longer show up unexpectedly

## Version 2.14.1

### Bugfixes
- The grid category layout now has the correct number of columns again
- The app no longer freezes when switching tabs while shortcut moving is enabled
- The icon picker dialogs are now properly preserved when screen orientation changes

## Version 2.14.0

### Custom Icons
The app now gives you more control over your custom icons. It now includes a new icon picker screen, which allows you to see all the icons you have used before and gives you the option to delete the ones you don't need anymore. In addition, you now have to open to download the favicon of a webpage and use that as the icon for your shortcut.

### Improvements
- In the Variables screen, it is now easier to find out which variables are unused.
- The Code Snippet Picker now contains an entry for the "trim()" function.
- When sharing text into variables, it is now possible to also receive the title being shared, not only the text.
- Changing the title in the app's main screen is now more discoverable via an option in the Settings screen

### Bugfixes
- Some text fields in the shortcut editor were misbehaving or not properly initialized. This is now fixed.
- Unusually large custom icons now just display an error instead of crashing the app
- In most places (not all yet, still work in progress) when a dialog is displayed and then the screen orientation changes, the dialog persists instead of being closed
- The Acknowledgments screen now respects dark mode
- Long pressing on shortcuts now no longer brings up the context menu while the app is locked
- The Settings screen no longer crashes when the screen orientation changes while a dialog is visible

## Version 2.13.1

### Bugfixes
- Deleted shortcuts can now be removed again from Multi-Shortcuts
- When exporting shortcuts and overwriting a file, the exported file no longer gets corrupted
- App no longer crashes when encountering a cookie without a value
- Added missing error handling for large responses
- Added missing loading states to some screens
- Improved resilience against crashes due to very large custom icons
- Fixed an issue with Indonesian translations and improved resilience against similar issues
- The state of the Acknowledgements screen is now preserved when the screen orientation changes

## Version 2.13.0

This version includes a massive refactoring under the hood, so there's a good chance that some new bugs or small changes in behavior were introduced. Please let me know if you find something unusual.

### Scripting
- The `setVariable` function can now be used to set the stored value of any type of variable, not only static/constant variables. This allows you to set the remembered value of a variable where the 'Remember value' option is enabled.
- Added functions `openApp` and `openUrl` to allow opening external apps more conveniently than by using `sendIntent` or a browser shortcut
- Added `getWifiSSID` function to allow querying the SSID of the currently connected Wi-Fi network
- Renamed `triggerShortcut` to `enqueueShortcut` to make it clearer what it does

### Miscellaneous
- Toast messages now support basic HTML tags for formatting
- Improved the UX for the Request Body input box
- Improved the internal data store, so that the app should take up a bit less storage on the device
- Improved text sizes and colors for better readability in various places
- Added Indonesian translation

### Bugfixes
- Quick Settings Tile shortcuts are now sorted alphabetically instead of appearing in no particular order
- Custom icons are now loaded asynchronously and more memory-efficiently, which should reduce the risk of running out of memory when loading many icons at once
- Fixed a deadlock that would happen when calling the `wait` function too many times
- Fixed crash on Android 12 when trying to open Wi-Fi selection screen
- Reduced likelihood of old response window showing up again when triggering a new shortcut
- Removed "Multline" checkbox from Number Input and Password Input variable types as it had no effect and was never supposed to be there
- Improved the handling of large HTTP responses. The app is now less likely to crash when encountering these

## Version 2.12.0

### Miscellaneous
- Use https for new shortcuts by default
- Improved Android backup support
- Improved error messages in some places
- Rearranged the order of the icons in the built-in icon picker

### Bugfixes
- Fixed an issue with the `-d` parameter in cURL parsing
- 3xx status codes are now treated as a successful response instead of a failure

## Version 2.11.0

### Scripting
`response.headers` and `response.cookies` now return a list of strings instead of just a string. This is a breaking change, but it allows handling the case where there are multiple headers or cookies with the same name.

For convenience, there is now a `response.getHeader(headerName)` and a `response.getCookie(cookieName)` function which can be used to retrieve a specific header or cookie by name. If there are multiple headers or cookies with the given name, the last one will be returned.

### Miscellaneous
- When choosing an icon for a shortcut, it is now possible to select a custom icon that was previously used
- Added partial Japanese translation

### Bugfixes
- Passing variable values to a shortcut via the `triggerShortcut` now passes the correct values again, without an unwanted prefix
- Fixed incorrect error message of `wakeOnLan` function

## Version 2.10.0

### Important Changes
The minimum supported Android version is now at 5.0 (Lollipop). Going forward, older versions of Android will no longer receive updates. You can still install older versions of the app by [downloading the APK directly from Github](https://github.com/Waboodoo/HTTP-Shortcuts/releases).

Furthermore, this version introduces a breaking change to the Scripting feature, specifically to the built-in `hmac` and `base64decode` functions. The return type of these functions is now `Uint8Array` (i.e., an array of bytes) instead of a hex string. See the **Scripting** section below for more information.

### Scripting
- The `base64encode` and `hmac` functions now also accept `Uint8Array` (byte arrays) as input, instead of only strings
- The `hmac` and `base64decode` functions now return `Uint8Array` (byte arrays) instead of a hex encoded string
- There are 2 new functions `toString` and `toHexString` which allow to convert `Uint8Array` into strings
- The Code Snippet picker now also allows easily adding "if { }" and "if { } else { }" blocks

### Miscellaneous
- The "Misc Settings" section was renamed to "Trigger & Execution Settings"
- Some error messages, in particular around import & export, were improved
- The color picker dialog for `Color` variables was swapped out for a different, easier-to-use component

### Bugfixes:
- The response headers can now be properly accessed again via Scripting
- Form parameters can now have empty values

## Version 2.9.0

### Improvements
- Added `wakeOnLan` function to allow sending magic packets to wake up devices on the network
- When using Scripting, JavaScript errors now also include the line number for easier debugging
- The `triggerShortcut` function now supports a 3rd argument which can be used to specify a custom delay (in milliseconds)
- Multiple Choice variables can now be configured to allow selecting multiple options

### Miscellaneous
- Prepared the app for compatibility with Android 12

### Bugfixes
- The `triggerShortcut` function can now be used to re-trigger the current shortcut itself, and do so indefinitely for as long as a delay larger than 500 milliseconds is specified.

## Version 2.8.0

### Improvements
- When only one shortcut is configured for the use in the Quick Settings Tile, the tile will assume the name of the shortcut instead of displaying "Execute Shortcut"
- Shortened deep-link URLs so they better fit on NFC tags and small QR codes

### Bugfixes
- When importing from cURL, the method is now set to POST when there is a request body, instead of falling back to GET and ignoring the body
- When using the "Number Input" variable type, it is no longer possible to enter invalid numbers
- When editing variables, the checkbox values are now also taken into account when detecting whether there are unsaved changes


## Version 2.7.0

### Improvements
- Categories can now be pinned to the home screen, allowing faster access
- Category tabs can now be long-pressed to go to the category settings

### Bugfixes
- When exporting a single shortcut via sharing, only that shortcut is exported instead of all shortcuts
- Better error handling for devices that don't offer an image picker

## Version 2.6.0

### Improvements
- Shortcuts can now be re-ordered within a category by dragging
- Basic support for Zeroconf/Bonjour/mDNS (.local domains)
- Added troubleshooting option to improve Tasker integration. Enable it you find that triggering shortcuts from Tasker or other 3rd party apps doesn't work.

### Bugfixes
- Fixed Quick Settings Tile not working when there are 3 or more shortcuts enabled for it

## Version 2.5.0

### Miscellaneous
- Added a "Rerun" button to the response window, allowing to easily execute a shortcut again
- Added option for multiline text input to the "Text Input" variable
- Added and improved some help texts throughout the app

### Bugfixes
- The "Password Input" variable now properly hides the password
- Tweaked the Tasker integration to more reliably execute shortcuts

## Version 2.4.0

### Important: Tasker Integration Overhaul
The integration with Tasker has been updated. It should now work more reliably, in particular in terms of passing around variables.
Unfortunately, this is a **breaking change**, so most likely you will need to re-configure your Tasker tasks, if you have any. Sorry for the inconvenience.

### Miscellaneous
- Added TLSv1.3 support also for older Android versions
- Added option to use a .p12 file directly for client certificate authentication
- Added function `playSound` which allows playing a notification sound
- Added info dialog for easier lookup for a shortcut's ID or deep-linking URL

### Bugfixes
- Fixed broken special characters when using Basic Authentication, now uses UTF-8

## Version 2.3.0

### Client Certificate Authentication
There is now the option to configure a shortcut to use a client certificate for authentication. You'll find the option in the shortcut's *Advanced Settings* section.

### Improved Import & Export
When exporting shortcuts and variables, the resulting file is now a ZIP file instead of a raw JSON file. This has the advantage that custom icons are now also exported and automatically become part of the exported bundle, and of course they can be imported again as well.

The Import and Export functionality was moved out of the general settings screen and into its own dedicated screen to make it easier to discover and use.

### Miscellaneous
- Added `hmac` function, which allows computing the HMAC of a message. Supported algorithms are md5, sha-1, sha-256 and sha-512.
- The "Number Slider" variable type now also supports negative numbers.
- Image responses can now also be displayed in the 'Dialog Window' option, not only with the 'Fullscreen Window' option

### Bugfixes
- When using the `getVariable` function to access a variable's value in code and that variable has not been loaded yet, instead of aborting with an error, the missing value is now resolved in a second variable loading step.

## Version 2.2.0

### Miscellaneous
- Added `base64decode` and `base64encode` functions for scripting

### Bugfixes
- When triggering the same shortcut multiple times but with different variable values, the values are preserved and passed correctly.

## Version 2.1.0

### Miscellaneous
- Browser Shortcuts are no longer limited to just HTTP(S) but can now use arbitrary URL schemes, allowing deep-linking into other apps.
- Improved some of the help texts to make it clearer how to use the app

### Bugfixes
- Replaced the icon picker & cropper, as it no longer worked on Android 10. As a bonus, it now supports free 360° rotation
- Original file names are preserved when sharing files into the app and accessing file information through scripting
- Fixed a crash when displaying responses close to the 1MB size limit.
- Fixed the displaying of the changelog dialog on app startup (so meta).
- Fixed detection of variable names in scripts when enclosed by single quotes instead of double quotes
- When unlocking app settings, the password prompt dialog no longer incorrectly pops up again when the correct password is entered.

## Version 2.0.0

### Response Handling
- When using the 'Window' UI type for displaying the response, it is now possible to save the response into a file.
- When using the 'Window' UI type for displaying the response and the response is an image, it will be displayed as such instead of displaying it as plain text.
- When the response is larger than the limit of 1MB, instead of failing, the request is treated as a success.
- When creating a new shortcut, the default way of displaying the response is via a window instead of a toast.

### Scripting
- When using the Code Snippet picker for the `changeIcons` action it is now possible to pick a custom image instead of only a built-in one.
- It is now possible to access the name, type and size of selected files via Scripting.
- The settings page now features a scripting editor which can be used to define a piece of code which is executed at the start of each shortcut, allowing to share code and define common variables and functions.

### Miscellaneous
- Shortcuts can now be limited to only execute when connected to a specific wifi (thanks [@crasu](https://github.com/crasu))
- The value input field for static (constant) variables is now multi-line, making it easier to enter or view larger values.
- It is now possible to trigger a specific shortcut via a invoking a deep-link of the form `http-shortcuts://deep-link/<id-of-shortcut-here>`
- When displaying a JSON formatted response in a window, it is now possible to toggle line-wrapping by tapping.
- The `About` section was moved out of the `Settings` page to be a standalone page.

### Bugfixes
- Deleted shortcuts now show in the shortcut list of Multi-Shortcuts, making it possible to fully remove them manually.
- When a response is too large for sharing as text, instead of crashing or silently failing it will now try to share it as a file instead.
- When selecting files (for request parameters or body) their original file name is used instead of a generated or internal one.

## Version 1.39.0

### Remote Editing

You can now edit your shortcuts and categories from the comfort of a desktop computer. Go to "Settings > Edit on Computer" to find out more.

### Miscellaneous

- Increased maximum length of static variables (constants) from 3000 to 30000 characters
- Improved Polish translation

### Bugfixes

- Fixed a crash caused by invalid characters in User-Agent header
- Fixed a rounding error when using large numbers in Scripting and storing them using `setVariable()`
- Using getVariable() or setVariable() now raises an error when the variable doesn't exist instead of silently failing

## Version 1.38.0

### Scripting Improvements

There is now a *hash()* action, which allows to apply MD5, SHA-1, SHA-256 and SHA-512.

### Bugfixes

- Content-Length is now correctly computed when using form-data. Previously it was counting some characters (e.g. Cyrillic letter) incorrectly.
- Shared text is no longer ignored when sharing text and files into the app at the same time
- The scrolling behavior is now smoother when displaying HTML responses

## Version 1.37.0

### Scripting Improvements

There is now a *showSelection()* action which allows showing a dialog of options to choose from, similar to the multiple-choice variable type.

### Miscellaneous

- Added help buttons that link to the online documentation throughout the app

### Bugfixes

- The "User-Agent" header can now be set without being overwritten by the default one

## Version 1.36.0

### Cookie Store

Shortcuts can now be configured to accept and store cookies via the *"Advanced Technical Settings"* section.

### Miscellaneous

- Privacy improvements in crash reporting (some unnecessary details excluded from crash reports)

## Version 1.35.0

### Improved "Response Handling"

The "Response Handling" section of the shortcut editor was redesigned to allow for more
fine-grained control over how the HTTP response should be handled and displayed when a
shortcut is executed. For example, it is now possible to display a custom message instead of
the default "Shortcut executed." message.

### Miscellaneous

- It is now possible to export individual shortcuts.
- The *showDialog()* action now supports &lt;a&gt; and &lt;pre&gt; tags

## Version 1.34.0

### Miscellaneous

- Added Korean translation
- Added function to send arbitrary Intents
- Added function to trigger Tasker tasks
- Added option to force requests to run in foreground to deal with device restrictions
- Added support for --get/-G flag in cURL parser

### Bugfixes

- Fixed arrow keys in scripting editor

## Version 1.33.0

### Scripting Improvements

- Redesigned the code snippet picker to make it easier to navigate
- Added *getWifiIPAddress()* helper function (thanks [@wahaha](https://github.com/2219160052))
- Added *wait()* helper function

### Bugfixes

- Requests with file parameters (form-data) or files as request body now correctly include a Content-Length header
- Fixed bug where the response was not available for scripting when code was executed before the request
- Fixed bug where shortcuts couldn't be placed on the home screen using the "Legacy" placement method
- Don't show Text-to-speech option when device does not support it

## Version 1.32.0

### Multi-Shortcuts

You can now create a special type of shortcut which allows you to easily trigger multiple
other shortcuts without having to write special code for it.

### Scripting Improvements

There is now a special type of shortcut which makes it easier to write custom logic through
scripting without making an actual HTTP request. Additionally, it is now also easier to
set up scripting actions which interact with or modify other shortcuts (e.g. to trigger or
rename them or change their icon).

### Miscellaneous

- The app now has a new logo
- You can now use a file as the request body
- Added a text-to-speech action which allows to read out a snippet of text.
Find it in the code snippet picker.
- Added Hungarian as a language (thanks Dezső Gergely)
- Added 6 new built-in icons
- All pickers which offer a selection of shortcuts now also show their icon, e.g. when sharing text or a file into the app
- Renamed "variable key" to "variable name" for clarity

### Bugfixes

- Fixed permission error when sharing a file into the app to be used as a file parameter
- The same shortcut can now be triggered multiple times
- Hidden categories no longer show the options to change layout or background

## Version 1.31.0

### Miscellaneous

- Drastically reduced the size of the app
- Improved quality of most built-in icons. This also allows them to be displayed larger when used in a widget.
- Added 10 new built-in icons to choose from
- cURL import/export now supports Bearer authentication and proxy settings
- The color picker for "Color" variables now supports HSV mode in addition to RGB mode
- When using the 'triggerShortcut' action you can now pass along a set of variable values to the triggered shortcut. See documentation for more information.
- When exporting, unnecessary and redundant fields are excluded from the export to make it smaller in size and easier to read
- Variable types can no longer be changed after creation

### Bugfixes

- Fixed widget creation on Android 10 which would previously result in a crash
- Variable placeholders are now replaced with their respective values when using cURL export

## Version 1.30.0

### Widgets!

The app now has a (somewhat) customizable widget. This provides an alternative to regular
home screen shortcuts and has the benefit that they can be customized more freely (size,
label color, etc.).

Please note that this feature is in an early stage. More customization options will
come in a future release.

### Miscellaneous

- Added a language picker to allow changing the language of the app. If your language is
not there, is incomplete or wrong, please feel free to help me out with the translations.
- Improved validation of inputs for better error messages
- When deleting variables a warning is shown if they are still in use by a shortcut

### Bugfixes

- Fixed crashes that sometimes happened when importing or exporting
- Fixed a bug where shortcuts would sometimes not properly trigger other shortcuts

## Version 1.29.0

### File Parameters

You can now add file parameters to your shortcuts, which allows to send one or multiple
files as part of a request. Triggering a shortcut with a file parameter will open a file
picker to select which file to send. Alternatively you can share a file from another app
to trigger a shortcut, similar to how it was already possible to share text values from
other apps to be used as a value for a variable.

### Response Debugging

There is now an additional option for how to display the response of an HTTP request, which
displays the response body in a fullscreen window but also displays additional meta information
such as response headers, status code and the response time. This should make it easier to
set up or debug shortcuts.

### User Interaction in Scripting

You can now use *alert()*, *prompt()* and *confirm()* as part of the scripting
feature to create flows that require user interaction. These 3 functions work the same way
as you'd expect from a browser.

### Proxy Support

It is now possible to specify an HTTP proxy for each shortcut. Check the "Advanced Technical
Settings" section when creating or editing a shortcut.

### Miscellaneous

- Triggering another shortcut when using scripting is now more reliable and much faster
- HTTP response is ignored if it's not needed, for better performance
- More meaningful error messages are displayed when importing fails

### Bugfixes

- Import and export now work as expected on Android 10 and no longer require Storage permission
- URLs are trimmed to avoid leading and trailing whitespace characters
- Duplicating shortcuts generates correct names

## Version 1.28.0

### Miscellaneous

- Response handling and scripting options are now logically separated for clarity
- Improved Chinese and Italian translation
- Improved error handling for importing, exporting and when features are not supported on the device

### Bugfixes

- Content-Type header no longer includes a charset unless explicitly specified
- Shortcuts on launcher icon are kept up-to-date more reliably

## Version 1.27.0
### Quick Settings Tile

The app now has a Quick Settings Tile, which can be used to trigger a shortcut without opening the app and without
going to the home screen. Simply enable the option for one or more shortcuts via the "Misc Settings" menu in the editor.

### Dynamically change icons

There is now a new action "changeIcon" which can be used to change the icon of a shortcut before or after execution. So far
it only supports built-in icons.

### Miscellaneous

- Bearer Authentication can now be set up more easily, directly from the "Authentication" settings of a shortcut
- Categories can now be hidden
- Added option to change the title on the main toolbar in the app
- Shortcuts can now be imported directly from a URL
- Added 45 new built-in icons
- Added *abort()* function that allows to cancel the execution of a shortcut
- HTTP errors include the HTTP status message in addition to the status code for more clarity
- Newly created shortcuts will show full response in toast by default instead of only simple toast

### Bugfixes

- Categories no longer show the wrong shortcuts after moving or deleting categories or after importing
- App no longer crashes when HTTP response is too large
- (Confirmation) dialogs have the correct colors now when dark mode is enabled
- One shortcut can now trigger multiple other shortcuts (kinda... it's not quite smooth yet...)

## Version 1.26.0

### Miscellaneous

- Better handling of empty responses
- Added Russian translation
- Made "Help me Translate" button more foolproof

### Bugfixes

- GET requests can now have a *Content-Type* header
- CURL export of GET requests no longer incorrectly sets method to POST
- Variables can now be used in URLs again without triggering validation errors

## Version 1.25.0

### Dark Mode

The app now has a dark theme. It can be enabled or disabled via the settings.

### Miscellaneous

- Increased font size of response
- Improved value picker for timeout settings

### Bugfixes

- Variable changes in pre-request actions now properly affect the current execution
- Execution now less likely to fail due to battery saver or data saver modes

## Version 1.24.1

### Bugfixes

- Fixed Tasker integration
- Fixed variable dialogs triggering when they should not
- Fixed resolution of variables within variables
- Fixed adding of variables to request body
- Fixed a crash when setting delay to 1 second

## Version 1.24.0

### Redesigned Editor

The editor for creating and editing shortcuts was completely redesigned. The many different options are now separated
into their own screens to make the UI easier to navigate.

### JavaScript Code Execution

Previously you were able to add specific actions to be executed before or after a shortcut runs. Now, instead of a static list
of actions, you can run arbitrary JavaScript code, which allows for much more flexibility and more advanced workflows.
On top of that, there are now 2 new actions that can be called directly from within the JavaScript code:


- Added action to show a dialog window with text in it
- Added action to copy text to the clipboard

### Miscellaneous

- HTML responses are shown in a web view instead of as plain text, allowing to include styling and formatting
- Basic HTML formatting is now possible when using response type "Dialog" or "showDialog" action
- Added option to have HTTP requests follow redirects
- Increased maximum length for variables of type 'Constant' from 300 to 3000 characters
- Loosened variable name restrictions: They may now contain underscores and be up to 30 characters long.
- Added option to change the background of individual categories

### Bugfixes

- Fixed bug where importing shortcuts would sometimes overwrite other existing shortcuts
- Fixed requests with method DELETE or OPTIONS not sending a request body
- Fixed zero or negative step sizes in variables of type 'Slider' crashing the app

## Version 1.23.0

### App Lock

It is now possible to lock the app with a password, to prevent modification of shortcuts or
app settings.

### Require Confirmation

There is now a new option that allows you to mark a shortcut as requiring confirmation.
These shortcuts will prompt you with a confirmation dialog before executing, thereby
preventing accidental execution.

### Miscellaneous

- Added link to FAQ page
- Added partial translation into Catalan
- Added partial translation into Polish

### Bugfixes

- Fixed shortcuts not working when Battery Saver or Data Saver enabled
- Fixed crash when using form-data on Android Marshmallow or older
- Fixed crash when deleting variable

## Version 1.22.0

### Shortcut Placement / Using in Other Apps

You may have encountered a problem when trying to use shortcuts in other apps (e.g. Tasker),
where it would not select or make the other app crash. There is now an alternative method of
selecting a shortcut that you may try in this case. Simply retry selecting the shortcut and
you will be prompted with a dialog that asks you to pick either the new or the old method.

### Longer Timeouts

It is now possible to have request timeouts of up to 10 minutes.

### Bugfixes

- Request body of *multipart/form* requests now correctly uses CRLF instead of only
LF

- Fixed various rare app crashes

## Version 1.21.0

### Shortcut Actions

You can now define what happens before and after a shortcut is executed. This includes
things like displaying a Toast message, extracting part of the response or triggering
another shortcut. More to come in future updates!

### Support for multipart/form-data

In addition to sending a custom request body and simple parameters, it is now also possible
to send form data, i.e, using the "multipart/form-data" content type.

### Miscellaneous

- New translations: Norwegian (Bokmål), Italian
- Shortcut can be opened in browser
- Improved syntax highlighting & pretty printing of JSON & XML responses

### Bugfixes

- Delayed execution & waiting for network connectivity now work again
- Custom icons now show on the home screen on Oreo
- gzipped responses are now correctly parsed
- Various occasional crashes

## Version 1.20.0

### Simplified Variable Insertion

Inserting variables into shortcuts has now become much easier. Each input field that
supports variables now has a button next to it. Clicking it opens a popup dialog with a list
of all available variables, allowing you to easily pick and insert one.

### Improved Tasker Integration

When triggering shortcuts from Tasker, it is now possible to send along variables. Simply
define variables with the same name in Tasker as in your shortcut and their value will
automatically be sent from Tasker to your shortcut when triggered.

### Drag & Drop Reordering

Categories & Variables, as well as options of *select* & *toggle* variables, can
now easily be reordered via drag & drop.

### Improved Specification of Request Body

When specifying the request body of a shortcut, you now have the option to chose between *x-www-form-urlencoded*,
i.e., the body is a list of parameters, or *custom text*, i.e., you specify the request
body as raw text and give it a content-type.

### Variables within Variables!

The *constant*, *toggle* and *select* variable types now support recursive
resolution of values. This means you can reference other variables from these variables. For
example, you could set up a *select* variable where one of the options is a
*color* variable.

### Miscellaneous

- The input field for request parameter values now allows multiline input.
- The cURL export dialog now includes a *copy* button to allow you to quickly copy
the cURL command to the clipboard.

- The *About* section in the settings now features a *Translate this App* button
as a call-to-action for translators. Help me translate everything!

## Version 1.19.0

- New variable type: number slider
- "Number Input" variable allows decimals and minus sign
- Better support for user-defined certificates
- Password input is hidden
- Internal storage is encrypted

## Version 1.18.0

- French translations
- Fixed shortcut creation on Oreo
- Added Privacy Policy & Crash Reporting opt-out
- Minor bugfixes

## Version 1.17.0

- New variable types: date & time
- Fixed/improved retry on failure
- Option to delay shortcut execution
- Portuguese translations
- Improved quality of custom icons
- Minor bugfixes

## Version 1.16.0

- Support for Digest Auth
- Support for OPTIONS, HEAD and TRACE methods
- Launcher Shortcuts (Android 7.1 and up only)
- Categories can have different layout types: list or grid
- Minor bugfixes & other improvements

## Version 1.15.0

- Shortcuts can be exported as cURL commands
- Chinese translations
- Minor bugfixes

## Version 1.14.0

- Syntax highlighting for JSON and XML responses
- Import shortcuts from cURL commands
- Improved import &amp; export
- Themes
- Minor bugfixes

## Version 1.13.0

- New variable type: color
- Allow sharing text snippets into variables
- New app icon

## Version 1.12.0

- New response options: show in dialog or window
- Option to remember/forget variable values
- Pending executions can be cancelled

## Version 1.11.0

- Variables: inject custom values through prompt dialogs
- Plugin support for integration with automation apps (e.g. Tasker)

## Version 1.10.1

- Shortcuts are removed from homescreen when deleted in app
- Fixed a crash when saving shortcuts

## Version 1.10.0

- Shortcuts can be organized into categories
- Possibility to accept any certificate
- More icons
- *Acknowledgements* section in settings screen
- Bugfixes

## Version 1.9.2

- Bugfixes

## Version 1.9.1

- Layout improvements
- Auto-complete suggestions for custom headers
- Import/Export uses JSON format now
- Improved execution retries after reconnecting with the internet
- Bugfixes

## Version 1.8.0

- Shortcut execution can be delayed when not connected to the internet
- Added *View in Play Store* button to settings screen

## Version 1.7.0

- Shortcuts can have different timeouts (3, 10, 30 or 60 seconds)
- Added *About* section to settings screen
- Added *What\'s new* dialog
- Minor language improvements

## Version 1.6.1

- Bugfixes

## Version 1.6.0

- Custom headers
- Custom request body
- Settings Screen
- Import & Export (new permissions needed)
- New request methods: *PUT*, *DELETE*, *PATCH*
- Shortcuts can be tested before being saved
- Bugfixes

## Version 1.5.0

- Possibility to add *POST* parameters
- More built-in icons, with improved selector dialog

## Version 1.4.0

- Built-in icons
- Shortcuts can have descriptions for the overview screen
- Bugfix: Connections are no longer left idle after use

## Version 1.2.0

- Added support for Ipack icons

## Version 1.1.0

- Shortcuts can be moved up and down in the shortcut list
- Default click action executes the shortcuts instead of opening context menu
- Buttons moved into action bar
- Bugfixes
