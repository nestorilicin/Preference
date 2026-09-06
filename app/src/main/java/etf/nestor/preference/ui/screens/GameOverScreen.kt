package etf.nestor.preference.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import etf.nestor.preference.R
import etf.nestor.preference.domain.game.GameSnapshot
import etf.nestor.preference.ui.stateholders.SinglePlayerViewModel
import kotlin.math.abs

@Composable
fun GameOverScreen(state: GameSnapshot, viewModel: SinglePlayerViewModel) {
    val youFallback = stringResource(R.string.player_name_you)
    val names = listOf(
        state.playerNames.getOrElse(0) { youFallback },
        stringResource(R.string.player_name_bot1),
        stringResource(R.string.player_name_bot2)
    )
    fun finalScore(pos: Int): Int {
        val leftPos = (pos + 2) % 3
        val rightPos = (pos + 1) % 3
        val mySheet = state.scoreSheets[pos]
        val center = mySheet?.center ?: 0
        val toLeft = mySheet?.soups?.get(leftPos) ?: 0
        val toRight = mySheet?.soups?.get(rightPos) ?: 0
        val fromRight = state.scoreSheets[rightPos]?.soups?.get(pos) ?: 0
        val fromLeft = state.scoreSheets[leftPos]?.soups?.get(pos) ?: 0
        return center * 10 - toLeft - toRight + fromRight + fromLeft
    }

    val standings = listOf(0, 1, 2).map { it to finalScore(it) }.sortedBy { it.second }

    AppScreen(title = stringResource(R.string.msg_game_over)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            standings.forEach { (pos, score) ->
                val isGood = score <= 0
                Text(
                    text = names[pos] + ": " + stringResource(
                        if (isGood) R.string.game_over_good else R.string.game_over_bad,
                        abs(score)
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isGood) FontWeight.Bold else FontWeight.Normal,
                    color = if (isGood) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { viewModel.startGame() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    stringResource(R.string.btn_new_game),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
