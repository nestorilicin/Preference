package etf.nestor.preference.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import etf.nestor.preference.R
import etf.nestor.preference.domain.game.GameMessage
import etf.nestor.preference.domain.game.GameSnapshot
import etf.nestor.preference.domain.game.HandPhase

@Composable
fun GameMessage.toLocalizedString(): String = when (this) {
    is GameMessage.Loading -> stringResource(R.string.msg_loading)
    is GameMessage.NewRound -> stringResource(R.string.msg_new_round, dealerName, firstBidderName)
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
    is GameMessage.BotTakesTalon -> stringResource(R.string.msg_bot_takes_talon, name)
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

    is GameMessage.BotAskedToBid -> stringResource(R.string.msg_bot_asked_bid, name)
    is GameMessage.BotPasses -> stringResource(R.string.msg_bot_passes, name)
    is GameMessage.BotSaysGood -> stringResource(R.string.msg_bot_says_good, name)

    is GameMessage.BotAskedToFollow -> stringResource(R.string.msg_bot_asked_bid, name)
    is GameMessage.BotFollows -> stringResource(R.string.msg_bot_follows, name)

    is GameMessage.GameOver -> stringResource(R.string.msg_game_over)
}

@Composable
fun GameSnapshot.actionMessageText(selectedCardsCount: Int): String {
    if (isHumanTurn) {
        when (phase) {
            HandPhase.BIDDING -> return stringResource(R.string.label_bid)
            HandPhase.DISCARDING -> return stringResource(R.string.label_discard, selectedCardsCount)
            HandPhase.TRUMP_DECLARATION -> return stringResource(R.string.label_declare_game, minimumBidValue)
            HandPhase.FOLLOW -> return stringResource(R.string.label_follow)
            HandPhase.PLAYING -> return stringResource(R.string.your_turn_play)
            else -> {}
        }
    }
    return message.toLocalizedString()
}
