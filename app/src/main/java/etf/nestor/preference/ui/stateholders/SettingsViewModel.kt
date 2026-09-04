package etf.nestor.preference.ui.stateholders

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import etf.nestor.preference.data.PlayerPreferences
import etf.nestor.preference.data.fetchUserCountry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application, private val playerPreferences: PlayerPreferences
) : AndroidViewModel(application) {

    val languageOptions = listOf("sr", "en")

    private val prefs = application.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(
        prefs.getString("language", "sr") ?: "sr"
    )
    val language: StateFlow<String> = _language.asStateFlow()

    private val _playerName = MutableStateFlow(playerPreferences.getName())
    val playerName: StateFlow<String> = _playerName.asStateFlow()

    private val _avatarPath = MutableStateFlow(playerPreferences.getAvatarPath())
    val avatarPath: StateFlow<String?> = _avatarPath.asStateFlow()
    private val _avatarVersion = MutableStateFlow(0)
    val avatarVersion: StateFlow<Int> = _avatarVersion.asStateFlow()

    private val _country = MutableStateFlow(playerPreferences.getCountry())
    val country: StateFlow<String?> = _country.asStateFlow()

    fun setLanguage(lang: String) {
        _language.value = lang
        prefs.edit { putString("language", lang) }
    }

    fun setPlayerName(name: String) {
        _playerName.value = name
        playerPreferences.setName(name)
    }

    fun setAvatar(bitmap: Bitmap) {
        _avatarPath.value = playerPreferences.saveAvatar(bitmap)
        _avatarVersion.value++
    }

    fun refreshCountry() {
        viewModelScope.launch {
            val result = fetchUserCountry(getApplication())
            if (result != null) {
                _country.value = result
                playerPreferences.setCountry(result)
            }
        }
    }
}
