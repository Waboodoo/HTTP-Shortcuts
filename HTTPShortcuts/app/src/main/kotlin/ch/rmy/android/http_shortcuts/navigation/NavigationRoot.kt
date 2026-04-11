package ch.rmy.android.http_shortcuts.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
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
import ch.rmy.android.http_shortcuts.activities.shortcutwidget.ShortcutWidgetSettingsScreen
import ch.rmy.android.http_shortcuts.activities.sync.SyncExportScreen
import ch.rmy.android.http_shortcuts.activities.sync.SyncImportScreen
import ch.rmy.android.http_shortcuts.activities.sync.SyncOverviewScreen
import ch.rmy.android.http_shortcuts.activities.troubleshooting.TroubleShootingScreen
import ch.rmy.android.http_shortcuts.activities.variables.GlobalVariablesScreen
import ch.rmy.android.http_shortcuts.activities.variables.editor.GlobalVariableEditorScreen
import ch.rmy.android.http_shortcuts.activities.variablewidget.VariableWidgetSettingsScreen
import ch.rmy.android.http_shortcuts.activities.workingdirectories.WorkingDirectoriesScreen
import ch.rmy.android.http_shortcuts.widget.WidgetsUtil

@Composable
fun NavigationRoot(navController: NavHostController) {
    NavigationEventHandler(navController)

    NavHost(
        navController = navController,
        startDestination = NavigationDestination.Main.routePattern,
    ) {
        composable(NavigationDestination.Main) { backStackEntry ->
            val activity = LocalActivity.current!!
            val intent = activity.intent!!
            MainScreen(
                savedStateHandle = backStackEntry.savedStateHandle,
                selectionMode = MainActivity.determineMode(activity, intent),
                initialCategoryId = intent.getStringExtra(MainActivity.EXTRA_CATEGORY_ID),
                widgetId = WidgetsUtil.getWidgetIdFromIntent(intent),
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

        composable(NavigationDestination.IconPicker) { backStackEntry ->
            IconPickerScreen(
                isMaterialDesignIconPicker = NavigationDestination.IconPicker.extractIsMaterialDesignIcon(backStackEntry.arguments!!),
            )
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

        composable(NavigationDestination.SyncOverview) { backStackEntry ->
            SyncOverviewScreen(
                backStackEntry.savedStateHandle,
            )
        }

        composable(NavigationDestination.SyncImport) {
            SyncImportScreen()
        }

        composable(NavigationDestination.SyncExport) {
            SyncExportScreen()
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

        composable(NavigationDestination.GlobalVariables) { backStackEntry ->
            GlobalVariablesScreen(
                savedStateHandle = backStackEntry.savedStateHandle,
                asPicker = NavigationDestination.GlobalVariables.extractAsPicker(backStackEntry.arguments!!),
            )
        }

        composable(NavigationDestination.GlobalVariableEditor) { backStackEntry ->
            GlobalVariableEditorScreen(
                savedStateHandle = backStackEntry.savedStateHandle,
                globalVariableId = NavigationDestination.GlobalVariableEditor.extractVariableId(backStackEntry.arguments!!),
                variableType = NavigationDestination.GlobalVariableEditor.extractVariableType(backStackEntry.arguments!!),
            )
        }

        composable(NavigationDestination.ShortcutWidget) { backStackEntry ->
            val arguments = backStackEntry.arguments!!
            ShortcutWidgetSettingsScreen(
                shortcutId = NavigationDestination.ShortcutWidget.extractShortcutId(arguments),
                shortcutName = NavigationDestination.ShortcutWidget.extractShortcutName(arguments),
                shortcutIcon = NavigationDestination.ShortcutWidget.extractShortcutIcon(arguments),
                widgetId = NavigationDestination.ShortcutWidget.extractWidgetId(arguments),
            )
        }

        composable(NavigationDestination.VariableWidget) { backStackEntry ->
            val arguments = backStackEntry.arguments!!
            VariableWidgetSettingsScreen(
                widgetId = NavigationDestination.VariableWidget.extractWidgetId(arguments),
            )
        }

        composable(NavigationDestination.WorkingDirectories) { backStackEntry ->
            WorkingDirectoriesScreen(
                picker = NavigationDestination.WorkingDirectories.extractPicker(backStackEntry.arguments!!),
            )
        }
    }
}
