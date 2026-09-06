package etf.nestor.preference.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import etf.nestor.preference.domain.game.GameSnapshot
import etf.nestor.preference.domain.game.HandPhase
import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.ui.composables.ActionArea
import etf.nestor.preference.ui.composables.HAND_CARD_H
import etf.nestor.preference.ui.composables.HAND_CARD_W
import etf.nestor.preference.ui.composables.PlayerHandSection
import etf.nestor.preference.ui.composables.ScoreBar
import etf.nestor.preference.ui.composables.TalonArea
import etf.nestor.preference.ui.composables.TrickArea
import etf.nestor.preference.ui.composables.actionMessageText
import etf.nestor.preference.ui.composables.currentGameLabel
import etf.nestor.preference.ui.stateholders.SinglePlayerViewModel

private val PORTRAIT_MESSAGE_ACTION_HEIGHT = 130.dp

@Composable
fun PortraitGamePlayScreen(
    state: GameSnapshot,
    viewModel: SinglePlayerViewModel,
    selectedCards: Set<PlayingCard>,
    onCardClick: (PlayingCard) -> Unit,
    onDiscardConfirm: () -> Unit,

    ) {
    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {

        ScoreBar(state, viewModel)

        state.currentGameLabel()?.let { label ->
            Text(
                text = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (state.phase == HandPhase.BIDDING || state.phase == HandPhase.TALON_PICKUP) {
                TalonArea(
                    state = state,
                    cardW = HAND_CARD_W,
                    cardH = HAND_CARD_H,
                    gap = 8.dp,
                    fillWidth = false
                )
            } else {
                TrickArea(state)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = PORTRAIT_MESSAGE_ACTION_HEIGHT)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = state.actionMessageText(selectedCards.size),
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )

            ActionArea(
                state = state,
                viewModel = viewModel,
                selectedCards = selectedCards,
                onDiscardConfirm = onDiscardConfirm
            )
        }

        HorizontalDivider()
        PlayerHandSection(
            cards = state.playerHand,
            selectedCards = selectedCards,
            phase = state.phase,
            isHumanTurn = state.isHumanTurn,
            legalCards = state.legalCardsForHuman,
            onCardClick = onCardClick
        )
    }
}
