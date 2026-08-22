package etf.nestor.preference.ui.screen.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import etf.nestor.preference.domain.scoring.ScoreSheet
import kotlin.collections.component1
import kotlin.collections.component2

@Composable
fun ScoreSheetDialog(
    playerName: String,
    sheet: ScoreSheet?,
    allNames: List<String>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_close)) }
        },
        title = { Text(stringResource(R.string.score_sheet_title, playerName)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val center = sheet?.center ?: 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.label_central_points),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$center",
                        fontWeight = FontWeight.Bold,
                        color = if (center <= 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                }
                if (sheet != null && sheet.soups.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(stringResource(R.string.label_soups), fontWeight = FontWeight.Bold)
                    sheet.soups.forEach { (opponentPos, count) ->
                        val opponentName = allNames.getOrElse(opponentPos) {
                            stringResource(R.string.player_n, opponentPos)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.vs_opponent, opponentName))
                            Text("$count")
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.no_soups),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    )
}
