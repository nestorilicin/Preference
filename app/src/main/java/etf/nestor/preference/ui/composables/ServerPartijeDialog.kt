package etf.nestor.preference.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import etf.nestor.preference.R
import etf.nestor.preference.data.network.PartijaSummary

/**
 * Dijalog sa listom partija sačuvanih na serveru (samo id i ime, bez lozinki) — igrač bira
 * partiju koju želi da preuzme, nakon čega se traži lozinka (vidi [PasswordDialog]).
 */
@Composable
fun ServerPartijeDialog(
    partije: List<PartijaSummary>,
    onDismiss: () -> Unit,
    onSelect: (PartijaSummary) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.server_partije_title)) },
        text = {
            if (partije.isEmpty()) {
                Text(stringResource(R.string.no_server_partije))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    partije.forEach { partija ->
                        TextButton(
                            onClick = { onSelect(partija) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(partija.name ?: partija.id.toString(), modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_close))
            }
        }
    )
}
