# Variables

Variables allow you to inject pieces of information dynamically into your shortcuts when executing them. A variable consists of a name and a value which is resolved at execution time, based on its type. For some variable types this means that a prompt dialog is shown that asks for a value, others can be resolved without user input. They are global, meaning that they do not belong to a specific shortcut but can be used by any of them.

Variables are also particularly useful when combined with the app's [Scripting](scripting.md) capabilities, as it allows you to compute a value using a piece of JavaScript code, store that value into a variable and then use that value as part of the HTTP request.

For more details on when variables are resolved see the [Execution Flow](execution-flow.md) documentation.

## Using Variables

You can insert a placeholder for a variable into your shortcuts' url, requests parameters, request body, authentication and header fields by clicking the *"{ }"* button next to the input field and selecting a variable.

When executing a shortcut that contains variable placeholders all of them are resolved according to their type. They are resolved in the order in which they appear in the Variables screen, so if you want one variable to be resolved before another, make sure to rearrange them accordingly by dragging.

You can also insert these placeholders into some of your variables, i.e., you can have variables reference other variables, and you can use them when writing [scripts](scripting.md#variables).

## Variable Types

When creating a variable, you have to select its type. The type dictates how the variable will receive its value, and what types of values it supports.

<a name="constant"></a>
### Static Variable

A *Static Variable* (formerly called *constant*) stores a static value. A typical use case is to store a piece of information that is shared across multiple shortcuts, such as an authentication token or a domain name. This way it can easily be changed.

It is also possible to change the value of a static variable programmatically before or after a shortcut runs, e.g., to store parts of an HTTP response into it. See the [Scripting](scripting.md#variables) documentation for more details.

> The value of a static variable can be at most 40'000 characters long.

<a name="multiple-choice"></a>
### Multiple Choice Selection

The *Multiple Choice Selection* type consists of a list of options, each of which has a value and a label. It triggers a dialog from which one of the options can be selected.

It can also be configured to allow selecting multiple values. The selected values will be concatenated using the specified separator. The order in which they were selected is preserved.

<a name="text-number-password"></a>
### Text Input, Number and Password Input

The *Text Input*, *Number Input* and *Password Input* types trigger a prompt dialog where a value can be entered into a text field.

<a name="number-slider"></a>
### Number Slider

The *Number Slider* type is similar to the *number* type. It allows you to pick a number. However, the number is entered using a horizontal slider, for which you can define the minimum and maximum value, as well as the step size.

<a name="date-time"></a>
### Date Input and Time Input

The *Date Input* and *Time Input* types trigger a prompt dialog where a date or time can be selected. If you just want the current date or time without showing a picker dialog, use the *timestamp* type instead.

The output format can be specified according to Android's [SimpleDateFormat](https://developer.android.com/reference/java/text/SimpleDateFormat.html).

<a name="timestamp"></a>
### Timestamp

A variable of *timestamp* type will use the current date and/or time as its valid, using the specified format. The output format can be specified according to Android's [SimpleDateFormat](https://developer.android.com/reference/java/text/SimpleDateFormat.html).

<a name="color"></a>
### Color Input

The *Color Input* type triggers a prompt dialog where a color can be selected. Its value is returned in RGB hex format (e.g., ff0000 for red).

<a name="toggle"></a>
### Toggle

The *Toggle* type consists of a list of values. Every time it is used it resolves to the next value in the list. When the last value is reached it starts again from the first.

<a name="increment"></a>
### Incrementing Counter

The *Incrementing Counter* type tracks a and returns a number. Each time the variable is resolved, the number is increased by 1.

<a name="uuid"></a>
### UUID

The *uuid* type will generate a random UUID (*U*niversally *U*nique *Id*entifier) and use that as its value.

> Please note that the UUID is generated once per shortcut execution, not once per variable use, meaning that if you use the same variable multiple times within one shortcut it will have the same value in all places. If you need multiple UUIDs for a single shortcut execution you'll need to use multiple different variables.

<a name="clipboard-content"></a>
### Clipboard Content

Variables of type *clipboard content* will resolve to the latest textual value that was copied to the clipboard. If there is no text in the clipboard or the last thing that was copied does not have a textual representation, the variable will have an empty value.

> This variable type can not be used when executing shortcuts in the background, as the Android OS (starting from Android 10) does not allow apps in the background to access the clipboard. In this case the variable will just assume an empty string as its value.

<a name="sharing"></a>
## Sharing Values into Variables
In the advanced settings section of a variable you can mark it as *Allow Receiving Value from Share Dialog*. This makes it possible to provide the value of this variable through Android's *Share*-dialog, e.g., by sharing a URL or text snippet from another app. The variable will then assume the shared value during the execution of a shortcut.

If you enable this option, you will also find a dropdown further down which lets you pick which part of the shared value the variable should assume: the text, the title/subject (if any), or both.

If you are on Android 11 or newer, you can enhance this by enabling a shortcut as a Direct Share target. You will find the checkbox for this in its "Trigger & Execution Settings". When this is enabled, the shortcut will appear in the Direct Share sheet, making it easier to quickly share text with that specific shortcut.

