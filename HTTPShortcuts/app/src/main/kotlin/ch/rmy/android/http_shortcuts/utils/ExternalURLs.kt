package ch.rmy.android.http_shortcuts.utils

object ExternalURLs {

    private const val BASE_URL = "https://http-shortcuts.rmy.ch"
    const val SHORTCUTS_DOCUMENTATION = "$BASE_URL/shortcuts"
    const val CATEGORIES_DOCUMENTATION = "$BASE_URL/categories"
    const val VARIABLES_DOCUMENTATION = "$BASE_URL/variables"
    const val SCRIPTING_DOCUMENTATION = "$BASE_URL/scripting#scripting"
    const val WORKING_DIRECTORIES_DOCUMENTATION = "$BASE_URL/directories"
    const val IMPORT_EXPORT_DOCUMENTATION = "$BASE_URL/import-export"
    const val CERTIFICATE_PINNING_DOCUMENTATION = "$BASE_URL/advanced#certificate-pinning"

    const val PRIVACY_POLICY = "$BASE_URL/privacy-policy"
    const val DOCUMENTATION_PAGE = "$BASE_URL/documentation"
    const val DONATION_PAGE = "$BASE_URL/support-me#donate"
    const val CONTACT_PAGE = "$BASE_URL/contact"
    const val PLAY_STORE = "https://play.google.com/store/apps/details?id=ch.rmy.android.http_shortcuts"
    const val F_DROID = "https://f-droid.org/en/packages/ch.rmy.android.http_shortcuts/"
    const val GITHUB = "https://github.com/Waboodoo/HTTP-Shortcuts"
    const val TRANSLATION = "https://crowdin.com/project/http-shortcuts"

    fun getScriptingDocumentation(docRef: String) =
        "$BASE_URL/scripting#$docRef"
}
