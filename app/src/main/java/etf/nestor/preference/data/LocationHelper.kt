package etf.nestor.preference.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

suspend fun fetchUserCountry(context: Context): String? {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
    ) {
        return null
    }

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val location = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        .mapNotNull { runCatching { locationManager.getLastKnownLocation(it) }.getOrNull() }
        .maxByOrNull { it.time }
        ?: return null

    if (!Geocoder.isPresent()) return null

    return withContext(Dispatchers.IO) {
        try {
            @Suppress("DEPRECATION")
            Geocoder(context, Locale.getDefault())
                .getFromLocation(location.latitude, location.longitude, 1)
                ?.firstOrNull()
                ?.countryName
        } catch (_: Exception) {
            null
        }
    }
}
