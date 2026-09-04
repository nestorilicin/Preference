package etf.nestor.preference.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayingCard(val suit: Suit, val rank: Rank)
