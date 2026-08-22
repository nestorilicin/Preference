package etf.nestor.preference.ui.screen.composables

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Rank
import etf.nestor.preference.domain.model.Suit

@SuppressLint("LocalContextResourcesRead")
@Composable
fun CardFront(card: PlayingCard, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val resName = card.toDrawableResName()
    val resId = remember(resName) {
        context.resources.getIdentifier(resName, "drawable", context.packageName)

    }
    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = resName,
            contentScale = ContentScale.Fit,
            modifier = modifier.clip(RoundedCornerShape(4.dp))
        )
    } else {
        // Fallback ako slika nije nađena
        Box(
            modifier = modifier
                .background(Color.White, RoundedCornerShape(4.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = resName.take(8), fontSize = 7.sp, textAlign = TextAlign.Center)
        }
    }
}

private fun PlayingCard.toDrawableResName(): String {
    val suitStr = when (suit) {
        Suit.SPADES -> "s"
        Suit.DIAMONDS -> "d"
        Suit.HEARTS -> "h"
        Suit.CLUBS -> "c"
        Suit.NO_SUIT -> "s"
    }
    val rankStr = when (rank) {
        Rank.SEVEN -> "7"
        Rank.EIGHT -> "8"
        Rank.NINE -> "9"
        Rank.TEN -> "10"
        Rank.JACK -> "j"
        Rank.QUEEN -> "q"
        Rank.KING -> "k"
        Rank.ACE -> "a"
    }
    return "card_${suitStr}_${rankStr}"
}