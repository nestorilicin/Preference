package etf.nestor.preference.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MenuScreen(
    onSingleplayerClick: () -> Unit,
    onMultiplayerClick: () -> Unit,
    onSettingsClick: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(
            onClick = onSingleplayerClick
        ) {
            Text("Singleplayer")
        }

        Button(
            onClick = onMultiplayerClick
        ) {
            Text("Multiplayer")
        }

        Button(
            onClick = onSettingsClick
        ) {
            Text("Settings")
        }
    }
}