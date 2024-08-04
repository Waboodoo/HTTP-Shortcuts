# Import & Export

The app allows you to export all of your shortcuts and variables into a zip file, which you can either store on your device directly or share into another app to send or upload it somewhere. You'll find these options by opening the dropdown menu on the app's main screen and selecting "Import / Export".

On the same screen you'll also find the option to import shortcuts from a file, either from the file system or from a URL.

You can also export a single shortcut by long pressing on it and selecting the "Export" option from the context menu.

## Variables

When exporting shortcuts, the app will automatically also export all of the variables that are used by those shortcuts. If these variables contain sensitive or device-specific information, you can configure them such that their value will be excluded from the export. This option can be found in the variable editor.

<a name="remote-edit"></a>
## Remote Editing

On the Import / Export Screen you'll also find the "Edit on Computer" option. It allows you to temporarily upload all of your shortcuts to a remote server and then conveniently edit them from a computer via the [Web Editor](https://http-shortcuts.rmy.ch/editor), and then download the changes back into the app. This way you can avoid doing tedious amounts of typing on your phone.

<a name="import-deep-linking"></a>
## Hosting Templates for Easy Importing
If you want to share your shortcuts as a template or as part of a tutorial, you can allow others to easily import them by uploading the exported zip file somewhere on your website (or any publicly accessible host) and then constructing a deep-link URL using the following format:

```
https://http-shortcuts.rmy.ch/import?url=[URL]
```

where `[URL]` is a URL-encoded version of the URL where your exported file can be downloaded from. You can then include this deep-link URL on your website so that people can click on it to immediately import your shortcuts into their app.
