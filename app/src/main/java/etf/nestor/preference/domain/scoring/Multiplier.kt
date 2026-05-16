package etf.nestor.preference.domain.scoring

enum class Multiplier(val value: Int) {
    NONE(1),
    COUNTER(2),
    RE_COUNTER(4),
    RE_RECOUNTER(8),
    RE_RE_RECOUNTER(16)
}