package com.example.treasurehunt.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.treasurehunt.game.GameEngine
import com.example.treasurehunt.model.AccuracyMode
import com.example.treasurehunt.model.GeoPoint
import com.example.treasurehunt.model.LocationSnapshot
import com.example.treasurehunt.model.TuningConfig
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
/**
 *  Reid Pettibone
 *  CS 492
 *  OSU
 * */
class LocationTracker(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _location = MutableStateFlow<LocationSnapshot?>(null)
    val location: StateFlow<LocationSnapshot?> = _location.asStateFlow()

    private var locationCallback: LocationCallback? = null
    private var currentBand: DistanceBand? = null
    private var target: GeoPoint? = null
    private var tuning: TuningConfig? = null
    private var accuracyMode: AccuracyMode = AccuracyMode.UNAVAILABLE

    fun isLocationServicesEnabled(): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun hasFinePermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    fun hasCoarsePermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    fun stopTracking() {
        locationCallback?.let(fusedLocationClient::removeLocationUpdates)
        locationCallback = null
        currentBand = null
        target = null
    }

    @SuppressLint("MissingPermission")
    fun beginTracking(target: GeoPoint?, accuracyMode: AccuracyMode, tuning: TuningConfig) {
        stopTracking()
        this.target = target
        this.tuning = tuning
        this.accuracyMode = accuracyMode
        if (target == null || !hasCoarsePermission()) return

        val band = currentBandFor(distanceMeters = _location.value?.let { snapshot ->
            GameEngine.distanceMeters(
                start = GeoPoint(snapshot.latitude, snapshot.longitude),
                end = target
            )
        }, tuning = tuning)
        currentBand = band
        val request = buildRequest(band, accuracyMode)
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val latest = result.lastLocation ?: return
                val snapshot = latest.toSnapshot()
                _location.value = snapshot
                maybeRestartTracking(snapshot)
            }
        }
        locationCallback = callback
        fusedLocationClient.requestLocationUpdates(
            request,
            callback,
            Looper.getMainLooper()
        )
    }

    @SuppressLint("MissingPermission")
    suspend fun requestCurrentLocation(): LocationSnapshot? {
        if (!hasCoarsePermission()) return null
        val request = CurrentLocationRequest.Builder()
            .setDurationMillis(4_000)
            .setMaxUpdateAgeMillis(1_000)
            .setPriority(
                if (accuracyMode == AccuracyMode.PRECISE && hasFinePermission()) {
                    Priority.PRIORITY_HIGH_ACCURACY
                } else {
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY
                }
            )
            .build()
        val tokenSource = CancellationTokenSource()
        val location = fusedLocationClient.getCurrentLocation(request, tokenSource.token).await()
        val snapshot = location?.toSnapshot()
        if (snapshot != null) {
            _location.value = snapshot
        }
        return snapshot
    }

    private fun maybeRestartTracking(snapshot: LocationSnapshot) {
        val currentTarget = target ?: return
        val currentTuning = tuning ?: return
        val distance = GameEngine.distanceMeters(
            start = GeoPoint(snapshot.latitude, snapshot.longitude),
            end = currentTarget
        )
        val nextBand = currentBandFor(distance, currentTuning)
        if (nextBand != currentBand) {
            beginTracking(currentTarget, accuracyMode, currentTuning)
        }
    }

    private fun buildRequest(band: DistanceBand, accuracyMode: AccuracyMode): LocationRequest {
        val priority = if (accuracyMode == AccuracyMode.PRECISE && hasFinePermission()) {
            if (band == DistanceBand.NEAR) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }
        val intervalMillis = when (band) {
            DistanceBand.FAR -> tuning?.farUpdateIntervalMillis ?: 10_000L
            DistanceBand.MID -> tuning?.midUpdateIntervalMillis ?: 4_000L
            DistanceBand.NEAR -> tuning?.nearUpdateIntervalMillis ?: 1_500L
        }
        return LocationRequest.Builder(priority, intervalMillis)
            .setMinUpdateIntervalMillis(intervalMillis / 2)
            .build()
    }

    private fun currentBandFor(distanceMeters: Float?, tuning: TuningConfig): DistanceBand {
        if (distanceMeters == null) return DistanceBand.FAR
        return when {
            distanceMeters <= tuning.nearDistanceMeters -> DistanceBand.NEAR
            distanceMeters <= tuning.midDistanceMeters -> DistanceBand.MID
            else -> DistanceBand.FAR
        }
    }

    private fun Location.toSnapshot(): LocationSnapshot = LocationSnapshot(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracy,
        timestampMillis = time
    )
}

private enum class DistanceBand {
    FAR,
    MID,
    NEAR
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { error -> continuation.resumeWithException(error) }
    addOnCanceledListener { continuation.cancel() }
}
