package etf.nestor.preference.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
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
import etf.nestor.preference.ui.composables.LandscapeTable
import etf.nestor.preference.ui.composables.PlayerHandSection
import etf.nestor.preference.ui.composables.ScoreBar
import etf.nestor.preference.ui.composables.TalonArea
import etf.nestor.preference.ui.composables.actionMessageText
import etf.nestor.preference.ui.composables.currentGameLabel
import etf.nestor.preference.ui.stateholders.SinglePlayerViewModel

private val LANDSCAPE_HAND_CARD_W = 54.dp
private val LANDSCAPE_HAND_CARD_H = 81.dp
private val LANDSCAPE_MARGIN = 20.dp

private val LANDSCAPE_SIDEBAR_WIDTH = 150.dp

@Composable
fun LandscapeGamePlayScreen(
    state: GameSnapshot,
    viewModel: SinglePlayerViewModel,
    selectedCards: Set<PlayingCard>,
    onCardClick: (PlayingCard) -> Unit,
    onDiscardConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = LANDSCAPE_MARGIN, vertical = 6.dp)
    ) {
        ScoreBar(state, viewModel, compact = true)

        state.currentGameLabel()?.let { label ->
            Text(
                text = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                if (state.phase == HandPhase.BIDDING || state.phase == HandPhase.TALON_PICKUP) {
                    TalonArea(
                        state = state,
                        cardW = LANDSCAPE_HAND_CARD_W,
                        cardH = LANDSCAPE_HAND_CARD_H,
                        fillWidth = false
                    )
                } else {
                    LandscapeTable(
                        state = state,
                        cardW = LANDSCAPE_HAND_CARD_W,
                        cardH = LANDSCAPE_HAND_CARD_H,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .width(LANDSCAPE_SIDEBAR_WIDTH)
                    .fillMaxHeight()
                    .padding(start = 10.dp),
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = state.actionMessageText(selectedCards.size),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )

                ActionArea(
                    state = state,
                    viewModel = viewModel,
                    selectedCards = selectedCards,
                    onDiscardConfirm = onDiscardConfirm,
                    contentPadding = 6.dp,
                    verticalButtons = true
                )
            }
        }
        HorizontalDivider()

        PlayerHandSection(
            cards = state.playerHand,
            selectedCards = selectedCards,
            phase = state.phase,
            isHumanTurn = state.isHumanTurn,
            legalCards = state.legalCardsForHuman,
            onCardClick = onCardClick,
            maxCardW = LANDSCAPE_HAND_CARD_W,
            maxCardH = LANDSCAPE_HAND_CARD_H
        )
    }
}
