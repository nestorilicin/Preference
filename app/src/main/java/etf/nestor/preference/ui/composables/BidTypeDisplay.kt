package etf.nestor.preference.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import etf.nestor.preference.R
import etf.nestor.preference.domain.bidding.BidType
import etf.nestor.preference.domain.game.GameSnapshot

@Composable
fun BidType.displayName(): String = stringResource(
    when (this) {
        BidType.SPADES -> R.string.bid_spades
        BidType.DIAMONDS -> R.string.bid_diamonds
        BidType.HEARTS -> R.string.bid_hearts
        BidType.CLUBS -> R.string.bid_clubs
        BidType.MISERE -> R.string.bid_misere
        BidType.SANS -> R.string.bid_sans
    }
)

@Composable
fun GameSnapshot.currentGameLabel(): String? {
    val gameType = currentGameType ?: return null
    return when (gameType) {
        BidType.SANS, BidType.MISERE -> gameType.displayName()
        else -> stringResource(R.string.label_current_trump, gameType.displayName())
    }
}
