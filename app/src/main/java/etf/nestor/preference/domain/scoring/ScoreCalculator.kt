package etf.nestor.preference.domain.scoring

class ScoreCalculator {
    fun applyRound(outcome: RoundOutcome, scoreSheets: MutableMap<Int, ScoreSheet>) {
        val roundValue = outcome.gameType.value * 2 * outcome.multiplier.value
        

    }
}