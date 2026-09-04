package etf.nestor.preference.ui.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import etf.nestor.preference.R

/**
 * Dijalog za unos igračevog imena pri prvom pokretanju aplikacije.
 * Ne može da se zatvori dok se ne unese ime.
 */
@Composable
fun PlayerNameDialog(onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { },
        title = { Text(stringResource(R.string.enter_name_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text(stringResource(R.string.player_name_label)) }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.btn_confirm))
            }
        }
    )
}
