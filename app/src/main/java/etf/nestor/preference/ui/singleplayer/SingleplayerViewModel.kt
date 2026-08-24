package etf.nestor.preference.ui.singleplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import etf.nestor.preference.data.GameSaveRepository
import etf.nestor.preference.data.PlayerPreferences
import etf.nestor.preference.domain.bidding.BidAction
import etf.nestor.preference.domain.bidding.BidType
import etf.nestor.preference.domain.game.GameMessage
import etf.nestor.preference.domain.game.GameSnapshot
import etf.nestor.preference.domain.game.SingleplayerFacade
import etf.nestor.preference.domain.model.PlayingCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SingleplayerViewModel @Inject constructor(
    private val playerPreferences: PlayerPreferences,
    private val gameSaveRepository: GameSaveRepository
) : ViewModel() {

    val startScoreOptions = listOf(4, 30, 60, 90, 120, 150, 180, 210, 240)

    private val _selectedStartScore = MutableStateFlow(120)
    val selectedStartScore: StateFlow<Int> = _selectedStartScore.asStateFlow()

    private val _isGameStarted = MutableStateFlow(false)
    val isGameStarted: StateFlow<Boolean> = _isGameStarted.asStateFlow()

    private val _gameState = MutableStateFlow<GameSnapshot?>(null)
    val gameState: StateFlow<GameSnapshot?> = _gameState.asStateFlow()

    // Koja igračeva tabela poena je otvorena u dijalogu (null = nijedna)
    private val _sheetDialogPlayer = MutableStateFlow<Int?>(null)
    val sheetDialogPlayer: StateFlow<Int?> = _sheetDialogPlayer.asStateFlow()

    private var facade: SingleplayerFacade? = null

    /** Da li postoji sačuvana partija koja može da se nastavi (Continue). */
    val hasSavedGame: Boolean
        get() = gameSaveRepository.hasSavedGame()

    fun selectStartScore(score: Int) {
        _selectedStartScore.value = score
    }

    /** Počinje potpuno novu partiju (briše prethodnu sačuvanu partiju, ako postoji). */
    fun startGame() {
        launchFacade(
            SingleplayerFacade(
                playerName = playerPreferences.getName().ifBlank { "Igrač" },
                startScore = _selectedStartScore.value,
                scope = viewModelScope
            )
        )
    }

    /** Nastavlja poslednju sačuvanu partiju. Ako sačuvana partija ne postoji, počinje novu. */
    fun continueGame() {
        val saved = gameSaveRepository.load()
        if (saved != null) {
            launchFacade(SingleplayerFacade(scope = viewModelScope, savedState = saved))
        } else {
            startGame()
        }
    }

    private fun launchFacade(f: SingleplayerFacade) {
        facade = f
        _sheetDialogPlayer.value = null
        viewModelScope.launch {
            f.state.collect { snapshot ->
                _gameState.value = snapshot

                if (snapshot.gameOver) {
                    // Partija je gotova — nema šta da se nastavlja, obriši sačuvanu partiju
                    gameSaveRepository.clear()
                } else {
                    // Partija se čuva posle svake promene stanja — tako da, bez obzira na koji
                    // način korisnik izađe iz igre (nazad, gašenje aplikacije...), Continue uvek
                    // vraća poslednje poznato stanje.
                    gameSaveRepository.save(f.captureSaveState())
                }

                // Čim se ruka završi, ako je igrač učestvovao u njoj (bio deklarant ili se
                // pridružio kao pratilac), automatski mu otvori tabelu poena
                if (snapshot.message is GameMessage.RoundFinished) {
                    val humanPlayed = snapshot.declarer == SingleplayerFacade.HUMAN ||
                        snapshot.followStatus[SingleplayerFacade.HUMAN] == true
                    if (humanPlayed) _sheetDialogPlayer.value = SingleplayerFacade.HUMAN
                }
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

    fun openScoreSheet(position: Int) {
        _sheetDialogPlayer.value = position
    }

    fun closeScoreSheet() {
        _sheetDialogPlayer.value = null
    }
}
