package etf.nestor.preference.domain.scoring

data class RoundOutcome(
    val gamePlayer : Int,
    val gameType: GameType,
    val multiplier: Multiplier,
    val playerResults : List<PlayerRoundResult>,
    val calledPlayer : Int? = null,
    val isRefa : Boolean = false
)
