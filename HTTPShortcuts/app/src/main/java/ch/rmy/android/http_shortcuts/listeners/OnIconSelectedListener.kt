package ch.rmy.android.http_shortcuts.listeners

interface OnIconSelectedListener {

    /**
     * Called when the user selects an icon.

     * @param iconName The name of the resource selected by the user
     */
    fun onIconSelected(iconName: String)

}
