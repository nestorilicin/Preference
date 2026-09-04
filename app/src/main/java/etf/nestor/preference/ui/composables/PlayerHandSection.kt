package etf.nestor.preference.ui.composables

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import etf.nestor.preference.domain.game.HandPhase
import etf.nestor.preference.domain.model.PlayingCard

// ─── Karte igrača (dno ekrana) ───────────────────────────────────────────

@Composable
fun PlayerHandSection(
    cards: List<PlayingCard>,
    selectedCards: Set<PlayingCard>,
    phase: HandPhase,
    isHumanTurn: Boolean,
    legalCards: List<PlayingCard>,
    onCardClick: (PlayingCard) -> Unit,
    maxCardW: Dp = HAND_CARD_W,
    maxCardH: Dp = HAND_CARD_H
) {
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
    ) {
        // Zaglavlje: malo dugme za pregled uzetih stihova (isto u portretu i landscape-u) —
        // klikljivo je samo dugme, ne ceo red


        // Landscape → uvijek jedan red; Portrait → ≤5 jedan red, >5 dva reda
        // Veličina karte: max HAND_CARD_W, smanji se ako ne stane (npr. 12 karata)
        @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val spacing = 4.dp
            val cardsPerRow = when {
                isLandscape -> cards.size.coerceAtLeast(1)
                cards.size <= 5 -> cards.size.coerceAtLeast(1)
                else -> (cards.size + 1) / 2
            }
            val availableW = maxWidth - spacing * (cardsPerRow - 1)
            val cardW = minOf(maxCardW, availableW / cardsPerRow)
            val cardH = cardW * (maxCardH / maxCardW)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                if (isLandscape || cards.size <= 5) {
                    // Jedan centriran red — razmak SAMO izmedju karata (spacedBy), ne i posle
                    // poslednje; inače red bude za jedan `spacing` širi nego što je predviđeno u
                    // availableW/cardW proračunu iznad, pa se krajnje (prve/poslednje) karte
                    // centriranjem seku na ivici i deluju manje.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)
                    ) {
                        cards.forEach { card ->
                            CardTile(
                                card,
                                cardW,
                                cardH,
                                selectedCards,
                                phase,
                                isHumanTurn,
                                legalCards,
                                onCardClick
                            )
                        }
                    }
                } else {
                    // Dva centrirana reda (portrait, >5 karata)
                    val mid = (cards.size + 1) / 2
                    listOf(cards.take(mid), cards.drop(mid)).forEach { rowCards ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)
                        ) {
                            rowCards.forEach { card ->
                                CardTile(
                                    card,
                                    cardW,
                                    cardH,
                                    selectedCards,
                                    phase,
                                    isHumanTurn,
                                    legalCards,
                                    onCardClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }


}

@Composable
private fun CardTile(
    card: PlayingCard,
    cardW: Dp,
    cardH: Dp,
    selectedCards: Set<PlayingCard>,
    phase: HandPhase,
    isHumanTurn: Boolean,
    legalCards: List<PlayingCard>,
    onCardClick: (PlayingCard) -> Unit
) {
    val isSelected = card in selectedCards
    val isClickable = isHumanTurn && (phase == HandPhase.PLAYING || phase == HandPhase.DISCARDING)
    val isIllegal = phase == HandPhase.PLAYING && isHumanTurn
            && legalCards.isNotEmpty() && card !in legalCards

    Box(modifier = Modifier.then(if (isSelected) Modifier.offset(y = (-8).dp) else Modifier)) {
        CardFront(
            card = card,
            modifier = Modifier
                .size(cardW, cardH)
                .alpha(if (isIllegal) 0.4f else 1f)
                .then(
                    if (isSelected) Modifier.border(
                        2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)
                    ) else Modifier
                )
                .then(if (isClickable) Modifier.clickable { onCardClick(card) } else Modifier)
        )
    }
}
