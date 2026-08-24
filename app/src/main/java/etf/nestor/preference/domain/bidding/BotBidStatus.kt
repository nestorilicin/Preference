package etf.nestor.preference.domain.bidding

/**
 * Status koji bot "govori" tokom licitacije — prikazuje se kao tekst pored bota u UI-u.
 * Postoji samo tokom faze licitacije (HandPhase.BIDDING).
 */
sealed class BotBidStatus {
    object Thinking : BotBidStatus()
    object Pass : BotBidStatus()

    // Specijalan slucaj: bot pasira odmah nakon sto je covek licitirao minimalnih 2 —
    // umesto "Dalje" kaze "Dobar"
    object Good : BotBidStatus()
}
