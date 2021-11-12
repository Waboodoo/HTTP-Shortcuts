# FAQ

## This app is free and contains no ads. What's the catch?

There is no catch. I'm a developer who one day realized he needed an app like this, built it, and then decided to share it. HTTP Shortcuts is essentially a one-man show, and I'm only working on it in my spare time. I'm doing it because I enjoy the project, which is why the app is completely free and will remain so. No ads, no tracking, no premium features, just a simple open-source app that aims to be useful.

## Running shortcuts works from within the app, but not from the homescreen. How do I fix it?

This usually happens when Data Saver or Battery Saver is enabled, as those restrict how apps can use the network. Try disabling them or whitelisting the HTTP Shortcuts app.

You may also try enabling the *Run Requests in Foreground* option which you find on the *Settings* screen.

## I don't like the blue arrow icon that overlays all my shortcuts on the home screen. Can I remove it?

Unfortunately, this icon overlay is added by the Android system itself, not the app. There is a potential workaround though. Try adding a shortcut via your home screen's widget menu (usually accessed by long pressing on the home screen), and when prompted by the app about which method to use for placement, select the "Legacy" option. Please note that this may not always work, and if it doesn't then there really is no way to remove the icon overlay. Also note that this will prevent you from dynamically changing the name or icon of the shortcut, i.e., you'll need to remove and re-add it to the homescreen manually if you change its name or icon.

<a name="trigger-from-other-app"></a>
## Can I trigger a shortcut from another app?

Most automation apps offer some way to trigger a shortcut directly. If that isn't an option you can instead trigger a shortcut by sending a *broadcast intent* with the following parameters:

- action: "ch.rmy.android.http_shortcuts.execute"
- package name: "ch.rmy.android.http_shortcuts"
- string extra "id" which holds the ID* of the shortcut you want to trigger

Alternatively, you can invoke a shortcut via a deeplinking URL, which is particularly useful when you want to trigger a shortcut from a QR code or an NFC tag.

\* You'll find the shortcut's ID (as well as its deeplinking URL) by long-pressing the shortcut and selecting *Show Info*.

## Can I send multiple requests with one shortcut?

A normal shortcut corresponds to a single request. You can, however, have one shortcut trigger one or more other shortcuts. The easiest way to achieve that is by creating a "Multi-Shortcut", which allows you to pick one or more shortcuts which are then all triggered when the multi-shortcut itself is executed. 

In some cases using a multi-shortcut might not be enough, e.g., when you want to trigger the same shortcut twice, want to trigger it only under certain conditions or if you want to pass variable values to it. In this case you can create a "Scripting" shortcut instead and add one or more instances of the "triggerShortcut" action to it. When creating or editing your scripting shortcut, open the section "Scripting" and then click the button "Add Code Snippet" underneath the textarea. In the dialog that opens select "Actions" and then "Trigger Shortcut". This way, whenever you run your first shortcut, it will trigger the selected other one after it completed. See [Code Execution](scripting.md#trigger-shortcut) for more information.

## Can I schedule requests to be sent periodically or at a specific time?

Currently there is no such functionality in the app. There are already many existing automation apps that can be used for scheduling and combined with the HTTP Shortcuts app. I recommend to use one of those; Tasker, IFTTT, E-Robot, Macrodroid, to name a few.

## How do I pass variables from Tasker to HTTP Shortcuts?

You can use [Tasker](https://play.google.com/store/apps/details?id=net.dinglisch.android.taskerm) to trigger a shortcut. To pass a value from Tasker to HTTP Shortcuts you need to create a variable of type "Static Variable (Constant)" in HTTP Shortcuts and a global variable with the same name in Tasker. Make sure to do so BEFORE you select the shortcut from Tasker. All global variables that have matching variables in HTTP Shortcuts are automatically passed over.

<a name="debugging"></a>
## Something's not working with my requests. Can I get more detailed information for debugging?
One way to get more information about the request and the response is by opening the *"Reponse Handling"* section when editing a shortcut and changing the *"Display Type"* to *"Window"* and then ticking the *"Show Meta Information"* checkbox. This will display the full response in a window, along with all response headers and some additional meta information.

In addition, there is the option to use [Stetho](https://github.com/facebook/stetho), which means you can debug the internal state and the network requests that are sent by following these steps:
1. Connect your phone to a PC with a USB cable
2. Open the app on the phone
3. Open Chrome on the PC
4. Go to chrome://inspect
5. Click the *inspect* button below "HTTP Shortcuts (powered by Stetho)"

## What about voice recognition?

There is currently no support for voice recognition in HTTP Shortcuts, with the exception of a very basic and experimental feature. Try "OK Google, search [Name of Shortcut] in HTTP Shortcuts".

## I would like to help translate the app. How can I contribute?

First of all, thank you for even considering this. I appreciate the effort. You can join the translation project here: [HTTP Shortcuts on poeditor.com](https://poeditor.com/join/project/8tHhwOTzVZ)

## Is this app also available on iOS?

No, this app only exists for Android.

## I've sent an email with a question / bug report, but haven't heard back anything. What gives?

I'm just one guy developing this app in my free time. As such, I don't regularly read my emails or respond to them right away. Sometimes it may take weeks. Sorry about that. Most likely I'll get back to you eventually. Please be patient with me.

