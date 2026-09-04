package etf.nestor.preference.ui.stateholders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import etf.nestor.preference.data.AddGameResult
import etf.nestor.preference.data.GameSaveRepository
import etf.nestor.preference.data.PlayerPreferences
import etf.nestor.preference.data.SavedGameSummary
import etf.nestor.preference.data.UploadResult
import etf.nestor.preference.data.network.PartijaSummary
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

    private val _avatarPath = MutableStateFlow(playerPreferences.getAvatarPath())
    val avatarPath: StateFlow<String?> = _avatarPath.asStateFlow()

    private val _country = MutableStateFlow(playerPreferences.getCountry())
    val country: StateFlow<String?> = _country.asStateFlow()

    private val _sheetDialogPlayer = MutableStateFlow<Int?>(null)
    val sheetDialogPlayer: StateFlow<Int?> = _sheetDialogPlayer.asStateFlow()

    // Reaktivno iz baze — radi ispravno bez obzira na to koja instanca ViewModel-a (svaki ekran
    // ima svoju, jer je hiltViewModel() vezan za NavBackStackEntry) je poslednja promenila stanje
    // automatskog backup-a (sačuvala ga, obrisala ga pri izlasku, ili ga prepisala igranjem).
    val hasSavedGame: StateFlow<Boolean> =
        gameSaveRepository.observeHasAutoBackup()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val savedGames: StateFlow<List<SavedGameSummary>> =
        gameSaveRepository.observeSavedGames()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var facade: SingleplayerFacade? = null

    // Id ručno sačuvane partije iz koje je tekuća sesija učitana (loadGame) — null ako je nova
    // partija ili nastavak iz automatskog backup-a. Dok je postavljen, izlazak iz partije ne
    // prikazuje dijalog nego tiho prepiše baš tu sačuvanu partiju (vidi exitLoadedSavedGame).
    private var loadedSavedGameId: Long? = null
    val isLoadedFromSavedGame: Boolean get() = loadedSavedGameId != null

    fun selectStartScore(score: Int) {
        _selectedStartScore.value = score
    }

    fun startGame() {
        loadedSavedGameId = null
        launchFacade(
            SingleplayerFacade(
                playerName = playerPreferences.getName().ifBlank { "Igrač" },
                startScore = _selectedStartScore.value,
                scope = viewModelScope
            )
        )
    }

    fun continueGame() {
        loadedSavedGameId = null
        viewModelScope.launch {
            val saved = gameSaveRepository.loadAutoBackup()
            if (saved != null) {
                launchFacade(SingleplayerFacade(scope = viewModelScope, savedState = saved))
            } else {
                startGame()
            }
        }
    }

    fun loadGame(id: Long) {
        viewModelScope.launch {
            val saved = gameSaveRepository.loadNamed(id)
            if (saved != null) {
                loadedSavedGameId = id
                launchFacade(SingleplayerFacade(scope = viewModelScope, savedState = saved))
            }
        }
    }

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

    /** Vraća JSON sadržaj partije za export u fajl (npr. da se upiše u Uri iz file pickera). */
    fun exportSavedGame(id: Long, onResult: (json: String?) -> Unit) {
        viewModelScope.launch { onResult(gameSaveRepository.exportToJson(id)) }
    }

    /** Uvozi partiju iz sadržaja JSON fajla (SUCCESS / DUPLICATE ako već postoji / FAILED). */
    fun importSavedGame(jsonText: String, onResult: (AddGameResult) -> Unit) {
        viewModelScope.launch { onResult(gameSaveRepository.importFromJson(jsonText)) }
    }

    /** Otprema (upload) sačuvanu partiju na Ktor server, zaštićenu unetom lozinkom. */
    fun uploadSavedGame(id: Long, password: String, onResult: (UploadResult) -> Unit) {
        viewModelScope.launch { onResult(gameSaveRepository.uploadToServer(id, password)) }
    }

    /** Učitava listu partija sa servera (id + ime) — za prikaz u dijalogu pre preuzimanja. */
    fun listServerPartije(onResult: (List<PartijaSummary>?) -> Unit) {
        viewModelScope.launch { onResult(gameSaveRepository.listServerPartije()) }
    }

    /** Preuzima (download) sa Ktor servera partiju sa datim id-jem (SUCCESS / DUPLICATE / FAILED / SERVER_UNAVAILABLE). */
    fun downloadSavedGame(id: Int, password: String, onResult: (AddGameResult) -> Unit) {
        viewModelScope.launch { onResult(gameSaveRepository.downloadFromServer(id, password)) }
    }

    /** Igrač je svesno izabrao izlazak bez čuvanja — obriši i automatski backup, ne samo ekran. */
    fun exitWithoutSaving() {
        viewModelScope.launch { gameSaveRepository.clearAutoBackup() }
    }

    /**
     * Tiho izlazi iz partije učitane sa ekrana "Sačuvane partije" — prepisuje baš tu sačuvanu
     * partiju tekućim stanjem, bez dijaloga za čuvanje.
     */
    fun exitLoadedSavedGame() {
        val f = facade ?: return
        val id = loadedSavedGameId ?: return
        viewModelScope.launch {
            gameSaveRepository.saveOverExisting(id, f.captureSaveState())
        }
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
                    gameSaveRepository.clearAutoBackup()
                } else {
                    gameSaveRepository.saveAutoBackup(f.captureSaveState())
                }

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
