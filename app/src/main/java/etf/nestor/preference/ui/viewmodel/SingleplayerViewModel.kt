package etf.nestor.preference.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import etf.nestor.preference.domain.game.GameEngine
import etf.nestor.preference.domain.game.GameState
import etf.nestor.preference.domain.game.HandPhase
import etf.nestor.preference.domain.game.HandState
import etf.nestor.preference.domain.model.PlayedCard
import etf.nestor.preference.domain.model.Player
import javax.inject.Inject

@HiltViewModel
class SingleplayerViewModel @Inject constructor() : ViewModel(){
    private lateinit var gameState: GameState
    private lateinit var engine: GameEngine

    private val _uiState = mutableStateOf<HandState?>(null)
    val uiState = _uiState
    fun startGame(players : List<Player>){
        val hand = HandState(players, firstPlayer = 0)
        gameState = GameState(players, 0)
        gameState.startHand(hand)
        engine = GameEngine(hand)
        _uiState.value = hand
    }
    fun playCard(player: Int, card : PlayedCard){
        engine.playTurn(card)
        _uiState.value = gameState.currentHand
    }
    fun finishHand(){
        gameState.currentHand?.moveToPhase(HandPhase.FINISHED)
        gameState.finishHand()
        _uiState.value = null
    }
    fun currentHand() : HandState? = gameState.currentHand

}