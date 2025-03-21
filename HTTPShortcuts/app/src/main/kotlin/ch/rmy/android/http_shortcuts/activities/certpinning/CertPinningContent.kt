package ch.rmy.android.http_shortcuts.activities.certpinning

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.certpinning.models.Pin
import ch.rmy.android.http_shortcuts.components.EmptyState
import ch.rmy.android.http_shortcuts.data.domains.certificate_pins.CertificatePinId

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CertPinningContent(
    pins: List<Pin>,
    onPinClicked: (CertificatePinId) -> Unit,
) {
    if (pins.isEmpty()) {
        EmptyState(
            description = stringResource(
                R.string.empty_state_certificate_pinning_instructions,
                stringResource(R.string.label_advanced_technical_settings),
            ),
        )
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        items(
            items = pins,
            key = { it.id },
        ) { pin ->
            PinItem(
                pin = pin,
                modifier = Modifier
                    .animateItem()
                    .clickable {
                        onPinClicked(pin.id)
                    },
            )
        }
    }
}

@Composable
private fun PinItem(
    pin: Pin,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier,
    ) {
        ListItem(
            modifier = Modifier
                .fillMaxWidth(),
            headlineContent = {
                Text(pin.pattern)
            },
            supportingContent = {
                Text(pin.formatted())
            },
        )
        HorizontalDivider()
    }
}
