# HTTP Shortcuts for Android
<img src="./HTTPShortcuts/app/src/main/res/drawable-xxxhdpi/ic_launcher.png" align="right" style="margin-left: 1em;"/>

A simple Android app that allows you to create shortcuts that can be placed on your home screen. Each shortcut, when clicked, triggers an HTTP request.

- GET, POST, PUT, DELETE and PATCH
- HTTP and HTTPS
- Basic Authentication & Digest Authentication
- Custom request headers
- Custom request body (text only)
- Option to accept all certificates (if you know what you're doing)
- Built-in icons, support for Ipack icons and custom icons
- Display response as a toast, window or dialog, or run silently
- Grouping into categories through tabs
- Value injection through variables (text, numbers, passwords, selections, colors, ...)
- Import & Export in JSON or cURL format
- 3rd party integration (e.g. Tasker)
- Themes

<a href="https://play.google.com/store/apps/details?id=ch.rmy.android.http_shortcuts">
<img alt="Get it on Google Play" src="http://steverichey.github.io/google-play-badge-svg/img/en_get.svg" width="280" />
</a>

## Screenshots

![Image](/Screenshots/main_screen_small.png)
![Image](/Screenshots/shortcut_options_small.png)
![Image](/Screenshots/editor_small.png)
![Image](/Screenshots/icons_small.png)
![Image](/Screenshots/headers_small.png)
![Image](/Screenshots/variable_small.png)
![Image](/Screenshots/variable_placeholder_small.png)


## Hints for Experts

### Inspect Network Requests
This app uses [Stetho](https://github.com/facebook/stetho), which means you can debug the internal state and the network requests that are sent:
1. Connect your phone to a PC with a USB cable
2. Open the app on the phone
3. Open Chrome on the PC
4. Go to chrome://inspect
5. Click the *inspect* button below "HTTP Shortcuts (powered by Stetho)"

### Bulk Creation / Modifying of Shortcuts
If you want to add or edit a lot of shortcuts at once it can be very cumbersome to do so through the app's UI. An easy solution is to go to the apps settings and use the export feature. It provides you with a JSON file that you can easily modify with any tool you want. Once you are done you can import it again and voila: you have all your changes in the app.


## Translations
- German: [Roland Meyer](https://github.com/Waboodoo)
- Chinese: [Kevin Mao](https://github.com/yuanrunmao)

Help me translate this app, feel free to open a pull request to add new languages or to fix translation mistakes.
