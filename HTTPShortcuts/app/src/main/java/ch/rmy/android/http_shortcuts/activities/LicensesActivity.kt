package ch.rmy.android.http_shortcuts.activities

import android.content.Context
import android.os.Bundle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import com.mikepenz.aboutlibraries.LibsBuilder

class LicensesActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_licenses)

        val fragmentManager = supportFragmentManager
        fragmentManager.findFragmentById(R.id.fragment_host) ?: run {
            LibsBuilder()
                    .withAutoDetect(false)
                    .withLibraries(*LIBRARIES)
                    .withLicenseShown(true)
                    .supportFragment()
                    .also {
                        fragmentManager
                                .beginTransaction()
                                .add(R.id.fragment_host, it)
                                .commit()
                    }
        }
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, LicensesActivity::class.java)

    companion object {

        private val LIBRARIES = arrayOf(
                "gson",
                "materialdialogs",
                "okhttp",
                "recyclerview_v7",
                "realm",
                "volley",
                "flaticons",
                "bitsies",
                "filepicker",
                "jdeferred",
                "vintagechroma",
                "codeviewandroid",
                "stetho",
                "okhttpdigest"
        )
    }
}
