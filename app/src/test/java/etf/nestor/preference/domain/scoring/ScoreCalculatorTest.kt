package etf.nestor.preference.domain.scoring

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ScoreCalculatorTest {

    private fun sheets(): MutableMap<Int, ScoreSheet> {
        return mutableMapOf(
            0 to ScoreSheet(center = 100),
            1 to ScoreSheet(center = 100),
            2 to ScoreSheet(center = 100)
        )
    }

    private fun baseOutcome(
        gamePlayer: Int = 0,
        gameType: GameType = GameType.SPADES,
        multiplier: Multiplier = Multiplier.NONE,
        p0: Int = 6,
        p1: Int = 2,
        p2: Int = 2
    ): RoundOutcome {

        return RoundOutcome(
            gamePlayer = gamePlayer,
            gameType = gameType,
            multiplier = multiplier,
            playerResults = listOf(
                PlayerRoundResult(0, p0),
                PlayerRoundResult(1, p1),
                PlayerRoundResult(2, p2)
            )
        )
    }

    @Test
    fun game_player_passes_reduces_center() {
        val calc = ScoreCalculator()
        val sheets = sheets()

        val outcome = baseOutcome(
            gamePlayer = 0,
            gameType = GameType.CLUBS,
            p0 = 6
        )

        calc.applyRound(outcome, sheets)

        assertTrue(sheets[0]!!.center < 100)
    }

    @Test
    fun game_player_fails_increases_center() {
        val calc = ScoreCalculator()
        val sheets = sheets()

        val outcome = baseOutcome(
            gamePlayer = 0,
            gameType = GameType.CLUBS,
            p0 = 5,
            p1 = 3,
            p2 = 2
        )

        calc.applyRound(outcome, sheets)

        assertTrue(sheets[0]!!.center > 100)
    }

    @Test
    fun follower_pays_center_when_fails() {
        val calc = ScoreCalculator()
        val sheets = sheets()

        val outcome = baseOutcome(
            gamePlayer = 0,
            gameType = GameType.CLUBS,
            p0 = 6,
            p1 = 1,
            p2 = 1
        )

        calc.applyRound(outcome, sheets)

        assertTrue(sheets[1]!!.center > 100)
        assertTrue(sheets[2]!!.center > 100)
    }

    @Test
    fun follower_does_not_pay_if_collective_pass() {
        val calc = ScoreCalculator()
        val sheets = sheets()

        val outcome = baseOutcome(
            gamePlayer = 0,
            gameType = GameType.CLUBS,
            p0 = 6,
            p1 = 2,
            p2 = 2
        )

        calc.applyRound(outcome, sheets)

        assertEquals(100, sheets[1]!!.center)
        assertEquals(100, sheets[2]!!.center)
    }

    @Test
    fun soups_are_calculated_for_normal_game() {
        val calc = ScoreCalculator()
        val sheets = sheets()

        val outcome = baseOutcome(
            gamePlayer = 0,
            gameType = GameType.SPADES,
            p0 = 6,
            p1 = 3,
            p2 = 3
        )

        calc.applyRound(outcome, sheets)

        val soup12 = sheets[1]!!.soups[0]!!
        val soup22 = sheets[2]!!.soups[0]!!

        assertTrue(soup12 > 0)
        assertTrue(soup22 > 0)
    }

    @Test
    fun soups_accumulate_correctly() {
        val calc = ScoreCalculator()
        val sheets = sheets()

        val outcome = baseOutcome()

        calc.applyRound(outcome, sheets)
        calc.applyRound(outcome, sheets)

        assertTrue(sheets[1]!!.soups[0]!! > 0)
    }

    @Test
    fun multiplier_affects_center() {
        val calc = ScoreCalculator()
        val sheets = sheets()

        val outcome = baseOutcome(
            gameType = GameType.HEARTS,
            multiplier = Multiplier.COUNTER
        )

        calc.applyRound(outcome, sheets)

        assertTrue(sheets[0]!!.center != 100)
    }

    @Test
    fun misere_does_not_apply_follower_rules() {
        val calc = ScoreCalculator()
        val sheets = sheets()

        val outcome = RoundOutcome(
            gamePlayer = 0,
            gameType = GameType.MISERE,
            multiplier = Multiplier.NONE,
            playerResults = listOf(
                PlayerRoundResult(0, 0),
                PlayerRoundResult(1, 3),
                PlayerRoundResult(2, 3)
            )
        )

        calc.applyRound(outcome, sheets)

        assertEquals(100, sheets[1]!!.center)
        assertEquals(100, sheets[2]!!.center)
    }
    @Test
    fun misere_fail_increases_center() {

        val calc = ScoreCalculator()
        val sheets = sheets()

        val outcome = RoundOutcome(
            gamePlayer = 0,
            gameType = GameType.MISERE,
            multiplier = Multiplier.NONE,
            playerResults = listOf(
                PlayerRoundResult(0, 1),
                PlayerRoundResult(1, 5),
                PlayerRoundResult(2, 4)
            )
        )

        calc.applyRound(outcome, sheets)

        assertTrue(sheets[0]!!.center > 100)
    }

    @Test
    fun hand_misere_fail_gives_70_soups() {

        val calc = ScoreCalculator()
        val sheets = sheets()

        val outcome = RoundOutcome(
            gamePlayer = 0,
            gameType = GameType.HAND_MISERE,
            multiplier = Multiplier.NONE,
            playerResults = listOf(
                PlayerRoundResult(0, 1),
                PlayerRoundResult(1, 5),
                PlayerRoundResult(2, 4)
            )
        )

        calc.applyRound(outcome, sheets)

        assertEquals(70, sheets[1]!!.soups[0])
        assertEquals(70, sheets[2]!!.soups[0])
    }

    @Test
    fun counter_doubles_center_value() {

        val calc = ScoreCalculator()
        val sheets = sheets()

        val outcome = baseOutcome(
            gamePlayer = 0,
            gameType = GameType.DIAMONDS,
            multiplier = Multiplier.COUNTER,
            p0 = 6,
            p1 = 2,
            p2 = 2
        )

        calc.applyRound(outcome, sheets)

        assertEquals(88, sheets[0]!!.center)
    }

    @Test
    fun counter_doubles_soups() {

        val calc = ScoreCalculator()
        val sheets = sheets()

        val outcome = baseOutcome(
            gamePlayer = 0,
            gameType = GameType.SPADES,
            multiplier = Multiplier.COUNTER,
            p0 = 6,
            p1 = 3,
            p2 = 1
        )

        calc.applyRound(outcome, sheets)

        assertEquals(24, sheets[1]!!.soups[0])
        assertEquals(8, sheets[2]!!.soups[0])
    }

    @Test
    fun defender_with_zero_tricks_passes_if_team_has_four() {

        val calc = ScoreCalculator()
        val sheets = sheets()

        val outcome = baseOutcome(
            gamePlayer = 0,
            gameType = GameType.CLUBS,
            p0 = 6,
            p1 = 0,
            p2 = 4
        )

        calc.applyRound(outcome, sheets)

        assertEquals(100, sheets[1]!!.center)
        assertEquals(100, sheets[2]!!.center)
    }
}