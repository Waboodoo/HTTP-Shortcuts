# FAQ

## This app is free and contains no ads. What's the catch?

There is no catch. I'm a developer who one day realized he needed an app like this, built it, and then decided to share it. HTTP Shortcuts is essentially a one-man show, and I'm only working on it in my spare time. I'm doing it because I enjoy the project, which is why the app is completely free and will remain so. No ads, no tracking, no premium features, just a simple open-source app that aims to be useful.

If you want to show your support though, check out [this page](https://http-shortcuts.rmy.ch/support-me).

## Running shortcuts works from within the app, but not from the home screen. How do I fix it?

This usually happens when Data Saver or Battery Saver is enabled, as those restrict how apps can use the network. Try disabling them or whitelisting the HTTP Shortcuts app.

It might also be that you need to enable the "Allow drawing over other apps" option. You'll find it in the app's Settings page in the Troubleshooting section.

## I don't like the blue arrow icon that overlays all my shortcuts on the home screen. Can I remove it?

Unfortunately, this icon overlay is added by the Android system itself, not the app. There is a potential workaround though. Try adding a shortcut via your home screen's widget menu (usually accessed by long pressing on the home screen), and when prompted by the app about which method to use for placement, select the *Legacy* option. Please note that this may not always work, and if it doesn't then there really is no way to remove the icon overlay. Also note that this will prevent you from dynamically changing the name or icon of the shortcut, i.e., you'll need to remove and re-add it to the home screen manually if you change its name or icon.

Alternatively, you can use the *"Customizable Widget"* from your home screen's widgets menu. This widget looks a bit different from a regular shortcut and offers a few customization options.

<a name="trigger-from-other-app"></a>
## Can I trigger a shortcut from another app?

Most automation apps offer some way to trigger a shortcut directly. If that isn't an option you can instead trigger a shortcut by sending a *broadcast intent* with the following parameters:

- action: "ch.rmy.android.http_shortcuts.execute"
- package name: "ch.rmy.android.http_shortcuts"
- string extra "id" which holds the ID* of the shortcut you want to trigger

Alternatively, you can invoke a shortcut via a deep-linking URL, which is particularly useful when you want to trigger a shortcut from a QR code or an NFC tag.

\* You'll find the shortcut's ID as well as its deep-linking URL by long-pressing the shortcut and selecting *Show Info* from the menu.

See also the [documentation on deep-linking](advanced.md#deep-link).

## Can I send multiple requests with one shortcut?

A normal shortcut corresponds to a single request. You can, however, have one shortcut trigger one or more other shortcuts. The easiest way to achieve that is by creating a ["Multi-Shortcut"](shortcuts.md#multi-shortcut), which allows you to pick one or more shortcuts which are then all triggered when the multi-shortcut itself is executed, one after the other. 

In some cases using a Multi Shortcut might not be enough, e.g., when you want to trigger the same shortcut twice, want to trigger it only under certain conditions or if you want to pass variable values to it. In this case you can create a ["Scripting Shortcut"](shortcuts.md#scripting-shortcut) instead and add one or more instances of the ["enqueueShortcut"](scripting.md#trigger-shortcut) action to it. When creating or editing your scripting shortcut, open the section "Scripting" and then click the button "Add Code Snippet" underneath the textarea. In the dialog that opens select "Miscellaneous" and then "Enqueue Shortcut". This way, whenever you run your first shortcut, it will trigger the selected other one after it completed. See [the Scripting documentation](scripting.md#trigger-shortcut) for more information.

## Can I schedule requests to be sent periodically or at a specific time?

Currently the app only has basic functionality for running a shortcut repeatedly. When creating or editing the shortcut, go to the *Trigger & Execution Settings* section and look for the *Run repeatedly* dropdown at the bottom.

If you need more advanced or more precise scheduling, you can try to achieve that by combining the app with an automation app, such as Tasker or Macrodroid.

## Can I trigger a shortcut with text shared from another app? Can I share text (e.g. a link) into a shortcut?

If you want to share text via an HTTP shortcut, you can do so like this:

1. Open the app
2. Open the dropdown menu at the top right and select *Variables*
3. Click the + button and select *Static Variable* as the variable type
4. Enter a name for the variable
5. **Tick the *Allow 'Share...'* checkbox
6. From the dropdown menu that appears below select which part of the shared text you want to handle: text, title, or both
7. Click the checkmark button at the top right to save your variable
8. Go back to the app's main screen
9. Click the + button to start creating a new shortcut or long press an existing shortcut and select *Edit* to open the shortcut editor
10. Find the input field for the place where you want to share the text as, e.g. the URL, the request body or a header. Click the *{}* button next to that field
11. Select the variable that you created earlier
12. Save the changes to your shortcut
13. You should now be able to share text from other apps (e.g. a URL from a browser) into the HTTP Shortcuts app and there select your shortcut as the share target. It will execute the shortcut and insert the shared text into where you put the variable placeholder.

## Can I share files into a shortcut's request body?

Yes, you can. You'll find information about this on the [advanced features](advanced.md#share-files) page.

## Can I pass values from one shortcut to another?

Yes, you can. To do so, you need to first create a [global variable](variables.md) (of static type) to hold the value. You can then use the [Scripting](scripting.md) feature to store a value into that variable from one of your shortcuts and then use or read out the value again in the other shortcut. To store a value into a variable, use the [setVariable](scripting.md#variables) function.

If you use [executeShortcut](scripting.md#execute-shortcut) to call another shortcut, you can also use the [setResult](scripting.md#set-result) function to pass data back to the calling shortcut.

## How do I pass data from Tasker to HTTP Shortcuts?

See the guide on [integrating with Tasker](advanced.md#integrate-with-tasker).

<a name="debugging"></a>
## Something's not working with my requests. Can I get more detailed information for debugging?
The easiest way to get more details about the shortcuts that you're executing in the app is by going to the *Event History* screen. You will find it in the app's main menu. The Event History shows all recently triggered shortcuts, the HTTP requests that were sent out and the HTTP responses that were received, as well as all the (network) errors that have occurred.

Another way to get more information about the request and the response is by opening the *Response Handling* section when editing a shortcut and changing the *Display Type* to *Window* and then ticking the *Show Meta Information* checkbox. This will display the full response in a window, along with all response headers and some additional meta information.

<a name="infinite-loops"></a>
## I accidentally created an infinite loop of shortcuts triggering other shortcuts, how do I stop it?
First, force stop the app. Then, assuming you're viewing this page in a browser on the same device where you have the app installed, click this link: <a href="http-shortcuts://cancel-executions">CANCEL ALL EXECUTIONS</a>. The link will open the app but in a safe mode, where all scheduled shortcut executions are cancelled.

## What about voice recognition?

There is currently no support for voice recognition in HTTP Shortcuts, with the exception of a very basic and experimental feature. Try "OK Google, search [Name of Shortcut] in HTTP Shortcuts".

## I would like to help translate the app. How can I contribute?

First of all, thank you for even considering this. I appreciate the effort. You can join the translation project here: [HTTP Shortcuts on poeditor.com](https://poeditor.com/join/project/8tHhwOTzVZ)

## Is this app also available on iOS?

No, this app only exists for Android.

## I've sent an email with a question / bug report, but haven't heard back anything. What gives?

I'm just one guy developing this app in my free time. As such, I don't regularly read my emails or respond to them right away. Sometimes it may take weeks. Sorry about that. Most likely I'll get back to you eventually. Please be patient with me.

## I love this app. How can I show my support?

First of all, thank you. Second of all: I've created a [page](https://http-shortcuts.rmy.ch/support-me) with a list of ways in which you can support this app.

