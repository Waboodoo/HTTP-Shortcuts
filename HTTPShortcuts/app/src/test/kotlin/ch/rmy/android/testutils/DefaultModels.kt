package ch.rmy.android.testutils

import android.net.Uri
import ch.rmy.android.http_shortcuts.data.dtos.TargetBrowser
import ch.rmy.android.http_shortcuts.data.enums.HttpMethod
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.enums.ResponseFailureOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseSuccessOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

object DefaultModels {
    val shortcut = Shortcut(
        id = "",
        executionType = ShortcutExecutionType.HTTP,
        categoryId = "",
        name = "",
        description = "",
        icon = ShortcutIcon.NoIcon,
        hidden = false,
        method = HttpMethod.GET,
        url = "",
        authenticationType = null,
        authUsername = "",
        authPassword = "",
        authToken = "",
        sectionId = null,
        bodyContent = "",
        timeout = 0,
        isWaitForNetwork = false,
        securityPolicy = null,
        launcherShortcut = false,
        secondaryLauncherShortcut = false,
        quickSettingsTileShortcut = false,
        delay = 0,
        repetitionInterval = null,
        contentType = "",
        fileUploadType = null,
        fileUploadSourceFile = null,
        fileUploadUseImageEditor = false,
        confirmationType = null,
        followRedirects = false,
        acceptCookies = false,
        keepConnectionOpen = false,
        wifiSsid = null,
        codeOnPrepare = "",
        codeOnSuccess = "",
        codeOnFailure = "",
        targetBrowser = TargetBrowser.Browser(null),
        excludeFromHistory = false,
        clientCertParams = null,
        requestBodyType = RequestBodyType.CUSTOM_TEXT,
        ipVersion = null,
        proxyType = null,
        proxyHost = null,
        proxyPort = null,
        proxyUsername = null,
        proxyPassword = null,
        excludeFromFileSharing = false,
        runInForegroundService = false,
        wolMacAddress = "",
        wolPort = 0,
        wolBroadcastAddress = "",
        responseActions = "",
        responseUiType = ResponseUiType.WINDOW,
        responseSuccessOutput = ResponseSuccessOutput.RESPONSE,
        responseFailureOutput = ResponseFailureOutput.DETAILED,
        responseContentType = null,
        responseCharset = null,
        responseSuccessMessage = "",
        responseIncludeMetaInfo = false,
        responseJsonArrayAsTable = false,
        responseMonospace = false,
        responseFontSize = null,
        responseJavaScriptEnabled = false,
        responseStoreDirectoryId = null,
        responseStoreFileName = null,
        responseReplaceFileIfExists = false,
        sortingOrder = 0,
    )

    val variable = Variable(
        id = "",
        key = "",
        type = VariableType.CONSTANT,
        value = null,
        data = null,
        rememberValue = false,
        urlEncode = false,
        jsonEncode = false,
        title = "",
        message = "",
        isShareText = false,
        isShareTitle = false,
        isMultiline = false,
        isExcludeValueFromExport = false,
        sortingOrder = 0,
    )

    val workingDirectory = WorkingDirectory(
        id = "",
        name = "",
        directory = Uri.EMPTY,
        accessed = null,
    )
}
