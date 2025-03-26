package ch.rmy.android.http_shortcuts.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId
import ch.rmy.android.http_shortcuts.data.dtos.TargetBrowser
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.enums.ConfirmationType
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.HttpMethod
import ch.rmy.android.http_shortcuts.data.enums.IpVersion
import ch.rmy.android.http_shortcuts.data.enums.ProxyType
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.enums.ResponseContentType
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import ch.rmy.android.http_shortcuts.data.enums.ResponseFailureOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseSuccessOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType
import ch.rmy.android.http_shortcuts.data.enums.SecurityPolicy
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.extensions.usesResponse
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import java.nio.charset.Charset

@Entity(tableName = "shortcut")
data class Shortcut(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: ShortcutId,
    @ColumnInfo(name = "execution_type")
    val executionType: ShortcutExecutionType,
    @ColumnInfo(name = "category_id", index = true)
    val categoryId: CategoryId,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "icon")
    val icon: ShortcutIcon,
    @ColumnInfo(name = "hidden")
    val hidden: Boolean,
    @ColumnInfo(name = "method")
    val method: HttpMethod,
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "authentication_type")
    val authenticationType: ShortcutAuthenticationType?,
    @ColumnInfo(name = "auth_username")
    val authUsername: String,
    @ColumnInfo(name = "auth_password")
    val authPassword: String,
    @ColumnInfo(name = "auth_token")
    val authToken: String,
    @ColumnInfo(name = "section_id")
    val sectionId: SectionId?,
    @ColumnInfo(name = "body_content")
    val bodyContent: String,
    @ColumnInfo(name = "timeout")
    val timeout: Int,
    @ColumnInfo(name = "wait_for_network")
    val isWaitForNetwork: Boolean,
    @ColumnInfo(name = "host_verification_policy")
    val securityPolicy: SecurityPolicy?,
    @ColumnInfo(name = "launcher_shortcut")
    val launcherShortcut: Boolean,
    @ColumnInfo(name = "secondary_launcher_shortcut")
    val secondaryLauncherShortcut: Boolean,
    @ColumnInfo(name = "quick_settings_tile_shortcut")
    val quickSettingsTileShortcut: Boolean,
    @ColumnInfo(name = "delay")
    val delay: Int,
    @ColumnInfo(name = "repetition_interval")
    val repetitionInterval: Int?,
    @ColumnInfo(name = "content_type")
    val contentType: String,
    @ColumnInfo(name = "file_upload_type")
    val fileUploadType: FileUploadType?,
    @ColumnInfo(name = "file_upload_source_file")
    val fileUploadSourceFile: String?,
    @ColumnInfo(name = "file_upload_use_image_editor")
    val fileUploadUseImageEditor: Boolean,
    @ColumnInfo(name = "confirmation_type")
    val confirmationType: ConfirmationType?,
    @ColumnInfo(name = "follow_redirects")
    val followRedirects: Boolean,
    @ColumnInfo(name = "accept_cookies")
    val acceptCookies: Boolean,
    @ColumnInfo(name = "keep_connection_open")
    val keepConnectionOpen: Boolean,
    @ColumnInfo(name = "wifi_ssid")
    val wifiSsid: String?,
    @ColumnInfo(name = "code_on_prepare")
    val codeOnPrepare: String,
    @ColumnInfo(name = "code_on_success")
    val codeOnSuccess: String,
    @ColumnInfo(name = "code_on_failure")
    val codeOnFailure: String,
    @ColumnInfo(name = "target_browser")
    val targetBrowser: TargetBrowser,
    @ColumnInfo(name = "exclude_from_history")
    val excludeFromHistory: Boolean,
    @ColumnInfo(name = "client_cert_params")
    val clientCertParams: ClientCertParams?,
    @ColumnInfo(name = "request_body_type")
    val requestBodyType: RequestBodyType,
    @ColumnInfo(name = "ip_version")
    val ipVersion: IpVersion?,
    @ColumnInfo(name = "proxy_type")
    val proxyType: ProxyType?,
    @ColumnInfo(name = "proxy_host")
    val proxyHost: String?,
    @ColumnInfo(name = "proxy_port")
    val proxyPort: Int?,
    @ColumnInfo(name = "proxy_username")
    val proxyUsername: String?,
    @ColumnInfo(name = "proxy_password")
    val proxyPassword: String?,
    @ColumnInfo(name = "exclude_from_file_sharing")
    val excludeFromFileSharing: Boolean,
    @ColumnInfo(name = "run_in_foreground_service")
    val runInForegroundService: Boolean,
    @ColumnInfo(name = "wol_mac_address")
    val wolMacAddress: String,
    @ColumnInfo(name = "wol_port")
    val wolPort: Int,
    @ColumnInfo(name = "wol_broadcast_address")
    val wolBroadcastAddress: String,
    @ColumnInfo(name = "response_actions")
    val responseActions: String,
    @ColumnInfo(name = "response_ui_type")
    val responseUiType: ResponseUiType,
    @ColumnInfo(name = "response_success_output")
    val responseSuccessOutput: ResponseSuccessOutput,
    @ColumnInfo(name = "response_failure_output")
    val responseFailureOutput: ResponseFailureOutput,
    @ColumnInfo(name = "response_content_type")
    val responseContentType: ResponseContentType?,
    @ColumnInfo(name = "response_charset")
    val responseCharset: Charset?,
    @ColumnInfo(name = "response_success_message")
    val responseSuccessMessage: String,
    @ColumnInfo(name = "response_include_meta_info")
    val responseIncludeMetaInfo: Boolean,
    @ColumnInfo(name = "response_json_array_as_table")
    val responseJsonArrayAsTable: Boolean,
    @ColumnInfo(name = "response_monospace")
    val responseMonospace: Boolean,
    @ColumnInfo(name = "response_font_size")
    val responseFontSize: Int?,
    @ColumnInfo(name = "response_java_script_enabled")
    val responseJavaScriptEnabled: Boolean,
    @ColumnInfo(name = "response_store_directory_id")
    val responseStoreDirectoryId: WorkingDirectoryId?,
    @ColumnInfo(name = "response_store_file_name")
    val responseStoreFileName: String?,
    @ColumnInfo(name = "response_replace_file_if_exists")
    val responseReplaceFileIfExists: Boolean,
    @ColumnInfo(name = "sorting_order", index = true)
    val sortingOrder: Int = 0,
) {
    fun allowsBody(): Boolean =
        method == HttpMethod.POST ||
            method == HttpMethod.PUT ||
            method == HttpMethod.DELETE ||
            method == HttpMethod.PATCH ||
            method == HttpMethod.OPTIONS

    fun usesRequestParameters() =
        executionType == ShortcutExecutionType.HTTP &&
            allowsBody() &&
            (requestBodyType == RequestBodyType.FORM_DATA || requestBodyType == RequestBodyType.X_WWW_FORM_URLENCODE)

    fun usesCustomBody() =
        allowsBody() && requestBodyType == RequestBodyType.CUSTOM_TEXT

    fun usesGenericFileBody() =
        allowsBody() && requestBodyType == RequestBodyType.FILE

    fun usesResponseBody(): Boolean =
        executionType.usesResponse &&
            (
                (responseSuccessOutput == ResponseSuccessOutput.RESPONSE || responseFailureOutput == ResponseFailureOutput.DETAILED) ||
                    codeOnSuccess.isNotEmpty() || codeOnFailure.isNotEmpty()
                )

    val responseDisplayActions: List<ResponseDisplayAction>
        get() = responseActions.takeUnlessEmpty()?.split(",")?.mapNotNull(ResponseDisplayAction::parse) ?: emptyList()

    companion object {
        const val TEMPORARY_ID: ShortcutId = "0"
        const val DEFAULT_CONTENT_TYPE = "text/plain"
    }
}
