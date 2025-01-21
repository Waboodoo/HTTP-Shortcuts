# Permissions
This page lists all the permissions that the app makes use of and what it uses them for.

## have full network access
Needed to make HTTP requests over the network (duh). See the [Privacy Policy](privacy-policy.md) for details on what data is sent over the network.

## install shortcuts
Needed to install shortcuts on the home screen.

## uninstall shortcuts
Needed to remove shortcuts from the home screen when a shortcut is deleted.

## view Wi-Fi connections
Needed to inspect the current Wi-Fi network's name, as well as prompting the user to select a different network to connect to in case they configured a shortcut to only use a specific network.

## view network connections
Needed to determine whether a suitable network connection is available to make HTTP requests.

## connect and disconnect from Wi-Fi
Needed for prompting the user to select a different network to connect to in case they configured a shortcut to only use a specific network. The app never switches the Wi-Fi network without the user's input.

## access approximate location only in the foreground
See next section.

## access precise location only in the foreground
This is used for 2 specific features only, both of which are non-essential and completely optional to use:

- Checking the SSID of the Wi-Fi that the device is currently connected to. This allows a shortcut to only run on a specific network, and it allows the use of the [`getWifiSsid()`](scripting.md#get-wifi-ssid) function in Scripting. Neither of these two features make direct use of the device's physical location information and only access the SSID.
- Using the [`getLocation()`](scripting.md#get-location) function for Scripting, which allows to programmatically look up the device's current location.

Unless configured so explicitly by the user through the use of [Scripting](scripting.md) or [Variables](variables.md), the Wi-Fi or location information never leaves the device and is only stored in memory, never persisted to disk.

## control vibration
Used by the [`vibrate()`][scripting.md#vibrate) function, which allows the device to vibrate via Scripting.

## run foreground service
Used to perform cleanup tasks, such as deleting obsolete cache files after a shortcut was run.

## This app can appear on top of other apps
Under some circumstances, this is needed for 3rd party integrations such as Tasker to work properly. Apart from that, the app works perfectly fine without this permission, so it does not need to be granted.

## show notifications
Used for displaying a notification while a shortcut is running, or while the app is interacting with Tasker

## ask to ignore battery optimizations
This is not needed for normal operation, but you might encounter problems with periodically running shortcuts not running reliably when it is not granted.

## net.dinglisch.android.tasker.PERMISSION_RUN_TASKS
Used by the [`triggerTaskerTask()`](scripting.md#trigger-tasker-task) function to allow triggering Tasker tasks from Scripting.

# com.wireguard.android.permission.CONTROL_TUNNELS
Used by the [`setWireguardTunnelState()`](scripting.md#set-wireguard-tunnel-state) function to allow enabling or disabling a Wireguard tunnel from Scripting. This permission needs to be granted explicitly by the user if they wish to make use of this feature.

## use biometric hardware / use fingerprint hardware
The app allows to configure a shortcut in a way that it requires biometric confirmation before running, for extra security. This requires a permission.

## prevent phone from sleeping
Used to perform cleanup tasks, such as deleting obsolete cache files after a shortcut was run.

## run at startup
Used for making sure that scheduled tasks (such as periodic shortcut execution and cleanup) can be re-scheduled when the device restarts.

