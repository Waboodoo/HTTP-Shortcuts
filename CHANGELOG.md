## Version 3.28.0

### Improvements
- You can now customize the buttons of the dialog window when using the `showDialog` function in Scripting
- The size of shortcut icons and text can now be increased, via the "Scaling Factor" option in the Category settings
- When opening the Variables screen from the variable picker dialog, you can now directly select a variable without first having to navigate back to the dialog
- Shortcuts triggered from a Multi-Shortcut can now also benefit from ["Headless Mode"](https://http-shortcuts.rmy.ch/advanced#headless-mode)

### Miscellaneous
- The entire internal database was migrated and refactored. This should not have any noticeable impact, but might have introduced new bugs. As always, if you find something, please [report it](https://http-shortcuts.rmy.ch/contact).

## Version 3.27.0

### Improvements
- You can now create "MQTT Shortcuts", which let you send MQTT messages without resorting to the `sendMqttMessages` Scripting function
- You can now define a custom charset for the response in the `sendHttpRequest` function

### Bugfixes
- The charset for the response can now also be configured even when the response is not displayed, e.g. for the use in Scripting.
- It is now no longer possible to create certificate pinnings with invalid hostname patterns
- Having cyclical references in Scripting function parameters no longer crashes the app
- Very long responses are now truncated when displayed to avoid crashing the app

### Miscellaneous
- When an unexpected error occurs, a more meaningful toast message is displayed
- Implemented a workaround for a crash caused by a recent Tasker update
- Lots of big changes in the underlying database. Hopefully everything still works.

## Version 3.26.0

### Improvements
- Categories can now be subdivided into sections, allowing for better structuring of your shortcuts
- You can now create "Wake-on-LAN" shortcuts, which allow you to wake up computers on your network
- Scripting functions `htmlEncode` and `htmlDecode` were added

### Bugfixes
- Fixed an issue where byte arrays were not properly handled in Scripting functions, such as `hmac`

### Miscellaneous
- Added icons to some of the menus
- Added a few useful hints here and there to better explain some features
- The Scripting examples were moved out of the general Scripting documentation and into their own dedicated page

## Version 3.25.0

### Improvements
- Various accessibility improvements
- Added better error handling in some places

### Bugfixes
- Scripting functions now work correctly when an integer, integer array, or byte array is used as a parameter (e.g. `hmac` or `writeFile`)
- The `showNotification` function now also works when only a title but no message is provided

### Miscellaneous
- The generation of the "Device ID", which is used for remote editing and crash logs, was unified and simplified

## Version 3.24.0

### Improvements
- The experimental setting for running all shortcuts in the background was removed and replaced with a revamped per-shortcut setting. It's still somewhat experimental, but should be more stable now.
- In addition to toasts, dialogs and windows, shortcuts can now be configured to display their response (or custom message) in a notification
- New Scripting functions `showNotification` and `showWindow` were added, which allow to display content in a notification or a fullscreen window
- The Scripting functions `promptColor`, `promptDate`, `promptTime` and `showSelection` now support adding a title to the dialog. Similarly, variables of type "Date Input" and "Time Input" can now be configured to show a title on the picker dialog.
- The Scripting function `sendHttpRequest` now also supports adding custom headers, a text-based body and form data to the request
- Improved accessibility in some places by adding content descriptions to some UI elements and increasing touch surfaces or font sizes

### Bugfixes
- The `parseHtml` function no longer fails when returning an array

## Version 3.23.0

### Improvements
- There is now rudimentary support for the Quick Access Device Controls feature (on Android 11 and up)

### Bugfixes
- The "Display Type" dropdown field no longer fails to open after having been disabled and re-enabled
- The progress spinner now no longer flickers up for really short-lived shortcut executions
- Scripting functions no longer fail when an empty string is passed to them

## Version 3.22.0

### Improvements
- The JavaScript engine used for the Scripting feature was completely replaced. This reduces the size of the app, should improve performance and fix some more exotic bugs, and allow the app to build again for F-Droid.

### Bugfixes
- The "Show at startup" checkbox in the Changelog now behaves correctly

### Miscellaneous
- The minimum supported Android version was raised to Android 8 (Oreo).

## Version 3.21.0

### Improvements
- Homescreen widgets can now be reconfigured without needing to be removed and re-added
- The size of the icon on home screen widgets can now be reduced
- It is now possible to select whether IPv4 or IPv6 should be used for a shortcut's hostname (thanks [menendezeldemente](https://github.com/menendezeldemente))
- You can now send simple HTTP requests directly from Scripting with the newly added `sendHttpRequest` function

### Bugfixes
- The circle icon in the icon picker now has the correct aspect ratio

### Miscellaneous
- The size of the app has been reduced

## Version 3.20.0

### Improvements
- It is now possible to enable JavaScript as part of the response display window. This feature is experimental and it may not work for every website.
- When selecting the "Read from File" or "Write to File" options from the Code Snippet picker in Scripting, there is now a picker to select the directory that should be used.

### Bugfixes
- When exporting shortcuts that use the `getDirectory()` function in their Scripting, the directories are now also included in the export.

### Miscellaneous
- When creating your very first shortcut, a useful hint is now shown if you haven't opened the "Response Handling" section
- Removed some broken icons

## Version 3.19.0

### Improvements
- It is now possible to show/hide categories with Scripting
- The `writeFile` and `appendFile` Scripting functions can now also write raw bytes instead of just strings
- The Code Snippet picker now includes an "Append to File" option

### Miscellaneous
- Prepared for Android 15
- Fixed typos and other minor improvements to documentation and help texts (thanks [@NathanBaulch](https://github.com/NathanBaulch))

## Version 3.18.0

### Improvements
- In addition to `writeFile`, there is now also an `appendFile()` Scripting function which allows to append content to a file instead of fully replacing the file's content.

### Bugfixes
- The `getDirectory(...).readFile()` and `getDirectory(...).writeFile()` Scripting function should now work as expected.
- The "Allow receiving files from share dialog" checkbox now only shows for regular HTTP shortcuts instead of all shortcut types.

### Miscellaneous
- Rearranged some options on the "Response Handling" screen for clarity
- The "Display JSON array as table" option is now enabled by default, and it now also applies if the response is a JSON object with a single key which holds a JSON array.

## Version 3.17.0

### Improvements
- It is now possible to hide shortcuts inside the app, either manually or with the `setShortcutHidden` Scripting function.

### Bugfixes
- When using the `prompt()` function with a very long text, the text now properly scrolls instead of pushing the text input field off screen.

### Miscellaneous
- Removed unnecessary details from malformed GSON error message
- The size of the APK file is back to a more reasonable value, after having blown up unexpectedly for version 3.16.0

## Version 3.16.0

### Improvements
- It is now possible to read and write files using the Scripting feature
- You can now exclude shortcuts from being able to receive files from the Share dialog, similar to how it was already possible to exclude variables. This option can be found on the "Trigger & Execution Settings" screen.
- Variables can now be configured such that their values are excluded from exports
- In widgets, it is now possible to hide the icon, allowing the creation of text-only home screen widgets

### Bugfixes
- The label in widgets is now aligned such that multiple widgets placed next to each other with varying label lengths still look nice
- Links to documentation now open in the browser instead of crashing when no web view is available
- Fixed a few crashes that would occur during app startup in some rare cases

### Miscellaneous
- Android 5 is no longer supported. Android 6 is the new minimum supported Android version.
- The maximum length for static variables is increased to 40'000 characters.
- I'm beginning to phase out the feature that allows having variable placeholders inside static variables. Let me know if you see a problem with that.

## Version 3.15.0

### Improvements
- If the response is a JSON array, there is now an option to display it as a table instead of raw JSON
- The curl import and export now support the `--silent` parameter
- There is now a dedicated screen for managing directories, for granting the app access to reading and writing files. So far this is only used for storing HTTP responses into files, but it will be expanded upon in upcoming releases.

### Bugfixes
- In the widget, the label is now less likely to be truncated or hidden when there isn't enough space
- When shortcuts are moved within the app, the shortcuts shown on the main app launcher are now also rearranged accordingly
- When a shortcut that used the "Secondary Launcher App" option is deleted, the "Trigger Shortcut" app is properly removed from the launcher now

## Version 3.14.0

### Improvements
- The Response Handling screen was redesigned for more clarity and to allow for future improvements
- Shortcut selection dialogs now also show the shortcuts' descriptions
- A warning is now displayed when a request body contains malformed JSON

### Bugfixes
- Fixed a crash that would happen on some devices when deleting characters in Scripting fields or the cURL import
- Home screen shortcuts are now properly disabled when the respective shortcut is deleted

## Version 3.13.0

### Improvements
- On the Event History screen, it is now possible to use relative timestamps instead of absolute ones
- The "Meta Information" section that is displayed above the response is now collapsed by default and can be expanded when needed
- The icon picker for built-in icons now has a (rudimentary, English-only) search
- There is now an `abortAndTreatAsFailure()` Scripting function which can be used to customize whether a response should be considered a success or not
- The `sendTCPPacket()` Scripting function can now also be used to listen for incoming data, allowing for a basic request-response setup
- Similar to the response window, it is now possible to also change the font size of the response dialog
- The curl import now supports the `--head` parameter
- Various small tweaks to the UI here and there to improve clarity, performance, and to make it more consistent

### Bugfixes
- Fixed a crash that occasionally happened when testing shortcuts

### Miscellaneous
- The Event History now shows up to 12 hours of data instead of only 8

## Version 3.12.1

### Bugfixes
- Fixed crash when placing a shortcut on the home screen

## Version 3.12.0

### Improvements
- The performance of the syntax highlighting is now improved, so that it also works on larger pieces of code or data
- When exporting shortcuts, you can now more easily select which shortcuts should be exported
- When importing from or exporting to curl, the `--insecure` option is now supported
- In Scripting, it is now possible to get the ID and name of the category the current shortcut belongs to
- Some tweaks to the built-in documentation were made to make it easier to understand

### Bugfixes
- The `getClipboarContent()` function should now also work when using the experimental background execution setting
- The `parseHtml()` function now also works for reading the content of `<script>` and `<style>` tags
- When saving changes to Global Scripting, the screen now properly closes without showing the discard warning

### Miscellaneous
- Newly created shortcuts now have the "Show as app shortcut on launcher" option enabled by default, for easier integration with other apps

## Version 3.11.1

### Bugfixes
- Fixed a bug that caused some shortcuts to fail when executed. Mostly shortcuts that send requests to subdomains and use cookies were affected.

## Version 3.11.0

### Improvements
- It is now possible to increase or decrease the font size when displaying a response in a window
- The Scripting function `parseHTML()` was added, which allows to extract information from an HTML string
- If you have Wireguard installed, you can now use the Scripting function `setWireguardTunnelState()` to enable or disable a tunnel
- When opening a custom tab with the `openUrl()` function, it now shows the Share button
- When the HTTP response is an image and it is displayed using the Fullscreen Window option, it is now possible to zoom in on the image

### Bugfixes
- Hitting the back button while viewing the in-app documentation will now first close the search instead of navigating back to the previous page
- When a text input field is automatically selected, the soft keyboard should now show up more reliably

### Miscellaneous
- There is now an experimental setting, which lets you change the execution mode of all shortcuts from running in the foreground (i.e. by showing the spinner animation during execution) to running in the background (i.e., no spinner). If you try it out and discover problems, or if you try it out for a longer time and find no problems, please let me know.

## Version 3.10.0

### Improvements
- There is now a new variable type "Incrementing Counter", which increases its value by 1 every time it is used
- The `openUrl` Scripting function can now be used to target a specific browser, or to open a URL in custom tabs
- When using the quick settings tile with only a single shortcut, and that shortcut uses a built-in icon with a distinct silhouette, the quick settings tile will use the shortcut's icon instead of displaying the default icon

### Bugfixes
- In the cURL import & export, digest authentication now properly uses `--digest`

### Miscellaneous
- When selecting a custom image and using the "circle" shape, an uncropped version of the image is preserved for better icon quality when placed on the home screen

## Version 3.9.0

### Improvements
- When using multiple variables in a shortcut, their values are now resolved in deterministic order, according to the order in which the variables appear on the Variables screen
- Built-in icons are now treated as adaptive icons, allowing them to have nicer backgrounds when placed on the home screen of devices that support this
- It is now possible to search for text in the built-in documentation pages

### Bugfixes
- `<sub>` tags in HTML responses are no longer incorrectly displayed as superscript
- Variable value resolution no longer fails under some rare conditions
- Fixed a crash that would happen when trying to display a 1000+ lines plain text response

### Miscellaneous
- When an HTML response tries to open a URL, either because of a redirect or a clicked link, it will first warn about the use of an external browser

## Version 3.8.0

### Improvements
- When configuring a Browser Shortcut to open in a Custom Tab, it is now possible to also select which specific browser to use
- It is now possible to override the response charset, to handle cases where a server does not indicate the content's charset correctly
- Custom icons can now be cropped into a circular shape
- The cURL import screen now shows a warning when unsupported options are used
- Experimental: added integration with Google Assistant. Activate it from the shortcut editor's "Trigger & Execution Settings" section

### Miscellaneous
- The "Troubleshooting" section was moved out of the Settings screen and onto its own dedicated screen for easier access
- Tweaked the swipe gesture for easier switching between categories

## Version 3.7.0

### Improvements
- You can now override the content type of a shortcut's response, forcing it to be displayed as plain text, JSON, XML, or HTML
- Browser Shortcuts can now be configured to open in a Custom Tab instead of a regular browser tab
- It is now possible to keep the HTTP connection open after a request was made, allowing for faster requests in succession
- The app's theme can now be changed to use Dynamic Color

### Bugfixes
- Deep-links for quickly importing from a URL now work without taking a detour through the browser
- It is no longer possible to bypass the app's lock by using deep-links

## Version 3.6.0

### Improvements
- The performance of the syntax highlighting in the Scripting editor was improved, allowing for larger bits of code
- Starting an import directly from clicking a link now works more universally, as you can use a URL of the form `https://http-shortcuts.rmy.ch/import?url=[URL]` to automatically start an import from a URL

### Bugfixes
- The `openUrl()` function now works again in Android 13

## Version 3.5.0

### Improvements
- Shortcuts can now be configured to show up as Direct Share targets (on Android 11 and up only). You'll find the option for this in the shortcut's "Trigger & Execution Settings".
- Performance improvements for the syntax highlighting for the Scripting feature and the editing of JSON request bodies
- Added an option to the Troubleshooting section in the settings that allows canceling all pending shortcut executions at once

### Bugfixes
- Variable now also respect the URL- and JSON-encode setting when a value for them is being shared into the app
- The app can now properly detect whether Tasker is installed, to advertise the `triggerTaskerTask` function on the code snippet screen.
- It is no longer possible to add invalid characters into the global user-agent

### Miscellaneous
- Lots and lots of cleanup and refactoring under the hood. If you find bugs, let me know.
- Minor cleanup of UI of the Response Handling screen
- Removed the option to export using the legacy JSON format

## Version 3.4.0

### Improvements
- When using files for request body or form parameters, you can now set a static file to avoid having to use the file picker every time
- When locking the app, it is now possible to unlock it with biometrics in addition to the PIN code
- Responses can be displayed using a monospace font
- The icon picker shows the previously selected icon at the top for easier re-use and re-coloring
- The `getLocation()` function can now also be used without Play Services (though the result may be less accurate and less reliable)

### Bugfixes
- Long responses displayed in a dialog are now scrollable

### Miscellaneous
- The screenshots in the bundled-in documentation were updated

## Version 3.3.0

### Improvements
- You can now globally change the User-Agent used for all HTTP requests
- Slightly increased the size of UI elements in selection dialogs to reduce the risk of misclicking
- Images used by the documentation are now bundled into the app, such that the documentation can be fully viewed without requiring an internet connection

### Bugfixes
- Client certificates work again

## Version 3.2.0

### Improvements
- A shortcut can now be configured to only check the fingerprint of a server certificate instead of validating the whole chain-of-trust, which allows the use of self-signed certificates
- In the Scripting editor, there is now an undo button
- There is now a `promptNumber` function available for Scripting, which allows to request the input of a number
- In addition to a simple confirmation prompt, shortcuts can now be configured to require biometric confirmation before execution

### Bugfixes
- HTTP responses encoded with Brotli can now be uncompressed and displayed correctly
- Horizontal scrolling in large HTML responses is smoother now
- Long section titles on the Settings screen are no longer cropped

### Miscellaneous
- Added documentation for the use of permissions

## Version 3.1.0

### Improvements
- Scripting shortcuts can now be tested directly from the code editor screen
- The contrast of the color scheme for syntax highlighting in the code editor was increased for better readability
- In addition to the `abort` function, there is now also an `abortAll` function which can be used in combination with the `executeShortcut` function.

### Bugfixes
- Fixed the crashes and related issues introduced in 3.0.2, which would affect the Import feature and the use of variables
- The `promptDate` & `promptTime` functions, as well as Date Input and Time Input variables now correctly handle custom date formats again

## Version 3.0.2

### Bugfixes
- Various crashes fixed
- The `timestamp` variable now uses the provided timestamp format correctly

## Version 3.0.1

### Improvements
- The width and spacing of the tabs for switching categories has now been reduced to make it easier again to switch between many categories quickly

### Bugfixes
- Calling `executeShortcut` no longer gets stuck on an endless loading spinner if the called shortcut makes use of dialog windows

## Version 3.0.0

### Improvements
- The app's UI was migrated to Material Design 3
- New variable type `timestamp` was added, which reflects the current date and/or time
- When using images for a shortcut's request body or form parameters, you now have the option to crop and rotate the image first
- The Scripting editor as well as the cURL import screen now have syntax highlighting for easier editing
- The input field for the request body text now supports syntax highlighting for JSON, XML and HTML. In addition, there is now a button that allows you to format your JSON.
- You can now configure Certificate Pinning. You'll find the option for this on the Settings screen.
- If a shortcut is configured to store the HTTP response into a file, it will now write that file directly instead of first storing the response into an internal file
- The cancelling of a shortcut (e.g. via the back button) is now tracked in the Event History
- When picking the color for an icon you now see a preview of the icon in that color
- The Event History now also shows a timestamp's seconds when the event is expanded

### Bugfixes
- When displaying an HTTP response, the charset provided in the response headers is now taken into account instead of always defaulting to UTF-8
- Text with variable placeholders in it can now be copied from one field to another without losing the placeholders
- In the Scripting feature, putting block comments around a piece of code that contains variable placeholders no longer leads to syntax errors during execution
- The MDNS host resolution now no longer just waits without doing anything and might actually resolve the host

### Miscellaneous
- Previously, when importing shortcuts, it was only possible to import shortcuts that were exported by the same or an older version of the app. It is now possible to also import shortcuts from a newer version, provided compatibility can be guaranteed.
- The Event History was moved out of the main menu into the Troubleshooting section on the Settings screen
- The feature for moving shortcuts was reworked. It is now easier to move shortcuts between categories.
- The recursion limits for shortcuts triggering other shortcuts were loosened. Previously, the execution would be cancelled after 5 recursion steps, now instead it will just include an artificial delay after 10 steps. This gives you more control and flexibility and still allows you to manually cancel an accidental infinite loop.

### Removed
- The app no longer supports different themes and instead just uses one default theme.
- It is no longer possible to use the home screen's wallpaper as the background for a category. This has the advantage that the app no longer requires the permission to access the file system.

## Version 2.31.0

### Improvements
- You can now configure a shortcut to automatically store the HTTP response body into a file
- When creating a home screen shortcut to a category, you can now choose an icon for it
- When parsing CURL commands, the `--data-raw` flag is now supported

### Bugfixes
- Fixed an error message popping up occasionally when combining Scripting, repetition and the "Wait for internet" feature in a single shortcut
- When selecting a content-type from the suggestions popup, it is now properly stored instead of sometimes being ignored

### Miscellaneous
- Minor change to the order in which variables are resolved when executing a shortcut: When a variable references another variable, the referenced variable is now resolved immediately afterwards instead of at the end
- If a shortcut is simple enough (i.e., no Scripting, variables, use of files, ...), it will now launch faster and with less UI flashing when triggered from the quick settings tile

## Version 2.30.0

### Improvements
- There is now basic functionality to run a shortcut repeatedly on a fixed interval.
- You can now share text from Scripting via the newly added `shareText()` function.

### Bugfixes
- When displaying a JSON formatted response, special characters are no longer HTML encoded
- The `base64encode()` function no longer appends unnecessary trailing newline characters
- When using the `sendIntent()` function, you can now set the "dataType" parameter without having to also set the "dataUri" parameter.

## Version 2.29.0

### Improvements
- You can now log custom events into the Event History via the `logEvent()` function, allowing for better troubleshooting
- The `setVariable()` function now allows you to change the stored value of a variable while still allowing the variable to be resolved normally afterwards. Simply pass `true` as the third parameter. This is useful if you want to change the preset / previously used value of a variable for which "Remember value" is enabled.

### Bugfixes
- Tables in the built-in documentation are now properly displayed
- Fixed a crash when trying to edit a shortcut with device language set to Spanish

### Miscellaneous
- The Event History will now heuristically try to remove tokens and secrets from the HTTP headers it displays
- The Privacy Policy can now be accessed more easily through the built-in documentation

## Version 2.28.0

### Improvements
- When dealing with image files, you now have access to some meta data such as orientation and time of creation via the Scripting feature

### Bugfixes
- The app no longer shows up in Recent Apps after a shortcut was executed
- Fixed a bug where under certain circumstances shortcuts would always fail when variable values were too large

### Miscellaneous
- A number of minor tweaks to animations and behavior improvements for text fields

## Version 2.27.0

### Improvements
- The new "Event History" feature allows you to see details about recently executed shortcuts, their HTTP requests and responses, which you can use for troubleshooting.
- You can now use the `setResult()` function in Scripting to pass data back to `executeShortcut()` or to Tasker
- It is now possible to enable a secondary launcher app, which can be used to trigger shortcuts. This allows for workarounds on devices that can't make use of home screen shortcuts or widget.
- Switching to other apps and back while a shortcut is executing is now possible

### Bugfixes
- Being unable to resolve a host is now treated as a regular error again, instead of an unexpected error
- For streaming URLs (e.g. EventStream URLs), reaching the read timeout is no longer considered an error

### Miscellaneous
- All variables are now resolved lazily, i.e., as late as possible and only when needed. The main difference this might make for you is that if you use `getVariable` or `setVariable` in a shortcut's "Run before Execution" Scripting, the order in which your variables are resolved might be different from before.
- Additional documentation for some more advanced or hidden feature is now available
- Clean-up work running in the background is now more considerate with memory and battery use

## Version 2.26.0

### Improvements
- Added support for SOCKS proxies
- Added `promptPassword()`, `promptColor()`, `promptDate()` and `promptTime()` functions to Scripting feature
- Added lots of translations. Much of it was machine translated so it might not be accurate, feedback for improvements is welcome

### Bugfixes
- Fixed a crash when clicking the "Share" button on the response dialog
- Fixed an issue where HTTP requests would fail when battery saver or data saver was enabled
- Fixed the handling of arguments passed to `sendMqttMessages()`
- Parameter-based request body is now also included when a custom content-type header is provided

### Miscellaneous
- Added better validation for shortcut import to better support externally edited or generated export files
- The default color in the color picker is now white instead of black to make it possible to pick a new color without touching the brightness slider
- The `hmac()` function now also accepts messages in byte array format instead of only strings

## Version 2.25.1

### Bugfixes
- Widgets no longer trigger the wrong shortcut when clicked
- App no longer crashes when using multiple choice variables

## Version 2.25.0

### Improvements
- It is now possible to use the newly added "executeShortcut" function to have a shortcut trigger another shortcut to execute immediately and use its response in Scripting
- You can now use the Scripting feature to send TCP packets and MQTT messages
- Simple shortcuts now automatically run in "headless mode", meaning that the HTTP request is done in the background instead of you having to look at the loading indicator until the request is complete

### Bugfixes
- In many cases, triggering shortcuts from 3rd party apps wasn't working anymore on Android 13. This should now be fixed.
- When using the Remote Editor, global scripting code is now correctly pulled instead of being ignored in some cases
- When configuring a shortcut to run silently (i.e., neither response nor errors are displayed), the checkboxes for selecting the available actions are no longer shown, as they have no effect

### Miscellaneous
- Support for Ipack icons was removed
- Many big changes under the hood, so there's a good chance some new bugs snuck in. Please let me know if you encounter any.

## Version 2.24.0

### Improvements
- You can now choose which action buttons are displayed on the fullscreen window that displays the HTTP response: rerun, share, copy, and save to file
- Improved interaction with 3rd party camera apps, even when they're not playing by the rules

### Bugfixes
- Fixed the F-Droid build once again

### Miscellaneous
- Added a '#' to the color picker dialog to make it clearer that it asks for an RGB hex value

## Version 2.23.0

### Improvements
- When using the "Dialog" response display type, you can now choose to add a Copy, Share or Rerun button to the dialog
- All color pickers in the app now show you the RGB hex code for the selected color and allow picking a color by entering a new code
- When picking a custom color for a built-in icon, the color picker will now remember the previously used color, making it easier to use the same color for multiple shortcuts
- All documentation is now bundled into the app, allowing you to read it even when offline and without having to leave the app
- It is now possible to set up the quick settings tile directly from the settings screen (Android 13 only)

### Bugfixes
- Shortcuts with a delay are no longer delayed twice in a row when triggered from a Multi-Shortcut or via `enqueueShortcut()`
- Browser shortcuts, Multi-shortcuts, and Scripting shortcuts no longer show the "Wait for connection" option, as it has no effect for them
- Launcher shortcuts are now properly updated when shortcuts are duplicated or deleted

### Miscellaneous
- Shortcut executions now always run in the foreground, which makes them more stable and avoids issues on Android 13
- When using Multiple Choice variables with multiple selectable values, the result now reflects the order in which the options were selected
- Added a mechanism to break the app out of an infinite loop of shortcuts triggering other shortcuts
- Added support for themed icons (Android 13 only)
- Rearranged the settings a bit for more clarity
- Re-added the language picker

## Version 2.22.0

### Improvements
- When picking a built-in icon which consists of only one color, it is now possible to pick a custom color for it. This replaces the predefined colored circle icons.

### Bugfixes
- Variable and shortcut placeholders in Scripting can now be properly deleted via the backspace key, which previously occasionally would not work
- It is now possible to fetch the favicon for a shortcut even when the shortcut's URL contains variable placeholders
- When editing a shortcut and only changing the "Require Confirmation" checkbox, the Save button now appears as expected

### Miscellaneous
- Added some minor UI animations to the shortcut editor to make it clearer when new input elements become available

## Version 2.21.1

### Improvements
- Improved support for navigating the app with a DPAD or arrow keys, for better TV support
- When sharing files into the app, a loading indicator is now displayed while the files are being processed
- There is now some debouncing to avoid accidentally executing the same shortcut twice in a row

### Bugfixes
- Selecting text from the HTTP response in the fullscreen window no longer crashes the app
- The menu on the Import/Export screen no longer shows a "Sort" option which doesn't do anything

### Miscellaneous
- The language selector is temporarily removed from the app, as a side-effect of a mitigation of another bug. Hopefully it will be back soon.

## Version 2.21.0

### Improvements
- Dialogs now support the `<img>`-HTML-tag, allowing you to display images e.g., via the `showDialog()` function. Base64-encoded images are also supported.

### Bugfixes
- Fixed a crash when interacting with the clipboard via variables or Scripting
- Fixed the F-Droid build, so that hopefully the app can be published there again

## Version 2.20.0

### Variables
- There are two new types of variables, UUID and Clipboard Content, which bring some of the functionality that was so far only possible via Scripting into an easier to use form. More to come (probably).
- With variables of type Text Input, Number Input, or Password Input, you can now choose to display a custom message in the dialog window, and similarly with variables of type Color Input, Date Input, or Time Input, you can now choose to display a title in the dialog window.
- You can now add a prefix and suffix to be displayed along the number that is displayed in a Number Slider variable's dialog window, e.g., to add a percentage sign or unit

### Scripting
- The code snippet picker now has a search feature to make it easier to explore and find available functions for Scripting
- You can now query the contents of the device's clipboard via the `getClipboardContent` function

### Improvements
- When doing a partial export of shortcuts, only those custom icons, variables and client certificates that are really needed are included in the exported file
- It is now possible to add the same shortcut multiple times to a multi-shortcut, and additionally it is now possible to add multiple shortcuts at once

### Bugfixes
- Inserting a variable placeholder into the "Message" field for response display settings works again as expected
- The User-Agent request header no longer contains mismatched parentheses
- For languages written right-to-left (specifically Hebrew) the variable picker button no longer overlaps with other views

### Miscellaneous
- The option to change the app's theme is disabled when dark mode is on
- Stetho (for network traffic inspection via Chrome) was removed from the app, as it was not working properly and posed a potential security risk

## Version 2.19.0

### Scripting
- You can now send UDP packets using the `sendUDPPacket` function

### Improvements
- When exporting, you can now select which shortcuts to export, allowing for partial exports

### Bugfixes
- When a request fails, the response is now displayed according to the configured display type for errors, instead of the one for successful requests
- Newly created shortcuts are no longer placed into the first category if that category is hidden
- Multiple-Choice variables can now be configured again to allow selecting more than one value at time

### Miscellaneous
- Added helper texts to the variable creation dialog to better explain the different types of variables and their use

## Version 2.18.0

### Scripting
- It is now possible to integrate with the 'QR Droid', 'Barcode Scanner' or 'Binary Eye' app to scan barcodes and use their contents in Scripting, via the new `scanBarcode()` function
- There is now a `getLocation()` function which allows you to query your device's physical location (not available in F-Droid version)

### Improvements
- You can now open the "Import from URL" dialog directly via deep-linking, allowing for convenient two-click importing of shortcuts from tutorials and template collection websites
- It is now possible to easily sort all variables alphabetically

### Bugfixes
- All webviews (e.g. for displaying responses) are now properly stopped when their window is closed, preventing them from using resources in the background
- In Scripting, the `response` variable is always defined. When no response is available, it is set to `null` instead of being undefined.
- Headers without a space between the name and value are now correctly imported from cURL
- The order of launcher shortcuts is now updated immediately when shortcuts are moved around within a category

### Miscellaneous
- Minor reorganization of the shortcut editor layout
- Added a helper text that explains what toasts are
- Moved the "Become a Translator" call-to-action up to a more prominent position

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

### Bugfixes
- When using the "-F" parameter in a cURL import, the method is now correctly set to POST instead of GET
- When the window displaying a response is closed, the app's main screen should now no longer show up unexpectedly

### Miscellaneous
- The "Client Certificate" option was moved to the "Authentication" section in the shortcut editor
- In categories that use the "Wallpaper" background, shortcut labels now have a drop shadow for better readability
- Various small tweaks to performance, app stability and UX improvements

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

### Bugfixes
- Quick Settings Tile shortcuts are now sorted alphabetically instead of appearing in no particular order
- Custom icons are now loaded asynchronously and more memory-efficiently, which should reduce the risk of running out of memory when loading many icons at once
- Fixed a deadlock that would happen when calling the `wait` function too many times
- Fixed crash on Android 12 when trying to open Wi-Fi selection screen
- Reduced likelihood of old response window showing up again when triggering a new shortcut
- Removed "Multiline" checkbox from Number Input and Password Input variable types as it had no effect and was never supposed to be there
- Improved the handling of large HTTP responses. The app is now less likely to crash when encountering these

### Miscellaneous
- Toast messages now support basic HTML tags for formatting
- Improved the UX for the Request Body input box
- Improved the internal data store, so that the app should take up a bit less storage on the device
- Improved text sizes and colors for better readability in various places
- Added Indonesian translation

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

### Bugfixes
- Passing variable values to a shortcut via the `triggerShortcut` now passes the correct values again, without an unwanted prefix
- Fixed incorrect error message of `wakeOnLan` function

### Miscellaneous
- When choosing an icon for a shortcut, it is now possible to select a custom icon that was previously used
- Added partial Japanese translation

## Version 2.10.0

### Important Changes
The minimum supported Android version is now at 5.0 (Lollipop). Going forward, older versions of Android will no longer receive updates. You can still install older versions of the app by [downloading the APK directly from GitHub](https://github.com/Waboodoo/HTTP-Shortcuts/releases).

Furthermore, this version introduces a breaking change to the Scripting feature, specifically to the built-in `hmac` and `base64decode` functions. The return type of these functions is now `Uint8Array` (i.e., an array of bytes) instead of a hex string. See the **Scripting** section below for more information.

### Scripting
- The `base64encode` and `hmac` functions now also accept `Uint8Array` (byte arrays) as input, instead of only strings
- The `hmac` and `base64decode` functions now return `Uint8Array` (byte arrays) instead of a hex encoded string
- There are 2 new functions `toString` and `toHexString` which allow to convert `Uint8Array` into strings
- The Code Snippet picker now also allows easily adding "if { }" and "if { } else { }" blocks

### Bugfixes:
- The response headers can now be properly accessed again via Scripting
- Form parameters can now have empty values

### Miscellaneous
- The "Misc Settings" section was renamed to "Trigger & Execution Settings"
- Some error messages, in particular around import & export, were improved
- The color picker dialog for `Color` variables was swapped out for a different, easier-to-use component

## Version 2.9.0

### Improvements
- Added `wakeOnLan` function to allow sending magic packets to wake up devices on the network
- When using Scripting, JavaScript errors now also include the line number for easier debugging
- The `triggerShortcut` function now supports a 3rd argument which can be used to specify a custom delay (in milliseconds)
- Multiple Choice variables can now be configured to allow selecting multiple options

### Bugfixes
- The `triggerShortcut` function can now be used to re-trigger the current shortcut itself, and do so indefinitely for as long as a delay larger than 500 milliseconds is specified.

### Miscellaneous
- Prepared the app for compatibility with Android 12

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
- It is now possible to trigger a specific shortcut by invoking a deep-link of the form `http-shortcuts://deep-link/<id-of-shortcut-here>`
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

- Shortcuts are removed from home screen when deleted in app
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
