package etf.nestor.preference.ui.screen

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import etf.nestor.preference.R
import etf.nestor.preference.ui.screen.composables.SimpleSpinner
import etf.nestor.preference.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val language by viewModel.language.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Text(text = stringResource(R.string.settings_language))

        SimpleSpinner(
            value = language,
            options = viewModel.languageOptions,
            displayText = { lang ->
                when (lang) {
                    "sr" -> "Srpski"
                    "en" -> "English"
                    else -> lang
                }
            },
            onSelect = { selectedLang ->
                viewModel.setLanguage(selectedLang)
                (context as? Activity)?.recreate()
            },
            modifier = Modifier.fillMaxWidth()
        )
        Button (onClick = onBack) {
            Text(stringResource(R.string.btn_back))
        }
    }
}


