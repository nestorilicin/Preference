package etf.nestor.preference.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * Zajednički okvir za "stranične" ekrane (meni, podešavanja, liste...): isti top bar sa
 * naslovom i ispravno rezervisan prostor oko sistemskih traka (status bar gore, dugmad/gesture
 * traka dole) na svakom ekranu koji ga koristi. Sam ekran za igru (ScoreBar služi kao njegov
 * "header") ovo ne koristi — vidi PortraitGamePlayScreen/LandscapeGamePlayScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    title: String,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleLarge) }
            )
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}
