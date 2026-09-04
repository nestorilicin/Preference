package etf.nestor.preference.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import etf.nestor.preference.domain.game.GameSnapshot
import etf.nestor.preference.domain.game.HandPhase

/**
 * Kup na stolu: dve karte okrenute licem dole dok traje licitacija (niko ne zna šta je u kupu).
 * Kada licitacija završi, kup se "okrene" — GameSnapshot.talon tada nosi prave karte, ali samo
 * za igrača koji ga je uzeo (inače prazno mesto — bot ga je uzeo). Mesto za obe karte je UVEK
 * iste veličine i pozicije dok je kup na stolu (licitacija → uzimanje), pa "okretanje" samo
 * promeni sadržaj kartice, bez pomeranja ičega oko sebe. Posle toga (kup je pokupljen, faza
 * ide dalje) se ništa ne prikazuje.
 */
@Composable
fun TalonArea(
    state: GameSnapshot,
    modifier: Modifier = Modifier,
    cardW: Dp = HAND_CARD_W,
    cardH: Dp = HAND_CARD_H,
    gap: Dp = 8.dp,
    // false u landscape modu — da ne "otme" celu širinu reda kad stoji pored tekućeg stiha
    fillWidth: Boolean = true
) {
    val onTable = state.phase == HandPhase.BIDDING || state.phase == HandPhase.TALON_PICKUP
    if (!onTable) return

    val duringBidding = state.phase == HandPhase.BIDDING
    val talon = state.talon

    Row(
        modifier = (if (fillWidth) Modifier.fillMaxWidth() else Modifier).then(modifier),
        horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally)
    ) {
        repeat(2) { index ->
            Box(modifier = Modifier.size(cardW, cardH)) {
                if (duringBidding) {
                    CardBack(modifier = Modifier.size(cardW, cardH))
                } else {
                    talon.getOrNull(index)?.let { card ->
                        CardFront(card = card, modifier = Modifier.size(cardW, cardH))
                    }
                }
            }
        }
    }
}
