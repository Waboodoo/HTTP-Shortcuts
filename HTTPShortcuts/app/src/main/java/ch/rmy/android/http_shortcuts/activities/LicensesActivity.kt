package ch.rmy.android.http_shortcuts.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import ch.rmy.android.http_shortcuts.R
import com.mikepenz.aboutlibraries.LibsBuilder

class LicensesActivity : BaseActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_licenses)

        val fragmentManager = supportFragmentManager
        var fragment: Fragment? = fragmentManager.findFragmentById(R.id.fragment_host)
        if (fragment == null) {
            fragment = LibsBuilder().withAutoDetect(false).withLibraries(*LIBRARIES).withLicenseShown(true).supportFragment()
            fragmentManager.beginTransaction().add(R.id.fragment_host, fragment).commit()
        }
    }

    companion object {

        private val LIBRARIES = arrayOf(
                "butterknife",
                "gson",
                "materialdialogs",
                "okhttp",
                "recyclerview_v7",
                "realm", "volley",
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
