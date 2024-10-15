package ch.rmy.android.http_shortcuts.activities.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.LiveHelp
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SettingsButton
import ch.rmy.android.http_shortcuts.components.Spacing

@Composable
fun AboutContent(
    versionNumber: String,
    fDroidVisible: Boolean,
    onChangeLogButtonClicked: () -> Unit,
    onDocumentationButtonClicked: () -> Unit,
    onContactButtonClicked: () -> Unit,
    onTranslateButtonClicked: () -> Unit,
    onPlayStoreButtonClicked: () -> Unit,
    onFDroidButtonClicked: () -> Unit,
    onGitHubButtonClicked: () -> Unit,
    onDonateButtonClicked: () -> Unit,
    onAcknowledgementButtonClicked: () -> Unit,
    onPrivacyPolicyButtonClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = Spacing.SMALL),
    ) {
        SettingsButton(
            icon = Icons.Outlined.Code,
            title = stringResource(R.string.settings_changelog),
            subtitle = stringResource(R.string.settings_changelog_summary, versionNumber),
            onClick = onChangeLogButtonClicked,
        )

        SettingsButton(
            icon = Icons.AutoMirrored.Outlined.LiveHelp,
            title = stringResource(R.string.settings_documentation),
            subtitle = stringResource(R.string.settings_documentation_summary),
            onClick = onDocumentationButtonClicked,
        )

        SettingsButton(
            icon = Icons.Outlined.Email,
            title = stringResource(R.string.settings_mail),
            subtitle = stringResource(R.string.settings_mail_summary),
            onClick = onContactButtonClicked,
        )

        SettingsButton(
            icon = Icons.Outlined.Translate,
            title = stringResource(R.string.settings_help_translate),
            subtitle = stringResource(R.string.settings_help_translate_summary),
            onClick = onTranslateButtonClicked,
        )

        SettingsButton(
            icon = painterResource(R.drawable.ic_google_play),
            title = stringResource(R.string.settings_play_store),
            subtitle = stringResource(R.string.settings_play_store_summary),
            onClick = onPlayStoreButtonClicked,
        )

        if (fDroidVisible) {
            SettingsButton(
                icon = painterResource(R.drawable.ic_f_droid),
                title = stringResource(R.string.settings_f_droid),
                subtitle = stringResource(R.string.settings_f_droid_summary),
                onClick = onFDroidButtonClicked,
            )
        }

        SettingsButton(
            icon = painterResource(R.drawable.ic_github_circle),
            title = stringResource(R.string.settings_github),
            subtitle = stringResource(R.string.settings_github_summary),
            onClick = onGitHubButtonClicked,
        )

        SettingsButton(
            icon = painterResource(R.drawable.ic_gift),
            title = stringResource(R.string.settings_donate),
            subtitle = stringResource(R.string.settings_donate_summary),
            onClick = onDonateButtonClicked,
        )

        SettingsButton(
            icon = Icons.Outlined.People,
            title = stringResource(R.string.settings_licenses),
            subtitle = stringResource(R.string.settings_licenses_summary),
            onClick = onAcknowledgementButtonClicked,
        )

        SettingsButton(
            icon = Icons.AutoMirrored.Outlined.Article,
            title = stringResource(R.string.settings_privacy_policy),
            subtitle = stringResource(R.string.settings_privacy_policy_summary),
            onClick = onPrivacyPolicyButtonClicked,
        )
    }
}
