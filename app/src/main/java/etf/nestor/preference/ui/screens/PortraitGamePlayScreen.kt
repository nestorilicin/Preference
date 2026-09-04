package etf.nestor.preference.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
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
import etf.nestor.preference.ui.composables.ActionArea
import etf.nestor.preference.ui.composables.BotHandArea
import etf.nestor.preference.ui.composables.HAND_CARD_H
import etf.nestor.preference.ui.composables.HAND_CARD_W
import etf.nestor.preference.ui.composables.PlayerHandSection
import etf.nestor.preference.ui.composables.ScoreBar
import etf.nestor.preference.ui.composables.TalonArea
import etf.nestor.preference.ui.composables.TrickArea
import etf.nestor.preference.ui.composables.toLocalizedString
import etf.nestor.preference.ui.stateholders.SingleplayerViewModel

// ─── Portret raspored (skrolabilan srednji deo) ──────────────────────────

// Minimalna visina zone poruka+akcije u portretu — dovoljna za najveći mogući sadržaj
// (npr. licitacija: tekst + red dugmadi), da promena faze ne menja preostali prostor za sto/kup
private val PORTRAIT_MESSAGE_ACTION_HEIGHT = 130.dp

@Composable
fun PortraitGamePlayScreen(
    state: GameSnapshot,
    viewModel: SingleplayerViewModel,
    selectedCards: Set<PlayingCard>,
    onCardClick: (PlayingCard) -> Unit,
    onDiscardConfirm: () -> Unit,

) {
    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {

        // Gornji bar sa poenima i stihovima
        ScoreBar(state, viewModel)

        // Botovi + sto (weight(1f) — zauzima sav preostali prostor iznad poruke/akcija)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Botovi — levo bot koji sedi levo od igrača (2), desno bot koji sedi desno (1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BotHandArea(
                    name = stringResource(R.string.player_name_bot2),
                    position = 2,
                    state = state,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                BotHandArea(
                    name = stringResource(R.string.player_name_bot1),
                    position = 1,
                    state = state,
                    modifier = Modifier.weight(1f)
                )
            }

            // Tekući stih ILI kup — nikad oba istovremeno (kup je na stolu samo tokom
            // licitacije/uzimanja, kada još nema odigranih karata u stihu), pa se prikazuje
            // tačno jedno od to dvoje, centrirano i horizontalno i vertikalno u preostalom
            // prostoru (koji je sada konstantan — vidi napomenu ispod uz poruku/akcije).
            // Karte kupa su iste veličine kao karte u ruci igrača. Kup je okrenut licem dole
            // tokom licitacije, otkriven tokom TALON_PICKUP (samo igraču koji ga uzima)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
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
        }

        // Poruka + akcioni deo — zajedno u koloni sa MINIMALNOM fiksnom visinom, ista bez
        // obzira na fazu (ActionArea nekad ne prikazuje ništa, nekad ceo red dugmadi).
        // Time preostali prostor iznad (botovi+sto/kup) ostaje konstantan i kup se
        // više ne pomera kad se karte "okrenu", a ostaje lepo centriran
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = PORTRAIT_MESSAGE_ACTION_HEIGHT)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = state.message.toLocalizedString(),
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

        // Karte igrača (dno, fiksno)
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
