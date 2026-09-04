package etf.nestor.preference.ui.screens

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
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
import etf.nestor.preference.ui.composables.ExitGameDialog
import etf.nestor.preference.ui.stateholders.SingleplayerViewModel


@Composable
fun GamePlayScreen(state: GameSnapshot, viewModel: SingleplayerViewModel, onExit: () -> Unit = {}) {
    if (state.gameOver) {
        GameOverScreen(state = state, viewModel = viewModel)
        return
    }

    var selectedCards by remember { mutableStateOf<Set<PlayingCard>>(emptySet()) }
    var showExitDialog by remember { mutableStateOf(false) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    BackHandler {
        if (viewModel.isLoadedFromSavedGame) {
            // Partija je učitana sa ekrana "Sačuvane partije" — nema dijaloga, tiho se prepiše
            // ista sačuvana partija i vraća se direktno na uvodni singleplayer ekran.
            viewModel.exitLoadedSavedGame()
            onExit()
        } else {
            showExitDialog = true
        }
    }

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
            onDiscardConfirm = onDiscardConfirm,

            )
    } else {
        PortraitGamePlayScreen(
            state = state,
            viewModel = viewModel,
            selectedCards = selectedCards,
            onCardClick = onCardClick,
            onDiscardConfirm = onDiscardConfirm,

            )
    }

    if (showExitDialog) {
        ExitGameDialog(onDismiss = { showExitDialog = false }, onExitWithoutSaving = {
            viewModel.exitWithoutSaving()
            showExitDialog = false
            onExit()
        }, onSaveAndExit = { name ->
            viewModel.saveNamed(name)
            showExitDialog = false
            onExit()
        })
    }
}
