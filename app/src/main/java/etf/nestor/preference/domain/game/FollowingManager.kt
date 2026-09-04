package etf.nestor.preference.domain.game

class FollowingManager(declarerPosition: Int) {

    val rightPlayer: Int = (declarerPosition + 1) % 3
    val leftPlayer: Int = (declarerPosition + 2) % 3

    private val decisions = mutableMapOf<Int, Boolean>()

    val currentPlayer: Int
        get() = if (!decisions.containsKey(rightPlayer)) rightPlayer else leftPlayer

    val isFinished: Boolean
        get() = decisions.size == 2

    fun decide(playerPosition: Int, follows: Boolean) {
        require(playerPosition == currentPlayer) { "Nije red igracu $playerPosition da odluci" }
        decisions[playerPosition] = follows
    }

    fun getFollowers(): List<Int> = decisions.filter { it.value }.keys.toList()

    fun nobodyFollows(): Boolean = isFinished && decisions.values.none { it }

    fun getDecision(playerPosition: Int): Boolean? = decisions[playerPosition]
}
