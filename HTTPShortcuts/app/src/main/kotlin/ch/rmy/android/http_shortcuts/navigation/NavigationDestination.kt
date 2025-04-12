package ch.rmy.android.http_shortcuts.navigation

import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import androidx.navigation.NamedNavArgument
import ch.rmy.android.framework.navigation.NavigationRequest
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import java.io.Serializable

sealed interface NavigationDestination {
    val path: String

    val routePattern: String
        get() = getRoute(path, arguments)

    val arguments: List<NamedNavArgument>

    object Main : NoArgNavigationDestination {
        override val path = "main"
    }

    object About : NoArgNavigationDestination {
        override val path = "about"
    }

    object Acknowledgment : NoArgNavigationDestination {
        override val path = "acknowledgment"
    }

    object Categories : NoArgNavigationDestination {
        override val path = "categories"

        const val RESULT_CATEGORIES_CHANGED = "categories-changed"
    }

    object CategoryEditor : NavigationDestination {
        private const val ARG_CATEGORY_ID = "categoryId"

        override val path = "categories/edit"

        override val arguments =
            listOf(
                optionalStringArg(ARG_CATEGORY_ID),
            )

        fun buildRequest(categoryId: CategoryId?) = buildNavigationRequest {
            parameter(ARG_CATEGORY_ID, categoryId)
        }

        fun extractCategoryId(bundle: Bundle): CategoryId? =
            bundle.getEncodedString(ARG_CATEGORY_ID)

        const val RESULT_CATEGORY_CREATED = "category-created"
        const val RESULT_CATEGORY_EDITED = "category-edited"
    }

    object CategorySectionsEditor : NavigationDestination {
        private const val ARG_CATEGORY_ID = "categoryId"

        override val path = "categories/sections"

        override val arguments =
            listOf(
                optionalStringArg(ARG_CATEGORY_ID),
            )

        fun buildRequest(categoryId: CategoryId?) = buildNavigationRequest {
            parameter(ARG_CATEGORY_ID, categoryId)
        }

        fun extractCategoryId(bundle: Bundle): CategoryId? =
            bundle.getEncodedString(ARG_CATEGORY_ID)
    }

    object CertPinning : NoArgNavigationDestination {
        override val path = "certPinning"
    }

    object CodeSnippetPicker : NavigationDestination {
        private const val ARG_SHORTCUT_ID = "shortcutId"
        private const val ARG_INCLUDE_SUCCESS_OPTIONS = "includeSuccessOptions"
        private const val ARG_INCLUDE_RESPONSE_OPTIONS = "includeResponseOptions"
        private const val ARG_INCLUDE_NETWORK_ERROR_OPTION = "includeNetworkErrorOption"

        override val path = "codeSnippetPicker"

        override val arguments =
            listOf(
                optionalStringArg(ARG_SHORTCUT_ID),
                optionalBooleanArg(ARG_INCLUDE_SUCCESS_OPTIONS),
                optionalBooleanArg(ARG_INCLUDE_RESPONSE_OPTIONS),
                optionalBooleanArg(ARG_INCLUDE_NETWORK_ERROR_OPTION),
            )

        fun buildRequest(
            shortcutId: ShortcutId? = null,
            includeSuccessOptions: Boolean = false,
            includeResponseOptions: Boolean = false,
            includeNetworkErrorOption: Boolean = false,
        ) = buildNavigationRequest {
            parameter(ARG_SHORTCUT_ID, shortcutId)
            parameter(ARG_INCLUDE_SUCCESS_OPTIONS, includeSuccessOptions)
            parameter(ARG_INCLUDE_RESPONSE_OPTIONS, includeResponseOptions)
            parameter(ARG_INCLUDE_NETWORK_ERROR_OPTION, includeNetworkErrorOption)
        }

        fun extractShortcutId(bundle: Bundle): ShortcutId? =
            bundle.getEncodedString(ARG_SHORTCUT_ID)

        fun extractIncludeSuccessOptions(bundle: Bundle): Boolean =
            bundle.getBoolean(ARG_INCLUDE_SUCCESS_OPTIONS)

        fun extractIncludeResponseOptions(bundle: Bundle): Boolean =
            bundle.getBoolean(ARG_INCLUDE_RESPONSE_OPTIONS)

        fun extractIncludeNetworkErrorOption(bundle: Bundle): Boolean =
            bundle.getBoolean(ARG_INCLUDE_NETWORK_ERROR_OPTION)

        data class Result(
            val textBeforeCursor: String,
            val textAfterCursor: String,
        ) : Serializable
    }

    object Contact : NoArgNavigationDestination {
        override val path = "contact"
    }

    object CurlImport : NoArgNavigationDestination {
        override val path = "curlImport"
    }

    object Documentation : NavigationDestination {
        private const val ARG_URL = "url"

        override val path = "documentation"

        override val arguments =
            listOf(
                stringArg(ARG_URL),
            )

        fun buildRequest(url: Uri) = buildNavigationRequest {
            pathPart(url)
        }

        fun extractUrl(bundle: Bundle): Uri =
            bundle.getEncodedString(ARG_URL)!!.toUri()
    }

    object GlobalScripting : NoArgNavigationDestination {
        override val path = "globalScripting"
    }

    object History : NoArgNavigationDestination {
        override val path = "history"
    }

    object IconPicker : NoArgNavigationDestination {
        override val path = "iconPicker"

        data class Result(
            val icon: ShortcutIcon,
        ) : Serializable
    }

    object ImportExport : NavigationDestination {
        private const val ARG_IMPORT_URL = "importUrl"

        override val path = "importExport"

        override val arguments =
            listOf(
                optionalStringArg(ARG_IMPORT_URL),
            )

        fun buildRequest(importUrl: Uri? = null) =
            buildNavigationRequest {
                parameter(ARG_IMPORT_URL, importUrl)
            }

        fun extractImportUrl(bundle: Bundle): Uri? =
            bundle.getEncodedString(ARG_IMPORT_URL)?.toUri()

        const val RESULT_CATEGORIES_CHANGED_FROM_IMPORT = "categories-changed-from-import"
    }

    object Export : NavigationDestination {
        private const val ARG_TO_FILE = "toFile"

        override val path = "export"

        override val arguments =
            listOf(
                optionalBooleanArg(ARG_TO_FILE),
            )

        fun buildRequest(toFile: Boolean) =
            buildNavigationRequest {
                parameter(ARG_TO_FILE, toFile)
            }

        fun extractToFile(bundle: Bundle): Boolean =
            bundle.getBoolean(ARG_TO_FILE)
    }

    object MoveShortcuts : NavigationDestination {
        private const val ARG_SHORTCUT_ID = "shortcutId"

        override val path = "moveShortcuts"

        override val arguments =
            listOf(
                stringArg(ARG_SHORTCUT_ID),
            )

        fun buildRequest(
            shortcutId: ShortcutId,
        ) = buildNavigationRequest {
            pathPart(shortcutId)
        }

        fun extractShortcutId(bundle: Bundle): ShortcutId =
            bundle.getEncodedString(ARG_SHORTCUT_ID)!!

        const val RESULT_SHORTCUTS_MOVED = "shortcuts-moved"
    }

    object RemoteEdit : NoArgNavigationDestination {
        override val path = "remoteEdit"

        const val RESULT_CHANGES_IMPORTED = "changes-imported"
    }

    object Settings : NoArgNavigationDestination {
        override val path = "settings"

        const val RESULT_APP_LOCKED = "app-locked"
    }

    object TypePicker : NavigationDestination {
        private const val ARG_CATEGORY_ID = "categoryId"

        override val path = "shortcutEditor/typePicker"

        override val arguments =
            listOf(
                stringArg(ARG_CATEGORY_ID),
            )

        fun buildRequest(
            categoryId: CategoryId,
        ) = buildNavigationRequest {
            pathPart(categoryId)
        }

        fun extractCategoryId(bundle: Bundle): CategoryId =
            bundle.getEncodedString(ARG_CATEGORY_ID)!!
    }

    object ShortcutEditor : NavigationDestination {
        private const val ARG_EXECUTION_TYPE = "executionType"
        private const val ARG_CATEGORY_ID = "categoryId"
        private const val ARG_SHORTCUT_ID = "shortcutId"
        private const val ARG_CURL_COMMAND_ID = "curlCommandId"
        private const val ARG_RECOVERY_MODE = "recoveryMode"

        override val path = "shortcutEditor/main"

        override val arguments =
            listOf(
                stringArg(ARG_EXECUTION_TYPE),
                stringArg(ARG_CATEGORY_ID),
                optionalStringArg(ARG_SHORTCUT_ID),
                optionalStringArg(ARG_CURL_COMMAND_ID),
                optionalBooleanArg(ARG_RECOVERY_MODE),
            )

        fun buildRequest(
            shortcutId: ShortcutId? = null,
            categoryId: CategoryId,
            executionType: ShortcutExecutionType = ShortcutExecutionType.HTTP,
            curlCommandId: NavigationArgStore.ArgStoreId? = null,
            recoveryMode: Boolean = false,
        ) = buildNavigationRequest {
            pathPart(executionType.type)
            pathPart(categoryId)
            parameter(ARG_SHORTCUT_ID, shortcutId)
            parameter(ARG_CURL_COMMAND_ID, curlCommandId)
            parameter(ARG_RECOVERY_MODE, recoveryMode)
        }

        fun extractCategoryId(bundle: Bundle): CategoryId =
            bundle.getEncodedString(ARG_CATEGORY_ID)!!

        fun extractShortcutId(bundle: Bundle): ShortcutId? =
            bundle.getEncodedString(ARG_SHORTCUT_ID)

        fun extractExecutionType(bundle: Bundle): ShortcutExecutionType =
            bundle.getEncodedString(ARG_EXECUTION_TYPE)
                ?.let(ShortcutExecutionType.Companion::parse)
                ?: ShortcutExecutionType.HTTP

        fun extractCurlCommandId(bundle: Bundle): NavigationArgStore.ArgStoreId? =
            bundle.getEncodedString(ARG_CURL_COMMAND_ID)
                ?.let(NavigationArgStore::ArgStoreId)

        fun extractRecoveryMode(bundle: Bundle): Boolean =
            bundle.getBoolean(ARG_RECOVERY_MODE)

        data class ShortcutCreatedResult(
            val shortcutId: ShortcutId,
        ) : Serializable

        data class ShortcutEditedResult(
            val shortcutId: ShortcutId,
        ) : Serializable
    }

    object ShortcutEditorAdvancedSettings : NoArgNavigationDestination {
        override val path = "shortcutEditor/advancedSettings"
    }

    object ShortcutEditorAuthentication : NoArgNavigationDestination {
        override val path = "shortcutEditor/authentication"
    }

    object ShortcutEditorBasicRequestSettings : NoArgNavigationDestination {
        override val path = "shortcutEditor/basicRequestSettings"
    }

    object ShortcutEditorExecutionSettings : NoArgNavigationDestination {
        override val path = "shortcutEditor/executionSettings"
    }

    object ShortcutEditorRequestBody : NoArgNavigationDestination {
        override val path = "shortcutEditor/requestBody"
    }

    object ShortcutEditorMqttMessages : NoArgNavigationDestination {
        override val path = "shortcutEditor/mqttMessages"
    }

    object ShortcutEditorRequestHeaders : NoArgNavigationDestination {
        override val path = "shortcutEditor/requestHeaders"
    }

    object ShortcutEditorResponse : NoArgNavigationDestination {
        override val path = "shortcutEditor/response"
    }

    object ShortcutEditorResponseDisplay : NoArgNavigationDestination {
        override val path = "shortcutEditor/responseDisplay"
    }

    object ShortcutEditorScripting : NavigationDestination {
        private const val ARG_SHORTCUT_ID = "shortcutId"
        override val path = "shortcutEditor/scripting"

        override val arguments =
            listOf(
                optionalStringArg(ARG_SHORTCUT_ID),
            )

        fun buildRequest(shortcutId: ShortcutId?) = buildNavigationRequest {
            parameter(ARG_SHORTCUT_ID, shortcutId)
        }

        fun extractShortcutId(bundle: Bundle): ShortcutId? =
            bundle.getEncodedString(ARG_SHORTCUT_ID)
    }

    object ShortcutEditorTriggerShortcuts : NavigationDestination {
        private const val ARG_SHORTCUT_ID = "shortcutId"
        override val path = "shortcutEditor/triggerShortcuts"

        override val arguments =
            listOf(
                optionalStringArg(ARG_SHORTCUT_ID),
            )

        fun buildRequest(shortcutId: ShortcutId?) = buildNavigationRequest {
            parameter(ARG_SHORTCUT_ID, shortcutId)
        }

        fun extractShortcutId(bundle: Bundle): ShortcutId? =
            bundle.getEncodedString(ARG_SHORTCUT_ID)
    }

    object TroubleShooting : NoArgNavigationDestination {
        override val path = "troubleShooting"
    }

    object Variables : NavigationDestination {
        private const val ARG_AS_PICKER = "asPicker"
        override val path = "variables"

        override val arguments: List<NamedNavArgument> =
            listOf(
                optionalBooleanArg(ARG_AS_PICKER),
            )

        fun buildRequest(asPicker: Boolean = false) = buildNavigationRequest {
            parameter(ARG_AS_PICKER, asPicker)
        }

        fun extractAsPicker(bundle: Bundle): Boolean =
            bundle.getBoolean(ARG_AS_PICKER)

        data class VariableSelectedResult(
            val variableId: VariableId,
        ) : Serializable
    }

    object VariableEditor : NavigationDestination {
        private const val ARG_VARIABLE_TYPE = "variableType"
        private const val ARG_VARIABLE_ID = "variableId"

        override val path = "variables/edit"

        override val arguments =
            listOf(
                stringArg(ARG_VARIABLE_TYPE),
                optionalStringArg(ARG_VARIABLE_ID),
            )

        fun buildRequest(variableType: VariableType, variableId: VariableId? = null) = buildNavigationRequest {
            pathPart(variableType.type)
            parameter(ARG_VARIABLE_ID, variableId)
        }

        fun extractVariableType(bundle: Bundle): VariableType =
            VariableType.parse(bundle.getEncodedString(ARG_VARIABLE_TYPE)!!)!!

        fun extractVariableId(bundle: Bundle): VariableId? =
            bundle.getEncodedString(ARG_VARIABLE_ID)

        data class VariableCreatedResult(
            val variableId: VariableId,
        ) : Serializable
    }

    object Widget : NavigationDestination {
        private const val ARG_SHORTCUT_ID = "shortcut_id"
        private const val ARG_SHORTCUT_NAME = "shortcut_name"
        private const val ARG_SHORTCUT_ICON = "shortcut_icon"
        private const val ARG_WIDGET_ID = "widget_id"

        override val path = "widget"

        override val arguments =
            listOf(
                stringArg(ARG_SHORTCUT_ID),
                stringArg(ARG_SHORTCUT_NAME),
                stringArg(ARG_SHORTCUT_ICON),
                optionalIntArg(ARG_WIDGET_ID),
            )

        fun buildRequest(shortcutId: ShortcutId, shortcutName: String, shortcutIcon: ShortcutIcon, widgetId: Int?) = buildNavigationRequest {
            pathPart(shortcutId)
            pathPart(shortcutName)
            pathPart(shortcutIcon)
            parameter(ARG_WIDGET_ID, widgetId)
        }

        fun extractShortcutId(bundle: Bundle): ShortcutId =
            bundle.getEncodedString(ARG_SHORTCUT_ID)!!

        fun extractShortcutName(bundle: Bundle): String =
            bundle.getEncodedString(ARG_SHORTCUT_NAME)!!

        fun extractShortcutIcon(bundle: Bundle): ShortcutIcon =
            ShortcutIcon.fromName(bundle.getEncodedString(ARG_SHORTCUT_ICON))

        fun extractWidgetId(bundle: Bundle): Int? =
            bundle.getInt(ARG_WIDGET_ID, -1).takeUnless { it == -1 }

        data class Result(
            val shortcutId: ShortcutId,
            val labelColor: String,
            val showLabel: Boolean,
            val showIcon: Boolean,
            val iconScale: Float,
        ) : Serializable

        const val RESULT_WIDGET_SETTINGS_CANCELLED = "widget-settings-cancelled"
    }

    object WorkingDirectories : NavigationDestination {
        private const val ARG_PICKER = "picker"

        override val path = "workingDirectories"

        override val arguments =
            listOf(
                booleanArg(ARG_PICKER),
            )

        fun buildRequest(picker: Boolean = false) = buildNavigationRequest {
            pathPart(picker)
        }

        fun extractPicker(bundle: Bundle): Boolean =
            bundle.getBoolean(ARG_PICKER)

        data class WorkingDirectoryPickerResult(
            val workingDirectoryId: WorkingDirectoryId,
            val name: String,
        ) : Serializable
    }

    companion object {
        const val RESULT_CHANGES_DISCARDED = "changes-discarded"
    }
}

interface NoArgNavigationDestination : NavigationDestination, NavigationRequest {
    override val routePattern
        get() = path

    override val arguments: List<NamedNavArgument>
        get() = emptyList()

    override val route: String
        get() = path
}
