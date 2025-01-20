# Introduction

## What is HTTP?

HTTP (**H**yper**t**ext **T**ransfer **P**rotocol) is a widely used protocol which forms the foundation of the web as we know it. It revolves around the concept of requesting resources (e.g. pages of a website or endpoints in a REST API), identified by a URL, from a server.

This app assumes that you're already familiar with the basics of the protocol, so in case you're not I suggest you get yourself an [overview](https://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol) first.

## What is this app about?

The main goal of this app is to provide an easy way to send HTTP requests from an Android device. It achieves this by allowing you to create so called [shortcuts](shortcuts.md) which can be clicked to trigger such a request, either from within the app directly or via a widget placed on your home screen.

Shortcuts can be grouped together into different [categories](categories.md) which are shown as separate tabs inside the app.

In some cases you want to do more than just send a simple HTTP request but need more powerful tools for advanced workflows. This is where [variables](variables.md) come in, which allow you to dynamically inject values into your request, and the [scripting](scripting.md) capabilities of the app, which allow you to use JavaScript code snippets to further customize how your shortcuts behave.

## Creating your first shortcut

To get started, click the "+" button on the app's main screen. On the screen that appears select the type of shortcut you want to create. More on this [here](shortcuts.md), but for now let's just go with a "Regular HTTP Shortcut" by selecting the "Create from scratch" option.

You are now in the shortcut editor. Here you can enter a name for your shortcut and optionally a short description. By clicking on the icon to the right of the name field you can choose an icon for your shortcut.

At the very least you need to provide a URL. To do this, click on "Basic Request Settings". On the screen that opens you can also select the HTTP method. By default it will be set to GET. Now you can go back and test your shortcut by clicking the play button or saving it by clicking the checkmark button.

You can also further configure it by checking out the options in the other sections further below. I encourage you to have a look around and try out different options.

Once you are happy with your shortcut you can save all changes and return to the main screen. You can now trigger your shortcut from here or choose to place it on the home screen. By long-pressing it, you open the menu with additional options.

