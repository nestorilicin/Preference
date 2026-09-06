package etf.nestor.preference.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import etf.nestor.preference.ui.stateholders.SinglePlayerViewModel

enum class SingleplayerStartMode { NEW, CONTINUE, LOAD }

@Composable
fun SinglePlayerGameScreen(
    startMode: SingleplayerStartMode = SingleplayerStartMode.NEW,
    loadId: Long? = null,
    onExit: () -> Unit = {},
    viewModel: SinglePlayerViewModel = hiltViewModel()
) {
    val isGameStarted by viewModel.isGameStarted.collectAsState()

    LaunchedEffect(Unit) {
        when (startMode) {
            SingleplayerStartMode.CONTINUE -> viewModel.continueGame()
            SingleplayerStartMode.LOAD -> loadId?.let { viewModel.loadGame(it) }
            SingleplayerStartMode.NEW -> {}
        }
    }

    if (!isGameStarted) {
        if (startMode == SingleplayerStartMode.NEW) {
            SetupScreen(viewModel)
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    } else {
        val gameState by viewModel.gameState.collectAsState()
        val state = gameState
        if (state == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            GamePlayScreen(state = state, viewModel = viewModel, onExit = onExit)
        }
    }
}
