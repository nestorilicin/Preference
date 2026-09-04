package etf.nestor.preference.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Rank(val power: Int) {
    SEVEN(7), EIGHT(8), NINE(9), TEN(10), JACK(11), QUEEN(12), KING(13), ACE(14)
}
