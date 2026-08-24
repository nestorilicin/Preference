package etf.nestor.preference.ui.singleplayer

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import etf.nestor.preference.domain.game.GameSnapshot
import etf.nestor.preference.domain.game.HandPhase
import etf.nestor.preference.domain.model.PlayingCard

// ─── Game screen ────────────────────────────────────────────────────

@Composable
fun GamePlayScreen(state: GameSnapshot, viewModel: SingleplayerViewModel) {
    if (state.gameOver) {
        GameOverScreen(state = state, viewModel = viewModel)
        return
    }

    var selectedCards by remember { mutableStateOf<Set<PlayingCard>>(emptySet()) }
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Reset selekcije kada se promeni faza
    LaunchedEffect(state.phase) {
        selectedCards = emptySet()
    }

    val onCardClick: (PlayingCard) -> Unit = { card ->
        when (state.phase) {
            HandPhase.DISCARDING -> {
                selectedCards = if (card in selectedCards) {
                    selectedCards - card
                } else if (selectedCards.size < 2) {
                    selectedCards + card
                } else {
                    selectedCards
                }
            }

            HandPhase.PLAYING -> {
                if (state.isHumanTurn) viewModel.playCard(card)
            }

            else -> {}
        }
    }
    val onDiscardConfirm: () -> Unit = { viewModel.discard(selectedCards.toList()) }

    if (isLandscape) {
        LandscapeGamePlayScreen(
            state = state,
            viewModel = viewModel,
            selectedCards = selectedCards,
            onCardClick = onCardClick,
            onDiscardConfirm = onDiscardConfirm
        )
    } else {
        PortraitGamePlayScreen(
            state = state,
            viewModel = viewModel,
            selectedCards = selectedCards,
            onCardClick = onCardClick,
            onDiscardConfirm = onDiscardConfirm
        )
    }
}
