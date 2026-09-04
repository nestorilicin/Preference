package etf.nestor.preference.domain.scoring

import kotlinx.serialization.Serializable

@Serializable
data class ScoreSheet(
    var center : Int,
    val soups : MutableMap<Int, Int> = mutableMapOf()
)
