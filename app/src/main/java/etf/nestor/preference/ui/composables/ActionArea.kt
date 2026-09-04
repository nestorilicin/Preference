package etf.nestor.preference.ui.composables

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import etf.nestor.preference.R
import etf.nestor.preference.domain.bidding.BidAction
import etf.nestor.preference.domain.bidding.BidType
import etf.nestor.preference.domain.game.GameSnapshot
import etf.nestor.preference.domain.game.HandPhase
import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.ui.stateholders.SingleplayerViewModel

private val GROUP_BUTTON_WIDTH = 120.dp

@Composable
fun ActionArea(
    state: GameSnapshot,
    viewModel: SingleplayerViewModel,
    selectedCards: Set<PlayingCard>,
    onDiscardConfirm: () -> Unit,
    contentPadding: Dp = 12.dp,
    verticalButtons: Boolean = false
) {
    val hasButtons = state.isHumanTurn && when (state.phase) {
        HandPhase.BIDDING, HandPhase.TALON_PICKUP, HandPhase.TRUMP_DECLARATION, HandPhase.FOLLOW -> true
        HandPhase.DISCARDING -> selectedCards.size == 2
        else -> false
    }
    if (!hasButtons) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(contentPadding).fillMaxWidth()) {
            when (state.phase) {

                HandPhase.BIDDING -> {
                    ButtonGroup(vertical = verticalButtons) {
                        GroupButton(
                            text = stringResource(R.string.btn_pass),
                            onClick = { viewModel.bid(BidAction.PASS) },
                            outlined = true
                        )
                        val nextBidValue = state.biddingHighestBid + 1
                        if (nextBidValue in 2..7) {
                            val nextBidAction = bidActionFromValue(nextBidValue)
                            if (nextBidAction != null) {
                                GroupButton(
                                    text = stringResource(R.string.btn_bid_n, nextBidValue),
                                    onClick = { viewModel.bid(nextBidAction) }
                                )
                            }
                        }
                    }
                }

                HandPhase.TALON_PICKUP -> {
                    CenteredSingleButton {
                        Button(
                            onClick = { viewModel.pickupTalon() },
                            modifier = Modifier.width(GROUP_BUTTON_WIDTH)
                        ) {
                            Text(stringResource(R.string.btn_take_talon))
                        }
                    }
                }

                HandPhase.DISCARDING -> {
                    CenteredSingleButton {
                        Button(
                            onClick = onDiscardConfirm,
                            modifier = Modifier.width(GROUP_BUTTON_WIDTH)
                        ) {
                            Text(stringResource(R.string.btn_confirm_discard))
                        }
                    }
                }

                HandPhase.TRUMP_DECLARATION -> {
                    ButtonGroup(vertical = verticalButtons, scrollable = true) {
                        BidType.entries.filter { it.strength >= state.minimumBidValue }
                            .forEach { gameType ->
                                GroupButton(
                                    text = gameType.displayName(),
                                    onClick = { viewModel.declareGame(gameType) }
                                )
                            }
                    }
                }

                HandPhase.FOLLOW -> {
                    ButtonGroup(vertical = verticalButtons) {
                        GroupButton(
                            text = stringResource(R.string.btn_follow_yes),
                            onClick = { viewModel.follow(true) }
                        )
                        GroupButton(
                            text = stringResource(R.string.btn_follow_no),
                            onClick = { viewModel.follow(false) },
                            outlined = true
                        )
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun ButtonGroup(
    vertical: Boolean,
    scrollable: Boolean = false,
    content: @Composable () -> Unit
) {
    if (vertical) {
        val modifier = if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) { content() }
    } else {
        val modifier = if (scrollable) Modifier.horizontalScroll(rememberScrollState()) else Modifier
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) { content() }
    }
}

/** Centrira jedno (samostalno) dugme fiksne širine unutar raspoloživog prostora. */
@Composable
private fun CenteredSingleButton(button: @Composable () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        button()
    }
}

/** Dugme fiksne, uniformne širine ([GROUP_BUTTON_WIDTH]) sa centriranim tekstom. */
@Composable
private fun GroupButton(text: String, onClick: () -> Unit, outlined: Boolean = false) {
    val label = @Composable {
        Text(text, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
    if (outlined) {
        OutlinedButton(onClick = onClick, modifier = Modifier.width(GROUP_BUTTON_WIDTH)) { label() }
    } else {
        Button(onClick = onClick, modifier = Modifier.width(GROUP_BUTTON_WIDTH)) { label() }
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
