package etf.nestor.preference.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import etf.nestor.preference.R
import etf.nestor.preference.domain.bidding.BotBidStatus
import etf.nestor.preference.domain.game.GameSnapshot
import etf.nestor.preference.domain.game.HandPhase

// Fiksna visina rezervisana za "govor" bota tokom licitacije — uvek zauzeta (prazna ili sa
// tekstom) da se karte ne bi pomerale kad tekst dodje/nestane
private val BID_BUBBLE_HEIGHT = 16.dp
// U landscape modu je tekst rotiran 90° pa njegova "visina" u koloni odgovara širini teksta —
// dovoljno velika da stane i najduži mogući tekst ("Razmišlja…")
private val BID_BUBBLE_HEIGHT_VERTICAL = 64.dp

@Composable
fun BotHandArea(
    name: String,
    position: Int,
    state: GameSnapshot,
    modifier: Modifier = Modifier,
    // Vertikalni prikaz (ime rotirano, karte u koloni) — koristi se u landscape modu
    // kad bot sedi skroz levo/desno i nema mesta za horizontalni raspored
    vertical: Boolean = false
) {
    val cardsLeft = when (state.phase) {
        HandPhase.PLAYING, HandPhase.FINISHED -> state.handSizes.getOrElse(position) { 0 }
        else -> 10
    }

    // Šta bot trenutno "govori" — postoji samo tokom faze licitacije
    val bidStatus = if (state.phase == HandPhase.BIDDING) state.biddingBotStatus[position] else null


    if (vertical) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.verticalText()
            )
            Spacer(Modifier.height(6.dp))
            // Rezervisan prostor za "govor" bota — uvek prisutan (i prazan) da se ime i
            // karte ne bi pomerali kad tekst dodje/nestane
            Box(
                modifier = Modifier.height(BID_BUBBLE_HEIGHT_VERTICAL),
                contentAlignment = Alignment.Center
            ) {
                if (bidStatus != null) {
                    BotBidBubble(bidStatus, modifier = Modifier.verticalText())
                }
            }
            Spacer(Modifier.height(12.dp))
            // Karte rotirane za 90° — u koloni izgledaju kao pravi (horizontalni) snop karata
            // (veličina uvećana 10% u odnosu na osnovnu, da bude bolje vidljivo u landscape modu)
            Column(verticalArrangement = Arrangement.spacedBy((-22).dp)) {
                repeat(minOf(cardsLeft, 10)) {
                    Box(
                        modifier = Modifier.size(53.dp, 35.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CardBack(
                            modifier = Modifier
                                .size(35.dp, 53.dp)
                                .rotate(-90f)
                        )
                    }
                }
            }
        }
    } else {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            // Rezervisan prostor za "govor" bota — uvek prisutan (i prazan) da se karte
            // ne bi pomerale kad tekst dodje/nestane
            Box(
                modifier = Modifier.height(BID_BUBBLE_HEIGHT),
                contentAlignment = Alignment.Center
            ) {
                if (bidStatus != null) {
                    BotBidBubble(bidStatus)
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy((-14).dp)) {
                    repeat(minOf(cardsLeft, 10)) {
                        CardBack(modifier = Modifier.size(28.dp, 42.dp))
                    }
                }
            }
        }
    }


}

/** Mali "oblačić" teksta koji pokazuje šta bot trenutno radi tokom licitacije. */
@Composable
private fun BotBidBubble(status: BotBidStatus, modifier: Modifier = Modifier) {
    Text(
        text = status.toLocalizedText(),
        style = MaterialTheme.typography.labelSmall,
        fontStyle = if (status == BotBidStatus.Thinking) FontStyle.Italic else FontStyle.Normal,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
        modifier = modifier
    )
}

@Composable
private fun BotBidStatus.toLocalizedText(): String = when (this) {
    BotBidStatus.Thinking -> stringResource(R.string.bid_status_thinking)
    BotBidStatus.Pass -> stringResource(R.string.btn_pass)
    BotBidStatus.Good -> stringResource(R.string.bid_status_good)
}

/**
 * Rotira tekst za -90° i pravilno ga smešta u layout (Modifier.rotate sam po sebi ne menja
 * zauzeti prostor pa rotiran tekst inače "curi" van svoje kutije i preklapa susedne elemente
 * u koloni). Zamenjuje širinu i visinu koje se prijavljuju roditelju i centrira sadržaj.
 */
private fun Modifier.verticalText(): Modifier = this
    .layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.height, placeable.width) {
            placeable.place(
                x = -(placeable.width / 2 - placeable.height / 2),
                y = -(placeable.height / 2 - placeable.width / 2)
            )
        }
    }
    .rotate(-90f)
