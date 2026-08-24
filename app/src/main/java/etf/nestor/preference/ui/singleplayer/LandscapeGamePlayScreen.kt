package etf.nestor.preference.ui.singleplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import etf.nestor.preference.R
import etf.nestor.preference.domain.game.GameSnapshot
import etf.nestor.preference.domain.game.HandPhase
import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.ui.common.BotHandArea
import etf.nestor.preference.ui.common.LandscapeTable
import etf.nestor.preference.ui.common.TalonArea

// ─── Landscape raspored (bez skrola — botovi skroz levo/desno) ──────────

private val LANDSCAPE_BOT_COLUMN_WIDTH = 64.dp

// Igračeva ruka i sto (tekući stih + kup) — ista veličina karata svuda u landscape modu
private val LANDSCAPE_HAND_CARD_W = 54.dp
private val LANDSCAPE_HAND_CARD_H = 81.dp
private val LANDSCAPE_MARGIN = 20.dp

// Minimalna visina zone poruka+akcije u landscape modu — dovoljna za najveći mogući sadržaj
// (npr. licitacija: tekst + red dugmadi), da promena faze ne menja preostali prostor za sto/kup
private val LANDSCAPE_MESSAGE_ACTION_HEIGHT = 110.dp

@Composable
fun LandscapeGamePlayScreen(
    state: GameSnapshot,
    viewModel: SingleplayerViewModel,
    selectedCards: Set<PlayingCard>,
    onCardClick: (PlayingCard) -> Unit,
    onDiscardConfirm: () -> Unit
) {
    Row(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = LANDSCAPE_MARGIN, vertical = 6.dp)
    ) {
        // Bot koji sedi levo od igrača — skroz levo, ime rotirano
        BotHandArea(
            name = stringResource(R.string.player_name_bot2),
            position = 2,
            state = state,
            modifier = Modifier
                .width(LANDSCAPE_BOT_COLUMN_WIDTH)
                .fillMaxHeight(),
            vertical = true
        )

        // Sredina — sve ostalo, bez skrola
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = LANDSCAPE_MARGIN)
        ) {
            ScoreBar(state, viewModel, compact = true)

            // Isto kao u portretu — kup se prikazuje samo dok je na stolu (BIDDING/TALON_PICKUP),
            // inače tekući stih (LandscapeTable). Malo razmaka od ScoreBar-a iznad da ga karte
            // ne dodiruju
            if (state.phase == HandPhase.BIDDING || state.phase == HandPhase.TALON_PICKUP) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TalonArea(
                        state = state,
                        cardW = LANDSCAPE_HAND_CARD_W,
                        cardH = LANDSCAPE_HAND_CARD_H,
                        fillWidth = false
                    )
                }
            } else {
                LandscapeTable(
                    state = state,
                    cardW = LANDSCAPE_HAND_CARD_W,
                    cardH = LANDSCAPE_HAND_CARD_H,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                )
            }

            // Poruka + akcioni deo — zajedno u koloni sa MINIMALNOM fiksnom visinom, ista bez
            // obzira na fazu (ActionArea nekad ne prikazuje ništa, nekad ceo red dugmadi).
            // Time preostali prostor iznad (LandscapeTable/kup) ostaje konstantan i kup se
            // više ne pomera kad se karte "okrenu"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = LANDSCAPE_MESSAGE_ACTION_HEIGHT),
                verticalArrangement = Arrangement.Center
            ) {
                // Poruka — jedan red, bez kartice
                Text(
                    text = state.message.toLocalizedString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )

                // Akcioni deo (faza-specifičan)
                ActionArea(
                    state = state,
                    viewModel = viewModel,
                    selectedCards = selectedCards,
                    onDiscardConfirm = onDiscardConfirm,
                    contentPadding = 6.dp
                )
            }

            // Karte igrača
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

        // Bot koji sedi desno od igrača — skroz desno, ime rotirano
        BotHandArea(
            name = stringResource(R.string.player_name_bot1),
            position = 1,
            state = state,
            modifier = Modifier
                .width(LANDSCAPE_BOT_COLUMN_WIDTH)
                .fillMaxHeight(),
            vertical = true
        )
    }
}
