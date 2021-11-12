# Variables

Variables allow you to inject pieces of information dynamically into your shortcuts when executing them. A variable consists of a name and a value which is resolved at execution time, based on its type. For some variable types this means that a prompt dialog is shown that asks for a value, others can be resolved without user input.

For more details on when variables are resolved see the [Execution Flow](execution-flow.md) documentation.

## Using Variables

You can insert a placeholder for a variable into your shortcuts' url, requests parameters, request body, authentication and header fields by clicking the *"{ }"* button next to the input field and selecting a variable.

When executing a shortcut that contains variable placeholders all of them are resolved according to their type.

You can also insert these placeholders into some of your variables, i.e., you can have variables reference other variables, and you can use them when writing [scripts](scripting.md#variables).

## Variable Types

When creating a variable, you have to select its type. The type dictates how the variable will receive its value, and what types of values it supports.

<a name="constant"></a>
### Static Variable (Constant)

A *static variable* or *constant* stores a static value, until explicitly changed. It can be used to store a piece of information that is shared across multiple shortcut, such as an authentication token or a domain name. This way it can easily be changed.

It is also possible to change the value of a static variable programmatically before or after a shortcut runs, e.g. to store parts of a HTTP response into it. See the [Scripting](scripting.md#variables) documentation for more details.

<a name="toggle"></a>
### Toggle

The *toggle* type consists of a list of values. Every time it is used it resolves to the next value in the list. When the last value is reached it starts again from the first.

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

The *color* type triggers a prompt dialog where a color can be selected. Its value is returned in RGB hex format (e.g. ff0000 for red).


## Sharing Values into Variables
In the advanced settings section of a variable you can mark it as *Allow 'Share …'*. This makes it possible to provide the value of this variable through Android's *Share*-dialog, e.g., by sharing a URL or text snippet from another app. The variable will then assume the shared value during execution.

