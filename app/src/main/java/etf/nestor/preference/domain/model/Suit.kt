package etf.nestor.preference.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Suit { SPADES, DIAMONDS, HEARTS, CLUBS, NO_SUIT } // NO_SUIT is special case for Misere (Betli)
