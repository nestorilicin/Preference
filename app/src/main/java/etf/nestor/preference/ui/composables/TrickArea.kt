package etf.nestor.preference.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import etf.nestor.preference.domain.game.GameSnapshot

// Podrazumevana veličina (portrait) — landscape uvek prosleđuje sopstvenu veličinu
// (LANDSCAPE_HAND_CARD_W/H) preko LandscapeTable, pa na nju ovo ne utiče
private val CARD_WIDTH = 64.dp
private val CARD_HEIGHT = 96.dp

/**
 * Prikaz tekućeg stiha rasporedjen kao za stolom: bot koji sedi levo od igrača (pozicija 2)
 * i bot koji sedi desno od igrača (pozicija 1) u gornjem redu, igrač (pozicija 0) na sredini
 * u drugom redu, malo ispod. Dok neko ne odigra kartu, na njegovom mestu je samo prazan prostor.
 *
 * @param cardW/cardH veličina karte — podesivo da bi se poklopilo sa veličinom karata kupa
 * (npr. u landscape modu, gde stih i kup treba da budu iste veličine).
 * @param fillWidth false u landscape modu — da ne "otme" celu širinu reda kad stoji pored kupa
 * (isto kao TalonArea.fillWidth), inače kup ostane bez mesta i izleti van vidljivog dela ekrana.
 */
@Composable
fun TrickArea(
    state: GameSnapshot,
    cardW: Dp = CARD_WIDTH,
    cardH: Dp = CARD_HEIGHT,
    fillWidth: Boolean = true
) {
    val gap = cardW / 2
    val vGap = cardH / 20

    Column(
        modifier = if (fillWidth) Modifier.fillMaxWidth() else Modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            listOf(2, 1).forEach { pos -> CardSlot(state, pos, cardW, cardH) }
        }
        Spacer(Modifier.height(vGap))
        CardSlot(state, 0, cardW, cardH)
    }
}
