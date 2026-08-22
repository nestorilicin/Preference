package etf.nestor.preference.ui.screen.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import etf.nestor.preference.R
import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.ui.screen.HAND_CARD_H
import etf.nestor.preference.ui.screen.HAND_CARD_W
import kotlin.collections.forEach

@Composable
fun TalonArea(talon: List<PlayingCard>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.label_talon),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            talon.forEach { card ->
                CardFront(card = card, modifier = Modifier.size(HAND_CARD_W, HAND_CARD_H))
            }
        }
    }
}