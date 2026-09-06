package etf.nestor.preference.domain.scoring

class ScoreCalculator {
    fun applyRound(outcome: RoundOutcome, scoreSheets: MutableMap<Int, ScoreSheet>) {
        val roundValue = calcRoundValue(outcome)
        applyGamePlayer(outcome, scoreSheets, roundValue)
        applyFollowers(outcome, scoreSheets, roundValue)
        applySoups(outcome, scoreSheets, roundValue)
    }

    private fun applyGamePlayer(
        outcome: RoundOutcome, scoreSheets: MutableMap<Int, ScoreSheet>, roundValue: Int
    ) {
        val sheet = scoreSheets[outcome.gamePlayer] ?: throw IllegalStateException()
        val result = outcome.playerResults.first {
            it.playerPosition == outcome.gamePlayer
        }
        if (outcome.gameType == GameType.MISERE || outcome.gameType == GameType.HAND_MISERE) {
            if (result.tricksWon == 0) sheet.center -= roundValue
            else sheet.center += roundValue
        } else {
            if (result.tricksWon >= 6) sheet.center -= roundValue
            else sheet.center += roundValue
        }
    }

    private fun applyFollowers(
        outcome: RoundOutcome, scoreSheets: MutableMap<Int, ScoreSheet>, roundValue: Int
    ) {
        if (outcome.gameType == GameType.MISERE || outcome.gameType == GameType.HAND_MISERE) return
        val defenders = outcome.playerResults.filter {
            it.playerPosition != outcome.gamePlayer
        }
        val totalDefenders = defenders.sumOf { it.tricksWon }
        defenders.forEach { defender ->
            val passed = defender.tricksWon >= 2 || totalDefenders >= 4
            if (!passed) {
                val sheet = scoreSheets[defender.playerPosition] ?: throw IllegalStateException()
                sheet.center += roundValue
            }
        }
    }

    private fun applySoups(
        outcome: RoundOutcome, scoreSheets: MutableMap<Int, ScoreSheet>, roundValue: Int
    ) {
        outcome.playerResults.filter { it.playerPosition != outcome.gamePlayer }.forEach { result ->
            val soups = calculateSoups(outcome, result.tricksWon, roundValue)
            val sheet = scoreSheets[result.playerPosition] ?: throw IllegalStateException()
            val current = sheet.soups[outcome.gamePlayer] ?: 0
            sheet.soups[outcome.gamePlayer] = current + soups
        }
    }

    private fun calculateSoups(
        outcome: RoundOutcome, tricksWon: Int, roundValue: Int
    ): Int {
        return when (outcome.gameType) {
            GameType.MISERE -> {
                if (outcome.playerResults[outcome.gamePlayer].tricksWon > 0) 60
                else 0
            }

            GameType.HAND_MISERE -> {
                if (outcome.playerResults[outcome.gamePlayer].tricksWon > 0) 70
                else 0
            }

            else -> tricksWon * roundValue
        }
    }

    private fun calcRoundValue(outcome: RoundOutcome): Int {
        return outcome.gameType.value * 2 * outcome.multiplier.value
    }
}