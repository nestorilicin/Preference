package etf.nestor.preference.domain.game

import kotlinx.serialization.Serializable

@Serializable
enum class HandPhase {
    BIDDING, TALON_PICKUP, DISCARDING, TRUMP_DECLARATION, FOLLOW, PLAYING, FINISHED
}
