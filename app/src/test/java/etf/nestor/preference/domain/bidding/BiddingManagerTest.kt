package etf.nestor.preference.domain.bidding

import etf.nestor.preference.domain.bidding.BidAction
import etf.nestor.preference.domain.bidding.BiddingManager
import org.junit.Test

class BiddingManagerTest {
    @Test
    fun highest_bid_updates_correctly(){
        val manager = BiddingManager(0)
        manager.bid(0, BidAction.TWO)
        manager.bid(1, BidAction.THREE)
        assert(manager.currentHighestBid == 3)
        assert(manager.currentWinner == 1)
    }
    @Test
    fun pass_test(){
        val manager = BiddingManager(0)
        manager.bid(0, BidAction.TWO)
        manager.bid(1, BidAction.PASS)
        manager.bid(2, BidAction.PASS)
        assert(manager.isFinished())
        assert(manager.currentWinner == 0)
    }
    @Test
    fun mine_transfers_winner_correctly() {
        val manager = BiddingManager(0)
        manager.bid(0, BidAction.TWO)
        manager.bid(1, BidAction.THREE)
        manager.bid(2, BidAction.PASS)
        manager.bid(0, BidAction.MINE)
        assert(manager.currentWinner == 0)
        assert(manager.currentHighestBid == 3)
    }
    @Test(expected = IllegalArgumentException::class)
    fun smaller_bid_throws_exception() {
        val manager = BiddingManager(0)
        manager.bid(0, BidAction.TWO)
        manager.bid(1, BidAction.THREE)
        manager.bid(2, BidAction.TWO)
    }
    @Test(expected = IllegalArgumentException::class)
    fun much_bigger_bid_throws_exception(){
        val manager = BiddingManager(0)
        manager.bid(0, BidAction.SEVEN)
    }
    @Test(expected = IllegalArgumentException::class)
    fun youngest_player_cannot_say_mine() {
        val manager = BiddingManager(0)
        manager.bid(0, BidAction.TWO)
        manager.bid(1, BidAction.THREE)
        manager.bid(2, BidAction.MINE)
    }

    @Test
    fun everyone_passes() {
        val manager = BiddingManager(0)
        manager.bid(0, BidAction.PASS)
        manager.bid(1, BidAction.PASS)
        manager.bid(2, BidAction.PASS)
        val result = manager.getResult()
        assert(result.everyonePassed)
        assert(result.winner == null)
    }

    @Test
    fun current_player_rotates_correctly() {
        val manager = BiddingManager(1)
        assert(manager.currentPlayer == 1)
        manager.bid(1, BidAction.TWO)
        assert(manager.currentPlayer == 2)
        manager.bid(2, BidAction.PASS)
        assert(manager.currentPlayer == 0)
    }
}