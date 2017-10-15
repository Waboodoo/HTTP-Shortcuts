# HTTP Shortcuts for Android
<img src="./HTTPShortcuts/app/src/main/res/drawable-xxxhdpi/ic_launcher.png" align="right" style="margin-left: 1em;"/>

![Version](https://img.shields.io/badge/version-1.17.6-green.svg)

A simple Android app that allows you to create shortcuts that can be placed on your home screen. Each shortcut, when clicked, triggers an HTTP request.

<a href="https://play.google.com/store/apps/details?id=ch.rmy.android.http_shortcuts">
<img alt="Get it on Google Play" src="http://steverichey.github.io/google-play-badge-svg/img/en_get.svg" width="280" />
</a>

## Features
- HTTP and HTTPS
- GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS and TRACE
- Basic Authentication & Digest Authentication
- Custom request headers
- Custom request body (text only)
- Option to accept all certificates (if you know what you're doing)
- Built-in icons, support for Ipack icons and custom icons
- Display response as a toast, window or dialog, or run silently
- Grouping into categories through tabs
- Value injection through variables (text, numbers, passwords, selections, colors, dates, ...)
- Import & Export in JSON or cURL format
- 3rd party integration (e.g. Tasker)
- Themes


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

### Customize look-and-feel of shortcuts
As opposed to Android's home screen widgets, home screen shortcuts are very limitted in how they can be customized, i.e. only icon and label can be specified. If you wish to change the look-and-feel of your shortcuts beyond that, I recommend you have a look at some third-party apps that allow you to create custom widgets. I recommend [Elixir 2 - Widgets](https://play.google.com/store/apps/details?id=com.bartat.android.elixir.widget&hl=en), which I personally use to combine multiple shortcuts into a nice looking widget.

## Acknowledgments

### Features
- Adding basic voice command support: [Adrián López](https://github.com/adrianlzt)
- Improving quality of generated icons: [idmadj](https://github.com/idmadj)

### Translations
- Brazilian Portuguese: [Eduardo Folly](https://github.com/idmadj)
- Chinese: [Kevin Mao](https://github.com/yuanrunmao)
- French: [Flavien](https://github.com/Flavien06)
- German: [Roland Meyer](https://github.com/Waboodoo)

Help me translate this app, feel free to open a pull request to add new languages or to fix translation mistakes.
