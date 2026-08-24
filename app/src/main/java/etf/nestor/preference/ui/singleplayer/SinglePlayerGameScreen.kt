package etf.nestor.preference.ui.singleplayer

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

// ─── Composable entry point ──────────────────────────────────────────────

enum class SingleplayerStartMode { NEW, CONTINUE }

@Composable
fun SinglePlayerGameScreen(
    startMode: SingleplayerStartMode = SingleplayerStartMode.NEW,
    viewModel: SingleplayerViewModel = hiltViewModel()
) {
    val isGameStarted by viewModel.isGameStarted.collectAsState()

    // Ako se ekranu pristupilo preko Continue, odmah učitaj sačuvanu partiju umesto
    // da se prikaže ekran za podešavanje nove partije
    LaunchedEffect(Unit) {
        if (startMode == SingleplayerStartMode.CONTINUE) {
            viewModel.continueGame()
        }
    }

    if (!isGameStarted) {
        if (startMode == SingleplayerStartMode.CONTINUE) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            SetupScreen(viewModel)
        }
    } else {
        val gameState by viewModel.gameState.collectAsState()
        val state = gameState
        if (state == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            GamePlayScreen(state = state, viewModel = viewModel)
        }
    }
}
