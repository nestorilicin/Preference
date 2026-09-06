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
import etf.nestor.preference.domain.game.SinglePlayerFacade
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
class SinglePlayerViewModel @Inject constructor(
    private val playerPreferences: PlayerPreferences,
    private val gameSaveRepository: GameSaveRepository
) : ViewModel() {

    val startScoreOptions = listOf(30, 60, 90, 120, 150, 180, 210, 240)

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

    val hasSavedGame: StateFlow<Boolean> =
        gameSaveRepository.observeHasAutoBackup()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val savedGames: StateFlow<List<SavedGameSummary>> =
        gameSaveRepository.observeSavedGames()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var facade: SinglePlayerFacade? = null
    private var loadedSavedGameId: Long? = null
    val isLoadedFromSavedGame: Boolean get() = loadedSavedGameId != null

    fun selectStartScore(score: Int) {
        _selectedStartScore.value = score
    }

    fun startGame() {
        loadedSavedGameId = null
        launchFacade(
            SinglePlayerFacade(
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
                launchFacade(SinglePlayerFacade(scope = viewModelScope, savedState = saved))
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
                launchFacade(SinglePlayerFacade(scope = viewModelScope, savedState = saved))
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

    fun exportSavedGame(id: Long, onResult: (json: String?) -> Unit) {
        viewModelScope.launch { onResult(gameSaveRepository.exportToJson(id)) }
    }

    fun importSavedGame(jsonText: String, onResult: (AddGameResult) -> Unit) {
        viewModelScope.launch { onResult(gameSaveRepository.importFromJson(jsonText)) }
    }

    fun uploadSavedGame(id: Long, password: String, onResult: (UploadResult) -> Unit) {
        viewModelScope.launch { onResult(gameSaveRepository.uploadToServer(id, password)) }
    }

    fun listServerGames(onResult: (List<PartijaSummary>?) -> Unit) {
        viewModelScope.launch { onResult(gameSaveRepository.listServerPartije()) }
    }

    fun downloadSavedGame(id: Int, password: String, onResult: (AddGameResult) -> Unit) {
        viewModelScope.launch { onResult(gameSaveRepository.downloadFromServer(id, password)) }
    }

    fun exitWithoutSaving() {
        viewModelScope.launch { gameSaveRepository.clearAutoBackup() }
    }

    fun exitLoadedSavedGame() {
        val f = facade ?: return
        val id = loadedSavedGameId ?: return
        viewModelScope.launch {
            gameSaveRepository.saveOverExisting(id, f.captureSaveState())
        }
    }

    private fun defaultSaveName(): String =
        SimpleDateFormat("dd.MM. HH:mm", Locale.getDefault()).format(Date())

    private fun launchFacade(f: SinglePlayerFacade) {
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
                    val humanPlayed = snapshot.declarer == SinglePlayerFacade.HUMAN ||
                            snapshot.followStatus[SinglePlayerFacade.HUMAN] == true
                    if (humanPlayed) _sheetDialogPlayer.value = SinglePlayerFacade.HUMAN
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
