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
import etf.nestor.preference.ui.composables.LandscapeTable
import etf.nestor.preference.ui.composables.PlayerHandSection
import etf.nestor.preference.ui.composables.ScoreBar
import etf.nestor.preference.ui.composables.TalonArea
import etf.nestor.preference.ui.composables.toLocalizedString
import etf.nestor.preference.ui.stateholders.SingleplayerViewModel

// ─── Landscape raspored (bez skrola) ─────────────────────────────────────
// Botovi više nemaju svoju kolonu levo/desno (BotHandArea ukinut) — ime, "govor" tokom
// licitacije i poeni svih igrača (uključujući botove) sada su deo ScoreBar-a.

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
    onDiscardConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = LANDSCAPE_MARGIN, vertical = 6.dp)
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
        HorizontalDivider()
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
}
