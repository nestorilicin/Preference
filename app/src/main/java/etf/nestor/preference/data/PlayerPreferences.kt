package etf.nestor.preference.data

import android.app.Application
import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

/**
 * Perzistira ime igrača (SharedPreferences) — postavlja se pri prvom pokretanju aplikacije
 * i može da se promeni u podešavanjima. Koristi ga i SettingsViewModel i SingleplayerViewModel.
 */
@Singleton
class PlayerPreferences @Inject constructor(
    application: Application
) {
    private val prefs = application.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun getName(): String = prefs.getString(KEY_NAME, "") ?: ""

    fun setName(name: String) {
        prefs.edit { putString(KEY_NAME, name) }
    }

    companion object {
        private const val KEY_NAME = "player_name"
    }
}
