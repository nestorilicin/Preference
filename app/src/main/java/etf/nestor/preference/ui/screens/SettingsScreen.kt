package etf.nestor.preference.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import etf.nestor.preference.R
import etf.nestor.preference.ui.composables.SimpleSpinner
import etf.nestor.preference.ui.composables.rememberCameraLauncher
import etf.nestor.preference.ui.stateholders.SettingsViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val language by viewModel.language.collectAsState()
    val playerName by viewModel.playerName.collectAsState()
    val avatarPath by viewModel.avatarPath.collectAsState()
    val country by viewModel.country.collectAsState()
    val context = LocalContext.current

    val takePhoto = rememberCameraLauncher { bitmap -> viewModel.setAvatar(bitmap) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.refreshCountry() }

    LaunchedEffect(Unit) {
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasLocationPermission) viewModel.refreshCountry()
    }

    AppScreen(title = stringResource(R.string.settings_title)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(text = stringResource(R.string.settings_player_name))

            OutlinedTextField(
                value = playerName,
                onValueChange = { viewModel.setPlayerName(it) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
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

            Text(text = stringResource(R.string.settings_avatar))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val avatarBitmap = remember(avatarPath) {
                    avatarPath?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() }
                }
                if (avatarBitmap != null) {
                    Image(
                        bitmap = avatarBitmap,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                }

                OutlinedButton(onClick = takePhoto) {
                    Text(
                        stringResource(
                            if (avatarBitmap != null) R.string.btn_retake_photo
                            else R.string.btn_take_photo
                        )
                    )
                }
            }

            Text(text = stringResource(R.string.settings_location))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = country ?: stringResource(R.string.location_unknown),
                    style = MaterialTheme.typography.bodyLarge
                )
                OutlinedButton(onClick = {
                    val hasLocationPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasLocationPermission) {
                        viewModel.refreshCountry()
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }
                }) {
                    Text(stringResource(R.string.btn_refresh_location))
                }
            }

            Button(onClick = onBack) {
                Text(stringResource(R.string.btn_back))
            }
        }
    }
}
