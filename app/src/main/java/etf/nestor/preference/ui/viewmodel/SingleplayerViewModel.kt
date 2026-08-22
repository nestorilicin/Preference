package etf.nestor.preference.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import etf.nestor.preference.domain.bidding.BidAction
import etf.nestor.preference.domain.bidding.BidType
import etf.nestor.preference.domain.game.GameSnapshot
import etf.nestor.preference.domain.game.SingleplayerFacade
import etf.nestor.preference.domain.model.PlayingCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SingleplayerViewModel @Inject constructor() : ViewModel() {

    val startScoreOptions = listOf(60, 90, 120, 150, 180, 210, 240)

    private val _selectedStartScore = MutableStateFlow(120)
    val selectedStartScore: StateFlow<Int> = _selectedStartScore.asStateFlow()

    private val _isGameStarted = MutableStateFlow(false)
    val isGameStarted: StateFlow<Boolean> = _isGameStarted.asStateFlow()

    private val _gameState = MutableStateFlow<GameSnapshot?>(null)
    val gameState: StateFlow<GameSnapshot?> = _gameState.asStateFlow()

    private var facade: SingleplayerFacade? = null

    fun selectStartScore(score: Int) {
        _selectedStartScore.value = score
    }

    fun startGame() {
        val f = SingleplayerFacade(
            playerName = "Igrač",
            startScore = _selectedStartScore.value,
            scope = viewModelScope
        )
        facade = f
        viewModelScope.launch {
            f.state.collect { snapshot ->
                _gameState.value = snapshot
            }
        }
        _isGameStarted.value = true
    }

    fun bid(action: BidAction) = facade?.playerBid(action)
    fun pickupTalon() = facade?.playerPickupTalon()
    fun discard(cards: List<PlayingCard>) = facade?.playerDiscard(cards)
    fun declareGame(gameType: BidType) = facade?.playerDeclareGame(gameType)
    fun follow(follows: Boolean) = facade?.playerFollow(follows)
    fun playCard(card: PlayingCard) = facade?.playerPlayCard(card)
}
