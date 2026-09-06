package etf.nestor.preference.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import etf.nestor.preference.domain.game.GameSnapshot

@Composable
internal fun CardSlot(
    state: GameSnapshot,
    position: Int,
    cardW: Dp,
    cardH: Dp,
    modifier: Modifier = Modifier
) {
    val playedCard = state.currentTrick.find { it.playerPosition == position }
    Box(modifier = modifier.size(cardW, cardH)) {
        if (playedCard != null) {
            CardFront(
                card = playedCard.card,
                modifier = Modifier.size(cardW, cardH)
            )
        }
    }
}
