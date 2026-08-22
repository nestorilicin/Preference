package etf.nestor.preference.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import etf.nestor.preference.R
import etf.nestor.preference.domain.bidding.BidAction
import etf.nestor.preference.domain.bidding.BidType
import etf.nestor.preference.domain.game.GameMessage
import etf.nestor.preference.domain.game.GameSnapshot
import etf.nestor.preference.domain.game.HandPhase
import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.ui.screen.composables.BotHandArea
import etf.nestor.preference.ui.screen.composables.CardBack
import etf.nestor.preference.ui.screen.composables.CardFront
import etf.nestor.preference.ui.screen.composables.ScoreBar
import etf.nestor.preference.ui.screen.composables.SimpleSpinner
import etf.nestor.preference.ui.screen.composables.TalonArea
import etf.nestor.preference.ui.screen.composables.TrickArea
import etf.nestor.preference.ui.screen.composables.WonTricksDialog
import etf.nestor.preference.ui.viewmodel.SingleplayerViewModel

// ─── Composable entry point ──────────────────────────────────────────────

@Composable
fun SinglePlayerGameScreen(viewModel: SingleplayerViewModel = hiltViewModel()) {
    val isGameStarted by viewModel.isGameStarted.collectAsState()

    if (!isGameStarted) {
        SetupScreen(viewModel)
    } else {
        val gameState by viewModel.gameState.collectAsState()
        val state = gameState
        if (state == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            GamePlayScreen(state = state, viewModel = viewModel)
        }
    }
}

// ─── Setup screen ──────────────────────────────────────────────────────────

@Composable
private fun SetupScreen(viewModel: SingleplayerViewModel) {
    val selectedScore by viewModel.selectedStartScore.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.subtitle_singleplayer),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(48.dp))

        Text(
            stringResource(R.string.label_starting_score),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))

        SimpleSpinner(
            value = selectedScore,
            options = viewModel.startScoreOptions,
            displayText = { stringResource(R.string.score_option, it) },
            onSelect = { viewModel.selectStartScore(it) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { viewModel.startGame() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                stringResource(R.string.btn_start_game),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

// ─── Game screen ────────────────────────────────────────────────────

@Composable
private fun GamePlayScreen(state: GameSnapshot, viewModel: SingleplayerViewModel) {
    var selectedCards by remember { mutableStateOf<Set<PlayingCard>>(emptySet()) }

    // Reset selekcije kada se promeni faza
    LaunchedEffect(state.phase) {
        selectedCards = emptySet()
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {

        // Gornji bar sa poenima i stihovima
        ScoreBar(state)

        // Srednji deo (scrollable)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Botovi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BotHandArea(
                    name = stringResource(R.string.player_name_bot1),
                    position = 1,
                    state = state,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                BotHandArea(
                    name = stringResource(R.string.player_name_bot2),
                    position = 2,
                    state = state,
                    modifier = Modifier.weight(1f)
                )
            }

            // Tekući stih
            TrickArea(state)

            // Kup (vidljiv tokom TALON_PICKUP)
            if (state.talon.isNotEmpty()) {
                TalonArea(state.talon)
            }

            // Poruka
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = state.message.toLocalizedString(),
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Akcioni deo (faza-specifičan)
            ActionArea(
                state = state,
                viewModel = viewModel,
                selectedCards = selectedCards,
                onDiscardConfirm = {
                    viewModel.discard(selectedCards.toList())
                }
            )
        }

        // Karte igrača (dno, fiksno)
        HorizontalDivider()
        PlayerHandSection(
            cards = state.playerHand,
            selectedCards = selectedCards,
            phase = state.phase,
            isHumanTurn = state.isHumanTurn,
            legalCards = state.legalCardsForHuman,
            wonTricks = state.wonTricksByPlayer[0] ?: emptyList(),
            onCardClick = { card ->
                when (state.phase) {
                    HandPhase.DISCARDING -> {
                        selectedCards = if (card in selectedCards) {
                            selectedCards - card
                        } else if (selectedCards.size < 2) {
                            selectedCards + card
                        } else {
                            selectedCards
                        }
                    }

                    HandPhase.PLAYING -> {
                        if (state.isHumanTurn) viewModel.playCard(card)
                    }

                    else -> {}
                }
            }
        )
    }
}


// ─── Akcioni deo ─────────────────────────────────────────────────────────

@Composable
private fun ActionArea(
    state: GameSnapshot,
    viewModel: SingleplayerViewModel,
    selectedCards: Set<PlayingCard>,
    onDiscardConfirm: () -> Unit
) {
    if (!state.isHumanTurn && state.phase != HandPhase.PLAYING) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
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

// ─── Dialog za uzete stihove ─────────────────────────────────────────────


// ─── Karte igrača (dno ekrana) ───────────────────────────────────────────

@Composable
private fun PlayerHandSection(
    cards: List<PlayingCard>,
    selectedCards: Set<PlayingCard>,
    phase: HandPhase,
    isHumanTurn: Boolean,
    legalCards: List<PlayingCard>,
    wonTricks: List<List<PlayingCard>>,
    onCardClick: (PlayingCard) -> Unit
) {
    var showWonDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
    ) {
        val labelColor =
            if (isHumanTurn && (phase == HandPhase.PLAYING || phase == HandPhase.DISCARDING))
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant

        // Zaglavlje: label + dugme za uzete stihove
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.your_cards, cards.size),
                style = MaterialTheme.typography.labelSmall,
                color = labelColor
            )
            if (wonTricks.isNotEmpty()) {
                Box(modifier = Modifier.clickable { showWonDialog = true }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CardBack(modifier = Modifier.size(20.dp, 30.dp))
                        Text(
                            text = stringResource(R.string.your_tricks_count, wonTricks.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))

        val isLandscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        // Landscape → uvijek jedan red; Portrait → ≤5 jedan red, >5 dva reda
        // Veličina karte: max HAND_CARD_W, smanji se ako ne stane (npr. 12 karata)
        @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val spacing = 4.dp
            val cardsPerRow = when {
                isLandscape -> cards.size.coerceAtLeast(1)
                cards.size <= 5 -> cards.size.coerceAtLeast(1)
                else -> (cards.size + 1) / 2
            }
            val availableW = maxWidth - spacing * (cardsPerRow - 1)
            val cardW = minOf(HAND_CARD_W, availableW / cardsPerRow)
            val cardH = cardW * (HAND_CARD_H / HAND_CARD_W)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                if (isLandscape || cards.size <= 5) {
                    // Jedan centriran red
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        cards.forEach { card ->
                            CardTile(
                                card,
                                cardW,
                                cardH,
                                selectedCards,
                                phase,
                                isHumanTurn,
                                legalCards,
                                onCardClick
                            )
                            Spacer(Modifier.width(spacing))
                        }
                    }
                } else {
                    // Dva centrirana reda (portrait, >5 karata)
                    val mid = (cards.size + 1) / 2
                    listOf(cards.take(mid), cards.drop(mid)).forEach { rowCards ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            rowCards.forEach { card ->
                                CardTile(
                                    card,
                                    cardW,
                                    cardH,
                                    selectedCards,
                                    phase,
                                    isHumanTurn,
                                    legalCards,
                                    onCardClick
                                )
                                Spacer(Modifier.width(spacing))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showWonDialog) {
        WonTricksDialog(
            playerName = stringResource(R.string.player_name_you),
            wonTricks = wonTricks,
            onDismiss = { }
        )
    }
}

// Maksimalna veličina karte u ruci — može biti manja ako ih ima 12 (faza bacanja)
val HAND_CARD_W = 56.dp
val HAND_CARD_H = 84.dp

@Composable
private fun CardTile(
    card: PlayingCard,
    cardW: androidx.compose.ui.unit.Dp,
    cardH: androidx.compose.ui.unit.Dp,
    selectedCards: Set<PlayingCard>,
    phase: HandPhase,
    isHumanTurn: Boolean,
    legalCards: List<PlayingCard>,
    onCardClick: (PlayingCard) -> Unit
) {
    val isSelected = card in selectedCards
    val isClickable = isHumanTurn && (phase == HandPhase.PLAYING || phase == HandPhase.DISCARDING)
    val isIllegal = phase == HandPhase.PLAYING && isHumanTurn
            && legalCards.isNotEmpty() && card !in legalCards

    Box(modifier = Modifier.then(if (isSelected) Modifier.offset(y = (-8).dp) else Modifier)) {
        CardFront(
            card = card,
            modifier = Modifier
                .size(cardW, cardH)
                .alpha(if (isIllegal) 0.4f else 1f)
                .then(
                    if (isSelected) Modifier.border(
                        2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)
                    ) else Modifier
                )
                .then(if (isClickable) Modifier.clickable { onCardClick(card) } else Modifier)
        )
    }
}


// ─── GameMessage → lokalizovani string ───────────────────────────────────

@Composable
private fun GameMessage.toLocalizedString(): String = when (this) {
    is GameMessage.Loading -> stringResource(R.string.msg_loading)
    is GameMessage.NewRound -> stringResource(R.string.msg_new_round)
    is GameMessage.YourTurnBid -> stringResource(R.string.msg_your_turn_bid)
    is GameMessage.TookTalon -> stringResource(R.string.msg_took_talon)
    is GameMessage.DeclareGamePrompt -> stringResource(R.string.msg_declare_game_prompt)
    is GameMessage.AllPassed -> stringResource(R.string.msg_all_passed)
    is GameMessage.TrickFinished -> stringResource(R.string.msg_trick_finished)
    is GameMessage.Continuing -> stringResource(R.string.msg_continuing)
    is GameMessage.YourTurn -> stringResource(R.string.msg_your_turn)
    is GameMessage.YouPlayedCard -> stringResource(R.string.msg_you_played_card)
    is GameMessage.NobodyFollows -> stringResource(R.string.msg_nobody_follows)
    is GameMessage.GameStarted -> stringResource(R.string.msg_game_started)
    is GameMessage.AskFollow -> stringResource(R.string.msg_ask_follow)
    is GameMessage.YouWonBid -> stringResource(R.string.msg_you_won_bid, value)
    is GameMessage.BotWonBid -> stringResource(R.string.msg_bot_won_bid, botPos)
    is GameMessage.BotPlaysMisere -> stringResource(R.string.msg_bot_plays_misere, name)
    is GameMessage.BotPlaysGame -> stringResource(
        R.string.msg_bot_plays_game,
        name,
        gameType.displayName()
    )

    is GameMessage.FollowersJoin -> stringResource(R.string.msg_followers_join, names)
    is GameMessage.BotPlayedCard -> stringResource(R.string.msg_bot_played_card, name)
    is GameMessage.RoundFinished -> stringResource(
        if (won) R.string.msg_round_won else R.string.msg_round_lost, name
    )

    is GameMessage.IllegalMove -> stringResource(R.string.msg_illegal_move, legalCount)
}


@Composable
private fun BidType.displayName(): String = stringResource(
    when (this) {
        BidType.SPADES -> R.string.bid_spades
        BidType.DIAMONDS -> R.string.bid_diamonds
        BidType.HEARTS -> R.string.bid_hearts
        BidType.CLUBS -> R.string.bid_clubs
        BidType.MISERE -> R.string.bid_misere
        BidType.SANS -> R.string.bid_sans
    }
)

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
