<a name="examples"></a>
# Scripting Examples

This page lists some examples for typical use-cases of the code execution feature.

### Generate values for use in the request

Sometimes you need to generate a value, for example the current date, or a random number, to be used in your request. You can do this by generating the value and then storing it into a variable that you then use in your request. Here's an example:

```js
const randomNumber = Math.floor(Math.random() * 10);
setVariable('myNumber', randomNumber);

const currentDate = (new Date()).toDateString();
setVariable('today', currentDate);
```

### Parse a JSON response

Here's an example on how to parse the response of your shortcut, and display the result or store it into a variable for later user.

```js
const temperature = JSON.parse(response.body).temperature;
showDialog(`The current temperature is ${temperature}°C`, 'Temperature');

setVariable('temperature', temperature);
```

### Change icon and label based on response

This example shows how the shortcut icon and label can be changed based on the received response. The example assumes that the server returns 'OK' if the request was a success.

```js
if (response.body == 'OK') {
  renameShortcut('', 'Success');
  changeIcon('', 'freepik_check'); // changes the icon of the current shortcut to a green checkmark
} else {
  renameShortcut('', 'Failure');
  changeIcon('', 'freepik_close'); // changes the icon of the current shortcut to a red cross
}
```

### Ask for confirmation before execution shortcut

This example shows how you can show a custom confirmation message before the shortcut executes and only execute it if the user confirms by clicking 'OK'.

```js
if (!confirm('Should I do the thing?')) {
  showToast('Not doing the thing.');
  abort();
}
```

Or you might want to bypass the confirmation step if you are in your home network:

```js
if (getWifiSSID() != 'My Home Network') {
  if (!confirm('Should I do the thing?')) {
    showToast('Not doing the thing.');
    abort();
  }
}
```

### Read the response out loud

This example shows how you can have the received response be read out loud (using text-to-speech). The example assumes that the response is in plain-text (i.e., not HTML, JSON, ...).

```js
speak(response.body);
```
<a name="split-color"></a>
### Show a color picker and split the result into R, G, and B

This example shows how you can open a color picker dialog, then separate the selected color into its red, green and blue components and convert them to a number from 0 to 255. It assumes that you created variables "red", "green" and "blue", into which the result is stored such that you can use it in your shortcut, e.g., in query parameters within the URL.

```js
const myColor = promptColor();
if (!myColor) {
  abort();
}
const red = parseInt(myColor.substring(0, 2), 16);
const green = parseInt(myColor.substring(2, 4), 16);
const blue = parseInt(myColor.substring(4, 6), 16);
setVariable("red", red);
setVariable("green", green);
setVariable("blue", blue);
```

