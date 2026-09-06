package etf.nestor.preference.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import etf.nestor.preference.R
import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Rank
import etf.nestor.preference.domain.model.Suit

@Composable
fun CardFront(card: PlayingCard, modifier: Modifier = Modifier) {
    val resId = card.toDrawableRes()
    if (resId != 0) {

        Image(
            painter = painterResource(id = resId),
            contentDescription = "${card.rank} ${card.suit}",
            contentScale = ContentScale.Fit,
            modifier = modifier
                .clip(RoundedCornerShape(4.dp))
        )
    } else {

        Box(
            modifier = modifier
                .background(Color.White, RoundedCornerShape(4.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "${card.rank} ${card.suit}".take(8), fontSize = 7.sp, textAlign = TextAlign.Center)
        }
    }
}

private fun PlayingCard.toDrawableRes(): Int = when (suit) {
    Suit.SPADES, Suit.NO_SUIT -> when (rank) {
        Rank.SEVEN -> R.drawable.card_s_7
        Rank.EIGHT -> R.drawable.card_s_8
        Rank.NINE -> R.drawable.card_s_9
        Rank.TEN -> R.drawable.card_s_10
        Rank.JACK -> R.drawable.card_s_j
        Rank.QUEEN -> R.drawable.card_s_q
        Rank.KING -> R.drawable.card_s_k
        Rank.ACE -> R.drawable.card_s_a
    }

    Suit.DIAMONDS -> when (rank) {
        Rank.SEVEN -> R.drawable.card_d_7
        Rank.EIGHT -> R.drawable.card_d_8
        Rank.NINE -> R.drawable.card_d_9
        Rank.TEN -> R.drawable.card_d_10
        Rank.JACK -> R.drawable.card_d_j
        Rank.QUEEN -> R.drawable.card_d_q
        Rank.KING -> R.drawable.card_d_k
        Rank.ACE -> R.drawable.card_d_a
    }

    Suit.HEARTS -> when (rank) {
        Rank.SEVEN -> R.drawable.card_h_7
        Rank.EIGHT -> R.drawable.card_h_8
        Rank.NINE -> R.drawable.card_h_9
        Rank.TEN -> R.drawable.card_h_10
        Rank.JACK -> R.drawable.card_h_j
        Rank.QUEEN -> R.drawable.card_h_q
        Rank.KING -> R.drawable.card_h_k
        Rank.ACE -> R.drawable.card_h_a
    }

    Suit.CLUBS -> when (rank) {
        Rank.SEVEN -> R.drawable.card_c_7
        Rank.EIGHT -> R.drawable.card_c_8
        Rank.NINE -> R.drawable.card_c_9
        Rank.TEN -> R.drawable.card_c_10
        Rank.JACK -> R.drawable.card_c_j
        Rank.QUEEN -> R.drawable.card_c_q
        Rank.KING -> R.drawable.card_c_k
        Rank.ACE -> R.drawable.card_c_a
    }
}
