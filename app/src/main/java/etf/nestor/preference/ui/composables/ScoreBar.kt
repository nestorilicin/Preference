package etf.nestor.preference.ui.composables

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import etf.nestor.preference.R
import etf.nestor.preference.domain.bidding.BotBidStatus
import etf.nestor.preference.domain.game.GameSnapshot
import etf.nestor.preference.domain.game.HandPhase
import etf.nestor.preference.ui.stateholders.SingleplayerViewModel


private val BID_BUBBLE_HEIGHT = 16.dp

@Composable
fun ScoreBar(
    state: GameSnapshot,
    viewModel: SingleplayerViewModel,
    compact: Boolean = false,
) {
    val youFallback = stringResource(R.string.player_name_you)
    val names = listOf(
        state.playerNames.getOrElse(0) { youFallback },
        stringResource(R.string.player_name_bot1),
        stringResource(R.string.player_name_bot2)
    )

    val order = listOf(2, 0, 1)
    val sheetDialogPlayer by viewModel.sheetDialogPlayer.collectAsState()

    val avatarPath by viewModel.avatarPath.collectAsState()
    val country by viewModel.country.collectAsState()
    val avatarBitmap = remember(avatarPath) {
        avatarPath?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() }
    }

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compact) 8.dp else 16.dp,
                    vertical = if (compact) 2.dp else 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                order.forEach { i ->
                    val center = state.scoreSheets[i]?.center ?: 0
                    if (i == 0) {
                        PlayerScoreColumn(
                            name = names[i],
                            center = center,
                            avatarBitmap = avatarBitmap,
                            country = country,
                            compact = compact,
                            onClick = { viewModel.openScoreSheet(i) }
                        )
                    } else {
                        val bidStatus =
                            if (state.phase == HandPhase.BIDDING) state.biddingBotStatus[i] else null
                        BotScoreColumn(
                            name = names[i],
                            center = center,
                            bidStatus = bidStatus,
                            compact = compact,
                            onClick = { viewModel.openScoreSheet(i) }
                        )
                    }
                }
            }
        }
    }

    val dialogPlayer = sheetDialogPlayer
    if (dialogPlayer != null) {
        ScoreSheetDialog(
            playerPosition = dialogPlayer,
            sheet = state.scoreSheets[dialogPlayer],
            allNames = names,
            onDismiss = { viewModel.closeScoreSheet() }
        )
    }
}

/**
 * Kolona za igrača: ime, pa slika i država (ako postoje), pa poeni. U landscape modu (compact)
 * previše je sadržaja za jednu kolonu, pa se deli na dve pored — ime+poeni, slika+država.
 */
@Composable
private fun PlayerScoreColumn(
    name: String,
    center: Int,
    avatarBitmap: ImageBitmap?,
    country: String?,
    compact: Boolean,
    onClick: () -> Unit
) {
    if (compact) {
        Row(
            modifier = Modifier.clickable(onClick = onClick),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                NameText(name)
                ScoreText(center, compact = true)
            }
            if (avatarBitmap != null || country != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap,
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                        )
                    }
                    if (country != null) {
                        CountryText(country)
                    }
                }
            }
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            NameText(name)
            if (avatarBitmap != null) {
                Image(
                    bitmap = avatarBitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                )
            }
            if (country != null) {
                CountryText(country)
            }
            ScoreText(center, compact = false)
        }
    }
}

/**
 * Kolona za bota: ime, pa ono što bot trenutno "govori" tokom licitacije, pa poeni. U portretu je
 * to oblačić ispod imena (rezervisanog prostora, da poeni ne skaču); u landscape modu (compact)
 * ide u istom redu kao ime ("Ime: status"), da se uštedi vertikalni prostor.
 */
@Composable
private fun BotScoreColumn(
    name: String,
    center: Int,
    bidStatus: BotBidStatus?,
    compact: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        if (compact) {
            Text(
                text = if (bidStatus != null) "$name: ${bidStatus.toLocalizedText()}" else name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1
            )
        } else {
            NameText(name)
            Box(
                modifier = Modifier.height(BID_BUBBLE_HEIGHT),
                contentAlignment = Alignment.Center
            ) {
                if (bidStatus != null) BotBidBubble(bidStatus)
            }
        }
        ScoreText(center, compact)
    }
}

@Composable
private fun NameText(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

@Composable
private fun CountryText(country: String) {
    Text(
        text = country,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
    )
}

@Composable
private fun ScoreText(center: Int, compact: Boolean) {
    Text(
        text = "$center",
        style = if (compact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = if (center <= 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
    )
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
