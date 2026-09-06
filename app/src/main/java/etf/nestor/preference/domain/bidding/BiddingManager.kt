package etf.nestor.preference.domain.bidding

class BiddingManager(firstPlayer: Int) {
    private val bids = mutableListOf<Bid>()
    private val activePlayers = mutableSetOf(0, 1, 2)
    var currentHighestBid = 1
        private set
    var currentWinner: Int? = null
        private set
    var currentPlayer = firstPlayer
        private set

    fun bid(playerPosition: Int, action: BidAction) {
        require(playerPosition == currentPlayer) {
            "Not player turn"
        }
        when {
            action.isCheck() -> pass(playerPosition)
            action.isMine() -> mine(playerPosition)
            action.isNumberBid() -> normalBid(playerPosition, action.ordinal)
            else -> {
                throw IllegalStateException("Unknown action")
            }
        }

        bids.add(Bid(playerPosition, action))
        nextPlayer()
    }

    private fun normalBid(player: Int, value: Int) {
        require(value > currentHighestBid) {
            "Bid must be bigger"
        }
        require(value < currentHighestBid + 2) {
            "Bid cannot skip values"
        }
        currentHighestBid = value
        currentWinner = player
    }

    private fun mine(player: Int) {
        currentWinner = player
    }

    private fun pass(player: Int) {
        activePlayers.remove(player)
    }

    private fun nextPlayer() {
        if (isFinished()) return
        do {
            currentPlayer = (currentPlayer + 1) % 3
        } while (!activePlayers.contains(currentPlayer))
    }

    fun isFinished(): Boolean {
        if (activePlayers.isEmpty()) {
            return true
        }
        return activePlayers.size == 1 && currentWinner != null
    }

    fun getResult(): BiddingResult {
        return BiddingResult(currentWinner, currentHighestBid, currentWinner == null)
    }

    fun getAllBids(): List<Bid> {
        return bids.toList()
    }
}