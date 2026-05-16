package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.model.PlayingCard

data class DealResult(val playersCards: List<List<PlayingCard>>, val talon : Talon)
