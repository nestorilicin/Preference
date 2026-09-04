package etf.nestor.preference.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayedCard(
    val playerPosition: Int, val card: PlayingCard
)
