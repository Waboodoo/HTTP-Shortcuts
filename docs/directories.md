# (Mounted) Directories

Some features in the app allow interaction with your device's file system. For example, you can configure your shortcuts to store the response into a file (via the "Response Handling" screen). For this, the app needs write access to a directory, which is where the "(Mounted) Directories" screen comes in. Here, you can *mount* a directory, i.e., create a connection to a specific existing directory on your device and granting the app the permission to read and write files in it.

You can revoke access by deleting the directory from this screen. This will not affect the real directory or its contents, only the connection to it.

Mounted directories can be used for the following:
- Store the response of an HTTP request into a file. This can be configured on the "Response Handling" screen when editing a shortcut.
- [Read and write files](scripting.md#read-write-files) using the Scripting feature.

