package etf.nestor.preference.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    val languageOptions = listOf("sr", "en")

    private val prefs = application.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(
        prefs.getString("language", "sr") ?: "sr"
    )
    val language: StateFlow<String> = _language.asStateFlow()

    fun setLanguage(lang: String) {
        _language.value = lang
        prefs.edit().putString("language", lang).apply()
    }
}
