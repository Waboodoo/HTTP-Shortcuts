# Shortcuts

The *HTTP Shortcuts* app allows you to create *shortcuts* that represent an action, resource or workflow. They can be invoked (executed) by clicking them, either inside the app itself, or after placing them on your device's home screen as widgets.

Get started by clicking the *plus button* at the bottom right when opening the app and pick which type of shortcut you want to create. For each shortcut you can set a name, an icon and a description. Depending on the type, there's a number of additional options.

Once you've create a shortcut, it will appear on the app's main screen. You can long-press it to open its context menu, which includes options such as editing, exporting, deleting, etc.

![Start creating a shortcut by picking a type](../assets/documentation/shortcuts/01.png)

<a name="regular"></a>
## Regular HTTP Shortcuts

The main purpose of the app is to make it easy to send HTTP requests, so the main type of shortcut is a regular HTTP shortcut. For this at the very least you need to specify an HTTP method (e.g. GET, POST, PUT, ...) and the URL that you want to reach (e.g. an API endpoint, a hosted text document or a website).

You will also find a number of options, such as custom request headers, request body settings, or how the response should be displayed. You will also find more advanced features like proxy settings or [scripting](scripting.md).

See the [Execution Flow](execution-flow.md) page for more information on how a shortcut is executed.

<a name="curl-import"></a>
### Import from cURL

Instead of starting from scratch you can use the *"Import from cURL"* option to type or paste an existing cURL command which then serves as a template for your shortcut.

## Other Types of Shortcuts

In some cases you need to create shortcuts that don't correspond to a single HTTP request but instead need to perform a different workflow.

<a name="multi-shortcut"></a>
### Multi-Shortcut

A *multi-shortcut* combines multiple shortcuts into one. When the multi-shortcut is executed it will trigger all of the shortcuts that are linked to it in sequence.

<a name="browser-shortcut"></a>
### Browser Shortcut

A *browser shortcut* is similar to a regular HTTP shortcut in that it corresponds to a single URL, but instead of making an HTTP request directly to that URL inside the app, it will open the URL in your device's browser instead.

<a name="scripting-shortcut"></a>
### Scripting Shortcut

A *scripting shortcut* allows you to run arbitrary JavaScript code, also known as [scripting](scripting.md).

