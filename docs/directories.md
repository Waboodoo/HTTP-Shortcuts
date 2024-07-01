# (Mounted) Directories

Some features in the app allow interaction with your device's file system. For example, you can configure your shortcuts to store the response into a file (via the "Response Handling" screen). For this, the app needs write access to a directory, which is where the "(Mounted) Directories" screen comes in. Here, you can *mount* a directory, i.e., create a connection to a specific existing directory on your device and granting the app the permission to read and write files in it.

In the future, there might be more features that make use of this, but for now it's just for the storing of HTTP responses.

You can revoke access by deleting the directory from this screen. This will not affect the real directory or its contents, only the connection to it.

