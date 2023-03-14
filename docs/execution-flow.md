# Execution Flow

When you click a shortcut, either on your home screen or within the app, it will execute. This means it will go through the following steps:

## 1. Ask for Confirmation

If you selected the *"Require confirmation before execution"* option for your shortcut, you will be asked to confirm in this step. If you select *"Cancel"* the remaining steps will not be executed.

## 2. Check Wi-Fi SSID

If you configured your shortcut to only run when on a specific Wi-Fi, this step will check whether that condition is met. If it is not, the remaining steps will not be executed and instead you will be prompted to switch Wi-Fis.

## 3. Delay

If you set your shortcut up to have a delay it will be applied in this step. E.g., if you selected that your shortcut should be delayed by 5 seconds then the app will wait for 5 seconds before executing the next steps.

## 4. Resolve Files

If your shortcut needs one or more files, e.g. for its request body or for a form parameter, they will be resolved in this step. You will be prompted with one or multiple file pickers.

If you cancel any of the file pickers then the remaining steps will not be executed.

## 5. Run "Global" Script

If you added any JavaScript code into the *"Global Scripting"* section in the app's settings then that code will execute in this step.

If the script fails (e.g., due to a syntax error) then an error message will be displayed and the remaining steps will not be executed.

For more details see the documentation on [Scripting](scripting.md).

## 6. Run "Before" Script

If you added any JavaScript code into the *"Run before Execution"* textarea in your shortcut's *"Scripting"* section (or your shortcut is a *Scripting* shortcut) then that code will be executed in this step.

If the script fails (e.g., due to a syntax error) then an error message will be displayed and the remaining steps will not be executed.

For more details see the documentation on [Scripting](scripting.md).

## 7. Resolve Variables

If you use any variables in your shortcut, they will be resolved in this step. E.g., if you used a variable of type *"Color Input*" you will be asked to pick a color, or if you have a variable of type *"Text Input"* you will be presented with a dialog window that asks you to enter a text value.

If you select *"Cancel"* on any of the variable resolution dialogs then the remaining steps will not be executed.

Note that it is possible for some variables to already be resolved before this step, e.g. if you ask for their value via Scripting in step 5 or 6 or if you explicitly passed a value for a variable when triggering the shortcut (which is possible when starting a shortcut via Intent, deep-link or Scripting).

For more information see the [Variables](variables.md) documentation.

## 8. Send HTTP Request

In this step the actual HTTP request is sent and the response received.

If the request fails due to a network problem (e.g., because your device is not connected to the internet) and you selected the *"Wait for connection when offline"* option then it will be rescheduled at this point. Once a network connection is detected the execution will start over from step 5.

## 9. Run "Success" or "Failure" Script

If the HTTP request was a success (i.e., it completed with a 2xx response code, or a 3xx response code and following redirects was disabled) and if you added any JavaScript code into the *"Run on Success"* textarea in your shortcut's *"Scripting"* section then that code will be executed in this step.

Similarly, if the HTTP request failed (e.g., because of a network error or because of a 4xx or 5xx response code) and if you added any JavaScript code into the *"Run on Failure"* textarea in your shortcut's *"Scripting"* section then that code will be executed in this step.

If the script fails (e.g., due to a syntax error or runtime error) then an error message will be displayed and the remaining steps will not be executed.

For more details see the documentation on [Scripting](scripting.md).

## 10. Store Response

If you configured your shortcut to store the HTTP response into a file, that will happen in this step. A file is only created for a successful request.

## 10. Display Result

Depending on what options you picked in the *"Response Handling"* section of your shortcut, the response or a pre-defined message will be displayed in this final step, either as a toast, in a dialog window or a fullscreen window.

## 11. Execute Next Shortcut

If you called [`enqueueShortcut`](scripting.md#trigger-shortcut) at any point in the Scripting of your shortcut, that enqueued shortcut will now be executed, possibly after a delay if you specified one.
