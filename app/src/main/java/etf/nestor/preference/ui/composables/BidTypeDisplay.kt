package etf.nestor.preference.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import etf.nestor.preference.R
import etf.nestor.preference.domain.bidding.BidType
import etf.nestor.preference.domain.game.GameSnapshot

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

/**
 * Tekst trenutno najavljene igre za prikaz na stolu — npr. "Adut: Herc" za igre u boji,
 * ili samo "Sans"/"Betl" (nemaju adut). Null dok igra još nije najavljena (licitacija,
 * uzimanje kupa, bacanje karata) ili kad ruka nije u toku.
 */
@Composable
fun GameSnapshot.currentGameLabel(): String? {
    val gameType = currentGameType ?: return null
    return when (gameType) {
        BidType.SANS, BidType.MISERE -> gameType.displayName()
        else -> stringResource(R.string.label_current_trump, gameType.displayName())
    }
}
