package etf.nestor.preference.ui.screen.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import etf.nestor.preference.R
import etf.nestor.preference.domain.model.PlayingCard
import kotlin.collections.forEach

@Composable
fun WonTricksDialog(
    playerName: String,
    wonTricks: List<List<PlayingCard>>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_close)) }
        },
        title = { Text(stringResource(R.string.won_tricks_title, playerName)) },
        text = {
            if (wonTricks.isEmpty()) {
                Text(stringResource(R.string.no_won_tricks))
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    wonTricks.forEachIndexed { i, trick ->
                        Column {
                            Text(
                                text = stringResource(R.string.trick_n, i + 1),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                trick.forEach { card ->
                                    CardFront(
                                        card = card,
                                        modifier = Modifier.size(44.dp, 66.dp)
                                    )
                                }
                            }
                        }
                        if (i < wonTricks.size - 1) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    )
}
