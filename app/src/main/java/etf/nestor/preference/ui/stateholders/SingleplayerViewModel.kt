package etf.nestor.preference.ui.stateholders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import etf.nestor.preference.data.GameSaveRepository
import etf.nestor.preference.data.PlayerPreferences
import etf.nestor.preference.data.SavedGameSummary
import etf.nestor.preference.domain.bidding.BidAction
import etf.nestor.preference.domain.bidding.BidType
import etf.nestor.preference.domain.game.GameMessage
import etf.nestor.preference.domain.game.GameSnapshot
import etf.nestor.preference.domain.game.SingleplayerFacade
import etf.nestor.preference.domain.model.PlayingCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

    // Profilna slika i država igrača — postavljaju se u podešavanjima, prikazuju se u ScoreBar-u
    private val _avatarPath = MutableStateFlow(playerPreferences.getAvatarPath())
    val avatarPath: StateFlow<String?> = _avatarPath.asStateFlow()

    private val _country = MutableStateFlow(playerPreferences.getCountry())
    val country: StateFlow<String?> = _country.asStateFlow()

    // Koja igračeva tabela poena je otvorena u dijalogu (null = nijedna)
    private val _sheetDialogPlayer = MutableStateFlow<Int?>(null)
    val sheetDialogPlayer: StateFlow<Int?> = _sheetDialogPlayer.asStateFlow()

    // Da li postoji automatski backup koji može da se nastavi (Continue) — puni se asinhrono,
    // jer je Room baza sad izvor istine umesto sinhronih SharedPreferences
    private val _hasSavedGame = MutableStateFlow(false)
    val hasSavedGame: StateFlow<Boolean> = _hasSavedGame.asStateFlow()

    // Lista ručno sačuvanih partija (sa imenom koje je igrač uneo) — za ekran "Sačuvane partije"
    val savedGames: StateFlow<List<SavedGameSummary>> =
        gameSaveRepository.observeSavedGames()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var facade: SingleplayerFacade? = null

    init {
        viewModelScope.launch {
            _hasSavedGame.value = gameSaveRepository.hasAutoBackup()
        }
    }

    fun selectStartScore(score: Int) {
        _selectedStartScore.value = score
    }

    /** Počinje potpuno novu partiju. */
    fun startGame() {
        launchFacade(
            SingleplayerFacade(
                playerName = playerPreferences.getName().ifBlank { "Igrač" },
                startScore = _selectedStartScore.value,
                scope = viewModelScope
            )
        )
    }

    /** Nastavlja partiju iz automatskog backup-a. Ako ne postoji, počinje novu. */
    fun continueGame() {
        viewModelScope.launch {
            val saved = gameSaveRepository.loadAutoBackup()
            if (saved != null) {
                launchFacade(SingleplayerFacade(scope = viewModelScope, savedState = saved))
            } else {
                startGame()
            }
        }
    }

    /** Učitava ručno sačuvanu partiju po njenom id-u (bira se na ekranu "Sačuvane partije"). */
    fun loadGame(id: Long) {
        viewModelScope.launch {
            val saved = gameSaveRepository.loadNamed(id)
            if (saved != null) {
                launchFacade(SingleplayerFacade(scope = viewModelScope, savedState = saved))
            }
        }
    }

    /** Ručno čuva tekuću partiju pod unetim imenom (pri izlasku) — ne dira automatski backup. */
    fun saveNamed(name: String) {
        val f = facade ?: return
        val finalName = name.ifBlank { defaultSaveName() }
        viewModelScope.launch {
            gameSaveRepository.saveNamed(finalName, f.captureSaveState())
        }
    }

    fun deleteSavedGame(id: Long) {
        viewModelScope.launch { gameSaveRepository.deleteSavedGame(id) }
    }

    private fun defaultSaveName(): String =
        SimpleDateFormat("dd.MM. HH:mm", Locale.getDefault()).format(Date())

    private fun launchFacade(f: SingleplayerFacade) {
        facade = f
        _sheetDialogPlayer.value = null
        viewModelScope.launch {
            f.state.collect { snapshot ->
                _gameState.value = snapshot

                if (snapshot.gameOver) {
                    // Partija je gotova — nema šta da se nastavlja, obriši automatski backup
                    gameSaveRepository.clearAutoBackup()
                } else {
                    // Partija se čuva posle svake promene stanja — tako da, bez obzira na koji
                    // način korisnik izađe iz igre (nazad, gašenje aplikacije...), Continue uvek
                    // vraća poslednje poznato stanje.
                    gameSaveRepository.saveAutoBackup(f.captureSaveState())
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
