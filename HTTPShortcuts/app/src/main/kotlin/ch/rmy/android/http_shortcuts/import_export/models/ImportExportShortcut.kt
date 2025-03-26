package ch.rmy.android.http_shortcuts.import_export.models

import ch.rmy.android.framework.extensions.isInt
import ch.rmy.android.framework.extensions.isUUID
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.isValidCertificateFingerprint

data class ImportExportShortcut(
    val id: ShortcutId? = null,
    val executionType: String? = null,
    val name: String? = null,
    val description: String? = null,
    val iconName: String? = null,
    val hidden: Boolean? = null,
    val method: String? = null,
    val url: String? = null,
    val username: String? = null,
    val password: String? = null,
    val authToken: String? = null,
    val section: SectionId? = null,
    val bodyContent: String? = null,
    val timeout: Int? = null,
    val waitForInternet: Boolean? = null,
    val acceptAllCertificates: Boolean? = null,
    val certificateFingerprint: String? = null,
    val authentication: String? = null,
    val launcherShortcut: Boolean? = null,
    val secondaryLauncherShortcut: Boolean? = false,
    val quickSettingsTileShortcut: Boolean? = false,
    val delay: Int? = null,
    val repetitionInterval: Int? = null,
    val requestBodyType: String? = null,
    val contentType: String? = null,
    val responseHandling: ImportExportResponseHandling? = null,
    val fileUploadOptions: ImportExportFileUploadOptions? = null,
    val confirmation: String? = null,
    val followRedirects: Boolean? = null,
    val acceptCookies: Boolean? = null,
    val keepConnectionOpen: Boolean? = null,
    val protocolVersion: String? = null,
    val proxy: String? = null,
    val proxyHost: String? = null,
    val proxyPort: Int? = null,
    val proxyUsername: String? = null,
    val proxyPassword: String? = null,
    val wifiSsid: String? = null,
    val clientCert: String? = null,
    val codeOnPrepare: String? = null,
    val codeOnSuccess: String? = null,
    val codeOnFailure: String? = null,
    val browserPackageName: String? = null,
    val excludeFromHistory: Boolean? = null,
    val excludeFromFileSharing: Boolean? = null,
    val runInForegroundService: Boolean? = null,
    val wolMacAddress: String? = null,
    val wolPort: Int? = null,
    val wolBroadcastAddress: String? = null,
    val headers: List<ImportExportHeader>? = null,
    val parameters: List<ImportExportParameter>? = null,
) {
    fun validate() {
        require((id == null || id.isUUID() || id.isInt()) && id != Shortcut.TEMPORARY_ID) {
            "Invalid shortcut ID found, must be UUID: $id"
        }
        require(!name.isNullOrBlank()) {
            "Shortcut must have a name"
        }
        require(certificateFingerprint.isNullOrEmpty() || certificateFingerprint.isValidCertificateFingerprint()) {
            "Invalid certificate fingerprint: $certificateFingerprint"
        }
        headers?.forEach { it.validate() }
        parameters?.forEach { it.validate() }
    }
}

typealias ImportShortcut = ImportExportShortcut

typealias ExportShortcut = ImportExportShortcut
