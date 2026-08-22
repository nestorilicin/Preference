package etf.nestor.preference.ui.screen.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import etf.nestor.preference.R
import etf.nestor.preference.domain.game.GameSnapshot
import etf.nestor.preference.domain.game.HandPhase


@Composable
fun BotHandArea(
    name: String,
    position: Int,
    state: GameSnapshot,
    modifier: Modifier = Modifier
) {
    val tricksPlayed = state.tricksWon.sum()
    val cardsLeft = when (state.phase) {
        HandPhase.PLAYING, HandPhase.FINISHED -> maxOf(0, 10 - tricksPlayed)
        else -> 10
    }
    val wonTricks = state.wonTricksByPlayer[position] ?: emptyList()
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy((-14).dp)) {
                repeat(minOf(cardsLeft, 7)) {
                    CardBack(modifier = Modifier.size(28.dp, 42.dp))
                }
            }
            // Uzeti stihovi — okrenuta karta, klik otvara dialog
            if (wonTricks.isNotEmpty()) {
                Box(modifier = Modifier.clickable { showDialog = true }) {
                    CardBack(modifier = Modifier.size(28.dp, 42.dp))
                    Text(
                        text = "${wonTricks.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }
        }
        Text(
            text = stringResource(R.string.cards_left, cardsLeft),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showDialog) {
        WonTricksDialog(
            playerName = name,
            wonTricks = wonTricks,
            onDismiss = { }
        )
    }
}
