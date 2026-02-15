# Import & Export

The app allows you to export all of your shortcuts and variables into a zip file, which you can either store on your device directly or share into another app to send or upload it somewhere. You'll find these options by opening the dropdown menu on the app's main screen and selecting "Import / Export".

On the same screen you'll also find the option to import shortcuts from a file, either from the file system or from a URL.

You can also export a single shortcut by long pressing on it and selecting the "Export" option from the context menu.

## Global Variables

When exporting shortcuts, the app will automatically also export all of the global variables that are used by those shortcuts. If these global variables contain sensitive or device-specific information, you can configure them such that their value will be excluded from the export. This option can be found in the global variable editor.

<a id="sync"></a>
## Automatic Import / Export

The Import / Export Screen includes the option "Automatic Import / Export". With this feature, you can configure the app to periodically export all of your shortcuts or to import from a specific source. This is mainly useful to create periodic backups, or to sync configuration between devices by setting up one device to periodically export and all other devices to periodically import. The same as with the regular import and export feature, this feature reads or writes a zip file which contains all of your shortcuts, variables, categories, etc. It can optionally be protected with a password. As a source or destination, you can choose a file on your local file system or a URL on a web server, see below for details.

The feature can be configured to run daily or weekly, though there is no guarantee on when exactly the import or export will run. It can also be triggered manually from this screen.

### Import Strategies

When setting up automatic import, the first option you see is "Import Strategy".

The default strategy is "Merge with existing data". This strategy behaves the same way as the normal import feature, in that it will try to merge the imported data with the already existing data. This way, if you already have shortcuts, variables etc., they will be preserved and only modified if the imported data also includes them.

The other strategy is "Replace existing data". This should be used with care, as it can lead to loss of data. When automatic import is active with this strategy, the creation and editing of shortcuts, variables, etc. is disabled. Instead, whenever the automatic import runs, all local data will be deleted and replaced with the data from the import. This is mainly useful if you want your device to match the state of another one, where the other one is configured to use automatic export.

### Using the local file system

To use the local file system, select a directory and then provide the name of the file that should be used. For import, if no file name is provided, the app will use the most recently modified zip file in the directory. If no suitable file can be found, import will fail.

You may include the placeholders `%Y`, `%M`, and `%D` in the file name, which will be replaced with the current year, month, or day, respectively.

### Using a web server

You can provide a URL to a web server, where you want to import to or export from. This can be a WebDAV server, or any other server that accepts GET or PUT HTTP requests to read or write files. Optionally, you can provide a username and password, which will be used for Basic Auth to authenticate with the server.

You may include the placeholders `%Y`, `%M`, and `%D` in the URL, which will be replaced with the current year, month, or day, respectively.

<a id="remote-edit"></a>
## Remote Editing

On the Import / Export Screen you'll also find the "Edit on Computer" option. It allows you to temporarily upload all of your shortcuts to a remote server and then conveniently edit them from a computer via the [Web Editor](https://http-shortcuts.rmy.ch/editor), and then download the changes back into the app. This way you can avoid doing tedious amounts of typing on your phone.

<a id="import-deep-linking"></a>
## Hosting Templates for Easy Importing
If you want to share your shortcuts as a template or as part of a tutorial, you can allow others to easily import them by uploading the exported zip file somewhere on your website (or any publicly accessible host) and then constructing a deep-link URL using the following format:

```
https://http-shortcuts.rmy.ch/import?url=[URL]
```

where `[URL]` is a URL-encoded version of the URL where your exported file can be downloaded from. You can then include this deep-link URL on your website so that people can click on it to immediately import your shortcuts into their app.
