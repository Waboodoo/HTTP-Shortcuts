package ch.rmy.android.http_shortcuts;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mikepenz.aboutlibraries.LibsBuilder;

public class LicensesActivity extends BaseActivity {

    private static final String[] LIBRARIES = {
            "butterknife",
            "gson",
            "materialdialogs",
            "okhttp",
            "recyclerview_v7",
            "realm",
            "volleyplus",
            "flaticons",
            "bitsies",
            "filepicker",
            "jdeferred"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_host);
        if (fragment == null) {
            fragment = new LibsBuilder().withAutoDetect(false).withLibraries(LIBRARIES).withLicenseShown(true).supportFragment();
            fragmentManager.beginTransaction().add(R.id.fragment_host, fragment).commit();
        }
    }
}
