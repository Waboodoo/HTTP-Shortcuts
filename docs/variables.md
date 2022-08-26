# Variables

Variables allow you to inject pieces of information dynamically into your shortcuts when executing them. A variable consists of a name and a value which is resolved at execution time, based on its type. For some variable types this means that a prompt dialog is shown that asks for a value, others can be resolved without user input. They are global, meaning that they do not belong to a specific shortcut but can be used by all of them.

Variables are also particularly useful when combined with the app's [Scripting](scripting.md) capabilities, as it allows you to compute a value using a piece of JavaScript, storing that value into a variable and then use that value as part of the HTTP request.

For more details on when variables are resolved see the [Execution Flow](execution-flow.md) documentation.

## Using Variables

You can insert a placeholder for a variable into your shortcuts' url, requests parameters, request body, authentication and header fields by clicking the *"{ }"* button next to the input field and selecting a variable.

When executing a shortcut that contains variable placeholders all of them are resolved according to their type.

You can also insert these placeholders into some of your variables, i.e., you can have variables reference other variables, and you can use them when writing [scripts](scripting.md#variables).

## Variable Types

When creating a variable, you have to select its type. The type dictates how the variable will receive its value, and what types of values it supports.

<a name="constant"></a>
### Static Variable

A *static variable* (formerly called *constant*) stores a static value. A typical use case is to store a piece of information that is shared across multiple shortcuts, such as an authentication token or a domain name. This way it can easily be changed.

It is also possible to change the value of a static variable programmatically before or after a shortcut runs, e.g., to store parts of an HTTP response into it. See the [Scripting](scripting.md#variables) documentation for more details.

<a name="multiple-choice"></a>
### Multiple-Choice

The *multiple choice* type consists of a list of options, each of which has a value and a label. It triggers a dialog from which one of the options can be selected.

It can also be configured to allow selecting multiple values.

<a name="text-number-password"></a>
### Text, Number and Password

The *text*, *number* and *password* types trigger a prompt dialog where a value can be entered into a text field.

<a name="number-slider"></a>
### Number Slider

The *number slider* type is similar to the *number* type. It allows you to pick a number. However, the number is entered using a horizontal slider, for which you can define the minimum and maximum value, as well as the step size.

<a name="date-time"></a>
### Date and Time

The *date* and *time* types trigger a prompt dialog where a date or time can be selected. The output format can be specified according to Android's [SimpleDateFormat](https://developer.android.com/reference/java/text/SimpleDateFormat.html).

<a name="color"></a>
### Color

The *color* type triggers a prompt dialog where a color can be selected. Its value is returned in RGB hex format (e.g., ff0000 for red).

<a name="toggle"></a>
### Toggle

The *toggle* type consists of a list of values. Every time it is used it resolves to the next value in the list. When the last value is reached it starts again from the first.

<a name="uuid"></a>
### UUID

The *uuid* type will generate a random UUID (*U*niversally *U*nique *Id*entifier) and use that as its value.

Please note that the UUID is generated once per shortcut execution, not once per variable use, meaning that if you use the same variable multiple times within one shortcut it will have the same value in all places. If you need multiple UUIDs for a single shortcut execution you'll need to use multiple different variables.

<a name="clipboard-content"></a>
### Clipboard Content

Variables of type *clipboard content* will resolve to the latest textual value that was copied to the clipboard. If there is no text in the clipboard or the last thing that was copied does not have a textual representation, the variable will have an empty value.

<a name="sharing"></a>
## Sharing Values into Variables
In the advanced settings section of a variable you can mark it as *Allow Receiving Value from Share Dialog*. This makes it possible to provide the value of this variable through Android's *Share*-dialog, e.g., by sharing a URL or text snippet from another app. The variable will then assume the shared value during execution.

If you enable this option, you will also find a dropdown further down which lets you pick which part of the shared value the variable should assume: the text, the title (if any), or both.
