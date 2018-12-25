package ch.rmy.android.http_shortcuts.import_export


import android.text.TextUtils
import ch.rmy.android.http_shortcuts.realm.DatabaseMigration
import ch.rmy.android.http_shortcuts.realm.models.Base
import ch.rmy.android.http_shortcuts.utils.UUIDUtils

internal object ImportMigrator {

    fun migrate(base: Base) {
        for (version in base.version + 1..DatabaseMigration.VERSION) {
            migrate(base, version)
        }
    }

    private fun migrate(base: Base, newVersion: Long) {
        when (newVersion) {
            5L -> { // 1.16.0
                for (category in base.categories) {
                    category.layoutType = "linear_list"
                }
            }
            6L -> { // 1.16.0
                for (category in base.categories) {
                    for (shortcut in category.shortcuts) {
                        if (!TextUtils.isEmpty(shortcut.username) || !TextUtils.isEmpty(shortcut.password)) {
                            shortcut.authentication = "basic"
                        }
                    }
                }
            }
            9L -> { // 1.16.2
                for (category in base.categories) {
                    for (shortcut in category.shortcuts) {
                        for (header in shortcut.headers) {
                            header.id = UUIDUtils.create()
                        }
                        for (parameter in shortcut.parameters) {
                            parameter.id = UUIDUtils.create()
                        }
                    }
                }
                for (variable in base.variables) {
                    if (variable.options != null) {
                        for (option in variable.options!!) {
                            option.id = UUIDUtils.create()
                        }
                    }
                }
            }
            10L -> { // 1.17.0
                for (category in base.categories) {
                    for (shortcut in category.shortcuts) {
                        if (shortcut.authentication == null) {
                            shortcut.authentication = "none"
                            shortcut.contentType = "text/plain"
                        }
                    }
                }
            }
            16L -> { // 1.20.0
                for (category in base.categories) {
                    for (shortcut in category.shortcuts) {
                        shortcut.contentType = "text/plain"
                        shortcut.requestBodyType = if (shortcut.parameters.isEmpty()) {
                            "custom_text"
                        } else {
                            "x_www_form_urlencode"
                        }
                    }
                }
            }
            18L -> {
                for (category in base.categories) {
                    for (shortcut in category.shortcuts) {
                        shortcut.executionType = "app"
                    }
                }
            }
        }
    }

}
