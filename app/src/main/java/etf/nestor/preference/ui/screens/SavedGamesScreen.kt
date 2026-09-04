package etf.nestor.preference.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import etf.nestor.preference.R
import etf.nestor.preference.data.AddGameResult
import etf.nestor.preference.data.UploadResult
import etf.nestor.preference.data.network.PartijaSummary
import etf.nestor.preference.ui.composables.PasswordDialog
import etf.nestor.preference.ui.composables.ServerPartijeDialog
import etf.nestor.preference.ui.stateholders.SingleplayerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedGamesScreen(
    onBack: () -> Unit,
    onLoad: (Long) -> Unit,
    viewModel: SingleplayerViewModel = hiltViewModel()
) {
    val savedGames by viewModel.savedGames.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy. HH:mm", Locale.getDefault()) }
    val context = LocalContext.current

    val exportSuccessMsg = stringResource(R.string.msg_export_success)
    val exportFailedMsg = stringResource(R.string.msg_export_failed)
    val importSuccessMsg = stringResource(R.string.msg_import_success)
    val importFailedMsg = stringResource(R.string.msg_import_failed)
    val importDuplicateMsg = stringResource(R.string.msg_import_duplicate)
    val uploadSuccessMsg = stringResource(R.string.msg_upload_success)
    val uploadFailedMsg = stringResource(R.string.msg_upload_failed)
    val uploadDuplicateMsg = stringResource(R.string.msg_upload_duplicate)
    val downloadSuccessMsg = stringResource(R.string.msg_download_success)
    val downloadFailedMsg = stringResource(R.string.msg_download_failed)
    val downloadDuplicateMsg = stringResource(R.string.msg_download_duplicate)
    val serverUnavailableMsg = stringResource(R.string.msg_server_unavailable)

    // Id partije čiji export/upload je u toku (postavlja se pre pokretanja file pickera/dijaloga,
    // koristi se kad stigne rezultat).
    var pendingExportId by remember { mutableStateOf<Long?>(null) }
    var pendingUploadId by remember { mutableStateOf<Long?>(null) }
    var serverPartije by remember { mutableStateOf<List<PartijaSummary>?>(null) }
    var pendingDownload by remember { mutableStateOf<PartijaSummary?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val id = pendingExportId
        pendingExportId = null
        if (uri == null || id == null) return@rememberLauncherForActivityResult
        viewModel.exportSavedGame(id) { json ->
            val success = json != null && try {
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                true
            } catch (_: Exception) {
                false
            }
            Toast.makeText(context, if (success) exportSuccessMsg else exportFailedMsg, Toast.LENGTH_SHORT).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val jsonText = try {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        } catch (_: Exception) {
            null
        }
        if (jsonText == null) {
            Toast.makeText(context, importFailedMsg, Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        viewModel.importSavedGame(jsonText) { result ->
            val message = when (result) {
                AddGameResult.SUCCESS -> importSuccessMsg
                AddGameResult.DUPLICATE -> importDuplicateMsg
                AddGameResult.FAILED, AddGameResult.SERVER_UNAVAILABLE -> importFailedMsg
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    if (pendingUploadId != null) {
        PasswordDialog(
            title = stringResource(R.string.upload_password_title),
            onDismiss = { pendingUploadId = null },
            onConfirm = { password ->
                val id = pendingUploadId
                pendingUploadId = null
                if (id != null) {
                    viewModel.uploadSavedGame(id, password) { result ->
                        val message = when (result) {
                            UploadResult.SUCCESS -> uploadSuccessMsg
                            UploadResult.DUPLICATE -> uploadDuplicateMsg
                            UploadResult.FAILED -> uploadFailedMsg
                            UploadResult.SERVER_UNAVAILABLE -> serverUnavailableMsg
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // Prvo se prikaže lista partija sa servera (samo imena, bez lozinki)...
    serverPartije?.let { list ->
        ServerPartijeDialog(
            partije = list,
            onDismiss = { serverPartije = null },
            onSelect = { partija ->
                serverPartije = null
                pendingDownload = partija
            }
        )
    }

    // ...pa se tek za izabranu partiju traži lozinka.
    pendingDownload?.let { partija ->
        PasswordDialog(
            title = stringResource(R.string.download_password_title),
            onDismiss = { pendingDownload = null },
            onConfirm = { password ->
                pendingDownload = null
                viewModel.downloadSavedGame(partija.id, password) { result ->
                    val message = when (result) {
                        AddGameResult.SUCCESS -> downloadSuccessMsg
                        AddGameResult.DUPLICATE -> downloadDuplicateMsg
                        AddGameResult.FAILED -> downloadFailedMsg
                        AddGameResult.SERVER_UNAVAILABLE -> serverUnavailableMsg
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    AppScreen(title = stringResource(R.string.saved_games_title)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.btn_import_game))
                }
                OutlinedButton(
                    onClick = {
                        viewModel.listServerPartije { list ->
                            if (list != null) {
                                serverPartije = list
                            } else {
                                Toast.makeText(context, serverUnavailableMsg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.btn_download_game))
                }
            }

            Spacer(Modifier.height(16.dp))

            if (savedGames.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_saved_games),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(savedGames, key = { it.id }) { save ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(save.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = dateFormat.format(Date(save.savedAt)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                // Dugmad u dve kolone (Učitaj/Izvezi levo, Obriši/Pošalji desno),
                                // svaka kolona centrirana — umesto reda razvučenog preko cele širine.
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    TextButton(onClick = { onLoad(save.id) }) {
                                        Text(stringResource(R.string.btn_load_game))
                                    }
                                    TextButton(onClick = {
                                        pendingExportId = save.id
                                        val fileName = save.name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
                                        exportLauncher.launch("$fileName.json")
                                    }) {
                                        Text(stringResource(R.string.btn_export_game))
                                    }
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    TextButton(onClick = { viewModel.deleteSavedGame(save.id) }) {
                                        Text(stringResource(R.string.btn_delete_game))
                                    }
                                    TextButton(onClick = { pendingUploadId = save.id }) {
                                        Text(stringResource(R.string.btn_upload_game))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = onBack) {
                Text(stringResource(R.string.btn_back))
            }
        }
    }
}
