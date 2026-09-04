package etf.nestor.preference.data

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.edit
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerPreferences @Inject constructor(
    private val application: Application
) {
    private val prefs = application.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun getName(): String = prefs.getString(KEY_NAME, "") ?: ""

    fun setName(name: String) {
        prefs.edit { putString(KEY_NAME, name) }
    }

    fun getAvatarPath(): String? = prefs.getString(KEY_AVATAR_PATH, null)

    fun saveAvatar(bitmap: Bitmap): String {
        val file = File(application.filesDir, AVATAR_FILE_NAME)
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out) }
        prefs.edit { putString(KEY_AVATAR_PATH, file.absolutePath) }
        return file.absolutePath
    }

    fun getCountry(): String? = prefs.getString(KEY_COUNTRY, null)

    fun setCountry(country: String) {
        prefs.edit { putString(KEY_COUNTRY, country) }
    }

    companion object {
        private const val KEY_NAME = "player_name"
        private const val KEY_AVATAR_PATH = "avatar_path"
        private const val KEY_COUNTRY = "country"
        private const val AVATAR_FILE_NAME = "avatar.jpg"
    }
}
