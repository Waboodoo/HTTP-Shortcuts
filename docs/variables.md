# Variables

Variables allow you to inject pieces of information dynamically into your shortcuts when executing them. Each variable, at the very least, consists of a name and a value. There are two kinds of variables; [local variables](#local-variables) and [global variables](#global-variables).

Variables are also particularly useful when combined with the app's [Scripting](scripting.md) capabilities, as it allows you to compute a value using a piece of JavaScript code, store that value into a variable and then use that value as part of the HTTP request.

For more details on when variables are resolved see the [Execution Flow](execution-flow.md) documentation.

## Using Variables

You can insert a placeholder for a variable into your shortcuts' url, requests parameters, request body, authentication and header fields by clicking the *"{ }"* button next to the input field and selecting a variable.

When executing a shortcut that contains variable placeholders, all of them are resolved according to their type. For some variable types this means that a prompt dialog is shown that asks for a value, others can be resolved without user input. They are resolved in the order in which they appear in the Global Variables screen, so if you want one variable to be resolved before another, make sure to rearrange them accordingly by dragging.

You can also insert these placeholders into some of your global variables, i.e., you can have variables reference other variables, and you can use them when writing [scripts](scripting.md#variables).

Placeholders for local variables are shown as orange and are enclosed by 2 sets of curly brackets, whereas those for global variables are shown as purple with only 1 set of curly brackets.

<a id="local-variables"></a>
## Local Variables

Local variables are implicitly created when a placeholder for them is used and only exist in the context of the shortcut they are used in. You can assign a value to a local variable by using the [setVariable](scripting.md#set-variable) Scripting function or by passing in a value via [deep linking](advanced.md#deep-link), the [executeShortcut](scripting.md#execute-shortcut) or [enqueueShortcut](scripting.md#trigger-shortcut) functions, or from [Tasker](advanced.md#integrate-with-tasker).

The assigned value is not stored and will be forgotten after the execution of the shortcut completes. If you want to store a value, use a global variable instead.

<a id="global-variables"></a>
## Global Variable

As opposed to local variables, global variables exist independently of shortcuts and can be used by multiple of them. They can be created and managed from the *Global Variables* screen, accessed via the menu on the main screen.

When creating a global variable, you have to select its type. The type dictates how the variable will receive its value, and what types of values it supports.

<a id="constant"></a>
### Static Variable

A *Static Variable* (formerly called *constant*) stores a static value. A typical use case is to store a piece of information that is shared across multiple shortcuts, such as an authentication token or a domain name. This way it can easily be changed.

It is also possible to change the value of a static variable programmatically before or after a shortcut runs, e.g., to store parts of an HTTP response into it. See the [Scripting](scripting.md#variables) documentation for more details.

> The value of a static variable can be at most 40'000 characters long.

If the value of the variable is sensitive, e.g. because it is a password or API token, you can check the "Treat value as secret" checkbox. This will ensure that the value will not be displayed when editing the variable and prevents it from being exposed in the Event History. Keep in mind though that this will not fully protect the variable, as it would still be possible to access the value from a shortcut or via the export feature. The latter can be mitigated by checking the "Exclude stored value from exports" checkbox at the bottom of the variable editing screen. If you want to fully protect the value, it is recommended to set up a lock in the app's Settings.

<a id="multiple-choice"></a>
### Multiple Choice Selection

The *Multiple Choice Selection* type consists of a list of options, each of which has a value and a label. It triggers a dialog from which one of the options can be selected.

It can also be configured to allow selecting multiple values. The selected values will be concatenated using the specified separator. The order in which they were selected is preserved.

<a id="text-number-password"></a>
### Text Input, Number and Password Input

The *Text Input*, *Number Input* and *Password Input* types trigger a prompt dialog where a value can be entered into a text field.

<a id="number-slider"></a>
### Number Slider

The *Number Slider* type is similar to the *number* type. It allows you to pick a number. However, the number is entered using a horizontal slider, for which you can define the minimum and maximum value, as well as the step size.

<a id="date-time"></a>
### Date Input and Time Input

The *Date Input* and *Time Input* types trigger a prompt dialog where a date or time can be selected. If you just want the current date or time without showing a picker dialog, use the [timestamp](#timestamp) type instead.

The output format can be specified using letters from the following table:

| Letter | Date or Time Component                           | Examples                                    |
| ------ | ------------------------------------------------ | ------------------------------------------- |
| `G`    | Era designator                                   | `AD`                                        |
| `y`    | Year                                             | `1996`; `96`                                |
| `Y`    | Week year                                        | `2009`; `09`                                |
| `M`    | Month in year (context sensitive)                | `July`; `Jul`; `07`                         |
| `L`    | Month in year (standalone form)                  | `July`; `Jul`; `07`                         |
| `w`    | Week in year                                     | `27`                                        |
| `W`    | Week in month                                    | `2`                                         |
| `D`    | Day in year                                      | `189`                                       |
| `d`    | Day in month                                     | `10`                                        |
| `F`    | Day of week in month                             | `2`                                         |
| `E`    | Day name in week                                 | `Tuesday`; `Tue`                            |
| `u`    | Day number of week (1 = Monday, ..., 7 = Sunday) | `1`                                         |
| `a`    | Am/pm marker                                     | `PM`                                        |
| `H`    | Hour in day (0-23)                               | `0`                                         |
| `k`    | Hour in day (1-24)                               | `24`                                        |
| `K`    | Hour in am/pm (0-11)                             | `0`                                         |
| `h`    | Hour in am/pm (1-12)                             | `12`                                        |
| `m`    | Minute in hour                                   | `30`                                        |
| `s`    | Second in minute                                 | `55`                                        |
| `S`    | Millisecond                                      | `978`                                       |
| `z`    | Time zone                                        | `Pacific Standard Time`; `PST`; `GMT-08:00` |
| `Z`    | Time zone                                        | `-0800`                                     |
| `X`    | Time zone                                        | `-08`; `-0800`; `-08:00`                    |

For more details see Android's [SimpleDateFormat](https://developer.android.com/reference/java/text/SimpleDateFormat.html), which is used under the hood.

<a id="timestamp"></a>
### Timestamp

A variable of *timestamp* type will use the current date and/or time as its valid, using the specified format. It can be configured to either use the local timezone of the device, or to use UTC.

The time format uses the same syntax as that of [Date Input and Time Input variables](#date-time)

> The device's default locale is used, meaning that e.g. "Day of week" will be in the language of the device

<a id="color"></a>
### Color Input

The *Color Input* type triggers a prompt dialog where a color can be selected. Its value is returned in RGB hex format (e.g., ff0000 for red).

<a id="toggle"></a>
### Toggle

The *Toggle* type consists of a list of values. Every time it is used it resolves to the next value in the list. When the last value is reached it starts again from the first.

<a id="increment"></a>
### Incrementing Counter

The *Incrementing Counter* type tracks a and returns a number. Each time the variable is resolved, the number is increased by 1.

<a id="uuid"></a>
### UUID

The *uuid* type will generate a random UUID (*U*niversally *U*nique *Id*entifier, version 4) and use that as its value.

> Please note that the UUID is generated once per shortcut execution, not once per variable use, meaning that if you use the same variable multiple times within one shortcut it will have the same value in all places. If you need multiple UUIDs for a single shortcut execution you'll need to use multiple different variables.

<a id="clipboard-content"></a>
### Clipboard Content

Variables of type *clipboard content* will resolve to the latest textual value that was copied to the clipboard. If there is no text in the clipboard or the last thing that was copied does not have a textual representation, the variable will have an empty value.

> This variable type can not be used when executing shortcuts in the background, as the Android OS (starting from Android 10) does not allow apps in the background to access the clipboard. In this case the variable will just assume an empty string as its value.

<a id="sharing"></a>
## Sharing Values into Variables
In the advanced settings section of a global variable you can mark it as *Allow Receiving Value from Share Dialog*. This makes it possible to provide the value of this variable through Android's *Share*-dialog, e.g., by sharing a URL or text snippet from another app. The variable will then assume the shared value during the execution of a shortcut.

If you enable this option, you will also find a dropdown further down which lets you pick which part of the shared value the variable should assume: the text, the title/subject (if any), or both.

If you are on Android 11 or newer, you can enhance this by enabling a shortcut as a Direct Share target. You will find the checkbox for this in its "Trigger & Execution Settings". When this is enabled, the shortcut will appear in the Direct Share sheet, making it easier to quickly share text with that specific shortcut.

