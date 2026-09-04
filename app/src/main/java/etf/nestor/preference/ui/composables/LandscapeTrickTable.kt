package etf.nestor.preference.ui.composables

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import etf.nestor.preference.domain.game.GameSnapshot

// Koliko su karte botova uvučene od ivice stola ka sredini, kao procenat širine stola
// (a ne fiksan dp) — da ostanu u liniji sa Bot 2 / Bot 1 kolonom u ScoreBar-u na svim
// veličinama ekrana. Po potrebi fino podesiti nakon testiranja na uređaju.
private const val LANDSCAPE_BOT_CARD_INSET_FRACTION = 0.2f

/**
 * Sto u landscape modu (samo tekući stih — prikaz kupa je na pozivaocu, isto kao u portretu
 * gde se TalonArea/TrickArea biraju spolja preko if/else na state.phase).
 */
@Composable
fun LandscapeTable(state: GameSnapshot, cardW: Dp, cardH: Dp, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val botCardInset = maxWidth * LANDSCAPE_BOT_CARD_INSET_FRACTION

        CardSlot(
            state, 2, cardW, cardH,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = botCardInset, y = 0.dp)
        )
        CardSlot(
            state, 1, cardW, cardH,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = -botCardInset, y = 0.dp)
        )
        CardSlot(
            state, 0, cardW, cardH,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = cardH / 6)
        )
    }
}
