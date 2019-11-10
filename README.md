# HTTP Shortcuts for Android
<img src="./HTTPShortcuts/app/src/main/res/drawable-xxxhdpi/ic_launcher.png" align="right" style="margin-left: 1em;"/>

![Version](https://img.shields.io/badge/version-1.25.0-green.svg)

A simple Android app that allows you to create shortcuts that can be placed on your home screen. Each shortcut, when clicked, triggers an HTTP request.

<a href="https://play.google.com/store/apps/details?id=ch.rmy.android.http_shortcuts">
<img alt="Get it on Google Play" src="http://steverichey.github.io/google-play-badge-svg/img/en_get.svg" width="280" />
</a>

Get it on the [Play Store](https://play.google.com/store/apps/details?id=ch.rmy.android.http_shortcuts) or [download the latest APK](https://github.com/Waboodoo/HTTP-Shortcuts/releases/download/v1.22.0/app-release.apk) directly.


Help me translate this app! Feel free to open a pull request to add new languages or to fix translation mistakes. Or contact me and I can grant you access to the translation tool.

Become a [Beta Tester](https://play.google.com/apps/testing/ch.rmy.android.http_shortcuts)!

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
- Value injection through variables (e.g., text, numbers, passwords, selections, colors, dates, …)
- Run arbitrary JavaScript before & after execution, including special actions (e.g., vibrate, show toast, parse and process response…), 
- Import & Export in JSON or cURL format
- 3rd party integration (e.g. Tasker)
- Themes (with Dark Mode support)


## Screenshots

![Image](/Screenshots/1.png)
![Image](/Screenshots/2.png)
![Image](/Screenshots/3.png)
![Image](/Screenshots/4.png)
![Image](/Screenshots/5.png)
![Image](/Screenshots/6.png)
![Image](/Screenshots/7.png)
![Image](/Screenshots/8.png)

## FAQ

Check out the [FAQ](http://waboodoo.ch/http-shortcuts/#faq) page.

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

### Voice Support
The app has very basic voice support, though only experimental at this point. Try "Ok Google, search \[Name of Shortcut\] in HTTP Shortcuts".

## Acknowledgments

### Features
- Adding basic voice command support: [Adrián López](https://github.com/adrianlzt)
- Improving quality of generated icons: [idmadj](https://github.com/idmadj)

### Translations
- Brazilian Portuguese: [Eduardo Folly](https://github.com/edufolly)
- Chinese: [Kevin Mao](https://github.com/yuanrunmao)
- French: [Flavien](https://github.com/Flavien06)
- German: [Roland Meyer](https://github.com/Waboodoo)
- Italian: [Flavio Barisi](https://github.com/flavio20002), [Roberto Bellingeri](https://github.com/bellingeri)
- Norwegian (Bokmål): [Imre Kristoffer Eilertsen](https://github.com/DandelionSprout)
- Persian: [Amir Motahari](https://github.com/a-motahari)
- Spanish: [iprockbyte](https://github.com/iprockbyte)
- Turkish: [Bilal Bayrak](https://github.com/bayrakbilal)
