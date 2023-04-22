package ch.rmy.android.http_shortcuts.dagger

import ch.rmy.android.http_shortcuts.Application
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.about.AboutViewModel
import ch.rmy.android.http_shortcuts.activities.categories.CategoriesViewModel
import ch.rmy.android.http_shortcuts.activities.categories.editor.CategoryEditorActivity
import ch.rmy.android.http_shortcuts.activities.categories.editor.CategoryEditorViewModel
import ch.rmy.android.http_shortcuts.activities.contact.ContactActivity
import ch.rmy.android.http_shortcuts.activities.editor.ShortcutEditorViewModel
import ch.rmy.android.http_shortcuts.activities.editor.advancedsettings.AdvancedSettingsActivity
import ch.rmy.android.http_shortcuts.activities.editor.advancedsettings.AdvancedSettingsViewModel
import ch.rmy.android.http_shortcuts.activities.editor.authentication.AuthenticationActivity
import ch.rmy.android.http_shortcuts.activities.editor.authentication.AuthenticationViewModel
import ch.rmy.android.http_shortcuts.activities.editor.basicsettings.BasicRequestSettingsActivity
import ch.rmy.android.http_shortcuts.activities.editor.basicsettings.BasicRequestSettingsViewModel
import ch.rmy.android.http_shortcuts.activities.editor.body.RequestBodyActivity
import ch.rmy.android.http_shortcuts.activities.editor.body.RequestBodyViewModel
import ch.rmy.android.http_shortcuts.activities.editor.executionsettings.ExecutionSettingsViewModel
import ch.rmy.android.http_shortcuts.activities.editor.headers.RequestHeadersActivity
import ch.rmy.android.http_shortcuts.activities.editor.headers.RequestHeadersViewModel
import ch.rmy.android.http_shortcuts.activities.editor.response.ResponseActivity
import ch.rmy.android.http_shortcuts.activities.editor.response.ResponseViewModel
import ch.rmy.android.http_shortcuts.activities.editor.scripting.ScriptingActivity
import ch.rmy.android.http_shortcuts.activities.editor.scripting.ScriptingViewModel
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.CodeSnippetPickerViewModel
import ch.rmy.android.http_shortcuts.activities.editor.shortcuts.TriggerShortcutsViewModel
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteViewModel
import ch.rmy.android.http_shortcuts.activities.execute.Execution
import ch.rmy.android.http_shortcuts.activities.globalcode.GlobalScriptingViewModel
import ch.rmy.android.http_shortcuts.activities.history.HistoryViewModel
import ch.rmy.android.http_shortcuts.activities.icons.IconPickerViewModel
import ch.rmy.android.http_shortcuts.activities.importexport.ImportExportViewModel
import ch.rmy.android.http_shortcuts.activities.main.MainViewModel
import ch.rmy.android.http_shortcuts.activities.main.ShortcutListViewModel
import ch.rmy.android.http_shortcuts.activities.misc.deeplink.DeepLinkViewModel
import ch.rmy.android.http_shortcuts.activities.misc.second_launcher.SecondLauncherViewModel
import ch.rmy.android.http_shortcuts.activities.misc.share.ShareViewModel
import ch.rmy.android.http_shortcuts.activities.misc.voice.VoiceViewModel
import ch.rmy.android.http_shortcuts.activities.remote_edit.RemoteEditViewModel
import ch.rmy.android.http_shortcuts.activities.response.DisplayResponseActivity
import ch.rmy.android.http_shortcuts.activities.settings.SettingsViewModel
import ch.rmy.android.http_shortcuts.activities.variables.VariablesViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.VariableEditorViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.color.ColorTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.constant.ConstantTypeFragment
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.constant.ConstantTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.date.DateTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.select.SelectTypeFragment
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.select.SelectTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.slider.SliderTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.text.TextTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.time.TimeTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.toggle.ToggleTypeFragment
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.toggle.ToggleTypeViewModel
import ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsViewModel
import ch.rmy.android.http_shortcuts.data.maintenance.CleanUpWorker
import ch.rmy.android.http_shortcuts.history.HistoryCleanUpWorker
import ch.rmy.android.http_shortcuts.http.HttpRequesterWorker
import ch.rmy.android.http_shortcuts.plugin.PluginEditActivity
import ch.rmy.android.http_shortcuts.plugin.TriggerShortcutActionRunner
import ch.rmy.android.http_shortcuts.scheduling.ExecutionBroadcastReceiver
import ch.rmy.android.http_shortcuts.scheduling.ExecutionSchedulerWorker
import ch.rmy.android.http_shortcuts.scheduling.ExecutionWorker
import ch.rmy.android.http_shortcuts.scripting.actions.types.ChangeDescriptionAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.ChangeIconAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.ConfirmAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.CopyToClipboardAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.DialogAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.EnqueueShortcutAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.ExecuteShortcutAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.GetClipboardContentAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.GetLocationAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.GetVariableAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.LogEventAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.OpenAppAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.OpenURLAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.PlaySoundAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.PromptAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.PromptColorAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.PromptDateAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.PromptPasswordAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.PromptTimeAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.RenameShortcutAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.ScanBarcodeAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.SelectionAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.SendIntentAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.SetVariableAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.ShareTextAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.TextToSpeechAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.ToastAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.TriggerTaskerTaskAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.VibrateAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.WifiIPAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.WifiSSIDAction
import ch.rmy.android.http_shortcuts.tiles.QuickTileService
import ch.rmy.android.http_shortcuts.variables.VariableEditText
import ch.rmy.android.http_shortcuts.variables.types.ClipboardType
import ch.rmy.android.http_shortcuts.variables.types.ColorType
import ch.rmy.android.http_shortcuts.variables.types.DateType
import ch.rmy.android.http_shortcuts.variables.types.NumberType
import ch.rmy.android.http_shortcuts.variables.types.PasswordType
import ch.rmy.android.http_shortcuts.variables.types.SelectType
import ch.rmy.android.http_shortcuts.variables.types.SliderType
import ch.rmy.android.http_shortcuts.variables.types.TextType
import ch.rmy.android.http_shortcuts.variables.types.TimeType
import ch.rmy.android.http_shortcuts.variables.types.ToggleType
import ch.rmy.android.http_shortcuts.variables.types.UUIDType
import ch.rmy.android.http_shortcuts.widget.WidgetProvider
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): ApplicationComponent
    }

    fun inject(application: Application)

    fun inject(categoriesViewModel: CategoriesViewModel)

    fun inject(categoryEditorViewModel: CategoryEditorViewModel)

    fun inject(advancedSettingsViewModel: AdvancedSettingsViewModel)

    fun inject(authenticationViewModel: AuthenticationViewModel)

    fun inject(basicRequestSettingsViewModel: BasicRequestSettingsViewModel)

    fun inject(requestBodyViewModel: RequestBodyViewModel)

    fun inject(executionSettingsViewModel: ExecutionSettingsViewModel)

    fun inject(requestHeadersViewModel: RequestHeadersViewModel)

    fun inject(responseViewModel: ResponseViewModel)

    fun inject(codeSnippetPickerViewModel: CodeSnippetPickerViewModel)

    fun inject(scriptingViewModel: ScriptingViewModel)

    fun inject(triggerShortcutsViewModel: TriggerShortcutsViewModel)

    fun inject(shortcutEditorViewModel: ShortcutEditorViewModel)

    fun inject(iconPickerViewModel: IconPickerViewModel)

    fun inject(mainViewModel: MainViewModel)

    fun inject(shortcutListViewModel: ShortcutListViewModel)

    fun inject(deepLinkViewModel: DeepLinkViewModel)

    fun inject(shareViewModel: ShareViewModel)

    fun inject(voiceViewModel: VoiceViewModel)

    fun inject(remoteEditViewModel: RemoteEditViewModel)

    fun inject(aboutViewModel: AboutViewModel)

    fun inject(globalScriptingViewModel: GlobalScriptingViewModel)

    fun inject(importExportViewModel: ImportExportViewModel)

    fun inject(settingsViewModel: SettingsViewModel)

    fun inject(constantTypeViewModel: ConstantTypeViewModel)

    fun inject(selectTypeViewModel: SelectTypeViewModel)

    fun inject(toggleTypeViewModel: ToggleTypeViewModel)

    fun inject(variableEditorViewModel: VariableEditorViewModel)

    fun inject(variablesViewModel: VariablesViewModel)

    fun inject(executeActivity: ExecuteActivity)

    fun inject(enqueueShortcutAction: EnqueueShortcutAction)

    fun inject(cleanUpWorker: CleanUpWorker)

    fun inject(pluginEditActivity: PluginEditActivity)

    fun inject(executionWorker: ExecutionWorker)

    fun inject(executionSchedulerWorker: ExecutionSchedulerWorker)

    fun inject(changeDescriptionAction: ChangeDescriptionAction)

    fun inject(changeIconAction: ChangeIconAction)

    fun inject(renameShortcutAction: RenameShortcutAction)

    fun inject(setVariableAction: SetVariableAction)

    fun inject(quickTileService: QuickTileService)

    fun inject(colorType: ColorType)

    fun inject(dateType: DateType)

    fun inject(selectType: SelectType)

    fun inject(sliderType: SliderType)

    fun inject(textType: TextType)

    fun inject(timeType: TimeType)

    fun inject(toggleType: ToggleType)

    fun inject(advancedSettingsActivity: AdvancedSettingsActivity)

    fun inject(authenticationActivity: AuthenticationActivity)

    fun inject(basicRequestSettingsActivity: BasicRequestSettingsActivity)

    fun inject(requestBodyActivity: RequestBodyActivity)

    fun inject(requestHeadersActivity: RequestHeadersActivity)

    fun inject(responseActivity: ResponseActivity)

    fun inject(scriptingActivity: ScriptingActivity)

    fun inject(constantTypeFragment: ConstantTypeFragment)

    fun inject(selectTypeFragment: SelectTypeFragment)

    fun inject(toggleTypeFragment: ToggleTypeFragment)

    fun inject(widgetProvider: WidgetProvider)

    fun inject(colorTypeViewModel: ColorTypeViewModel)

    fun inject(dateTypeViewModel: DateTypeViewModel)

    fun inject(sliderTypeViewModel: SliderTypeViewModel)

    fun inject(textTypeViewModel: TextTypeViewModel)

    fun inject(timeTypeViewModel: TimeTypeViewModel)

    fun inject(vibrateAction: VibrateAction)

    fun inject(getLocationAction: GetLocationAction)

    fun inject(contactActivity: ContactActivity)

    fun inject(variableEditText: VariableEditText)

    fun inject(copyToClipboardAction: CopyToClipboardAction)

    fun inject(getClipboardContentAction: GetClipboardContentAction)

    fun inject(uuidType: UUIDType)

    fun inject(clipboardType: ClipboardType)

    fun inject(getVariableAction: GetVariableAction)

    fun inject(displayResponseActivity: DisplayResponseActivity)

    fun inject(widgetSettingsViewModel: WidgetSettingsViewModel)

    fun inject(wifiSSIDAction: WifiSSIDAction)

    fun inject(numberType: NumberType)

    fun inject(passwordType: PasswordType)

    fun inject(categoryEditorActivity: CategoryEditorActivity)

    fun inject(execution: Execution)

    fun inject(wifiIPAction: WifiIPAction)

    fun inject(confirmAction: ConfirmAction)

    fun inject(dialogAction: DialogAction)

    fun inject(promptAction: PromptAction)

    fun inject(scanBarcodeAction: ScanBarcodeAction)

    fun inject(httpRequesterWorker: HttpRequesterWorker)

    fun inject(selectionAction: SelectionAction)

    fun inject(executeShortcutAction: ExecuteShortcutAction)

    fun inject(executeViewModel: ExecuteViewModel)

    fun inject(triggerShortcutActionRunner: TriggerShortcutActionRunner)

    fun inject(openURLAction: OpenURLAction)

    fun inject(openAppAction: OpenAppAction)

    fun inject(triggerTaskerTaskAction: TriggerTaskerTaskAction)

    fun inject(toastAction: ToastAction)

    fun inject(playSoundAction: PlaySoundAction)

    fun inject(sendIntentAction: SendIntentAction)

    fun inject(textToSpeechAction: TextToSpeechAction)

    fun inject(promptDateAction: PromptDateAction)

    fun inject(promptTimeAction: PromptTimeAction)

    fun inject(promptColorAction: PromptColorAction)

    fun inject(promptPasswordAction: PromptPasswordAction)

    fun inject(historyViewModel: HistoryViewModel)

    fun inject(historyCleanUpWorker: HistoryCleanUpWorker)

    fun inject(secondLauncherViewModel: SecondLauncherViewModel)

    fun inject(logEventAction: LogEventAction)

    fun inject(shareTextAction: ShareTextAction)

    fun inject(executionBroadcastReceiver: ExecutionBroadcastReceiver)
}
