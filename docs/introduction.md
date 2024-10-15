# Introduction

## What is HTTP?

HTTP (**H**yper**t**ext **T**ransfer **P**rotocol) is a widely used protocol which forms the foundation of the web as we know it. It revolves around the concept of requesting resources (e.g. pages of a website or endpoints in a REST API), identified by a URL, from a server.

This app assumes that you're already familiar with the basics of the protocol, so in case you're not I suggest you get yourself an [overview](https://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol) first.

## What is this app about?

The main goal of this app is to provide an easy way to send HTTP requests from an Android device. It achieves this by allowing you to create so called [shortcuts](shortcuts.md) which can be clicked to trigger such a request, either from within the app directly or via a widget placed on your home screen.

Shortcuts can be grouped together into different [categories](categories.md) which are shown as separate tabs inside the app.

In some cases you want to do more than just send a simple HTTP request but need more powerful tools for advanced workflows. This is where [variables](variables.md) come in, which allow you to dynamically inject values into your request, and the [scripting](scripting.md) capabilities of the app, which allow you to use JavaScript code snippets to further customize how your shortcuts behave.
