package etf.nestor.preference.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import etf.nestor.preference.R

@Composable
fun ExitGameDialog(
    onDismiss: () -> Unit,
    onExitWithoutSaving: () -> Unit,
    onSaveAndExit: (name: String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.exit_game_title)) },
        text = {
            Column {
                Text(stringResource(R.string.exit_game_message))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.save_name_label)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
                TextButton(onClick = onExitWithoutSaving) {
                    Text(stringResource(R.string.btn_exit_without_saving))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSaveAndExit(name.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.btn_save_and_exit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
