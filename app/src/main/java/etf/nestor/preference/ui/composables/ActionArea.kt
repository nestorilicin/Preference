package etf.nestor.preference.ui.composables

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import etf.nestor.preference.R
import etf.nestor.preference.domain.bidding.BidAction
import etf.nestor.preference.domain.bidding.BidType
import etf.nestor.preference.domain.game.GameSnapshot
import etf.nestor.preference.domain.game.HandPhase
import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.ui.stateholders.SingleplayerViewModel

@Composable
fun ActionArea(
    state: GameSnapshot,
    viewModel: SingleplayerViewModel,
    selectedCards: Set<PlayingCard>,
    onDiscardConfirm: () -> Unit,
    contentPadding: Dp = 12.dp
) {
    if (!state.isHumanTurn && state.phase != HandPhase.PLAYING) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(contentPadding)) {
            when (state.phase) {

                HandPhase.BIDDING -> {
                    if (state.isHumanTurn) {
                        Text(
                            stringResource(R.string.label_bid),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { viewModel.bid(BidAction.PASS) }) {
                                Text(stringResource(R.string.btn_pass))
                            }
                            val nextBidValue = state.biddingHighestBid + 1
                            if (nextBidValue in 2..7) {
                                val nextBidAction = bidActionFromValue(nextBidValue)
                                if (nextBidAction != null) {
                                    Button(onClick = { viewModel.bid(nextBidAction) }) {
                                        Text(stringResource(R.string.btn_bid_n, nextBidValue))
                                    }
                                }
                            }
                        }
                    }
                }

                HandPhase.TALON_PICKUP -> {
                    if (state.isHumanTurn) {
                        Button(
                            onClick = { viewModel.pickupTalon() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.btn_take_talon))
                        }
                    }
                }

                HandPhase.DISCARDING -> {
                    if (state.isHumanTurn) {
                        Text(
                            stringResource(R.string.label_discard, selectedCards.size),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        if (selectedCards.size == 2) {
                            Button(
                                onClick = onDiscardConfirm,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.btn_confirm_discard))
                            }
                        } else {
                            Text(
                                stringResource(R.string.hint_click_cards),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HandPhase.TRUMP_DECLARATION -> {
                    if (state.isHumanTurn) {
                        Text(
                            stringResource(R.string.label_declare_game, state.minimumBidValue),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BidType.entries.filter { it.strength >= state.minimumBidValue }
                                .forEach { gameType ->
                                    Button(onClick = { viewModel.declareGame(gameType) }) {
                                        Text(gameType.displayName())
                                    }
                                }
                        }
                    }
                }

                HandPhase.FOLLOW -> {
                    if (state.isHumanTurn) {
                        Text(
                            stringResource(R.string.label_follow),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.follow(true) }) {
                                Text(stringResource(R.string.btn_follow_yes))
                            }
                            OutlinedButton(onClick = { viewModel.follow(false) }) {
                                Text(stringResource(R.string.btn_follow_no))
                            }
                        }
                    }
                }

                HandPhase.PLAYING -> {
                    if (state.isHumanTurn) {
                        Text(
                            stringResource(R.string.your_turn_play),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            stringResource(R.string.bots_playing),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {}
            }
        }
    }
}

private fun bidActionFromValue(value: Int): BidAction? {
    return when (value) {
        2 -> BidAction.TWO
        3 -> BidAction.THREE
        4 -> BidAction.FOUR
        5 -> BidAction.FIVE
        6 -> BidAction.SIX
        7 -> BidAction.SEVEN
        else -> null
    }
}
