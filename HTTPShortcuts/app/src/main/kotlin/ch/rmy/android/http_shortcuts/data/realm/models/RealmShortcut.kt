package ch.rmy.android.http_shortcuts.data.realm.models

import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey

@Deprecated("Only used in Realm-to-Room migration")
@PersistedName(name = "Shortcut")
class RealmShortcut() : RealmObject {
    @PrimaryKey
    var id: ShortcutId = ""
    var executionType: String? = null
    var categoryId: CategoryId? = null
    var name: String = ""
    var iconName: String? = null
    var hidden: Boolean = false
    var method: String = ""
    var url: String = ""
    var username: String = ""
    var password: String = ""
    var authToken: String = ""
    var description: String = ""
    var section: SectionId? = null
    var bodyContent: String = ""
    var timeout: Int = 0
    var retryPolicy: String = ""
    var headers: RealmList<RealmHeader> = realmListOf()
    var parameters: RealmList<RealmParameter> = realmListOf()
    var acceptAllCertificates: Boolean = false
    var certificateFingerprint: String = ""
    var authentication: String? = null
    var launcherShortcut: Boolean = true
    var secondaryLauncherShortcut: Boolean = false
    var quickSettingsTileShortcut: Boolean = false
    var delay: Int = 0
    var requestBodyType: String = ""
    var contentType: String = ""
    var responseHandling: RealmResponseHandling? = null
    var fileUploadOptions: RealmFileUploadOptions? = null
    var confirmation: String? = null
    var followRedirects: Boolean = true
    var acceptCookies: Boolean = true
    var keepConnectionOpen: Boolean = false
    var protocolVersion: String? = null
    var proxy: String = "HTTP"
    var proxyHost: String? = null
    var proxyPort: Int? = null
    var proxyUsername: String? = null
    var proxyPassword: String? = null
    var wifiSsid: String = ""
    var clientCert: String = ""
    var codeOnPrepare: String = ""
    var codeOnSuccess: String = ""
    var codeOnFailure: String = ""
    var browserPackageName: String = ""
    var excludeFromHistory: Boolean = false
    var repetition: RealmRepetition? = null
    var excludeFromFileSharing: Boolean = false
    var runInForegroundService: Boolean = false
    var wolMacAddress: String = ""
    var wolPort: Int = 0
    var wolBroadcastAddress: String = ""
}
