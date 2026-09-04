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
 * Dijalog za unos lozinke — koristi se i za otpremanje (upload) partije na server (bira se nova
 * lozinka) i za preuzimanje (download) partije sa servera (unosi se lozinka pod kojom je sačuvana).
 */
@Composable
fun PasswordDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (password: String) -> Unit
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                singleLine = true,
                label = { Text(stringResource(R.string.password_label)) }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(password.trim()) },
                enabled = password.isNotBlank()
            ) {
                Text(stringResource(R.string.btn_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
