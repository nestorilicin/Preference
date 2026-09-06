package etf.nestor.preference.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import etf.nestor.preference.R
import etf.nestor.preference.domain.scoring.ScoreSheet

@Composable
fun ScoreSheetDialog(
    playerPosition: Int,
    sheet: ScoreSheet?,
    allNames: List<String>,
    onDismiss: () -> Unit
) {
    val leftPos = (playerPosition + 2) % 3
    val rightPos = (playerPosition + 1) % 3
    val center = sheet?.center ?: 0
    val leftSoups = sheet?.soups?.get(leftPos) ?: 0
    val rightSoups = sheet?.soups?.get(rightPos) ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_close)) }
        },
        title = { Text(stringResource(R.string.score_sheet_title, allNames[playerPosition])) },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScoreColumn(
                    name = allNames[leftPos],
                    value = leftSoups,
                    modifier = Modifier.weight(1f)
                )
                VerticalDivider()
                ScoreColumn(
                    name = allNames[playerPosition],
                    value = center,
                    highlight = true,
                    modifier = Modifier.weight(1f)
                )
                VerticalDivider()
                ScoreColumn(
                    name = allNames[rightPos],
                    value = rightSoups,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    )
}

@Composable
private fun ScoreColumn(
    name: String,
    value: Int,
    modifier: Modifier = Modifier,
    highlight: Boolean = false

) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        Text(
            text = "$value",
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight && value <= 0) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}
