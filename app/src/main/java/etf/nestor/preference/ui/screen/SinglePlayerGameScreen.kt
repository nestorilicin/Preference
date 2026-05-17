package etf.nestor.preference.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import etf.nestor.preference.domain.model.PlayedCard
import etf.nestor.preference.ui.viewmodel.SingleplayerViewModel

@Composable
fun SinglePlayerGameScreen(viewModel: SingleplayerViewModel = hiltViewModel()) {
    val hand = viewModel.uiState.value
    if(hand == null){
        Column(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Game not started")
        }
        return
    }
    Column {

        Text("Current player: ${hand.currentPlayer}")

        Text("Phase: ${hand.phase}")

        hand.players.forEach { player ->

            Text("Player ${player.name}")

            player.hand.forEach { card ->

                Button(
                    onClick = {
                        viewModel.playCard(
                            player.position,
                            PlayedCard(player.position, card)
                        )
                    }
                ) {
                    Text("${card.suit} ${card.rank}")
                }
            }
        }
    }
}