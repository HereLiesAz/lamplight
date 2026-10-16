package com.hereliesaz.lamplight

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/**
 * A single, on-demand location fix for setting the Home Lantern -- never background or
 * continuous tracking, and no Play Services Location dependency. Requests a fresh fix from every enabled provider, and falls back to the last known
 * fix if a fresh one doesn't arrive quickly (e.g. indoors, weak signal).
 *
 * Caller must already hold ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION.
 */
@SuppressLint("MissingPermission")
suspend fun requestOneTimeLocation(context: Context): Location? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        ?: return null
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        .filter { runCatching { locationManager.isProviderEnabled(it) }.getOrDefault(false) }
    if (providers.isEmpty()) return null

    val fresh = withTimeoutOrNull(8_000) {
        suspendCancellableCoroutine { continuation ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (continuation.isActive) continuation.resume(location)
                }
            }
            // Ask every enabled provider: indoors GPS often can't fix, but the network provider can.
            // First fix wins; resume() is guarded by isActive, and cancellation removes the listener.
            val requested = providers.count { provider ->
                runCatching {
                    locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                }.isSuccess
            }
            if (requested == 0 && continuation.isActive) continuation.resume(null)
            continuation.invokeOnCancellation { runCatching { locationManager.removeUpdates(listener) } }
        }
    }

    return fresh ?: providers.firstNotNullOfOrNull { provider ->
        runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
    }
}
