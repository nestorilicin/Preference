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

private val CARD_WIDTH = 64.dp
private val CARD_HEIGHT = 96.dp

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
