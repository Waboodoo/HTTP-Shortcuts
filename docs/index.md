A simple but powerful Android app that allows you to create shortcuts and widgets that can be placed on your home screen. Each shortcut, when clicked, triggers an HTTP request, with the possibility of processing and displaying the response in various ways. Ideal for home automation projects.

<a href="https://play.google.com/store/apps/details?id=ch.rmy.android.http_shortcuts"><img alt="Get it on Google Play" src="../assets/play_store.svg" height="80" style="margin-right: 20px"></a><a href="https://f-droid.org/en/packages/ch.rmy.android.http_shortcuts/"><img alt="Get it on F-Droid" src="../assets/f_droid.svg" height="80" style="margin-right: 20px"></a><a href="https://github.com/Waboodoo/HTTP-Shortcuts/releases"><img alt="Get it on GitHub" src="../assets/github.svg" height="80"></a>

This app is open-source and can be found on [GitHub](https://github.com/Waboodoo/HTTP-Shortcuts).
If you find bugs, have questions or feature requests, feel free to [contact me](https://http-shortcuts.rmy.ch/contact).

If you like this app, please consider [supporting my work](https://http-shortcuts.rmy.ch/support-me) on it. Thank you.

## Features
- Completely free, open-source, no ads and [(almost) no tracking](privacy-policy.md)
- Supports HTTP and HTTPS (TLSv1.3), and methods GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS and TRACE
- Basic Authentication, Digest Authentication, Bearer Authentication and Client Certificate Authentication
- Custom request headers
- Custom request body (text based, from static file, file picker, static, or camera)
- Upload files as form-data parameters
- Display response as a toast, window or dialog, or run silently
- Store response into a file
- Support for self-signed certificates & Certificate Pinning
- Big set of colorful built-in icons, support for custom icons
- Dark Mode support
- Value injection through [global variables](variables.md) with dynamically determined values (e.g., text, numbers, passwords, selections, colors, dates, …)
- Run arbitrary [JavaScript](scripting.md) before & after execution, which allows for lots of customization with features such as:
  - compute values such as timestamps, random numbers, UUIDs, hashes, HMACs, base64, etc.
  - parse JSON, HTML or XML and extract data from it
  - show toast message or message dialogs
  - vibrate or play notification sounds
  - read and write files
  - trigger other shortcuts to chain multiple HTTP requests 
  - interact with other devices and services by sending TCP or UDP packets, MQTT messages or using Wake-on-LAN
- Support for non-HTTP use-cases:
  - [Browser Shortcuts](shortcuts.md#browser-shortcut) allow opening a URL in a browser
  - [MQTT Shortcuts](shortcuts.md#mqtt-shortcut) allow sending MQTT messages
  - [Wake-on-LAN Shortcuts](shortcuts.md#wake-on-lan) allow waking up a device on the network
  - [Scripting Shortcuts](shortcuts.md#scripting-shortcut) allow defining custom logic using JavaScript code
- Keep everything organized by categorizing into different [tabs and grouping into sections](categories.md)
- Import & Export in JSON or cURL format
- 3rd party integration (e.g. Tasker, Wireguard)
- Configure it from your browser using the [Web Editor](https://http-shortcuts.rmy.ch/editor)
- Runs on Android 8 (Oreo) or newer.
  - For Android 6 or 7, download and [install the APK of version 3.21.0](https://github.com/Waboodoo/HTTP-Shortcuts/releases/tag/v3.21.0).
  - For Android 5, download and [install the APK of version 3.14.0](https://github.com/Waboodoo/HTTP-Shortcuts/releases/tag/v3.14.0).
  - For Android 4.0 - Android 4.4.4, download and [install the APK of version 2.9.0](https://github.com/Waboodoo/HTTP-Shortcuts/releases/tag/v2.9.0).

## Screenshots
![Main Screen](../assets/screenshots/01.png)
![Creation Menu](../assets/screenshots/02.png)
![Shortcut Editor](../assets/screenshots/03.png)
![Built-In Icons](../assets/screenshots/04.png)
![Authentication Settings](../assets/screenshots/05.png)
![Scripting](../assets/screenshots/06.png)
![Code Snippet Picker](../assets/screenshots/07.png)
![Categories](../assets/screenshots/08.png)
![Variables](../assets/screenshots/09.png)
![Variable Editor](../assets/screenshots/10.png)
![Settings](../assets/screenshots/11.png)
![Import / Export](../assets/screenshots/12.png)
![Display Response with Meta Data](../assets/screenshots/13.png)
