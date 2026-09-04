package etf.nestor.preference.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import etf.nestor.preference.R
import etf.nestor.preference.domain.bidding.BidType

/** Lokalizovan naziv igre za prikaz — koriste ActionArea (najava igre) i GameMessage tekstovi. */
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
