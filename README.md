# HTTP Shortcuts for Android
<a href="https://http-shortcuts.rmy.ch/">
<img src="/assets/logo.png" style="margin-left: 1em; float: right" alt="HTTP Shortcuts"/>
</a>

[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/Waboodoo/HTTP-Shortcuts)](https://github.com/Waboodoo/HTTP-Shortcuts/releases)
[![F-Droid](https://img.shields.io/f-droid/v/ch.rmy.android.http_shortcuts)](https://f-droid.org/en/packages/ch.rmy.android.http_shortcuts/)
![Downloads](https://PlayBadges.pavi2410.me/badge/downloads?id=ch.rmy.android.http_shortcuts&pretty)
![Ratings](https://PlayBadges.pavi2410.me/badge/ratings?id=ch.rmy.android.http_shortcuts)
[![Crowdin](https://badges.crowdin.net/http-shortcuts/localized.svg)](https://crowdin.com/project/http-shortcuts)

A simple Android app that allows you to create shortcuts that can be placed on your home screen. Each shortcut, when clicked, triggers an HTTP request, with the possibility to process and display the response in various ways.

<a href="https://play.google.com/store/apps/details?id=ch.rmy.android.http_shortcuts"><img alt="Get it on Google Play" src="/assets/play_store.svg" height="80" /></a>
<a href="https://f-droid.org/en/packages/ch.rmy.android.http_shortcuts/"><img alt="Get it on F-Droid" src="/assets/f_droid.svg" height="80" /></a>


or [download the latest APK](https://github.com/Waboodoo/HTTP-Shortcuts/releases) directly.

Find more information and documentation on the [official website](https://http-shortcuts.rmy.ch/).

[Support this app](https://http-shortcuts.rmy.ch/support-me) by helping me [translate it](https://crowdin.com/project/http-shortcuts) or by becoming a [Beta Tester](https://play.google.com/apps/testing/ch.rmy.android.http_shortcuts)!

## Features
- Supports HTTP and HTTPS, and methods GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS and TRACE
- Basic Authentication, Digest Authentication & Bearer Authentication
- Custom request headers
- Custom request body (text based, from static file, file picker, static, or camera)
- Upload files as form-data parameters
- Client Certificate Authentication
- Support for TLSv1.3
- Support for HTTP & SOCKS proxies
- Support for Self-Signed Certificates & Certificate Pinning
- Use a built-in icon or a custom image
- Display response as a toast, window or dialog, or run silently
- Display response as HTML-formatted text, as image or as web page
- [Grouping into tabs and section](https://http-shortcuts.rmy.ch/categories)
- Value injection through variables (e.g., text, numbers, passwords, selections, colors, dates, …)
- [Import & Export](https://http-shortcuts.rmy.ch/import-export) in JSON or cURL format
- 3rd party integration support (e.g. Tasker, Macrodroid or Wireguard)
- Invoke from home screen widgets, quick settings tiles, or quick access device controls
- Dark Mode support
- Easy remote editing on a computer via the [web editor](https://http-shortcuts.rmy.ch/import-export#remote-edit)
- Event history to see details about recently executed HTTP requests & responses
- Run arbitrary [JavaScript](https://http-shortcuts.rmy.ch/scripting) before & after execution, which allows for lots of customization with features such as:
  - compute values such as timestamps, random numbers, UUIDs, hashes, HMACs, base64, etc.
  - parse JSON, HTML or XML and extract data from it
  - show toast message or message dialogs
  - vibrate or play notification sounds
  - trigger other shortcuts to chain multiple HTTP requests 
  - interact with other devices and services by sending TCP or UDP packets, MQTT messages or using Wake-on-LAN
- Support for non-HTTP use-cases:
  - [Browser Shortcuts](https://http-shortcuts.rmy.ch/shortcuts#browser-shortcut) allow opening a URL in a browser
  - [MQTT Shortcuts](https://http-shortcuts.rmy.ch/shortcuts#mqtt-shortcut) allow sending MQTT messages
  - [Wake-on-LAN Shortcuts](https://http-shortcuts.rmy.ch/shortcuts#wake-on-lan) allow waking up a device on the network
  - [Scripting Shortcuts](https://http-shortcuts.rmy.ch/shortcuts#scripting-shortcut) allow defining custom logic using JavaScript code

See also the [Changelog](CHANGELOG.md) for the latest additions and updates.

## Screenshots
![Main Screen](/assets/screenshots/01.png)
![Creation Menu](/assets/screenshots/02.png)
![Shortcut Editor](/assets/screenshots/03.png)
![Built-In Icons](/assets/screenshots/04.png)
![Authentication Settings](/assets/screenshots/05.png)
![Scripting](/assets/screenshots/06.png)
![Code Snippet Picker](/assets/screenshots/07.png)
![Categories](/assets/screenshots/08.png)
![Variables](/assets/screenshots/09.png)
![Variable Editor](/assets/screenshots/10.png)
![Settings](/assets/screenshots/11.png)
![Import / Export](/assets/screenshots/12.png)
![Display Response with Meta Data](/assets/screenshots/13.png)

