package etf.nestor.preference

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import etf.nestor.preference.ui.navigation.AppNavHost
import etf.nestor.preference.ui.theme.PreferenceTheme
import java.util.Locale
import androidx.compose.material3.Surface

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", MODE_PRIVATE)
        val lang = prefs.getString("language", "sr") ?: "sr"
        val locale = Locale.forLanguageTag(lang)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PreferenceTheme {
                Surface {
                    val navController = rememberNavController()
                    AppNavHost(navController)
                }
            }
        }
    }
}
