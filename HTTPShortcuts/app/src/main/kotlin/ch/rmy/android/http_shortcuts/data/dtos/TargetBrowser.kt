package ch.rmy.android.http_shortcuts.data.dtos

import ch.rmy.android.framework.extensions.takeUnlessEmpty

sealed class TargetBrowser {
    data class Browser(override val packageName: String?) : TargetBrowser() {
        override fun serialize(): String =
            packageName ?: ""
    }

    data class CustomTabs(override val packageName: String?) : TargetBrowser() {
        override fun serialize(): String =
            if (packageName != null) {
                "$CUSTOM_TABS_PACKAGE_NAME($packageName)"
            } else {
                CUSTOM_TABS_PACKAGE_NAME
            }
    }

    abstract fun serialize(): String

    abstract val packageName: String?

    companion object {
        private const val CUSTOM_TABS_PACKAGE_NAME = "custom-tabs"

        fun parse(string: String): TargetBrowser =
            if (string.startsWith(CUSTOM_TABS_PACKAGE_NAME)) {
                CustomTabs(
                    string.removePrefix(CUSTOM_TABS_PACKAGE_NAME)
                        .trim('(', ')')
                        .takeUnlessEmpty(),
                )
            } else {
                Browser(string.takeUnlessEmpty())
            }
    }
}
