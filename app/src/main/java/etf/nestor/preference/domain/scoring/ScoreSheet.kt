package etf.nestor.preference.domain.scoring

data class ScoreSheet(
    var center : Int,
    val soups : MutableMap<Int, Int> = mutableMapOf()
)
