package ch.rmy.android.http_shortcuts.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import ch.rmy.android.framework.extensions.getParcelable
import ch.rmy.android.http_shortcuts.activities.about.AboutScreen
import ch.rmy.android.http_shortcuts.activities.acknowledgment.AcknowledgmentScreen
import ch.rmy.android.http_shortcuts.activities.categories.CategoriesScreen
import ch.rmy.android.http_shortcuts.activities.categories.editor.CategoryEditorScreen
import ch.rmy.android.http_shortcuts.activities.categories.sections.CategorySectionsScreen
import ch.rmy.android.http_shortcuts.activities.certpinning.CertPinningScreen
import ch.rmy.android.http_shortcuts.activities.contact.ContactScreen
import ch.rmy.android.http_shortcuts.activities.curl_import.CurlImportScreen
import ch.rmy.android.http_shortcuts.activities.documentation.DocumentationScreen
import ch.rmy.android.http_shortcuts.activities.editor.ShortcutEditorScreen
import ch.rmy.android.http_shortcuts.activities.editor.advancedsettings.AdvancedSettingsScreen
import ch.rmy.android.http_shortcuts.activities.editor.authentication.AuthenticationScreen
import ch.rmy.android.http_shortcuts.activities.editor.basicsettings.BasicRequestSettingsScreen
import ch.rmy.android.http_shortcuts.activities.editor.body.RequestBodyScreen
import ch.rmy.android.http_shortcuts.activities.editor.executionsettings.ExecutionSettingsScreen
import ch.rmy.android.http_shortcuts.activities.editor.headers.RequestHeadersScreen
import ch.rmy.android.http_shortcuts.activities.editor.mqttmessages.MqttMessagesScreen
import ch.rmy.android.http_shortcuts.activities.editor.response.ResponseDisplayScreen
import ch.rmy.android.http_shortcuts.activities.editor.response.ResponseScreen
import ch.rmy.android.http_shortcuts.activities.editor.scripting.ScriptingScreen
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.CodeSnippetPickerScreen
import ch.rmy.android.http_shortcuts.activities.editor.shortcuts.TriggerShortcutsScreen
import ch.rmy.android.http_shortcuts.activities.editor.typepicker.TypePickerScreen
import ch.rmy.android.http_shortcuts.activities.globalcode.GlobalScriptingScreen
import ch.rmy.android.http_shortcuts.activities.history.HistoryScreen
import ch.rmy.android.http_shortcuts.activities.icons.IconPickerScreen
import ch.rmy.android.http_shortcuts.activities.importexport.ExportScreen
import ch.rmy.android.http_shortcuts.activities.importexport.ImportExportScreen
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.activities.main.MainScreen
import ch.rmy.android.http_shortcuts.activities.moving.MoveScreen
import ch.rmy.android.http_shortcuts.activities.remote_edit.RemoteEditScreen
import ch.rmy.android.http_shortcuts.activities.settings.SettingsScreen
import ch.rmy.android.http_shortcuts.activities.troubleshooting.TroubleShootingScreen
import ch.rmy.android.http_shortcuts.activities.variables.VariablesScreen
import ch.rmy.android.http_shortcuts.activities.variables.editor.VariableEditorScreen
import ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsScreen
import ch.rmy.android.http_shortcuts.activities.workingdirectories.WorkingDirectoriesScreen
import ch.rmy.android.http_shortcuts.widget.WidgetManager

@Composable
fun NavigationRoot() {
    val navController = rememberNavController()
    NavigationEventHandler(navController)

    NavHost(
        navController = navController,
        startDestination = NavigationDestination.Main.routePattern,
    ) {
        composable(NavigationDestination.Main) { backStackEntry ->
            val intent = LocalActivity.current!!.intent!!
            MainScreen(
                savedStateHandle = backStackEntry.savedStateHandle,
                selectionMode = MainActivity.determineMode(intent.action),
                initialCategoryId = intent.getStringExtra(MainActivity.EXTRA_CATEGORY_ID),
                widgetId = WidgetManager.getWidgetIdFromIntent(intent),
                importUrl = intent.getParcelable(MainActivity.EXTRA_IMPORT_URL),
                cancelPendingExecutions = intent.getBooleanExtra(MainActivity.EXTRA_CANCEL_PENDING_EXECUTIONS, false),
            )
        }

        composable(NavigationDestination.About) {
            AboutScreen()
        }

        composable(NavigationDestination.Acknowledgment) {
            AcknowledgmentScreen()
        }

        composable(NavigationDestination.Categories) { backStackEntry ->
            CategoriesScreen(backStackEntry.savedStateHandle)
        }

        composable(NavigationDestination.CategoryEditor) { backStackEntry ->
            CategoryEditorScreen(
                categoryId = NavigationDestination.CategoryEditor.extractCategoryId(backStackEntry.arguments!!),
            )
        }

        composable(NavigationDestination.CategorySectionsEditor) { backStackEntry ->
            CategorySectionsScreen(
                categoryId = NavigationDestination.CategorySectionsEditor.extractCategoryId(backStackEntry.arguments!!)!!,
            )
        }

        composable(NavigationDestination.CertPinning) {
            CertPinningScreen()
        }

        composable(NavigationDestination.CodeSnippetPicker) { backStackEntry ->
            CodeSnippetPickerScreen(
                backStackEntry.savedStateHandle,
                currentShortcutId = NavigationDestination.CodeSnippetPicker.extractShortcutId(backStackEntry.arguments!!),
                includeSuccessOptions = NavigationDestination.CodeSnippetPicker.extractIncludeSuccessOptions(backStackEntry.arguments!!),
                includeResponseOptions = NavigationDestination.CodeSnippetPicker.extractIncludeResponseOptions(backStackEntry.arguments!!),
                includeNetworkErrorOption = NavigationDestination.CodeSnippetPicker.extractIncludeNetworkErrorOption(backStackEntry.arguments!!),
            )
        }

        composable(NavigationDestination.Contact) {
            ContactScreen()
        }

        composable(NavigationDestination.CurlImport) {
            CurlImportScreen()
        }

        composable(NavigationDestination.Documentation) { backStackEntry ->
            DocumentationScreen(
                url = NavigationDestination.Documentation.extractUrl(backStackEntry.arguments!!),
            )
        }

        composable(NavigationDestination.GlobalScripting) { backStackEntry ->
            GlobalScriptingScreen(
                savedStateHandle = backStackEntry.savedStateHandle,
            )
        }

        composable(NavigationDestination.History) {
            HistoryScreen()
        }

        composable(NavigationDestination.IconPicker) {
            IconPickerScreen()
        }

        composable(NavigationDestination.ImportExport) { backStackEntry ->
            ImportExportScreen(
                savedStateHandle = backStackEntry.savedStateHandle,
                importUrl = NavigationDestination.ImportExport.extractImportUrl(backStackEntry.arguments!!),
            )
        }

        composable(NavigationDestination.Export) { backStackEntry ->
            ExportScreen(
                toFile = NavigationDestination.Export.extractToFile(backStackEntry.arguments!!),
            )
        }

        composable(NavigationDestination.MoveShortcuts) { backStackEntry ->
            MoveScreen(
                initialShortcut = NavigationDestination.MoveShortcuts.extractShortcutId(backStackEntry.arguments!!),
            )
        }

        composable(NavigationDestination.RemoteEdit) {
            RemoteEditScreen()
        }

        composable(NavigationDestination.Settings) {
            SettingsScreen()
        }

        composable(NavigationDestination.TypePicker) { backStackEntry ->
            TypePickerScreen(categoryId = NavigationDestination.TypePicker.extractCategoryId(backStackEntry.arguments!!))
        }

        composable(NavigationDestination.ShortcutEditor) { backStackEntry ->
            ShortcutEditorScreen(
                backStackEntry.savedStateHandle,
                categoryId = NavigationDestination.ShortcutEditor.extractCategoryId(backStackEntry.arguments!!),
                shortcutId = NavigationDestination.ShortcutEditor.extractShortcutId(backStackEntry.arguments!!),
                curlCommandId = NavigationDestination.ShortcutEditor.extractCurlCommandId(backStackEntry.arguments!!),
                executionType = NavigationDestination.ShortcutEditor.extractExecutionType(backStackEntry.arguments!!),
                recoveryMode = NavigationDestination.ShortcutEditor.extractRecoveryMode(backStackEntry.arguments!!),
            )
        }

        composable(NavigationDestination.ShortcutEditorAdvancedSettings) { backStackEntry ->
            AdvancedSettingsScreen(
                backStackEntry.savedStateHandle,
            )
        }

        composable(NavigationDestination.ShortcutEditorAuthentication) { backStackEntry ->
            AuthenticationScreen(
                backStackEntry.savedStateHandle,
            )
        }

        composable(NavigationDestination.ShortcutEditorBasicRequestSettings) { backStackEntry ->
            BasicRequestSettingsScreen(
                backStackEntry.savedStateHandle,
            )
        }

        composable(NavigationDestination.ShortcutEditorExecutionSettings) {
            ExecutionSettingsScreen()
        }

        composable(NavigationDestination.ShortcutEditorRequestBody) { backStackEntry ->
            RequestBodyScreen(
                backStackEntry.savedStateHandle,
            )
        }

        composable(NavigationDestination.ShortcutEditorRequestHeaders) { backStackEntry ->
            RequestHeadersScreen(
                backStackEntry.savedStateHandle,
            )
        }

        composable(NavigationDestination.ShortcutEditorMqttMessages) { backStackEntry ->
            MqttMessagesScreen(
                backStackEntry.savedStateHandle,
            )
        }

        composable(NavigationDestination.ShortcutEditorResponse) { backStackEntry ->
            ResponseScreen(
                savedStateHandle = backStackEntry.savedStateHandle,
            )
        }

        composable(NavigationDestination.ShortcutEditorResponseDisplay) {
            ResponseDisplayScreen()
        }

        composable(NavigationDestination.ShortcutEditorScripting) { backStackEntry ->
            ScriptingScreen(
                savedStateHandle = backStackEntry.savedStateHandle,
                currentShortcutId = NavigationDestination.ShortcutEditorScripting.extractShortcutId(backStackEntry.arguments!!),
            )
        }

        composable(NavigationDestination.ShortcutEditorTriggerShortcuts) { backStackEntry ->
            TriggerShortcutsScreen(
                currentShortcutId = NavigationDestination.ShortcutEditorTriggerShortcuts.extractShortcutId(backStackEntry.arguments!!),
            )
        }

        composable(NavigationDestination.TroubleShooting) {
            TroubleShootingScreen()
        }

        composable(NavigationDestination.Variables) { backStackEntry ->
            VariablesScreen(
                savedStateHandle = backStackEntry.savedStateHandle,
                asPicker = NavigationDestination.Variables.extractAsPicker(backStackEntry.arguments!!),
            )
        }

        composable(NavigationDestination.VariableEditor) { backStackEntry ->
            VariableEditorScreen(
                savedStateHandle = backStackEntry.savedStateHandle,
                variableId = NavigationDestination.VariableEditor.extractVariableId(backStackEntry.arguments!!),
                variableType = NavigationDestination.VariableEditor.extractVariableType(backStackEntry.arguments!!),
            )
        }

        composable(NavigationDestination.Widget) { backStackEntry ->
            val arguments = backStackEntry.arguments!!
            WidgetSettingsScreen(
                shortcutId = NavigationDestination.Widget.extractShortcutId(arguments),
                shortcutName = NavigationDestination.Widget.extractShortcutName(arguments),
                shortcutIcon = NavigationDestination.Widget.extractShortcutIcon(arguments),
                widgetId = NavigationDestination.Widget.extractWidgetId(arguments),
            )
        }

        composable(NavigationDestination.WorkingDirectories) { backStackEntry ->
            WorkingDirectoriesScreen(
                picker = NavigationDestination.WorkingDirectories.extractPicker(backStackEntry.arguments!!),
            )
        }
    }
}
